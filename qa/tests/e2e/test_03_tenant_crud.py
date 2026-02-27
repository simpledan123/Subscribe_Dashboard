import uuid
import pytest
from playwright.sync_api import expect

@pytest.mark.e2e
def test_tenant_crud(logged_in_page):
    page = logged_in_page

    page.get_by_test_id("nav-tenants").click()
    expect(page.get_by_test_id("tenant-form")).to_be_visible()

    company = f"Acme-{uuid.uuid4().hex[:6]}"
    email = f"{uuid.uuid4().hex[:6]}@acme.test"
    phone = "010-1111-2222"

    page.get_by_test_id("tenant-companyName").fill(company)
    page.get_by_test_id("tenant-email").fill(email)
    page.get_by_test_id("tenant-phone").fill(phone)
    page.get_by_test_id("tenant-submit").click()

    table = page.get_by_test_id("tenant-table")
    row = table.locator("tbody tr").filter(has_text=company).first
    expect(row).to_be_visible()

    tenant_id = row.locator("td").nth(0).inner_text().strip()

    # edit
    page.get_by_test_id(f"tenant-edit-{tenant_id}").click()
    new_phone = "010-3333-4444"
    page.get_by_test_id("tenant-phone").fill(new_phone)
    page.get_by_test_id("tenant-submit").click()

    row2 = table.locator("tbody tr").filter(has_text=company).first
    expect(row2).to_contain_text(new_phone)

    # delete
    page.get_by_test_id(f"tenant-delete-{tenant_id}").click()
    expect(page.get_by_test_id(f"tenant-row-{tenant_id}")).to_have_count(0)