import json
import re

with open('c:/Users/abhin/Downloads/Zivaa Apps/Sleep for Ranjit.html', 'r', encoding='utf-8') as f:
    content = f.read()

template_match = re.search(r'<script type="__bundler/template">(.*?)</script>', content, re.DOTALL)
if template_match:
    template_str = template_match.group(1).strip()
    try:
        template_json = json.loads(template_str)
        with open('template.html', 'w', encoding='utf-8') as out:
            out.write(template_json)
        print("Template extracted to template.html")
    except Exception as e:
        print("Failed to parse template JSON:", e)
else:
    print("No template found.")
