"""Authenticated-by-configuration local API for structured quote interpretation."""

from __future__ import annotations

import json
import os
from typing import Literal

import httpx
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field, ValidationError


class InterpretationRequest(BaseModel):
    requirement: str = Field(min_length=3, max_length=2000)


class InterpretationResponse(BaseModel):
    service: Literal["Carpas", "Toldos", "Tapizado de moto"] | None = None
    length_m: float | None = Field(default=None, gt=0)
    width_m: float | None = Field(default=None, gt=0)
    material: str | None = Field(default=None, max_length=100)
    motorcycle_type: str | None = Field(default=None, max_length=100)
    tent_type: Literal["ABIERTA", "CERRADA"] | None = None
    windows: int | None = Field(default=None, ge=0)
    doors: int | None = Field(default=None, ge=0)
    missing_fields: list[str] = Field(default_factory=list)
    ambiguities: list[str] = Field(default_factory=list)
    review_required: Literal[True] = True


RESPONSE_SCHEMA = {
    "type": "object",
    "additionalProperties": False,
    "properties": {
        "service": {"type": ["string", "null"], "enum": ["Carpas", "Toldos", "Tapizado de moto", None]},
        "length_m": {"type": ["number", "null"]},
        "width_m": {"type": ["number", "null"]},
        "material": {"type": ["string", "null"]},
        "motorcycle_type": {"type": ["string", "null"]},
        "tent_type": {"type": ["string", "null"], "enum": ["ABIERTA", "CERRADA", None]},
        "windows": {"type": ["integer", "null"]},
        "doors": {"type": ["integer", "null"]},
        "missing_fields": {"type": "array", "items": {"type": "string"}},
        "ambiguities": {"type": "array", "items": {"type": "string"}},
        "review_required": {"type": "boolean", "enum": [True]},
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

app = FastAPI(title="Cotizador Leguía Gemini API", version="0.2.0")


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
            "temperature": 0,
        },
    }
    try:
        async with httpx.AsyncClient(timeout=25.0) as client:
            response = await client.post(
                f"https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent",
                headers={"x-goog-api-key": api_key},
                json=payload,
            )
        response.raise_for_status()
        parsed = json.loads(_extract_output_text(response.json()))
        result = InterpretationResponse.model_validate(parsed)
    except httpx.TimeoutException as exc:
        raise HTTPException(status_code=504, detail="AI provider timed out.") from exc
    except httpx.HTTPStatusError as exc:
        # Do not return provider response bodies; they may contain sensitive diagnostics.
        if exc.response.status_code == 429:
            raise HTTPException(status_code=429, detail="Gemini quota or rate limit reached.") from exc
        if exc.response.status_code in {401, 403}:
            raise HTTPException(status_code=503, detail="Gemini API key is invalid or not authorized.") from exc
        raise HTTPException(status_code=502, detail="AI provider request failed.") from exc
    except (httpx.RequestError, ValueError, json.JSONDecodeError, ValidationError) as exc:
        raise HTTPException(status_code=502, detail="AI provider returned an invalid interpretation.") from exc

    # Discard semantically invalid dimensions rather than passing them into a quote.
    if result.length_m is not None and result.length_m <= 0:
        result.length_m = None
    if result.width_m is not None and result.width_m <= 0:
        result.width_m = None
    return result
