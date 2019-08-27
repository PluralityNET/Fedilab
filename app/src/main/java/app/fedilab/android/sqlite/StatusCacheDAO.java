package app.fedilab.android.sqlite;
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

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import app.fedilab.android.client.Entities.Charts;
import app.fedilab.android.client.Entities.Statistics;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.client.Entities.Tag;
import app.fedilab.android.helper.FilterToots;
import app.fedilab.android.helper.Helper;


/**
 * Created by Thomas on 15/02/2018.
 * Manage Status in cache
 */
public class StatusCacheDAO {

    private SQLiteDatabase db;
    public Context context;

    //Type of cache
    public static int BOOKMARK_CACHE = 0;
    public static int ARCHIVE_CACHE = 1;
    public static int NOTIFICATION_CACHE = 2;

    public StatusCacheDAO(Context context, SQLiteDatabase db) {
        //Creation of the DB with tables
        this.context = context;
        this.db = db;
    }


    //------- INSERTIONS  -------
    /**
     * Insert a status in database
     * @param cacheType int cache type
     * @param status Status
     * @param userId String
     * @return boolean
     */
    public long insertStatus(int cacheType, Status status, String userId, String instance) {
        ContentValues values = new ContentValues();
        values.put(Sqlite.COL_USER_ID, userId);
        values.put(Sqlite.COL_CACHED_ACTION, cacheType);
        values.put(Sqlite.COL_INSTANCE, instance);
        values.put(Sqlite.COL_STATUS_ID, status.getId());
        values.put(Sqlite.COL_URI, status.getUri());
        values.put(Sqlite.COL_CARD, Helper.cardToStringStorage(status.getCard()));
        values.put(Sqlite.COL_URL, status.getUrl());
        values.put(Sqlite.COL_ACCOUNT, Helper.accountToStringStorage(status.getAccount()));
        values.put(Sqlite.COL_IN_REPLY_TO_ID, status.getIn_reply_to_id());
        values.put(Sqlite.COL_IN_REPLY_TO_ACCOUNT_ID, status.getIn_reply_to_account_id());
        values.put(Sqlite.COL_REBLOG, status.getReblog()!=null?Helper.statusToStringStorage(status.getReblog()):null);
        values.put(Sqlite.COL_CONTENT, status.getContent());
        values.put(Sqlite.COL_EMOJIS, status.getEmojis()!=null?Helper.emojisToStringStorage(status.getEmojis()):null);
        values.put(Sqlite.COL_REBLOGS_COUNT, status.getReblogs_count());
        values.put(Sqlite.COL_FAVOURITES_COUNT, status.getFavourites_count());
        values.put(Sqlite.COL_REBLOGGED, status.isReblogged());
        values.put(Sqlite.COL_FAVOURITED, status.isFavourited());
        values.put(Sqlite.COL_MUTED, status.isMuted());
        values.put(Sqlite.COL_CREATED_AT, Helper.dateToString(status.getCreated_at()));
        values.put(Sqlite.COL_DATE_BACKUP, Helper.dateToString(new Date()));
        values.put(Sqlite.COL_SENSITIVE, status.isSensitive());
        values.put(Sqlite.COL_SPOILER_TEXT, status.getSpoiler_text());
        values.put(Sqlite.COL_VISIBILITY, status.getVisibility());
        values.put(Sqlite.COL_MEDIA_ATTACHMENTS, status.getMedia_attachments()!=null?Helper.attachmentToStringStorage(status.getMedia_attachments()):null);
        values.put(Sqlite.COL_MENTIONS, status.getMentions()!=null?Helper.mentionToStringStorage(status.getMentions()):null);
        values.put(Sqlite.COL_TAGS, status.getTags()!=null?Helper.tagToStringStorage(status.getTags()):null);
        values.put(Sqlite.COL_APPLICATION, status.getApplication()!=null?Helper.applicationToStringStorage(status.getApplication()):null);
        values.put(Sqlite.COL_LANGUAGE, status.getLanguage());
        values.put(Sqlite.COL_PINNED, status.isPinned());

        //Inserts cached status
        long last_id;
        try{
            last_id = db.insert(Sqlite.TABLE_STATUSES_CACHE, null, values);
        }catch (Exception e) {
            last_id =  -1;
            e.printStackTrace();
        }
        return last_id;
    }

