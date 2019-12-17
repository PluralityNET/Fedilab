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


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.R;
import app.fedilab.android.asynctasks.RetrieveFeedsAsyncTask;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.drawers.StatusListAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnRetrieveFeedsInterface;
import app.fedilab.android.sqlite.SearchDAO;
import app.fedilab.android.sqlite.Sqlite;
import es.dmoral.toasty.Toasty;


/**
 * Created by Thomas on 27/05/2017.
 * Show hashtag stream
 */

public class HashTagActivity extends BaseActivity implements OnRetrieveFeedsInterface {


    public static int position;
    private StatusListAdapter statusListAdapter;
    private String max_id;
    private List<Status> statuses;
    private RelativeLayout mainLoader, nextElementLoader, textviewNoAction;
    private boolean firstLoad;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String tag;
    private int tootsPerPage;
    private boolean flag_loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
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

        setContentView(R.layout.activity_hashtag);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle b = getIntent().getExtras();
        if (b != null) {
            tag = b.getString("tag", null);
        }
        if (tag == null)
            finish();
        statuses = new ArrayList<>();
        max_id = null;
        flag_loading = true;
        firstLoad = true;
        boolean isOnWifi = Helper.isOnWIFI(getApplicationContext());
        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        int c1 = getResources().getColor(R.color.cyanea_accent);
        int c2 = getResources().getColor(R.color.cyanea_primary_dark);
        int c3 = getResources().getColor(R.color.cyanea_primary);
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(c3);
        swipeRefreshLayout.setColorSchemeColors(
                c1, c2, c1
        );
        toolbar.setBackgroundColor(ContextCompat.getColor(HashTagActivity.this, R.color.cyanea_primary));
        final RecyclerView lv_status = findViewById(R.id.lv_status);
        tootsPerPage = sharedpreferences.getInt(Helper.SET_TOOT_PER_PAGE, Helper.TOOTS_PER_PAGE);
        mainLoader = findViewById(R.id.loader);
        nextElementLoader = findViewById(R.id.loading_next_status);
        textviewNoAction = findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);
        statusListAdapter = new StatusListAdapter(RetrieveFeedsAsyncTask.Type.TAG, null, isOnWifi, this.statuses);
        lv_status.setAdapter(statusListAdapter);
        setTitle(String.format("#%s", tag));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                max_id = null;
                statuses = new ArrayList<>();
                firstLoad = true;
                flag_loading = true;
                new RetrieveFeedsAsyncTask(getApplicationContext(), RetrieveFeedsAsyncTask.Type.TAG, tag, null, max_id, HashTagActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        final LinearLayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(this);
        lv_status.setLayoutManager(mLayoutManager);
        lv_status.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    int visibleItemCount = mLayoutManager.getChildCount();
                    int totalItemCount = mLayoutManager.getItemCount();
                    int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                    if (firstVisibleItem + visibleItemCount == totalItemCount) {
                        if (!flag_loading) {
                            flag_loading = true;
                            new RetrieveFeedsAsyncTask(getApplicationContext(), RetrieveFeedsAsyncTask.Type.TAG, tag, null, max_id, HashTagActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                            nextElementLoader.setVisibility(View.VISIBLE);
                        }
                    } else {
                        nextElementLoader.setVisibility(View.GONE);
                    }
                }
            }
        });
        new RetrieveFeedsAsyncTask(getApplicationContext(), RetrieveFeedsAsyncTask.Type.TAG, tag, null, max_id, HashTagActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(@NotNull Menu menu) {
        getMenuInflater().inflate(R.menu.tag_pin, menu);

        SQLiteDatabase db = Sqlite.getInstance(HashTagActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        List<String> searchInDb = new SearchDAO(HashTagActivity.this, db).getSearchByKeyword(tag.trim());
        if (searchInDb != null && searchInDb.size() > 0) {
            menu.findItem(R.id.action_pin).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_pin:
                SQLiteDatabase db = Sqlite.getInstance(HashTagActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                new SearchDAO(HashTagActivity.this, db).insertSearch(tag);
                Intent intent = new Intent(HashTagActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Helper.INTENT_ACTION, Helper.SEARCH_TAG);
                intent.putExtra(Helper.SEARCH_KEYWORD, tag);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRetrieveFeeds(APIResponse apiResponse) {

        mainLoader.setVisibility(View.GONE);
        nextElementLoader.setVisibility(View.GONE);
        if (apiResponse == null || apiResponse.getError() != null) {
            if (apiResponse != null)
                Toasty.error(getApplicationContext(), apiResponse.getError().getError(), Toast.LENGTH_LONG).show();
            else
                Toasty.error(getApplicationContext(), getString(R.string.toast_error), Toast.LENGTH_LONG).show();
            return;
        }
        List<Status> statuses = apiResponse.getStatuses();
        if (firstLoad && (statuses == null || statuses.size() == 0))
            textviewNoAction.setVisibility(View.VISIBLE);
        else
            textviewNoAction.setVisibility(View.GONE);
        if (statuses != null && statuses.size() > 1)
            max_id = statuses.get(statuses.size() - 1).getId();
        else
            max_id = null;
        if (statuses != null) {
            this.statuses.addAll(statuses);
            statusListAdapter.notifyDataSetChanged();
        }
        swipeRefreshLayout.setRefreshing(false);
        firstLoad = false;
        flag_loading = statuses != null && statuses.size() < tootsPerPage;
    }

}
