package fr.gouv.etalab.mastodon.fragments;
/* Copyright 2017 Thomas Schneider
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
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Peertube;
import fr.gouv.etalab.mastodon.drawers.PeertubeAdapter;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveFeedsInterface;
import fr.gouv.etalab.mastodon.sqlite.PeertubeFavoritesDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.StatusCacheDAO;


/**
 * Created by Thomas on 21/10/2018.
 * Fragment to display bookmarks for Peertube
 */
public class DisplayFavoritesPeertubeFragment extends Fragment implements OnRetrieveFeedsInterface {


    private Context context;
    private List<Peertube> peertubes;
    private PeertubeAdapter peertubeAdapter;
    private RelativeLayout textviewNoAction;
    private RelativeLayout mainLoader;
    private RecyclerView lv_status;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_peertube_favourites, container, false);
        context = getContext();

         lv_status = rootView.findViewById(R.id.lv_status);

        mainLoader = rootView.findViewById(R.id.loader);
        textviewNoAction = rootView.findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        return rootView;
    }


    @Override
    public void onCreate(Bundle saveInstance)
    {
        super.onCreate(saveInstance);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onResume() {
        super.onResume();
        new RetrieveFeedsAsyncTask(context, RetrieveFeedsAsyncTask.Type.CACHE_BOOKMARKS_PEERTUBE, null, DisplayFavoritesPeertubeFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onRetrieveFeeds(APIResponse apiResponse) {

        final SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        mainLoader.setVisibility(View.GONE);
        FloatingActionButton delete_all = null;
        try {
            delete_all = ((MainActivity) context).findViewById(R.id.delete_all);
        }catch (Exception ignored){}
        peertubes = apiResponse.getPeertubes();
        if( peertubes != null && peertubes.size() > 0) {
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(context);
            peertubeAdapter = new PeertubeAdapter(context, peertubes.get(0).getInstance(), this.peertubes);
            lv_status.setAdapter(peertubeAdapter);
            lv_status.setLayoutManager(mLayoutManager);
            textviewNoAction.setVisibility(View.GONE);
            lv_status.setVisibility(View.VISIBLE);
        }else {
            textviewNoAction.setVisibility(View.VISIBLE);
            lv_status.setVisibility(View.GONE);
        }

        if( delete_all != null)
            delete_all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(R.string.delete_all);
                    builder.setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogConfirm, int which) {
                                    new PeertubeFavoritesDAO(context, db).removeAll();
                                    peertubes = new ArrayList<>();
                                    peertubes.clear();
                                    peertubeAdapter = new PeertubeAdapter(context,null, peertubes);
                                    lv_status.setAdapter(peertubeAdapter);
                                    peertubeAdapter.notifyDataSetChanged();
                                    textviewNoAction.setVisibility(View.VISIBLE);
                                    dialogConfirm.dismiss();
                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogConfirm, int which) {
                                    dialogConfirm.dismiss();
                                }
                            })
                            .show();
                }
            });
    }
}
