package cz.droidboy.worktime;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Highlight;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;

import cz.droidboy.worktime.data.WorkTimeManager;
import cz.droidboy.worktime.ui.WeekBarChart;
import cz.droidboy.worktime.util.GraphValueFormatter;
import cz.droidboy.worktime.util.PrefUtils;
import cz.droidboy.worktime.util.WorkTimeUtils;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REFRESH_INTERVAL = 60_000; //millis
    private Handler mRefreshHandler = new Handler();
    private WorkTimeManager mWorkTimeManager;
    private DateTimeFormatter mDateTimeFormatter;
    private LocalDate mDateNow;
    private WeekBarChart mChart;
    private Runnable mRefreshGraphRunnable = new Runnable() {
        @Override
        public void run() {
            if (mChart != null) {
                setGraphData();
                mRefreshHandler.postDelayed(mRefreshGraphRunnable, REFRESH_INTERVAL);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!PrefUtils.hasSavedFilterData(this)) {
            startActivity(new Intent(this, SettingsActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
            return;
        }

        mWorkTimeManager = new WorkTimeManager(this);

        mDateTimeFormatter = DateTimeFormat.shortDate();
        mDateNow = LocalDate.now();
        mChart = (WeekBarChart) findViewById(R.id.main_graph);
        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                if (e == null || e.getVal() < 1f) {
                    return;
                }
                int xIndex = e.getXIndex();
                Intent intent = new Intent(MainActivity.this, DayActivity.class)
                        .putExtra(DayActivity.DATE_KEY, mDateNow.minusDays(xIndex));
                startActivity(intent);
            }

            @Override
            public void onNothingSelected() {
            }
        });
        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);
        mChart.setDescription("");
        mChart.setPinchZoom(false);
        mChart.setDragEnabled(false);
        mChart.setDoubleTapToZoomEnabled(false);
        mChart.setScaleEnabled(false);
        mChart.setDescriptionColor(getResources().getColor(R.color.primary_text));
        mChart.setDrawGridBackground(false);
        mChart.setDrawValueAboveBar(false);

        XAxis xl = mChart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setDrawAxisLine(false);
        xl.setDrawGridLines(false);
        xl.setTextSize(12f);
        xl.setXOffset(14f);

        YAxis yl = mChart.getAxisLeft();
        yl.setDrawAxisLine(false);
        yl.setDrawGridLines(false);
        yl.setDrawLabels(false);

        YAxis yr = mChart.getAxisRight();
        yr.setDrawAxisLine(false);
        yr.setDrawGridLines(false);
        yr.setDrawLabels(false);

        mChart.getLegend().setEnabled(false);

        if (savedInstanceState == null) { //animate on first start
            mChart.animateY(1000);
        }

        startService(new Intent(this, WiFiDetectionService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mChart != null) {
            setGraphData();
            mRefreshHandler.postDelayed(mRefreshGraphRunnable, REFRESH_INTERVAL);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        mRefreshHandler.removeCallbacks(mRefreshGraphRunnable);
    }

    private void setGraphData() {
        setData();
        mChart.clearAnimation();
    }

    private void setData() {
        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<BarEntry> yVals1 = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = mDateNow.minusDays(i);
            xVals.add(day.dayOfWeek().getAsText());
            yVals1.add(new BarEntry(WorkTimeUtils.calculateDayWorktimeMinutes(mWorkTimeManager.getWorkTimesInDay(day), day), i));
        }

        BarDataSet set1 = new BarDataSet(yVals1, "DataSet");
        set1.setBarSpacePercent(32f);
        set1.setColor(getResources().getColor(R.color.primary));
        set1.setHighLightColor(getResources().getColor(R.color.primary_light));

        ArrayList<BarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(xVals, dataSets);
        data.setValueTextSize(12f);
        data.setValueTextColor(getResources().getColor(android.R.color.white));
        data.setValueFormatter(new GraphValueFormatter());

        mChart.setData(data);
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
