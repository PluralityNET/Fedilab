package fr.gouv.etalab.mastodon.services;
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

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.StatusCacheDAO;

import static fr.gouv.etalab.mastodon.helper.Helper.notify_user;


/**
 * Created by Thomas on 17/02/2018.
 * Manage service for owner status backup in database
 */

public class BackupStatusInDataBaseService extends IntentService {


    private static int instanceRunning = 0;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    @SuppressWarnings("unused")
    public BackupStatusInDataBaseService(String name) {
        super(name);
    }
    @SuppressWarnings("unused")
    public BackupStatusInDataBaseService() {
        super("BackupStatusInDataBaseService");
    }



    public void onCreate() {
        super.onCreate();
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if( instanceRunning == 0 ){
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toasty.info(getApplicationContext(), getString(R.string.data_export_start), Toast.LENGTH_LONG).show();
                }
            });
        }else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toasty.info(getApplicationContext(), getString(R.string.data_export_running), Toast.LENGTH_LONG).show();
                }
            });
            return;
        }
        instanceRunning++;
        String message;
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        SQLiteDatabase db = Sqlite.getInstance(BackupStatusInDataBaseService.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        Account account = new AccountDAO(getApplicationContext(), db).getAccountByID(userId);
        API api = new API(getApplicationContext(), account.getInstance(), account.getToken());
        try {
            //Starts from the last recorded ID
            String since_id = new StatusCacheDAO(BackupStatusInDataBaseService.this, db).getLastTootIDCache(StatusCacheDAO.ARCHIVE_CACHE);
            String max_id = null;
            List<Status> backupStatus = new ArrayList<>();
            boolean canContinue = true;
            do {
                APIResponse apiResponse = api.getStatus(userId, max_id);
                max_id = apiResponse.getMax_id();
                List<Status> statuses = apiResponse.getStatuses();
                for(Status tmpStatus : statuses) {
                    if(since_id != null && max_id != null && Long.parseLong(tmpStatus.getId()) <= Long.parseLong(since_id)){
                        canContinue = false;
                        break;
                    }
                    new StatusCacheDAO(BackupStatusInDataBaseService.this, db).insertStatus(StatusCacheDAO.ARCHIVE_CACHE, tmpStatus);
                    backupStatus.add(tmpStatus);
                }
            }while (max_id != null && canContinue);

            if(backupStatus.size() > 0){
                Intent backupIntent = new Intent(Helper.INTENT_BACKUP_FINISH);
                LocalBroadcastManager.getInstance(this).sendBroadcast(backupIntent);
            }
            message = getString(R.string.data_backup_success, String.valueOf(backupStatus.size()));
            Intent mainActivity = new Intent(BackupStatusInDataBaseService.this, MainActivity.class);
            mainActivity.putExtra(Helper.INTENT_ACTION, Helper.BACKUP_INTENT);
            long notif_id = Long.parseLong(account.getId());
            int notificationId = ((notif_id + 4) > 2147483647) ? (int) (2147483647 - notif_id - 4) : (int) (notif_id + 4);
            String title = getString(R.string.data_backup_toots, account.getAcct());
            notify_user(getApplicationContext(), mainActivity, notificationId, BitmapFactory.decodeResource(getResources(),
                    R.drawable.mastodonlogo), Helper.NotifType.BACKUP, title, message);
        } catch (Exception e) {
            e.printStackTrace();
            message = getString(R.string.data_export_error, account.getAcct());
            final String finalMessage = message;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toasty.error(getApplicationContext(), finalMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
        instanceRunning--;

    }


}