    /**
     * Insert a status in database
     * @param cacheType int cache type
     * @param status Status
     * @return boolean
     */
    public long insertStatus(int cacheType, Status status) {

        ContentValues values = new ContentValues();
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        values.put(Sqlite.COL_USER_ID, userId);
        values.put(Sqlite.COL_CACHED_ACTION, cacheType);
        values.put(Sqlite.COL_INSTANCE, instance);
        values.put(Sqlite.COL_STATUS_ID, status.getId());
        values.put(Sqlite.COL_URI, status.getUri());
        values.put(Sqlite.COL_URL, status.getUrl());
        values.put(Sqlite.COL_ACCOUNT, Helper.accountToStringStorage(status.getAccount()));
        values.put(Sqlite.COL_CARD, Helper.cardToStringStorage(status.getCard()));
        values.put(Sqlite.COL_IN_REPLY_TO_ID, status.getIn_reply_to_id());
        values.put(Sqlite.COL_IN_REPLY_TO_ACCOUNT_ID, status.getIn_reply_to_account_id());
        values.put(Sqlite.COL_REBLOG, status.getReblog()!=null?Helper.statusToStringStorage(status.getReblog()):null);
        values.put(Sqlite.COL_CONTENT, status.getContent());
        values.put(Sqlite.COL_EMOJIS, status.getEmojis()!=null?Helper.emojisToStringStorage(status.getEmojis()):null);
        values.put(Sqlite.COL_REBLOGS_COUNT, status.getReblogs_count());
        values.put(Sqlite.COL_FAVOURITES_COUNT, status.getFavourites_count());
        values.put(Sqlite.COL_REBLOGGED, status.isReblogged());
        values.put(Sqlite.COL_FAVOURITED, status.isFavourited());
        values.put(Sqlite.COL_MUTED, status.isMuted());
        values.put(Sqlite.COL_CREATED_AT, Helper.dateToString(status.getCreated_at()));
        values.put(Sqlite.COL_DATE_BACKUP, Helper.dateToString(new Date()));
        values.put(Sqlite.COL_SENSITIVE, status.isSensitive());
        values.put(Sqlite.COL_SPOILER_TEXT, status.getSpoiler_text());
        values.put(Sqlite.COL_VISIBILITY, status.getVisibility());
        values.put(Sqlite.COL_MEDIA_ATTACHMENTS, status.getMedia_attachments()!=null?Helper.attachmentToStringStorage(status.getMedia_attachments()):null);
        values.put(Sqlite.COL_MENTIONS, status.getMentions()!=null?Helper.mentionToStringStorage(status.getMentions()):null);
        values.put(Sqlite.COL_TAGS, status.getTags()!=null?Helper.tagToStringStorage(status.getTags()):null);
        values.put(Sqlite.COL_APPLICATION, status.getApplication()!=null?Helper.applicationToStringStorage(status.getApplication()):null);
        values.put(Sqlite.COL_LANGUAGE, status.getLanguage());
        values.put(Sqlite.COL_PINNED, status.isPinned());

        //Inserts cached status
        long last_id;
        try{
            last_id = db.insert(Sqlite.TABLE_STATUSES_CACHE, null, values);
        }catch (Exception e) {
            last_id =  -1;
            e.printStackTrace();
        }
        return last_id;
    }

    //------- UPDATES  -------

    /**
     * Update a Status cached in database
     * @param status Status
     * @return boolean
     */
    public int updateStatus(int cacheType, Status status ) {
        ContentValues values = new ContentValues();
        String instance = Helper.getLiveInstance(context);
        values.put(Sqlite.COL_REBLOGS_COUNT, status.getReblogs_count());
        values.put(Sqlite.COL_FAVOURITES_COUNT, status.getFavourites_count());
        values.put(Sqlite.COL_REBLOGGED, status.isReblogged());
        values.put(Sqlite.COL_FAVOURITED, status.isFavourited());
        values.put(Sqlite.COL_MUTED, status.isMuted());
        values.put(Sqlite.COL_PINNED, status.isPinned());
        return db.update(Sqlite.TABLE_STATUSES_CACHE,
                values, Sqlite.COL_STATUS_ID + " =  ? AND " + Sqlite.COL_INSTANCE + " =  ? " + Sqlite.COL_CACHED_ACTION + " = ?",
                new String[]{String.valueOf(status.getId()), instance, String.valueOf(cacheType)});
    }


    //------- REMOVE  -------

