# DataForge — Excel Data Automation Toolkit

**Portfolio demo · Python · Excel-compatible CSV · Data Quality · Reporting**

Batch-process multiple Excel-exported CSV files, normalize inconsistent columns, reject invalid dates and numbers, identify duplicate order IDs, and produce clean outputs and a browser-friendly HTML report.

## Features

- Handles different column names and UTF-8 BOM from Excel exports.
- Uses `Decimal` to calculate money and rejects malformed rows without silently dropping them.
- Rejects duplicate order IDs across input files; keeps an auditable rejection CSV.
- Generates cleaned CSV, category summary, JSON KPIs and standalone HTML report.
- No cloud accounts, API keys or external Python packages required.

## Quick start

```bash
cd 01-excel-data-automation
python dataforge.py samples/orders_oct.csv samples/orders_nov.csv --output outputs
python -m unittest discover -s tests -v
```

Open `outputs/report.html` in a browser. Open CSV outputs in Excel or WPS (UTF-8 BOM is included). A **pre-generated demonstration workbook** is available at `outputs/DataForge_Dashboard.xlsx`; the CLI itself produces CSV/HTML/JSON, not XLSX.

**Scope:** CSV input/export is deliberately used for portability. This demo does not edit XLSX workbooks in place, run macros, or replace Excel's calculation engine.

## Hand-off and customization

Adapt `ALIASES` to the client's schema and change grouping dimensions in `run`. Replace synthetic input samples with authorized customer exports. Do not commit client data.

**Portfolio disclosure:** self-initiated demonstration project; not a paid client engagement. All example data is synthetic.
