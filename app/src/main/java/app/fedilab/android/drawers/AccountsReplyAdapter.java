package app.fedilab.android.drawers;
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


import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.R;
import app.fedilab.android.activities.TootActivity;


/**
 * Created by Thomas on 25/10/2017.
 * Adapter for accounts when replying
 */
public class AccountsReplyAdapter extends BaseAdapter{

    private List<Account> accounts;
    private LayoutInflater layoutInflater;
    private boolean[] checked;
    private Context context;

    public AccountsReplyAdapter(Context context, List<Account> accounts, boolean[] checked){
        this.accounts = accounts;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.checked = checked;
    }

    public AccountsReplyAdapter(Context context, List<Account> accounts, List<Boolean> checked){
        this.accounts = accounts;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.checked = new boolean[checked.size()];
        int index = 0;
        for (Boolean val : checked) {
            this.checked[index++] = val;
        }
    }

    @Override
    public int getCount() {
        return accounts.size();
    }

    @Override
    public Account getItem(int position) {
        return accounts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }



    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

        final Account account = accounts.get(position);
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.drawer_account_reply, parent, false);
            holder = new ViewHolder();
            holder.account_pp = convertView.findViewById(R.id.account_pp);
            holder.account_dn = convertView.findViewById(R.id.account_dn);
            holder.checkbox = convertView.findViewById(R.id.checkbox);

            holder.account_container = convertView.findViewById(R.id.account_container);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                try {
                    ((TootActivity) context).changeAccountReply(isChecked, "@"+account.getAcct());
                    checked[position] = isChecked;
                }catch (Exception ignored){}
            }
        });
        holder.checkbox.setChecked(checked[position]);
        holder.account_dn.setText(String.format("@%s", account.getAcct()));
        holder.account_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.checkbox.performClick();
            }
        });
        //Profile picture
        Glide.with(holder.account_pp.getContext())
                .load(account.getAvatar())
                .into(holder.account_pp);
        return convertView;
    }




    private class ViewHolder {
        ImageView account_pp;
        TextView account_dn;
        CheckBox checkbox;
        LinearLayout account_container;
    }


}