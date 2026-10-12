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
    "type": "object",
    "additionalProperties": False,
    "properties": {
        "service": {"type": ["string", "null"], "enum": ["Carpas", "Toldos", "Tapizado de moto"]},
        "length_m": {"type": ["number", "null"]},
        "width_m": {"type": ["number", "null"]},
        "material": {"type": ["string", "null"]},
        "motorcycle_type": {"type": ["string", "null"]},
        "tent_type": {"type": ["string", "null"], "enum": ["ABIERTA", "CERRADA"]},
        "windows": {"type": ["integer", "null"]},
        "doors": {"type": ["integer", "null"]},
        "missing_fields": {"type": "array", "items": {"type": "string"}},
        "ambiguities": {"type": "array", "items": {"type": "string"}},
        "review_required": {"type": "boolean"},
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

app = FastAPI(title="Cotizador Leguía Gemini API", version="0.3.0")


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

    model = os.getenv("GEMINI_MODEL", "gemini-1.5-flash").strip()

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
        url = f"https://googleapis.com{model}%3AgenerateContent?key={api_key}"

        async with httpx.AsyncClient(timeout=25.0) as client:
            response = await client.post(url, json=payload)

        response.raise_for_status()
        parsed = json.loads(_extract_output_text(response.json()))
        result = InterpretationResponse.model_validate(parsed)

    except httpx.HTTPStatusError as exc:
        print(f"[ERROR HTTP DIRECTO]: {exc.response.status_code} - {exc.response.text}")
        if exc.response.status_code in {401, 403}:
            raise HTTPException(status_code=503, detail="Gemini API key is invalid or unauthorized.")
        raise HTTPException(status_code=502, detail="AI provider request failed.")
    except Exception as exc:
        import traceback
        print(f"[ERROR DETALLADO DE RED]: {str(exc)}")
        traceback.print_exc()
        raise HTTPException(status_code=502, detail="AI provider returned an invalid interpretation.")

    if result.length_m is not None and result.length_m <= 0:
        result.length_m = None
    if result.width_m is not None and result.width_m <= 0:
        result.width_m = None
    return result