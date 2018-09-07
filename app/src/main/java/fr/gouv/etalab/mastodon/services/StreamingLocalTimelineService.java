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
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.client.TLSSocketFactory;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;


/**
 * Created by Thomas on 29/09/2017.
 * Manage service for streaming api for local timeline
 */

public class StreamingLocalTimelineService extends IntentService {


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    @SuppressWarnings("unused")
    public StreamingLocalTimelineService(String name) {
        super(name);
    }
    @SuppressWarnings("unused")
    public StreamingLocalTimelineService() {
        super("StreamingLocalTimelineService");
    }

    private static HttpsURLConnection httpsURLConnection;
    protected Account account;

    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean display_local = sharedpreferences.getBoolean(Helper.SET_DISPLAY_LOCAL, true);
        if( !display_local){
            stopSelf();
        }
        SharedPreferences.Editor editor = sharedpreferences.edit();
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(getApplicationContext()));
        editor.putBoolean(Helper.SHOULD_CONTINUE_STREAMING_LOCAL + userId + instance, true);
        editor.apply();
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        InputStream inputStream;
        BufferedReader reader = null;
        Account accountStream = null;
        if( userId != null) {
            SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            accountStream = new AccountDAO(getApplicationContext(), db).getAccountByID(userId);
        }
        if( accountStream != null){
            try {
                if(!Helper.isConnectedToInternet(StreamingLocalTimelineService.this, accountStream.getInstance()))
                    return;
                URL url = new URL("https://" + accountStream.getInstance() + "/api/v1/streaming/public/local");
                httpsURLConnection = (HttpsURLConnection) url.openConnection();
                httpsURLConnection.setRequestProperty("Content-Type", "application/json");
                httpsURLConnection.setRequestProperty("Authorization", "Bearer " + accountStream.getToken());
                httpsURLConnection.setRequestProperty("Connection", "Keep-Alive");
                httpsURLConnection.setRequestProperty("Keep-Alive", "header");
                httpsURLConnection.setRequestProperty("Connection", "close");
                httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory());
                httpsURLConnection.setRequestMethod("GET");
                httpsURLConnection.setConnectTimeout(70000);
                httpsURLConnection.setReadTimeout(70000);
                inputStream = new BufferedInputStream(httpsURLConnection.getInputStream());
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String event;
                while((event = reader.readLine()) != null) {
                    if (!sharedpreferences.getBoolean(Helper.SHOULD_CONTINUE_STREAMING_LOCAL + accountStream.getId() + accountStream.getInstance(), true)) {
                        stopSelf();
                        return;
                    }
                    if (!event.startsWith("data: ")) {
                        continue;
                    }
                    event = event.substring(6);
                    if( event.matches("^[0-9]{1,}$"))
                        continue;
                    try {
                        JSONObject eventJson = new JSONObject(event);
                        onRetrieveStreaming(accountStream, eventJson);
                    } catch (JSONException ignored) {}
                }
            } catch (Exception ignored) {
            }finally {
                if(reader != null){
                    try{
                        reader.close();
                    }catch (IOException ignored){}
                }
                if( sharedpreferences.getBoolean(Helper.SHOULD_CONTINUE_STREAMING_LOCAL + accountStream.getId() + accountStream.getInstance(), true)) {
                    SystemClock.sleep(1000);
                    Intent streamingLocalTimelineService = new Intent(this, StreamingLocalTimelineService.class);
                    try {
                        startService(streamingLocalTimelineService);
                    }catch (Exception ignored){}
                }
            }
        }
    }

    public void onRetrieveStreaming(Account account, JSONObject response) {
        if(  response == null )
            return;
        Status status ;
        Bundle b = new Bundle();
        status = API.parseStatuses(getApplicationContext(), response);
        status.setReplies(new ArrayList<Status>());
        status.setNew(true);
        b.putParcelable("data", status);
        if( account != null)
            b.putString("userIdService",account.getId());
        Intent intentBC = new Intent(Helper.RECEIVE_LOCAL_DATA);
        intentBC.putExtras(b);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentBC);
    }

}
