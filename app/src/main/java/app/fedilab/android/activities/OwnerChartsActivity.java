/* Copyright 2019 Thomas Schneider
 *
 * This file is a part of Fedilab
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Fedilab is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Fedilab; if not,
 * see <http://www.gnu.org/licenses>. */
package app.fedilab.android.activities;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import app.fedilab.android.R;
import app.fedilab.android.asynctasks.RetrieveChartsAsyncTask;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Charts;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnRetrieveChartsInterface;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.StatusCacheDAO;
import es.dmoral.toasty.Toasty;


/**
 * Created by Thomas on 28/07/2019.
 * Charts for owner activity
 */

public class OwnerChartsActivity extends BaseActivity implements OnRetrieveChartsInterface {


    LinearLayoutManager mLayoutManager;
    private Button settings_time_from, settings_time_to;
    private Date dateIni, dateEnd;
    private LineChart chart;
    private int theme;
    private RelativeLayout loader;
    private ImageButton validate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme) {
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme);
                break;
            case Helper.THEME_DARK:
                setTheme(R.style.AppThemeDark);
                break;
            case Helper.THEME_BLACK:
                setTheme(R.style.AppThemeBlack);
                break;
            default:
                setTheme(R.style.AppThemeDark);
        }
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View view = inflater.inflate(R.layout.simple_action_bar, new LinearLayout(getApplicationContext()), false);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            ImageView toolbar_close = actionBar.getCustomView().findViewById(R.id.close_conversation);
            ImageView pp_actionBar = actionBar.getCustomView().findViewById(R.id.pp_actionBar);
            TextView toolbar_title = actionBar.getCustomView().findViewById(R.id.toolbar_title);


            SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, null);
            Account account = new AccountDAO(getApplicationContext(), db).getUniqAccount(userId, instance);
            if (account != null) {
                Helper.loadGiF(getApplicationContext(), account.getAvatar(), pp_actionBar);
            }

            toolbar_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            if (account != null) {
                toolbar_title.setText(getString(R.string.owner_charts) + " - " + account.getUsername() + "@" + account.getInstance());
            } else {
                toolbar_title.setText(R.string.owner_charts);
            }
        }
        setContentView(R.layout.activity_ower_charts);


        chart = findViewById(R.id.chart);
        settings_time_from = findViewById(R.id.settings_time_from);
        settings_time_to = findViewById(R.id.settings_time_to);
        loader = findViewById(R.id.loader);
        validate = findViewById(R.id.validate);

        SQLiteDatabase db = Sqlite.getInstance(OwnerChartsActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        dateIni = new StatusCacheDAO(OwnerChartsActivity.this, db).getSmallerDate(StatusCacheDAO.ARCHIVE_CACHE);
        dateEnd = new StatusCacheDAO(OwnerChartsActivity.this, db).getGreaterDate(StatusCacheDAO.ARCHIVE_CACHE);


        int style;
        if (theme == Helper.THEME_DARK) {
            style = R.style.DialogDark;
        } else if (theme == Helper.THEME_BLACK) {
            style = R.style.DialogBlack;
        } else {
            style = R.style.Dialog;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(dateIni);
        int yearIni = c.get(Calendar.YEAR);
        int monthIni = c.get(Calendar.MONTH);
        int dayIni = c.get(Calendar.DAY_OF_MONTH);

        final DatePickerDialog dateIniPickerDialog = new DatePickerDialog(
                OwnerChartsActivity.this, style, iniDateSetListener, yearIni, monthIni, dayIni);
        settings_time_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateIniPickerDialog.show();
            }
        });


        Calendar ce = Calendar.getInstance();
        c.setTime(dateEnd);
        int yearEnd = ce.get(Calendar.YEAR);
        int monthEnd = ce.get(Calendar.MONTH);
        int dayEnd = ce.get(Calendar.DAY_OF_MONTH);
        final DatePickerDialog dateEndPickerDialog = new DatePickerDialog(
                OwnerChartsActivity.this, style, endDateSetListener, yearEnd, monthEnd, dayEnd);
        settings_time_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateEndPickerDialog.show();
            }
        });

        dateIniPickerDialog.getDatePicker().setMinDate(dateIni.getTime());
        dateIniPickerDialog.getDatePicker().setMaxDate(dateEnd.getTime());

        dateEndPickerDialog.getDatePicker().setMinDate(dateIni.getTime());
        dateEndPickerDialog.getDatePicker().setMaxDate(dateEnd.getTime());

        Calendar cal = Calendar.getInstance();
        cal.setTime(dateEnd);
        cal.add(Calendar.MONTH, -1);
        Date result = cal.getTime();
        if (result.after(dateIni))
            dateIni = result;

        if (dateIni == null) {
            dateIni = new Date();
        }
        if (dateEnd == null) {
            dateEnd = new Date();
        }


        CustomMarkerView mv = new CustomMarkerView(getApplicationContext(), R.layout.markerview);
        chart.setMarkerView(mv);

        validate.setOnClickListener(v -> {
            loadGraph(dateIni, dateEnd);
        });

        loadGraph(dateIni, dateEnd);


    }

    public class CustomMarkerView extends MarkerView {
        private TextView tvContent;

        public CustomMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            tvContent = findViewById(R.id.tvContent);
            tvContent.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            Date date = new Date(((long) e.getX()));
            tvContent.setText(String.valueOf(Helper.shortDateToString(date) + " - " + (int) e.getY()));
            super.refreshContent(e, highlight);
        }

        private MPPointF mOffset;

        @Override
        public MPPointF getOffset() {
            if (mOffset == null) {
                mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
            }
            return mOffset;
        }
    }

    private DatePickerDialog.OnDateSetListener iniDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {
                    Calendar c = Calendar.getInstance();
                    c.set(year, monthOfYear, dayOfMonth, 0, 0);
                    dateIni = new Date(c.getTimeInMillis());
                    settings_time_from.setText(Helper.shortDateToString(new Date(c.getTimeInMillis())));
                }

            };
    private DatePickerDialog.OnDateSetListener endDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {
                    Calendar c = Calendar.getInstance();
                    c.set(year, monthOfYear, dayOfMonth, 23, 59);

                    dateEnd = new Date(c.getTimeInMillis());
                    settings_time_to.setText(Helper.shortDateToString(new Date(c.getTimeInMillis())));
                }

            };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadGraph(Date dateIni, Date dateEnd) {
        String dateInitString = Helper.shortDateToString(dateIni);
        String dateEndString = Helper.shortDateToString(dateEnd);

        settings_time_from.setText(dateInitString);
        settings_time_to.setText(dateEndString);
        chart.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);
        validate.setEnabled(false);
        new RetrieveChartsAsyncTask(OwnerChartsActivity.this, dateIni, dateEnd, OwnerChartsActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onCharts(Charts charts) {

        List<Entry> boostsEntry = new ArrayList<>();
        int i = 0;
        Iterator it = charts.getBoosts().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            boostsEntry.add(new Entry((long) pair.getKey(), (int) pair.getValue()));
            it.remove();
        }

        List<Entry> repliesEntry = new ArrayList<>();
        it = charts.getReplies().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            repliesEntry.add(new Entry((long) pair.getKey(), (int) pair.getValue()));
            it.remove();
        }

        List<Entry> statusesEntry = new ArrayList<>();
        it = charts.getStatuses().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            statusesEntry.add(new Entry((long) pair.getKey(), (int) pair.getValue()));
            it.remove();
        }

        LineDataSet dataSetBoosts = new LineDataSet(boostsEntry, getString(R.string.reblog));
        dataSetBoosts.setColor(ContextCompat.getColor(OwnerChartsActivity.this, R.color.chart_boost));
        dataSetBoosts.setValueTextSize(12f);
        dataSetBoosts.setValueTextColor(ContextCompat.getColor(OwnerChartsActivity.this, R.color.chart_boost));
        dataSetBoosts.setFillColor(ContextCompat.getColor(OwnerChartsActivity.this, R.color.chart_boost));
        dataSetBoosts.setDrawValues(false);
        dataSetBoosts.setDrawFilled(true);
        dataSetBoosts.setDrawCircles(false);
        dataSetBoosts.setDrawCircleHole(false);
        dataSetBoosts.setLineWidth(2f);
        dataSetBoosts.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineDataSet dateSetReplies = new LineDataSet(repliesEntry, getString(R.string.replies));
        dateSetReplies.setColor(ContextCompat.getColor(OwnerChartsActivity.this, R.color.chart_reply));
        dateSetReplies.setValueTextSize(12f);
        dateSetReplies.setValueTextColor(ContextCompat.getColor(OwnerChartsActivity.this, R.color.chart_reply));
        dateSetReplies.setFillColor(ContextCompat.getColor(OwnerChartsActivity.this, R.color.chart_reply));
        dateSetReplies.setDrawValues(false);
        dateSetReplies.setDrawFilled(true);
        dateSetReplies.setDrawCircles(false);
        dateSetReplies.setDrawCircleHole(false);
        dateSetReplies.setLineWidth(2f);
        dateSetReplies.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineDataSet dataSetStatuses = new LineDataSet(statusesEntry, getString(R.string.statuses));
        dataSetStatuses.setColor(ContextCompat.getColor(OwnerChartsActivity.this, R.color.chart_status));
        dataSetStatuses.setValueTextSize(12f);
        dataSetStatuses.setValueTextColor(ContextCompat.getColor(OwnerChartsActivity.this, R.color.chart_status));
        dataSetStatuses.setFillColor(ContextCompat.getColor(OwnerChartsActivity.this, R.color.chart_status));
        dataSetStatuses.setDrawValues(false);
        dataSetStatuses.setDrawFilled(true);
        dataSetStatuses.setDrawCircles(false);
        dataSetStatuses.setDrawCircleHole(false);
        dataSetStatuses.setLineWidth(2f);
        dataSetStatuses.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        List<ILineDataSet> dataSets = new ArrayList<>();


        dataSets.add(dataSetBoosts);
        dataSets.add(dateSetReplies);
        dataSets.add(dataSetStatuses);

        //X axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(45);
        xAxis.setTextSize(14f);

        //Legend
        Legend legend = chart.getLegend();
        legend.setTextSize(16f);
        legend.setXEntrySpace(15f);

        //Left axis
        YAxis leftAxis = chart.getAxis(YAxis.AxisDependency.LEFT);
        leftAxis.setTextSize(14f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawAxisLine(true);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawLabels(true);
        //Remove right axis
        chart.getAxis(YAxis.AxisDependency.RIGHT).setEnabled(false);


        Description description = chart.getDescription();
        description.setEnabled(false);

        //Update colors
        switch (theme) {
            case Helper.THEME_LIGHT:
                xAxis.setTextColor(Color.BLACK);
                dataSetBoosts.setValueTextColor(Color.BLACK);
                dateSetReplies.setValueTextColor(Color.BLACK);
                dataSetStatuses.setValueTextColor(Color.BLACK);

                legend.setTextColor(Color.BLACK);
                leftAxis.setTextColor(Color.BLACK);
                break;
            case Helper.THEME_DARK:
            case Helper.THEME_BLACK:
                int color = ContextCompat.getColor(OwnerChartsActivity.this, R.color.dark_text);
                xAxis.setTextColor(color);
                dataSetBoosts.setValueTextColor(color);
                dateSetReplies.setValueTextColor(color);
                dataSetStatuses.setValueTextColor(color);
                legend.setTextColor(color);
                leftAxis.setTextColor(color);
        }

        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);

        xAxis.setValueFormatter(new MyXAxisValueFormatter());
        LineData data = new LineData(dataSets);
        chart.setData(data);
        chart.setVisibility(View.VISIBLE);
        loader.setVisibility(View.GONE);
        validate.setEnabled(true);
        chart.invalidate();
    }


    public class MyXAxisValueFormatter extends ValueFormatter {
        private DateFormat mDataFormat;
        private Date mDate;

        MyXAxisValueFormatter() {
            this.mDataFormat = new SimpleDateFormat("dd.MM", Locale.getDefault());
            this.mDate = new Date();
        }

        @Override
        public String getFormattedValue(float value) {
            return getDateString((long) value);
        }

        private String getDateString(long timestamp) {
            try {
                mDate.setTime(timestamp);
                return mDataFormat.format(mDate);
            } catch (Exception ex) {
                return "xx";
            }
        }
    }

}
