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
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import app.fedilab.android.client.API;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.interfaces.OnRetrieveAccountInterface;

/**
 * Created by Thomas on 04/06/2017.
 * Verify credential
 */

public class RetrieveAccountInfoAsyncTask extends AsyncTask<Void, Void, Void> {


    private OnRetrieveAccountInterface listener;
    private Account account;
    private API api;
    private WeakReference<Context> contextReference;

    public RetrieveAccountInfoAsyncTask(Context context, OnRetrieveAccountInterface onRetrieveAccountInterface) {
        this.contextReference = new WeakReference<>(context);
        this.listener = onRetrieveAccountInterface;
    }

    @Override
    protected Void doInBackground(Void... params) {
        api = new API(this.contextReference.get());
        account = api.verifyCredentials();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveAccount(account, api.getError());
    }

}