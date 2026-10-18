#!/usr/bin/env python3
"""Build SongMeld offline icon lookup TSV files from licensed full font files.

Usage: python tools/build_full_icon_catalog.py --symbols-rounded path.ttf --symbols-outlined path.ttf --symbols-sharp path.ttf --mdi path.ttf --flutter path.otf

Uses public Unicode cmap glyph names. Symbols' 3 styles share one catalog.
Requires pip install fonttools. This script does not fetch any assets.
"""
from argparse import ArgumentParser
from pathlib import Path
from fontTools.ttLib import TTFont

parser = ArgumentParser()
for flag in ('symbols-rounded','symbols-outlined','symbols-sharp','mdi','flutter'):
    parser.add_argument('--'+flag, required=True, type=Path)
parser.add_argument('--output',type=Path,default=Path('app/src/main/assets/icon_catalog'))
a = parser.parse_args()

def catalog(path):
    font=TTFont(path,lazy=True)
    try:
        return {cp:name for cp,name in font.getBestCmap().items()
            if cp >= 0xE000 and name not in ('.notdef','space') and not name.startswith('uni')}
    finally:font.close()

symbols = catalog(a.symbols_rounded)
for style in (a.symbols_outlined,a.symbols_sharp):
    if set(symbols) != set(catalog(style)):
        raise SystemExit(f'Symbol codepoints differ between styles: {style}')
a.output.mkdir(parents=True,exist_ok=True)
for pack, entries in [('symbols',symbols),('mdi',catalog(a.mdi)),('flutter',catalog(a.flutter))]:
    target=a.output/f'{pack}.tsv'
    # Searchable names come from actual glyph names in each supplied font version.
    lines=[f'{codepoint:X}\t{name}' for codepoint,name in sorted(entries.items())]
    target.write_text('\n'.join(lines)+'\n',encoding='utf-8')
    print(f'{pack}: {len(lines)} searchable icons, {target.stat().st_size:,} bytes')
