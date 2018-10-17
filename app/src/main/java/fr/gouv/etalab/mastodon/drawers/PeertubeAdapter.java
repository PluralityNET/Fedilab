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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.activities.PeertubeActivity;
import fr.gouv.etalab.mastodon.asynctasks.ManageListsAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Peertube;
import fr.gouv.etalab.mastodon.helper.CrossActions;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnListActionInterface;



/**
 * Created by Thomas on 06/10/2018.
 * Adapter for peertube
 */
public class PeertubeAdapter extends RecyclerView.Adapter implements OnListActionInterface {

    private List<Peertube> peertubes;
    private LayoutInflater layoutInflater;
    private Context context;
    private String instance;

    public PeertubeAdapter(Context context, String instance, List<Peertube> peertubes){
        this.peertubes = peertubes;
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.instance = instance;

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PeertubeAdapter.ViewHolder(layoutInflater.inflate(R.layout.drawer_peertube, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {


        final PeertubeAdapter.ViewHolder holder = (PeertubeAdapter.ViewHolder) viewHolder;
        final Peertube peertube = peertubes.get(position);

        Account account = peertube.getAccount();

        holder.peertube_account_name.setText(account.getAcct());
        holder.peertube_title.setText(peertube.getName());
        holder.peertube_duration.setText(context.getString(R.string.duration_video, Helper.secondsToString(peertube.getDuration())));
        holder.peertube_date.setText(String.format(" - %s", Helper.dateDiff(context, peertube.getCreated_at())));
        holder.peertube_views.setText(context.getString(R.string.number_view_video, Helper.withSuffix(peertube.getView())));

        holder.peertube_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CrossActions.doCrossProfile(context, account);
            }
        });
        Glide.with(holder.peertube_video_image.getContext())
                .load("https://" + peertube.getInstance() + peertube.getThumbnailPath())
                .into(holder.peertube_video_image);
        Helper.loadGiF(context, account.getAvatar(), holder.peertube_profile);
        holder.how_to_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PeertubeActivity.class);
                Bundle b = new Bundle();
                String finalUrl = "https://"  + instance + peertube.getEmbedPath();
                Pattern link = Pattern.compile("(https?:\\/\\/[\\da-z\\.-]+\\.[a-z\\.]{2,10})\\/videos\\/embed\\/(\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12})$");
                Matcher matcherLink = link.matcher(finalUrl);
                if( matcherLink.find()) {
                    String url = matcherLink.group(1) + "/videos/watch/" + matcherLink.group(2);
                    b.putString("peertubeLinkToFetch", url);
                    b.putString("peertube_instance", matcherLink.group(1).replace("https://","").replace("http://",""));
                    b.putString("video_id", matcherLink.group(2));
                }
                intent.putExtras(b);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return peertubes.size();
    }


    @Override
    public void onActionDone(ManageListsAsyncTask.action actionType, APIResponse apiResponse, int statusCode) {

    }

    class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout how_to_container;
        ImageView peertube_profile, peertube_video_image;
        TextView peertube_account_name, peertube_views, peertube_duration;
        TextView peertube_title, peertube_date;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            peertube_account_name = itemView.findViewById(R.id.peertube_account_name);
            peertube_title = itemView.findViewById(R.id.peertube_title);
            peertube_video_image = itemView.findViewById(R.id.peertube_video_image);
            peertube_profile = itemView.findViewById(R.id.peertube_profile);
            how_to_container = itemView.findViewById(R.id.how_to_container);
            peertube_date = itemView.findViewById(R.id.peertube_date);
            peertube_views = itemView.findViewById(R.id.peertube_views);
            peertube_duration = itemView.findViewById(R.id.peertube_duration);
        }
    }




}