import uuid
import pytest
from playwright.sync_api import expect

@pytest.mark.e2e
def test_service_crud(logged_in_page):
    page = logged_in_page
    page.get_by_test_id("nav-services").click()
    suffix = uuid.uuid4().hex[:6]
    service = f"TestAI-{suffix}"
    page.get_by_placeholder("서비스명 (예: ChatGPT)").fill(service)
    page.get_by_placeholder("플랜명 (예: Plus)").fill("Pro")
    page.get_by_role("button", name="서비스 추가").click()
    card = page.locator("article").filter(has_text=service).first
    expect(card).to_be_visible()
    card.get_by_role("button", name="수정").click()
    page.get_by_placeholder("서비스 설명").fill("수정된 설명")
    page.get_by_role("button", name="서비스 수정").click()
    expect(page.locator("article").filter(has_text=service).first).to_contain_text("수정된 설명")
    page.locator("article").filter(has_text=service).first.get_by_role("button", name="삭제").click()
    expect(page.locator("article").filter(has_text=service)).to_have_count(0)
