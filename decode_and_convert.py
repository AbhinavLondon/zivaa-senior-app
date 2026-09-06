import re
import base64
import os
from fontTools.ttLib import TTFont

html_path = r"C:\Users\abhin\.gemini\antigravity-ide\brain\7a6be679-214f-438d-bcc2-a226420db0c1\scratch\today_template.html"
target_dir = r"c:\Users\abhin\Downloads\Zivaa Apps\ZivaaSeniorApp\app\src\main\res\font"

with open(html_path, "r", encoding="utf-8") as f:
    content = f.read()

# match: url(data:font/woff2;charset=utf-8;base64,...)
# and font-family: '...'
font_defs = re.findall(r"@font-face\s*\{[^}]*font-family:\s*'([^']+)'[^}]*src:\s*url\(data:font/woff2(?:;charset=utf-8)?;base64,([^)]+)\)", content)

print(f"Found {len(font_defs)} fonts in HTML.")

for i, (family, b64) in enumerate(font_defs):
    name = family.lower().replace(" ", "_").replace("-", "_") + f"_{i}"
    woff2_path = f"{name}.woff2"
    with open(woff2_path, "wb") as f:
        f.write(base64.b64decode(b64))
    
    # Convert using fonttools
    ttf_path = os.path.join(target_dir, f"{name}.ttf")
    try:
        font = TTFont(woff2_path)
        font.flavor = None # removes woff2 flavor, makes it pure ttf
        font.save(ttf_path)
        print(f"Converted {family} to {ttf_path}")
    except Exception as e:
        print(f"Failed to convert {family}: {e}")
