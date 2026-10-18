#!/usr/bin/env python3
"""Fetch SongMeld's complete icon fonts FROM THEIR PUBLISHERS and rebuild indexes.

Official sources, revision-pinned for reproducibility:
  * Google Material Symbols (Rounded / Outlined / Sharp)
  * Pictogrammers Material Design Icons
  * The public Flutter SDK Material Fonts artifact (MaterialIcons-Regular.otf)

Usage (from SongMeld repository):
  python3 -m pip install --user fonttools
  python3 tools/sync_official_icon_fonts.py
  python3 tools/sync_official_icon_fonts.py --verify

No Paisa APK, Flutter installation, Node or Git required. Python 3.10+ and
fontTools are required. Downloads are capped and font tables are validated.
The initial download records SHA-256 digests for future --verify checks; it
cannot independently attest the publishers' signatures. To change versions,
review and update the PINNED SOURCE REVISIONS below and regenerate the lock.
"""

from __future__ import annotations

import argparse
import hashlib
import io
import json
import os
from pathlib import Path
import tempfile
import time
import urllib.error
import urllib.request
import zipfile
from urllib.parse import quote

GOOGLE_REF = "49d4db35df873165d6bd6ba09b063c7dafbac2f4"
MDI_REF = "57b567a448bd579892174cd47c47f9e187ea56c6"
FLUTTER_REF = "abaf9c523780a608bd46686fd5e53740a07077f8"
# From flutter/flutter at FLUTTER_REF: bin/internal/material_fonts.version
FLUTTER_FONT_ARCHIVE = (
    "flutter_infra_release/flutter/fonts/"
    "3012db47f3130e62f7cc0beabff968a33cbec8d8/fonts.zip"
)
GOOGLE_BASE = f"https://raw.githubusercontent.com/google/material-design-icons/{GOOGLE_REF}"
MDI_BASE = f"https://raw.githubusercontent.com/Templarian/MaterialDesign-Webfont/{MDI_REF}"
FLUTTER_URL = "https://storage.googleapis.com/" + FLUTTER_FONT_ARCHIVE
REPO_ROOT = Path(__file__).resolve().parents[1]
FONT_DIR = REPO_ROOT / "app/src/main/res/font"
INDEX_DIR = REPO_ROOT / "app/src/main/assets/icon_catalog"
LICENSE_DIR = REPO_ROOT / "app/src/main/assets/licenses"
LOCK_FILE = REPO_ROOT / "tools/icon-fonts.lock.json"
MAX_DOWNLOAD = 90 * 1024 * 1024

SOURCES = {
    "material_symbols_rounded.ttf": GOOGLE_BASE + "/variablefont/" + quote("MaterialSymbolsRounded[FILL,GRAD,opsz,wght].ttf"),
    "material_symbols_outlined.ttf": GOOGLE_BASE + "/variablefont/" + quote("MaterialSymbolsOutlined[FILL,GRAD,opsz,wght].ttf"),
    "material_symbols_sharp.ttf": GOOGLE_BASE + "/variablefont/" + quote("MaterialSymbolsSharp[FILL,GRAD,opsz,wght].ttf"),
    "material_design_icons.ttf": MDI_BASE + "/fonts/materialdesignicons-webfont.ttf",
    "flutter_material_icons.otf": FLUTTER_URL + "#MaterialIcons-Regular.otf",
}
LICENSE_SOURCES = {
    "Apache-2.0-Material-Symbols.txt": GOOGLE_BASE + "/LICENSE",
    "Pictogrammers-Free-License.txt": MDI_BASE + "/LICENSE",
}


