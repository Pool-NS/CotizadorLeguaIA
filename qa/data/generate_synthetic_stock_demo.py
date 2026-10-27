"""Generate a reproducible, explicitly synthetic inventory/pricing demo dataset."""

from __future__ import annotations

import csv
import json
import random
from dataclasses import dataclass
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
DATA_DIR = Path(__file__).resolve().parent
SOURCE = DATA_DIR / "synthetic_quotes_200.csv"
OUTPUT_CSV = DATA_DIR / "synthetic_quote_stock_pricing_200.csv"
INVENTORY_CSV = DATA_DIR / "synthetic_material_inventory.csv"
PRICE_CSV = DATA_DIR / "synthetic_price_rules.csv"
SUMMARY_JSON = DATA_DIR / "synthetic_stock_dashboard.json"
ANDROID_ASSET = ROOT / "app" / "src" / "main" / "assets" / "synthetic_stock_dashboard.json"

SEED = 20261012
SYNTHETIC_AREA_UNIT = "m² simulado"
MOTORCYCLE_SERVICE_NAME = "Tapizado de moto"
SIZE_OPTIONS = [(2, 2), (3, 2), (3, 3), (4, 3), (5, 4), (6, 4)]
MATERIALS = {
    "Carpas": [
        {"name": "Lino económico DEMO", "unit": SYNTHETIC_AREA_UNIT, "stock": 32.0, "cost": 18.0, "rate": 95.0, "weight": "ECONOMICO"},
        {"name": "Lino liviano DEMO", "unit": SYNTHETIC_AREA_UNIT, "stock": 28.0, "cost": 22.0, "rate": 108.0, "weight": "LIVIANO"},
        {"name": "Lino pesado DEMO", "unit": SYNTHETIC_AREA_UNIT, "stock": 20.0, "cost": 29.0, "rate": 125.0, "weight": "PESADO"},
    ],
    "Toldos": [
        {"name": "Tarflex DEMO", "unit": SYNTHETIC_AREA_UNIT, "stock": 24.0, "cost": 23.0, "rate": 112.0, "weight": "ESTANDAR"},
        {"name": "Lona orillo azul DEMO", "unit": SYNTHETIC_AREA_UNIT, "stock": 22.0, "cost": 25.0, "rate": 120.0, "weight": "ESTANDAR"},
        {"name": "Lona orillo rojo DEMO", "unit": SYNTHETIC_AREA_UNIT, "stock": 18.0, "cost": 26.0, "rate": 122.0, "weight": "ESTANDAR"},
    ],
    "Tapizado": [
        {"name": "Oxford DEMO", "unit": "m² equivalente DEMO", "stock": 9.0, "cost": 38.0, "rate": 155.0, "weight": "NO_APLICA"},
    ],
}


def service_name_for_material(material: dict[str, object]) -> str:
    if material["weight"] == "NO_APLICA":
        return MOTORCYCLE_SERVICE_NAME
    if str(material["name"]).startswith("Lino"):
        return "Carpas"
    return "Toldos"


@dataclass(frozen=True)
class QuoteScenario:
    row: dict[str, str]
    service: str
    minutes: float
    final_price: float
    direct_cost: float
    contribution: float
    feasibility: str


