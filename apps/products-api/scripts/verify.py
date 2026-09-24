#!/usr/bin/env python3
"""Run acceptance experiments in a temporary clone of committed HEAD."""
import hashlib
import json
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]


def require(condition, message):
    if not condition:
        raise RuntimeError(message)


def main():
    with tempfile.TemporaryDirectory(prefix="spring-openapi-verify-") as directory:
        checkout = Path(directory) / "checkout"
        subprocess.run(["git", "clone", "--quiet", "--no-local", str(ROOT), str(checkout)], check=True)
        spec = checkout / "build/openapi/openapi.json"
        product = checkout / "src/main/java/com/example/shop/Product.java"

        def gradle(*args):
            result = subprocess.run(["./gradlew", *args, "--console=plain"], cwd=checkout,
                                    text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
            print(result.stdout, flush=True)
            require(result.returncode == 0, "Gradle failed: " + " ".join(args))
            return result.stdout

        gradle("build", "--no-build-cache")
        baseline = spec.read_bytes()
        document = json.loads(baseline)
        require(set(document["paths"]) == {"/products", "/products/{id}", "/orders"}, "Missing paths")
        require({"Product", "Order"} <= document["components"]["schemas"].keys(), "Missing schemas")
        print("PASS: clean clone build emitted endpoints and schemas", flush=True)

        output = gradle("build")
        require(":generateOpenApi UP-TO-DATE" in output, "Expected UP-TO-DATE")
        print("PASS: unchanged build is UP-TO-DATE", flush=True)

        # Force a real execution to populate the build cache, then delete the JSON.
        gradle("generateOpenApi", "--rerun-tasks")
        require(spec.read_bytes() == baseline, "Repeated execution changed JSON")
        spec.unlink()
        output = gradle("generateOpenApi")
        require(":generateOpenApi FROM-CACHE" in output, "Expected cache restoration")
        require(spec.read_bytes() == baseline, "Restored JSON differs")
        print("PASS: deleted spec restored FROM-CACHE with identical bytes", flush=True)

        gradle("generateOpenApi", "--rerun-tasks", "--no-build-cache")
        require(spec.read_bytes() == baseline, "Uncached generation changed JSON")
        print("PASS: repeated uncached generation is byte-identical", flush=True)

        original = product.read_text()
        require("String name" in original, "Rename fixture no longer matches")
        product.write_text(original.replace("String name", "String title"))
        gradle("generateOpenApi")
        properties = json.loads(spec.read_bytes())["components"]["schemas"]["Product"]["properties"]
        require("title" in properties and "name" not in properties, "Field rename missing from spec")
        require(spec.read_bytes() != baseline, "Field rename did not change JSON")
        print("PASS: Product.name -> title changes schema", flush=True)

        product.write_text(original + "\n// A comment that does not change the API.\n")
        gradle("generateOpenApi", "--rerun-tasks", "--no-build-cache")
        require(spec.read_bytes() == baseline, "Comment-only change modified JSON")
        print("PASS: comment-only edit leaves identical JSON", flush=True)
        print("Baseline SHA-256: " + hashlib.sha256(baseline).hexdigest(), flush=True)


if __name__ == "__main__":
    main()
