/**
 * Backend Anomaly Detection Update (Node.js / Supabase Edge Function Example)
 * 
 * Logic to segment incoming HR records and compare them to the proper baseline.
 */

function getTimeSegment(dateString, zoneOffsetStr) {
    // Parse the date using the zone offset provided by the Android app
    // e.g. "2026-09-02T06:30:00" with offset "+01:00"
    const localDate = new Date(dateString); 
    // Simplified logic: extracting the hour in local time
    const hour = localDate.getHours();

    if (hour >= 6 && hour < 12) return 'morning';
    if (hour >= 12 && hour < 18) return 'afternoon';
    if (hour >= 18 && hour < 24) return 'evening';
    return 'night';
}

async function evaluateHeartRateAnomaly(patientId, incomingHrRecords) {
    // 1. Group incoming records by time segment
    const segmentAverages = { morning: [], afternoon: [], evening: [], night: [] };
    
    for (const record of incomingHrRecords) {
        // We now receive zone_offset from the Android app!
        const segment = getTimeSegment(record.time, record.zone_offset);
        segmentAverages[segment].push(record.bpm);
    }

    // 2. Fetch the patient's 30-day baseline for the segments we have data for
    const { data: baseline } = await supabase
        .from('vitals_Daily')
        .select('hr_avg_morning, hr_avg_afternoon, hr_avg_evening, hr_avg_night')
        .eq('patient_id', patientId)
        .order('date', { ascending: false })
        .limit(30);

    // Calculate rolling averages for each segment...
    const rollingMorningAvg = calculateRolling(baseline, 'hr_avg_morning');

    // 3. Compare specifically!
    if (segmentAverages.morning.length > 0) {
        const currentMorningAvg = getMean(segmentAverages.morning);
        
        // Instead of comparing currentMorningAvg to a daily 75bpm average,
        // it compares it to the morning 55bpm average!
        if (currentMorningAvg < (rollingMorningAvg * 0.85)) {
            // Trigger "Low HR" Nudge
            await generateNudge(patientId, 'Heart rate unusually low for this time of day');
        }
    }
}
