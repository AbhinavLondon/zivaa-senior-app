import json
import base64
import zlib
import re

def extract():
    with open('../Movement for Ranjit.html', 'r', encoding='utf-8') as f:
        html = f.read()
    
    match = re.search(r'<script type="__bundler/manifest">(.*?)</script>', html, re.DOTALL)
    if not match:
        print("No manifest found")
        return
        
    manifest = json.loads(match.group(1).strip())
    for uuid, entry in manifest.items():
        if entry['mime'] in ['application/javascript', 'text/javascript', 'text/babel']:
            data = base64.b64decode(entry['data'])
            if entry.get('compressed'):
                try:
                    data = zlib.decompress(data, 16 + zlib.MAX_WBITS)
                except Exception as e:
                    print(f"Failed to decompress {uuid}: {e}")
            
            with open(f"extracted_{uuid[:8]}.js", "wb") as outf:
                outf.write(data)
            print(f"Extracted {uuid[:8]}.js ({len(data)} bytes)")

extract()
