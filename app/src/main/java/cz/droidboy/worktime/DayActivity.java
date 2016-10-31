package cz.droidboy.worktime;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.droidboy.worktime.data.WorkTimeManager;
import cz.droidboy.worktime.model.WorkTime;
import cz.droidboy.worktime.util.GraphValueFormatter;
import cz.droidboy.worktime.util.PrefUtils;
import cz.droidboy.worktime.util.WorkTimeUtils;


public class DayActivity extends AppCompatActivity {

    private static final String TAG = DayActivity.class.getSimpleName();

    public static final String DATE_KEY = "selected_date";
    private static final int DAY_MINUTES = 24 * 60;
    private static final int REFRESH_INTERVAL = 60_000; //millis

    private WorkTimeManager mWorkTimeManager;
    private Handler refreshHandler = new Handler();

    private Runnable mRefreshGraphRunnable = new Runnable() {
        @Override
        public void run() {
            if (mChart != null) {
                setGraphData();
                if (WorkTimeUtils.dayIsToday(mDateDisplayed)) {
                    refreshHandler.postDelayed(mRefreshGraphRunnable, REFRESH_INTERVAL);
                }
            }
        }
    };

    private DateTimeFormatter mDateTimeFormatter;
    private LocalDate mDateDisplayed;
    private PieChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);

        if (!PrefUtils.hasSavedFilterData(this)) {
            startActivity(new Intent(this, SettingsActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
            return;
        }

        mWorkTimeManager = new WorkTimeManager(this);
        mDateTimeFormatter = DateTimeFormat.mediumDate();

        mDateDisplayed = (LocalDate) getIntent().getSerializableExtra(DATE_KEY);
        getSupportActionBar().setTitle(mDateTimeFormatter.print(mDateDisplayed));

        mChart = (PieChart) findViewById(R.id.main_graph);
        mChart.setCenterTextSize(36);
        mChart.setUsePercentValues(false);
        mChart.setDescription("");
        mChart.getLegend().setEnabled(false);

        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColorTransparent(true);

        mChart.setTransparentCircleColor(Color.WHITE);

        mChart.setHoleRadius(32f);
        mChart.setTransparentCircleRadius(36f);

        mChart.setDrawCenterText(true);
        mChart.setCenterTextColor(getResources().getColor(R.color.primary_dark));
        //mChart.setCenterText(mDateTimeFormatter.print(mDateDisplayed));

        // enable rotation of the chart by touch
        mChart.setRotationEnabled(false);

        // add a selection listener
        //mChart.setOnChartValueSelectedListener(this);

        startService(new Intent(this, WiFiDetectionService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mChart != null) {
            setGraphData();
            if (WorkTimeUtils.dayIsToday(mDateDisplayed)) {
                refreshHandler.postDelayed(mRefreshGraphRunnable, REFRESH_INTERVAL);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        refreshHandler.removeCallbacks(mRefreshGraphRunnable);
    }

    private void setGraphData() {
        setData(mWorkTimeManager.getWorkTimesInDay(mDateDisplayed));
        mChart.clearAnimation();
    }

    private void setData(List<WorkTime> workTimes) {
        List<Entry> yVals1 = new ArrayList<>();

        // IMPORTANT: In a PieChart, no values (Entry) should have the same
        // xIndex (even if from different DataSets), since no values can be
        // drawn above each other.
        int overallTimeMins = WorkTimeUtils.calculateDayWorktimeMinutes(workTimes, mDateDisplayed);
        yVals1.add(new Entry(overallTimeMins, 0));

        //pokud je to dnes, tak rest pocitam z uplynuleho casu, jinak beru 24hodin
        yVals1.add(new Entry((WorkTimeUtils.dayIsToday(mDateDisplayed) ? (LocalTime.now().getMillisOfDay() / 60_000) : DAY_MINUTES) - overallTimeMins, 1));

        List<String> xVals = Arrays.asList("worktime", "rest");

        PieDataSet dataSet = new PieDataSet(yVals1, null);
        dataSet.setSliceSpace(0f);
        dataSet.setSelectionShift(8f);

        dataSet.setColors(new int[]{getResources().getColor(R.color.primary), getResources().getColor(R.color.pie)});

        PieData data = new PieData(xVals, dataSet);
        data.setValueFormatter(new GraphValueFormatter());
        data.setValueTextSize(16f);
        data.setValueTextColor(Color.WHITE);
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
