import uuid
import pytest
from playwright.sync_api import expect

@pytest.mark.e2e
def test_account_crud(logged_in_page):
    page = logged_in_page
    page.get_by_test_id("nav-accounts").click()
    expect(page.get_by_test_id("account-form")).to_be_visible()

    suffix = uuid.uuid4().hex[:6]
    nickname = f"테스트 계정 {suffix}"
    email = f"{suffix}@example.test"
    page.get_by_placeholder("계정 별칭").fill(nickname)
    page.get_by_placeholder("로그인 이메일").fill(email)
    page.get_by_role("button", name="계정 추가").click()
    row = page.locator("tbody tr").filter(has_text=nickname).first
    expect(row).to_be_visible()

    row.get_by_role("button", name="수정").click()
    page.get_by_placeholder("메모 (선택)").fill("수정된 메모")
    page.get_by_role("button", name="계정 수정").click()
    expect(page.locator("tbody tr").filter(has_text=nickname).first).to_contain_text("수정된 메모")

    page.locator("tbody tr").filter(has_text=nickname).first.get_by_role("button", name="삭제").click()
    expect(page.locator("tbody tr").filter(has_text=nickname)).to_have_count(0)
