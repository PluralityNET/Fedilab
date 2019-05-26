/* Copyright 2019 Thomas Schneider
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

import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Playlist;
import app.fedilab.android.client.PeertubeAPI;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnPlaylistActionInterface;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;


/**
 * Created by Thomas on 26/05/2019.
 * Async works to manage Playlists
 */

public class ManagePlaylistsAsyncTask extends AsyncTask<Void, Void, Void> {

    public enum action{
        GET_PLAYLIST,
        GET_LIST_TIMELINE,
        GET_LIST_ACCOUNT,
        CREATE_PLAYLIST,
        DELETE_LIST,
        UPDATE_LIST,
        ADD_USERS,
        DELETE_USERS,
        SEARCH_USER
    }

    private OnPlaylistActionInterface listener;
    private APIResponse apiResponse;
    private int statusCode;
    private action apiAction;
    private WeakReference<Context> contextReference;
    private String max_id, since_id;

    public ManagePlaylistsAsyncTask(Context context, action apiAction, Playlist playlist, String max_id, String since_id, OnPlaylistActionInterface onPlaylistActionInterface){
        contextReference = new WeakReference<>(context);
        this.listener = onPlaylistActionInterface;
        this.apiAction = apiAction;
        this.max_id = max_id;
        this.since_id = since_id;

    }


    @Override
    protected Void doInBackground(Void... params) {
        SharedPreferences sharedpreferences = contextReference.get().getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(contextReference.get()));
        SQLiteDatabase db = Sqlite.getInstance(contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        Account account = new AccountDAO(contextReference.get(), db).getAccountByUserIDInstance(userId, instance);
        if (apiAction == action.GET_PLAYLIST) {
            apiResponse = new PeertubeAPI(contextReference.get()).getPlayists(account.getUsername());
        }else if(apiAction == action.GET_LIST_TIMELINE){
            apiResponse = new API(contextReference.get()).getListTimeline(this.listId, this.max_id, this.since_id, this.limit);
        }else if(apiAction == action.GET_LIST_ACCOUNT){
            apiResponse = new API(contextReference.get()).getAccountsInList(this.listId,0);
        }else if( apiAction == action.CREATE_PLAYLIST){
            apiResponse = new API(contextReference.get()).createPlaylist(this.title);
        }else if(apiAction == action.DELETE_LIST){
            statusCode = new API(contextReference.get()).deleteList(this.listId);
        }else if(apiAction == action.UPDATE_LIST){
            apiResponse = new API(contextReference.get()).updateList(this.listId, this.title);
        }else if(apiAction == action.ADD_USERS){
            apiResponse = new API(contextReference.get()).addAccountToList(this.listId, this.accountsId);
        }else if(apiAction == action.DELETE_USERS){
            statusCode = new API(contextReference.get()).deleteAccountFromList(this.listId, this.accountsId);
        }else if( apiAction == action.SEARCH_USER){
            apiResponse = new API(contextReference.get()).searchAccounts(this.search, 20, true);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onActionDone(this.apiAction, apiResponse, statusCode);
    }

}
