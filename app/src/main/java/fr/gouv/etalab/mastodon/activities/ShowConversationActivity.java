/* Copyright 2017 Thomas Schneider
 *
 * This file is a part of Mastalab
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastalab is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Mastalab; if not,
 * see <http://www.gnu.org/licenses>. */
package fr.gouv.etalab.mastodon.activities;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveContextAsyncTask;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Context;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.drawers.ConversationDecoration;
import fr.gouv.etalab.mastodon.drawers.StatusListAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveContextInterface;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;

import static fr.gouv.etalab.mastodon.helper.Helper.THEME_BLACK;
import static fr.gouv.etalab.mastodon.helper.Helper.THEME_LIGHT;


/**
 * Created by Thomas on 04/05/2017.
 * Show conversation activity class
 */

public class ShowConversationActivity extends BaseActivity implements  OnRetrieveContextInterface {


    private Status initialStatus;
    private Status detailsStatus;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView lv_status;
    private ImageView pp_actionBar;
    private List<Status> statuses;
    private StatusListAdapter statusListAdapter;
    private boolean expanded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme){
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme_NoActionBar);
                break;
            case Helper.THEME_DARK:
                setTheme(R.style.AppThemeDark_NoActionBar);
                break;
            case Helper.THEME_BLACK:
                setTheme(R.style.AppThemeBlack_NoActionBar);
                break;
            default:
                setTheme(R.style.AppThemeDark_NoActionBar);
        }

        setContentView(R.layout.activity_show_conversation);
        lv_status = findViewById(R.id.lv_status);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if( theme == THEME_BLACK)
            toolbar.setBackgroundColor(ContextCompat.getColor(ShowConversationActivity.this, R.color.black));
        setSupportActionBar(toolbar);

        Bundle b = getIntent().getExtras();
        statuses = new ArrayList<>();
        if(b != null) {
            detailsStatus = b.getParcelable("status");
            expanded  = b.getBoolean("expanded", false);
            initialStatus = b.getParcelable("initialStatus");
        }
        if( detailsStatus == null || detailsStatus.getId() == null)
            finish();

        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if( getSupportActionBar() != null) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.conversation_action_bar, null);
            getSupportActionBar().setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            TextView title = getSupportActionBar().getCustomView().findViewById(R.id.toolbar_title);
            pp_actionBar = getSupportActionBar().getCustomView().findViewById(R.id.pp_actionBar);
            ImageView action_refresh = getSupportActionBar().getCustomView().findViewById(R.id.action_refresh);
            ImageView action_expand = getSupportActionBar().getCustomView().findViewById(R.id.action_expand);
            title.setText(R.string.conversation);
            ImageView close_conversation = getSupportActionBar().getCustomView().findViewById(R.id.close_conversation);

            if( expanded)
                action_expand.setImageResource(R.drawable.ic_expand_less);
            else
                action_expand.setImageResource(R.drawable.ic_expand_more);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if( lv_status != null) {
                        lv_status.setAdapter(statusListAdapter);
                    }
                }
            });
            if( close_conversation != null){
                close_conversation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }
            action_refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ShowConversationActivity.this, ShowConversationActivity.class);
                    Bundle b = new Bundle();
                    b.putParcelable("status", detailsStatus);
                    b.putBoolean("expanded", expanded);
                    if( expanded && statuses != null && statuses.size() > 0)
                        b.putParcelable("initialStatus", statuses.get(0));
                    intent.putExtras(b);
                    finish();
                    startActivity(intent);
                }
            });
            action_expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    expanded = !expanded;
                    Intent intent = new Intent(ShowConversationActivity.this, ShowConversationActivity.class);
                    Bundle b = new Bundle();
                    b.putParcelable("status", detailsStatus);
                    b.putBoolean("expanded", expanded);
                    if( expanded && statuses != null && statuses.size() > 0)
                        b.putParcelable("initialStatus", statuses.get(0));
                    intent.putExtras(b);
                    finish();
                    startActivity(intent);
                }
            });

            if (theme == THEME_LIGHT){
                Helper.colorizeToolbar(getSupportActionBar().getCustomView().findViewById(R.id.toolbar), R.color.black, ShowConversationActivity.this);
            }
        }else{
            setTitle(R.string.conversation);
        }

        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        Account account = new AccountDAO(getApplicationContext(),db).getAccountByID(userId);
        if( account.getAvatar() == null){
            Toast.makeText(ShowConversationActivity.this,R.string.toast_error, Toast.LENGTH_LONG).show();
            finish();
        }
        String url = account.getAvatar();
        if( url.startsWith("/") ){
            url = Helper.getLiveInstanceWithProtocol(getApplicationContext()) + account.getAvatar();
        }
        Helper.loadGiF(getApplicationContext(), url, pp_actionBar);


        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        boolean isOnWifi = Helper.isOnWIFI(getApplicationContext());
        int behaviorWithAttachments = sharedpreferences.getInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);
        int positionSpinnerTrans = sharedpreferences.getInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX);
        if( initialStatus != null)
            statuses.add(initialStatus);
        else
            statuses.add(detailsStatus);
        statusListAdapter = new StatusListAdapter(ShowConversationActivity.this, 0, null, isOnWifi, behaviorWithAttachments, positionSpinnerTrans, statuses);

        final LinearLayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(this);
        lv_status.setLayoutManager(mLayoutManager);
        boolean compactMode = sharedpreferences.getBoolean(Helper.SET_COMPACT_MODE, false);
        lv_status.addItemDecoration(new ConversationDecoration(ShowConversationActivity.this, theme, compactMode));
        lv_status.setAdapter(statusListAdapter);
        String statusIdToFetch;
        if( initialStatus != null)
            statusIdToFetch = initialStatus.getId();
        else
            statusIdToFetch = detailsStatus.getId();
        new RetrieveContextAsyncTask(getApplicationContext(), expanded, statusIdToFetch, ShowConversationActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        switch (theme){
            case Helper.THEME_LIGHT:
                swipeRefreshLayout.setColorSchemeResources(R.color.mastodonC4,
                        R.color.mastodonC2,
                        R.color.mastodonC3);
                swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(ShowConversationActivity.this, R.color.white));
                break;
            case Helper.THEME_DARK:
                swipeRefreshLayout.setColorSchemeResources(R.color.mastodonC4__,
                        R.color.mastodonC4,
                        R.color.mastodonC4);
                swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(ShowConversationActivity.this, R.color.mastodonC1_));
                break;
            case Helper.THEME_BLACK:
                swipeRefreshLayout.setColorSchemeResources(R.color.dark_icon,
                        R.color.mastodonC2,
                        R.color.mastodonC3);
                swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(ShowConversationActivity.this, R.color.black_3));
                break;
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Intent intent = new Intent(ShowConversationActivity.this, ShowConversationActivity.class);
                Bundle b = new Bundle();
                b.putParcelable("status", detailsStatus);
                b.putBoolean("expanded", expanded);
                if( expanded && statuses != null && statuses.size() > 0)
                    b.putParcelable("initialStatus", statuses.get(0));
                b.putParcelable("status", detailsStatus);
                intent.putExtras(b);
                finish();
                startActivity(intent);
            }
        });


    }


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


    @Override
    public void onRetrieveContext(Context context, Error error) {
        swipeRefreshLayout.setRefreshing(false);
        if( error != null){
            Toast.makeText(getApplicationContext(), error.getError(),Toast.LENGTH_LONG).show();
            return;
        }

        statusListAdapter.setConversationPosition( context.getAncestors().size());
        if(!expanded) {
            if (context.getAncestors() != null && context.getAncestors().size() > 0) {
                statuses.addAll(0, context.getAncestors());
                statusListAdapter.notifyItemRangeInserted(0, context.getAncestors().size());
            }
            if (context.getDescendants() != null && context.getDescendants().size() > 0) {
                statuses.addAll(context.getAncestors().size() + 1, context.getDescendants());
                statusListAdapter.notifyItemRangeChanged(context.getAncestors().size()+1, context.getDescendants().size());
            }
        }else{
            List<Status> statusesTemp = context.getDescendants();
            int i = 1;
            int position = 0;
            for(Status status: statusesTemp){
                statuses.add(status);
                if( status.getId().equals(detailsStatus.getId())) {
                    statusListAdapter.setConversationPosition(i);
                    detailsStatus = status;
                    position = i;
                }
                i++;
            }
            statusListAdapter.notifyItemRangeChanged(1,context.getDescendants().size());
            lv_status.scrollToPosition(position);
        }
    }

}
