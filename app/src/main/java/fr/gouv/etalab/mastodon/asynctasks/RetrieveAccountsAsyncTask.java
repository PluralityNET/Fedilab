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

import java.lang.ref.WeakReference;

import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.GNUAPI;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveAccountsInterface;


/**
 * Created by Thomas on 27/04/2017.
 * Retrieves accounts on the instance
 */

public class RetrieveAccountsAsyncTask extends AsyncTask<Void, Void, Void> {

    private Type action;
    private APIResponse apiResponse;
    private String max_id;
    private OnRetrieveAccountsInterface listener;
    private String targetedId;
    private WeakReference<Context> contextReference;
    private String instance, name;

    public enum Type{
        BLOCKED,
        MUTED,
        FOLLOWING,
        FOLLOWERS,
        CHANNELS,
        REBLOGGED,
        FAVOURITED
    }

    public RetrieveAccountsAsyncTask(Context context, String instance, String name, OnRetrieveAccountsInterface onRetrieveAccountsInterface){
        this.contextReference = new WeakReference<>(context);
        this.instance = instance;
        this.name = name;
        this.listener = onRetrieveAccountsInterface;
        this.action = Type.CHANNELS;
    }

    public RetrieveAccountsAsyncTask(Context context, Type action, String targetedId, String max_id, OnRetrieveAccountsInterface onRetrieveAccountsInterface){
        this.contextReference = new WeakReference<>(context);
        this.action = action;
        this.max_id = max_id;
        this.listener = onRetrieveAccountsInterface;
        this.targetedId = targetedId;
    }

    public RetrieveAccountsAsyncTask(Context context, Type action, String max_id, OnRetrieveAccountsInterface onRetrieveAccountsInterface){
        this.contextReference = new WeakReference<>(context);
        this.action = action;
        this.max_id = max_id;
        this.listener = onRetrieveAccountsInterface;
    }

    @Override
    protected Void doInBackground(Void... params) {
        API api = null;
        GNUAPI gnuapi = null;
        if( MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.GNU)
            api = new API(this.contextReference.get());
        else
            gnuapi = new GNUAPI(this.contextReference.get());
        switch (action){
            case REBLOGGED:
                assert api != null;
                apiResponse = api.getRebloggedBy(targetedId, max_id);
                break;
            case FAVOURITED:
                assert api != null;
                apiResponse = api.getFavouritedBy(targetedId, max_id);
                break;
            case BLOCKED:
                assert api != null;
                apiResponse = api.getBlocks(max_id);
                break;
            case MUTED:
                assert api != null;
                apiResponse = api.getMuted(max_id);
                break;
            case FOLLOWING:
                if(MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.GNU) {
                    assert api != null;
                    apiResponse = api.getFollowing(targetedId, max_id);
                }
                else {
                    assert gnuapi != null;
                    apiResponse = gnuapi.getFollowing(targetedId, max_id);
                }
                break;
            case FOLLOWERS:
                if(MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.GNU) {
                    assert api != null;
                    apiResponse = api.getFollowers(targetedId, max_id);
                }else {
                    assert gnuapi != null;
                    apiResponse = gnuapi.getFollowers(targetedId, max_id);
                }
                break;
            case CHANNELS:
                assert api != null;
                apiResponse = api.getPeertubeChannel(instance, name);
                break;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveAccounts(apiResponse);
    }

}
