import uuid
import pytest
from playwright.sync_api import expect

@pytest.mark.e2e
def test_subscription_to_invoice_flow(logged_in_page, http, api_base_url):
    # ---- setup via API (빠르고 안정적) ----
    suffix = uuid.uuid4().hex[:6]
    tenant_payload = {
        "companyName": f"Tenant-{suffix}",
        "email": f"{suffix}@tenant.test",
        "phone": "010-0000-0000",
    }
    r = http.post(f"{api_base_url}/tenants", json=tenant_payload, timeout=10)
    r.raise_for_status()
    tenant = r.json()
    tenant_id = tenant["id"]

    plan_payload = {
        "name": f"Plan-{suffix}",
        "description": "E2E plan",
        "monthlyPrice": 10000,
        "yearlyPrice": 99000,
        "maxApiCalls": 1000,
        "maxStorage": 100,
        "maxUsers": 5,
    }
    r = http.post(f"{api_base_url}/plans", json=plan_payload, timeout=10)
    r.raise_for_status()
    plan = r.json()
    plan_id = plan["id"]

    # ---- UI: create subscription ----
    page = logged_in_page
    page.get_by_test_id("nav-subs").click()
    expect(page.get_by_test_id("sub-form")).to_be_visible()

    tenant_select = page.get_by_test_id("sub-tenant")
    plan_select = page.get_by_test_id("sub-plan")

    expect(tenant_select.locator("option", has_text=tenant_payload["companyName"])).to_have_count(1)
    expect(plan_select.locator("option", has_text=plan_payload["name"])).to_have_count(1)

    tenant_select.select_option(value=str(tenant_id))
    plan_select.select_option(value=str(plan_id))
    page.get_by_test_id("sub-billingCycle").select_option(value="MONTHLY")

    page.get_by_test_id("sub-create").click()

    sub_table = page.get_by_test_id("sub-table")
    row = (
        sub_table.locator("tbody tr")
        .filter(has_text=tenant_payload["companyName"])
        .filter(has_text=plan_payload["name"])
        .first
    )
    expect(row).to_be_visible()

    sub_id = row.locator("td").nth(0).inner_text().strip()

    # ---- UI: generate + pay invoice ----
    page.get_by_test_id("nav-invoices").click()
    expect(page.get_by_test_id("invoice-form")).to_be_visible()

    inv_select = page.get_by_test_id("invoice-subscription")
    expect(inv_select.locator(f"option[value='{sub_id}']")).to_have_count(1)
    inv_select.select_option(value=str(sub_id))

    page.get_by_test_id("invoice-generate").click()

    inv_table = page.get_by_test_id("invoice-table")
    inv_row = inv_table.locator("tbody tr").filter(has_text=tenant_payload["companyName"]).first
    expect(inv_row).to_be_visible()

    pay_btn = inv_row.get_by_role("button", name="결제완료")
    expect(pay_btn).to_be_visible()
    pay_btn.click()

    # 결제 후 상태가 PAID로 바뀌고, 결제 버튼은 사라짐
    expect(inv_row).to_contain_text("PAID")
    expect(inv_row.get_by_role("button", name="결제완료")).to_have_count(0)