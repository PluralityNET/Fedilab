package app.fedilab.android.fragments;
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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.drawers.AccountsFollowRequestAdapter;
import app.fedilab.android.helper.Helper;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.asynctasks.RetrieveFollowRequestSentAsyncTask;
import app.fedilab.android.interfaces.OnRetrieveAccountsInterface;


/**
 * Created by Thomas on 07/06/2017.
 * Fragment to display follow requests for the authenticated account
 */
public class DisplayFollowRequestSentFragment extends Fragment implements OnRetrieveAccountsInterface {


    private boolean flag_loading;
    private Context context;
    private AsyncTask<Void, Void, Void> asyncTask;
    private AccountsFollowRequestAdapter accountsFollowRequestAdapter;
    private String max_id;
    private List<Account> accounts;
    private RelativeLayout mainLoader, nextElementLoader, textviewNoAction;
    private boolean firstLoad;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int accountPerPage;
    private TextView no_action_text;
    private boolean swiped;
    private RecyclerView lv_accounts;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //View for fragment is the same that fragment accounts
        View rootView = inflater.inflate(R.layout.fragment_accounts, container, false);

        context = getContext();
        accounts = new ArrayList<>();
        max_id = null;
        firstLoad = true;
        flag_loading = true;
        swiped = false;

        swipeRefreshLayout = rootView.findViewById(R.id.swipeContainer);
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        accountPerPage = Helper.ACCOUNTS_PER_PAGE;
        lv_accounts = rootView.findViewById(R.id.lv_accounts);
        lv_accounts.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        no_action_text = rootView.findViewById(R.id.no_action_text);
        mainLoader = rootView.findViewById(R.id.loader);
        nextElementLoader = rootView.findViewById(R.id.loading_next_accounts);
        textviewNoAction = rootView.findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);
        accountsFollowRequestAdapter = new AccountsFollowRequestAdapter(this.accounts);
        lv_accounts.setAdapter(accountsFollowRequestAdapter);
        final LinearLayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(context);
        lv_accounts.setLayoutManager(mLayoutManager);
        lv_accounts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    int visibleItemCount = mLayoutManager.getChildCount();
                    int totalItemCount = mLayoutManager.getItemCount();
                    int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                    if (firstVisibleItem + visibleItemCount == totalItemCount) {
                        if (!flag_loading) {
                            flag_loading = true;
                            asyncTask = new RetrieveFollowRequestSentAsyncTask(context, max_id, DisplayFollowRequestSentFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            nextElementLoader.setVisibility(View.VISIBLE);
                        }
                    } else {
                        nextElementLoader.setVisibility(View.GONE);
                    }
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                max_id = null;
                accounts = new ArrayList<>();
                firstLoad = true;
                flag_loading = true;
                swiped = true;
                asyncTask = new RetrieveFollowRequestSentAsyncTask(context, max_id, DisplayFollowRequestSentFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme) {
            case Helper.THEME_LIGHT:
                swipeRefreshLayout.setColorSchemeResources(R.color.mastodonC4,
                        R.color.mastodonC2,
                        R.color.mastodonC3);
                swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(context, R.color.white));
                break;
            case Helper.THEME_DARK:
                swipeRefreshLayout.setColorSchemeResources(R.color.mastodonC4__,
                        R.color.mastodonC4,
                        R.color.mastodonC4);
                swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(context, R.color.mastodonC1_));
                break;
            case Helper.THEME_BLACK:
                swipeRefreshLayout.setColorSchemeResources(R.color.dark_icon,
                        R.color.mastodonC2,
                        R.color.mastodonC3);
                swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(context, R.color.black_3));
                break;
        }

        asyncTask = new RetrieveFollowRequestSentAsyncTask(context, max_id, DisplayFollowRequestSentFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return rootView;
    }


    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public void onDestroy() {
        super.onDestroy();
        if (asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING)
            asyncTask.cancel(true);
    }


    @Override
    public void onRetrieveAccounts(APIResponse apiResponse) {

        mainLoader.setVisibility(View.GONE);
        nextElementLoader.setVisibility(View.GONE);
        if (apiResponse.getError() != null) {
            if(apiResponse.getError().getError().length() < 100) {
                Toasty.error(context, apiResponse.getError().getError(), Toast.LENGTH_LONG).show();
            }else{
                Toasty.error(context, getString(R.string.long_api_error,"\ud83d\ude05"), Toast.LENGTH_LONG).show();
            }
            flag_loading = false;
            swipeRefreshLayout.setRefreshing(false);
            swiped = false;
            return;
        }
        List<Account> accounts = apiResponse.getAccounts();
        if (!swiped && firstLoad && (accounts == null || accounts.size() == 0)) {
            no_action_text.setText(context.getString(R.string.no_follow_request));
            textviewNoAction.setVisibility(View.VISIBLE);
        } else
            textviewNoAction.setVisibility(View.GONE);
        max_id = apiResponse.getMax_id();
        if (swiped) {
            accountsFollowRequestAdapter = new AccountsFollowRequestAdapter(this.accounts);
            lv_accounts.setAdapter(accountsFollowRequestAdapter);
            swiped = false;
        }
        if (accounts != null && accounts.size() > 0) {
            this.accounts.addAll(accounts);
            accountsFollowRequestAdapter.notifyDataSetChanged();
        }
        swipeRefreshLayout.setRefreshing(false);
        firstLoad = false;
        flag_loading = accounts != null && accounts.size() < accountPerPage;
    }
}
