"""Generate a reproducible, explicitly synthetic inventory/pricing demo dataset."""

from __future__ import annotations

import csv
import json
import random
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
SIZE_OPTIONS = [(2, 2), (3, 2), (3, 3), (4, 3), (5, 4), (6, 4)]
MATERIALS = {
    "Carpas": [
        {"name": "Lino económico DEMO", "unit": "m² simulado", "stock": 32.0, "cost": 18.0, "rate": 95.0, "weight": "ECONOMICO"},
        {"name": "Lino liviano DEMO", "unit": "m² simulado", "stock": 28.0, "cost": 22.0, "rate": 108.0, "weight": "LIVIANO"},
        {"name": "Lino pesado DEMO", "unit": "m² simulado", "stock": 20.0, "cost": 29.0, "rate": 125.0, "weight": "PESADO"},
    ],
    "Toldos": [
        {"name": "Tarflex DEMO", "unit": "m² simulado", "stock": 24.0, "cost": 23.0, "rate": 112.0, "weight": "ESTANDAR"},
        {"name": "Lona orillo azul DEMO", "unit": "m² simulado", "stock": 22.0, "cost": 25.0, "rate": 120.0, "weight": "ESTANDAR"},
        {"name": "Lona orillo rojo DEMO", "unit": "m² simulado", "stock": 18.0, "cost": 26.0, "rate": 122.0, "weight": "ESTANDAR"},
    ],
    "Tapizado": [
        {"name": "Oxford DEMO", "unit": "m² equivalente DEMO", "stock": 9.0, "cost": 38.0, "rate": 155.0, "weight": "NO_APLICA"},
    ],
}


