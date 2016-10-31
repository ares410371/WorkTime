package cz.droidboy.worktime.util;

import com.github.mikephil.charting.utils.ValueFormatter;

import java.util.concurrent.TimeUnit;

/**
 * @author Jonas Sevcik
 */
public class GraphValueFormatter implements ValueFormatter {
    @Override
    public String getFormattedValue(float value) {
        return getHumanReadableDuration((int) value);
    }

    private String getHumanReadableDuration(int durationMins) {
        long hours = TimeUnit.MINUTES.toHours(durationMins);
        long remainingMinutes = durationMins - TimeUnit.HOURS.toMinutes(hours);
        return String.format("%02d", hours) + ":" + String.format("%02d", remainingMinutes);
    }
}
