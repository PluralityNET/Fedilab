package app.fedilab.android.services;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.Headers;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import app.fedilab.android.client.API;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Notification;
import app.fedilab.android.client.TLSSocketFactory;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.R;
import app.fedilab.android.activities.MainActivity;


/**
 * Created by Thomas on 29/11/2017.
 * Manage service for streaming api and new notifications
 */

public class LiveNotificationService extends Service implements NetworkStateReceiver.NetworkStateReceiverListener {

    static {
        Helper.installProvider();
    }

    protected Account account;
    boolean backgroundProcess;
    private static HashMap<String, Thread>  threads = new HashMap<>();
    private NetworkStateReceiver networkStateReceiver;
    private static HashMap<String, WebSocket> webSocketFutures = new HashMap<>();

    public void onCreate() {
        super.onCreate();
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void startStream(){
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        backgroundProcess = sharedpreferences.getBoolean(Helper.SET_KEEP_BACKGROUND_PROCESS, true);
        boolean liveNotifications = sharedpreferences.getBoolean(Helper.SET_LIVE_NOTIFICATIONS, true);
        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        if( liveNotifications ){
            List<Account> accountStreams = new AccountDAO(getApplicationContext(), db).getAllAccountCrossAction();
            if (accountStreams != null) {
                for (final Account accountStream : accountStreams) {
                    startWork(accountStream);
                }
            }
        }
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if( intent == null || intent.getBooleanExtra("stop", false) ) {
            stopSelf();
        }
        if( backgroundProcess)
            return START_STICKY;
        else
            return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        networkStateReceiver.removeListener(this);
        unregisterReceiver(networkStateReceiver);
    }




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent){

        super.onTaskRemoved(rootIntent);
        if(backgroundProcess){
            restart();
        }
    }

