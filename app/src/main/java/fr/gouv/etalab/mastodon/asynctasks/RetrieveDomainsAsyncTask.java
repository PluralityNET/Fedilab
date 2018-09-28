/* Copyright 2018 Thomas Schneider
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

import java.lang.ref.WeakReference;

import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveDomainsInterface;


/**
 * Created by Thomas on 26/09/2018.
 * Retrieves blocked instance
 */

public class RetrieveDomainsAsyncTask extends AsyncTask<Void, Void, Void> {


    private OnRetrieveDomainsInterface listener;
    private WeakReference<Context> contextReference;
    private APIResponse apiResponse;
    private String max_id;

    public RetrieveDomainsAsyncTask(Context context, String max_id, OnRetrieveDomainsInterface onRetrieveDomainsInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onRetrieveDomainsInterface;
        this.max_id = max_id;
    }

    @Override
    protected Void doInBackground(Void... params) {
        API api = new API(this.contextReference.get());
        apiResponse = api.getBlockedDomain(max_id);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveDomains(apiResponse);
    }

}
