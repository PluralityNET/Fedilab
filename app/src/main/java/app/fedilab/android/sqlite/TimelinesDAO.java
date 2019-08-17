package app.fedilab.android.sqlite;
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

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.client.Entities.ManageTimelines;
import app.fedilab.android.helper.Helper;

public class TimelinesDAO {


    private SQLiteDatabase db;
    public Context context;

    public TimelinesDAO(Context context, SQLiteDatabase db) {
        //Creation of the DB with tables
        this.context = context;
        this.db = db;
    }

    //------- INSERTIONS  -------
    public void insert(ManageTimelines timeline) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        ContentValues values = new ContentValues();
        values.put(Sqlite.COL_TYPE, ManageTimelines.typeToDb(timeline.getType()));
        values.put(Sqlite.COL_DISPLAYED, timeline.isDisplayed());
        values.put(Sqlite.COL_POSITION, timeline.getPosition());
        values.put(Sqlite.COL_USER_ID, userId);
        values.put(Sqlite.COL_INSTANCE, instance);
        if( timeline.getTagTimeline() != null)
            values.put(Sqlite.COL_TAG_TIMELINE, Helper.tagTimelineToStringStorage(timeline.getTagTimeline()));
        if( timeline.getRemoteInstance() != null)
            values.put(Sqlite.COL_REMOTE_INSTANCE, Helper.remoteInstanceToStringStorage(timeline.getRemoteInstance()));
        if( timeline.getListTimeline() != null)
            values.put(Sqlite.COL_LIST_TIMELINE, Helper.listTimelineToStringStorage(timeline.getListTimeline()));
        try{
            db.insert(Sqlite.TABLE_TIMELINES, null, values);
        }catch (Exception ignored) {}
    }

    //------- REMOVE  -------
    public int remove(ManageTimelines timeline){
        return db.delete(Sqlite.TABLE_TIMELINES,  Sqlite.COL_ID + " = \"" + timeline.getId() + "\"", null);
    }

    //------- REMOVE  -------
    public int removeAll(){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        return db.delete(Sqlite.TABLE_TIMELINES,  Sqlite.COL_USER_ID + " = '" + userId+ "' AND " + Sqlite.COL_INSTANCE + " = '" + instance+ "'", null);
    }

    //------- UPDATE  -------
    public int update(ManageTimelines timeline) {
        ContentValues values = new ContentValues();
        values.put(Sqlite.COL_DISPLAYED, timeline.isDisplayed());
        values.put(Sqlite.COL_POSITION, timeline.getPosition());
        return db.update(Sqlite.TABLE_TIMELINES,
                values, Sqlite.COL_ID + " =  ? ",
                new String[]{String.valueOf(timeline.getId())});
    }


    //------- UPDATE  -------
    public void updateTag(ManageTimelines timeline) {
        ContentValues values = new ContentValues();
        values.put(Sqlite.COL_DISPLAYED, timeline.isDisplayed());
        if( timeline.getTagTimeline() != null)
            values.put(Sqlite.COL_TAG_TIMELINE, Helper.tagTimelineToStringStorage(timeline.getTagTimeline()));
        db.update(Sqlite.TABLE_TIMELINES,
                values, Sqlite.COL_ID + " =  ? ",
                new String[]{String.valueOf(timeline.getId())});
    }

    //------- UPDATE  -------
    public void updateRemoteInstance(ManageTimelines timeline) {
        ContentValues values = new ContentValues();
        values.put(Sqlite.COL_DISPLAYED, timeline.isDisplayed());
        if( timeline.getTagTimeline() != null)
            values.put(Sqlite.COL_REMOTE_INSTANCE, Helper.remoteInstanceToStringStorage(timeline.getRemoteInstance()));
        db.update(Sqlite.TABLE_TIMELINES,
                values, Sqlite.COL_ID + " =  ? ",
                new String[]{String.valueOf(timeline.getId())});
    }

    public int countVisibleTimelines(){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        Cursor mCount= db.rawQuery("select count(*) from " + Sqlite.TABLE_TIMELINES
                + " where " + Sqlite.COL_USER_ID + " = '" + userId+ "' AND " + Sqlite.COL_INSTANCE + " = '" + instance+ "' AND " + Sqlite.COL_DISPLAYED + "= 1", null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        return count;
    }

    public List<ManageTimelines> getAllTimelines(){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        try {
            Cursor c = db.query(Sqlite.TABLE_TIMELINES, null, Sqlite.COL_USER_ID + " = '" + userId+ "' AND " + Sqlite.COL_INSTANCE + " = '" + instance+ "'", null, null, null, Sqlite.COL_POSITION + " ASC", null);
            return cursorToTimelines(c);
        } catch (Exception e) {
            return null;
        }
    }

    public List<ManageTimelines> getDisplayedTimelines(){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        try {
            Cursor c = db.query(Sqlite.TABLE_TIMELINES, null, Sqlite.COL_USER_ID + " = '" + userId+ "' AND " + Sqlite.COL_INSTANCE + " = '" + instance+ "' AND " + Sqlite.COL_DISPLAYED + " = '1'", null, null, null, Sqlite.COL_POSITION + " ASC", null);
            return cursorToTimelines(c);
        } catch (Exception e) {
            return null;
        }
    }


    public ManageTimelines getById(int id){
        try {
            Cursor c = db.query(Sqlite.TABLE_TIMELINES, null, Sqlite.COL_ID + " = '" + id+ "'", null, null, null, Sqlite.COL_POSITION + " ASC", null);
            return cursorToTimeline(c);
        } catch (Exception e) {
            return null;
        }
    }


    /***
     * Method to hydrate a timeline
     * @param c Cursor
     * @return ManageTimelines
     */
    private ManageTimelines cursorToTimeline(Cursor c){
        //No element found
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
        //Take the first element
        c.moveToFirst();
        //New timeline
        ManageTimelines manageTimelines = new ManageTimelines();
        manageTimelines.setId(c.getInt(c.getColumnIndex(Sqlite.COL_ID)));
        manageTimelines.setDisplayed(c.getInt(c.getColumnIndex(Sqlite.COL_DISPLAYED)) == 1);
        manageTimelines.setPosition(c.getInt(c.getColumnIndex(Sqlite.COL_POSITION)));
        if( c.getString(c.getColumnIndex(Sqlite.COL_TAG_TIMELINE)) != null )
            manageTimelines.setTagTimeline(Helper.restoreTagTimelineFromString(c.getString(c.getColumnIndex(Sqlite.COL_TAG_TIMELINE))));
        if( c.getString(c.getColumnIndex(Sqlite.COL_REMOTE_INSTANCE)) != null )
            manageTimelines.setRemoteInstance(Helper.restoreRemoteInstanceFromString(c.getString(c.getColumnIndex(Sqlite.COL_REMOTE_INSTANCE))));
        if( c.getString(c.getColumnIndex(Sqlite.COL_LIST_TIMELINE)) != null )
            manageTimelines.setListTimeline(Helper.restoreListtimelineFromString(c.getString(c.getColumnIndex(Sqlite.COL_LIST_TIMELINE))));
        manageTimelines.setType(ManageTimelines.typeFromDb(c.getString(c.getColumnIndex(Sqlite.COL_TYPE))));
        //Close the cursor
        c.close();
        //Timeline is returned
        return manageTimelines;
    }


    /***
     * Method to hydrate stored instances from database
     * @param c Cursor
     * @return List<RemoteInstance>
     */
    private List<ManageTimelines> cursorToTimelines(Cursor c){
        //No element found
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
        List<ManageTimelines> remoteInstances = new ArrayList<>();
        while (c.moveToNext() ) {

            ManageTimelines manageTimelines = new ManageTimelines();
            manageTimelines.setId(c.getInt(c.getColumnIndex(Sqlite.COL_ID)));
            manageTimelines.setDisplayed(c.getInt(c.getColumnIndex(Sqlite.COL_DISPLAYED)) == 1);
            manageTimelines.setPosition(c.getInt(c.getColumnIndex(Sqlite.COL_POSITION)));
            if( c.getString(c.getColumnIndex(Sqlite.COL_TAG_TIMELINE)) != null )
                manageTimelines.setTagTimeline(Helper.restoreTagTimelineFromString(c.getString(c.getColumnIndex(Sqlite.COL_TAG_TIMELINE))));
            if( c.getString(c.getColumnIndex(Sqlite.COL_REMOTE_INSTANCE)) != null )
                manageTimelines.setRemoteInstance(Helper.restoreRemoteInstanceFromString(c.getString(c.getColumnIndex(Sqlite.COL_REMOTE_INSTANCE))));
            if( c.getString(c.getColumnIndex(Sqlite.COL_LIST_TIMELINE)) != null )
                manageTimelines.setListTimeline(Helper.restoreListtimelineFromString(c.getString(c.getColumnIndex(Sqlite.COL_LIST_TIMELINE))));
            manageTimelines.setType(ManageTimelines.typeFromDb(c.getString(c.getColumnIndex(Sqlite.COL_TYPE))));
            remoteInstances.add(manageTimelines);
        }
        //Close the cursor
        c.close();
        return remoteInstances;
    }


}
