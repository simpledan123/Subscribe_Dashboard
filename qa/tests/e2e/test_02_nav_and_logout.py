import re
import pytest
from playwright.sync_api import expect

@pytest.mark.e2e
def test_sidebar_nav_and_logout(logged_in_page):
    page = logged_in_page

    page.get_by_test_id("nav-tenants").click()
    expect(page).to_have_url(re.compile(r".*/tenants/?$"))

    page.get_by_test_id("nav-plans").click()
    expect(page).to_have_url(re.compile(r".*/plans/?$"))

    page.get_by_test_id("logout").click()
    # 로그아웃하면 Login 폼이 다시 보임
    expect(page.get_by_test_id("login-form")).to_be_visible()