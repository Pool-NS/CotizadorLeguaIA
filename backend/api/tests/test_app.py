import json as json_lib
import os
import unittest
from unittest.mock import patch

from fastapi import HTTPException

import app


INTERPRETATION = {
    "service": "Carpas",
    "length_m": 3.0,
    "width_m": 2.0,
    "material": "lona",
    "motorcycle_type": None,
    "tent_type": "CERRADA",
    "windows": 2,
    "doors": 1,
    "missing_fields": [],
    "ambiguities": [],
    "review_required": True,
}


class FakeResponse:
    def __init__(self, body: dict):
        self.body = body
        self.status_code = 200

    def raise_for_status(self) -> None:
        return None

    def json(self) -> dict:
        return self.body


class FakeAsyncClient:
    last_payload = None
    last_url = None
    last_headers = None

    def __init__(self, timeout: float):
        self.timeout = timeout

    async def __aenter__(self):
        return self

    async def __aexit__(self, *_args):
        return False

    async def post(self, url: str, *, headers: dict, json: dict) -> FakeResponse:
        self.__class__.last_payload = json
        self.__class__.last_url = url
        self.__class__.last_headers = headers
        return FakeResponse({"candidates": [{"content": {"parts": [{"text": json_lib.dumps(INTERPRETATION)}]}}]})


class InterpretationApiTests(unittest.IsolatedAsyncioTestCase):
    def setUp(self) -> None:
        FakeAsyncClient.last_payload = None
        FakeAsyncClient.last_url = None
        FakeAsyncClient.last_headers = None

    async def test_rejects_when_provider_key_is_missing(self) -> None:
        with patch.dict(os.environ, {}, clear=True):
            with self.assertRaises(HTTPException) as raised:
                await app.interpret(app.InterpretationRequest(requirement="Carpa 3 por 2"))
        self.assertEqual(503, raised.exception.status_code)

    async def test_returns_structured_suggestion_and_never_enables_provider_storage(self) -> None:
        with patch.dict(os.environ, {"GEMINI_API_KEY": "test-secret"}, clear=True):
            with patch("app.httpx.AsyncClient", FakeAsyncClient):
                result = await app.interpret(app.InterpretationRequest(requirement="Carpa cerrada 3 por 2"))

        self.assertEqual("Carpas", result.service)
        self.assertEqual(3.0, result.length_m)
        self.assertEqual(2.0, result.width_m)
        self.assertTrue(result.review_required)
        self.assertEqual("application/json", FakeAsyncClient.last_payload["generationConfig"]["responseMimeType"])
        self.assertNotIn("price", FakeAsyncClient.last_payload)
        self.assertEqual("test-secret", FakeAsyncClient.last_headers["x-goog-api-key"])
        self.assertNotIn("key=", FakeAsyncClient.last_url)

    async def test_uses_gemini_model_configured_in_environment(self) -> None:
        with patch.dict(os.environ, {"GEMINI_API_KEY": "test-secret", "GEMINI_MODEL": "gemini-test"}, clear=True):
            with patch("app.httpx.AsyncClient", FakeAsyncClient):
                result = await app.interpret(app.InterpretationRequest(requirement="Carpa 3 por 2"))

        self.assertEqual("Carpas", result.service)
        self.assertIn("/models/gemini-test:generateContent", FakeAsyncClient.last_url)

    def test_extracts_text_from_gemini_api_shape(self) -> None:
        body = {"candidates": [{"content": {"parts": [{"text": "{}"}]}}]}
        self.assertEqual("{}", app._extract_output_text(body))

    def test_rejects_missing_model_output(self) -> None:
        with self.assertRaises(ValueError):
            app._extract_output_text({"candidates": []})


if __name__ == "__main__":
    unittest.main()
