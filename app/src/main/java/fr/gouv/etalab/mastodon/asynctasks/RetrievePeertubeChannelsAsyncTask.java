/* Copyright 2019 Thomas Schneider
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
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.PeertubeAPI;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrievePeertubeInterface;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;


/**
 * Created by Thomas on 07/01/2019.
 * Retrieves peertube Channels
 */

public class RetrievePeertubeChannelsAsyncTask extends AsyncTask<Void, Void, Void> {



    private APIResponse apiResponse;
    private OnRetrievePeertubeInterface listener;
    private WeakReference<Context> contextReference;




    public RetrievePeertubeChannelsAsyncTask(Context context, OnRetrievePeertubeInterface onRetrievePeertubeInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onRetrievePeertubeInterface;
    }

    @Override
    protected Void doInBackground(Void... params) {
        PeertubeAPI peertubeAPI = new PeertubeAPI(this.contextReference.get());
        SQLiteDatabase db = Sqlite.getInstance(contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        SharedPreferences sharedpreferences = contextReference.get().getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(contextReference.get()));
        Account account = new AccountDAO(contextReference.get(), db).getUniqAccount(userId, instance);
        apiResponse = peertubeAPI.getPeertubeChannel(account.getUsername());
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrievePeertubeChannels(apiResponse);
    }
}
