import re

with open('../zivaa-backend/app/services/insights/data_fetcher.py', 'r') as f:
    text = f.read()

replacement = '''        metrics_mapping = {
            "bp_systolic": row.get("bp_systolic"),
            "bp_diastolic": row.get("bp_diastolic"),
            "avg_heart_rate": row.get("avg_heart_rate"),
            "hr_avg_morning": row.get("hr_avg_morning"),
            "hr_avg_afternoon": row.get("hr_avg_afternoon"),
            "hr_avg_evening": row.get("hr_avg_evening"),
            "hr_avg_night": row.get("hr_avg_night"),'''

text = text.replace('''        metrics_mapping = {
            "bp_systolic": row.get("bp_systolic"),
            "bp_diastolic": row.get("bp_diastolic"),
            "avg_heart_rate": row.get("avg_heart_rate"),''', replacement)

with open('../zivaa-backend/app/services/insights/data_fetcher.py', 'w') as f:
    f.write(text)
