package app.fedilab.android.fragments;
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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import app.fedilab.android.client.Entities.TrunkAccount;
import app.fedilab.android.drawers.WhoToFollowAdapter;
import app.fedilab.android.helper.Helper;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.asynctasks.WhoToFollowAsyncTask;
import app.fedilab.android.interfaces.OnRetrieveWhoToFollowInterface;


/**
 * Created by Thomas on 10/09/2018.
 * Fragment to display who to follow list
 */
public class WhoToFollowFragment extends Fragment implements OnRetrieveWhoToFollowInterface {


    private Context context;
    private View rootView;
    private RelativeLayout mainLoader;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_who_to_follow, container, false);
        context = getContext();
        mainLoader = rootView.findViewById(R.id.loader);
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String lastDateListRefresh = sharedpreferences.getString(Helper.LAST_DATE_LIST_REFRESH, null);
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, -15);
        Date dateAllowed = cal.getTime();
        if (lastDateListRefresh == null || Helper.stringToDate(context, lastDateListRefresh).before(dateAllowed)) {
            mainLoader.setVisibility(View.VISIBLE);
            new WhoToFollowAsyncTask(context, null, WhoToFollowFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            String lastList = sharedpreferences.getString(Helper.LAST_LIST, null);
            List<String> list = Helper.restoreArrayFromString(lastList);
            displayResults(list);
        }

        return rootView;
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.context = context;
    }


    private void displayResults(List<String> list) {

        mainLoader.setVisibility(View.GONE);
        if (list != null) {
            ListView lv_list = rootView.findViewById(R.id.lv_list);
            WhoToFollowAdapter whoToFollowAdapter = new WhoToFollowAdapter(list);
            lv_list.setAdapter(whoToFollowAdapter);
        } else {
            Toasty.error(context, context.getString(R.string.toast_error), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRetrieveWhoToFollowList(List<String> list) {
        if (list != null) {
            SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Helper.LAST_DATE_LIST_REFRESH, Helper.dateToString(new Date()));
            editor.putString(Helper.LAST_LIST, Helper.arrayToStringStorage(list));
            editor.apply();
        }
        displayResults(list);
    }

    @Override
    public void onRetrieveWhoToFollowAccount(List<TrunkAccount> trunkAccounts) {
    }
}