def build_quote_scenario(row: dict[str, str], rng: random.Random) -> QuoteScenario:
    service = "Tapizado" if row["service_type"].strip().lower().startswith("tapizado") else row["service_type"].strip()
    if service not in MATERIALS:
        raise ValueError(f"Unexpected service in source: {service}")

    material = rng.choice(MATERIALS[service])
    length, width = rng.choice(SIZE_OPTIONS)
    area = float(length * width)
    tent_type = rng.choice(["ABIERTA", "CERRADA"]) if service == "Carpas" else "NO_APLICA"
    consumption_factor = {
        "Carpas": 1.55 if tent_type == "CERRADA" else 1.12,
        "Toldos": 1.10,
        "Tapizado": 0.55,
    }[service]
    required = area * consumption_factor
    stock = float(material["stock"])
    shortage = max(0.0, required - stock)
    discount_pct = float(row["discount_percentage"])
    surcharge = 1.35 if tent_type == "CERRADA" else 1.0
    tariff_base = area * float(material["rate"]) * surcharge
    discount_amount = tariff_base * discount_pct / 100.0
    final_price = tariff_base - discount_amount
    direct_cost = required * float(material["cost"])
    contribution = final_price - direct_cost
    feasibility = determine_feasibility(row["requirement_complete"] == "1", shortage, contribution)

    output_row = dict(row)
    output_row["source_synthetic_quote_amount"] = output_row.pop("quote_amount")
    output_row.update({
        "service_type": MOTORCYCLE_SERVICE_NAME if service == "Tapizado" else service,
        "length_m": str(length),
        "width_m": str(width),
        "tent_type": tent_type,
        "material_name": material["name"],
        "material_weight_variant": material["weight"],
        "material_unit": material["unit"],
        "material_consumption_factor": f"{consumption_factor:.2f}",
        "required_material_quantity": f"{required:.2f}",
        "stock_snapshot_quantity": f"{stock:.2f}",
        "material_shortage_quantity": f"{shortage:.2f}",
        "material_status": "SUFICIENTE_EN_ESCENARIO" if shortage == 0 else "FALTANTE_EN_ESCENARIO",
        "synthetic_tariff_per_m2": f"{float(material['rate']):.2f}",
        "simulated_tariff_before_discount": f"{tariff_base:.2f}",
        "simulated_discount_amount": f"{discount_amount:.2f}",
        "simulated_final_price": f"{final_price:.2f}",
        "synthetic_material_unit_cost": f"{float(material['cost']):.2f}",
        "simulated_direct_material_cost": f"{direct_cost:.2f}",
        "simulated_gross_contribution_before_labor_overhead": f"{contribution:.2f}",
        "synthetic_feasibility_result": feasibility,
        "simulation_flag": "true",
    })
    return QuoteScenario(
        row=output_row,
        service=service,
        minutes=float(row["quotation_time_minutes"]),
        final_price=final_price,
        direct_cost=direct_cost,
        contribution=contribution,
        feasibility=feasibility,
    )


def determine_feasibility(requirement_complete: bool, shortage: float, contribution: float) -> str:
    if not requirement_complete:
        return "REVISAR_DATOS"
    if shortage > 0:
        return "FALTA_STOCK"
    if contribution <= 0:
        return "REVISAR_MARGEN_DIRECTO"
    return "FACTIBLE_CON_REGLAS_SINTETICAS"


def read_source_rows() -> list[dict[str, str]]:
    with SOURCE.open(newline="", encoding="utf-8-sig") as source_file:
        rows = list(csv.DictReader(source_file))
    if len(rows) != 200:
        raise ValueError(f"Expected 200 source rows, found {len(rows)}")
    return rows


def build_scenarios(source_rows: list[dict[str, str]], rng: random.Random) -> tuple[list[QuoteScenario], dict, dict]:
    scenarios = [build_quote_scenario(row, rng) for row in source_rows]
    statuses: dict[str, int] = {}
    services: dict[str, dict[str, float | int]] = {}
    for scenario in scenarios:
        statuses[scenario.feasibility] = statuses.get(scenario.feasibility, 0) + 1
        service_stats = services.setdefault(
            scenario.service,
            {"quotes": 0, "simulated_final_total": 0.0, "material_shortages": 0},
        )
        service_stats["quotes"] = int(service_stats["quotes"]) + 1
        service_stats["simulated_final_total"] = (
            float(service_stats["simulated_final_total"]) + scenario.final_price
        )
        service_stats["material_shortages"] = int(service_stats["material_shortages"]) + int(
            scenario.row["material_status"] == "FALTANTE_EN_ESCENARIO"
        )
    return scenarios, statuses, services


def write_quote_scenarios(generated: list[dict[str, str]]) -> None:
    with OUTPUT_CSV.open("w", newline="", encoding="utf-8") as output_file:
        writer = csv.DictWriter(output_file, fieldnames=list(generated[0]))
        writer.writeheader()
        writer.writerows(generated)


def write_inventory() -> None:
    fields = ["service_type", "material_name", "material_variant", "stock_quantity", "unit", "synthetic_unit_cost", "simulation_flag"]
    with INVENTORY_CSV.open("w", newline="", encoding="utf-8") as output_file:
        writer = csv.DictWriter(output_file, fieldnames=fields)
        writer.writeheader()
        for materials in MATERIALS.values():
            for material in materials:
                writer.writerow({
                    "service_type": service_name_for_material(material),
                    "material_name": material["name"],
                    "material_variant": material["weight"],
                    "stock_quantity": f"{float(material['stock']):.2f}",
                    "unit": material["unit"],
                    "synthetic_unit_cost": f"{float(material['cost']):.2f}",
                    "simulation_flag": "true",
                })


