import csv
import tempfile
import unittest
from pathlib import Path
from dataforge import run, date_iso, amount


class TestDataForge(unittest.TestCase):
    def test_date_and_amount(self):
        self.assertEqual(date_iso("10/10/2026"), "2026-10-10")
        self.assertEqual(str(amount("$1,250.00")), "1250.00")

    def test_merge_validate_and_deduplicate(self):
        with tempfile.TemporaryDirectory() as folder:
            base = Path(folder)
            a = base / "a.csv"
            a.write_text("Order ID,Date,Customer,Category,Qty,Unit Price\nA1,2026-10-10, Alice ,hardware,2,12.50\nA2,invalid,Bob,Parts,1,7\n", encoding="utf-8")
            b = base / "b.csv"
            b.write_text("order_id,order_date,client,type,quantity,price\nA1,2026-10-10,Alice,Hardware,2,12.50\nB1,2026/10/09,Bob,service,3,5.00\n", encoding="utf-8")
            result = run([a, b], base / "out")
            self.assertEqual(result["valid_orders"], 2)
            self.assertEqual(result["rejected_rows"], 2)
            self.assertEqual(result["gross_revenue"], "40.00")
            self.assertTrue((base / "out" / "report.html").exists())
            with (base / "out" / "category_summary.csv").open(encoding="utf-8-sig") as f:
                self.assertEqual(len(list(csv.DictReader(f))), 2)


if __name__ == "__main__":
    unittest.main()
