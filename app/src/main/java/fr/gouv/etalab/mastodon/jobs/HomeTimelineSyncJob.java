package fr.gouv.etalab.mastodon.jobs;
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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;


import static fr.gouv.etalab.mastodon.helper.Helper.HOME_TIMELINE_INTENT;
import static fr.gouv.etalab.mastodon.helper.Helper.INTENT_ACTION;
import static fr.gouv.etalab.mastodon.helper.Helper.PREF_INSTANCE;
import static fr.gouv.etalab.mastodon.helper.Helper.PREF_KEY_ID;
import static fr.gouv.etalab.mastodon.helper.Helper.canNotify;
import static fr.gouv.etalab.mastodon.helper.Helper.notify_user;


/**
 * Created by Thomas on 20/05/2017.
 * Notifications for home timeline job
 */

public class HomeTimelineSyncJob extends Job {

    static final String HOME_TIMELINE = "home_timeline";
    static {
        Helper.installProvider();
    }

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        callAsynchronousTask();
        return Result.SUCCESS;
    }
    public static int schedule(boolean updateCurrent){

        Set<JobRequest> jobRequests = JobManager.instance().getAllJobRequestsForTag(HOME_TIMELINE);
        if (!jobRequests.isEmpty() && !updateCurrent) {
            return jobRequests.iterator().next().getJobId();
        }

        return new JobRequest.Builder(HomeTimelineSyncJob.HOME_TIMELINE)
                .setPeriodic(TimeUnit.MINUTES.toMillis(Helper.MINUTES_BETWEEN_HOME_TIMELINE), TimeUnit.MINUTES.toMillis(5))
                .setUpdateCurrent(updateCurrent)
                .setRequiredNetworkType(JobRequest.NetworkType.METERED)
                .setRequiresBatteryNotLow(true)
                .setRequirementsEnforced(false)
                .build()
                .schedule();
    }


    /**
     * Task in background starts here.
     */
    private void callAsynchronousTask() {

        if( !canNotify(getContext()))
            return;
        final SharedPreferences sharedpreferences = getContext().getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean notif_hometimeline = sharedpreferences.getBoolean(Helper.SET_NOTIF_HOMETIMELINE, false);
        //User disagree with home timeline refresh
        if( !notif_hometimeline)
            return; //Nothing is done
        //No account connected, the service is stopped
        if(!Helper.isLoggedIn(getContext()))
            return;
        SQLiteDatabase db = Sqlite.getInstance(getContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        //If an Internet connection and user agrees with notification refresh
        //If WIFI only and on WIFI OR user defined any connections to use the service.
        if(!sharedpreferences.getBoolean(Helper.SET_WIFI_ONLY, false) || Helper.isOnWIFI(getContext())) {
            List<Account> accounts = new AccountDAO(getContext(),db).getAllAccount();
            //It means there is no user in DB.
            if( accounts == null )
                return;
            //Retrieve users in db that owner has.
            for (Account account: accounts) {
                String max_id = sharedpreferences.getString(Helper.LAST_HOMETIMELINE_NOTIFICATION_MAX_ID + account.getId() + account.getInstance(), null);
                String lastHomeSeen = sharedpreferences.getString(Helper.LAST_HOMETIMELINE_MAX_ID + account.getId() + account.getInstance(), null);
                if( lastHomeSeen != null && max_id != null){
                    if( Long.parseLong(lastHomeSeen) > Long.parseLong(max_id)){
                        max_id = lastHomeSeen;
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString(Helper.LAST_HOMETIMELINE_NOTIFICATION_MAX_ID + account.getId() + account.getInstance(), max_id);
                        editor.apply();
                    }
                }
                API api = new API(getContext(), account.getInstance(), account.getToken());
                APIResponse apiResponse = api.getHomeTimelineSinceId(max_id);
                onRetrieveHomeTimelineService(apiResponse, account);
            }
        }
    }

    private void onRetrieveHomeTimelineService(APIResponse apiResponse, final Account account) {
        final List<Status> statuses = apiResponse.getStatuses();
        if( apiResponse.getError() != null || statuses == null || statuses.size() == 0 || account == null)
            return;

        final SharedPreferences sharedpreferences = getContext().getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);

        final String max_id = sharedpreferences.getString(Helper.LAST_HOMETIMELINE_NOTIFICATION_MAX_ID + account.getId() + account.getInstance(), null);
        //No previous notifications in cache, so no notification will be sent
        String message;

        for(Status status: statuses){
            //The notification associated to max_id is discarded as it is supposed to have already been sent
            //Also, if the toot comes from the owner, we will avoid to warn him/her...
            if( max_id != null && (status.getId().equals(max_id)) || (account.getAcct() != null && status.getAccount().getAcct().trim().equals(account.getAcct().trim()) ))
                continue;
            final String notificationUrl = status.getAccount().getAvatar();

            if(statuses.size() > 0 )
                message = getContext().getResources().getQuantityString(R.plurals.other_notif_hometimeline, statuses.size(), statuses.size());
            else
                message = "";
            final Intent intent = new Intent(getContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
            intent.putExtra(INTENT_ACTION, HOME_TIMELINE_INTENT);
            intent.putExtra(PREF_KEY_ID, account.getId());
            intent.putExtra(PREF_INSTANCE, account.getInstance());
            long notif_id = Long.parseLong(account.getId());
            final int notificationId = ((notif_id + 2) > 2147483647) ? (int) (2147483647 - notif_id - 2) : (int) (notif_id + 2);
            if( notificationUrl != null){

                final String finalMessage = message;
                String title;
                if( status.getAccount().getDisplay_name() != null && status.getAccount().getDisplay_name().length() > 0 )
                    title = getContext().getResources().getString(R.string.notif_pouet, Helper.shortnameToUnicode(status.getAccount().getDisplay_name(), true));
                else
                    title = getContext().getResources().getString(R.string.notif_pouet, status.getAccount().getUsername());
                final String finalTitle = title;

                Handler mainHandler = new Handler(Looper.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {Glide.with(getContext())
                            .asBitmap()
                            .load(notificationUrl)
                            .listener(new RequestListener<Bitmap>(){

                                @Override
                                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                                    notify_user(getContext(), intent, notificationId, BitmapFactory.decodeResource(getContext().getResources(),
                                            R.drawable.mastodonlogo), Helper.NotifType.TOOT, finalTitle, finalMessage);
                                    SharedPreferences.Editor editor = sharedpreferences.edit();
                                    editor.putString(Helper.LAST_HOMETIMELINE_NOTIFICATION_MAX_ID + account.getId() + account.getInstance(), statuses.get(0).getId());
                                    editor.apply();
                                    return false;
                                }
                            })
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                                    notify_user(getContext(), intent, notificationId, resource, Helper.NotifType.TOOT, finalTitle, finalMessage);
                                    SharedPreferences.Editor editor = sharedpreferences.edit();
                                    editor.putString(Helper.LAST_HOMETIMELINE_NOTIFICATION_MAX_ID + account.getId() + account.getInstance(), statuses.get(0).getId());
                                    editor.apply();
                                }
                            });
                    }
                };
                mainHandler.post(myRunnable);

            }
        }

    }

}