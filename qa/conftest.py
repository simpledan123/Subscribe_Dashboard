import os
import time
import uuid
import pathlib
import pytest
import requests
from dotenv import load_dotenv
from playwright.sync_api import expect

load_dotenv()

UI_BASE_URL = os.getenv("UI_BASE_URL", "http://localhost:5173").rstrip("/")
API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8080/api").rstrip("/")
WAIT_TIMEOUT = int(os.getenv("QA_WAIT_TIMEOUT_SEC", "60"))

QA_ROOT = pathlib.Path(__file__).parent
RESULTS_DIR = QA_ROOT / "test-results"
SCREENSHOT_DIR = RESULTS_DIR / "screenshots"

def _wait_until_ok(url: str, timeout_sec: int = 60) -> None:
    """HTTP GET이 200~499 응답을 주면 '서버가 떴다'고 보고 종료(연결 실패만 재시도)."""
    start = time.time()
    last_err = None
    while time.time() - start < timeout_sec:
        try:
            r = requests.get(url, timeout=2)
            # 200~499면 서버가 응답은 한다(404도 서버는 뜬 것)
            if 200 <= r.status_code < 500:
                return
        except Exception as e:
            last_err = e
        time.sleep(1)
    raise RuntimeError(f"Timed out waiting for server: {url} (last_err={last_err})")

def _rand(prefix: str) -> str:
    return f"{prefix}-{uuid.uuid4().hex[:8]}"

@pytest.fixture(scope="session")
def ui_base_url():
    return UI_BASE_URL

@pytest.fixture(scope="session")
def api_base_url():
    return API_BASE_URL

@pytest.fixture(scope="session", autouse=True)
def _ensure_dirs_and_wait(ui_base_url, api_base_url):
    SCREENSHOT_DIR.mkdir(parents=True, exist_ok=True)

    # API & UI 뜰 때까지 기다리기 (로컬에서 서버 먼저 켜두는 전제)
    _wait_until_ok(f"{api_base_url}/accounts", WAIT_TIMEOUT)
    _wait_until_ok(f"{ui_base_url}/", WAIT_TIMEOUT)

@pytest.fixture(scope="session")
def http(api_base_url):
    s = requests.Session()
    s.headers.update({"Content-Type": "application/json"})
    return s

@pytest.fixture
def test_user():
    # 매 테스트마다 유저를 랜덤으로 만들어서 충돌 방지
    return {"username": _rand("qauser"), "password": "Passw0rd!!"}

@pytest.fixture
def ensure_user_and_token(http, api_base_url, test_user):
    # register (이미 존재하면 실패할 수 있지만 랜덤 유저라 보통 성공)
    try:
        http.post(f"{api_base_url}/auth/register", json=test_user, timeout=5)
    except Exception:
        pass

    r = http.post(f"{api_base_url}/auth/login", json=test_user, timeout=5)
    r.raise_for_status()
    token = r.json().get("token")
    assert token, "Login succeeded but token missing"
    http.headers.update({"Authorization": f"Bearer {token}"})
    return token

@pytest.fixture
def logged_in_page(page, ui_base_url, ensure_user_and_token, test_user):
    # alert/confirm 자동 수락 (삭제 confirm 등 대비)
    page.on("dialog", lambda d: d.accept())

    page.goto(ui_base_url)

    page.get_by_test_id("login-username").fill(test_user["username"])
    page.get_by_test_id("login-password").fill(test_user["password"])
    page.get_by_test_id("login-submit").click()

    # 로그인 성공하면 sidebar가 보여야 함
    expect(page.get_by_test_id("sidebar")).to_be_visible()
    return page

@pytest.hookimpl(hookwrapper=True)
def pytest_runtest_makereport(item, call):
    """테스트 실패 시 스샷 저장 (UI 테스트만 해당)"""
    outcome = yield
    rep = outcome.get_result()

    if rep.when == "call" and rep.failed:
        page = item.funcargs.get("page")
        if page:
            fname = SCREENSHOT_DIR / f"{item.name}.png"
            try:
                page.screenshot(path=str(fname), full_page=True)
            except Exception:
                pass
