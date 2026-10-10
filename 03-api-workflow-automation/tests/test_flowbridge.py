import tempfile
import unittest
from pathlib import Path
from flowbridge import sync, SyncError, ensure_same_origin


class TestFlowBridge(unittest.TestCase):
    def test_pagination_resume_and_deduplication(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            base = "https://example.invalid/api/leads?page=1"
            def fake_get(url, token=None):
                if "page=1" in url:
                    return {"items": [{"id": "1", "name": "A"}, {"id": "2", "name": "B"}], "next": "/api/leads?page=2"}
                return {"items": [{"id": "2", "name": "B-updated"}, {"id": "3", "name": "C"}], "next": None}
            path, state = root / "out.jsonl", root / "state.json"
            first = sync(base, path, state, max_pages=1, fetch=fake_get)
            self.assertFalse(first["complete"])
            second = sync(base, path, state, fetch=fake_get)
            self.assertTrue(second["complete"])
            self.assertEqual(second["records"], 3)
            self.assertIn("B-updated", path.read_text())
            final = sync(base, path, state, fetch=fake_get)
            self.assertEqual(final["pages_this_run"], 0)

    def test_cross_origin_refused(self):
        with self.assertRaises(SyncError):
            ensure_same_origin("https://trusted.example/api", "https://untrusted.example/api")

    def test_invalid_records_are_rejected(self):
        with tempfile.TemporaryDirectory() as folder:
            def bad_get(url, token=None):
                return {"items": [{"status": "missing id"}], "next": None}
            with self.assertRaises(SyncError):
                sync("https://example.invalid/api", Path(folder)/"a.jsonl", Path(folder)/"b.json", fetch=bad_get)


if __name__ == "__main__":
    unittest.main()
