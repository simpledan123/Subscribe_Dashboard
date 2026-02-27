import pytest

@pytest.mark.api
def test_api_tenants_smoke(http, api_base_url):
    r = http.get(f"{api_base_url}/tenants", timeout=5)
    assert r.status_code == 200
    assert isinstance(r.json(), list)