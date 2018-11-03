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
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.DeleteDomainsAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveDomainsInterface;

import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 26/09/2018.
 * Adapter for domains
 */
public class DomainsListAdapter extends RecyclerView.Adapter implements OnRetrieveDomainsInterface {

    private List<String> domains;
    private LayoutInflater layoutInflater;
    private Context context;
    private DomainsListAdapter domainsListAdapter;
    private RelativeLayout textviewNoAction;

    public DomainsListAdapter(Context context, List<String> domains, RelativeLayout textviewNoAction){
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.domains = domains;
        this.domainsListAdapter = this;
        this.textviewNoAction = textviewNoAction;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.drawer_domain, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        final DomainsListAdapter.ViewHolder holder = (DomainsListAdapter.ViewHolder) viewHolder;
        final String domain = domains.get(position);
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_DARK){
            changeDrawableColor(context, holder.domain_delete, R.color.dark_text);
        }else{
            changeDrawableColor(context, holder.domain_delete, R.color.black);
        }
        holder.domain_name.setText(domain);
        holder.domain_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
                int style;
                if (theme == Helper.THEME_DARK) {
                    style = R.style.DialogDark;
                } else if (theme == Helper.THEME_BLACK){
                    style = R.style.DialogBlack;
                }else {
                    style = R.style.Dialog;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(context, style);
                builder.setMessage(context.getString(R.string.unblock_domain_confirm_message, domain));
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.unblock_domain_confirm)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                domains.remove(domain);
                                domainsListAdapter.notifyItemRemoved(holder.getAdapterPosition());
                                new DeleteDomainsAsyncTask(context, domain, DomainsListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                if( domains.size() == 0 && textviewNoAction != null && textviewNoAction.getVisibility() == View.GONE)
                                    textviewNoAction.setVisibility(View.VISIBLE);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });


    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return domains.size();
    }



    @Override
    public void onRetrieveDomains(APIResponse apiResponse) {

    }

    @Override
    public void onRetrieveDomainsDeleted(int response) {
        String message;
        if( response == 200){
            message = context.getString(R.string.toast_unblock_domain);
        }else{
            message = context.getString(R.string.toast_error);
        }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    private class ViewHolder extends RecyclerView.ViewHolder{
        TextView domain_name;
        ImageView domain_delete;

        ViewHolder(View itemView) {
            super(itemView);
            domain_name = itemView.findViewById(R.id.domain_name);
            domain_delete = itemView.findViewById(R.id.domain_delete);
        }
    }

}