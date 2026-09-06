import json, re
with open(r'c:\Users\abhin\Downloads\Zivaa Apps\Choose Body Parts for Ranjit.html', 'r', encoding='utf-8') as f:
    html = f.read()

m = re.search(r'<script type="__bundler/template">(.*?)</script>', html, re.DOTALL)
if m:
    template_str = m.group(1)
    template = json.loads(template_str)
    with open('parsed_template.html', 'w', encoding='utf-8') as f2:
        f2.write(template)
else:
    print('Not found')