    /***
     * Remove stored status
     * @return int
     */
    public int remove(int cacheType, Status status){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        return db.delete(Sqlite.TABLE_STATUSES_CACHE,  Sqlite.COL_CACHED_ACTION + " = \""+ cacheType +"\" AND " + Sqlite.COL_STATUS_ID + " = \"" + status.getId() + "\" AND " + Sqlite.COL_INSTANCE + " = \"" + instance + "\" AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null);
    }

    public void removeDuplicate(){
        db.execSQL("DELETE FROM "+Sqlite.TABLE_STATUSES_CACHE+" WHERE "+Sqlite.COL_ID+" NOT IN (SELECT MIN("+Sqlite.COL_ID+") FROM "+Sqlite.TABLE_STATUSES_CACHE+" GROUP BY "+Sqlite.COL_STATUS_ID+","+Sqlite.COL_INSTANCE+")");
    }


    /***
     * Remove stored status
     * @return int
     */
    public int remove(int cacheType, Status status, String userId, String instance){
        return db.delete(Sqlite.TABLE_STATUSES_CACHE,  Sqlite.COL_CACHED_ACTION + " = \""+ cacheType +"\" AND " + Sqlite.COL_STATUS_ID + " = \"" + status.getId() + "\" AND " + Sqlite.COL_INSTANCE + " = \"" + instance + "\" AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null);
    }

    public int removeAllStatus(int cacheType){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        return db.delete(Sqlite.TABLE_STATUSES_CACHE,  Sqlite.COL_CACHED_ACTION + " = \""+ cacheType +"\" AND " + Sqlite.COL_INSTANCE + " = '" + instance+ "' AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null);
    }

    public int removeAll(int cacheType){
        return db.delete(Sqlite.TABLE_STATUSES_CACHE,  Sqlite.COL_CACHED_ACTION + " = \""+ cacheType +"\"", null);
    }

    //------- GETTERS  -------

    /**
     * Returns all cached Statuses in db depending of their cache type
     * @return stored status List<StoredStatus>
     */
    public List<Status> getAllStatus(int cacheType){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        try {
            Cursor c = db.query(Sqlite.TABLE_STATUSES_CACHE, null, Sqlite.COL_CACHED_ACTION + " = '" + cacheType+ "' AND " + Sqlite.COL_INSTANCE + " = '" + instance+ "' AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null, null, null, Sqlite.COL_CREATED_AT + " DESC", null);
            return cursorToListStatuses(c);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Returns all cached Statuses in db depending of their cache type
     * @return stored status List<StoredStatus>
     */
    public List<String> getAllStatusId(int cacheType){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        try {
            Cursor c = db.query(Sqlite.TABLE_STATUSES_CACHE, new String[]{Sqlite.COL_STATUS_ID}, Sqlite.COL_CACHED_ACTION + " = '" + cacheType+ "' AND " + Sqlite.COL_INSTANCE + " = '" + instance+ "' AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null, null, null, Sqlite.COL_CREATED_AT + " DESC", null);
            return cursorToListStatusesId(c);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Returns all cached Statuses in db depending of their cache type
     * @return stored status List<StoredStatus>
     */
    public List<Status> getStatusFromID(int cacheType, FilterToots filterToots, String max_id){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        //That the basic selection for all toots
        StringBuilder selection = new StringBuilder(Sqlite.COL_CACHED_ACTION + " = '" + cacheType + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance + "' AND " + Sqlite.COL_USER_ID + " = '" + userId + "'");
        if( max_id != null)
            selection.append(" AND " + Sqlite.COL_STATUS_ID + " < '").append(max_id).append("'");
        //BOOST
        if(filterToots.getBoosts() == FilterToots.typeFilter.NONE)
            selection.append(" AND (" + Sqlite.COL_REBLOG + " IS NULL OR " + Sqlite.COL_REBLOG + " = 'null')");
        else if(filterToots.getBoosts() == FilterToots.typeFilter.ONLY)
            selection.append(" AND " + Sqlite.COL_REBLOG + " IS NOT NULL AND " + Sqlite.COL_REBLOG + " <> 'null'");
        //REPLIES
        if(filterToots.getReplies() == FilterToots.typeFilter.NONE)
            selection.append(" AND (" + Sqlite.COL_IN_REPLY_TO_ID + " IS NULL OR " + Sqlite.COL_IN_REPLY_TO_ID + " = 'null')");
        else if(filterToots.getReplies() == FilterToots.typeFilter.ONLY)
            selection.append(" AND " + Sqlite.COL_IN_REPLY_TO_ID + " IS NOT NULL AND " + Sqlite.COL_IN_REPLY_TO_ID + " <> 'null'");
        //PINNED
        if(filterToots.getPinned() == FilterToots.typeFilter.NONE)
            selection.append(" AND " + Sqlite.COL_PINNED + " = 0");
        else if(filterToots.getPinned() == FilterToots.typeFilter.ONLY)
            selection.append(" AND " + Sqlite.COL_PINNED + " = 1");
        //PINNED
        if(filterToots.getMedia() == FilterToots.typeFilter.NONE)
            selection.append(" AND " + Sqlite.COL_MEDIA_ATTACHMENTS + " = '[]'");
        else if(filterToots.getMedia() == FilterToots.typeFilter.ONLY)
            selection.append(" AND " + Sqlite.COL_MEDIA_ATTACHMENTS + " <> '[]'");

        if( !filterToots.isV_direct())
            selection.append(" AND " + Sqlite.COL_VISIBILITY + " <> 'direct'");
        if( !filterToots.isV_private())
            selection.append(" AND " + Sqlite.COL_VISIBILITY + " <> 'private'");
        if( !filterToots.isV_public())
            selection.append(" AND " + Sqlite.COL_VISIBILITY + " <> 'public'");
        if( !filterToots.isV_unlisted())
            selection.append(" AND " + Sqlite.COL_VISIBILITY + " <> 'unlisted'");

        if( filterToots.getDateIni() != null)
            selection.append(" AND " + Sqlite.COL_CREATED_AT + " >= '").append(filterToots.getDateIni()).append("'");

        if( filterToots.getDateEnd() != null)
            selection.append(" AND " + Sqlite.COL_CREATED_AT + " <= '").append(filterToots.getDateEnd()).append("'");

        if( filterToots.getFilter() != null ){
            String[] keywords = filterToots.getFilter().split(" ");
            selection.append(" AND (");
            int i = 0;
            for(String kw: keywords){

                if( i == 0 && keywords.length == 1)
                    selection.append(Sqlite.COL_CONTENT + " LIKE '%").append(kw).append("%'");
                else if( i == 0 && keywords.length > 1)
                    selection.append(Sqlite.COL_CONTENT + " LIKE '%").append(kw).append("%' OR ");
                else if( i == keywords.length -1 )
                    selection.append(Sqlite.COL_CONTENT + " LIKE '%").append(kw).append("%'");
                i++;
            }
            selection.append(")");
        }

        try {
            Cursor c = db.query(Sqlite.TABLE_STATUSES_CACHE, null, selection.toString(), null, null, null, Sqlite.COL_CREATED_AT + " DESC", "40");
            return cursorToListStatuses(c);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns the last date of backup for a user depending of the type of cache
     * @return Date
     */
    public Date getLastDateCache(int cacheType){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        try {
            Cursor c = db.query(Sqlite.TABLE_STATUSES_CACHE, null, Sqlite.COL_CACHED_ACTION + " = '" + cacheType+ "' AND " + Sqlite.COL_INSTANCE + " = '" + instance+ "' AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null, null, null, Sqlite.COL_DATE_BACKUP + " DESC", "1");
            //No element found
            if (c.getCount() == 0) {
                c.close();
                return null;
            }
            //Take the first element
            c.moveToFirst();
            String date = c.getString(c.getColumnIndex(Sqlite.COL_DATE_BACKUP));
            c.close();
            return Helper.stringToDate(context, date);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the smaller date
     * @return Date
     */
    public Date getSmallerDate(int cacheType){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        try {
            Cursor c = db.query(Sqlite.TABLE_STATUSES_CACHE, null, Sqlite.COL_CACHED_ACTION + " = '" + cacheType+ "' AND " + Sqlite.COL_INSTANCE + " = '" + instance+ "' AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null, null, null, Sqlite.COL_CREATED_AT + " ASC", "1");
            //No element found
            if (c.getCount() == 0) {
                c.close();
                return null;
            }
            //Take the first element
            c.moveToFirst();
            String date = c.getString(c.getColumnIndex(Sqlite.COL_CREATED_AT));
            c.close();
            return Helper.stringToDate(context, date);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the smaller date
     * @return Date
     */
    public Date getGreaterDate(int cacheType){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        try {
            Cursor c = db.query(Sqlite.TABLE_STATUSES_CACHE, null, Sqlite.COL_CACHED_ACTION + " = '" + cacheType+ "' AND " + Sqlite.COL_INSTANCE + " = '" + instance+ "' AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null, null, null, Sqlite.COL_CREATED_AT + " DESC", "1");
            //No element found
            if (c.getCount() == 0) {
                c.close();
                return null;
            }
            //Take the first element
            c.moveToFirst();
            String date = c.getString(c.getColumnIndex(Sqlite.COL_CREATED_AT));
            c.close();
            return Helper.stringToDate(context, date);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the last id of backup for a user depending of the type of cache
     * @return Date
     */
    public String getLastTootIDCache(int cacheType){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        try {
            Cursor c = db.query(Sqlite.TABLE_STATUSES_CACHE, null, Sqlite.COL_CACHED_ACTION + " = '" + cacheType+ "' AND " + Sqlite.COL_INSTANCE + " = '" + instance+ "' AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null, null, null, Sqlite.COL_STATUS_ID + " DESC", "1");
            //No element found
            if (c.getCount() == 0) {
                c.close();
                return null;
            }
            //Take the first element
            c.moveToFirst();
            String last_id = c.getString(c.getColumnIndex(Sqlite.COL_STATUS_ID));
            c.close();
            return last_id;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the last date of backup for a user depending of the type of cache
     * @return Date
     */
    public Date getLastTootDateCache(int cacheType, String userId, String instance){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        if( userId == null || instance == null) {
            userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            instance = Helper.getLiveInstance(context);
        }
        try {
            Cursor c = db.query(Sqlite.TABLE_STATUSES_CACHE, null, Sqlite.COL_CACHED_ACTION + " = '" + cacheType+ "' AND " + Sqlite.COL_INSTANCE + " = '" + instance+ "' AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null, null, null, Sqlite.COL_CREATED_AT + " DESC", "1");
            //No element found
            if (c.getCount() == 0) {
                c.close();
                return null;
            }
            //Take the first element
            c.moveToFirst();
            Date last_id = Helper.stringToDate(context, c.getString(c.getColumnIndex(Sqlite.COL_CREATED_AT)));
            c.close();
            return last_id;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns a cached status by id in db
     * @return stored status StoredStatus
     */
    public Status getStatus(int cacheType, String id){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        try {
            Cursor c = db.query(Sqlite.TABLE_STATUSES_CACHE, null, Sqlite.COL_CACHED_ACTION + " = '" + cacheType+ "' AND " + Sqlite.COL_STATUS_ID + " = '" + id + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance +"' AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null, null, null, null, null);
            return cursorToStoredStatus(c);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Returns a cached status by id in db
     * @return stored status StoredStatus
     */
    public Status getStatus(String id){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        try {
            Cursor c = db.query(Sqlite.TABLE_STATUSES_CACHE, null, Sqlite.COL_STATUS_ID + " = '" + id + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance +"' AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null, null, null, null, null);
            return cursorToStoredStatus(c);
        } catch (Exception e) {
            return null;
        }
    }

    public Charts getCharts(Date dateIni, Date dateEnd){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        Charts charts = new Charts();

        Calendar start = Calendar.getInstance();
        start.setTime(dateIni);
        start.set(Calendar.HOUR_OF_DAY,0);
        start.set(Calendar.MINUTE,0);
        start.set(Calendar.SECOND,0);

        Calendar end = Calendar.getInstance();
        end.setTime(dateEnd);
        end.set(Calendar.HOUR_OF_DAY,23);
        end.set(Calendar.MINUTE,59);
        end.set(Calendar.SECOND,59);

        StringBuilder selection = new StringBuilder(Sqlite.COL_CACHED_ACTION + " = '" + ARCHIVE_CACHE + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance + "' AND " + Sqlite.COL_USER_ID + " = '" + userId + "'");
        selection.append(" AND " + Sqlite.COL_CREATED_AT + " >= '").append(Helper.dateToString(start.getTime())).append("'");
        selection.append(" AND " + Sqlite.COL_CREATED_AT + " <= '").append(Helper.dateToString(end.getTime())).append("'");

        List<Status> data = new ArrayList<>();
        try {
            Cursor c = db.query(Sqlite.TABLE_STATUSES_CACHE, null, selection.toString(), null, null, null, Sqlite.COL_CREATED_AT + " ASC");
            data = cursorToListStatuses(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<String> xLabel = new ArrayList<>();

        charts.setStatuses(new LinkedHashMap<>());
        charts.setBoosts(new LinkedHashMap<>());
        charts.setReplies(new LinkedHashMap<>());
        if( data != null) {
            for (Status status : data) {
                Calendar tempdate = Calendar.getInstance();
                tempdate.setTime(status.getCreated_at());
                tempdate.set(Calendar.HOUR_OF_DAY, 0);
                tempdate.set(Calendar.MINUTE, 0);
                tempdate.set(Calendar.SECOND, 0);
                long date = tempdate.getTimeInMillis();
                if (status.getReblog() != null) {
                    if(charts.getBoosts().containsKey(date)){
                        charts.getBoosts().put(date,(charts.getBoosts().get(date)+1));
                    }else{
                        charts.getBoosts().put(date,1);
                    }

                } else if (status.getIn_reply_to_id() != null && !status.getIn_reply_to_id().trim().equals("null")) {
                    if(charts.getReplies().containsKey(date)){
                        charts.getReplies().put(date,(charts.getReplies().get(date)+1));
                    }else{
                        charts.getReplies().put(date,1);
                    }
                } else {
                    if(charts.getStatuses().containsKey(date)){
                        charts.getStatuses().put(date,(charts.getStatuses().get(date)+1));
                    }else{
                        charts.getStatuses().put(date,1);
                    }
                }
            }
        }
        charts.setxLabels(xLabel);
        return charts;
    }

    /**
     * Returns a cached status by id in db
     * @return stored status StoredStatus
     */
    public Status getStatus(int cacheType, String id, String userId, String instance){
        try {
            Cursor c = db.query(Sqlite.TABLE_STATUSES_CACHE, null, Sqlite.COL_CACHED_ACTION + " = '" + cacheType+ "' AND " + Sqlite.COL_STATUS_ID + " = '" + id + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance +"' AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null, null, null, null, null);
            return cursorToStoredStatus(c);
        } catch (Exception e) {
            return null;
        }
    }

    public Statistics getStat(){

        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);

        Statistics statistics = new Statistics();

        //Count All
        Cursor mCount = db.rawQuery("select count(*) from " + Sqlite.TABLE_STATUSES_CACHE
                        + " where " + Sqlite.COL_CACHED_ACTION + " = '" + ARCHIVE_CACHE + "' AND " + Sqlite.COL_USER_ID + " = '" + userId + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance +"'"
                , null);
        mCount.moveToFirst();
        statistics.setTotal_statuses(mCount.getInt(0));
        mCount.close();

        //Count boosts
        mCount = db.rawQuery("select count(*) from " + Sqlite.TABLE_STATUSES_CACHE
                + " where " + Sqlite.COL_CACHED_ACTION + " = '" + ARCHIVE_CACHE + "' AND " + Sqlite.COL_USER_ID + " = '" + userId + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance +"' AND "
                + Sqlite.COL_REBLOG + " IS NOT NULL" + " AND " + Sqlite.COL_REBLOG + " != ''"
                , null);
        mCount.moveToFirst();
        statistics.setNumber_boosts(mCount.getInt(0));
        mCount.close();

        //Count replies
        mCount = db.rawQuery("select count(*) from " + Sqlite.TABLE_STATUSES_CACHE
                        + " where " + Sqlite.COL_CACHED_ACTION + " = '" + ARCHIVE_CACHE + "' AND " + Sqlite.COL_USER_ID + " = '" + userId + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance +"' AND "
                        + Sqlite.COL_IN_REPLY_TO_ID + " IS NOT NULL"  + " AND " + Sqlite.COL_IN_REPLY_TO_ID + " != 'null'"  + " AND " + Sqlite.COL_REBLOG + " IS NULL"
                , null);
        mCount.moveToFirst();
        statistics.setNumber_replies(mCount.getInt(0));
        mCount.close();

        statistics.setNumber_status(statistics.getTotal_statuses() - statistics.getNumber_boosts() - statistics.getNumber_replies());

        //Count media
        mCount = db.rawQuery("select count(*) from " + Sqlite.TABLE_STATUSES_CACHE
                        + " where " + Sqlite.COL_CACHED_ACTION + " = '" + ARCHIVE_CACHE + "' AND " + Sqlite.COL_USER_ID + " = '" + userId + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance +"' AND "
                        + Sqlite.COL_MEDIA_ATTACHMENTS + " IS NOT NULL"   + " AND " + Sqlite.COL_MEDIA_ATTACHMENTS + " !='"+Helper.attachmentToStringStorage(new ArrayList<>())+"' "   + " AND " +  Sqlite.COL_REBLOG + " IS NULL"
                , null);
        mCount.moveToFirst();
        statistics.setNumber_with_media(mCount.getInt(0));
        mCount.close();


        //Count sensitive
        mCount = db.rawQuery("select count(*) from " + Sqlite.TABLE_STATUSES_CACHE
                        + " where " + Sqlite.COL_CACHED_ACTION + " = '" + ARCHIVE_CACHE + "' AND " + Sqlite.COL_USER_ID + " = '" + userId + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance +"' AND "
                        + Sqlite.COL_SENSITIVE + "= 1 AND "  + Sqlite.COL_MEDIA_ATTACHMENTS + " IS NOT NULL"   + " AND " + Sqlite.COL_MEDIA_ATTACHMENTS + " !='"+Helper.attachmentToStringStorage(new ArrayList<>())+"' "   + " AND " + Sqlite.COL_REBLOG + " IS NULL"
                , null);
        mCount.moveToFirst();
        statistics.setNumber_with_sensitive_media(mCount.getInt(0));
        mCount.close();


        //Count sensitive
        mCount = db.rawQuery("select count(*) from " + Sqlite.TABLE_STATUSES_CACHE
                        + " where " + Sqlite.COL_CACHED_ACTION + " = '" + ARCHIVE_CACHE + "' AND " + Sqlite.COL_USER_ID + " = '" + userId + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance +"' AND "
                        + Sqlite.COL_SPOILER_TEXT + " IS NOT NULL"   + " AND " +   Sqlite.COL_SPOILER_TEXT + " != '' "   + " AND " +Sqlite.COL_REBLOG + " IS NULL"
                , null);
        mCount.moveToFirst();
        statistics.setNumber_with_cw(mCount.getInt(0));
        mCount.close();

        //Count public
        mCount = db.rawQuery("select count(*) from " + Sqlite.TABLE_STATUSES_CACHE
                        + " where " + Sqlite.COL_CACHED_ACTION + " = '" + ARCHIVE_CACHE + "' AND " + Sqlite.COL_USER_ID + " = '" + userId + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance +"' AND "
                        + Sqlite.COL_VISIBILITY + "='public'"   + " AND " + Sqlite.COL_REBLOG + " IS NULL"
                , null);
        mCount.moveToFirst();
        statistics.setV_public(mCount.getInt(0));
        mCount.close();

        //Count unlisted
        mCount = db.rawQuery("select count(*) from " + Sqlite.TABLE_STATUSES_CACHE
                        + " where " + Sqlite.COL_CACHED_ACTION + " = '" + ARCHIVE_CACHE + "' AND " + Sqlite.COL_USER_ID + " = '" + userId + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance +"' AND "
                        + Sqlite.COL_VISIBILITY + "='unlisted'"   + " AND " + Sqlite.COL_REBLOG + " IS NULL"
                , null);
        mCount.moveToFirst();
        statistics.setV_unlisted(mCount.getInt(0));
        mCount.close();

        //Count private
        mCount = db.rawQuery("select count(*) from " + Sqlite.TABLE_STATUSES_CACHE
                        + " where " + Sqlite.COL_CACHED_ACTION + " = '" + ARCHIVE_CACHE + "' AND " + Sqlite.COL_USER_ID + " = '" + userId + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance +"' AND "
                        + Sqlite.COL_VISIBILITY + "='private'"   + " AND " + Sqlite.COL_REBLOG + " IS NULL"
                , null);
        mCount.moveToFirst();
        statistics.setV_private(mCount.getInt(0));
        mCount.close();


        //Count private
        mCount = db.rawQuery("select count(*) from " + Sqlite.TABLE_STATUSES_CACHE
                        + " where " + Sqlite.COL_CACHED_ACTION + " = '" + ARCHIVE_CACHE + "' AND " + Sqlite.COL_USER_ID + " = '" + userId + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance +"' AND "
                        + Sqlite.COL_VISIBILITY + "='direct'"   + " AND " + Sqlite.COL_REBLOG + " IS NULL"
                , null);
        mCount.moveToFirst();
        statistics.setV_direct(mCount.getInt(0));
        mCount.close();


        HashMap<String, Integer> countTags = new HashMap<>();
        //Get tags
        Cursor c = db.query(Sqlite.TABLE_STATUSES_CACHE, new String[]{Sqlite.COL_TAGS}, Sqlite.COL_CACHED_ACTION + " = '" + ARCHIVE_CACHE + "' AND " + Sqlite.COL_USER_ID + " = '" + userId + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance + "' AND " + Sqlite.COL_REBLOG + " IS NULL", null, null, null, null);
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                //Restore cached status
                List<Tag> tags = Helper.restoreTagFromString(c.getString(c.getColumnIndex(Sqlite.COL_TAGS)));
                if (tags != null && tags.size() > 0) {
                    for (Tag tag : tags) {
                        if (countTags.containsKey(tag.getName())) {
                            int val = countTags.get(tag.getName());
                            countTags.put(tag.getName(), val + 1);
                        } else {
                            countTags.put(tag.getName(), 1);
                        }
                    }
                }
            }
        }
        //Close the cursor
        c.close();
        if( countTags.size() > 0) {
            statistics.setTagsTrend(Helper.sortByValue(countTags));
        }else{
            statistics.setTagsTrend(countTags);
        }


        statistics.setFirstTootDate(getSmallerDate(ARCHIVE_CACHE));
        statistics.setLastTootDate(getGreaterDate(ARCHIVE_CACHE));

        long days = 1;
        if( statistics.getLastTootDate() != null && statistics.getFirstTootDate() != null) {
            long diff = statistics.getLastTootDate().getTime() - statistics.getFirstTootDate().getTime();
            days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        }
        statistics.setFrequency((float)statistics.getTotal_statuses()/days);

        return statistics;
    }


    /***
     * Method to hydrate statuses from database
     * @param c Cursor
     * @return Status
     */
    private Status cursorToStoredStatus(Cursor c){
        //No element found
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
        //Take the first element
        c.moveToFirst();
        //New status
        Status status = new Status();
        status.setViewType(context);
        status.setId(c.getString(c.getColumnIndex(Sqlite.COL_STATUS_ID)));
        status.setUri(c.getString(c.getColumnIndex(Sqlite.COL_URI)));
        status.setUrl(c.getString(c.getColumnIndex(Sqlite.COL_URL)));
        status.setAccount(Helper.restoreAccountFromString(c.getString(c.getColumnIndex(Sqlite.COL_ACCOUNT))));
        status.setCard(Helper.restoreCardFromString(c.getString(c.getColumnIndex(Sqlite.COL_CARD))));
        status.setIn_reply_to_id(c.getString(c.getColumnIndex(Sqlite.COL_IN_REPLY_TO_ID)));
        status.setIn_reply_to_account_id(c.getString(c.getColumnIndex(Sqlite.COL_IN_REPLY_TO_ACCOUNT_ID)));
        status.setReblog(Helper.restoreStatusFromString(c.getString(c.getColumnIndex(Sqlite.COL_REBLOG))));
        status.setContent(c.getString(c.getColumnIndex(Sqlite.COL_CONTENT)));
        status.setCreated_at(Helper.stringToDate(context, c.getString(c.getColumnIndex(Sqlite.COL_CREATED_AT))));
        status.setEmojis(Helper.restoreEmojisFromString(c.getString(c.getColumnIndex(Sqlite.COL_EMOJIS))));
        status.setReblogs_count(c.getInt(c.getColumnIndex(Sqlite.COL_REBLOGS_COUNT)));
        status.setFavourites_count(c.getInt(c.getColumnIndex(Sqlite.COL_FAVOURITES_COUNT)));
        status.setReblogged(c.getInt(c.getColumnIndex(Sqlite.COL_REBLOGGED))==1);
        status.setFavourited(c.getInt(c.getColumnIndex(Sqlite.COL_FAVOURITED))==1);
        status.setMuted(c.getInt(c.getColumnIndex(Sqlite.COL_MUTED))==1);
        status.setSensitive(c.getInt(c.getColumnIndex(Sqlite.COL_SENSITIVE))==1);
        status.setPinned(c.getInt(c.getColumnIndex(Sqlite.COL_PINNED))==1);
        status.setSpoiler_text(c.getString(c.getColumnIndex(Sqlite.COL_SPOILER_TEXT)));
        status.setVisibility(c.getString(c.getColumnIndex(Sqlite.COL_VISIBILITY)));
        status.setMedia_attachments(Helper.restoreAttachmentFromString(c.getString(c.getColumnIndex(Sqlite.COL_MEDIA_ATTACHMENTS))));
        status.setMentions(Helper.restoreMentionFromString(c.getString(c.getColumnIndex(Sqlite.COL_MENTIONS))));
        status.setTags(Helper.restoreTagFromString(c.getString(c.getColumnIndex(Sqlite.COL_TAGS))));
        status.setApplication(Helper.restoreApplicationFromString(c.getString(c.getColumnIndex(Sqlite.COL_APPLICATION))));
        status.setLanguage(c.getString(c.getColumnIndex(Sqlite.COL_LANGUAGE)));
        //Close the cursor
        c.close();
        //Cached status is returned
        return status;
    }

    /***
     * Method to hydrate cached statuses from database
     * @param c Cursor
     * @return List<Status>
     */
    private List<Status> cursorToListStatuses(Cursor c){
        //No element found
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
        List<Status> statuses = new ArrayList<>();
        while (c.moveToNext() ) {
            //Restore cached status
            Status status = new Status();
            status.setViewType(context);
            status.setId(c.getString(c.getColumnIndex(Sqlite.COL_STATUS_ID)));
            status.setUri(c.getString(c.getColumnIndex(Sqlite.COL_URI)));
            status.setUrl(c.getString(c.getColumnIndex(Sqlite.COL_URL)));
            status.setAccount(Helper.restoreAccountFromString(c.getString(c.getColumnIndex(Sqlite.COL_ACCOUNT))));
            status.setCard(Helper.restoreCardFromString(c.getString(c.getColumnIndex(Sqlite.COL_CARD))));
            status.setIn_reply_to_id(c.getString(c.getColumnIndex(Sqlite.COL_IN_REPLY_TO_ID)));
            status.setIn_reply_to_account_id(c.getString(c.getColumnIndex(Sqlite.COL_IN_REPLY_TO_ACCOUNT_ID)));
            status.setReblog(Helper.restoreStatusFromString(c.getString(c.getColumnIndex(Sqlite.COL_REBLOG))));
            status.setContent(c.getString(c.getColumnIndex(Sqlite.COL_CONTENT)));
            status.setCreated_at(Helper.stringToDate(context, c.getString(c.getColumnIndex(Sqlite.COL_CREATED_AT))));
            status.setEmojis(Helper.restoreEmojisFromString(c.getString(c.getColumnIndex(Sqlite.COL_EMOJIS))));
            status.setReblogs_count(c.getInt(c.getColumnIndex(Sqlite.COL_REBLOGS_COUNT)));
            status.setFavourites_count(c.getInt(c.getColumnIndex(Sqlite.COL_FAVOURITES_COUNT)));
            status.setReblogged(c.getInt(c.getColumnIndex(Sqlite.COL_REBLOGGED))==1);
            status.setFavourited(c.getInt(c.getColumnIndex(Sqlite.COL_FAVOURITED))==1);
            status.setMuted(c.getInt(c.getColumnIndex(Sqlite.COL_MUTED))==1);
            status.setSensitive(c.getInt(c.getColumnIndex(Sqlite.COL_SENSITIVE))==1);
            status.setPinned(c.getInt(c.getColumnIndex(Sqlite.COL_PINNED))==1);
            status.setSpoiler_text(c.getString(c.getColumnIndex(Sqlite.COL_SPOILER_TEXT)));
            status.setVisibility(c.getString(c.getColumnIndex(Sqlite.COL_VISIBILITY)));
            status.setMedia_attachments(Helper.restoreAttachmentFromString(c.getString(c.getColumnIndex(Sqlite.COL_MEDIA_ATTACHMENTS))));
            status.setMentions(Helper.restoreMentionFromString(c.getString(c.getColumnIndex(Sqlite.COL_MENTIONS))));
            status.setTags(Helper.restoreTagFromString(c.getString(c.getColumnIndex(Sqlite.COL_TAGS))));
            status.setApplication(Helper.restoreApplicationFromString(c.getString(c.getColumnIndex(Sqlite.COL_APPLICATION))));
            status.setLanguage(c.getString(c.getColumnIndex(Sqlite.COL_LANGUAGE)));
            statuses.add(status);
        }
        //Close the cursor
        c.close();
        //Statuses list is returned
        return statuses;
    }


    /***
     * Method to get cached statuses ID from database
     * @param c Cursor
     * @return List<Status>
     */
    private List<String> cursorToListStatusesId(Cursor c){
        //No element found
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
        List<String> statusesId = new ArrayList<>();
        while (c.moveToNext() ) {
            //Restore cached status

            statusesId.add(c.getString(c.getColumnIndex(Sqlite.COL_STATUS_ID)));
        }
        //Close the cursor
        c.close();
        //Statuses list is returned
        return statusesId;
    }
}
