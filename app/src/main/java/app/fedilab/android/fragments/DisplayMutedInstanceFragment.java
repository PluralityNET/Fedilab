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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.asynctasks.PostActionAsyncTask;
import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.drawers.DomainsListAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnPostActionInterface;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.asynctasks.RetrieveDomainsAsyncTask;
import app.fedilab.android.interfaces.OnRetrieveDomainsInterface;


/**
 * Created by Thomas on 26/09/2018.
 * Fragment to display muted instances
 */
public class DisplayMutedInstanceFragment extends Fragment implements OnRetrieveDomainsInterface, OnPostActionInterface {

    private boolean flag_loading;
    private Context context;
    private AsyncTask<Void, Void, Void> asyncTask;
    private DomainsListAdapter domainsListAdapter;
    private String max_id;
    private List<String> domains;
    private RelativeLayout mainLoader, nextElementLoader, textviewNoAction;
    private boolean firstLoad;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean swiped;
    private RecyclerView lv_domains;
    private FloatingActionButton add_new;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_muted_instances, container, false);

        context = getContext();
        Bundle bundle = this.getArguments();
        domains = new ArrayList<>();
        max_id = null;
        firstLoad = true;
        flag_loading = true;
        swiped = false;

        swipeRefreshLayout = rootView.findViewById(R.id.swipeContainer);
        int c1 = getResources().getColor(R.color.cyanea_accent);
        int c2 = getResources().getColor(R.color.cyanea_primary_dark);
        int c3 = getResources().getColor(R.color.cyanea_primary);
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(c3);
        swipeRefreshLayout.setColorSchemeColors(
                c1, c2, c1
        );
        lv_domains = rootView.findViewById(R.id.lv_domains);
        lv_domains.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        mainLoader = rootView.findViewById(R.id.loader);
        nextElementLoader = rootView.findViewById(R.id.loading_next_domains);
        textviewNoAction = rootView.findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);
        domainsListAdapter = new DomainsListAdapter(this.domains, textviewNoAction);
        lv_domains.setAdapter(domainsListAdapter);

        final LinearLayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(context);
        lv_domains.setLayoutManager(mLayoutManager);
        lv_domains.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    int visibleItemCount = mLayoutManager.getChildCount();
                    int totalItemCount = mLayoutManager.getItemCount();
                    int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                    if (firstVisibleItem + visibleItemCount == totalItemCount) {
                        if (!flag_loading) {
                            flag_loading = true;
                            asyncTask = new RetrieveDomainsAsyncTask(context, max_id, DisplayMutedInstanceFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            nextElementLoader.setVisibility(View.VISIBLE);
                        }
                    } else {
                        nextElementLoader.setVisibility(View.GONE);
                    }
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                max_id = null;
                domains = new ArrayList<>();
                firstLoad = true;
                flag_loading = true;
                swiped = true;
                asyncTask = new RetrieveDomainsAsyncTask(context, max_id, DisplayMutedInstanceFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });


        asyncTask = new RetrieveDomainsAsyncTask(context, max_id, DisplayMutedInstanceFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        try {
            add_new = ((MainActivity) context).findViewById(R.id.add_new);
        } catch (Exception ignored) {
        }
        if (add_new != null)
            add_new.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                    int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
                    int style;
                    if (theme == Helper.THEME_DARK) {
                        style = R.style.DialogDark;
                    } else if (theme == Helper.THEME_BLACK) {
                        style = R.style.DialogBlack;
                    } else {
                        style = R.style.Dialog;
                    }
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context, style);
                    LayoutInflater inflater = getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.add_blocked_instance, new LinearLayout(context), false);
                    dialogBuilder.setView(dialogView);

                    EditText add_domain = dialogView.findViewById(R.id.add_domain);
                    dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            if (add_domain.getText() != null && add_domain.getText().toString().trim().matches("^[\\da-zA-Z.-]+\\.[a-zA-Z.]{2,10}$")) {
                                new PostActionAsyncTask(context, API.StatusAction.BLOCK_DOMAIN, add_domain.getText().toString().trim(), DisplayMutedInstanceFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                dialog.dismiss();
                            } else {
                                Toasty.error(context, context.getString(R.string.toast_empty_content)).show();
                            }
                        }
                    });
                    dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = dialogBuilder.create();
                    alertDialog.setTitle(getString(R.string.block_domain));
                    if (alertDialog.getWindow() != null)
                        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    alertDialog.show();
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

    public void onDestroy() {
        super.onDestroy();
        if (asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING)
            asyncTask.cancel(true);
    }

    public void scrollToTop() {
        if (lv_domains != null)
            lv_domains.setAdapter(domainsListAdapter);
    }

    @Override
    public void onRetrieveDomains(APIResponse apiResponse) {
        mainLoader.setVisibility(View.GONE);
        nextElementLoader.setVisibility(View.GONE);
        if (apiResponse.getError() != null) {
            if(apiResponse.getError().getError().length() < 100) {
                Toasty.error(context, apiResponse.getError().getError(), Toast.LENGTH_LONG).show();
            }else{
                Toasty.error(context, getString(R.string.long_api_error,"\ud83d\ude05"), Toast.LENGTH_LONG).show();
            }
            swipeRefreshLayout.setRefreshing(false);
            swiped = false;
            flag_loading = false;
            return;
        }
        flag_loading = (apiResponse.getMax_id() == null);
        List<String> domains = apiResponse.getDomains();
        if (!swiped && firstLoad && (domains == null || domains.size() == 0))
            textviewNoAction.setVisibility(View.VISIBLE);
        else
            textviewNoAction.setVisibility(View.GONE);
        max_id = apiResponse.getMax_id();
        if (swiped) {
            domainsListAdapter = new DomainsListAdapter(this.domains, textviewNoAction);
            lv_domains.setAdapter(domainsListAdapter);
            swiped = false;
        }
        if (domains != null && domains.size() > 0) {
            this.domains.addAll(domains);
            domainsListAdapter.notifyDataSetChanged();
        }
        swipeRefreshLayout.setRefreshing(false);
        firstLoad = false;
    }

    @Override
    public void onRetrieveDomainsDeleted(int response) {

    }


    @Override
    public void onPostAction(int statusCode, API.StatusAction statusAction, String userId, Error error) {
        if (error != null) {
            Toasty.error(context, error.getError(), Toast.LENGTH_LONG).show();
            return;
        }
        Helper.manageMessageStatusCode(context, statusCode, statusAction);
        this.domains.add(0, userId);
        domainsListAdapter.notifyItemInserted(0);
    }
}
