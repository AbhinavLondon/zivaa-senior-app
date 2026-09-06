import os
import urllib.request

fonts = {
    "manrope_regular.ttf": "https://github.com/googlefonts/manrope/raw/master/fonts/ttf/Manrope-Regular.ttf",
    "manrope_medium.ttf": "https://github.com/googlefonts/manrope/raw/master/fonts/ttf/Manrope-Medium.ttf",
    "manrope_semibold.ttf": "https://github.com/googlefonts/manrope/raw/master/fonts/ttf/Manrope-SemiBold.ttf",
    "manrope_bold.ttf": "https://github.com/googlefonts/manrope/raw/master/fonts/ttf/Manrope-Bold.ttf",
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
