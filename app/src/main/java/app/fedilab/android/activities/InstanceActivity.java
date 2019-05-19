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
package app.fedilab.android.activities;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Instance;
import app.fedilab.android.helper.Helper;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.asynctasks.RetrieveInstanceAsyncTask;
import app.fedilab.android.interfaces.OnRetrieveInstanceInterface;

import static app.fedilab.android.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 05/06/2017.
 * Instance activity
 */

public class InstanceActivity extends BaseActivity implements OnRetrieveInstanceInterface {

    private LinearLayout instance_container;
    private RelativeLayout loader;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme){
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme);
                break;
            case Helper.THEME_DARK:
                setTheme(R.style.AppThemeDark);
                break;
            case Helper.THEME_BLACK:
                setTheme(R.style.AppThemeBlack);
                break;
            default:
                setTheme(R.style.AppThemeDark);
        }

        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.simple_bar, null);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            ImageView toolbar_close = actionBar.getCustomView().findViewById(R.id.toolbar_close);
            TextView toolbar_title = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            toolbar_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            toolbar_title.setText(R.string.action_about_instance);
            if (theme == Helper.THEME_LIGHT){
                Toolbar toolbar = actionBar.getCustomView().findViewById(R.id.toolbar);
                Helper.colorizeToolbar(toolbar, R.color.black, InstanceActivity.this);
            }
        }
        setContentView(R.layout.activity_instance);
        Helper.changeDrawableColor(getApplicationContext(), R.drawable.ic_mail_outline,R.color.white);
        instance_container = findViewById(R.id.instance_container);
        loader = findViewById(R.id.loader);
        instance_container.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);
        setTitle(getString(R.string.action_about_instance));
        new RetrieveInstanceAsyncTask(getApplicationContext(), InstanceActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onRetrieveInstance(APIResponse apiResponse) {
        instance_container.setVisibility(View.VISIBLE);
        loader.setVisibility(View.GONE);
        if( apiResponse.getError() != null){
            Toasty.error(getApplicationContext(), getString(R.string.toast_error), Toast.LENGTH_LONG).show();
            return;
        }
        final Instance instance = apiResponse.getInstance();
        TextView instance_title = findViewById(R.id.instance_title);
        TextView instance_description = findViewById(R.id.instance_description);
        TextView instance_version = findViewById(R.id.instance_version);
        TextView instance_uri = findViewById(R.id.instance_uri);
        FloatingActionButton instance_contact = findViewById(R.id.instance_contact);

        instance_title.setText(instance.getTitle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            instance_description.setText(Html.fromHtml(instance.getDescription(), Html.FROM_HTML_MODE_LEGACY));
        else
            //noinspection deprecation
            instance_description.setText(Html.fromHtml(instance.getDescription()));
        if( instance.getDescription() == null || instance.getDescription().trim().length() == 0 )
            instance_description.setText(getString(R.string.instance_no_description));
        instance_version.setText(instance.getVersion());
        instance_uri.setText(instance.getUri());
        if( instance.getEmail() == null){
            instance_contact.hide();
        }

        instance_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",instance.getEmail(), null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "[Mastodon] - " + instance.getUri());
                startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email)));
            }
        });
    }
}
