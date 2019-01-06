package fr.gouv.etalab.mastodon.fragments;
/* Copyright 2018 Thomas Schneider
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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.helper.Helper;



/**
 * Created by Thomas on 06/01/2019.
 * Fragment for peertube settings
 */
public class SettingsPeertubeFragment extends Fragment {


    private Context context;
    private int count1;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_peertube_settings, container, false);
        context = getContext();
        assert context != null;
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);

        int videoMode = sharedpreferences.getInt(Helper.SET_VIDEO_MODE, Helper.VIDEO_MODE_TORRENT);


        //Video mode
        final Spinner video_mode_spinner = rootView.findViewById(R.id.set_video_mode);
        ArrayAdapter<CharSequence> video_mode_spinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.settings_video_mode, android.R.layout.simple_spinner_item);
        video_mode_spinner.setAdapter(video_mode_spinnerAdapter);

        video_mode_spinner.setSelection(videoMode);
        video_mode_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if( count1 > 0 ) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    switch (position) {
                        case 0:
                            editor.putInt(Helper.SET_VIDEO_MODE, Helper.VIDEO_MODE_TORRENT);
                            editor.apply();
                            break;
                        case 1:
                            editor.putInt(Helper.SET_VIDEO_MODE, Helper.VIDEO_MODE_WEBVIEW);
                            editor.apply();
                            break;
                        case 2:
                            editor.putInt(Helper.SET_VIDEO_MODE, Helper.VIDEO_MODE_DIRECT);
                            editor.apply();
                            break;
                    }
                }
                count1++;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return rootView;
    }



    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }


}
