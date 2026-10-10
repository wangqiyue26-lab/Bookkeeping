import tempfile
import unittest
from pathlib import Path
from doclens import extract_fields, process, read_pdf

SAMPLE = Path(__file__).resolve().parents[1] / "samples" / "sample_invoice.pdf"


class TestDocLens(unittest.TestCase):
    def test_extract(self):
        text = "Invoice Number: INV-009\nInvoice Date: 2026-10-10\nVendor: Example Labs\nCustomer: Demo Co\nTotal: $120.75"
        fields = extract_fields(text)
        self.assertEqual(fields["invoice_number"], "INV-009")
        self.assertEqual(fields["total"], "120.75")
        self.assertEqual(fields["vendor"], "Example Labs")

    def test_pdf_and_report(self):
        self.assertEqual(read_pdf(SAMPLE)["pages"], 1)
        with tempfile.TemporaryDirectory() as folder:
            data = process(SAMPLE, folder)
            self.assertEqual(data["fields"]["invoice_number"], "INV-2026-104")
            self.assertEqual(data["fields"]["total"], "1240.50")
            self.assertEqual(data["mode"], "offline_rules")
            self.assertTrue((Path(folder) / "report.md").exists())

    def test_missing_fields_are_null(self):
        fields = extract_fields("Not an invoice")
        self.assertTrue(all(v is None for v in fields.values()))


if __name__ == "__main__":
    unittest.main()
