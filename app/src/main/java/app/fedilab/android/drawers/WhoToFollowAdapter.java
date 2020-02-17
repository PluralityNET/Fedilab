package app.fedilab.android.drawers;
/* Copyright 2018 Thomas Schneider
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


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.List;

import app.fedilab.android.R;
import app.fedilab.android.activities.WhoToFollowActivity;
import app.fedilab.android.helper.Helper;


/**
 * Created by Thomas on 10/09/2018.
 * Adapter for who to follow list
 */
public class WhoToFollowAdapter extends BaseAdapter {

    private List<String> lists;
    private Context context;

    public WhoToFollowAdapter(List<String> lists) {
        this.lists = lists;
    }

    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public Object getItem(int position) {
        return lists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        String item = lists.get(position);
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.drawer_search, parent, false);
            holder = new ViewHolder();
            holder.search_title = convertView.findViewById(R.id.search_keyword);
            holder.search_container = convertView.findViewById(R.id.search_container);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Helper.changeDrawableColor(context, R.drawable.ic_keyboard_arrow_right, R.attr.menuIconColor);
        Drawable next = ContextCompat.getDrawable(context, R.drawable.ic_keyboard_arrow_right);
        holder.search_title.setText(item);
        assert next != null;
        final float scale = context.getResources().getDisplayMetrics().density;
        next.setBounds(0, 0, (int) (30 * scale + 0.5f), (int) (30 * scale + 0.5f));
        holder.search_title.setCompoundDrawables(null, null, next, null);

        holder.search_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, WhoToFollowActivity.class);
                Bundle b = new Bundle();
                b.putString("item", item);
                intent.putExtras(b);
                context.startActivity(intent);
            }
        });

        return convertView;
    }


    private class ViewHolder {
        LinearLayout search_container;
        TextView search_title;
    }


}