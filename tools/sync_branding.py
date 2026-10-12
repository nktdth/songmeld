#!/usr/bin/env python3
"""Synchronize all SongMeld SVG and Android vector assets from one SVG.

The canonical geometry is branding/songmeld-logo.svg. The logo's central
music/wrench note already includes its optical correction in its coordinates;
consumers MUST NOT add per-resource translation offsets to that note.

Usage:
    python3 tools/sync_branding.py
    python3 tools/sync_branding.py --check

Uses Python's standard library only. Run after editing the brand SVG.
"""

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent.parent
SVG_NS = "http://www.w3.org/2000/svg"
ANDROID_NS = "http://schemas.android.com/apk/res/android"
NOTE_VECTOR_FILES = (
    "app/src/main/res/drawable/songmeld_mark.xml",
    "app/src/main/res/drawable/ic_launcher_foreground.xml",
    "app/src/main/res/drawable/ic_launcher_monochrome.xml",
    "app/src/main/res/drawable/songmeld_splash_note.xml",
)
FULL_VECTOR_FILES = (
    "app/src/main/res/drawable/songmeld_logo.xml",
    "app/src/main/res/drawable/songmeld_splash_vector.xml",
)
PATH_DATA_RE = re.compile(r'android:pathData="[^"]+"')


def canonical_paths() -> tuple[str, str]:
    root = ET.parse(ROOT / "branding/songmeld-logo.svg").getroot()
    paths = {node.get("id"): node.get("d") for node in root.findall(f"{{{SVG_NS}}}path")}
    if not paths.get("logo-star") or not paths.get("logo-note"):
        raise ValueError("Full logo requires logo-star and logo-note paths")
    return paths["logo-star"], paths["logo-note"]


def swap_path_data(filename: str, expected_paths: tuple[str, ...]) -> str:
    """Replace just vector path geometry, preserving colors and animations."""
    source = (ROOT / filename).read_text(encoding="utf-8")
    matches = list(PATH_DATA_RE.finditer(source))
    if len(matches) != len(expected_paths):
        raise ValueError(f"{filename}: expected {len(expected_paths)} paths, found {len(matches)}")
    iterator = iter(expected_paths)
    return PATH_DATA_RE.sub(lambda _: f'android:pathData="{next(iterator)}"', source)


def orange_readme_logo(badge: str, note: str) -> str:
    # Static GitHub images cannot use Android runtime / wallpaper-based colors.
    return (
        '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" '
        'width="512" height="512" role="img" aria-labelledby="logo-title logo-desc">\n'
        '  <title id="logo-title">SongMeld</title>\n'
        '  <desc id="logo-desc">Orange SongMeld badge with dark brown music and wrench note</desc>\n'
        f'  <path id="logo-star" fill="#FFA069" d="{badge}"/>\n'
        f'  <path id="logo-note" fill="#57331E" d="{note}"/>\n'
        '</svg>\n'
    )


def new_splash_note(note: str) -> str:
    # No per-screen translateX/Y: the correct position lives in the SVG path.
    return (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        '    android:width="24dp"\n'
        '    android:height="24dp"\n'
        '    android:viewportWidth="24"\n'
        '    android:viewportHeight="24">\n'
        '    <!-- Canonical note path: no individual splash alignment transform. -->\n'
        '    <path android:fillColor="@color/songmeld_launcher_foreground"\n'
        f'        android:pathData="{note}" />\n'
        '</vector>\n'
    )


def produce_outputs() -> dict[str, str]:
    badge, note = canonical_paths()
    result: dict[str, str] = {}

    # Source note is also published without the exterior badge.
    file = "branding/songmeld-mark.svg"
    mark = (ROOT / file).read_text(encoding="utf-8")
    mark, count = re.subn(r'(?<= d=")[^"]+', lambda _: note, mark)
    if count != 1:
        raise ValueError("Expected exactly one standalone SVG mark path")
    result[file] = mark

    result["branding/songmeld-logo-orange.svg"] = orange_readme_logo(badge, note)

    for file in NOTE_VECTOR_FILES:
        if file.endswith("songmeld_splash_note.xml"):
            result[file] = new_splash_note(note)
            continue
        contents = swap_path_data(file, (note,))
        if "ic_launcher_" in file:
            # Scaling for launcher safe area is NOT an alignment correction.
            # Preserve scaleX/scaleY and 108dp foreground size.
            contents = re.sub(r'\s+android:translateX="-0\.42"', "", contents)
            contents = re.sub(r'\s+android:translateY="0\.38"', "", contents)
            contents = contents.replace(
                "Launcher-only alignment: gently nudge the note left and down to balance the flag.",
                "Canonical note geometry; scale only for the adaptive-icon safe area.",
            )
        result[file] = contents

    for file in FULL_VECTOR_FILES:
        result[file] = swap_path_data(file, (badge, note))

    file = "app/src/main/res/animator/songmeld_badge_morph.xml"
    morph = (ROOT / file).read_text(encoding="utf-8")
    morph, count = re.subn(r'android:valueFrom="[^"]+"', f'android:valueFrom="{badge}"', morph)
    if count != 1:
        raise ValueError("Expected a single badge morph from path")
    result[file] = morph

    file = "app/src/main/java/moe/ktandth/songmeld/ui/splash/SongMeldSplashIntro.kt"
    kotlin = (ROOT / file).read_text(encoding="utf-8")
    kotlin, count = re.subn(r'(private const val BADGE_PATH = """)[^\n]*(""")',
                            lambda m: m.group(1) + badge + m.group(2), kotlin)
    if count != 1:
        raise ValueError("Could not update Compose BADGE_PATH constant")
    result[file] = kotlin

    return result


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="Fail if generated assets need regeneration")
    args = parser.parse_args()
    changes = []

    for relative, contents in produce_outputs().items():
        target = ROOT / relative
        if target.exists() and target.read_text(encoding="utf-8") == contents:
            continue
        changes.append(relative)
        if not args.check:
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_text(contents, encoding="utf-8")

    if changes:
        for relative in changes:
            print(("OUT OF SYNC" if args.check else "UPDATED") + f": {relative}")
        return 1 if args.check else 0

    print("Branding assets are in sync with branding/songmeld-logo.svg")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (OSError, ValueError, ET.ParseError) as error:
        print(f"Branding sync failed: {error}", file=sys.stderr)
        sys.exit(2)
