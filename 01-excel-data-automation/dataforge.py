#!/usr/bin/env python3
"""Clean, validate, reconcile and report on Excel-exported CSV order data.

No account credentials, external services or third-party packages required.
"""
import argparse
import csv
from collections import Counter, defaultdict
from datetime import datetime
from decimal import Decimal, InvalidOperation, ROUND_HALF_UP
from html import escape
from pathlib import Path
import json

ALIASES = {
    "order_id": {"order_id", "order id", "order", "invoice id"},
    "date": {"date", "order_date", "order date"},
    "customer": {"customer", "client", "customer name"},
    "category": {"category", "product category", "type"},
    "quantity": {"quantity", "qty", "units"},
    "unit_price": {"unit_price", "unit price", "price", "unit cost"},
}
FIELDS = ["order_id", "date", "customer", "category", "quantity", "unit_price", "line_total", "source_file"]


def normalize_header(s):
    return " ".join(s.strip().lower().replace("_", " ").split())


def column_map(headers):
    mapping = {}
    for header in headers or []:
        normalized = normalize_header(header)
        matches = [field for field, names in ALIASES.items() if normalized in {normalize_header(n) for n in names}]
        if matches:
            mapping[matches[0]] = header
    missing = sorted(set(ALIASES) - set(mapping))
    if missing:
        raise ValueError("Missing required columns: " + ", ".join(missing))
    return mapping


def amount(s):
    raw = str(s).strip().replace(",", "").replace("$", "")
    result = Decimal(raw)
    if not result.is_finite() or result < 0:
        raise ValueError("must be a finite non-negative number")
    return result


def date_iso(s):
    raw = str(s).strip()
    for form in ("%Y-%m-%d", "%m/%d/%Y", "%Y/%m/%d"):
        try:
            return datetime.strptime(raw, form).date().isoformat()
        except ValueError:
            pass
    raise ValueError("unsupported date; use YYYY-MM-DD or MM/DD/YYYY")


def clean_order(record, mapping, source):
    row = {key: (record.get(col) or "").strip() for key, col in mapping.items()}
    if not row["order_id"] or not row["customer"]:
        raise ValueError("order_id and customer are required")
    row["date"] = date_iso(row["date"])
    try:
        qty = amount(row["quantity"])
        price = amount(row["unit_price"])
    except (InvalidOperation, ValueError) as exc:
        raise ValueError(f"invalid quantity or price: {exc}") from exc
    if qty != qty.to_integral_value() or qty == 0:
        raise ValueError("quantity must be a positive whole number")
    row["customer"] = " ".join(row["customer"].split())
    row["category"] = row["category"].strip().title() or "Uncategorized"
    row["quantity"] = str(qty.to_integral_value())
    row["unit_price"] = str(price.quantize(Decimal("0.01"), rounding=ROUND_HALF_UP))
    row["line_total"] = str((qty * price).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP))
    row["source_file"] = source
    return row


def write_csv(path, headers, rows):
    with path.open("w", encoding="utf-8-sig", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=headers, extrasaction="ignore")
        writer.writeheader()
        writer.writerows(rows)


def run(files, output):
    output = Path(output)
    output.mkdir(parents=True, exist_ok=True)
    valid, rejects, seen = [], [], set()
    for source in files:
        source = Path(source)
        with source.open("r", encoding="utf-8-sig", newline="") as f:
            reader = csv.DictReader(f)
            mapping = column_map(reader.fieldnames)
            for line, record in enumerate(reader, 2):
                try:
                    row = clean_order(record, mapping, source.name)
                    # An order ID is a business key: a repeated ID must be reviewed,
                    # rather than silently summing potentially duplicate invoices.
                    key = row["order_id"].casefold()
                    if key in seen:
                        raise ValueError("duplicate order_id across inputs")
                    seen.add(key)
                    valid.append(row)
                except ValueError as exc:
                    rejects.append({"source_file": source.name, "row_number": line,
                                    "order_id": record.get(mapping["order_id"], ""), "reason": str(exc)})
    totals = defaultdict(lambda: {"orders": 0, "units": 0, "revenue": Decimal(0)})
    for row in valid:
        t = totals[row["category"]]
        t["orders"] += 1
        t["units"] += int(row["quantity"])
        t["revenue"] += Decimal(row["line_total"])
    summary = [dict(category=k, orders=v["orders"], units=v["units"], revenue=f'{v["revenue"]:.2f}') for k, v in sorted(totals.items())]
    write_csv(output / "cleaned_orders.csv", FIELDS, valid)
    write_csv(output / "rejected_rows.csv", ["source_file", "row_number", "order_id", "reason"], rejects)
    write_csv(output / "category_summary.csv", ["category", "orders", "units", "revenue"], summary)
    gross = sum((Decimal(row["line_total"]) for row in valid), Decimal(0))
    metrics = {"valid_orders": len(valid), "rejected_rows": len(rejects), "gross_revenue": f"{gross:.2f}",
               "input_files": len(files), "currency": "example USD (no FX conversion)"}
    (output / "metrics.json").write_text(json.dumps(metrics, indent=2), encoding="utf-8")
    bars = "".join(f'<tr><td>{escape(r["category"])}</td><td>{r["orders"]}</td><td>{r["units"]}</td><td>{r["revenue"]}</td></tr>' for r in summary)
    html = f'''<!doctype html><html lang="en"><meta charset="utf-8"><title>DataForge Report</title>
<style>body{{font:16px system-ui;margin:4rem auto;max-width:820px;color:#16283b}}h1{{color:#174a62}}.stats{{display:flex;gap:18px}}.stat{{background:#edf4f8;padding:20px;border-radius:12px;flex:1}}.num{{font-size:28px;font-weight:700}}table{{border-collapse:collapse;width:100%;margin-top:18px}}th,td{{padding:12px;text-align:left;border-bottom:1px solid #ddd}}th{{background:#edf4f8}}small{{color:#657482}}</style>
<h1>Order Data Quality &amp; Revenue</h1><small>Demonstration data. Amounts are not client financials.</small><div class="stats">
<div class="stat"><div>Valid orders</div><div class="num">{len(valid)}</div></div>
<div class="stat"><div>Rejected rows</div><div class="num">{len(rejects)}</div></div>
<div class="stat"><div>Gross revenue</div><div class="num">{gross:.2f}</div></div></div>
<h2>Revenue by category</h2><table><thead><tr><th>Category</th><th>Orders</th><th>Units</th><th>Revenue</th></tr></thead><tbody>{bars}</tbody></table></html>'''
    (output / "report.html").write_text(html, encoding="utf-8")
    return metrics


def main():
    parser = argparse.ArgumentParser(description="Excel-compatible CSV cleaning, deduplication, and reporting")
    parser.add_argument("files", type=Path, nargs="+", help="CSV files exported from Excel")
    parser.add_argument("--output", type=Path, default=Path("outputs"))
    args = parser.parse_args()
    print(json.dumps(run(args.files, args.output), indent=2))


if __name__ == "__main__":
    main()
