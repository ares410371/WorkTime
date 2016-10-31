package cz.droidboy.worktime.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.listener.BarLineChartTouchListener;
import com.github.mikephil.charting.renderer.HorizontalBarChartRenderer;
import com.github.mikephil.charting.renderer.XAxisRendererHorizontalBarChart;
import com.github.mikephil.charting.utils.Highlight;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ValueFormatter;

import java.lang.reflect.Field;
import java.util.List;

import cz.droidboy.worktime.R;
import timber.log.Timber;

/**
 * @author Jonas Sevcik
 */
public class WeekBarChart extends HorizontalBarChart {

    private static final float ABOVE_BAR_LIMIT = 120f;

    public WeekBarChart(Context context) {
        super(context);
    }

    public WeekBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WeekBarChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void highlightTouch(Highlight high) {
        super.highlightTouch(high);
        Field mLastHighlighted;
        try {
            mLastHighlighted = mListener.getClass().getDeclaredField("mLastHighlighted");
            mLastHighlighted.setAccessible(true);
            mLastHighlighted.set(mListener, null);

        } catch (NoSuchFieldException e) {
            Timber.e(e, "Cannot access mLastHighlighted");
        } catch (IllegalAccessException e) {
            Timber.e(e, "Cannot set mLastHighlighted to null");
        }
    }