    private void restart(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(getApplicationContext(),new Intent(getApplicationContext(), LiveNotificationService.class));
        } else {
            Intent restartServiceIntent = new Intent(LiveNotificationService.this, LiveNotificationService.class);
            restartServiceIntent.setPackage(getPackageName());
            PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            assert alarmService != null;
            alarmService.set(
                    AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + 1000,
                    restartServicePendingIntent);
        }
    }

    private void taks(Account account) {
        if (account != null) {
            Headers headers = new Headers();
            headers.add("Authorization", "Bearer " + account.getToken());
            headers.add("Connection", "Keep-Alive");
            headers.add("method", "GET");
            headers.add("scheme", "https");
            String notif_url = "user:notification";
            if( account.getSocial().toUpperCase().equals("PLEROMA"))
                notif_url = "user";
            String urlKey = "wss://" + account.getInstance() + "/api/v1/streaming/?stream="+notif_url+"&access_token=" + account.getToken();
            Uri url = Uri.parse(urlKey);
            AsyncHttpRequest.setDefaultHeaders(headers, url);
            if( Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT ) {
                try {
                    AsyncHttpClient.getDefaultInstance().getSSLSocketMiddleware().setSSLContext(new TLSSocketFactory(account.getInstance()).getSSLContext());
                    AsyncHttpClient.getDefaultInstance().getSSLSocketMiddleware().setConnectAllAddresses(true);
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
            AsyncHttpClient.getDefaultInstance().websocket("wss://" + account.getInstance() + "/api/v1/streaming/?stream="+notif_url+"&access_token=" + account.getToken(), "wss", new AsyncHttpClient.WebSocketConnectCallback() {
                @Override
                public void onCompleted(Exception ex, WebSocket webSocket) {
                    webSocketFutures.put(account.getAcct()+"@"+account.getInstance(), webSocket);
                    if (ex != null) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startWork(account);
                            }
                        }, 60000 );
                        return;
                    }
                    webSocket.setStringCallback(new WebSocket.StringCallback() {
                        public void onStringAvailable(String s) {
                            try {
                                JSONObject eventJson = new JSONObject(s);
                                onRetrieveStreaming(account, eventJson);
                            } catch (JSONException ignored) {}
                        }
                    });

                    webSocket.setClosedCallback(new CompletedCallback() {
                        @Override
                        public void onCompleted(Exception ex) {
                            startWork(account);
                        }
                    });
                    webSocket.setDataCallback(new DataCallback() {
                        public void onDataAvailable(DataEmitter emitter, ByteBufferList byteBufferList) {
                            // note that this data has been read
                            byteBufferList.recycle();
                        }
                    });
                }
            });


        }
    }


    private void startWork(Account accountStream){
        String key = accountStream.getAcct() + "@" + accountStream.getInstance();
        if(webSocketFutures.containsKey(key)){
            if (webSocketFutures.get(key) != null && Objects.requireNonNull(webSocketFutures.get(key)).isOpen()) {
                try {
                    Objects.requireNonNull(webSocketFutures.get(key)).close();

                }catch (Exception ignored){}
            }
        }
        if(threads.containsKey(key)){
            if (threads.get(key) != null && !Objects.requireNonNull(threads.get(key)).isAlive()) {
                Objects.requireNonNull(threads.get(key)).interrupt();
            }
        }
        Thread thread = new Thread() {
            @Override
            public void run() {

                taks(accountStream);
            }
        };
        thread.start();
        threads.put(accountStream.getAcct() + "@" + accountStream.getInstance(), thread);
    }

    private void onRetrieveStreaming(Account account, JSONObject response) {
        if(  response == null )
            return;
        final Notification notification;
        String dataId;
        Bundle b = new Bundle();
        boolean canSendBroadCast = true;
        Helper.EventStreaming event;
        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        try {
            switch (response.get("event").toString()) {
                case "notification":
                    event = Helper.EventStreaming.NOTIFICATION;
                    notification = API.parseNotificationResponse(getApplicationContext(), new JSONObject(response.get("payload").toString()));
                    b.putParcelable("data", notification);
                    boolean liveNotifications = sharedpreferences.getBoolean(Helper.SET_LIVE_NOTIFICATIONS, true);
                    boolean canNotify = Helper.canNotify(getApplicationContext());
                    boolean notify = sharedpreferences.getBoolean(Helper.SET_NOTIFY, true);
                    String targeted_account = null;
                    Helper.NotifType notifType = Helper.NotifType.MENTION;
                    boolean activityRunning = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isMainActivityRunning", false);
                    if ((userId == null || !userId.equals(account.getId()) || !activityRunning) && liveNotifications && canNotify && notify) {
                        boolean notif_follow = sharedpreferences.getBoolean(Helper.SET_NOTIF_FOLLOW, true);
                        boolean notif_add = sharedpreferences.getBoolean(Helper.SET_NOTIF_ADD, true);
                        boolean notif_mention = sharedpreferences.getBoolean(Helper.SET_NOTIF_MENTION, true);
                        boolean notif_share = sharedpreferences.getBoolean(Helper.SET_NOTIF_SHARE, true);
                        boolean notif_poll = sharedpreferences.getBoolean(Helper.SET_NOTIF_POLL, true);
                        boolean somethingToPush = (notif_follow || notif_add || notif_mention || notif_share || notif_poll);
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
                                case "poll":
                                    notifType = Helper.NotifType.POLL;
                                    if (notif_poll) {
                                        if (notification.getAccount().getId() != null && notification.getAccount().getId().equals(userId))
                                            title = getString(R.string.notif_poll_self);
                                        else
                                            title = getString(R.string.notif_poll);
                                    } else {
                                        canSendBroadCast = false;
                                    }
                                    break;
                                default:
                            }
                            //Some others notification
                            final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra(Helper.INTENT_ACTION, Helper.NOTIFICATION_INTENT);
                            intent.putExtra(Helper.PREF_KEY_ID, account.getId());
                            intent.putExtra(Helper.PREF_INSTANCE, account.getInstance());
                            if (targeted_account != null) {
                                intent.putExtra(Helper.INTENT_TARGETED_ACCOUNT, targeted_account);
                            }
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
                                                        Helper.notify_user(getApplicationContext(),account, intent, BitmapFactory.decodeResource(getResources(),
                                                                R.drawable.mastodonlogo), finalNotifType, finalTitle, "@" + account.getAcct() + "@" + account.getInstance());
                                                        String lastNotif = sharedpreferences.getString(Helper.LAST_NOTIFICATION_MAX_ID + account.getId() + account.getInstance(), null);
                                                        if (lastNotif == null || notification.getId().compareTo(lastNotif) >= 1) {
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

                                                        Helper.notify_user(getApplicationContext(), account,intent, resource, finalNotifType, finalTitle, "@" + account.getAcct() + "@" + account.getInstance());
                                                        String lastNotif = sharedpreferences.getString(Helper.LAST_NOTIFICATION_MAX_ID + account.getId() + account.getInstance(), null);
                                                        if (lastNotif == null || notification.getId().compareTo(lastNotif) >= 1) {
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
                    if( canSendBroadCast) {
                        if (account != null)
                            b.putString("userIdService", account.getId());
                        Intent intentBC = new Intent(Helper.RECEIVE_DATA);
                        intentBC.putExtra("eventStreaming", event);
                        intentBC.putExtras(b);
                        b.putParcelable("data", notification);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentBC);
                    }
                    break;
                case "delete":
                    event = Helper.EventStreaming.DELETE;
                    try {
                        dataId = response.getString("id");
                        b.putString("dataId", dataId);
                    } catch (JSONException ignored) { }
                    break;
            }
        } catch (Exception ignored) { }
    }

    @Override
    public void networkAvailable() {
        startStream();
    }

    @Override
    public void networkUnavailable() {
        Iterator it = threads.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (pair.getValue() == null || !((Thread) pair.getValue()).isAlive()) {
                if ((pair.getValue()) != null)
                    ((Thread) pair.getValue()).interrupt();
            }
            it.remove();
        }
    }
}
