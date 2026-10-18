#!/usr/bin/env python3
"""Regenerate SongMeld's compact Material Symbols *variable* fonts from Google TTFs.

Install: python3 -m pip install fonttools
Download Google's Rounded / Outlined / Sharp variable TTFs from:
https://github.com/google/material-design-icons/tree/master/variablefont
Then run: python3 tools/subset_material_symbols.py /path/to/downloaded-fonts

The CLI accepts original Google-style bracketed filenames or plain MaterialSymbols*.ttf.
"""
from pathlib import Path
from sys import argv
from fontTools import subset
from fontTools.ttLib import TTFont

# Keep synchronized with SongMeldIconId in ui/icons/SongMeldIcons.kt.
CHARACTERS = {
    'arrow_back': 0xE5C4,
    'chevron_right': 0xE5CC,
    'close': 0xE5CD,
    'folder_open': 0xE2C8,
    'info': 0xE88E,
    'library_music': 0xE030,
    'lock': 0xE897,
    'more_vert': 0xE5D4,
    'music_note': 0xE405,
    'palette': 0xE40A,
    'refresh': 0xE5D5,
    'search': 0xE8B6,
    'search_off': 0xEA76,
    'settings': 0xE8B8,
}
EXPECTED_AXES = {'FILL', 'wght', 'GRAD', 'opsz'}
ROOT = Path(__file__).resolve().parents[1]
DEST = ROOT / 'app/src/main/res/font'


def main(source_dir: Path) -> None:
    for style in ('Rounded', 'Outlined', 'Sharp'):
        sources = list(source_dir.glob(f'MaterialSymbols{style}*.ttf'))
        if len(sources) != 1:
            raise RuntimeError(f'Expected one MaterialSymbols{style}*.ttf in {source_dir}, found {len(sources)}')
        font = TTFont(sources[0])
        cmap = font.getBestCmap()
        for name, codepoint in CHARACTERS.items():
            if cmap.get(codepoint) != name:
                raise RuntimeError(f'{sources[0]}: {hex(codepoint)} is not {name}')
        settings = subset.Options()
        settings.layout_features = []  # Direct Unicode mapping; no ligatures needed.
        settings.name_IDs = [0, 1, 2, 3, 4, 5, 6, 13, 14]
        sub = subset.Subsetter(options=settings)
        sub.populate(unicodes=list(CHARACTERS.values()))
        sub.subset(font)
        output = DEST / f'material_symbols_{style.lower()}.ttf'
        font.save(output)
        check = TTFont(output)
        assert EXPECTED_AXES == {axis.axisTag for axis in check['fvar'].axes}
        assert 'gvar' in check
        assert all(key in check.getBestCmap() for key in CHARACTERS.values())
        print(f'{style}: {output.stat().st_size:,} bytes, {len(check.getBestCmap())} codepoints')


if __name__ == '__main__':
    if len(argv) != 2:
        raise SystemExit('Usage: python3 tools/subset_material_symbols.py /path/to/google/variablefont')
    main(Path(argv[1]))
