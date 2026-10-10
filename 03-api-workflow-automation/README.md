# FlowBridge — REST API Workflow Automation

**Portfolio demo · Python · REST APIs · pagination · reliability · local mock server**

A small, transparent ETL job that fetches paginated JSON records, validates IDs, updates records idempotently, and checkpoints progress so interrupted jobs can resume. Suitable as a starting point for inventory sync, CRM exports or operational reports.

## Run completely offline (local API server)

Open two terminals:

```bash
cd 03-api-workflow-automation
python demo_server.py
```

```bash
cd 03-api-workflow-automation
python flowbridge.py 'http://127.0.0.1:8765/api/leads?page=1' --output outputs/leads.jsonl --state outputs/state.json
python -m unittest discover -s tests -v
```

Run the client a second time: it sees the completed checkpoint and does not refetch any pages. Remove `outputs/state.json` to start a new full sync; items are upserted by ID.

## Optional real API

```bash
export API_TOKEN="YOUR_TOKEN" # On PowerShell: $env:API_TOKEN="YOUR_TOKEN"
python flowbridge.py 'https://api.yourcompany.example/v1/items'
```

The API must return `{ "items": [{"id":"..."}], "next": "/path?page=2" }`. Bearer token is read from `API_TOKEN`, never hard-coded. Pagination links must stay on the same origin to prevent credential leaks. Retries apply to transient errors and rate limits.

## Trade-offs / production hardening

For large datasets, replace the in-memory idempotent index with SQLite or a real database and add durable versioning. For cron use, add alerting, monitoring and API request quotas. The local demo purposely does not call third-party services.

**Portfolio disclosure:** self-initiated demonstration project; CRM contacts and values are fictional.
