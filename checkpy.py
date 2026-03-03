# check2.py
import xml.etree.ElementTree as ET

tree = ET.parse('app/src/main/res/values-id/strings.xml')
root = tree.getroot()

for s in root.findall('string'):
    name = s.get('name')
    text = s.text or ''
    # check for suspicious characters
    if any(c in text for c in ['&', '<', '>', '"']) or text != text.strip():
        print(f"SUSPICIOUS: {name} = {repr(text)}")

print(f"Total strings: {len(root.findall('string'))}")