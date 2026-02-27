import re
import uuid
import pytest
from playwright.sync_api import expect

def _parse_current_calls(text: str) -> int:
    # "1,234 / 10,000" 형태에서 앞 숫자만 파싱
    m = re.match(r"\s*([\d,]+)\s*/", text)
    if not m:
        return 0
    return int(m.group(1).replace(",", ""))

@pytest.mark.e2e
def test_usage_simulate_api_call(logged_in_page, http, api_base_url):
    suffix = uuid.uuid4().hex[:6]

    tenant_payload = {
        "companyName": f"UsageTenant-{suffix}",
        "email": f"{suffix}@usage.test",
        "phone": "010-0000-0000",
    }
    r = http.post(f"{api_base_url}/tenants", json=tenant_payload, timeout=10)
    r.raise_for_status()
    tenant_id = r.json()["id"]

    plan_payload = {
        "name": f"UsagePlan-{suffix}",
        "description": "Usage test plan",
        "monthlyPrice": 10000,
        "yearlyPrice": 99000,
        "maxApiCalls": 1000,
        "maxStorage": 100,
        "maxUsers": 5,
    }
    r = http.post(f"{api_base_url}/plans", json=plan_payload, timeout=10)
    r.raise_for_status()
    plan_id = r.json()["id"]

    # subscription도 API로 만들어서 버튼 disabled 안되게
    r = http.post(
        f"{api_base_url}/subscriptions",
        json={"tenantId": tenant_id, "planId": plan_id, "billingCycle": "MONTHLY"},
        timeout=10,
    )
    r.raise_for_status()

    page = logged_in_page
    page.get_by_test_id("nav-usages").click()
    card = page.get_by_test_id(f"usage-card-{tenant_id}")
    expect(card).to_be_visible()

    calls_span = page.get_by_test_id(f"usage-apiCalls-{tenant_id}")
    before = _parse_current_calls(calls_span.inner_text())

    page.get_by_test_id(f"usage-simulate-{tenant_id}").click()

    import time

# 호출 수가 증가할 때까지 직접 폴링(최대 10초)
deadline = time.time() + 10
now = before
while time.time() < deadline:
    now = _parse_current_calls(calls_span.inner_text())
    if now > before:
        break
    time.sleep(0.5)

assert now > before, f"API calls did not increase (before={before}, after={now})"