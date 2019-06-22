package app.fedilab.android.activities;
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import app.fedilab.android.R;
import app.fedilab.android.asynctasks.PostAdminActionAsyncTask;
import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.AccountAdmin;
import app.fedilab.android.client.Entities.AdminAction;
import app.fedilab.android.client.Entities.Report;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.drawers.StatusReportAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnAdminActionInterface;
import es.dmoral.toasty.Toasty;

import static app.fedilab.android.client.API.adminAction.DISABLE;
import static app.fedilab.android.client.API.adminAction.NONE;
import static app.fedilab.android.client.API.adminAction.SILENCE;
import static app.fedilab.android.client.API.adminAction.SUSPEND;

public class AccountReportActivity extends BaseActivity implements OnAdminActionInterface {

    TextView permissions, username, email, email_status, login_status, joined, recent_ip;
    Button warn, disable, silence, suspend;
    private String account_id;
    private CheckBox email_user;
    private EditText comment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);

        setTheme(R.style.AppAdminTheme);
        Report report = null;
        AccountAdmin targeted_account = null;
        Bundle b = getIntent().getExtras();
        if (b != null) {
            account_id = b.getString("account_id", null);
            targeted_account = b.getParcelable("targeted_account");
            report =  b.getParcelable("report");
        }

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.simple_bar, null);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            ImageView toolbar_close = actionBar.getCustomView().findViewById(R.id.toolbar_close);
            TextView toolbar_title = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            toolbar_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            toolbar_title.setText(String.format(getString(R.string.administration) + " %s", Helper.getLiveInstance(getApplicationContext())));
        }
        setContentView(R.layout.activity_admin_report);

        warn = findViewById(R.id.warn);
        disable = findViewById(R.id.disable);
        silence = findViewById(R.id.silence);
        suspend = findViewById(R.id.suspend);

        permissions = findViewById(R.id.permissions);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        email_status = findViewById(R.id.email_status);
        login_status = findViewById(R.id.login_status);
        joined = findViewById(R.id.joined);
        recent_ip = findViewById(R.id.recent_ip);

        email_user = findViewById(R.id.email_user);
        comment = findViewById(R.id.comment);

        if( account_id == null && report == null && targeted_account == null){
            Toasty.error(getApplicationContext(), getString(R.string.toast_error), Toast.LENGTH_LONG).show();
            finish();
        }

        if( account_id != null){
            new PostAdminActionAsyncTask(getApplicationContext(), API.adminAction.GET_ONE_ACCOUNT, account_id, null, AccountReportActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return;
        }
        if( report != null) {
            targeted_account = report.getTarget_account();
            RecyclerView lv_statuses = findViewById(R.id.lv_statuses);

            ArrayList<String> contents = new ArrayList<>();
            for(Status status: report.getStatuses()){
                contents.add(status.getContent());
            }
            lv_statuses.setLayoutManager(new LinearLayoutManager(this));
            StatusReportAdapter adapter = new StatusReportAdapter(this, contents);
            lv_statuses.setAdapter(adapter);

            Group statuses_group = findViewById(R.id.statuses_group);
            statuses_group.setVisibility(View.VISIBLE);
        }
        account_id = targeted_account.getId();
        fillReport(targeted_account);

    }

    @Override
    public void onAdminAction(APIResponse apiResponse) {
        if( apiResponse.getError() != null){
            if( apiResponse.getError().getStatusCode() == 403){
                AlertDialog.Builder builderInner;
                builderInner = new AlertDialog.Builder(AccountReportActivity.this, R.style.AdminDialog);
                builderInner.setTitle(R.string.reconnect_account);
                builderInner.setMessage(R.string.reconnect_account_message);
                builderInner.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        dialog.dismiss();
                    }
                });
                builderInner.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        Intent intent = new Intent(AccountReportActivity.this, LoginActivity.class);
                        intent.putExtra("admin", true);
                        startActivity(intent);
                    }
                });
                builderInner.show();
            }else{
                Toasty.error(AccountReportActivity.this, apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            }
            return;
        }
        if( apiResponse.getAccountAdmins() != null && apiResponse.getAccountAdmins().size() > 0) {
            fillReport(apiResponse.getAccountAdmins().get(0));
        }
    }

    private void fillReport(AccountAdmin accountAdmin){

        if( accountAdmin == null){
            Toasty.error(getApplicationContext(), getString(R.string.toast_error), Toast.LENGTH_LONG).show();
            return;
        }

        warn.setOnClickListener(view->{
            AdminAction adminAction = new AdminAction();
            adminAction.setType(NONE);
            adminAction.setSend_email_notification(email_user.isChecked());
            adminAction.setText(comment.getText().toString().trim());
            new PostAdminActionAsyncTask(getApplicationContext(), NONE, account_id, adminAction, AccountReportActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        });


        if( !accountAdmin.isSilenced() ) {
            silence.setText(getString(R.string.silence));
        }else{
            silence.setText(getString(R.string.unsilence));
        }
        silence.setOnClickListener(view->{
            if( !accountAdmin.isSilenced() ) {
                AdminAction adminAction = new AdminAction();
                adminAction.setType(SILENCE);
                adminAction.setSend_email_notification(email_user.isChecked());
                adminAction.setText(comment.getText().toString().trim());
                new PostAdminActionAsyncTask(getApplicationContext(), SILENCE, account_id, adminAction, AccountReportActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }else{
                new PostAdminActionAsyncTask(getApplicationContext(), API.adminAction.UNSILENCE, account_id, null, AccountReportActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        if( !accountAdmin.isDisabled() ) {
            disable.setText(getString(R.string.disable));
        }else{
            disable.setText(getString(R.string.undisable));
        }
        disable.setOnClickListener(view->{
            if( !accountAdmin.isSilenced()) {
                AdminAction adminAction = new AdminAction();
                adminAction.setType(DISABLE);
                adminAction.setSend_email_notification(email_user.isChecked());
                adminAction.setText(comment.getText().toString().trim());
                new PostAdminActionAsyncTask(getApplicationContext(), DISABLE, account_id, adminAction, AccountReportActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }else{
                new PostAdminActionAsyncTask(getApplicationContext(), API.adminAction.UNSUSPEND, account_id, null, AccountReportActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        if( !accountAdmin.isSuspended() ) {
            suspend.setText(getString(R.string.suspend));
        }else{
            suspend.setText(getString(R.string.unsuspend));
        }
        suspend.setOnClickListener(view->{
            if( !accountAdmin.isSuspended() ){
                AdminAction adminAction = new AdminAction();
                adminAction.setType(SUSPEND);
                adminAction.setSend_email_notification(email_user.isChecked());
                adminAction.setText(comment.getText().toString().trim());
                new PostAdminActionAsyncTask(getApplicationContext(), SUSPEND, account_id, adminAction, AccountReportActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }else{
                new PostAdminActionAsyncTask(getApplicationContext(), API.adminAction.UNSUSPEND, account_id, null, AccountReportActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        if( accountAdmin.getAction() != null) {
            String message = null;
            switch (accountAdmin.getAction()) {
                case SILENCE:
                    message = getString(R.string.account_silenced);
                    break;
                case UNSILENCE:
                    message = getString(R.string.account_unsilenced);
                    break;
                case DISABLE:
                    message = getString(R.string.account_disabled);
                    break;
                case UNDISABLE:
                    message = getString(R.string.account_undisabled);
                    break;
                case SUSPEND:
                    message = getString(R.string.account_suspended);
                    break;
                case UNSUSPEND:
                    message = getString(R.string.account_unsuspended);
                    break;
                case NONE:
                    message = getString(R.string.account_warned);
                    break;
            }
            if( message != null){
                Toasty.success(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
            comment.setText("");
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(comment.getWindowToken(), 0);
        }
        if( accountAdmin.getRole() == null) {
            return;
        }

        switch (accountAdmin.getRole()) {
            case "user":
                permissions.setText(getString(R.string.user));
                break;
            case "mod":
                permissions.setText(getString(R.string.moderator));
                break;
            case "admin":
                permissions.setText(getString(R.string.administrator));
                break;
        }


        username.setText(String.format("@%s", accountAdmin.getAccount().getAcct()));

        email.setText(accountAdmin.getEmail());
        email_status.setText(accountAdmin.isConfirmed()?getString(R.string.confirmed):getString(R.string.unconfirmed));

        if( accountAdmin.isDisabled()){
            login_status.setText(getString(R.string.disabled));
        }else if( accountAdmin.isSilenced()){
            login_status.setText(getString(R.string.silenced));
        }else if( accountAdmin.isSuspended()){
            login_status.setText(getString(R.string.suspended));
        }else{
            login_status.setText(getString(R.string.active));
        }
        if( accountAdmin.getDomain() == null || accountAdmin.getDomain().equals("null")){
            warn.setVisibility(View.VISIBLE);
            email_user.setVisibility(View.VISIBLE);
            comment.setVisibility(View.VISIBLE);
            recent_ip.setText(accountAdmin.getIp());
            disable.setVisibility(View.VISIBLE);
            suspend.setVisibility(View.VISIBLE);
        }else{
            warn.setVisibility(View.GONE);
            email_user.setVisibility(View.GONE);
            email_user.setChecked(false);
            comment.setVisibility(View.GONE);
            recent_ip.setText("-");
            permissions.setText("-");
            email.setText("-");
            disable.setVisibility(View.GONE);
            suspend.setVisibility(View.VISIBLE);
        }
        if( accountAdmin.getRole().equals("admin") || accountAdmin.getRole().equals("mod")){
            warn.setVisibility(View.GONE);
            suspend.setVisibility(View.GONE);
            silence.setVisibility(View.GONE);
            disable.setVisibility(View.GONE);
            email_user.setVisibility(View.GONE);
            email_user.setChecked(false);
            comment.setVisibility(View.GONE);
        }
        joined.setText(Helper.dateToString(accountAdmin.getCreated_at()));


    }
}
