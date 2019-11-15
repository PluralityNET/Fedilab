package app.fedilab.android.drawers;
/*
 * Copyright (C) 2015 Paul Burke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import app.fedilab.android.R;
import app.fedilab.android.activities.ReorderTimelinesActivity;
import app.fedilab.android.client.Entities.ManageTimelines;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.helper.itemtouchhelper.ItemTouchHelperAdapter;
import app.fedilab.android.helper.itemtouchhelper.ItemTouchHelperViewHolder;
import app.fedilab.android.helper.itemtouchhelper.OnStartDragListener;
import app.fedilab.android.helper.itemtouchhelper.OnUndoListener;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.TimelinesDAO;
import es.dmoral.toasty.Toasty;


/**
 * Simple RecyclerView.Adapter that implements {@link ItemTouchHelperAdapter} to respond to move and
 * dismiss events from a {@link androidx.recyclerview.widget.ItemTouchHelper}.
 *
 * @author Paul Burke (ipaulpro)
 */
public class ReorderTabAdapter extends RecyclerView.Adapter<ReorderTabAdapter.ItemViewHolder> implements ItemTouchHelperAdapter {

    private final OnStartDragListener mDragStartListener;
    private final OnUndoListener mUndoListener;
    private List<ManageTimelines> mItems;
    private Context context;
    private SharedPreferences sharedpreferences;

    public ReorderTabAdapter(List<ManageTimelines> manageTimelines, OnStartDragListener dragStartListener, OnUndoListener undoListener) {
        this.mDragStartListener = dragStartListener;
        this.mUndoListener = undoListener;
        this.mItems = manageTimelines;
    }

    @NotNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_reorder, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull final ItemViewHolder holder, int position) {


        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        ManageTimelines tl = mItems.get(position);
        switch (tl.getType()) {
            case HOME:
                holder.iconView.setImageResource(R.drawable.ic_home);
                holder.textView.setText(context.getString(R.string.home_menu));
                break;
            case NOTIFICATION:
                holder.iconView.setImageResource(R.drawable.ic_notifications);
                holder.textView.setText(context.getString(R.string.notifications));
                break;
            case DIRECT:
                holder.iconView.setImageResource(R.drawable.ic_direct_messages);
                holder.textView.setText(context.getString(R.string.direct_message));
                break;
            case LOCAL:
                holder.iconView.setImageResource(R.drawable.ic_people);
                holder.textView.setText(context.getString(R.string.local_menu));
                break;
            case PUBLIC:
                holder.iconView.setImageResource(R.drawable.ic_public);
                holder.textView.setText(context.getString(R.string.global_menu));
                break;
            case ART:
                holder.iconView.setImageResource(R.drawable.ic_color_lens);
                holder.textView.setText(context.getString(R.string.art_menu));
                break;
            case PEERTUBE:
                holder.iconView.setImageResource(R.drawable.ic_video_peertube);
                holder.textView.setText(context.getString(R.string.peertube_menu));
                break;
            case INSTANCE:
                switch (tl.getRemoteInstance().getType()) {
                    case "PEERTUBE":
                        holder.iconView.setImageResource(R.drawable.peertube_icon);
                        break;
                    case "MASTODON":
                        holder.iconView.setImageResource(R.drawable.mastodon_icon_item);
                        break;
                    case "PIXELFED":
                        holder.iconView.setImageResource(R.drawable.pixelfed);
                        break;
                    case "MISSKEY":
                        holder.iconView.setImageResource(R.drawable.misskey);
                        break;
                    case "GNU":
                        holder.iconView.setImageResource(R.drawable.ic_gnu_social);
                        break;
                }
                holder.textView.setText(tl.getRemoteInstance().getHost());
                break;
            case TAG:
                holder.iconView.setImageResource(R.drawable.ic_tag_timeline);
                if (tl.getTagTimeline().getDisplayname() != null)
                    holder.textView.setText(tl.getTagTimeline().getDisplayname());
                else
                    holder.textView.setText(tl.getTagTimeline().getName());
                break;
            case LIST:
                holder.iconView.setImageResource(R.drawable.ic_list);
                holder.textView.setText(tl.getListTimeline().getTitle());
                break;
        }
        if (tl.getType() != ManageTimelines.Type.INSTANCE) {
            if (theme == Helper.THEME_LIGHT) {
                holder.iconView.setColorFilter(ContextCompat.getColor(context, R.color.action_light_header), PorterDuff.Mode.SRC_IN);
            } else {
                holder.iconView.setColorFilter(ContextCompat.getColor(context, R.color.dark_text), PorterDuff.Mode.SRC_IN);
            }
        } else {
            holder.iconView.setColorFilter(null);
        }

        if (theme == Helper.THEME_LIGHT) {
            holder.handleView.setColorFilter(ContextCompat.getColor(context, R.color.action_light_header), PorterDuff.Mode.SRC_IN);
            holder.hideView.setColorFilter(ContextCompat.getColor(context, R.color.action_light_header), PorterDuff.Mode.SRC_IN);
        } else {
            holder.handleView.setColorFilter(ContextCompat.getColor(context, R.color.dark_text), PorterDuff.Mode.SRC_IN);
            holder.hideView.setColorFilter(ContextCompat.getColor(context, R.color.dark_text), PorterDuff.Mode.SRC_IN);
        }

        if (tl.isDisplayed()) {
            holder.hideView.setImageResource(R.drawable.ic_make_tab_visible);
        } else {
            holder.hideView.setImageResource(R.drawable.ic_make_tab_unvisible);
        }

        holder.hideView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                int count = new TimelinesDAO(context, db).countVisibleTimelines();
                if (count > 2 || !tl.isDisplayed()) {
                    tl.setDisplayed(!tl.isDisplayed());
                    if (tl.isDisplayed()) {
                        holder.hideView.setImageResource(R.drawable.ic_make_tab_visible);
                    } else {
                        holder.hideView.setImageResource(R.drawable.ic_make_tab_unvisible);
                    }
                    ReorderTimelinesActivity.updated = true;
                    new TimelinesDAO(context, db).update(tl);
                } else {
                    Toasty.info(context, context.getString(R.string.visible_tabs_needed), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Start a drag whenever the handle view it touched
        holder.handleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public void onItemDismiss(int position) {
        ManageTimelines item = mItems.get(position);
        if (item.getType() == ManageTimelines.Type.TAG || item.getType() == ManageTimelines.Type.INSTANCE || item.getType() == ManageTimelines.Type.LIST) {
            mUndoListener.onUndo(item, position);
            mItems.remove(position);
            notifyItemRemoved(position);
        } else {
            notifyItemChanged(position);
            Toasty.info(context, context.getString(R.string.warning_main_timeline), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mItems, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        int i = 0;
        for (ManageTimelines timelines : mItems) {
            timelines.setPosition(i);
            new TimelinesDAO(context, db).update(timelines);
            i++;
        }
        ReorderTimelinesActivity.updated = true;
        return true;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    /**
     * Simple example of a view holder that implements {@link ItemTouchHelperViewHolder} and has a
     * "handle" view that initiates a drag event when touched.
     */
    public class ItemViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder {

        final TextView textView;
        final ImageView handleView;
        final ImageView hideView;
        final ImageView iconView;

        ItemViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text);
            handleView = itemView.findViewById(R.id.handle);
            iconView = itemView.findViewById(R.id.icon);
            hideView = itemView.findViewById(R.id.hide);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.mastodonC3));
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }
}
