from concurrent.futures import ThreadPoolExecutor
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest

from app.services.retrain import RetrainingBusy, _retraining_lock


class RetrainingLockTests(unittest.TestCase):
    def test_blocks_threads_and_releases_after_failure(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / ".retrain.lock"

            def acquire():
                with _retraining_lock(path):
                    pass

            with self.assertRaisesRegex(RuntimeError, "training failed"):
                with _retraining_lock(path):
                    with ThreadPoolExecutor(max_workers=1) as executor:
                        with self.assertRaises(RetrainingBusy):
                            executor.submit(acquire).result(timeout=10)
                    raise RuntimeError("training failed")
            acquire()

    def test_blocks_other_process_and_allows_it_after_release(self):
        script = """
from pathlib import Path
import sys
from app.services.retrain import RetrainingBusy, _retraining_lock
try:
    with _retraining_lock(Path(sys.argv[1])):
        pass
except RetrainingBusy:
    sys.exit(2)
"""
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / ".retrain.lock"

            def run_child():
                return subprocess.run(
                    [sys.executable, "-c", script, str(path)],
                    cwd=Path(__file__).resolve().parents[1],
                    capture_output=True,
                    text=True,
                    timeout=30,
                )

            with _retraining_lock(path):
                child = run_child()
                self.assertEqual(child.returncode, 2, child.stderr)
            child = run_child()
            self.assertEqual(child.returncode, 0, child.stderr)


if __name__ == "__main__":
    unittest.main()
