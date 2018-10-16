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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.Headers;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Notification;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;

import static fr.gouv.etalab.mastodon.helper.Helper.INTENT_ACTION;
import static fr.gouv.etalab.mastodon.helper.Helper.INTENT_TARGETED_ACCOUNT;
import static fr.gouv.etalab.mastodon.helper.Helper.NOTIFICATION_INTENT;
import static fr.gouv.etalab.mastodon.helper.Helper.PREF_KEY_ID;
import static fr.gouv.etalab.mastodon.helper.Helper.notify_user;


/**
 * Created by Thomas on 29/11/2017.
 * Manage service for streaming api and new notifications
 */

public class LiveNotificationService extends Service implements NetworkStateReceiver.NetworkStateReceiverListener {



    protected Account account;
    boolean backgroundProcess;
    private static Thread thread;
    private NetworkStateReceiver networkStateReceiver;

    public void onCreate() {
        super.onCreate();
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        startStream();
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void startStream(){
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        backgroundProcess = sharedpreferences.getBoolean(Helper.SET_KEEP_BACKGROUND_PROCESS, true);
        boolean liveNotifications = sharedpreferences.getBoolean(Helper.SET_LIVE_NOTIFICATIONS, true);
        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        if( liveNotifications ){
            if( thread == null || !thread.isAlive())
                thread = new Thread() {
                    @Override
                    public void run() {
                        List<Account> accountStreams = new AccountDAO(getApplicationContext(), db).getAllAccount();
                        if (accountStreams != null){
                            for (final Account accountStream : accountStreams) {
                                taks(accountStream);
                            }
                        }
                    }
                };
            thread.start();
        }
    }

    static {
        Helper.installProvider();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if( intent == null || intent.getBooleanExtra("stop", false) ) {
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent){
        if(backgroundProcess){
            restart();
        }
        super.onTaskRemoved(rootIntent);
    }

    private void restart(){
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);
    }

    private void taks(Account account) {


        if (account != null) {
            Headers headers = new Headers();
            headers.add("Authorization", "Bearer " + account.getToken());
            headers.add("Connection", "Keep-Alive");
            headers.add("method", "GET");
            headers.add("scheme", "https");
            Uri url = Uri.parse("wss://" + account.getInstance() + "/api/v1/streaming/?stream=user&access_token=" + account.getToken());
            AsyncHttpRequest.setDefaultHeaders(headers, url);
            try {
                AsyncHttpClient.getDefaultInstance().websocket("wss://" + account.getInstance() + "/api/v1/streaming/?stream=user&access_token=" + account.getToken(), "wss", new AsyncHttpClient.WebSocketConnectCallback() {
                    @Override
                    public void onCompleted(Exception ex, WebSocket webSocket) {
                        if (ex != null) {
                            ex.printStackTrace();
                            return;
                        }
                        webSocket.setStringCallback(new WebSocket.StringCallback() {
                            public void onStringAvailable(String s) {
                                try {
                                    JSONObject eventJson = new JSONObject(s);
                                    onRetrieveStreaming(account, eventJson);
                                } catch (JSONException ignored) { ignored.printStackTrace();
                                }
                            }
                        });
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
                startStream();
            }

        }
    }


    private void onRetrieveStreaming(Account account, JSONObject response) {
        if(  response == null )
            return;
        fr.gouv.etalab.mastodon.client.Entities.Status status ;
        final Notification notification;
        String dataId = null;
        Bundle b = new Bundle();
        boolean canSendBroadCast = true;
        Helper.EventStreaming event = null;
        try {
            switch (response.get("event").toString()) {
                case "notification":
                    event = Helper.EventStreaming.NOTIFICATION;
                    notification = API.parseNotificationResponse(getApplicationContext(), new JSONObject(response.get("payload").toString()));
                    b.putParcelable("data", notification);

                    final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                    boolean liveNotifications = sharedpreferences.getBoolean(Helper.SET_LIVE_NOTIFICATIONS, true);
                    boolean canNotify = Helper.canNotify(getApplicationContext());
                    boolean notify = sharedpreferences.getBoolean(Helper.SET_NOTIFY, true);
                    String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
                    String targeted_account = null;
                    Helper.NotifType notifType = Helper.NotifType.MENTION;
                    boolean activityRunning = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isMainActivityRunning", false);
                    if ((userId == null || !userId.equals(account.getId()) || !activityRunning) && liveNotifications && canNotify && notify) {
                        boolean notif_follow = sharedpreferences.getBoolean(Helper.SET_NOTIF_FOLLOW, true);
                        boolean notif_add = sharedpreferences.getBoolean(Helper.SET_NOTIF_ADD, true);
                        boolean notif_mention = sharedpreferences.getBoolean(Helper.SET_NOTIF_MENTION, true);
                        boolean notif_share = sharedpreferences.getBoolean(Helper.SET_NOTIF_SHARE, true);
                        boolean somethingToPush = (notif_follow || notif_add || notif_mention || notif_share);
                        String title = null;
                        if (somethingToPush && notification != null) {
                            switch (notification.getType()) {
                                case "mention":
                                    notifType = Helper.NotifType.MENTION;
                                    if (notif_mention) {
                                        if (notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0)
                                            title = String.format("%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true), getString(R.string.notif_mention));
                                        else
                                            title = String.format("@%s %s", notification.getAccount().getAcct(), getString(R.string.notif_mention));
                                    } else {
                                        canSendBroadCast = false;
                                    }
                                    break;
                                case "reblog":
                                    notifType = Helper.NotifType.BOOST;
                                    if (notif_share) {
                                        if (notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0)
                                            title = String.format("%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true), getString(R.string.notif_reblog));
                                        else
                                            title = String.format("@%s %s", notification.getAccount().getAcct(), getString(R.string.notif_reblog));
                                    } else {
                                        canSendBroadCast = false;
                                    }
                                    break;
                                case "favourite":
                                    notifType = Helper.NotifType.FAV;
                                    if (notif_add) {
                                        if (notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0)
                                            title = String.format("%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true), getString(R.string.notif_favourite));
                                        else
                                            title = String.format("@%s %s", notification.getAccount().getAcct(), getString(R.string.notif_favourite));
                                    } else {
                                        canSendBroadCast = false;
                                    }
                                    break;
                                case "follow":
                                    notifType = Helper.NotifType.FOLLLOW;
                                    if (notif_follow) {
                                        if (notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0)
                                            title = String.format("%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true), getString(R.string.notif_follow));
                                        else
                                            title = String.format("@%s %s", notification.getAccount().getAcct(), getString(R.string.notif_follow));
                                        targeted_account = notification.getAccount().getId();
                                    } else {
                                        canSendBroadCast = false;
                                    }
                                    break;
                                default:
                            }
                            //Some others notification
                            final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra(INTENT_ACTION, NOTIFICATION_INTENT);
                            intent.putExtra(PREF_KEY_ID, account.getId());
                            if (targeted_account != null)
                                intent.putExtra(INTENT_TARGETED_ACCOUNT, targeted_account);
                            long notif_id = Long.parseLong(account.getId());
                            final int notificationId = ((notif_id + 1) > 2147483647) ? (int) (2147483647 - notif_id - 1) : (int) (notif_id + 1);
                            if (notification.getAccount().getAvatar() != null) {
                                final String finalTitle = title;
                                Handler mainHandler = new Handler(Looper.getMainLooper());
                                Helper.NotifType finalNotifType = notifType;
                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        if (finalTitle != null) {
                                            Glide.with(getApplicationContext())
                                                    .asBitmap()
                                                    .load(notification.getAccount().getAvatar())
                                                    .listener(new RequestListener<Bitmap>() {
                                                        @Override
                                                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                                            return false;
                                                        }

                                                        @Override
                                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                                                            notify_user(getApplicationContext(), intent, notificationId, BitmapFactory.decodeResource(getResources(),
                                                                    R.drawable.mastodonlogo), finalNotifType, finalTitle, "@" + account.getAcct() + "@" + account.getInstance());
                                                            String lastNotif = sharedpreferences.getString(Helper.LAST_NOTIFICATION_MAX_ID + account.getId() + account.getInstance(), null);
                                                            if (lastNotif == null || Long.parseLong(notification.getId()) > Long.parseLong(lastNotif)) {
                                                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                                                editor.putString(Helper.LAST_NOTIFICATION_MAX_ID + account.getId() + account.getInstance(), notification.getId());
                                                                editor.apply();
                                                            }
                                                            return false;
                                                        }
                                                    })
                                                    .into(new SimpleTarget<Bitmap>() {
                                                        @Override
                                                        public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                                                            notify_user(getApplicationContext(), intent, notificationId, resource, finalNotifType, finalTitle, "@" + account.getAcct() + "@" + account.getInstance());
                                                            String lastNotif = sharedpreferences.getString(Helper.LAST_NOTIFICATION_MAX_ID + account.getId() + account.getInstance(), null);
                                                            if (lastNotif == null || Long.parseLong(notification.getId()) > Long.parseLong(lastNotif)) {
                                                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                                                editor.putString(Helper.LAST_NOTIFICATION_MAX_ID + account.getId() + account.getInstance(), notification.getId());
                                                                editor.apply();
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                };
                                mainHandler.post(myRunnable);
                            }
                        }
                    }
                    break;
                case "update":
                    event = Helper.EventStreaming.UPDATE;
                    status = API.parseStatuses(getApplicationContext(), new JSONObject(response.get("payload").toString()));
                    status.setNew(true);
                    b.putParcelable("data", status);
                    break;
                case "delete":
                    event = Helper.EventStreaming.DELETE;
                    try {
                        dataId = response.getString("id");
                        b.putString("dataId", dataId);
                    } catch (JSONException ignored) {
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if( canSendBroadCast) {
            if (account != null)
                b.putString("userIdService", account.getId());
            Intent intentBC = new Intent(Helper.RECEIVE_DATA);
            intentBC.putExtra("eventStreaming", event);
            intentBC.putExtras(b);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentBC);
        }
    }

    @Override
    public void networkAvailable() {
        startStream();
    }

    @Override
    public void networkUnavailable() {
    }
}
