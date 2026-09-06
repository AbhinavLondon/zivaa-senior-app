import os
import re

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
        
    # Remove import
    content = content.replace("import com.zivaa.app.ui.theme.PlanSetupTheme\n", "")
    content = content.replace("import com.zivaa.app.ui.theme.PlanSetupTheme\r\n", "")
    
    # Remove PlanSetupTheme { and the matching closing brace }
    # Let's just use string replace.
    content = content.replace("    PlanSetupTheme {\n", "")
    content = content.replace("    PlanSetupTheme {\r\n", "")
    content = content.replace("    // We use PlanSetupTheme here so that the background color matches the \"Customise Plan\" journey\n", "")
    content = content.replace("    // We use PlanSetupTheme here so that the background color matches the \"Customise Plan\" journey\r\n", "")
    
    # Also need to dedent by 4 spaces and remove the last brace.
    lines = content.split('\n')
    new_lines = []
    
    for i in range(len(lines)):
        # if this line is just '    }' at the end of the file, skip it
        if i == len(lines) - 2 and lines[i].startswith("    }"):
            continue
        if lines[i].startswith("        "):
            new_lines.append(lines[i][4:])
        elif lines[i].startswith("    }"): # Could be closing brace of the Screen
            new_lines.append(lines[i])
        else:
            new_lines.append(lines[i])

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write('\n'.join(new_lines))
    print(f"Fixed {filename}")

