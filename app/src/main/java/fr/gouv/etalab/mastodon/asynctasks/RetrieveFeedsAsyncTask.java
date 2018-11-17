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
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.List;

import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Peertube;
import fr.gouv.etalab.mastodon.client.Entities.RemoteInstance;
import fr.gouv.etalab.mastodon.helper.FilterToots;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveFeedsInterface;
import fr.gouv.etalab.mastodon.sqlite.InstancesDAO;
import fr.gouv.etalab.mastodon.sqlite.PeertubeFavoritesDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.StatusCacheDAO;


/**
 * Created by Thomas on 23/04/2017.
 * Retrieves toots on the instance
 */

public class RetrieveFeedsAsyncTask extends AsyncTask<Void, Void, Void> {


    private Type action;
    private APIResponse apiResponse;
    private String max_id;
    private OnRetrieveFeedsInterface listener;
    private String targetedID;
    private String tag;
    private boolean showMediaOnly = false;
    private boolean showPinned = false;
    private boolean showReply = false;
    private WeakReference<Context> contextReference;
    private FilterToots filterToots;
    private String instanceName,remoteInstance, name;

    public enum Type{
        HOME,
        LOCAL,
        DIRECT,
        CONVERSATION,
        PUBLIC,
        HASHTAG,
        LIST,
        USER,
        FAVOURITES,
        ONESTATUS,
        CONTEXT,
        TAG,
        CACHE_BOOKMARKS,
        CACHE_BOOKMARKS_PEERTUBE,
        CACHE_STATUS,
        REMOTE_INSTANCE
    }


    public RetrieveFeedsAsyncTask(Context context, FilterToots filterToots, String max_id, OnRetrieveFeedsInterface onRetrieveFeedsInterface){
        this.contextReference = new WeakReference<>(context);
        this.action = Type.CACHE_STATUS;
        this.max_id = max_id;
        this.listener = onRetrieveFeedsInterface;
        this.filterToots = filterToots;
    }

    public RetrieveFeedsAsyncTask(Context context, Type action, String max_id, OnRetrieveFeedsInterface onRetrieveFeedsInterface){
        this.contextReference = new WeakReference<>(context);
        this.action = action;
        this.max_id = max_id;
        this.listener = onRetrieveFeedsInterface;
    }

    public RetrieveFeedsAsyncTask(Context context, Type action, String instanceName, String max_id, OnRetrieveFeedsInterface onRetrieveFeedsInterface){
        this.contextReference = new WeakReference<>(context);
        this.action = action;
        this.max_id = max_id;
        this.listener = onRetrieveFeedsInterface;
        this.instanceName = instanceName;
    }

    public RetrieveFeedsAsyncTask(Context context, Type action, String targetedID, String max_id, boolean showMediaOnly, boolean showPinned, OnRetrieveFeedsInterface onRetrieveFeedsInterface){
        this.contextReference = new WeakReference<>(context);
        this.action = action;
        this.max_id = max_id;
        this.listener = onRetrieveFeedsInterface;
        this.targetedID = targetedID;
        this.showMediaOnly = showMediaOnly;
        this.showPinned = showPinned;
    }
    public RetrieveFeedsAsyncTask(Context context, Type action, String targetedID, String max_id, boolean showMediaOnly, boolean showPinned, boolean showReply, OnRetrieveFeedsInterface onRetrieveFeedsInterface){
        this.contextReference = new WeakReference<>(context);
        this.action = action;
        this.max_id = max_id;
        this.listener = onRetrieveFeedsInterface;
        this.targetedID = targetedID;
        this.showMediaOnly = showMediaOnly;
        this.showPinned = showPinned;
        this.showReply = showReply;
    }
    public RetrieveFeedsAsyncTask(Context context, Type action, String tag, String targetedID, String max_id, OnRetrieveFeedsInterface onRetrieveFeedsInterface){
        this.contextReference = new WeakReference<>(context);
        this.action = action;
        this.max_id = max_id;
        this.listener = onRetrieveFeedsInterface;
        this.targetedID = targetedID;
        this.tag = tag;
    }

    public RetrieveFeedsAsyncTask(Context context, String  remoteInstance, String name, String max_id, OnRetrieveFeedsInterface onRetrieveFeedsInterface){
        this.contextReference = new WeakReference<>(context);
        this.remoteInstance = remoteInstance;
        this.max_id = max_id;
        this.listener = onRetrieveFeedsInterface;
        this.name = name;
        this.action = Type.REMOTE_INSTANCE;
    }