def write_price_rules() -> None:
    fields = ["service_type", "material_name", "material_variant", "rate_per_m2", "closed_tent_multiplier", "discount_source", "simulation_flag"]
    with PRICE_CSV.open("w", newline="", encoding="utf-8") as output_file:
        writer = csv.DictWriter(output_file, fieldnames=fields)
        writer.writeheader()
        for service, materials in MATERIALS.items():
            for material in materials:
                writer.writerow({
                    "service_type": MOTORCYCLE_SERVICE_NAME if service == "Tapizado" else service,
                    "material_name": material["name"],
                    "material_variant": material["weight"],
                    "rate_per_m2": f"{float(material['rate']):.2f}",
                    "closed_tent_multiplier": "1.35" if service == "Carpas" else "1.00",
                    "discount_source": "supuesto_de_demo_no_validado",
                    "simulation_flag": "true",
                })


def build_dashboard_summary(source_rows: list[dict[str, str]], scenarios: list[QuoteScenario], statuses: dict, services: dict) -> dict:
    generated = [scenario.row for scenario in scenarios]
    times = [scenario.minutes for scenario in scenarios]
    prices = [scenario.final_price for scenario in scenarios]
    direct_costs = [scenario.direct_cost for scenario in scenarios]
    contributions = [scenario.contribution for scenario in scenarios]
    return {
        "title": "Demostración sintética de cotizaciones, precios e inventario",
        "synthetic": True,
        "seed": SEED,
        "quote_count": len(generated),
        "mean_quote_minutes": round(sum(times) / len(times), 2),
        "mean_simulated_final_price": round(sum(prices) / len(prices), 2),
        "sum_simulated_final_prices": round(sum(prices), 2),
        "mean_simulated_direct_material_cost": round(sum(direct_costs) / len(direct_costs), 2),
        "mean_simulated_contribution_before_labor_overhead": round(sum(contributions) / len(contributions), 2),
        "non_positive_direct_contribution_count": sum(value <= 0 for value in contributions),
        "conversion_rate_pct": round(100.0 * sum(int(row["converted_to_sale"]) for row in source_rows) / len(source_rows), 2),
        "feasibility_counts": statuses,
        "service_summary": services,
        "inventory": [item for materials in MATERIALS.values() for item in materials],
        "price_rules": [
            {
                "service": (MOTORCYCLE_SERVICE_NAME if service == "Tapizado" else service) + " · " + material["name"],
                "rate": material["rate"],
                "rule": "tarifa sintética por m²; carpa cerrada × 1.35 (supuesto no validado)" if service == "Carpas" else "tarifa sintética por m² (supuesto no validado)",
                "unit": "S/ por " + material["unit"],
            }
            for service, materials in MATERIALS.items()
            for material in materials
        ],
        "sample_quotes": [
            {
                "quote_id": row["quote_id"],
                "service_type": row["service_type"],
                "length_m": float(row["length_m"]),
                "width_m": float(row["width_m"]),
                "material_name": row["material_name"],
                "required_material_quantity": float(row["required_material_quantity"]),
                "stock_snapshot_quantity": float(row["stock_snapshot_quantity"]),
                "material_shortage_quantity": float(row["material_shortage_quantity"]),
                "simulated_direct_material_cost": float(row["simulated_direct_material_cost"]),
                "simulated_final_price": float(row["simulated_final_price"]),
                "simulated_gross_contribution_before_labor_overhead": float(row["simulated_gross_contribution_before_labor_overhead"]),
                "synthetic_feasibility_result": row["synthetic_feasibility_result"],
            }
            for row in generated[:8]
        ],
    }


def main() -> None:
    source_rows = read_source_rows()
    # Seeded randomness is intentional for reproducible synthetic demo data only.
    rng = random.Random(SEED)
    scenarios, statuses, services = build_scenarios(source_rows, rng)
    generated = [scenario.row for scenario in scenarios]
    write_quote_scenarios(generated)
    write_inventory()
    write_price_rules()
    summary = build_dashboard_summary(source_rows, scenarios, statuses, services)
    SUMMARY_JSON.write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    ANDROID_ASSET.parent.mkdir(parents=True, exist_ok=True)
    ANDROID_ASSET.write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Generated {len(generated)} synthetic quote scenarios")
    print(f"Feasibility counts: {json.dumps(statuses, ensure_ascii=False, sort_keys=True)}")
    print(f"Mean simulated final price: {summary['mean_simulated_final_price']}")


if __name__ == "__main__":
    main()
