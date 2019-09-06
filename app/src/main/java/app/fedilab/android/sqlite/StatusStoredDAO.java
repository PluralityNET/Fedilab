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
import java.util.Date;
import java.util.List;

import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.client.Entities.StoredStatus;
import app.fedilab.android.helper.Helper;


/**
 * Created by Thomas on 15/07/2017.
 * Manage Status storage in DB
 */
public class StatusStoredDAO {

    private SQLiteDatabase db;
    public Context context;


    public StatusStoredDAO(Context context, SQLiteDatabase db) {
        //Creation of the DB with tables
        this.context = context;
        this.db = db;
    }


    //------- INSERTIONS  -------

    /**
     * Insert a status in database
     *
     * @param status Status
     * @return boolean
     */
    public long insertStatus(Status status, Status statusReply) {
        ContentValues values = new ContentValues();
        String serializedStatus = Helper.statusToStringStorage(status);

        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        if (userId == null || instance == null)
            return -1;
        if (statusReply != null) {
            String serializedStatusReply = Helper.statusToStringStorage(statusReply);
            values.put(Sqlite.COL_STATUS_REPLY_SERIALIZED, serializedStatusReply);
        }
        values.put(Sqlite.COL_STATUS_SERIALIZED, serializedStatus);
        values.put(Sqlite.COL_DATE_CREATION, Helper.dateToString(new Date()));
        values.put(Sqlite.COL_IS_SCHEDULED, 0);
        values.put(Sqlite.COL_INSTANCE, instance);
        values.put(Sqlite.COL_USER_ID, userId);
        values.put(Sqlite.COL_SENT, 0);
        //Inserts stored status
        long last_id;
        try {
            last_id = db.insert(Sqlite.TABLE_STATUSES_STORED, null, values);
        } catch (Exception e) {
            last_id = -1;
        }
        return last_id;
    }

    //------- UPDATES  -------

    /**
     * Update a Status in database
     *
     * @param status Status
     * @return boolean
     */
    public int updateStatus(long id, Status status) {
        ContentValues values = new ContentValues();

        String serializedStatus = Helper.statusToStringStorage(status);
        values.put(Sqlite.COL_STATUS_SERIALIZED, serializedStatus);
        values.put(Sqlite.COL_DATE_CREATION, Helper.dateToString(new Date()));
        return db.update(Sqlite.TABLE_STATUSES_STORED,
                values, Sqlite.COL_ID + " =  ? ",
                new String[]{String.valueOf(id)});
    }

    /**
     * Update a Status in database
     *
     * @param id    long
     * @param jobId int
     * @return int
     */
    public int updateJobId(long id, int jobId) {
        ContentValues values = new ContentValues();
        values.put(Sqlite.COL_IS_SCHEDULED, jobId);
        return db.update(Sqlite.TABLE_STATUSES_STORED,
                values, Sqlite.COL_ID + " =  ? ",
                new String[]{String.valueOf(id)});
    }

    /**
     * Schedule a status in db
     *
     * @param id             long
     * @param jobId          int
     * @param date_scheduled Date
     * @return boolean
     */
    public int scheduleStatus(long id, int jobId, Date date_scheduled) {
        ContentValues values = new ContentValues();
        values.put(Sqlite.COL_IS_SCHEDULED, jobId);
        values.put(Sqlite.COL_DATE_SCHEDULED, Helper.dateToString(date_scheduled));
        return db.update(Sqlite.TABLE_STATUSES_STORED,
                values, Sqlite.COL_ID + " =  ? ",
                new String[]{String.valueOf(id)});
    }

    /**
     * Update scheduled date for a Status in database
     *
     * @param scheduled_date Date
     * @return boolean
     */
    public int updateScheduledDate(int jobid, Date scheduled_date) {
        ContentValues values = new ContentValues();
        values.put(Sqlite.COL_DATE_SCHEDULED, Helper.dateToString(scheduled_date));
        return db.update(Sqlite.TABLE_STATUSES_STORED,
                values, Sqlite.COL_IS_SCHEDULED + " =  ? ",
                new String[]{String.valueOf(jobid)});
    }

    /**
     * Update date when task is done for a scheduled Status in database
     *
     * @param jobid     int
     * @param date_sent Date
     * @return boolean
     */
    public int updateScheduledDone(int jobid, Date date_sent) {
        ContentValues values = new ContentValues();
        values.put(Sqlite.COL_DATE_SENT, Helper.dateToString(date_sent));
        values.put(Sqlite.COL_SENT, 1);
        return db.update(Sqlite.TABLE_STATUSES_STORED,
                values, Sqlite.COL_IS_SCHEDULED + " =  ? ",
                new String[]{String.valueOf(jobid)});
    }

