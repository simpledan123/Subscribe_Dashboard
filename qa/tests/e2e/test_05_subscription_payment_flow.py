import uuid
import time
import pytest

@pytest.mark.e2e
def test_subscription_payment_flow(http, api_base_url, ensure_user_and_token):
    suffix = uuid.uuid4().hex[:6]
    account = http.post(f"{api_base_url}/accounts", json={
        "nickname": f"결제 계정 {suffix}", "email": f"pay-{suffix}@example.test", "purpose": "PERSONAL"
    }, timeout=10).json()
    service = http.post(f"{api_base_url}/services", json={
        "serviceName": f"Test Service {suffix}", "planName": "Plus", "category": "AI",
        "monthlyPrice": 12000, "yearlyPrice": 120000
    }, timeout=10).json()
    subscription_response = http.post(f"{api_base_url}/subscriptions", json={
        "accountId": account["id"], "servicePlanId": service["id"], "billingCycle": "MONTHLY",
        "benefitType": "PAID", "price": 12000, "autoRenew": True
    }, timeout=10)
    subscription_response.raise_for_status()
    subscription = subscription_response.json()
    payment = None
    for _ in range(30):
        payments_response = http.get(f"{api_base_url}/payments", timeout=10)
        payments_response.raise_for_status()
        payment = next((item for item in payments_response.json()
                        if item["subscription"]["id"] == subscription["id"]), None)
        if payment:
            break
        time.sleep(0.2)
    assert payment, "Kafka consumer가 결제 일정을 생성하지 못했습니다."
    paid_response = http.put(f"{api_base_url}/payments/{payment['id']}/paid", json={
        "paymentMethod": "QA 카드"
    }, timeout=10)
    paid_response.raise_for_status()
    assert paid_response.json()["status"] == "PAID"
