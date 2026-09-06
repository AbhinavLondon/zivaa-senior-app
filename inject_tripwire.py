import re

with open('../zivaa-backend/app/services/tripwire.py', 'r') as f:
    text = f.read()

replacement = '''        for metric_name in vitals_metrics:
            # Skip overall avg_heart_rate anomaly checks to rely strictly on the 
            # more accurate time-segmented HR averages (morning, afternoon, etc.)
            if metric_name == "avg_heart_rate":
                continue
                
            try:
                m = ctx.vitals.metric(metric_name)'''

text = text.replace('''        for metric_name in vitals_metrics:
            try:
                m = ctx.vitals.metric(metric_name)''', replacement)

with open('../zivaa-backend/app/services/tripwire.py', 'w') as f:
    f.write(text)
