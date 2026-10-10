# DocLens — AI Document Processing Assistant

**Portfolio demo · Python · PDF extraction · structured JSON · optional AI integration**

Extract selectable text from PDF files, identify common invoice fields, and produce an auditable JSON record plus Markdown summary. Works **offline by default**. The optional OpenAI-compatible summarization mode is opt-in and requires a separately configured API key.

## Quick start

```bash
cd 02-ai-document-assistant
python -m pip install -r requirements.txt
python doclens.py samples/sample_invoice.pdf --output outputs
python -m unittest discover -s tests -v
```

`outputs/extraction.json` includes extracted values, the mode used and missing fields. `outputs/report.md` provides a handover summary.

## Optional AI summary (only on authorized, non-sensitive documents)

```bash
export OPENAI_API_KEY="YOUR_KEY" # On Windows PowerShell: $env:OPENAI_API_KEY="YOUR_KEY"
python doclens.py samples/sample_invoice.pdf --output outputs --use-ai
```

Set `OPENAI_BASE_URL` to a trusted OpenAI-compatible HTTPS endpoint and `OPENAI_MODEL` as appropriate. Optional API usage can incur fees. **Never upload confidential client documents to a model endpoint without approval.** No keys are stored or committed to Git.

## Engineering decisions / limitations

- Local extraction uses `pypdf`; scanned/image-only PDFs need a separate OCR pipeline.
- Regex-extracted fields are **candidates** and must be verified against the original document.
- Robust API integrations should add rate-limit handling, structured output validation and security review before production deployment.
- No claim is made that this demo automates regulated financial approvals.

**Portfolio disclosure:** self-initiated demonstration project using a fictional invoice, not a paid customer case study.
