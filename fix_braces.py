import os

files_to_fix = [
    "SetupFocusScreen.kt",
    "SetupMorningsScreen.kt",
    "SetupMovementScreen.kt",
    "SetupDietScreen.kt",
    "SetupMetricsScreen.kt",
    "HealthConditionsScreen.kt",
    "SetupEveningsScreen.kt",
    "SetupRemindersScreen.kt"
]

directory = r"c:\Users\abhin\Downloads\Zivaa Apps\ZivaaSeniorApp\app\src\main\java\com\zivaa\app\presentation\setup"

for filename in files_to_fix:
    filepath = os.path.join(directory, filename)
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Just remove `    }\n}` or `    }\r\n}` at the end and replace with `}`
    # Wait, some might just have `}\n}`. Let's just fix the last occurrence.
    # A robust way is to strip whitespace from right, then remove the last `}`, then strip whitespace, and append `\n}`.
    
    lines = [line for line in content.splitlines() if line.strip() != ""]
    
    # Check if the last two lines are both some form of closing brace
    if lines[-1].strip() == "}" and lines[-2].strip() == "}":
        lines.pop(-2)
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write('\n'.join(lines) + '\n')
    print(f"Fixed {filename}")

