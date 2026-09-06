import re

with open('../zivaa-backend/migrations/027_add_skin_temp_priority_and_delta.sql', 'r') as f:
    sql = f.read()

hr_daily_new = '''        ), hr_daily AS (
           SELECT hr_samples.patient_id,
              hr_samples.date,
              avg(hr_samples.bpm) AS avg_heart_rate,
              max(hr_samples.bpm) AS max_heart_rate,
              min(hr_samples.bpm) AS min_heart_rate,
              avg(CASE WHEN extract(hour from hr_samples.sample_time) BETWEEN 6 AND 11 THEN hr_samples.bpm END) AS hr_avg_morning,
              avg(CASE WHEN extract(hour from hr_samples.sample_time) BETWEEN 12 AND 17 THEN hr_samples.bpm END) AS hr_avg_afternoon,
              avg(CASE WHEN extract(hour from hr_samples.sample_time) BETWEEN 18 AND 23 THEN hr_samples.bpm END) AS hr_avg_evening,
              avg(CASE WHEN extract(hour from hr_samples.sample_time) BETWEEN 0 AND 5 THEN hr_samples.bpm END) AS hr_avg_night
             FROM hr_samples
            GROUP BY hr_samples.patient_id, hr_samples.date
          ),'''

sql = re.sub(r'\s*\),\s*hr_daily\s*AS\s*\([\s\S]*?GROUP\s*BY\s*hr_samples\.patient_id,\s*hr_samples\.date\s*\),', hr_daily_new, sql)

select_replacement = '''      h.avg_heart_rate,
      h.max_heart_rate,
      h.min_heart_rate,
      h.hr_avg_morning,
      h.hr_avg_afternoon,
      h.hr_avg_evening,
      h.hr_avg_night,'''

sql = sql.replace('''      h.avg_heart_rate,
      h.max_heart_rate,
      h.min_heart_rate,''', select_replacement)

with open('../zivaa-backend/migrations/031_add_segmented_hr.sql', 'w') as f:
    f.write(sql)
