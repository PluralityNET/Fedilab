package app.fedilab.android.drawers;
/* Copyright 2019 Thomas Schneider
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.fedilab.android.R;
import app.fedilab.android.activities.PeertubeRegisterActivity;
import app.fedilab.android.client.Entities.InstanceReg;
import app.fedilab.android.helper.Helper;


/**
 * Created by Thomas on 04/11/2019.
 * Adapter to display instances
 */

public class InstancePeertubeRegAdapter extends RecyclerView.Adapter {
    private Context context;
    private List<InstanceReg> instanceRegs;
    private LayoutInflater layoutInflater;

    public InstancePeertubeRegAdapter(Context context, List<InstanceReg> instanceRegs) {
        this.context = context;
        this.instanceRegs = instanceRegs;
        this.layoutInflater = LayoutInflater.from(this.context);
    }

    public int getCount() {
        return instanceRegs.size();
    }

    public InstanceReg getItem(int position) {
        return instanceRegs.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.drawer_instance_reg_peertube, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        InstanceReg instanceReg = instanceRegs.get(position);

        final InstancePeertubeRegAdapter.ViewHolder holder = (InstancePeertubeRegAdapter.ViewHolder) viewHolder;

        holder.instance_choose.setOnClickListener(v -> {
            ((PeertubeRegisterActivity) context).pickupInstance(instanceReg.getDomain());
        });

        holder.instance_count_user.setText(context.getString(R.string.users, Helper.withSuffix(instanceReg.getTotal_users())));
        holder.instance_description.setText(instanceReg.getDescription());
        holder.instance_host.setText(instanceReg.getDomain());
        holder.instance_version.setText(String.format("%s - %s (%s)", instanceReg.getCategory(), instanceReg.getVersion(), instanceReg.getCountry()));
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return instanceRegs.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        TextView instance_host, instance_version, instance_description, instance_count_user;
        ImageButton instance_choose;

        public ViewHolder(View itemView) {
            super(itemView);
            instance_host = itemView.findViewById(R.id.instance_host);
            instance_version = itemView.findViewById(R.id.instance_version);
            instance_description = itemView.findViewById(R.id.instance_description);
            instance_count_user = itemView.findViewById(R.id.instance_count_user);
            instance_choose = itemView.findViewById(R.id.instance_choose);
        }
    }
}