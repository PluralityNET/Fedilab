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
package app.fedilab.android.asynctasks;


import android.content.Context;
import android.os.AsyncTask;

import java.io.ByteArrayInputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.interfaces.OnUpdateCredentialInterface;

/**
 * Created by Thomas on 05/06/2017.
 * Update account credential
 */

public class UpdateCredentialAsyncTask extends AsyncTask<Void, Void, Void> {

    private String display_name, note, avatarName, headerName;
    private boolean senstive;
    private ByteArrayInputStream avatar, header;
    private API.accountPrivacy privacy;
    private APIResponse apiResponse;
    private OnUpdateCredentialInterface listener;
    private WeakReference<Context> contextReference;
    private HashMap<String, String> customFields;

    public UpdateCredentialAsyncTask(Context context, HashMap<String, String> customFields, String display_name, String note, ByteArrayInputStream avatar, String avatarName, ByteArrayInputStream header, String headerName, API.accountPrivacy privacy, boolean senstive, OnUpdateCredentialInterface onUpdateCredentialInterface){
        this.contextReference = new WeakReference<>(context);
        this.display_name = display_name;
        this.note = note;
        this.avatar = avatar;
        this.header = header;
        this.listener = onUpdateCredentialInterface;
        this.privacy = privacy;
        this.avatarName = avatarName;
        this.headerName = headerName;
        this.customFields = customFields;
        this.senstive = senstive;
    }

    @Override
    protected Void doInBackground(Void... params) {
        apiResponse = new API(this.contextReference.get()).updateCredential(display_name, note, avatar, avatarName, header, headerName, privacy, customFields, senstive);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onUpdateCredential(apiResponse);
    }

}