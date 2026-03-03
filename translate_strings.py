from deep_translator import GoogleTranslator
import xml.etree.ElementTree as ET
from xml.sax.saxutils import escape as xml_escape
import os
import sys

# ── Force UTF-8 output (important on Windows)
try:
    sys.stdout.reconfigure(encoding="utf-8")
except Exception:
    pass

# ─────────────────────────────────────────
# CONFIG — edit these to match your project
# ─────────────────────────────────────────

# Source language code (e.g. "ar", "en", "fr", "tr" ...)
SOURCE_LANG = "ar"

# Source strings.xml path
SOURCE_FILE = "app/src/main/res/values/strings.xml"

# Resources directory
RES_DIR = "app/src/main/res"

# Languages to translate INTO (remove or add as needed)
LANGUAGES = {
    "es": "Spanish",
    "de": "German",
    "tr": "Turkish",
    "ur": "Urdu",
    "ms": "Malay",
    "in": "Indonesian",
    "bn": "Bengali",
}

# ─────────────────────────────────────────

def safe_text(text):
    """
    Make text safe for Android XML string resources:
    - Escape XML special characters (&, <, >)
    - Escape apostrophes (Android requirement)
    - Strip whitespace that causes issues
    """
    if not text:
        return ""
    text = text.strip()
    text = xml_escape(text)           # & → &amp;   < → &lt;   > → &gt;
    text = text.replace("'", "\\'")   # Android requires escaped apostrophes
    return text


def safe_translate(text, translator, name):
    """Translate text and return a safe, XML-compatible string."""
    try:
        translated = translator.translate(text)
        if not translated:
            return safe_text(text)
        return safe_text(translated)
    except Exception:
        print(f"  Failed to translate key: {name}")
        return safe_text(text)


def translate_file():
    if not os.path.exists(SOURCE_FILE):
        print(f"File not found: {SOURCE_FILE}")
        sys.exit(1)

    tree = ET.parse(SOURCE_FILE)
    root = tree.getroot()

    for lang_code, lang_name in LANGUAGES.items():
        if lang_code == SOURCE_LANG:
            print(f"Skipping {lang_name} (same as source language)")
            continue

        out_dir = f"{RES_DIR}/values-{lang_code}"
        out_file = f"{out_dir}/strings.xml"

        # ── Read existing translations if file already exists
        existing_translations = {}
        if os.path.exists(out_file):
            existing_tree = ET.parse(out_file)
            for s in existing_tree.getroot().findall("string"):
                existing_translations[s.get("name")] = s.text

        translator = GoogleTranslator(source=SOURCE_LANG, target=lang_code)
        new_root = ET.Element("resources")
        new_strings_count = 0

        for string in root.findall("string"):
            name = string.get("name")
            text = string.text or ""
            translatable = string.get("translatable", "true")

            if name in existing_translations:
                # ── Key already translated → keep existing
                translated = existing_translations[name]

            elif translatable == "false":
                # ── Not translatable → copy as-is
                translated = safe_text(text)

            else:
                # ── New key → translate it
                translated = safe_translate(text, translator, name)
                new_strings_count += 1

            new_string = ET.SubElement(new_root, "string", name=name)
            if translatable == "false":
                new_string.set("translatable", "false")
            new_string.text = translated

        os.makedirs(out_dir, exist_ok=True)
        out_tree = ET.ElementTree(new_root)
        ET.indent(out_tree, space="    ")
        out_tree.write(out_file, encoding="utf-8", xml_declaration=True)

        if new_strings_count > 0:
            print(f"Updated {lang_name}: added {new_strings_count} new string(s)")
        else:
            print(f"{lang_name}: nothing new to translate")

    print("All translations completed successfully ✅")


if __name__ == "__main__":
    translate_file()