    @Override
    protected void init() {
        super.init();

        mXAxisRenderer = new XAxisRendererHorizontalBarChart(mViewPortHandler, mXAxis,
                mLeftAxisTransformer, this) {

            @Override
            public void computeAxis(float xValAverageLength, List<String> xValues) {
                super.computeAxis(xValAverageLength, xValues);
                mXAxis.mLabelWidth = (int) (Utils.calcTextWidth(mAxisLabelPaint, mXAxis.getLongestLabel()) + mXAxis.getXOffset());
                mXAxis.mLabelHeight -= Utils.convertDpToPixel(1f);
            }
        };
        mRenderer = new HorizontalBarChartRenderer(this, mAnimator, mViewPortHandler) {

            @Override
            public void drawValues(Canvas c) {
                // if values are drawn
                if (passesCheck()) {

                    List<BarDataSet> dataSets = mChart.getBarData().getDataSets();

                    final float valueOffsetPlus = Utils.convertDpToPixel(8f);
                    float posOffset;
                    float negOffset;

                    for (int i = 0; i < mChart.getBarData().getDataSetCount(); i++) {

                        BarDataSet dataSet = dataSets.get(i);

                        if (!dataSet.isDrawValuesEnabled())
                            continue;

                        boolean isInverted = mChart.isInverted(dataSet.getAxisDependency());

                        // apply the text-styling defined by the DataSet
                        applyValueTextStyle(dataSet);

                        ValueFormatter formatter = dataSet.getValueFormatter();

                        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

                        List<BarEntry> entries = dataSet.getYVals();

                        float[] valuePoints = getTransformedValues(trans, entries, i);

                        // if only single values are drawn (sum)
                        if (!mChart.isDrawValuesForWholeStackEnabled()) {

                            for (int j = 0; j < valuePoints.length * mAnimator.getPhaseX(); j += 2) {

                                if (!mViewPortHandler.isInBoundsX(valuePoints[j]))
                                    continue;

                                if (!mViewPortHandler.isInBoundsTop(valuePoints[j + 1]))
                                    break;

                                if (!mViewPortHandler.isInBoundsBottom(valuePoints[j + 1]))
                                    continue;

                                float val = entries.get(j / 2).getVal();
                                String valueText = formatter.getFormattedValue(val);

                                // calculate the correct offset depending on the draw position of the value
                                float valueTextWidth = Utils.calcTextWidth(mValuePaint, valueText);

                                boolean drawValueAboveBar = val < ABOVE_BAR_LIMIT;
                                setPaint(drawValueAboveBar);

                                posOffset = (drawValueAboveBar ? valueOffsetPlus : -(valueTextWidth + valueOffsetPlus) + Utils.convertDpToPixel(24f));
                                negOffset = (drawValueAboveBar ? -(valueTextWidth + valueOffsetPlus) : valueOffsetPlus);

                                if (isInverted)
                                {
                                    posOffset = -posOffset - valueTextWidth;
                                    negOffset = -negOffset - valueTextWidth;
                                }

                                drawValue(c, valueText, valuePoints[j] + (val >= 0 ? posOffset : negOffset),
                                        valuePoints[j + 1]);
                            }

                            // if each value of a potential stack should be drawn
                        } else {

                            for (int j = 0; j < (valuePoints.length - 1) * mAnimator.getPhaseX(); j += 2) {

                                BarEntry e = entries.get(j / 2);

                                float[] vals = e.getVals();

                                // we still draw stacked bars, but there is one
                                // non-stacked
                                // in between
                                if (vals == null) {

                                    if (!mViewPortHandler.isInBoundsX(valuePoints[j]))
                                        continue;

                                    if (!mViewPortHandler.isInBoundsTop(valuePoints[j + 1]))
                                        break;

                                    if (!mViewPortHandler.isInBoundsBottom(valuePoints[j + 1]))
                                        continue;

                                    float val = e.getVal();
                                    String valueText = formatter.getFormattedValue(val);

                                    boolean drawValueAboveBar = val < ABOVE_BAR_LIMIT;

                                    setPaint(drawValueAboveBar);

                                    // calculate the correct offset depending on the draw position of the value
                                    float valueTextWidth = Utils.calcTextWidth(mValuePaint, valueText);
                                    posOffset = (drawValueAboveBar ? valueOffsetPlus : -(valueTextWidth + valueOffsetPlus) + Utils.convertDpToPixel(24f));
                                    negOffset = (drawValueAboveBar ? -(valueTextWidth + valueOffsetPlus) : valueOffsetPlus);

                                    if (isInverted)
                                    {
                                        posOffset = -posOffset - valueTextWidth;
                                        negOffset = -negOffset - valueTextWidth;
                                    }

                                    drawValue(c, valueText, valuePoints[j]
                                                    + (e.getVal() >= 0 ? posOffset : negOffset),
                                            valuePoints[j + 1]);

                                } else {

                                    float[] transformed = new float[vals.length * 2];
                                    int cnt = 0;
                                    float add = e.getVal();

                                    for (int k = 0; k < transformed.length; k += 2) {

                                        add -= vals[cnt];
                                        transformed[k] = (vals[cnt] + add) * mAnimator.getPhaseY();
                                        cnt++;
                                    }

                                    trans.pointValuesToPixel(transformed);

                                    for (int k = 0; k < transformed.length; k += 2) {

                                        float val = vals[k / 2];
                                        String valueText = formatter.getFormattedValue(val);

                                        boolean drawValueAboveBar = val < ABOVE_BAR_LIMIT;
                                        setPaint(drawValueAboveBar);

                                        // calculate the correct offset depending on the draw position of the value
                                        float valueTextWidth = Utils.calcTextWidth(mValuePaint, valueText);
                                        posOffset = (drawValueAboveBar ? valueOffsetPlus : -(valueTextWidth + valueOffsetPlus) + Utils.convertDpToPixel(24f));
                                        negOffset = (drawValueAboveBar ? -(valueTextWidth + valueOffsetPlus) : valueOffsetPlus);

                                        if (isInverted)
                                        {
                                            posOffset = -posOffset - valueTextWidth;
                                            negOffset = -negOffset - valueTextWidth;
                                        }

                                        float x = transformed[k]
                                                + (val >= 0 ? posOffset : negOffset);
                                        float y = valuePoints[j + 1];

                                        if (!mViewPortHandler.isInBoundsX(x))
                                            continue;

                                        if (!mViewPortHandler.isInBoundsTop(y))
                                            break;

                                        if (!mViewPortHandler.isInBoundsBottom(y))
                                            continue;

                                        drawValue(c, valueText, x, y);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            private void setPaint(boolean drawValueAboveBar) {
                if (drawValueAboveBar) {
                    mValuePaint.setColor(getResources().getColor(R.color.primary_text));
                    mValuePaint.setTextAlign(Paint.Align.LEFT);
                } else {
                    mValuePaint.setColor(getResources().getColor(android.R.color.white));
                    mValuePaint.setTextAlign(Paint.Align.RIGHT);
                }
            }

            @Override
            protected void drawValue(Canvas c, String value, float xPos, float yPos) {
                super.drawValue(c, value, xPos, yPos + Utils.convertDpToPixel(6f));
            }
        };

    }

}