    //------- REMOVE  -------

    /***
     * Remove stored status by id
     * @return int
     */
    public int remove(long id) {
        return db.delete(Sqlite.TABLE_STATUSES_STORED, Sqlite.COL_ID + " = \"" + id + "\"", null);
    }

    public int removeAllDrafts() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        return db.delete(Sqlite.TABLE_STATUSES_STORED, Sqlite.COL_IS_SCHEDULED + " = \"0\" AND " + Sqlite.COL_USER_ID + " = '" + userId + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance + "'", null);
    }

    public int removeAllSent() {
        return db.delete(Sqlite.TABLE_STATUSES_STORED, Sqlite.COL_IS_SCHEDULED + " != 0 AND " + Sqlite.COL_SENT + " = 1", null);
    }

    //------- GETTERS  -------

    /**
     * Returns all stored Statuses in db
     *
     * @return stored status List<StoredStatus>
     */
    public List<StoredStatus> getAllStatus() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        try {
            Cursor c = db.query(Sqlite.TABLE_STATUSES_STORED, null, Sqlite.COL_USER_ID + " = '" + userId + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance + "'", null, null, null, Sqlite.COL_DATE_CREATION + " DESC", null);
            return cursorToListStatuses(c);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns all stored Statuses in db
     *
     * @return stored status List<StoredStatus>
     */
    public List<StoredStatus> getAllDrafts() {
        try {
            SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            String instance = Helper.getLiveInstance(context);
            Cursor c = db.query(Sqlite.TABLE_STATUSES_STORED, null, Sqlite.COL_USER_ID + " = '" + userId + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance + "' AND " + Sqlite.COL_IS_SCHEDULED + " = 0", null, null, null, Sqlite.COL_DATE_CREATION + " DESC", null);
            return cursorToListStatuses(c);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Returns all scheduled Statuses in db
     *
     * @return stored status List<StoredStatus>
     */
    public List<StoredStatus> getAllScheduled() {
        try {
            SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            String instance = Helper.getLiveInstance(context);
            Cursor c = db.query(Sqlite.TABLE_STATUSES_STORED, null, Sqlite.COL_USER_ID + " = '" + userId + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance + "' AND " + Sqlite.COL_IS_SCHEDULED + " != 0 AND " + Sqlite.COL_SENT + " = 0", null, null, null, Sqlite.COL_DATE_SCHEDULED + " ASC", null);
            return cursorToListStatuses(c);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns all not sent Statuses in db
     *
     * @return stored status List<StoredStatus>
     */
    public List<StoredStatus> getAllNotSent() {
        try {
            SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            String instance = Helper.getLiveInstance(context);
            Cursor c = db.query(Sqlite.TABLE_STATUSES_STORED, null, Sqlite.COL_USER_ID + " = '" + userId + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance + "' AND " + Sqlite.COL_IS_SCHEDULED + " != 0 AND " + Sqlite.COL_SENT + " = 0", null, null, null, Sqlite.COL_DATE_CREATION + " DESC", null);
            return cursorToListStatuses(c);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns all sent Statuses in db
     *
     * @return stored status List<StoredStatus>
     */
    public List<StoredStatus> getAllSent() {
        try {
            SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            String instance = Helper.getLiveInstance(context);
            Cursor c = db.query(Sqlite.TABLE_STATUSES_STORED, null, Sqlite.COL_USER_ID + " = '" + userId + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance + "' AND " + Sqlite.COL_IS_SCHEDULED + " != 0 AND " + Sqlite.COL_SENT + " = 1", null, null, null, Sqlite.COL_DATE_CREATION + " DESC", null);
            return cursorToListStatuses(c);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns a stored status by id in db
     *
     * @return stored status StoredStatus
     */
    public StoredStatus getStatus(long id) {
        try {
            Cursor c = db.query(Sqlite.TABLE_STATUSES_STORED, null, Sqlite.COL_ID + " = '" + id + "'", null, null, null, null, null);
            return cursorToStoredStatus(c);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Returns a stored status by id of job in db
     *
     * @return stored status StoredStatus
     */
    public StoredStatus getStatusScheduled(int jobid) {
        try {
            Cursor c = db.query(Sqlite.TABLE_STATUSES_STORED, null, Sqlite.COL_IS_SCHEDULED + " = '" + jobid + "'", null, null, null, null, null);
            return cursorToStoredStatus(c);
        } catch (Exception e) {
            return null;
        }
    }

    /***
     * Method to hydrate Stored statuses from database
     * @param c Cursor
     * @return StoredStatus
     */
    private StoredStatus cursorToStoredStatus(Cursor c) {
        //No element found
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
        //Take the first element
        c.moveToFirst();
        //New user
        StoredStatus storedStatus = new StoredStatus();
        storedStatus.setId(c.getInt(c.getColumnIndex(Sqlite.COL_ID)));
        Status status = Helper.restoreStatusFromString(c.getString(c.getColumnIndex(Sqlite.COL_STATUS_SERIALIZED)));
        if (status == null) {
            remove(c.getInt(c.getColumnIndex(Sqlite.COL_ID)));
            return null;
        }
        storedStatus.setStatus(status);
        Status statusReply = Helper.restoreStatusFromString(c.getString(c.getColumnIndex(Sqlite.COL_STATUS_REPLY_SERIALIZED)));
        storedStatus.setStatusReply(statusReply);
        storedStatus.setSent(c.getInt(c.getColumnIndex(Sqlite.COL_SENT)) == 1);
        storedStatus.setJobId(c.getInt(c.getColumnIndex(Sqlite.COL_IS_SCHEDULED)));
        storedStatus.setCreation_date(Helper.stringToDate(context, c.getString(c.getColumnIndex(Sqlite.COL_DATE_CREATION))));
        storedStatus.setScheduled_date(Helper.stringToDate(context, c.getString(c.getColumnIndex(Sqlite.COL_DATE_SCHEDULED))));
        storedStatus.setSent_date(Helper.stringToDate(context, c.getString(c.getColumnIndex(Sqlite.COL_DATE_SENT))));
        storedStatus.setUserId(c.getString(c.getColumnIndex(Sqlite.COL_USER_ID)));
        storedStatus.setInstance(c.getString(c.getColumnIndex(Sqlite.COL_INSTANCE)));
        //Close the cursor
        c.close();
        //Stored status is returned
        return storedStatus;
    }

    /***
     * Method to hydrate stored statuses from database
     * @param c Cursor
     * @return List<StoredStatus>
     */
    private List<StoredStatus> cursorToListStatuses(Cursor c) {
        //No element found
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
        List<StoredStatus> storedStatuses = new ArrayList<>();
        while (c.moveToNext()) {
            //Restore the status
            StoredStatus storedStatus = new StoredStatus();
            storedStatus.setId(c.getInt(c.getColumnIndex(Sqlite.COL_ID)));
            Status status = Helper.restoreStatusFromString(c.getString(c.getColumnIndex(Sqlite.COL_STATUS_SERIALIZED)));
            if (status == null) {
                remove(c.getInt(c.getColumnIndex(Sqlite.COL_ID)));
                continue;
            }
            storedStatus.setStatus(status);
            Status statusReply = Helper.restoreStatusFromString(c.getString(c.getColumnIndex(Sqlite.COL_STATUS_REPLY_SERIALIZED)));
            storedStatus.setStatusReply(statusReply);
            storedStatus.setSent(c.getInt(c.getColumnIndex(Sqlite.COL_SENT)) == 1);
            storedStatus.setJobId(c.getInt(c.getColumnIndex(Sqlite.COL_IS_SCHEDULED)));
            storedStatus.setCreation_date(Helper.stringToDate(context, c.getString(c.getColumnIndex(Sqlite.COL_DATE_CREATION))));
            storedStatus.setScheduled_date(Helper.stringToDate(context, c.getString(c.getColumnIndex(Sqlite.COL_DATE_SCHEDULED))));
            storedStatus.setSent_date(Helper.stringToDate(context, c.getString(c.getColumnIndex(Sqlite.COL_DATE_SENT))));
            storedStatus.setUserId(c.getString(c.getColumnIndex(Sqlite.COL_USER_ID)));
            storedStatus.setInstance(c.getString(c.getColumnIndex(Sqlite.COL_INSTANCE)));
            storedStatuses.add(storedStatus);
        }
        //Close the cursor
        c.close();
        //Statuses list is returned
        return storedStatuses;
    }
}
