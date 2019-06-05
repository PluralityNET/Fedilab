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
package app.fedilab.android.asynctasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import app.fedilab.android.client.API;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.interfaces.OnRetrieveAccountsReplyInterface;


/**
 * Created by Thomas on 25/10/2017.
 * Retrieves accounts which are involved in a conversation
 */

public class RetrieveAccountsForReplyAsyncTask extends AsyncTask<Void, Void, Void> {

    private app.fedilab.android.client.Entities.Status status;
    private OnRetrieveAccountsReplyInterface listener;
    private ArrayList<String> addedAccounts;
    private ArrayList<Account> accounts;
    private WeakReference<Context> contextReference;

    public RetrieveAccountsForReplyAsyncTask(Context context, app.fedilab.android.client.Entities.Status status, OnRetrieveAccountsReplyInterface onRetrieveAccountsReplyInterface){
        this.contextReference = new WeakReference<>(context);
        this.status = status;
        this.listener = onRetrieveAccountsReplyInterface;
        this.accounts = new ArrayList<>();
    }

    @Override
    protected Void doInBackground(Void... params) {
        API api = new API(this.contextReference.get());
        app.fedilab.android.client.Entities.Context statusContext = api.getStatusContext(status.getId());
        addedAccounts = new ArrayList<>();
        accounts.add(status.getAccount());
        addedAccounts.add(status.getAccount().getAcct());

        //Retrieves the first toot
        if( statusContext.getAncestors().size() > 0 ) {
            statusContext = api.getStatusContext(statusContext.getAncestors().get(0).getId());
        }
        if( statusContext != null && statusContext.getDescendants().size() >  0){
            for(app.fedilab.android.client.Entities.Status status : statusContext.getDescendants()){
                if( canBeAdded(status.getAccount().getAcct())){
                    accounts.add(status.getAccount());
                    addedAccounts.add(status.getAccount().getAcct());
                }
            }
        }
        if( statusContext != null && statusContext.getAncestors().size() >  0){
            for(app.fedilab.android.client.Entities.Status status : statusContext.getAncestors()){
                if( canBeAdded(status.getAccount().getAcct())){
                    accounts.add(status.getAccount());
                    addedAccounts.add(status.getAccount().getAcct());
                }
            }
        }
        return null;
    }

    private boolean canBeAdded(String acct){
        final SharedPreferences sharedpreferences = this.contextReference.get().getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        SQLiteDatabase db = Sqlite.getInstance(this.contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, null);
        Account currentAccount = new AccountDAO(this.contextReference.get(), db).getUniqAccount(userId, instance);
        return acct != null && !acct.equals(currentAccount.getAcct()) && !addedAccounts.contains(acct);
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveAccountsReply(accounts);
    }

}
