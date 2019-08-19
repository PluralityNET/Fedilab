package app.fedilab.android.fragments;
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.client.APIResponse;
import app.fedilab.android.drawers.ListAdapter;
import app.fedilab.android.helper.Helper;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.activities.ListActivity;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.asynctasks.ManageListsAsyncTask;
import app.fedilab.android.interfaces.OnListActionInterface;


/**
 * Created by Thomas on 13/12/2017.
 * Fragment to display Lists
 */
public class DisplayListsFragment extends Fragment implements OnListActionInterface {


    private Context context;
    private AsyncTask<Void, Void, Void> asyncTask;
    private List<app.fedilab.android.client.Entities.List> lists;
    private RelativeLayout mainLoader;
    private FloatingActionButton add_new;
    private ListAdapter listAdapter;
    private RelativeLayout textviewNoAction;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //View for fragment is the same that fragment accounts
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);

        context = getContext();
        lists = new ArrayList<>();


        ListView lv_lists = rootView.findViewById(R.id.lv_lists);
        textviewNoAction = rootView.findViewById(R.id.no_action);
        mainLoader = rootView.findViewById(R.id.loader);
        RelativeLayout nextElementLoader = rootView.findViewById(R.id.loading_next_items);
        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);
        lists = new ArrayList<>();
        listAdapter = new ListAdapter(lists, textviewNoAction);
        lv_lists.setAdapter(listAdapter);
        asyncTask = new ManageListsAsyncTask(context, ManageListsAsyncTask.action.GET_LIST, null, null, null, null, DisplayListsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        try {
            add_new = ((MainActivity) context).findViewById(R.id.add_new);
        }catch (Exception ignored){}
        if( add_new != null)
        add_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
                int style;
                if (theme == Helper.THEME_DARK) {
                    style = R.style.DialogDark;
                } else if (theme == Helper.THEME_BLACK){
                    style = R.style.DialogBlack;
                }else {
                    style = R.style.Dialog;
                }
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context, style);
                LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.add_list,   new LinearLayout(context), false);
                dialogBuilder.setView(dialogView);
                final EditText editText = dialogView.findViewById(R.id.add_list);
                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(255)});
                dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if( editText.getText() != null && editText.getText().toString().trim().length() > 0 )
                            new ManageListsAsyncTask(context, ManageListsAsyncTask.action.CREATE_LIST, null, null, null, editText.getText().toString().trim(), DisplayListsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        dialog.dismiss();
                        add_new.setEnabled(false);
                    }
                });
                dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });


                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.setTitle(getString(R.string.action_lists_create));
                alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        //Hide keyboard
                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        assert imm != null;
                        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    }
                });
                if( alertDialog.getWindow() != null )
                    alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                alertDialog.show();
            }
        });
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

    public void onDestroy() {
        super.onDestroy();
        if(asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING)
            asyncTask.cancel(true);
    }


    @Override
    public void onActionDone(ManageListsAsyncTask.action actionType, APIResponse apiResponse, int statusCode) {
        mainLoader.setVisibility(View.GONE);
        add_new.setEnabled(true);
        if( apiResponse.getError() != null){
            Toasty.error(context, apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            return;
        }
        if( actionType == ManageListsAsyncTask.action.GET_LIST) {
            if (apiResponse.getLists() != null && apiResponse.getLists().size() > 0) {
                this.lists.addAll(apiResponse.getLists());
                listAdapter.notifyDataSetChanged();
                textviewNoAction.setVisibility(View.GONE);
            } else {
                textviewNoAction.setVisibility(View.VISIBLE);
            }
        }else if( actionType == ManageListsAsyncTask.action.CREATE_LIST){
            if (apiResponse.getLists() != null && apiResponse.getLists().size() > 0) {
                String listId = apiResponse.getLists().get(0).getId();
                String title = apiResponse.getLists().get(0).getTitle();
                Intent intent = new Intent(context, ListActivity.class);
                Bundle b = new Bundle();
                b.putString("id", listId);
                b.putString("title", title);
                intent.putExtras(b);
                context.startActivity(intent);
                this.lists.add(0, apiResponse.getLists().get(0));
                listAdapter.notifyDataSetChanged();
                textviewNoAction.setVisibility(View.GONE);
            }else{
                Toasty.error(context, apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            }
        }else if( actionType == ManageListsAsyncTask.action.DELETE_LIST){
            if( this.lists.size() == 0)
                textviewNoAction.setVisibility(View.VISIBLE);
        }
    }
}
