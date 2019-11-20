/* Copyright 2017 Thomas Schneider
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


import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import app.fedilab.android.R;
import app.fedilab.android.asynctasks.RetrieveFeedsAsyncTask;
import app.fedilab.android.asynctasks.RetrieveStatsAsyncTask;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Statistics;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.drawers.StatusListAdapter;
import app.fedilab.android.helper.FilterToots;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnRetrieveFeedsInterface;
import app.fedilab.android.interfaces.OnRetrieveStatsInterface;
import app.fedilab.android.services.BackupStatusInDataBaseService;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.StatusCacheDAO;
import es.dmoral.toasty.Toasty;


/**
 * Created by Thomas on 17/02/2018.
 * Show owner's toots
 */

public class OwnerStatusActivity extends BaseActivity implements OnRetrieveFeedsInterface, OnRetrieveStatsInterface {


    LinearLayoutManager mLayoutManager;
    private ImageView pp_actionBar;
    private StatusListAdapter statusListAdapter;
    private String max_id;
    private List<Status> statuses;
    private RelativeLayout mainLoader, nextElementLoader, textviewNoAction;
    private boolean firstLoad;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean swiped;
    private boolean flag_loading;
    private int style;
    private Button settings_time_from, settings_time_to;
    private FilterToots filterToots;
    private Date dateIni, dateEnd;
    private View statsDialogView;
    private Statistics statistics;
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
    private BroadcastReceiver backupFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            max_id = null;
            firstLoad = true;
            flag_loading = true;
            swiped = true;
            new RetrieveFeedsAsyncTask(OwnerStatusActivity.this, filterToots, null, OwnerStatusActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme) {
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme_NoActionBar_Fedilab);
                break;
            case Helper.THEME_BLACK:
                setTheme(R.style.AppThemeBlack_NoActionBar);
                break;
            default:
                setTheme(R.style.AppThemeDark_NoActionBar);
        }
        setContentView(R.layout.activity_ower_status);

        filterToots = new FilterToots();

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(backupFinishedReceiver,
                        new IntentFilter(Helper.INTENT_BACKUP_FINISH));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View view = inflater.inflate(R.layout.toot_action_bar, new LinearLayout(getApplicationContext()), false);
            view.setBackground(new ColorDrawable(ContextCompat.getColor(OwnerStatusActivity.this, R.color.cyanea_primary)));
            toolbar.setBackgroundColor(ContextCompat.getColor(OwnerStatusActivity.this, R.color.cyanea_primary));
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

            ImageView close_toot = actionBar.getCustomView().findViewById(R.id.close_toot);
            close_toot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            TextView toolbarTitle = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            pp_actionBar = actionBar.getCustomView().findViewById(R.id.pp_actionBar);
            toolbarTitle.setText(getString(R.string.owner_cached_toots));
        }
        statuses = new ArrayList<>();
        RecyclerView lv_status = findViewById(R.id.lv_status);
        mainLoader = findViewById(R.id.loader);
        nextElementLoader = findViewById(R.id.loading_next_status);
        textviewNoAction = findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);
        max_id = null;
        flag_loading = true;
        firstLoad = true;
        swiped = false;
        boolean isOnWifi = Helper.isOnWIFI(OwnerStatusActivity.this);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, null);
        statusListAdapter = new StatusListAdapter(RetrieveFeedsAsyncTask.Type.CACHE_STATUS, userId, isOnWifi, this.statuses);
        lv_status.setAdapter(statusListAdapter);
        mLayoutManager = new LinearLayoutManager(OwnerStatusActivity.this);
        lv_status.setLayoutManager(mLayoutManager);


        if (theme == Helper.THEME_DARK) {
            style = R.style.DialogDark;
        } else if (theme == Helper.THEME_BLACK) {
            style = R.style.DialogBlack;
        } else {
            style = R.style.Dialog;
        }

        SQLiteDatabase db = Sqlite.getInstance(OwnerStatusActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        Account account = new AccountDAO(OwnerStatusActivity.this, db).getUniqAccount(userId, instance);

        Helper.loadGiF(getApplicationContext(), account.getAvatar(), pp_actionBar);

        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        int c1 = getResources().getColor(R.color.cyanea_accent);
        int c2 = getResources().getColor(R.color.cyanea_primary_dark);
        int c3 = getResources().getColor(R.color.cyanea_primary);
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(c3);
        swipeRefreshLayout.setColorSchemeColors(
                c1, c2, c1
        );
        new RetrieveFeedsAsyncTask(OwnerStatusActivity.this, filterToots, null, OwnerStatusActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                max_id = null;
                firstLoad = true;
                flag_loading = true;
                swiped = true;
                new RetrieveFeedsAsyncTask(OwnerStatusActivity.this, filterToots, null, OwnerStatusActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        lv_status.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                if (dy > 0) {
                    int visibleItemCount = mLayoutManager.getChildCount();
                    int totalItemCount = mLayoutManager.getItemCount();
                    if (firstVisibleItem + visibleItemCount == totalItemCount) {
                        if (!flag_loading) {
                            flag_loading = true;
                            new RetrieveFeedsAsyncTask(OwnerStatusActivity.this, filterToots, max_id, OwnerStatusActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            nextElementLoader.setVisibility(View.VISIBLE);
                        }
                    } else {
                        nextElementLoader.setVisibility(View.GONE);
                    }
                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(@NotNull Menu menu) {
        getMenuInflater().inflate(R.menu.option_owner_cache, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_sync:
                Intent backupIntent = new Intent(OwnerStatusActivity.this, BackupStatusInDataBaseService.class);
                startService(backupIntent);
                statistics = null;
                return true;
            case R.id.action_stats:
                SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
                int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
                if (theme == Helper.THEME_DARK) {
                    style = R.style.DialogDark;
                } else if (theme == Helper.THEME_BLACK) {
                    style = R.style.DialogBlack;
                } else {
                    style = R.style.Dialog;
                }
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(OwnerStatusActivity.this, style);
                LayoutInflater inflater = this.getLayoutInflater();
                statsDialogView = inflater.inflate(R.layout.stats_owner_toots, null);
                dialogBuilder.setView(statsDialogView);
                dialogBuilder
                        .setTitle(R.string.action_stats)
                        .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                dialogBuilder.create().show();
                if (statistics == null) {
                    new RetrieveStatsAsyncTask(getApplicationContext(), OwnerStatusActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    displayStats();
                }
                return true;
            case R.id.action_filter:
                sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
                theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
                if (theme == Helper.THEME_DARK) {
                    style = R.style.DialogDark;
                } else if (theme == Helper.THEME_BLACK) {
                    style = R.style.DialogBlack;
                } else {
                    style = R.style.Dialog;
                }
                dialogBuilder = new AlertDialog.Builder(OwnerStatusActivity.this, style);
                inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.filter_owner_toots, new LinearLayout(getApplicationContext()), false);
                dialogBuilder.setView(dialogView);


                SQLiteDatabase db = Sqlite.getInstance(OwnerStatusActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                if (dateIni == null)
                    dateIni = new StatusCacheDAO(OwnerStatusActivity.this, db).getSmallerDate(StatusCacheDAO.ARCHIVE_CACHE);
                if (dateEnd == null)
                    dateEnd = new StatusCacheDAO(OwnerStatusActivity.this, db).getGreaterDate(StatusCacheDAO.ARCHIVE_CACHE);
                if (dateIni == null || dateEnd == null)
                    return true;
                String dateInitString = Helper.shortDateToString(dateIni);
                String dateEndString = Helper.shortDateToString(dateEnd);

                //Initializes settings for filter
                settings_time_from = dialogView.findViewById(R.id.settings_time_from);
                settings_time_to = dialogView.findViewById(R.id.settings_time_to);

                final CheckBox filter_visibility_public = dialogView.findViewById(R.id.filter_visibility_public);
                final CheckBox filter_visibility_unlisted = dialogView.findViewById(R.id.filter_visibility_unlisted);
                final CheckBox filter_visibility_private = dialogView.findViewById(R.id.filter_visibility_private);
                final CheckBox filter_visibility_direct = dialogView.findViewById(R.id.filter_visibility_direct);

                filter_visibility_public.setChecked(filterToots.isV_public());
                filter_visibility_unlisted.setChecked(filterToots.isV_unlisted());
                filter_visibility_private.setChecked(filterToots.isV_private());
                filter_visibility_direct.setChecked(filterToots.isV_direct());

                final Spinner filter_boost = dialogView.findViewById(R.id.filter_boost);
                final Spinner filter_replies = dialogView.findViewById(R.id.filter_replies);
                final Spinner filter_media = dialogView.findViewById(R.id.filter_media);
                final Spinner filter_pinned = dialogView.findViewById(R.id.filter_pinned);
                final Spinner filter_order = dialogView.findViewById(R.id.filter_order);

                filter_boost.setSelection(filterToots.getBoosts().ordinal());
                filter_replies.setSelection(filterToots.getReplies().ordinal());
                filter_media.setSelection(filterToots.getMedia().ordinal());
                filter_pinned.setSelection(filterToots.getPinned().ordinal());
                filter_order.setSelection(filterToots.getOrder().ordinal());

                final EditText filter_keywords = dialogView.findViewById(R.id.filter_keywords);

                settings_time_from.setText(dateInitString);
                settings_time_to.setText(dateEndString);

                if (filterToots.getFilter() != null)
                    filter_keywords.setText(filterToots.getFilter());

                Calendar c = Calendar.getInstance();
                c.setTime(dateIni);
                int yearIni = c.get(Calendar.YEAR);
                int monthIni = c.get(Calendar.MONTH);
                int dayIni = c.get(Calendar.DAY_OF_MONTH);

                final DatePickerDialog dateIniPickerDialog = new DatePickerDialog(
                        OwnerStatusActivity.this, style, iniDateSetListener, yearIni, monthIni, dayIni);
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
                        OwnerStatusActivity.this, style, endDateSetListener, yearEnd, monthEnd, dayEnd);
                settings_time_to.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dateEndPickerDialog.show();
                    }
                });
                dialogBuilder
                        .setTitle(R.string.action_filter)
                        .setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                RelativeLayout no_result = findViewById(R.id.no_result);
                                no_result.setVisibility(View.GONE);

                                filterToots.setBoosts(FilterToots.typeFilter.values()[filter_boost.getSelectedItemPosition()]);
                                filterToots.setReplies(FilterToots.typeFilter.values()[filter_replies.getSelectedItemPosition()]);
                                filterToots.setMedia(FilterToots.typeFilter.values()[filter_media.getSelectedItemPosition()]);
                                filterToots.setPinned(FilterToots.typeFilter.values()[filter_pinned.getSelectedItemPosition()]);
                                filterToots.setOrder(FilterToots.typeOrder.values()[filter_order.getSelectedItemPosition()]);

                                filterToots.setV_public(filter_visibility_public.isChecked());
                                filterToots.setV_unlisted(filter_visibility_unlisted.isChecked());
                                filterToots.setV_private(filter_visibility_private.isChecked());
                                filterToots.setV_direct(filter_visibility_direct.isChecked());

                                filterToots.setDateIni(Helper.dateToString(dateIni));
                                filterToots.setDateEnd(Helper.dateToString(dateEnd));

                                if (filter_keywords.getText() != null && filter_keywords.getText().toString().trim().length() > 0)
                                    filterToots.setFilter(filter_keywords.getText().toString());
                                else
                                    filterToots.setFilter(null);
                                swipeRefreshLayout.setRefreshing(true);
                                max_id = null;
                                firstLoad = true;
                                flag_loading = true;
                                swiped = true;
                                new RetrieveFeedsAsyncTask(OwnerStatusActivity.this, filterToots, null, OwnerStatusActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                dialogBuilder.create().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRetrieveFeeds(APIResponse apiResponse) {
        mainLoader.setVisibility(View.GONE);
        nextElementLoader.setVisibility(View.GONE);
        //Discards 404 - error which can often happen due to toots which have been deleted
        if (apiResponse.getError() != null && apiResponse.getError().getStatusCode() != 404) {
            Toasty.error(getApplicationContext(), apiResponse.getError().getError(), Toast.LENGTH_LONG).show();
            swipeRefreshLayout.setRefreshing(false);
            swiped = false;
            flag_loading = false;
            return;
        }
        int previousPosition = this.statuses.size();
        List<Status> statuses = apiResponse.getStatuses();
        max_id = apiResponse.getMax_id();
        flag_loading = (max_id == null);
        if (!swiped && firstLoad && (statuses == null || statuses.size() == 0))
            textviewNoAction.setVisibility(View.VISIBLE);
        else
            textviewNoAction.setVisibility(View.GONE);

        if (swiped) {
            if (previousPosition > 0) {
                for (int i = 0; i < previousPosition; i++) {
                    this.statuses.remove(0);
                }
                statusListAdapter.notifyItemRangeRemoved(0, previousPosition);
            }
            swiped = false;
        }
        if (statuses != null && statuses.size() > 0) {
            this.statuses.addAll(statuses);
            statusListAdapter.notifyItemRangeInserted(previousPosition, statuses.size());
        } else {
            if (textviewNoAction.getVisibility() != View.VISIBLE && firstLoad) {
                RelativeLayout no_result = findViewById(R.id.no_result);
                no_result.setVisibility(View.VISIBLE);
            }
        }
        swipeRefreshLayout.setRefreshing(false);
        firstLoad = false;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(backupFinishedReceiver);
    }

    @Override
    public void onStats(Statistics statistics) {
        this.statistics = statistics;
        displayStats();
    }

    private void displayStats() {
        if (statsDialogView != null) {
            ScrollView stats_container = statsDialogView.findViewById(R.id.stats_container);
            RelativeLayout loader = statsDialogView.findViewById(R.id.loader);

            TextView total_statuses = statsDialogView.findViewById(R.id.total_statuses);
            TextView number_boosts = statsDialogView.findViewById(R.id.number_boosts);
            TextView number_replies = statsDialogView.findViewById(R.id.number_replies);
            TextView number_statuses = statsDialogView.findViewById(R.id.number_statuses);
            TextView number_with_media = statsDialogView.findViewById(R.id.number_with_media);
            TextView number_with_cw = statsDialogView.findViewById(R.id.number_with_cw);
            TextView number_with_sensitive_media = statsDialogView.findViewById(R.id.number_with_sensitive_media);
            TextView v_public = statsDialogView.findViewById(R.id.v_public);
            TextView v_unlisted = statsDialogView.findViewById(R.id.v_unlisted);
            TextView v_private = statsDialogView.findViewById(R.id.v_private);
            TextView v_direct = statsDialogView.findViewById(R.id.v_direct);

            TextView frequency = statsDialogView.findViewById(R.id.frequency);
            TextView last_toot_date = statsDialogView.findViewById(R.id.last_toot_date);
            TextView first_toot_date = statsDialogView.findViewById(R.id.first_toot_date);
            TextView tags = statsDialogView.findViewById(R.id.tags);

            ImageButton charts = statsDialogView.findViewById(R.id.charts);
            charts.setOnClickListener(w -> {
                Intent intent = new Intent(OwnerStatusActivity.this, OwnerChartsActivity.class);
                startActivity(intent);
            });

            total_statuses.setText(String.valueOf(statistics.getTotal_statuses()));
            number_boosts.setText(String.valueOf(statistics.getNumber_boosts()));
            number_replies.setText(String.valueOf(statistics.getNumber_replies()));
            number_statuses.setText(String.valueOf(statistics.getNumber_status()));
            number_with_media.setText(String.valueOf(statistics.getNumber_with_media()));
            number_with_cw.setText(String.valueOf(statistics.getNumber_with_cw()));
            number_with_sensitive_media.setText(String.valueOf(statistics.getNumber_with_sensitive_media()));
            v_public.setText(String.valueOf(statistics.getV_public()));
            v_unlisted.setText(String.valueOf(statistics.getV_unlisted()));
            v_private.setText(String.valueOf(statistics.getV_private()));
            v_direct.setText(String.valueOf(statistics.getV_direct()));


            first_toot_date.setText(Helper.dateToString(statistics.getFirstTootDate()));
            last_toot_date.setText(Helper.dateToString(statistics.getLastTootDate()));
            DecimalFormat df = new DecimalFormat("#.##");
            frequency.setText(getString(R.string.toot_per_day, df.format(statistics.getFrequency())));

            if (statistics.getTagsTrend() != null && statistics.getTagsTrend().size() > 0) {
                Iterator it = statistics.getTagsTrend().entrySet().iterator();
                StringBuilder text = new StringBuilder();
                int i = 1;
                while (it.hasNext() && i <= 10) {
                    Map.Entry pair = (Map.Entry) it.next();
                    System.out.println(pair.getKey() + " = " + pair.getValue());
                    text.append(i).append(" - ").append(pair.getKey()).append(" → ").append(pair.getValue()).append("\r\n");
                    i++;
                }
                tags.setText(text.toString());
            } else {
                tags.setText(getString(R.string.no_tags));
            }

            stats_container.setVisibility(View.VISIBLE);
            loader.setVisibility(View.GONE);

        } else {
            Toasty.error(getApplicationContext(), getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
        }
    }
}
