import uuid
import pytest
from playwright.sync_api import expect

@pytest.mark.e2e
def test_plan_crud(logged_in_page):
    page = logged_in_page

    page.get_by_test_id("nav-plans").click()
    expect(page.get_by_test_id("plan-form")).to_be_visible()

    plan_name = f"Pro-{uuid.uuid4().hex[:6]}"
    desc1 = "QA plan"
    desc2 = "QA plan updated"

    page.get_by_test_id("plan-name").fill(plan_name)
    page.get_by_test_id("plan-description").fill(desc1)
    page.get_by_test_id("plan-monthlyPrice").fill("10000")
    page.get_by_test_id("plan-yearlyPrice").fill("99000")
    page.get_by_test_id("plan-maxApiCalls").fill("1000")
    page.get_by_test_id("plan-maxStorage").fill("100")
    page.get_by_test_id("plan-maxUsers").fill("5")

    page.get_by_test_id("plan-submit").click()

    card = page.locator("[data-testid^='plan-card-']").filter(has_text=plan_name).first
    expect(card).to_be_visible()

    dtid = card.get_attribute("data-testid")
    assert dtid and dtid.startswith("plan-card-")
    plan_id = dtid.split("-")[-1]

    # edit
    page.get_by_test_id(f"plan-edit-{plan_id}").click()
    page.get_by_test_id("plan-description").fill(desc2)
    page.get_by_test_id("plan-submit").click()

    expect(page.get_by_test_id(f"plan-card-{plan_id}")).to_contain_text(desc2)

    # delete
    page.get_by_test_id(f"plan-delete-{plan_id}").click()
    expect(page.get_by_test_id(f"plan-card-{plan_id}")).to_have_count(0)