def sha256(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def fetch(url: str, *, max_bytes: int = MAX_DOWNLOAD) -> bytes:
    req = urllib.request.Request(url, headers={"User-Agent": "SongMeld-icon-sync/1.0", "Accept": "application/octet-stream"})
    error: Exception | None = None
    for attempt in range(3):
        try:
            with urllib.request.urlopen(req, timeout=45) as response:
                result = response.read(max_bytes + 1)
            if len(result) > max_bytes:
                raise ValueError(f"Download larger than {max_bytes:,} bytes: {url}")
            if not result:
                raise ValueError(f"Empty download: {url}")
            return result
        except (urllib.error.URLError, TimeoutError) as exc:
            error = exc
            if attempt < 2:
                time.sleep(attempt + 1)
    raise RuntimeError(f"Could not download from {url}: {error}")


def flutter_artifacts(data: bytes) -> tuple[bytes, bytes]:
    with zipfile.ZipFile(io.BytesIO(data)) as archive:
        names = archive.namelist()
        def one(suffix: str) -> bytes:
            matches = [n for n in names if Path(n).name == suffix and not n.endswith("/")]
            if len(matches) != 1:
                raise ValueError(f"Flutter archive: expected exactly one {suffix}, found {len(matches)}")
            item = archive.getinfo(matches[0])
            if item.file_size > 35 * 1024 * 1024:
                raise ValueError(f"Flutter archive entry unexpectedly large: {item.filename}")
            return archive.read(item)
        return one("MaterialIcons-Regular.otf"), one("MaterialIcons_LICENSE")


def cmap_from_font(data: bytes, *, symbol: bool, name: str) -> dict[int, str]:
    try:
        from fontTools.ttLib import TTFont
    except ImportError as exc:
        raise SystemExit("Missing dependency: python3 -m pip install --user fonttools") from exc
    font = TTFont(io.BytesIO(data), lazy=False)
    try:
        if symbol:
            if "fvar" not in font or "gvar" not in font:
                raise ValueError(f"{name}: not a TrueType variable font")
            axes = {axis.axisTag for axis in font["fvar"].axes}
            if not {"FILL", "wght", "GRAD", "opsz"}.issubset(axes):
                raise ValueError(f"{name}: missing Material Symbols axes: {axes}")
        mapping = font.getBestCmap() or {}
        entries = {
            cp: glyph for cp, glyph in mapping.items()
            if cp >= 0xE000 and glyph not in ("space", ".notdef")
            and not glyph.startswith("uni") and "\t" not in glyph and "\n" not in glyph
        }
        threshold = 3500 if symbol else 6500
        if len(entries) < threshold:
            raise ValueError(f"{name}: only {len(entries)} searchable cmap glyphs (expected at least {threshold})")
        return entries
    finally:
        font.close()


def compile_indexes(fonts: dict[str, bytes]) -> dict[str, bytes]:
    symbols = cmap_from_font(fonts["material_symbols_rounded.ttf"], symbol=True, name="Rounded")
    for style in ("outlined", "sharp"):
        other = cmap_from_font(fonts[f"material_symbols_{style}.ttf"], symbol=True, name=style)
        if symbols != other:
            # UI assumes identical name/codepoint mapping across all three styles.
            raise ValueError(f"Material Symbols {style} cmap differs from Rounded; choose matching upstream revisions")
    entries = {
        "symbols.tsv": symbols,
        "mdi.tsv": cmap_from_font(fonts["material_design_icons.ttf"], symbol=False, name="MDI"),
        "flutter.tsv": cmap_from_font(fonts["flutter_material_icons.otf"], symbol=False, name="Flutter"),
    }
    output = {}
    for filename, cmap in entries.items():
        data = "".join(f"{cp:X}\t{glyph}\n" for cp, glyph in sorted(cmap.items())).encode("utf-8")
        output[filename] = data
        print(f"  {filename}: {len(cmap):,} searchable icons")
    return output


def write_atomic(target: Path, data: bytes) -> None:
    target.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.NamedTemporaryFile(dir=target.parent, prefix=".icon-sync-", delete=False) as tmp:
        filename = Path(tmp.name)
        tmp.write(data)
        tmp.flush()
        os.fsync(tmp.fileno())
    try:
        os.replace(filename, target)
    finally:
        filename.unlink(missing_ok=True)


def install(*, offline_dir: Path | None) -> None:
    fonts: dict[str, bytes] = {}
    licenses: dict[str, bytes] = {}
    if offline_dir is None:
        print("Downloading version-pinned font assets from Google, Pictogrammers and Flutter…")
        for filename, url in SOURCES.items():
            if filename == "flutter_material_icons.otf":
                continue
            print(f"  {filename}")
            fonts[filename] = fetch(url)
        archive = fetch(FLUTTER_URL)
        fonts["flutter_material_icons.otf"], licenses["Flutter-MaterialIcons-LICENSE.txt"] = flutter_artifacts(archive)
        for filename, url in LICENSE_SOURCES.items():
            licenses[filename] = fetch(url, max_bytes=500_000)
    else:
        # Offline mode is solely for validating locally obtained ORIGINAL font files.
        # Normal use should fetch directly from publishers.
        print(f"Reading local fonts from {offline_dir} (offline validation mode)")
        for filename in SOURCES:
            fonts[filename] = (offline_dir / filename).read_bytes()
        for filename in LICENSE_SOURCES:
            path = offline_dir / filename
            if path.exists():
                licenses[filename] = path.read_bytes()
        flutter_license = offline_dir / "Flutter-MaterialIcons-LICENSE.txt"
        if flutter_license.exists():
            licenses[flutter_license.name] = flutter_license.read_bytes()

    print("Validating OpenType tables and regenerating ALL search catalogs…")
    catalogs = compile_indexes(fonts)
    lock = {
        "schema": 1,
        "note": "SHA-256 values recorded after fetching pinned upstream sources; not upstream-signed checksums.",
        "sources": {
            "google_material_symbols": {"repository": "google/material-design-icons", "commit": GOOGLE_REF},
            "pictogrammers_mdi": {"repository": "Templarian/MaterialDesign-Webfont", "commit": MDI_REF},
            "flutter_material_fonts": {"repository": "flutter/flutter", "commit": FLUTTER_REF, "artifact": FLUTTER_URL},
        },
        "files": {
            **{str((FONT_DIR / name).relative_to(REPO_ROOT)): {"sha256": sha256(data), "bytes": len(data), "url": SOURCES[name]}
               for name, data in fonts.items()},
            **{str((INDEX_DIR / name).relative_to(REPO_ROOT)): {"sha256": sha256(data), "bytes": len(data), "generated": True}
               for name, data in catalogs.items()},
        },
    }
    if offline_dir is not None:
        lock["note"] = "Offline/local fonts. No claim of upstream provenance; local SHA-256 only."
        lock["sources"] = {"mode": "user-supplied-local-fonts"}
        for filename in fonts:
            lock["files"][str((FONT_DIR / filename).relative_to(REPO_ROOT))]["url"] = "local-file"

    # Preflight done: no resource file was modified until ALL five fonts validated.
    for name, data in fonts.items():
        write_atomic(FONT_DIR / name, data)
    for name, data in catalogs.items():
        write_atomic(INDEX_DIR / name, data)
    for name, data in licenses.items():
        write_atomic(LICENSE_DIR / name, data)
    write_atomic(LOCK_FILE, (json.dumps(lock, indent=2, ensure_ascii=False) + "\n").encode("utf-8"))
    print("Installed all five fonts; indexes, license notices and SHA-256 lock updated.")
    print("Next: python3 tools/sync_official_icon_fonts.py --verify && ./gradlew installDebug")


def verify() -> None:
    if not LOCK_FILE.is_file():
        raise SystemExit("No lockfile. First run: python3 tools/sync_official_icon_fonts.py")
    lock = json.loads(LOCK_FILE.read_text(encoding="utf-8"))
    for relative, metadata in lock["files"].items():
        path = REPO_ROOT / relative
        if not path.is_file():
            raise SystemExit(f"Missing: {relative}")
        actual = sha256(path.read_bytes())
        if actual != metadata["sha256"]:
            raise SystemExit(f"SHA-256 mismatch for {relative}: expected {metadata['sha256']}, got {actual}")
        print(f"  OK {relative}")
    print("All installed fonts and search catalogs match icon-fonts.lock.json")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument("--verify", action="store_true", help="Compare installed files to generated SHA-256 lock")
    mode.add_argument("--offline-dir", type=Path, help="Validate locally supplied font files instead of downloading")
    args = parser.parse_args()
    if args.verify:
        verify()
    else:
        install(offline_dir=args.offline_dir)


if __name__ == "__main__":
    main()
