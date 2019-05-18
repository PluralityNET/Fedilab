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
import android.os.Build;
import android.text.Html;
import android.util.Patterns;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.fedilab.android.client.HttpsConnection;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnRetrieveMetaDataInterface;


/**
 * Created by Thomas on 02/09/2017.
 * Retrieves metadata of a remote page
 */

public class RetrieveMetaDataAsyncTask extends AsyncTask<Void, Void, Void> {

    private OnRetrieveMetaDataInterface listener;
    private String url;
    private boolean error = false;
    private String image, title, description, sharedSubject, sharedText;
    private WeakReference<Context> contextWeakReference;
    private boolean shouldFetchMetaData = true;
    public RetrieveMetaDataAsyncTask(Context context,boolean shouldFetchMetaData, String sharedSubject, String sharedText,String url, OnRetrieveMetaDataInterface onRetrieveRemoteAccountInterface){
        this.url = url;
        this.listener = onRetrieveRemoteAccountInterface;
        this.sharedText = sharedText;
        this.sharedSubject = sharedSubject;
        this.contextWeakReference = new WeakReference<>(context);
        this.shouldFetchMetaData = shouldFetchMetaData;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if(shouldFetchMetaData)
            return execRetrieveMetaDataInBackground();
        else
            return null;
    }
    private Void execRetrieveMetaDataInBackground(){

        String potentialUrl = "";
        if (url == null) {
            error = true;
            return null;
        }
        try {
            Matcher matcher;
            if (url.startsWith("www."))
                url = "http://" + url;

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
                matcher = Patterns.WEB_URL.matcher(url);
            else
                matcher = Helper.urlPattern.matcher(url);
            while (matcher.find()){
                int matchStart = matcher.start(1);
                int matchEnd = matcher.end();
                if(matchStart < matchEnd && url.length() >= matchEnd)
                    potentialUrl = url.substring(matchStart, matchEnd);
            }
            // If we actually have a URL then make use of it.
            if (potentialUrl.length() > 0) {
                Pattern titlePattern = Pattern.compile("meta[ a-zA-Z=\"'-]+property=[\"']og:title[\"']\\s+content=[\"']([^>]*)[\"']");
                Pattern descriptionPattern = Pattern.compile("meta[ a-zA-Z=\"'-]+property=[\"']og:description[\"']\\s+content=[\"']([^>]*)[\"']");
                Pattern imagePattern = Pattern.compile("meta[ a-zA-Z=\"'-]+property=[\"']og:image[\"']\\s+content=[\"']([^>]*)[\"']");
                try {
                    String response = new HttpsConnection(this.contextWeakReference.get()).get(potentialUrl);
                    Matcher matcherTitle = titlePattern.matcher(response);
                    Matcher matcherDescription = descriptionPattern.matcher(response);
                    Matcher matcherImage = imagePattern.matcher(response);
                    String titleEncoded = null;
                    String descriptionEncoded = null;
                    while (matcherTitle.find())
                        titleEncoded = matcherTitle.group(1);
                    while (matcherDescription.find())
                        descriptionEncoded = matcherDescription.group(1);
                    while (matcherImage.find())
                        image = matcherImage.group(1);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        if( titleEncoded != null)
                            title = Html.fromHtml(titleEncoded, Html.FROM_HTML_MODE_LEGACY).toString();
                        if( descriptionEncoded != null)
                            description = Html.fromHtml(descriptionEncoded, Html.FROM_HTML_MODE_LEGACY).toString();
                    }else {
                        //noinspection deprecation
                        if( titleEncoded != null)
                            title = Html.fromHtml(titleEncoded).toString();
                        if( descriptionEncoded != null)
                            description = Html.fromHtml(descriptionEncoded).toString();
                    }
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                } catch (HttpsConnection.HttpsConnectionException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException | IndexOutOfBoundsException e) {
            error = true;
        }
        return null;
    }
    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveMetaData(error, sharedSubject, sharedText, image, title, description);
    }

}
