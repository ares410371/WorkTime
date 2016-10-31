package cz.droidboy.worktime.util;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.List;

import cz.droidboy.worktime.model.WorkTime;

/**
 * @author Jonas Sevcik
 */
public class WorkTimeUtils {

    public static int calculateDayWorktimeMinutes(List<WorkTime> workTimes, LocalDate day) {
        if (workTimes == null) {
            throw new IllegalArgumentException("workTimes == null");
        }
        if (day == null) {
            throw new IllegalArgumentException("day == null");
        }

        int overallTimeMins = 0;
        if (!workTimes.isEmpty()) {
            boolean dayIsToday = dayIsToday(day);
            DateTime midnight = day.plusDays(1).toDateTimeAtStartOfDay();
            final int size = workTimes.size();
            for (int i = 0; i < size; i++) {
                WorkTime workTime = workTimes.get(i);
                //kdyz je to dnes a neni konec, tak dam soucasnost, pokud je to skonceny den a stale nema end, tak dam pulnoc
                if (workTime.getEndDate() == null) {
                    overallTimeMins += (dayIsToday ? DateTime.now() : midnight).getMillis() - workTime.getStartDate().getMillis();
                } else if (workTime.getEndDate().isAfter(midnight)) {
                    overallTimeMins += midnight.getMillis() - workTime.getStartDate().getMillis();
                } else {
                    overallTimeMins += workTime.getEndDate().getMillis() - workTime.getStartDate().getMillis();
                }
            }
            overallTimeMins /= 60_000;
        }
        return overallTimeMins;
    }

    public static boolean dayIsToday(LocalDate day) {
        return LocalDate.now().isEqual(day);
    }
}
