import re

with open('../zivaa-backend/migrations/022_update_vitals_daily_timezone.sql', 'r') as f:
    sql = f.read()

# Replace hr_daily
hr_daily_new = '''hr_daily AS (
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

sql = re.sub(r'hr_daily AS \([\s\S]*?GROUP BY hr_samples.patient_id, hr_samples.date\n\),', hr_daily_new, sql)

# Add to main SELECT
select_new = '''SELECT COALESCE(o.patient_id, h.patient_id, ox.patient_id, s.patient_id, r.patient_id, sp.patient_id, ms.patient_id, spf.patient_id, hrr.patient_id) AS patient_id,
    COALESCE(o.date, h.date, ox.date, s.date, r.date, sp.date, ms.date, spf.date, hrr.date) AS date,
    h.avg_heart_rate,
    h.max_heart_rate,
    h.min_heart_rate,
    h.hr_avg_morning,
    h.hr_avg_afternoon,
    h.hr_avg_evening,
    h.hr_avg_night,'''

sql = sql.replace('SELECT COALESCE(o.patient_id, h.patient_id, ox.patient_id, s.patient_id, r.patient_id, sp.patient_id, ms.patient_id, spf.patient_id, hrr.patient_id) AS patient_id,\n    COALESCE(o.date, h.date, ox.date, s.date, r.date, sp.date, ms.date, spf.date, hrr.date) AS date,\n    h.avg_heart_rate,\n    h.max_heart_rate,\n    h.min_heart_rate,', select_new)

with open('../zivaa-backend/migrations/031_add_segmented_hr.sql', 'w') as f:
    f.write(sql)
