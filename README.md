# Python Automation & AI Integration — Engineering Portfolio

Three **working, self-initiated demonstration projects** that show practical Python engineering skills for business automation, document processing, and REST API integration.

> **Disclosure:** these are independently created portfolio demos, not paid client deliveries. All input examples are fictional. No employer endorsements or past earnings are claimed.

| Project | Business problem | What to inspect |
| --- | --- | --- |
| [DataForge — Excel Data Automation](01-excel-data-automation/) | Spreadsheet exports are inconsistent and hard to reconcile | Schema normalization, auditable bad rows, deduplication, category KPIs, HTML report |
| [DocLens — AI Document Assistant](02-ai-document-assistant/) | Convert PDF text into searchable, structured fields | PDF parser, validated extraction candidates, opt-in LLM, JSON/Markdown deliverables |
| [FlowBridge — REST API Automation](03-api-workflow-automation/) | Synchronize paginated application data reliably | API integration, retries, checkpoint/resume, security guardrails, local fake API |

## Reproduce the demos

Requires Python 3.10+. Each project has its own `README.md`, sample inputs and `unittest` tests. DocLens requires `pypdf`; the other two are standard-library only.

```bash
cd 01-excel-data-automation
python dataforge.py samples/orders_oct.csv samples/orders_nov.csv --output outputs
python -m unittest discover -s tests -v

cd ../02-ai-document-assistant
python -m pip install -r requirements.txt
python doclens.py samples/sample_invoice.pdf --output outputs
python -m unittest discover -s tests -v

cd ../03-api-workflow-automation
python -m unittest discover -s tests -v
```

See FlowBridge's README for a two-terminal local mock API demo.

## Engineering practices demonstrated

- Predictable command-line interfaces and human-readable output files.
- Input validation, visible failure reasons, safe API token handling.
- Automated unit tests and reproducible examples.
- Clear trade-offs: demos are intentionally small and are **not** promoted as production SaaS products.

**Available for:** Python automation, Excel-export data cleanup, API integrations, lightweight AI/document pipelines and code debugging.
