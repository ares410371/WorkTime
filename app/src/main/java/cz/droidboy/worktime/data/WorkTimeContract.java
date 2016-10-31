package cz.droidboy.worktime.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * @author Jonas Sevcik
 */
public class WorkTimeContract {

    public static final String CONTENT_AUTHORITY = "cz.droidboy.worktime.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_WORK_TIME = "worktime";

    public static final String DATE_FORMAT = "yyyyMMddHHmm";

    /**
     * Converts Date class to a string representation, used for easy comparison and database
     * lookup.
     *
     * @param date The input date
     * @return a DB-friendly representation of the date, using the format defined in DATE_FORMAT.
     */
    public static String getDbDateString(DateTime date) {
        return date.toString(DATE_FORMAT);
    }

    /**
     * Converts a dateText to a long Unix time representation
     *
     * @param dateText the input date string
     * @return the Date object
     */
    public static DateTime getDateFromDb(String dateText) {
        return DateTime.parse(dateText, DateTimeFormat.forPattern(DATE_FORMAT).withOffsetParsed());
    }

    public static final class WorkTimeEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_WORK_TIME).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_WORK_TIME;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_WORK_TIME;


        public static final String TABLE_NAME = "worktime";

        public static final String COLUMN_START_DATE_TEXT = "start_date";
        public static final String COLUMN_END_DATE_TEXT = "end_date";

        public static Uri buildWorkTimeUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

}
