from app.core.security import hash_password, verify_password


def test_hash_password_returns_plain_text() -> None:
    password = "12345678"

    assert hash_password(password) == password


def test_verify_password_uses_plain_text_comparison() -> None:
    assert verify_password("12345678", "12345678") is True
    assert verify_password("wrong-password", "12345678") is False
