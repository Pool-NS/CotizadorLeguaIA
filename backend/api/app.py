"""Local API for structured quote interpretation with Gemini."""

from __future__ import annotations

import json
import logging
import os
from typing import Literal

import httpx
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field, ValidationError


class InterpretationRequest(BaseModel):
    requirement: str = Field(min_length=3, max_length=2000)


class InterpretationResponse(BaseModel):
    service: Literal["Carpas", "Toldos", "Tapizado de moto"] | None = None
    length_m: float | None = None
    width_m: float | None = None
    material: str | None = None
    motorcycle_type: str | None = None
    tent_type: Literal["ABIERTA", "CERRADA"] | None = None
    windows: int | None = None
    doors: int | None = None
    missing_fields: list[str] = Field(default_factory=list)
    ambiguities: list[str] = Field(default_factory=list)
    review_required: bool = True


RESPONSE_SCHEMA = {
    "type": "OBJECT",
    "properties": {
        "service": {"type": "STRING", "nullable": True, "enum": ["Carpas", "Toldos", "Tapizado de moto"]},
        "length_m": {"type": "NUMBER", "nullable": True},
        "width_m": {"type": "NUMBER", "nullable": True},
        "material": {"type": "STRING", "nullable": True},
        "motorcycle_type": {"type": "STRING", "nullable": True},
        "tent_type": {"type": "STRING", "nullable": True, "enum": ["ABIERTA", "CERRADA"]},
        "windows": {"type": "INTEGER", "nullable": True},
        "doors": {"type": "INTEGER", "nullable": True},
        "missing_fields": {"type": "ARRAY", "items": {"type": "STRING"}},
        "ambiguities": {"type": "ARRAY", "items": {"type": "STRING"}},
        "review_required": {"type": "BOOLEAN"},
    },
    "required": [
        "service", "length_m", "width_m", "material", "motorcycle_type",
        "tent_type", "windows", "doors", "missing_fields", "ambiguities", "review_required",
    ],
}

SYSTEM_INSTRUCTIONS = """Eres un asistente que estructura solicitudes de cotización en español.
Extrae solo información expresamente indicada por el cliente. No inventes ni completes datos faltantes.
Cuando una solicitud diga '3 x 2' o '3 por 2', interpreta largo=3 y ancho=2, salvo que el texto indique otra cosa.
Servicios permitidos: Carpas, Toldos y Tapizado de moto. En carpas clasifica ABIERTA/CERRADA solo si se indica.
Captura material, tipo/modelo de moto, número de ventanas y puertas únicamente cuando se mencionen.
No calcules ni sugieras precios, descuentos, ventas ni consumo de stock. Señala datos ausentes y ambigüedades.
La persona cotizadora siempre debe revisar y confirmar la propuesta."""

app = FastAPI(title="Cotizador Leguía Gemini API", version="0.4.0")
logger = logging.getLogger(__name__)


def _extract_output_text(response_body: dict) -> str:
    candidates = response_body.get("candidates", [])
    for candidate in candidates:
        for part in candidate.get("content", {}).get("parts", []):
            if isinstance(part.get("text"), str):
                return part["text"]
    raise ValueError("Gemini returned no structured text.")


@app.get("/health")
async def health() -> dict[str, bool]:
    return {"ok": True, "ai_configured": bool(os.getenv("GEMINI_API_KEY"))}


@app.post("/v1/interpret", response_model=InterpretationResponse)
async def interpret(request: InterpretationRequest) -> InterpretationResponse:
    api_key = os.getenv("GEMINI_API_KEY", "").strip()
    if not api_key:
        raise HTTPException(status_code=503, detail="Gemini API is not configured.")

    model = os.getenv("GEMINI_MODEL", "gemini-3.5-flash-lite").strip()

    payload = {
        "systemInstruction": {"parts": [{"text": SYSTEM_INSTRUCTIONS}]},
        "contents": [{"role": "user", "parts": [{"text": request.requirement}]}],
        "generationConfig": {
            "responseMimeType": "application/json",
            "responseSchema": RESPONSE_SCHEMA,
            "temperature": 0.0,
        },
    }

    try:
        url = f"https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent"

        async with httpx.AsyncClient(timeout=25.0) as client:
            response = await client.post(
                url,
                headers={"x-goog-api-key": api_key},
                json=payload,
            )

        response.raise_for_status()
        parsed = json.loads(_extract_output_text(response.json()))
        result = InterpretationResponse.model_validate(parsed)

    except httpx.HTTPStatusError as exc:
        status_code = exc.response.status_code
        logger.warning("Gemini request failed with HTTP %s", status_code)
        if status_code == 401:
            raise HTTPException(status_code=503, detail="Gemini did not authenticate the configured API key.") from exc
        if status_code == 403:
            raise HTTPException(status_code=503, detail="The Gemini API key lacks permission for this project or API.") from exc
        if status_code == 400:
            raise HTTPException(status_code=400, detail="Gemini rejected the request format or generation settings.") from exc
        if status_code == 404:
            raise HTTPException(status_code=404, detail="The configured Gemini model was not found or is unavailable.") from exc
        if status_code == 429:
            raise HTTPException(status_code=429, detail="Gemini quota or rate limit reached.") from exc
        raise HTTPException(status_code=502, detail="Gemini provider request failed.") from exc
    except (httpx.RequestError, ValueError, json.JSONDecodeError, ValidationError) as exc:
        raise HTTPException(status_code=502, detail="AI provider returned an invalid interpretation.") from exc

    if result.length_m is not None and result.length_m <= 0:
        result.length_m = None
    if result.width_m is not None and result.width_m <= 0:
        result.width_m = None
    return result
