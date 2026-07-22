import pytest

@pytest.mark.api
def test_accounts_api_smoke(http, api_base_url, ensure_user_and_token):
    response = http.get(f"{api_base_url}/accounts", timeout=5)
    assert response.status_code == 200
    assert isinstance(response.json(), list)
