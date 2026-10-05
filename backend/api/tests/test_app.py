import asyncio
import json
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

    def raise_for_status(self) -> None:
        return None

    def json(self) -> dict:
        return self.body


class FakeAsyncClient:
    last_payload = None

    def __init__(self, timeout: float):
        self.timeout = timeout

    async def __aenter__(self):
        return self

    async def __aexit__(self, *_args):
        return False

    async def post(self, _url: str, *, headers: dict, json: dict) -> FakeResponse:
        self.__class__.last_payload = json
        return FakeResponse({"output": [{"content": [{"type": "output_text", "text": __import__("json").dumps(INTERPRETATION)}]}]})


class InterpretationApiTests(unittest.IsolatedAsyncioTestCase):
    def setUp(self) -> None:
        FakeAsyncClient.last_payload = None

    def test_rejects_when_provider_key_is_missing(self) -> None:
        with patch.dict(os.environ, {}, clear=True):
            with self.assertRaises(HTTPException) as raised:
                asyncio.run(app.interpret(app.InterpretationRequest(requirement="Carpa 3 por 2")))
        self.assertEqual(503, raised.exception.status_code)

    async def test_returns_structured_suggestion_and_never_enables_provider_storage(self) -> None:
        with patch.dict(os.environ, {"OPENAI_API_KEY": "test-secret"}, clear=True):
            with patch("app.httpx.AsyncClient", FakeAsyncClient):
                result = await app.interpret(app.InterpretationRequest(requirement="Carpa cerrada 3 por 2"))

        self.assertEqual("Carpas", result.service)
        self.assertEqual(3.0, result.length_m)
        self.assertEqual(2.0, result.width_m)
        self.assertTrue(result.review_required)
        self.assertFalse(FakeAsyncClient.last_payload["store"])
        self.assertNotIn("price", FakeAsyncClient.last_payload)

    def test_extracts_text_from_responses_api_shape(self) -> None:
        body = {"output": [{"content": [{"type": "output_text", "text": "{}"}]}]}
        self.assertEqual("{}", app._extract_output_text(body))

    def test_rejects_missing_model_output(self) -> None:
        with self.assertRaises(ValueError):
            app._extract_output_text({"output": []})


if __name__ == "__main__":
    unittest.main()
