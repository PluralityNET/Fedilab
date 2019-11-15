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

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import app.fedilab.android.R;
import app.fedilab.android.asynctasks.ManageListsAsyncTask;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.drawers.AccountsInAListAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnListActionInterface;
import es.dmoral.toasty.Toasty;


/**
 * Created by Thomas on 15/12/2017.
 * Manage accounts in Lists
 */

public class ManageAccountsInListActivity extends BaseActivity implements OnListActionInterface {

    public EditText search_account;
    private LinearLayout main_account_container;
    private RelativeLayout loader;
    private RecyclerView lv_accounts_current, lv_accounts_search;
    private String title, listId;
    private java.util.List<Account> accounts;
    private AccountsInAListAdapter accountsInAListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if (theme == Helper.THEME_LIGHT) {
            setTheme(R.style.Dialog);
        } else {
            setTheme(R.style.DialogDark);
        }
        setContentView(R.layout.activity_manage_accounts_list);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        Bundle b = getIntent().getExtras();
        if (b != null) {
            title = b.getString("title");
            listId = b.getString("id");
        } else {
            Toasty.error(getApplicationContext(), getString(R.string.toast_error), Toast.LENGTH_LONG).show();
        }

        main_account_container = findViewById(R.id.main_account_container);
        loader = findViewById(R.id.loader);
        TextView list_title = findViewById(R.id.list_title);
        search_account = findViewById(R.id.search_account);
        lv_accounts_search = findViewById(R.id.lv_accounts_search);
        lv_accounts_current = findViewById(R.id.lv_accounts_current);


        this.accounts = new ArrayList<>();
        accountsInAListAdapter = new AccountsInAListAdapter(AccountsInAListAdapter.type.CURRENT, listId, this.accounts);
        lv_accounts_current.setAdapter(accountsInAListAdapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(ManageAccountsInListActivity.this);
        lv_accounts_current.setLayoutManager(mLayoutManager);


        list_title.setText(title);
        loader.setVisibility(View.VISIBLE);
        new ManageListsAsyncTask(ManageAccountsInListActivity.this, ManageListsAsyncTask.action.GET_LIST_ACCOUNT, null, null, listId, null, ManageAccountsInListActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        search_account.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0) {
                    search_account.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close, 0);
                } else {
                    search_account.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_search, 0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    new ManageListsAsyncTask(ManageAccountsInListActivity.this, s.toString(), ManageAccountsInListActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    lv_accounts_search.setVisibility(View.GONE);
                    lv_accounts_current.setVisibility(View.VISIBLE);
                }
            }
        });

        search_account.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (search_account.length() > 0 && event.getRawX() >= (search_account.getRight() - search_account.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        search_account.setText("");
                    }
                }

                return false;
            }
        });
    }

    public void addAccount(Account account) {
        search_account.setText("");
        accounts.add(0, account);
        accountsInAListAdapter.notifyItemInserted(0);
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
    public void onActionDone(ManageListsAsyncTask.action actionType, APIResponse apiResponse, int statusCode) {
        loader.setVisibility(View.GONE);
        main_account_container.setVisibility(View.VISIBLE);
        if (apiResponse.getError() != null) {
            Toasty.error(getApplicationContext(), apiResponse.getError().getError(), Toast.LENGTH_LONG).show();
            return;
        }
        if (actionType == ManageListsAsyncTask.action.GET_LIST_ACCOUNT) {
            if (apiResponse.getAccounts() != null && apiResponse.getAccounts().size() > 0) {
                this.accounts.addAll(apiResponse.getAccounts());
                accountsInAListAdapter.notifyDataSetChanged();
                lv_accounts_search.setVisibility(View.GONE);
                lv_accounts_current.setVisibility(View.VISIBLE);

            }
        } else if (actionType == ManageListsAsyncTask.action.SEARCH_USER) {
            if (apiResponse.getAccounts() != null && apiResponse.getAccounts().size() > 0) {
                java.util.List<Account> accountsSearch = new ArrayList<>();
                accountsSearch.addAll(apiResponse.getAccounts());
                AccountsInAListAdapter accountsSearchInAListAdapter = new AccountsInAListAdapter(AccountsInAListAdapter.type.SEARCH, listId, accounts, accountsSearch);
                lv_accounts_search.setAdapter(accountsSearchInAListAdapter);
                LinearLayoutManager mLayoutManager1 = new LinearLayoutManager(ManageAccountsInListActivity.this);
                lv_accounts_search.setLayoutManager(mLayoutManager1);
                lv_accounts_search.setVisibility(View.VISIBLE);
                lv_accounts_current.setVisibility(View.GONE);
            }
        }
    }
}
