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
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.interfaces.OnListActionInterface;


/**
 * Created by Thomas on 13/12/2017.
 * Async works to manage Lists
 */

public class ManageListsAsyncTask extends AsyncTask<Void, Void, Void> {

    public enum action{
        GET_LIST,
        GET_LIST_TIMELINE,
        GET_LIST_ACCOUNT,
        CREATE_LIST,
        DELETE_LIST,
        UPDATE_LIST,
        ADD_USERS,
        DELETE_USERS,
        SEARCH_USER
    }

    private OnListActionInterface listener;
    private APIResponse apiResponse;
    private int statusCode;
    private String targetedId;
    private String listId;
    private String title;
    private String[] accountsId;
    private action apiAction;
    private WeakReference<Context> contextReference;
    private String max_id, since_id;
    private int limit;
    private String search;

    public ManageListsAsyncTask(Context context, action apiAction, String[] accountsId, String targetedId, String listId, String title, OnListActionInterface onListActionInterface){
        contextReference = new WeakReference<>(context);
        this.listener = onListActionInterface;
        this.listId = listId;
        this.title = title;
        this.accountsId = accountsId;
        this.apiAction = apiAction;
        this.targetedId = targetedId;
    }

    public ManageListsAsyncTask(Context context, String listId, String max_id, String since_id, OnListActionInterface onListActionInterface){
        contextReference = new WeakReference<>(context);
        this.listener = onListActionInterface;
        this.listId = listId;
        this.max_id = max_id;
        this.since_id = since_id;
        this.limit = 40;
        this.apiAction = action.GET_LIST_TIMELINE;
    }

    public ManageListsAsyncTask(Context context, String search, OnListActionInterface onListActionInterface){
        contextReference = new WeakReference<>(context);
        this.listener = onListActionInterface;
        this.search = search;
        this.apiAction = action.SEARCH_USER;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (apiAction == action.GET_LIST) {
            apiResponse = new API(contextReference.get()).getLists();
        }else if(apiAction == action.GET_LIST_TIMELINE){
            apiResponse = new API(contextReference.get()).getListTimeline(this.listId, this.max_id, this.since_id, this.limit);
        }else if(apiAction == action.GET_LIST_ACCOUNT){
            apiResponse = new API(contextReference.get()).getAccountsInList(this.listId,0);
        }else if( apiAction == action.CREATE_LIST){
            apiResponse = new API(contextReference.get()).createList(this.title);
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
