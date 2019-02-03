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
package fr.gouv.etalab.mastodon.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;

import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.GNUAPI;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveNotificationsInterface;



/**
 * Created by Thomas on 28/04/2017.
 * Retrieves notifications on the instance
 */

public class RetrieveNotificationsAsyncTask extends AsyncTask<Void, Void, Void> {


    private APIResponse apiResponse;
    private String max_id;
    private Account account;
    private OnRetrieveNotificationsInterface listener;
    private boolean refreshData;
    private WeakReference<Context> contextReference;
    private boolean display;

    public RetrieveNotificationsAsyncTask(Context context, boolean display, Account account, String max_id, OnRetrieveNotificationsInterface onRetrieveNotificationsInterface){
        this.contextReference = new WeakReference<>(context);
        this.max_id = max_id;
        this.listener = onRetrieveNotificationsInterface;
        this.account = account;
        this.refreshData = true;
        this.display = display;
    }


    @Override
    protected Void doInBackground(Void... params) {
        Log.v(Helper.TAG,"MainActivity.social : " + MainActivity.social );
        if(MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.GNU) {
            API api;
            if (account == null) {
                api = new API(this.contextReference.get());
                apiResponse = api.getNotifications(max_id, display);
            } else {
                if (this.contextReference.get() == null) {
                    apiResponse.setError(new Error());
                    return null;
                }
                api = new API(this.contextReference.get(), account.getInstance(), account.getToken());
                apiResponse = api.getNotificationsSince(max_id, display);
            }
        }else{
            GNUAPI gnuapi;
            if (account == null) {
                gnuapi = new GNUAPI(this.contextReference.get());
                apiResponse = gnuapi.getNotifications(max_id, display);
            } else {
                if (this.contextReference.get() == null) {
                    apiResponse.setError(new Error());
                    return null;
                }
                gnuapi = new GNUAPI(this.contextReference.get(), account.getInstance(), account.getToken());
                apiResponse = gnuapi.getNotificationsSince(max_id, display);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveNotifications(apiResponse, account, refreshData);
    }

}
