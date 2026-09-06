import re

with open('../zivaa-backend/migrations/031_add_segmented_hr.sql', 'r') as f:
    sql = f.read()

sql = re.sub(r'h\.avg_heart_rate,\s*h\.max_heart_rate,\s*h\.min_heart_rate,', '''h.avg_heart_rate,
      h.max_heart_rate,
      h.min_heart_rate,
      h.hr_avg_morning,
      h.hr_avg_afternoon,
      h.hr_avg_evening,
      h.hr_avg_night,''', sql)

with open('../zivaa-backend/migrations/031_add_segmented_hr.sql', 'w') as f:
    f.write(sql)
