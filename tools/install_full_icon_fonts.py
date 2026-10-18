#!/usr/bin/env python3
"""Install exact full icon fonts used by SongMeld's offline icon catalog.

No third-party Python packages required.

Example:
    python3 tools/install_full_icon_fonts.py --paisa /path/to/Paisa_2026.09.1210_antisplit.apk

The catalog TSV files were generated from these exact font versions. This installer
verifies each file's SHA-256 before it overwrites any app font resource. The
APK must be lawfully obtained by the user. An optional --directory accepts
locally obtained font binaries renamed as in the FONTS mapping below.
"""
import argparse
from hashlib import sha256
from pathlib import Path
from zipfile import ZipFile

FONTS = {
    'material_symbols_rounded.ttf': (
        'assets/flutter_assets/packages/material_symbols_icons/lib/fonts/MaterialSymbolsRounded.ttf',
        '0ff80fedb8afa8eba26c58426a569f03da4da3597b9cb8806408a568a98dd031',
    ),
    'material_symbols_outlined.ttf': (
        'assets/flutter_assets/packages/material_symbols_icons/lib/fonts/MaterialSymbolsOutlined.ttf',
        'a1f6d261c858ea371370087a67e48ce5d1b9e658a1e7c012fcfa98b8bbdf77b7',
    ),
    'material_symbols_sharp.ttf': (
        'assets/flutter_assets/packages/material_symbols_icons/lib/fonts/MaterialSymbolsSharp.ttf',
        '57cb1f7daa4ae4ac4092b73d1debefd0d38a1069b36411735ccf7c9ddbe9e7d1',
    ),
    'material_design_icons.ttf': (
        'assets/flutter_assets/packages/flutter_material_design_icons/assets/materialdesignicons-webfont.ttf',
        '61e8aba5a4e981fe22cf7c8e8bcdbea00476e75c62c37f01bf7ee33361d68428',
    ),
    'flutter_material_icons.otf': (
        'assets/flutter_assets/fonts/MaterialIcons-Regular.otf',
        'd9865b671a09d683d13a863089d8825e0f61a37696ce5d7d448bc8023aa62453',
    ),
}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument('--paisa', type=Path, help='Paisa APK provided for this experiment')
    group.add_argument('--directory', type=Path, help='Directory with the five named font files')
    args = parser.parse_args()
    destination = Path(__file__).resolve().parent.parent / 'app/src/main/res/font'
    if args.paisa:
        with ZipFile(args.paisa) as archive:
            data = {name: archive.read(archive_path) for name, (archive_path, _) in FONTS.items()}
    else:
        data = {name: (args.directory / name).read_bytes() for name in FONTS}

    # Transaction-style preflight: do not touch any existing font unless ALL match.
    for name, content in data.items():
        expected = FONTS[name][1]
        actual = sha256(content).hexdigest()
        if actual != expected:
            raise SystemExit(f'{name}: hash mismatch. Catalog was generated for another font version. '
                             'Use the expected Paisa APK or regenerate catalogs for new font versions.')
    destination.mkdir(parents=True, exist_ok=True)
    for name, content in data.items():
        target = destination / name
        target.write_bytes(content)
        print(f'Installed {target.relative_to(destination.parent.parent.parent)}: {len(content):,} bytes')
    print('Success: all five full font files match the bundled offline search catalogs.')
    print('Next: ./gradlew installDebug')


if __name__ == '__main__':
    main()
