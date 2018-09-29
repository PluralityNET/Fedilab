package fr.gouv.etalab.mastodon.drawers;
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
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.activities.ListActivity;
import fr.gouv.etalab.mastodon.activities.WebviewActivity;
import fr.gouv.etalab.mastodon.asynctasks.ManageListsAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.HowToVideo;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnListActionInterface;

import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 29/09/2018.
 * Adapter for how to videos
 */
public class HowToVideosAdapter extends BaseAdapter implements OnListActionInterface {

    private List<HowToVideo> howToVideos;
    private LayoutInflater layoutInflater;
    private Context context;

    public HowToVideosAdapter(Context context, List<HowToVideo> howToVideos){
        this.howToVideos = howToVideos;
        layoutInflater = LayoutInflater.from(context);
        this.context = context;

    }

    @Override
    public int getCount() {
        return howToVideos.size();
    }

    @Override
    public Object getItem(int position) {
        return howToVideos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final HowToVideo howToVideo = howToVideos.get(position);
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.drawer_how_to_videos, parent, false);
            holder = new ViewHolder();
            holder.how_to_description = convertView.findViewById(R.id.how_to_description);
            holder.how_to_image = convertView.findViewById(R.id.how_to_image);
            holder.how_to_container = convertView.findViewById(R.id.how_to_container);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);

        if( theme == Helper.THEME_LIGHT){
            holder.how_to_container.setBackgroundResource(R.color.mastodonC3__);
            changeDrawableColor(context, R.drawable.ic_keyboard_arrow_right,R.color.black);
        }else if(theme == Helper.THEME_DARK){
            holder.how_to_container.setBackgroundResource(R.color.mastodonC1_);
            changeDrawableColor(context, R.drawable.ic_keyboard_arrow_right,R.color.dark_text);
        }else if(theme == Helper.THEME_BLACK) {
            holder.how_to_container.setBackgroundResource(R.color.black_2);
            changeDrawableColor(context, R.drawable.ic_keyboard_arrow_right,R.color.dark_text);
        }
        Drawable next = ContextCompat.getDrawable(context, R.drawable.ic_keyboard_arrow_right);
        holder.how_to_description.setText(howToVideo.getDescription());
        assert next != null;
        final float scale = context.getResources().getDisplayMetrics().density;
        next.setBounds(0,0,(int) (30  * scale + 0.5f),(int) (30  * scale + 0.5f));
        holder.how_to_description.setCompoundDrawables(null, null, next, null);
        Glide.with(holder.how_to_image.getContext())
                .load(howToVideo.getThumbnailPath())
                .into(holder.how_to_image);
        holder.how_to_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, WebviewActivity.class);
                Bundle b = new Bundle();
                String finalUrl = "https://peertube.fr/" + howToVideo.getEmbedPath();
                b.putString("url", finalUrl);
                intent.putExtras(b);
                context.startActivity(intent);
            }
        });


        return convertView;
    }

    @Override
    public void onActionDone(ManageListsAsyncTask.action actionType, APIResponse apiResponse, int statusCode) {

    }


    private class ViewHolder {
        LinearLayout how_to_container;
        ImageView how_to_image;
        TextView how_to_description;
    }


}