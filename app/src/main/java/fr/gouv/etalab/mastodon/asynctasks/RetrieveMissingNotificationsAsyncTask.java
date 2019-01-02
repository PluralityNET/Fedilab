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
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.List;

import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Notification;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveMissingNotificationsInterface;


/**
 * Created by Thomas on 27/09/2017.
 * Retrieves missing notifications since last pause
 */

public class RetrieveMissingNotificationsAsyncTask extends AsyncTask<Void, Void, Void> {


    private String since_id;
    private OnRetrieveMissingNotificationsInterface listener;
    private WeakReference<Context> contextReference;
    private List<Notification> notifications;

    public RetrieveMissingNotificationsAsyncTask(Context context, String since_id, OnRetrieveMissingNotificationsInterface onRetrieveMissingNotifications){
        this.contextReference = new WeakReference<>(context);
        this.since_id = since_id;
        this.listener = onRetrieveMissingNotifications;
    }


    @Override
    protected Void doInBackground(Void... params) {
        API api = new API(this.contextReference.get());
        APIResponse apiResponse = api.getNotificationsSince(since_id, 40, false);
        since_id = apiResponse.getSince_id();
        notifications = apiResponse.getNotifications();
        if( notifications != null && notifications.size() > 0) {
            MainActivity.lastNotificationId = notifications.get(0).getId();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveMissingNotifications(notifications);
    }
}