    @Override
    protected Void doInBackground(Void... params) {
        API api = new API(this.contextReference.get());
        SQLiteDatabase db = Sqlite.getInstance(this.contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        switch (action){
            case HOME:
                List<fr.gouv.etalab.mastodon.client.Entities.Status> statuses = new StatusCacheDAO(contextReference.get(), db).getAllCachedStatus(max_id);
                if( statuses == null || statuses.size() == 0)
                    apiResponse = api.getHomeTimeline(max_id);
                else{
                    apiResponse = new APIResponse();
                    apiResponse.setStatuses(statuses);
                    apiResponse.setMax_id(statuses.get(statuses.size()-1).getId());
                }
                break;
            case LOCAL:
                apiResponse = api.getPublicTimeline(true, max_id);
                break;
            case PUBLIC:
                apiResponse = api.getPublicTimeline(false, max_id);
                break;
            case DIRECT:
                apiResponse = api.getDirectTimeline(max_id);
                break;
            case CONVERSATION:
                apiResponse = api.getConversationTimeline(max_id);
                break;
            case REMOTE_INSTANCE:
                if( this.name != null && this.remoteInstance != null){ //For Peertube channels
                    apiResponse = api.getPeertubeChannelVideos(this.remoteInstance, this.name);
                }else{ //For other remote instance
                    List<RemoteInstance> remoteInstanceObj = new InstancesDAO(this.contextReference.get(), db).getInstanceByName(this.instanceName);
                    if( remoteInstanceObj != null && remoteInstanceObj.size() > 0 && remoteInstanceObj.get(0).getType().equals("MASTODON")) {
                        apiResponse = api.getPublicTimeline(this.instanceName, false, max_id);
                        List<fr.gouv.etalab.mastodon.client.Entities.Status> statusesTemp = apiResponse.getStatuses();
                        if( statusesTemp != null){
                            for(fr.gouv.etalab.mastodon.client.Entities.Status status: statusesTemp){
                                status.setType(action);
                            }
                        }
                    }else {
                        apiResponse = api.getPeertube(this.instanceName, max_id);
                    }
                }
                break;
            case FAVOURITES:
                apiResponse = api.getFavourites(max_id);
                break;
            case USER:
                if( showMediaOnly)
                    apiResponse = api.getStatusWithMedia(targetedID, max_id);
                else if (showPinned)
                    apiResponse = api.getPinnedStatuses(targetedID, max_id);
                else
                    apiResponse = api.getAccountTLStatuses(targetedID, max_id, !showReply);
                break;
            case ONESTATUS:
                apiResponse = api.getStatusbyId(targetedID);
                break;
            case TAG:
                apiResponse = api.getPublicTimelineTag(tag, false, max_id);
                break;
            case CACHE_BOOKMARKS:
                apiResponse = new APIResponse();
                db = Sqlite.getInstance(contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                statuses = new StatusCacheDAO(contextReference.get(), db).getAllStatus(StatusCacheDAO.BOOKMARK_CACHE);
                apiResponse.setStatuses(statuses);
                break;
            case CACHE_BOOKMARKS_PEERTUBE:
                apiResponse = new APIResponse();
                db = Sqlite.getInstance(contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                List<Peertube> peertubes = new PeertubeFavoritesDAO(contextReference.get(), db).getAllPeertube();
                apiResponse.setPeertubes(peertubes);
                break;
            case CACHE_STATUS:
                apiResponse = new APIResponse();
                db = Sqlite.getInstance(contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                statuses = new StatusCacheDAO(contextReference.get(), db).getStatusFromID(StatusCacheDAO.ARCHIVE_CACHE, filterToots, max_id);
                if( statuses != null && statuses.size() > 0) {
                    apiResponse.setStatuses(statuses);
                    apiResponse.setSince_id(statuses.get(0).getId());
                    apiResponse.setMax_id(statuses.get(statuses.size() - 1).getId());
                }else{
                    apiResponse.setStatuses(null);
                    apiResponse.setMax_id(null);
                    apiResponse.setSince_id(null);
                }
                break;
            case HASHTAG:
                break;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveFeeds(apiResponse);
    }
}
