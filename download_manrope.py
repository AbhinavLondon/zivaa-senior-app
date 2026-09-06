import os
import urllib.request

fonts = {
    "manrope_regular.ttf": "https://fonts.gstatic.com/s/manrope/v20/xn7_YHE41ni1AdIRqAuZuw1Bx9mbZk79FN_C-bw.ttf",
    "manrope_medium.ttf": "https://fonts.gstatic.com/s/manrope/v20/xn7_YHE41ni1AdIRqAuZuw1Bx9mbZk7PFN_C-bw.ttf",
    "manrope_semibold.ttf": "https://fonts.gstatic.com/s/manrope/v20/xn7_YHE41ni1AdIRqAuZuw1Bx9mbZk4jE9_C-bw.ttf",
    "manrope_bold.ttf": "https://fonts.gstatic.com/s/manrope/v20/xn7_YHE41ni1AdIRqAuZuw1Bx9mbZk4aE9_C-bw.ttf",
}

target_dir = r"c:\Users\abhin\Downloads\Zivaa Apps\ZivaaSeniorApp\app\src\main\res\font"

# Ensure directory exists
os.makedirs(target_dir, exist_ok=True)

for name, url in fonts.items():
    print(f"Downloading {name}...")
    target_path = os.path.join(target_dir, name)
    try:
        urllib.request.urlretrieve(url, target_path)
        print(f"Saved {name}")
    except Exception as e:
        print(f"Failed to download {name}: {e}")
