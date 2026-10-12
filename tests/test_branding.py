"""Regression tests for reusable SongMeld branding geometry.

Run with: python3 -m unittest discover -s tests -v
No external Python packages are required.
"""

from pathlib import Path
import unittest
import xml.etree.ElementTree as ET

PROJECT_ROOT = Path(__file__).resolve().parent.parent
ANDROID = "{http://schemas.android.com/apk/res/android}"
SVG = "{http://www.w3.org/2000/svg}"


class BrandingGeometryTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        root = ET.parse(PROJECT_ROOT / "branding/songmeld-logo.svg").getroot()
        cls.paths = {p.get("id"): p.get("d") for p in root.findall(f"{SVG}path")}

    def android_paths(self, name):
        root = ET.parse(PROJECT_ROOT / "app/src/main/res/drawable" / name).getroot()
        return [p.get(f"{ANDROID}pathData") for p in root.findall(".//path")]

    def test_every_note_matches_canonical(self):
        note = self.paths["logo-note"]
        for name in (
            "songmeld_logo.xml",
            "songmeld_mark.xml",
            "songmeld_splash_note.xml",
            "songmeld_splash_vector.xml",
            "ic_launcher_foreground.xml",
            "ic_launcher_monochrome.xml",
        ):
            with self.subTest(name=name):
                self.assertEqual(self.android_paths(name)[-1], note)

        for name in ("songmeld-mark.svg", "songmeld-logo-orange.svg"):
            root = ET.parse(PROJECT_ROOT / "branding" / name).getroot()
            paths = root.findall(f"{SVG}path")
            self.assertEqual(paths[-1].get("d"), note)

    def test_badge_paths_match_canonical(self):
        badge = self.paths["logo-star"]
        for file in ("songmeld_logo.xml", "songmeld_splash_vector.xml"):
            with self.subTest(file=file):
                self.assertEqual(self.android_paths(file)[0], badge)
        morph = ET.parse(PROJECT_ROOT / "app/src/main/res/animator/songmeld_badge_morph.xml").getroot()
        self.assertEqual(morph.get(f"{ANDROID}valueFrom"), badge)
        kotlin = (PROJECT_ROOT / "app/src/main/java/moe/ktandth/songmeld/ui/splash/SongMeldSplashIntro.kt").read_text()
        self.assertIn(f'private const val BADGE_PATH = """{badge}"""', kotlin)

    def test_no_duplicate_note_alignment(self):
        for file in ("songmeld_splash_note.xml", "ic_launcher_foreground.xml", "ic_launcher_monochrome.xml"):
            with self.subTest(file=file):
                root = ET.parse(PROJECT_ROOT / "app/src/main/res/drawable" / file).getroot()
                for group in root.findall(".//group"):
                    self.assertNotIn(f"{ANDROID}translateX", group.attrib)
                    self.assertNotIn(f"{ANDROID}translateY", group.attrib)

    def test_readme_uses_static_orange_variant(self):
        source = (PROJECT_ROOT / "README.md").read_text()
        self.assertIn('src="branding/songmeld-logo-orange.svg"', source)
        root = ET.parse(PROJECT_ROOT / "branding/songmeld-logo-orange.svg").getroot()
        paths = root.findall(f"{SVG}path")
        self.assertEqual(paths[0].get("fill"), "#FFA069")
        self.assertEqual(paths[1].get("fill"), "#57331E")

    def test_every_svg_and_android_vector_parses(self):
        files = list((PROJECT_ROOT / "branding").glob("*.svg"))
        files += list((PROJECT_ROOT / "app/src/main/res").rglob("*.xml"))
        for file in files:
            with self.subTest(file=str(file.relative_to(PROJECT_ROOT))):
                ET.parse(file)


if __name__ == "__main__":
    unittest.main()
