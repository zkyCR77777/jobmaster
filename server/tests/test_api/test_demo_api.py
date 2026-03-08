from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_health_check() -> None:
    response = client.get('/health')

    assert response.status_code == 200
    assert response.json() == {'status': 'ok'}


def test_dashboard_home_returns_expected_shape() -> None:
    response = client.get('/api/v1/dashboard/home')

    assert response.status_code == 200
    body = response.json()
    assert body['code'] == 0
    assert body['data']['greeting']
    assert body['data']['hero_stats']['new_jobs_today'] >= 0
    assert len(body['data']['agent_feed']) >= 1


def test_jobs_list_returns_items() -> None:
    response = client.get('/api/v1/jobs?page=1&page_size=20')

    assert response.status_code == 200
    body = response.json()
    assert body['data']['total'] >= 1
    first_item = body['data']['items'][0]
    assert first_item['title']
    assert first_item['company']
    assert isinstance(first_item['tags'], list)


def test_deliveries_returns_queue() -> None:
    response = client.get('/api/v1/deliveries?page=1&page_size=20')

    assert response.status_code == 200
    body = response.json()
    assert body['data']['total'] >= 1
    assert body['data']['items'][0]['status'] in {
        'pending',
        'delivering',
        'delivered',
        'viewed',
        'written_test',
        'interview',
        'offer',
        'rejected',
    }


def test_company_reports_returns_list() -> None:
    response = client.get('/api/v1/company-reports?page=1&page_size=20')

    assert response.status_code == 200
    body = response.json()
    assert body['data']['total'] >= 1
    assert body['data']['items'][0]['risk_level'] in {'low', 'medium', 'high'}


def test_contract_demo_returns_summary_and_clauses() -> None:
    summary_response = client.get('/api/v1/contracts/demo')
    clauses_response = client.get('/api/v1/contracts/demo/clauses')

    assert summary_response.status_code == 200
    assert clauses_response.status_code == 200

    summary_body = summary_response.json()
    clauses_body = clauses_response.json()
    assert summary_body['data']['risk_level'] in {'safe', 'warning', 'danger'}
    assert len(clauses_body['data']['items']) >= 1
    assert clauses_body['data']['items'][0]['explanation']


def test_chat_roundtrip_persists_history() -> None:
    session_response = client.post('/api/v1/chat/sessions')
    assert session_response.status_code == 200
    session_id = session_response.json()['data']['session_id']

    send_response = client.post(
        f'/api/v1/chat/sessions/{session_id}/messages',
        json={'content': '帮我分析这份 offer', 'current_module': 'guardian'},
    )
    assert send_response.status_code == 200
    assert send_response.json()['data']['detected_module'] == 'guardian'

    history_response = client.get(f'/api/v1/chat/sessions/{session_id}/messages')
    assert history_response.status_code == 200
    messages = history_response.json()['data']['items']
    assert len(messages) >= 3
    assert messages[-2]['role'] == 'user'
    assert messages[-1]['role'] == 'assistant'
    assert messages[-1]['module'] == 'guardian'
