-- Migration: Add segmented heart rate averages to vitals_Daily table
-- This allows the anomaly engine to compare morning waking HR against morning baselines.

ALTER TABLE public."vitals_Daily"
ADD COLUMN hr_avg_morning NUMERIC NULL,
ADD COLUMN hr_avg_afternoon NUMERIC NULL,
ADD COLUMN hr_avg_evening NUMERIC NULL,
ADD COLUMN hr_avg_night NUMERIC NULL;

COMMENT ON COLUMN public."vitals_Daily".hr_avg_morning IS 'Average HR between 06:00 and 12:00';
COMMENT ON COLUMN public."vitals_Daily".hr_avg_afternoon IS 'Average HR between 12:00 and 18:00';
COMMENT ON COLUMN public."vitals_Daily".hr_avg_evening IS 'Average HR between 18:00 and 24:00';
COMMENT ON COLUMN public."vitals_Daily".hr_avg_night IS 'Average HR between 00:00 and 06:00';