def main() -> None:
    rng = random.Random(SEED)
    with SOURCE.open(newline="", encoding="utf-8-sig") as source_file:
        source_rows = list(csv.DictReader(source_file))
    if len(source_rows) != 200:
        raise ValueError(f"Expected 200 source rows, found {len(source_rows)}")

    generated: list[dict[str, str]] = []
    times: list[float] = []
    prices: list[float] = []
    direct_costs: list[float] = []
    contributions: list[float] = []
    statuses: dict[str, int] = {}
    services: dict[str, dict[str, float | int]] = {}

    for row in source_rows:
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
        direct_material_cost = required * float(material["cost"])
        gross_contribution = final_price - direct_material_cost
        complete = row["requirement_complete"] == "1"
        if not complete:
            feasibility = "REVISAR_DATOS"
        elif shortage > 0:
            feasibility = "FALTA_STOCK"
        elif gross_contribution <= 0:
            feasibility = "REVISAR_MARGEN_DIRECTO"
        else:
            feasibility = "FACTIBLE_CON_REGLAS_SINTETICAS"

        output_row = dict(row)
        output_row["source_synthetic_quote_amount"] = output_row.pop("quote_amount")
        output_row.update({
            "service_type": "Tapizado de moto" if service == "Tapizado" else service,
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
            "simulated_direct_material_cost": f"{direct_material_cost:.2f}",
            "simulated_gross_contribution_before_labor_overhead": f"{gross_contribution:.2f}",
            "synthetic_feasibility_result": feasibility,
            "simulation_flag": "true",
        })
        generated.append(output_row)
        times.append(float(row["quotation_time_minutes"]))
        prices.append(final_price)
        direct_costs.append(direct_material_cost)
        contributions.append(gross_contribution)
        statuses[feasibility] = statuses.get(feasibility, 0) + 1
        service_stats = services.setdefault(service, {"quotes": 0, "simulated_final_total": 0.0, "material_shortages": 0})
        service_stats["quotes"] = int(service_stats["quotes"]) + 1
        service_stats["simulated_final_total"] = float(service_stats["simulated_final_total"]) + final_price
        service_stats["material_shortages"] = int(service_stats["material_shortages"]) + int(shortage > 0)

    fieldnames = list(generated[0])
    with OUTPUT_CSV.open("w", newline="", encoding="utf-8") as output_file:
        writer = csv.DictWriter(output_file, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(generated)

    with INVENTORY_CSV.open("w", newline="", encoding="utf-8") as output_file:
        writer = csv.DictWriter(output_file, fieldnames=["service_type", "material_name", "material_variant", "stock_quantity", "unit", "synthetic_unit_cost", "simulation_flag"])
        writer.writeheader()
        for materials in MATERIALS.values():
            for material in materials:
                writer.writerow({
                    "service_type": "Tapizado de moto" if material["weight"] == "NO_APLICA" else ("Carpas" if material["name"].startswith("Lino") else "Toldos"),
                    "material_name": material["name"],
                    "material_variant": material["weight"],
                    "stock_quantity": f"{float(material['stock']):.2f}",
                    "unit": material["unit"],
                    "synthetic_unit_cost": f"{float(material['cost']):.2f}",
                    "simulation_flag": "true",
                })

    with PRICE_CSV.open("w", newline="", encoding="utf-8") as output_file:
        writer = csv.DictWriter(output_file, fieldnames=["service_type", "material_name", "material_variant", "rate_per_m2", "closed_tent_multiplier", "discount_source", "simulation_flag"])
        writer.writeheader()
        for service, materials in MATERIALS.items():
            for material in materials:
                writer.writerow({
                    "service_type": "Tapizado de moto" if service == "Tapizado" else service,
                    "material_name": material["name"],
                    "material_variant": material["weight"],
                    "rate_per_m2": f"{float(material['rate']):.2f}",
                    "closed_tent_multiplier": "1.35" if service == "Carpas" else "1.00",
                    "discount_source": "supuesto_de_demo_no_validado",
                    "simulation_flag": "true",
                })

    summary = {
        "title": "Demostración sintética de cotizaciones, precios y materiales",
        "synthetic": True,
        "seed": SEED,
        "quote_count": len(generated),
        "mean_quote_minutes": round(sum(times) / len(times), 2),
        "mean_simulated_final_price": round(sum(prices) / len(prices), 2),
        "sum_simulated_final_prices": round(sum(prices), 2),
        "mean_simulated_direct_material_cost": round(sum(direct_costs) / len(direct_costs), 2),
        "mean_simulated_contribution_before_labor_overhead": round(sum(contributions) / len(contributions), 2),
        "non_positive_direct_contribution_count": sum(value <= 0 for value in contributions),
        "conversion_rate_pct": round(100.0 * sum(int(r["converted_to_sale"]) for r in source_rows) / len(source_rows), 2),
        "feasibility_counts": statuses,
        "service_summary": services,
        "inventory": [item for materials in MATERIALS.values() for item in materials],
        "price_rules": [
            {
                "service": ("Tapizado de moto" if service == "Tapizado" else service) + " · " + material["name"],
                "rate": material["rate"],
                "rule": "tarifa sintética por m²; carpa cerrada × 1.35 (supuesto no validado)" if service == "Carpas" else "tarifa sintética por m² (supuesto no validado)",
                "unit": "S/ por " + material["unit"],
            }
            for service, materials in MATERIALS.items()
            for material in materials
        ],
        "sample_quotes": [
            {
                "quote_id": r["quote_id"],
                "service_type": r["service_type"],
                "length_m": float(r["length_m"]),
                "width_m": float(r["width_m"]),
                "material_name": r["material_name"],
                "required_material_quantity": float(r["required_material_quantity"]),
                "stock_snapshot_quantity": float(r["stock_snapshot_quantity"]),
                "material_shortage_quantity": float(r["material_shortage_quantity"]),
                "simulated_direct_material_cost": float(r["simulated_direct_material_cost"]),
                "simulated_final_price": float(r["simulated_final_price"]),
                "simulated_gross_contribution_before_labor_overhead": float(r["simulated_gross_contribution_before_labor_overhead"]),
                "synthetic_feasibility_result": r["synthetic_feasibility_result"],
            }
            for r in generated[:8]
        ],
    }
    SUMMARY_JSON.write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    ANDROID_ASSET.parent.mkdir(parents=True, exist_ok=True)
    ANDROID_ASSET.write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Generated {len(generated)} synthetic quote scenarios")
    print(f"Feasibility counts: {json.dumps(statuses, ensure_ascii=False, sort_keys=True)}")
    print(f"Mean simulated final price: {summary['mean_simulated_final_price']}")


if __name__ == "__main__":
    main()
