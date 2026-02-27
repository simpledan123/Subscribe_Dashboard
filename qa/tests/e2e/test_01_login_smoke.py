import pytest
from playwright.sync_api import expect

@pytest.mark.e2e
def test_login_smoke(logged_in_page):
    page = logged_in_page
    expect(page.get_by_test_id("nav-dashboard")).to_be_visible()
    expect(page.get_by_test_id("logout")).to_be_visible()