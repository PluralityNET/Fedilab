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
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.ContextCompat;

import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.HashMap;

import app.fedilab.android.client.API;
import app.fedilab.android.client.Entities.InstanceSocial;
import app.fedilab.android.client.HttpsConnection;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.R;


/**
 * Created by Thomas on 24/11/2017.
 * Instance health activity class
 */

public class InstanceHealthActivity extends BaseActivity {

    private InstanceSocial instanceSocial;
    private TextView name, values, checked_at, up, uptime;
    private String instance;
    private LinearLayout instance_container;
    private ImageView back_ground_image;
    private RelativeLayout loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if (theme == Helper.THEME_LIGHT) {
            setTheme(R.style.Dialog);
        } else {
            setTheme(R.style.DialogDark);
        }
        setContentView(R.layout.activity_instance_social);
        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Bundle b = getIntent().getExtras();
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        instance = Helper.getLiveInstance(getApplicationContext());
        if (b != null)
            instance = b.getString("instance", Helper.getLiveInstance(getApplicationContext()));

        Button close = findViewById(R.id.close);
        name = findViewById(R.id.name);
        values = findViewById(R.id.values);
        checked_at = findViewById(R.id.checked_at);
        up = findViewById(R.id.up);
        uptime = findViewById(R.id.uptime);
        instance_container = findViewById(R.id.instance_container);
        loader = findViewById(R.id.loader);
        back_ground_image = findViewById(R.id.back_ground_image);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TextView ref_instance = findViewById(R.id.ref_instance);
        SpannableString content = new SpannableString(ref_instance.getText().toString());
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        ref_instance.setText(content);
        ref_instance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://instances.social"));
                startActivity(browserIntent);
            }
        });

        checkInstance();
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


    private void checkInstance() {

        if (instance == null)
            return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HashMap<String, String> parameters = new HashMap<>();
                    parameters.put("name", instance.trim());
                    final String response = new HttpsConnection(InstanceHealthActivity.this, instance).get("https://instances.social/api/1.0/instances/show", 30, parameters, Helper.THEKINRAR_SECRET_TOKEN);
                    if (response != null)
                        instanceSocial = API.parseInstanceSocialResponse(getApplicationContext(), new JSONObject(response));
                    runOnUiThread(new Runnable() {
                        @SuppressLint({"SetTextI18n", "DefaultLocale"})
                        public void run() {
                            if (instanceSocial.getThumbnail() != null && !instanceSocial.getThumbnail().equals("null"))
                                Glide.with(getApplicationContext())
                                        .asBitmap()
                                        .load(instanceSocial.getThumbnail())
                                        .into(back_ground_image);
                            name.setText(instanceSocial.getName());
                            if (instanceSocial.isUp()) {
                                up.setText("Is up!");
                                up.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.green_1));
                            } else {
                                up.setText("Is down!");
                                up.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red_1));
                            }
                            uptime.setText(String.format("Uptime: %.2f %%", (instanceSocial.getUptime() * 100)));
                            if (instanceSocial.getChecked_at() != null)
                                checked_at.setText(String.format("Checked at: %s", Helper.dateToString(instanceSocial.getChecked_at())));
                            values.setText(String.format("version: %s \n %s users - %s statuses", instanceSocial.getVersion(), Helper.withSuffix(instanceSocial.getUsers()), Helper.withSuffix(instanceSocial.getStatuses())));
                            instance_container.setVisibility(View.VISIBLE);
                            loader.setVisibility(View.GONE);
                        }
                    });

                } catch (Exception ignored) {
                }
            }
        }).start();
    }


}
