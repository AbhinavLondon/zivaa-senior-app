import re

with open('../zivaa-backend/app/services/insights/baseline.py', 'r') as f:
    text = f.read()

replacement = '''    "avg_heart_rate":   {"min_days": 5, "window_days": 30, "label": "Average Heart Rate"},
    "hr_avg_morning":   {"min_days": 5, "window_days": 30, "label": "Morning Heart Rate"},
    "hr_avg_afternoon": {"min_days": 5, "window_days": 30, "label": "Afternoon Heart Rate"},
    "hr_avg_evening":   {"min_days": 5, "window_days": 30, "label": "Evening Heart Rate"},
    "hr_avg_night":     {"min_days": 5, "window_days": 30, "label": "Night Heart Rate"},'''

text = text.replace('    "avg_heart_rate":   {"min_days": 5, "window_days": 30, "label": "Average Heart Rate"},', replacement)

with open('../zivaa-backend/app/services/insights/baseline.py', 'w') as f:
    f.write(text)
