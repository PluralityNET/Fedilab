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
package fr.gouv.etalab.mastodon.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Patterns;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.dmoral.toasty.Toasty;
import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.ManageFiltersAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.ManageListsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveAccountsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveInstanceAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveMetaDataAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrievePeertubeInformationAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveRemoteDataAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.UpdateAccountInfoAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.UpdateAccountInfoByIDAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Filters;
import fr.gouv.etalab.mastodon.client.Entities.Notification;
import fr.gouv.etalab.mastodon.client.Entities.RemoteInstance;
import fr.gouv.etalab.mastodon.client.Entities.Results;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.client.Entities.TagTimeline;
import fr.gouv.etalab.mastodon.client.Entities.Version;
import fr.gouv.etalab.mastodon.client.HttpsConnection;
import fr.gouv.etalab.mastodon.fragments.DisplayAccountsFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayBookmarksFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayDraftsFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayFavoritesPeertubeFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayFiltersFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayFollowRequestSentFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayHowToFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayListsFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayMutedInstanceFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayNotificationsFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayStatusFragment;
import fr.gouv.etalab.mastodon.fragments.SettingsPeertubeFragment;
import fr.gouv.etalab.mastodon.fragments.TabLayoutScheduleFragment;
import fr.gouv.etalab.mastodon.fragments.TabLayoutSettingsFragment;
import fr.gouv.etalab.mastodon.fragments.WhoToFollowFragment;
import fr.gouv.etalab.mastodon.helper.CrossActions;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.helper.MenuFloating;
import fr.gouv.etalab.mastodon.interfaces.OnFilterActionInterface;
import fr.gouv.etalab.mastodon.interfaces.OnListActionInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveEmojiAccountInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveInstanceInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveMetaDataInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveRemoteAccountInterface;
import fr.gouv.etalab.mastodon.interfaces.OnUpdateAccountInfoInterface;
import fr.gouv.etalab.mastodon.services.BackupStatusService;
import fr.gouv.etalab.mastodon.services.LiveNotificationService;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.InstancesDAO;
import fr.gouv.etalab.mastodon.sqlite.SearchDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;

import static fr.gouv.etalab.mastodon.asynctasks.ManageFiltersAsyncTask.action.GET_ALL_FILTER;
import static fr.gouv.etalab.mastodon.helper.Helper.ADD_USER_INTENT;
import static fr.gouv.etalab.mastodon.helper.Helper.BACKUP_INTENT;
import static fr.gouv.etalab.mastodon.helper.Helper.BACK_TO_SETTINGS;
import static fr.gouv.etalab.mastodon.helper.Helper.EXTERNAL_STORAGE_REQUEST_CODE;
import static fr.gouv.etalab.mastodon.helper.Helper.HOME_TIMELINE_INTENT;
import static fr.gouv.etalab.mastodon.helper.Helper.INSTANCE_NAME;
import static fr.gouv.etalab.mastodon.helper.Helper.INTENT_ACTION;
import static fr.gouv.etalab.mastodon.helper.Helper.INTENT_TARGETED_ACCOUNT;
import static fr.gouv.etalab.mastodon.helper.Helper.NOTIFICATION_INTENT;
import static fr.gouv.etalab.mastodon.helper.Helper.PREF_KEY_ID;
import static fr.gouv.etalab.mastodon.helper.Helper.SEARCH_INSTANCE;
import static fr.gouv.etalab.mastodon.helper.Helper.SEARCH_REMOTE;
import static fr.gouv.etalab.mastodon.helper.Helper.SEARCH_URL;
import static fr.gouv.etalab.mastodon.helper.Helper.THEME_BLACK;
import static fr.gouv.etalab.mastodon.helper.Helper.THEME_LIGHT;
import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;
import static fr.gouv.etalab.mastodon.helper.Helper.changeUser;
import static fr.gouv.etalab.mastodon.helper.Helper.menuAccounts;
import static fr.gouv.etalab.mastodon.helper.Helper.unCheckAllMenuItems;
import static fr.gouv.etalab.mastodon.helper.Helper.updateHeaderAccountInfo;


public abstract class BaseMainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnUpdateAccountInfoInterface, OnRetrieveMetaDataInterface, OnRetrieveInstanceInterface, OnRetrieveRemoteAccountInterface, OnRetrieveEmojiAccountInterface, OnFilterActionInterface, OnListActionInterface {

    private FloatingActionButton toot, delete_all, add_new;
    private HashMap<String, String> tagTile = new HashMap<>();
    private HashMap<String, Integer> tagItem = new HashMap<>();
    private TextView toolbarTitle;
    private SearchView toolbar_search;
    private View headerLayout;
    public static String currentLocale;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private RelativeLayout main_app_container;
    private Stack<Integer> stackBack = new Stack<>();
    public static List<Filters> filters = new ArrayList<>();
    private DisplayStatusFragment homeFragment, federatedFragment, localFragment, artFragment;
    private DisplayNotificationsFragment notificationsFragment;
    private static final int ERROR_DIALOG_REQUEST_CODE = 97;
    private static BroadcastReceiver receive_data, receive_home_data, receive_federated_data, receive_local_data;
    private boolean display_direct, display_local, display_global, display_art;
    public static int countNewStatus;
    public static int countNewNotifications;
    private String userIdService;
    public static String lastHomeId = null, lastNotificationId = null;
    boolean notif_follow, notif_add, notif_mention, notif_share, show_boosts, show_replies , show_nsfw;
    String show_filtered;
    private AppBarLayout appBar;
    private String userId;
    private String instance;
    public int countPage;
    private PagerAdapter adapter;
    private String oldSearch;
    boolean isLoadingInstance = false;
    private ImageView delete_instance;
    public static String displayPeertube = null;
    private PopupMenu popup;
    private String instance_id;
    private int style;
    private Activity activity;
    private HashMap<String, Integer> tabPosition = new HashMap<>();
    public static HashMap<Integer, RetrieveFeedsAsyncTask.Type> typePosition = new HashMap<>();
    private FloatingActionButton federatedTimelines;
    public static UpdateAccountInfoAsyncTask.SOCIAL social;
    SparseArray<Fragment> registeredFragments = new SparseArray<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);


        userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String token = sharedpreferences.getString(Helper.PREF_KEY_OAUTH_TOKEN, null);
        instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(getApplicationContext()));
        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        boolean displayFollowInstance = sharedpreferences.getBoolean(Helper.SET_DISPLAY_FOLLOW_INSTANCE, true);
        Account account = new AccountDAO(getApplicationContext(), db).getAccountByToken(token);
        if( account == null){
            Helper.logout(getApplicationContext());
            Intent myIntent = new Intent(BaseMainActivity.this, LoginActivity.class);
            startActivity(myIntent);
            finish();
            return;
        }
        social = (account.getSocial() == null || account.getSocial().equals("MASTODON")? UpdateAccountInfoAsyncTask.SOCIAL.MASTODON: UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE);

        countNewStatus = 0;
        countNewNotifications = 0;

        final int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme){
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme_NoActionBar);
                break;
            case Helper.THEME_DARK:
                setTheme(R.style.AppThemeDark_NoActionBar);
                break;
            case Helper.THEME_BLACK:
                setTheme(R.style.AppThemeBlack_NoActionBar);
                break;
            default:
                setTheme(R.style.AppThemeDark_NoActionBar);
        }
        setContentView(R.layout.activity_main);

        display_direct = sharedpreferences.getBoolean(Helper.SET_DISPLAY_DIRECT, true);
        display_local = sharedpreferences.getBoolean(Helper.SET_DISPLAY_LOCAL, true);
        display_global = sharedpreferences.getBoolean(Helper.SET_DISPLAY_GLOBAL, true);
        display_art = sharedpreferences.getBoolean(Helper.SET_DISPLAY_ART, true);
        //Test if user is still log in
        if( ! Helper.isLoggedIn(getApplicationContext())) {
            //It is not, the user is redirected to the login page
            Intent myIntent = new Intent(BaseMainActivity.this, LoginActivity.class);
            startActivity(myIntent);
            finish();
            return;
        }
        activity = this;
        rateThisApp();

        //Intialize Peertube information
        if( social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE){
            try{
                new RetrievePeertubeInformationAsyncTask(getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }catch (Exception ignored){}
        }

        Helper.canPin = false;
        Helper.fillMapEmoji(getApplicationContext());
        //Here, the user is authenticated
        appBar = findViewById(R.id.appBar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if( theme == THEME_BLACK)
            toolbar.setBackgroundColor(ContextCompat.getColor(BaseMainActivity.this, R.color.black));
        setSupportActionBar(toolbar);
        toolbarTitle  = toolbar.findViewById(R.id.toolbar_title);
        toolbar_search = toolbar.findViewById(R.id.toolbar_search);
        delete_instance = findViewById(R.id.delete_instance);
        if( theme == THEME_LIGHT) {
            ImageView icon = toolbar_search.findViewById(android.support.v7.appcompat.R.id.search_button);
            ImageView close = toolbar_search.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
            if( icon != null)
                icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_icon));
            if( close != null)
                close.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_icon));
            EditText editText = toolbar_search.findViewById(android.support.v7.appcompat.R.id.search_src_text);
            editText.setHintTextColor(getResources().getColor(R.color.dark_icon));
            editText.setTextColor(getResources().getColor(R.color.dark_icon));
            changeDrawableColor(BaseMainActivity.this,delete_instance, R.color.dark_icon);
        }
        tabLayout = findViewById(R.id.tabLayout);

        viewPager = findViewById(R.id.viewpager);


        final NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Helper.hideMenuItem(navigationView.getMenu());


        toot = findViewById(R.id.toot);
        tootShow();
        delete_all = findViewById(R.id.delete_all);
        add_new = findViewById(R.id.add_new);

        if( social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON) {
            TabLayout.Tab tabHome = tabLayout.newTab();
            TabLayout.Tab tabNotif = tabLayout.newTab();
            TabLayout.Tab tabDirect = tabLayout.newTab();
            TabLayout.Tab tabLocal = tabLayout.newTab();
            TabLayout.Tab tabPublic = tabLayout.newTab();
            TabLayout.Tab tabArt = tabLayout.newTab();

            tabHome.setCustomView(R.layout.tab_badge);
            tabNotif.setCustomView(R.layout.tab_badge);
            tabDirect.setCustomView(R.layout.tab_badge);
            tabLocal.setCustomView(R.layout.tab_badge);
            tabPublic.setCustomView(R.layout.tab_badge);
            tabArt.setCustomView(R.layout.tab_badge);

            @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
            ImageView iconHome = tabHome.getCustomView().findViewById(R.id.tab_icon);

            iconHome.setImageResource(R.drawable.ic_home);

            if (theme == THEME_BLACK)
                iconHome.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_icon), PorterDuff.Mode.SRC_IN);
            else
                iconHome.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.mastodonC4), PorterDuff.Mode.SRC_IN);


            @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
            ImageView iconNotif = tabNotif.getCustomView().findViewById(R.id.tab_icon);
            iconNotif.setImageResource(R.drawable.ic_notifications);


            @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
            ImageView iconDirect = tabDirect.getCustomView().findViewById(R.id.tab_icon);
            iconDirect.setImageResource(R.drawable.ic_direct_messages);

            @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
            ImageView iconLocal = tabLocal.getCustomView().findViewById(R.id.tab_icon);
            iconLocal.setImageResource(R.drawable.ic_people);

            @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
            ImageView iconGlobal = tabPublic.getCustomView().findViewById(R.id.tab_icon);
            iconGlobal.setImageResource(R.drawable.ic_public);


            @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
            ImageView iconArt = tabArt.getCustomView().findViewById(R.id.tab_icon);
            iconArt.setImageResource(R.drawable.ic_color_lens);

            iconHome.setContentDescription(getString(R.string.home_menu));
            iconNotif.setContentDescription(getString(R.string.notifications));
            iconDirect.setContentDescription(getString(R.string.direct_message));
            iconLocal.setContentDescription(getString(R.string.local_menu));
            iconGlobal.setContentDescription(getString(R.string.global_menu));
            iconArt.setContentDescription(getString(R.string.art_menu));

            if (theme == THEME_LIGHT) {
                iconHome.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.action_light_header), PorterDuff.Mode.SRC_IN);
                iconNotif.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.action_light_header), PorterDuff.Mode.SRC_IN);
                iconDirect.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.action_light_header), PorterDuff.Mode.SRC_IN);
                iconLocal.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.action_light_header), PorterDuff.Mode.SRC_IN);
                iconGlobal.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.action_light_header), PorterDuff.Mode.SRC_IN);
                iconArt.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.action_light_header), PorterDuff.Mode.SRC_IN);
            } else {
                iconHome.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
                iconNotif.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
                iconDirect.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
                iconLocal.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
                iconGlobal.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
                iconArt.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
            }



            tabLayout.addTab(tabHome);
            tabLayout.addTab(tabNotif);
            tabPosition.put("home",0);
            typePosition.put(0, RetrieveFeedsAsyncTask.Type.HOME);
            tabPosition.put("notifications",1);
            typePosition.put(1, RetrieveFeedsAsyncTask.Type.NOTIFICATION);
            int i = 2;
            if( display_direct) {
                tabLayout.addTab(tabDirect);
                tabPosition.put("direct",i);

                userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
                instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(getApplicationContext()));

                String instanceVersion = sharedpreferences.getString(Helper.INSTANCE_VERSION + userId + instance, null);
                if (instanceVersion != null) {
                    Version currentVersion = new Version(instanceVersion);
                    Version minVersion = new Version("2.6");
                    if (currentVersion.compareTo(minVersion) == 1 || currentVersion.equals(minVersion)) {
                        typePosition.put(i, RetrieveFeedsAsyncTask.Type.CONVERSATION);
                    } else {
                        typePosition.put(i, RetrieveFeedsAsyncTask.Type.DIRECT);
                    }
                }else{
                    typePosition.put(i, RetrieveFeedsAsyncTask.Type.DIRECT);
                }
                i++;
            }
            if( display_local) {
                tabLayout.addTab(tabLocal);
                tabPosition.put("local", i);
                typePosition.put(i, RetrieveFeedsAsyncTask.Type.LOCAL);
                i++;
            }
            if( display_global) {
                tabLayout.addTab(tabPublic);
                tabPosition.put("global", i);
                typePosition.put(i, RetrieveFeedsAsyncTask.Type.PUBLIC);
                i++;
            }
            if( display_art) {
                tabLayout.addTab(tabArt);
                tabPosition.put("art", i);
                typePosition.put(i, RetrieveFeedsAsyncTask.Type.ART);
            }

            if( (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE)
                tabLayout.setTabMode(TabLayout.MODE_FIXED);
            else if( i > 3 && !Helper.isTablet(getApplicationContext())){
                tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            }else{
                tabLayout.setTabMode(TabLayout.MODE_FIXED);
            }
            //Display filter for notification when long pressing the tab
            final LinearLayout tabStrip = (LinearLayout) tabLayout.getChildAt(0);
            tabStrip.getChildAt(1).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //Only shown if the tab has focus
                    if( notificationsFragment != null && notificationsFragment.getUserVisibleHint()){
                        PopupMenu popup = new PopupMenu(BaseMainActivity.this, tabStrip.getChildAt(1));
                        popup.getMenuInflater()
                                .inflate(R.menu.option_filter_notifications, popup.getMenu());
                        Menu menu = popup.getMenu();
                        final MenuItem itemFavourite = menu.findItem(R.id.action_favorite);
                        final MenuItem itemFollow = menu.findItem(R.id.action_follow);
                        final MenuItem itemMention = menu.findItem(R.id.action_mention);
                        final MenuItem itemBoost = menu.findItem(R.id.action_boost);
                        notif_follow = sharedpreferences.getBoolean(Helper.SET_NOTIF_FOLLOW_FILTER, true);
                        notif_add = sharedpreferences.getBoolean(Helper.SET_NOTIF_ADD_FILTER, true);
                        notif_mention = sharedpreferences.getBoolean(Helper.SET_NOTIF_MENTION_FILTER, true);
                        notif_share = sharedpreferences.getBoolean(Helper.SET_NOTIF_SHARE_FILTER, true);
                        itemFavourite.setChecked(notif_add);
                        itemFollow.setChecked(notif_follow);
                        itemMention.setChecked(notif_mention);
                        itemBoost.setChecked(notif_share);
                        popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
                            @Override
                            public void onDismiss(PopupMenu menu) {
                                if( notificationsFragment != null)
                                    notificationsFragment.refreshAll();
                            }
                        });
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                                item.setActionView(new View(getApplicationContext()));
                                item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                                    @Override
                                    public boolean onMenuItemActionExpand(MenuItem item) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onMenuItemActionCollapse(MenuItem item) {
                                        return false;
                                    }
                                });
                                switch (item.getItemId()) {
                                    case R.id.action_favorite:
                                        SharedPreferences.Editor editor = sharedpreferences.edit();
                                        notif_add = !notif_add;
                                        editor.putBoolean(Helper.SET_NOTIF_ADD_FILTER, notif_add);
                                        itemFavourite.setChecked(notif_add);
                                        editor.apply();
                                        break;
                                    case R.id.action_follow:
                                        editor = sharedpreferences.edit();
                                        notif_follow = !notif_follow;
                                        editor.putBoolean(Helper.SET_NOTIF_FOLLOW_FILTER, notif_follow);
                                        itemFollow.setChecked(notif_follow);
                                        editor.apply();
                                        break;
                                    case R.id.action_mention:
                                        editor = sharedpreferences.edit();
                                        notif_mention = !notif_mention;
                                        editor.putBoolean(Helper.SET_NOTIF_MENTION_FILTER, notif_mention);
                                        itemMention.setChecked(notif_mention);
                                        editor.apply();
                                        break;
                                    case R.id.action_boost:
                                        editor = sharedpreferences.edit();
                                        notif_share = !notif_share;
                                        editor.putBoolean(Helper.SET_NOTIF_SHARE_FILTER, notif_share);
                                        itemBoost.setChecked(notif_share);
                                        editor.apply();
                                        break;
                                }
                                return false;
                            }
                        });
                        popup.show();
                    }
                    return true;
                }
            });


            tabStrip.getChildAt(0).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return manageFilters(tabStrip, sharedpreferences);
                }
            });

            countPage = 2;
            if( sharedpreferences.getBoolean(Helper.SET_DISPLAY_DIRECT, true))
                countPage++;
            if( sharedpreferences.getBoolean(Helper.SET_DISPLAY_LOCAL, true))
                countPage++;
            if( sharedpreferences.getBoolean(Helper.SET_DISPLAY_GLOBAL, true))
                countPage++;
            if( sharedpreferences.getBoolean(Helper.SET_DISPLAY_ART, true))
                countPage++;

            if( tabPosition.containsKey("global"))
                tabStrip.getChildAt(tabPosition.get("global")).setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        return manageFilters(tabStrip, sharedpreferences);
                    }
                });
            if( tabPosition.containsKey("local"))
                tabStrip.getChildAt(tabPosition.get("local")).setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        return manageFilters(tabStrip, sharedpreferences);
                    }
                });
            if( tabPosition.containsKey("art"))
                tabStrip.getChildAt(tabPosition.get("art")).setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        return manageFilters(tabStrip, sharedpreferences);
                    }
                });

            viewPager.setOffscreenPageLimit(countPage);
            main_app_container = findViewById(R.id.main_app_container);
            adapter = new PagerAdapter
                    (getSupportFragmentManager(), tabLayout.getTabCount());
            viewPager.setAdapter(adapter);
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    viewPager.setCurrentItem(tab.getPosition());
                    if (stackBack.empty())
                        stackBack.push(0);
                    if (stackBack.contains(tab.getPosition())) {
                        stackBack.remove(stackBack.indexOf(tab.getPosition()));
                        stackBack.push(tab.getPosition());
                    } else {
                        stackBack.push(tab.getPosition());
                    }
                    main_app_container.setVisibility(View.GONE);
                    viewPager.setVisibility(View.VISIBLE);
                    delete_instance.setVisibility(View.GONE);
                    Helper.switchLayout(BaseMainActivity.this);
                    if( tab.getPosition() == 1 || (tabPosition.containsKey("art") && tab.getPosition() == tabPosition.get("art"))) {
                        toot.hide();
                        federatedTimelines.hide();
                    }else {
                        tootShow();
                        if( !displayFollowInstance)
                            federatedTimelines.hide();
                        else
                            federatedTimelinesShow();
                    }
                    DrawerLayout drawer = findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
                    if( tab.getCustomView() != null) {
                        ImageView icon = tab.getCustomView().findViewById(R.id.tab_icon);
                        if( icon != null)
                            if( theme == THEME_BLACK)
                                icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_icon), PorterDuff.Mode.SRC_IN);
                            else
                                icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.mastodonC4), PorterDuff.Mode.SRC_IN);

                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    if( tab.getCustomView() != null) {
                        ImageView icon = tab.getCustomView().findViewById(R.id.tab_icon);
                        if( icon != null)
                            if( theme == THEME_LIGHT)
                                icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_icon), PorterDuff.Mode.SRC_IN);
                            else
                                icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
                    }
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    if( viewPager.getVisibility() == View.GONE){
                        viewPager.setVisibility(View.VISIBLE);
                        delete_instance.setVisibility(View.GONE);
                        Helper.switchLayout(BaseMainActivity.this);
                        main_app_container.setVisibility(View.GONE);
                        DrawerLayout drawer = findViewById(R.id.drawer_layout);
                        drawer.closeDrawer(GravityCompat.START);
                    }
                    if( tab.getPosition() == 1 || (tabPosition.containsKey("art") && tab.getPosition() == tabPosition.get("art"))) {
                        toot.hide();
                        federatedTimelines.hide();
                    }else {
                        tootShow();
                        if( !displayFollowInstance)
                            federatedTimelines.hide();
                        else
                            federatedTimelinesShow();
                    }

                    if( viewPager.getAdapter() != null) {
                        Fragment fragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, tab.getPosition());

                        DisplayStatusFragment displayStatusFragment;
                        if (tab.getPosition() == 0) {
                            displayStatusFragment = ((DisplayStatusFragment) fragment);
                            countNewStatus = 0;
                            updateHomeCounter();
                            displayStatusFragment.scrollToTop();
                            displayStatusFragment.updateLastReadToot();
                        } else if( tab.getPosition() == 1) {
                            DisplayNotificationsFragment notificationsFragment = ((DisplayNotificationsFragment) fragment);
                            countNewNotifications = 0;
                            updateNotifCounter();
                            notificationsFragment.scrollToTop();

                        }else if (tab.getPosition() > 1) {
                            if (typePosition.containsKey(tab.getPosition()))
                                updateTimeLine(typePosition.get(tab.getPosition()), 0);
                            displayStatusFragment = ((DisplayStatusFragment) fragment);
                            displayStatusFragment.scrollToTop();
                        }
                    }
                    if( tab.getCustomView() != null) {
                        ImageView icon = tab.getCustomView().findViewById(R.id.tab_icon);
                        if( icon != null)
                            if( theme == THEME_BLACK)
                                icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_icon), PorterDuff.Mode.SRC_IN);
                            else
                                icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.mastodonC4), PorterDuff.Mode.SRC_IN);
                    }
                }
            });

            //Scroll to top when top bar is clicked for favourites/blocked/muted
            toolbarTitle.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    if( navigationView.getMenu().findItem(R.id.nav_favorites) != null && navigationView.getMenu().findItem(R.id.nav_favorites).isChecked()){
                        DisplayStatusFragment faveFrag = (DisplayStatusFragment) fragmentManager.findFragmentByTag("FAVOURITES");
                        if (faveFrag != null && faveFrag.isVisible()) {
                            faveFrag.scrollToTop();
                        }
                    } else if (navigationView.getMenu().findItem(R.id.nav_blocked) != null && navigationView.getMenu().findItem(R.id.nav_blocked).isChecked()) {
                        DisplayAccountsFragment blockFrag = (DisplayAccountsFragment) fragmentManager.findFragmentByTag("BLOCKS");

                        if (blockFrag != null && blockFrag.isVisible()) {
                            blockFrag.scrollToTop();
                        }
                    } else if (navigationView.getMenu().findItem(R.id.nav_muted) != null && navigationView.getMenu().findItem(R.id.nav_muted).isChecked()) {
                        DisplayAccountsFragment muteFrag = (DisplayAccountsFragment) fragmentManager.findFragmentByTag("MUTED");

                        if (muteFrag != null && muteFrag.isVisible()) {
                            muteFrag.scrollToTop();
                        }
                        //Scroll to top when top bar is clicked (THEME_MENU only)
                    } else {
                        int pos = tabLayout.getSelectedTabPosition();
                        if( viewPager.getAdapter() != null) {
                            Fragment fragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, pos);
                            switch (pos) {
                                case 0:
                                case 2:
                                case 3:
                                case 4:
                                    DisplayStatusFragment displayStatusFragment = ((DisplayStatusFragment) fragment);
                                    displayStatusFragment.scrollToTop();
                                    break;
                                case 1:
                                    DisplayNotificationsFragment displayNotificationsFragment = ((DisplayNotificationsFragment) fragment);
                                    displayNotificationsFragment.scrollToTop();
                                    break;
                            }
                        }
                    }
                }
            });
        }else if (social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE){
            TabLayout.Tab pTabsub = tabLayout.newTab();
            TabLayout.Tab pTabOver = tabLayout.newTab();
            TabLayout.Tab pTabTrend = tabLayout.newTab();
            TabLayout.Tab pTabAdded = tabLayout.newTab();
            TabLayout.Tab pTabLocal = tabLayout.newTab();

            pTabsub.setCustomView(R.layout.tab_badge);
            pTabOver.setCustomView(R.layout.tab_badge);
            pTabTrend.setCustomView(R.layout.tab_badge);
            pTabAdded.setCustomView(R.layout.tab_badge);
            pTabLocal.setCustomView(R.layout.tab_badge);


            @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
            ImageView iconSub = pTabsub.getCustomView().findViewById(R.id.tab_icon);

            iconSub.setImageResource(R.drawable.ic_subscriptions);

            if (theme == THEME_BLACK)
                iconSub.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_icon), PorterDuff.Mode.SRC_IN);
            else
                iconSub.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.mastodonC4), PorterDuff.Mode.SRC_IN);


            @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
            ImageView iconOver = pTabOver.getCustomView().findViewById(R.id.tab_icon);
            iconOver.setImageResource(R.drawable.ic_overview);


            @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
            ImageView iconTrend = pTabTrend.getCustomView().findViewById(R.id.tab_icon);
            iconTrend.setImageResource(R.drawable.ic_trending_up);

            @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
            ImageView iconAdded = pTabAdded.getCustomView().findViewById(R.id.tab_icon);
            iconAdded.setImageResource(R.drawable.ic_recently_added);

            @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
            ImageView iconLocal = pTabLocal.getCustomView().findViewById(R.id.tab_icon);
            iconLocal.setImageResource(R.drawable.ic_home);



            iconSub.setContentDescription(getString(R.string.subscriptions));
            iconOver.setContentDescription(getString(R.string.overview));
            iconTrend.setContentDescription(getString(R.string.trending));
            iconAdded.setContentDescription(getString(R.string.recently_added));
            iconLocal.setContentDescription(getString(R.string.local));


            if (theme == THEME_LIGHT) {
                iconSub.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.action_light_header), PorterDuff.Mode.SRC_IN);
                iconOver.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.action_light_header), PorterDuff.Mode.SRC_IN);
                iconTrend.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.action_light_header), PorterDuff.Mode.SRC_IN);
                iconAdded.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.action_light_header), PorterDuff.Mode.SRC_IN);
                iconLocal.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.action_light_header), PorterDuff.Mode.SRC_IN);
            } else {
                iconSub.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
                iconOver.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
                iconTrend.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
                iconAdded.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
                iconLocal.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
            }

            toot.setImageResource(R.drawable.ic_cloud_upload);

            tabLayout.addTab(pTabsub);
            tabLayout.addTab(pTabOver);
            tabLayout.addTab(pTabTrend);
            tabLayout.addTab(pTabAdded);
            tabLayout.addTab(pTabLocal);



            main_app_container = findViewById(R.id.main_app_container);
            adapter = new PagerAdapter
                    (getSupportFragmentManager(), tabLayout.getTabCount());
            viewPager.setAdapter(adapter);
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    viewPager.setCurrentItem(tab.getPosition());
                    if (stackBack.empty())
                        stackBack.push(0);
                    if (stackBack.contains(tab.getPosition())) {
                        stackBack.remove(stackBack.indexOf(tab.getPosition()));
                        stackBack.push(tab.getPosition());
                    } else {
                        stackBack.push(tab.getPosition());
                    }
                    main_app_container.setVisibility(View.GONE);
                    viewPager.setVisibility(View.VISIBLE);
                    delete_instance.setVisibility(View.GONE);
                    Helper.switchLayout(BaseMainActivity.this);
                    if( tab.getPosition() == 1 || (tabPosition.containsKey("art") && tab.getPosition() == tabPosition.get("art"))) {
                        toot.hide();
                        federatedTimelines.hide();
                    }else {
                        tootShow();
                        if( !displayFollowInstance)
                            federatedTimelines.hide();
                        else
                            federatedTimelinesShow();
                    }
                    DrawerLayout drawer = findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
                    if( tab.getCustomView() != null) {
                        ImageView icon = tab.getCustomView().findViewById(R.id.tab_icon);
                        if( icon != null)
                            if( theme == THEME_BLACK)
                                icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_icon), PorterDuff.Mode.SRC_IN);
                            else
                                icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.mastodonC4), PorterDuff.Mode.SRC_IN);

                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    if( tab.getCustomView() != null) {
                        ImageView icon = tab.getCustomView().findViewById(R.id.tab_icon);
                        if( icon != null)
                            if( theme == THEME_LIGHT)
                                icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_icon), PorterDuff.Mode.SRC_IN);
                            else
                                icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
                    }
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    if( tab.getCustomView() != null) {
                        ImageView icon = tab.getCustomView().findViewById(R.id.tab_icon);
                        if( icon != null)
                            if( theme == THEME_BLACK)
                                icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_icon), PorterDuff.Mode.SRC_IN);
                            else
                                icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.mastodonC4), PorterDuff.Mode.SRC_IN);
                    }
                }
            });

            //Scroll to top when top bar is clicked for favourites/blocked/muted
            toolbarTitle.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Fragment fragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, tabLayout.getSelectedTabPosition());
                    DisplayStatusFragment displayStatusFragment = ((DisplayStatusFragment) fragment);
                    displayStatusFragment.scrollToTop();
                }
            });
        }

        if (theme == Helper.THEME_DARK) {
            style = R.style.DialogDark;
        } else if (theme == Helper.THEME_BLACK){
            style = R.style.DialogBlack;
        }else {
            style = R.style.Dialog;
        }

        displayFollowInstances();

        if( theme == THEME_LIGHT){
            changeDrawableColor(getApplicationContext(), R.drawable.ic_home,R.color.dark_icon);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_notifications,R.color.dark_icon);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_direct_messages,R.color.dark_icon);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_people,R.color.dark_icon);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_public,R.color.dark_icon);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_color_lens,R.color.dark_icon);

            changeDrawableColor(getApplicationContext(), R.drawable.ic_subscriptions,R.color.dark_icon);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_overview,R.color.dark_icon);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_trending_up,R.color.dark_icon);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_recently_added,R.color.dark_icon);

        }else {
            changeDrawableColor(getApplicationContext(), R.drawable.ic_home,R.color.dark_text);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_notifications,R.color.dark_text);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_direct_messages,R.color.dark_text);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_people,R.color.dark_text);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_public,R.color.dark_text);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_color_lens,R.color.dark_text);

            changeDrawableColor(getApplicationContext(), R.drawable.ic_subscriptions,R.color.dark_text);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_overview,R.color.dark_text);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_trending_up,R.color.dark_text);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_recently_added,R.color.dark_text);
        }

        if( social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON)
            startSreaming();


        if( social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON)
            Helper.refreshSearchTag(BaseMainActivity.this, tabLayout, adapter);
        int tabCount = tabLayout.getTabCount();
        if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON)
            for( int j = countPage ; j < tabCount ; j++){
                attacheDelete(j);
            }


        toolbar_search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Hide keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(toolbar_search.getWindowToken(), 0);
                String peertube = null;

                query= query.replaceAll("^#+", "");
                //It's not a peertube search
                if(displayPeertube == null){
                    Intent intent = new Intent(BaseMainActivity.this, SearchResultActivity.class);
                    intent.putExtra("search", query);
                    startActivity(intent);
                }else{ //Peertube search
                    if( main_app_container.getVisibility() == View.GONE){
                        DisplayStatusFragment statusFragment;
                        Bundle bundle = new Bundle();
                        statusFragment = new DisplayStatusFragment();
                        bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE);
                        bundle.putString("remote_instance", displayPeertube);
                        bundle.putString("search_peertube", query);
                        statusFragment.setArguments(bundle);
                        String fragmentTag = "REMOTE_INSTANCE";
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.main_app_container, statusFragment, fragmentTag).commit();
                        main_app_container.setVisibility(View.VISIBLE);
                        toolbarTitle.setVisibility(View.VISIBLE);
                        delete_instance.setVisibility(View.VISIBLE);
                        viewPager.setVisibility(View.GONE);
                        tabLayout.setVisibility(View.GONE);
                    }else{
                        DisplayStatusFragment statusFragment;
                        Bundle bundle = new Bundle();
                        statusFragment = new DisplayStatusFragment();
                        bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE);
                        bundle.putString("remote_instance", displayPeertube);
                        bundle.putString("search_peertube", query);
                        statusFragment.setArguments(bundle);
                        String fragmentTag = "REMOTE_INSTANCE";
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.main_app_container, statusFragment, fragmentTag).commit();
                    }
                }
                toolbar_search.setQuery("", false);
                toolbar_search.setIconified(true);
                if( main_app_container.getVisibility() == View.VISIBLE){
                    main_app_container.setVisibility(View.VISIBLE);
                    viewPager.setVisibility(View.GONE);
                    delete_instance.setVisibility(View.GONE);
                    tabLayout.setVisibility(View.GONE);
                    toolbarTitle.setVisibility(View.VISIBLE);
                }else {
                    main_app_container.setVisibility(View.GONE);
                    viewPager.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.VISIBLE);
                    delete_instance.setVisibility(View.GONE);
                    toolbarTitle.setVisibility(View.GONE);
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        //Hide/Close the searchview


        toolbar_search.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if( main_app_container.getVisibility() == View.VISIBLE){
                    main_app_container.setVisibility(View.VISIBLE);
                    viewPager.setVisibility(View.GONE);
                    tabLayout.setVisibility(View.GONE);
                    toolbarTitle.setVisibility(View.VISIBLE);
                }else {
                    main_app_container.setVisibility(View.GONE);
                    viewPager.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.VISIBLE);
                    toolbarTitle.setVisibility(View.GONE);
                }
                delete_instance.setVisibility(View.GONE);
                //your code here
                return false;
            }
        });
        toolbar_search.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( toolbar_search.isIconified()){
                    if( main_app_container.getVisibility() == View.VISIBLE){
                        main_app_container.setVisibility(View.VISIBLE);
                        viewPager.setVisibility(View.GONE);
                        tabLayout.setVisibility(View.GONE);
                        toolbarTitle.setVisibility(View.VISIBLE);
                    }else {
                        main_app_container.setVisibility(View.GONE);
                        viewPager.setVisibility(View.VISIBLE);
                        tabLayout.setVisibility(View.VISIBLE);
                        toolbarTitle.setVisibility(View.GONE);
                    }
                }else {
                    toolbarTitle.setVisibility(View.GONE);
                    tabLayout.setVisibility(View.GONE);
                }
                delete_instance.setVisibility(View.GONE);
            }
        });

        //Hide the default title
        if( getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().getThemedContext().setTheme(R.style.AppThemeBlack);
        }
        //Defines the current locale of the device in a static variable
        currentLocale = Helper.currentLocale(getApplicationContext());


        tabLayout.getTabAt(0).select();
        if( social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON) {
            toot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), TootActivity.class);
                    startActivity(intent);
                }
            });
            toot.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    CrossActions.doCrossReply(BaseMainActivity.this, null, null, false);
                    return false;
                }
            });
        }
        //Image loader configuration

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setDrawerIndicatorEnabled(false);
        ImageView iconbar = toolbar.findViewById(R.id.iconbar);
        iconbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.START);
            }
        });
        Helper.loadPictureIcon(BaseMainActivity.this, account.getAvatar(),iconbar);
        headerLayout = navigationView.getHeaderView(0);

        final ImageView menuMore = headerLayout.findViewById(R.id.header_option_menu);
        menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(BaseMainActivity.this, menuMore);
                popup.getMenuInflater()
                        .inflate(R.menu.main, popup.getMenu());

                if( social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE){
                    MenuItem action_about_instance = popup.getMenu().findItem(R.id.action_about_instance);
                    if( action_about_instance != null)
                        action_about_instance.setVisible(false);
                    MenuItem action_size = popup.getMenu().findItem(R.id.action_size);
                    if( action_size != null)
                        action_size.setVisible(false);
                    MenuItem action_export = popup.getMenu().findItem(R.id.action_export);
                    if( action_export != null)
                        action_export.setVisible(false);
                }

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_logout:
                                Helper.logout(getApplicationContext());
                                Intent myIntent = new Intent(BaseMainActivity.this, LoginActivity.class);
                                startActivity(myIntent);
                                finish();
                                return true;
                            case R.id.action_privacy:
                                Intent intent = new Intent(getApplicationContext(), PrivacyActivity.class);
                                startActivity(intent);
                                return true;
                            case R.id.action_about_instance:
                                intent = new Intent(getApplicationContext(), InstanceActivity.class);
                                startActivity(intent);
                                return true;
                            case R.id.action_cache:

                                new AsyncTask<Void, Void, Void>() {
                                    private float cacheSize;
                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        long sizeCache = Helper.cacheSize(getCacheDir().getParentFile());
                                        cacheSize = 0;
                                        if( sizeCache > 0 ) {
                                            cacheSize = (float) sizeCache / 1000000.0f;
                                        }
                                        return null;
                                    }
                                    @Override
                                    protected void onPostExecute(Void result){
                                        AlertDialog.Builder builder = new AlertDialog.Builder(BaseMainActivity.this, style);
                                        builder.setTitle(R.string.cache_title);

                                        final float finalCacheSize = cacheSize;
                                        builder.setMessage(getString(R.string.cache_message, String.format("%s %s", String.format(Locale.getDefault(), "%.2f", cacheSize), getString(R.string.cache_units))))
                                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        // continue with delete
                                                        AsyncTask.execute(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                try {
                                                                    String path = getCacheDir().getParentFile().getPath();
                                                                    File dir = new File(path);
                                                                    if (dir.isDirectory()) {
                                                                        Helper.deleteDir(dir);
                                                                    }
                                                                } catch (Exception ignored) {}
                                                            }
                                                        });
                                                        Toasty.success(BaseMainActivity.this, getString(R.string.toast_cache_clear,String.format("%s %s", String.format(Locale.getDefault(), "%.2f", finalCacheSize), getString(R.string.cache_units))), Toast.LENGTH_LONG).show();
                                                        dialog.dismiss();
                                                    }
                                                })
                                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                })
                                                .setIcon(android.R.drawable.ic_dialog_alert)
                                                .show();
                                    }
                                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


                                return true;
                            case R.id.action_size:
                                final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                                int textSize = sharedpreferences.getInt(Helper.SET_TEXT_SIZE,110);
                                int iconSize = sharedpreferences.getInt(Helper.SET_ICON_SIZE,130);

                                AlertDialog.Builder builder = new AlertDialog.Builder(BaseMainActivity.this, style);
                                builder.setTitle(R.string.text_size);

                                @SuppressLint("InflateParams") View popup_quick_settings = getLayoutInflater().inflate( R.layout.popup_text_size, null );
                                builder.setView(popup_quick_settings);

                                SeekBar set_text_size = popup_quick_settings.findViewById(R.id.set_text_size);
                                SeekBar set_icon_size = popup_quick_settings.findViewById(R.id.set_icon_size);
                                final TextView set_text_size_value = popup_quick_settings.findViewById(R.id.set_text_size_value);
                                final TextView set_icon_size_value = popup_quick_settings.findViewById(R.id.set_icon_size_value);
                                set_text_size_value.setText(String.format("%s%%",String.valueOf(textSize)));
                                set_icon_size_value.setText(String.format("%s%%",String.valueOf(iconSize)));

                                set_text_size.setMax(20);
                                set_icon_size.setMax(20);

                                set_text_size.setProgress(((textSize-80)/5));
                                set_icon_size.setProgress(((iconSize-80)/5));

                                set_text_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                    @Override
                                    public void onStopTrackingTouch(SeekBar seekBar) {}
                                    @Override
                                    public void onStartTrackingTouch(SeekBar seekBar) {}
                                    @Override
                                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                                        int value = 80 + progress*5;
                                        set_text_size_value.setText(String.format("%s%%",String.valueOf(value)));
                                        SharedPreferences.Editor editor = sharedpreferences.edit();
                                        editor.putInt(Helper.SET_TEXT_SIZE, value);
                                        editor.apply();
                                    }
                                });
                                set_icon_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                    @Override
                                    public void onStopTrackingTouch(SeekBar seekBar) {}
                                    @Override
                                    public void onStartTrackingTouch(SeekBar seekBar) {}
                                    @Override
                                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                        int value = 80 + progress*5;
                                        set_icon_size_value.setText(String.format("%s%%",String.valueOf(value)));
                                        SharedPreferences.Editor editor = sharedpreferences.edit();
                                        editor.putInt(Helper.SET_ICON_SIZE, value);
                                        editor.apply();
                                    }
                                });
                                builder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        BaseMainActivity.this.recreate();
                                        dialog.dismiss();
                                    }
                                })
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                                return true;
                            case R.id.action_proxy:
                                intent = new Intent(getApplicationContext(), ProxyActivity.class);
                                startActivity(intent);
                                return true;
                            case R.id.action_export:
                                if(Build.VERSION.SDK_INT >= 23 ){
                                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
                                        ActivityCompat.requestPermissions(BaseMainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_REQUEST_CODE);
                                    } else {
                                        Intent backupIntent = new Intent(BaseMainActivity.this, BackupStatusService.class);
                                        backupIntent.putExtra("userId", userId);
                                        startService(backupIntent);
                                    }
                                }else{
                                    Intent backupIntent = new Intent(BaseMainActivity.this, BackupStatusService.class);
                                    backupIntent.putExtra("userId", userId);
                                    startService(backupIntent);
                                }
                                return true;
                            default:
                                return true;
                        }
                    }
                });
                popup.show();
            }
        });
        final ImageView optionInfo = headerLayout.findViewById(R.id.header_option_info);
        optionInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), InstanceHealthActivity.class);
                startActivity(intent);
            }
        });
        if( social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE)
            optionInfo.setVisibility(View.GONE);
        MenuFloating.tags = new ArrayList<>();
        updateHeaderAccountInfo(activity, account, headerLayout);
        //Locked account can see follow request
        if (account.isLocked()) {
            navigationView.getMenu().findItem(R.id.nav_follow_request).setVisible(true);
        } else {
            navigationView.getMenu().findItem(R.id.nav_follow_request).setVisible(false);
        }

        //Check instance release for lists
        String instanceVersion = sharedpreferences.getString(Helper.INSTANCE_VERSION + userId + instance, null);
        if (instanceVersion != null) {
            Version currentVersion = new Version(instanceVersion);
            Version minVersion = new Version("2.1");
            if (currentVersion.compareTo(minVersion) == 1 || currentVersion.equals(minVersion)) {
                navigationView.getMenu().findItem(R.id.nav_list).setVisible(true);
            } else {
                navigationView.getMenu().findItem(R.id.nav_list).setVisible(false);
            }
        }

        LinearLayout owner_container = headerLayout.findViewById(R.id.main_header_container);
        owner_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuAccounts(BaseMainActivity.this);
                if( main_app_container.getVisibility() == View.VISIBLE){
                    main_app_container.setVisibility(View.VISIBLE);
                    viewPager.setVisibility(View.GONE);
                    tabLayout.setVisibility(View.GONE);
                    toolbarTitle.setVisibility(View.VISIBLE);
                }else {
                    main_app_container.setVisibility(View.GONE);
                    viewPager.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.VISIBLE);
                    toolbarTitle.setVisibility(View.GONE);
                }
                delete_instance.setVisibility(View.GONE);
            }
        });




        // Asked once for notification opt-in
        boolean popupShown = sharedpreferences.getBoolean(Helper.SET_POPUP_PUSH, false);
        if(!popupShown && social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON){
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(BaseMainActivity.this, style);
            LayoutInflater inflater = getLayoutInflater();
            @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.popup_quick_settings, null);
            dialogBuilder.setView(dialogView);

            final SwitchCompat set_push_hometimeline = dialogView.findViewById(R.id.set_push_hometimeline);
            final SwitchCompat set_push_notification = dialogView.findViewById(R.id.set_push_notification);
            boolean notif_hometimeline = sharedpreferences.getBoolean(Helper.SET_NOTIF_HOMETIMELINE, false);
            boolean notif_follow = sharedpreferences.getBoolean(Helper.SET_NOTIF_FOLLOW, true);
            boolean notif_add = sharedpreferences.getBoolean(Helper.SET_NOTIF_ADD, true);
            boolean notif_ask = sharedpreferences.getBoolean(Helper.SET_NOTIF_ASK, true);
            boolean notif_mention = sharedpreferences.getBoolean(Helper.SET_NOTIF_MENTION, true);
            boolean notif_share = sharedpreferences.getBoolean(Helper.SET_NOTIF_SHARE, true);
            boolean notifif_notifications = !( !notif_follow &&  !notif_add && !notif_ask && !notif_mention && !notif_share);
            set_push_hometimeline.setChecked(notif_hometimeline);
            set_push_notification.setChecked(notifif_notifications);

            dialogBuilder.setTitle(R.string.settings_popup_title);
            dialogBuilder.setCancelable(false);
            dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putBoolean(Helper.SET_NOTIF_FOLLOW, set_push_notification.isChecked());
                    editor.putBoolean(Helper.SET_NOTIF_ADD, set_push_notification.isChecked());
                    editor.putBoolean(Helper.SET_NOTIF_ASK, set_push_notification.isChecked());
                    editor.putBoolean(Helper.SET_NOTIF_MENTION, set_push_notification.isChecked());
                    editor.putBoolean(Helper.SET_NOTIF_SHARE, set_push_notification.isChecked());
                    editor.putBoolean(Helper.SET_NOTIF_HOMETIMELINE, set_push_hometimeline.isChecked());
                    editor.putBoolean(Helper.SET_POPUP_PUSH, true);
                    editor.apply();
                }
            }).show();
        }
        Helper.switchLayout(BaseMainActivity.this);


        if( receive_data != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receive_data);
        receive_data = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getExtras();
                assert b != null;
                userIdService = b.getString("userIdService", null);
                if( userIdService != null && userIdService.equals(userId)) {
                    Notification notification = b.getParcelable("data");
                    if (notificationsFragment != null) {
                        notificationsFragment.refresh(notification);
                    }
                    updateNotifCounter();
                }
            }
        };

        mamageNewIntent(getIntent());

        if( social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON) {
            LocalBroadcastManager.getInstance(this).registerReceiver(receive_data, new IntentFilter(Helper.RECEIVE_DATA));
            // Retrieves instance
            new RetrieveInstanceAsyncTask(getApplicationContext(), BaseMainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            // Retrieves filters
            new ManageFiltersAsyncTask(getApplicationContext(), GET_ALL_FILTER, null, BaseMainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }


    public void removeSearchTab(String tag){
        Helper.removeSearchTag(tag, tabLayout, adapter);
        int allTabCount = tabLayout.getTabCount();
        if( allTabCount == countPage){
            main_app_container.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.VISIBLE);
            toolbarTitle.setVisibility(View.GONE);
            delete_instance.setVisibility(View.GONE);
        }
    }


    protected abstract void rateThisApp();


    private boolean manageFilters(LinearLayout tabStrip, final SharedPreferences sharedpreferences){
        //Only shown if the tab has focus
        if(
                (homeFragment != null && homeFragment.getUserVisibleHint()) ||
                (federatedFragment != null && federatedFragment.getUserVisibleHint()) ||
                (localFragment != null && localFragment.getUserVisibleHint()) ||
                (artFragment != null && artFragment.getUserVisibleHint())
        ){
            PopupMenu popup = null;
            if(homeFragment != null && homeFragment.getUserVisibleHint())
                popup = new PopupMenu(BaseMainActivity.this, tabStrip.getChildAt(0));
            else if(localFragment != null && localFragment.getUserVisibleHint())
                popup = new PopupMenu(BaseMainActivity.this, tabStrip.getChildAt(tabPosition.get("local")));
            else if(federatedFragment != null && federatedFragment.getUserVisibleHint()){
                popup = new PopupMenu(BaseMainActivity.this, tabStrip.getChildAt(tabPosition.get("global")));
            }else if(artFragment != null && artFragment.getUserVisibleHint()){
                popup = new PopupMenu(BaseMainActivity.this, tabStrip.getChildAt(tabPosition.get("art")));
                popup.getMenuInflater()
                        .inflate(R.menu.option_tag_timeline, popup.getMenu());
                Menu menu = popup.getMenu();

                show_nsfw = sharedpreferences.getBoolean(Helper.SET_ART_WITH_NSFW, false);
                final MenuItem itemShowNSFW = menu.findItem(R.id.action_show_nsfw);
                final MenuItem itemMedia = menu.findItem(R.id.action_show_media_only);
                final MenuItem itemDelete = menu.findItem(R.id.action_delete);

                final MenuItem itemAny = menu.findItem(R.id.action_any);
                final MenuItem itemAll = menu.findItem(R.id.action_all);
                final MenuItem itemNone = menu.findItem(R.id.action_none);
                final MenuItem action_displayname = menu.findItem(R.id.action_displayname);
                itemAny.setVisible(false);
                itemAll.setVisible(false);
                itemNone.setVisible(false);
                action_displayname.setVisible(false);
                itemMedia.setVisible(false);
                itemDelete.setVisible(false);
                itemShowNSFW.setChecked(show_nsfw);
                final boolean[] changes = {false};
                popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu menu) {
                        if(changes[0]) {
                            FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
                            fragTransaction.detach(artFragment);
                            fragTransaction.attach(artFragment);
                            fragTransaction.commit();
                        }
                    }
                });
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        changes[0] = true;
                        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                        item.setActionView(new View(getApplicationContext()));
                        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                            @Override
                            public boolean onMenuItemActionExpand(MenuItem item) {
                                return false;
                            }

                            @Override
                            public boolean onMenuItemActionCollapse(MenuItem item) {
                                return false;
                            }
                        });
                        switch (item.getItemId()) {

                            case R.id.action_show_nsfw:
                                show_nsfw = !show_nsfw;
                                itemShowNSFW.setChecked(show_nsfw);
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putBoolean(Helper.SET_ART_WITH_NSFW, show_nsfw);
                                editor.apply();
                                break;
                        }
                        return false;
                    }
                });
                popup.show();
                return false;
            }
            if( popup == null)
                return true;
            popup.getMenuInflater()
                    .inflate(R.menu.option_filter_toots, popup.getMenu());
            Menu menu = popup.getMenu();
            final MenuItem itemShowBoosts = menu.findItem(R.id.action_show_boosts);
            final MenuItem itemShowReplies = menu.findItem(R.id.action_show_replies);
            final MenuItem itemFilter = menu.findItem(R.id.action_filter);
            if((federatedFragment != null && federatedFragment.getUserVisibleHint()) ||
                    (localFragment != null && localFragment.getUserVisibleHint())){
                itemShowBoosts.setVisible(false);
                itemShowReplies.setVisible(false);
                itemFilter.setVisible(true);
            }else {
                itemShowBoosts.setVisible(true);
                itemShowReplies.setVisible(true);
                itemFilter.setVisible(true);
            }
            show_boosts = sharedpreferences.getBoolean(Helper.SET_SHOW_BOOSTS, true);
            show_replies = sharedpreferences.getBoolean(Helper.SET_SHOW_REPLIES, true);

            if(homeFragment != null && homeFragment.getUserVisibleHint())
                show_filtered = sharedpreferences.getString(Helper.SET_FILTER_REGEX_HOME, null);
            if(localFragment != null && localFragment.getUserVisibleHint())
                show_filtered = sharedpreferences.getString(Helper.SET_FILTER_REGEX_LOCAL, null);
            if(federatedFragment != null && federatedFragment.getUserVisibleHint())
                show_filtered = sharedpreferences.getString(Helper.SET_FILTER_REGEX_PUBLIC, null);

            itemShowBoosts.setChecked(show_boosts);
            itemShowReplies.setChecked(show_replies);
            if( show_filtered != null && show_filtered.length() > 0){
                itemFilter.setTitle(show_filtered);
            }

            popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
                @Override
                public void onDismiss(PopupMenu menu) {
                    refreshFilters();
                }
            });
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                    item.setActionView(new View(getApplicationContext()));
                    item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                        @Override
                        public boolean onMenuItemActionExpand(MenuItem item) {
                            return false;
                        }

                        @Override
                        public boolean onMenuItemActionCollapse(MenuItem item) {
                            return false;
                        }
                    });
                    final SharedPreferences.Editor editor = sharedpreferences.edit();
                    switch (item.getItemId()) {
                        case R.id.action_show_boosts:
                            show_boosts = !show_boosts;
                            editor.putBoolean(Helper.SET_SHOW_BOOSTS, show_boosts);
                            itemShowBoosts.setChecked(show_boosts);
                            editor.apply();
                            break;
                        case R.id.action_show_replies:
                            show_replies = !show_replies;
                            editor.putBoolean(Helper.SET_SHOW_REPLIES, show_replies);
                            itemShowReplies.setChecked(show_replies);
                            editor.apply();
                            break;
                        case R.id.action_filter:
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(BaseMainActivity.this, style);
                            LayoutInflater inflater = getLayoutInflater();
                            @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.filter_regex, null);
                            dialogBuilder.setView(dialogView);
                            final EditText editText = dialogView.findViewById(R.id.filter_regex);
                            Toast alertRegex = Toasty.warning(BaseMainActivity.this, getString(R.string.alert_regex), Toast.LENGTH_LONG);
                            editText.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                }
                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                }
                                @Override
                                public void afterTextChanged(Editable s) {
                                    try {
                                        //noinspection ResultOfMethodCallIgnored
                                        Pattern.compile("(" + s.toString() + ")", Pattern.CASE_INSENSITIVE);
                                    }catch (Exception e){
                                        if( !alertRegex.getView().isShown()){
                                            alertRegex.show();
                                        }
                                    }

                                }
                            });
                            if( show_filtered != null) {
                                editText.setText(show_filtered);
                                editText.setSelection(editText.getText().toString().length());
                            }
                            dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    itemFilter.setTitle(editText.getText().toString().trim());
                                    if(homeFragment != null && homeFragment.getUserVisibleHint())
                                        editor.putString(Helper.SET_FILTER_REGEX_HOME, editText.getText().toString().trim());
                                    if(localFragment != null && localFragment.getUserVisibleHint())
                                        editor.putString(Helper.SET_FILTER_REGEX_LOCAL, editText.getText().toString().trim());
                                    if(federatedFragment != null && federatedFragment.getUserVisibleHint())
                                        editor.putString(Helper.SET_FILTER_REGEX_PUBLIC, editText.getText().toString().trim());
                                    editor.apply();
                                }
                            });
                            AlertDialog alertDialog = dialogBuilder.create();
                            alertDialog.show();
                            break;
                    }
                    return false;
                }
            });
            popup.show();
        }
        return true;
    }



    public void refreshFilters(){
        if(homeFragment != null)
            homeFragment.refreshFilter();
        if(localFragment != null)
            localFragment.refreshFilter();
        if(federatedFragment != null)
            federatedFragment.refreshFilter();
        if(artFragment != null)
            artFragment.refreshFilter();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mamageNewIntent(intent);
    }

    /**
     * Manages new intents
     * @param intent Intent - intent related to a notification in top bar
     */
    private void mamageNewIntent(Intent intent){

        if( intent == null )
            return;

        String action = intent.getAction();
        String type = intent.getType();
        Bundle extras = intent.getExtras();
        String userIdIntent;
        if( extras != null && extras.containsKey(INTENT_ACTION) ){
            final NavigationView navigationView = findViewById(R.id.nav_view);
            userIdIntent = extras.getString(PREF_KEY_ID); //Id of the account in the intent
            if (extras.getInt(INTENT_ACTION) == NOTIFICATION_INTENT){
                changeUser(BaseMainActivity.this, userIdIntent, false); //Connects the account which is related to the notification
                unCheckAllMenuItems(navigationView);
                if( tabLayout.getTabAt(1) != null)
                    //noinspection ConstantConditions
                    tabLayout.getTabAt(1).select();
                if( extras.getString(INTENT_TARGETED_ACCOUNT) != null ){
                    Intent intentShow = new Intent(BaseMainActivity.this, ShowAccountActivity.class);
                    Bundle b = new Bundle();
                    b.putString("accountId", extras.getString(INTENT_TARGETED_ACCOUNT));
                    intentShow.putExtras(b);
                    startActivity(intentShow);
                }
            }else if( extras.getInt(INTENT_ACTION) == SEARCH_INSTANCE){
                String instance = extras.getString(INSTANCE_NAME);
                DisplayStatusFragment statusFragment;
                Bundle bundle = new Bundle();
                statusFragment = new DisplayStatusFragment();
                bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE);
                bundle.putString("remote_instance", instance);
                statusFragment.setArguments(bundle);
                String fragmentTag = "REMOTE_INSTANCE";
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.main_app_container, statusFragment, fragmentTag).commit();
                main_app_container.setVisibility(View.VISIBLE);
                toolbarTitle.setVisibility(View.VISIBLE);
                delete_instance.setVisibility(View.VISIBLE);
                viewPager.setVisibility(View.GONE);
                tabLayout.setVisibility(View.GONE);
                toolbarTitle.setText(instance);
            }else if( extras.getInt(INTENT_ACTION) == HOME_TIMELINE_INTENT){
                changeUser(BaseMainActivity.this, userIdIntent, true); //Connects the account which is related to the notification
            }else if( extras.getInt(INTENT_ACTION) == BACK_TO_SETTINGS){
                unCheckAllMenuItems(navigationView);
                navigationView.setCheckedItem(R.id.nav_settings);
                navigationView.getMenu().performIdentifierAction(R.id.nav_settings, 0);
                toolbarTitle.setText(R.string.settings);
            }else if( extras.getInt(INTENT_ACTION) == BACK_TO_SETTINGS){
                unCheckAllMenuItems(navigationView);
                navigationView.setCheckedItem(R.id.nav_peertube_settings);
                navigationView.getMenu().performIdentifierAction(R.id.nav_peertube_settings, 0);
                toolbarTitle.setText(R.string.settings);
            }else if (extras.getInt(INTENT_ACTION) == ADD_USER_INTENT){
                this.recreate();
            }else if( extras.getInt(INTENT_ACTION) == BACKUP_INTENT){
                Intent myIntent = new Intent(BaseMainActivity.this, OwnerStatusActivity.class);
                startActivity(myIntent);
            }else if (extras.getInt(INTENT_ACTION) == SEARCH_REMOTE) {
                String url = extras.getString(SEARCH_URL);
                intent.replaceExtras(new Bundle());
                intent.setAction("");
                intent.setData(null);
                intent.setFlags(0);
                if( url == null)
                    return;
                Matcher matcher;
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
                    matcher = Patterns.WEB_URL.matcher(url);
                else
                    matcher = Helper.urlPattern.matcher(url);
                boolean isUrl = false;
                while (matcher.find()){
                    isUrl = true;
                }
                if(!isUrl)
                    return;
                //Here we know that the intent contains a valid URL
                new RetrieveRemoteDataAsyncTask(BaseMainActivity.this, url, BaseMainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }else if( Intent.ACTION_SEND.equals(action) && type != null ) {
            if ("text/plain".equals(type)) {
                String url = null;
                String sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    /* Some apps don't send the URL as the first part of the EXTRA_TEXT,
                        the BBC News app being one such, in this case find where the URL
                        is and strip that out into sharedText.
                     */
                    Matcher matcher;
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
                        matcher = Patterns.WEB_URL.matcher(sharedText);
                    else
                        matcher = Helper.urlPattern.matcher(sharedText);
                    while (matcher.find()){
                        int matchStart = matcher.start(1);
                        int matchEnd = matcher.end();
                        if(matchStart < matchEnd && sharedText.length() >= matchEnd)
                            url = sharedText.substring(matchStart, matchEnd);
                    }
                    new RetrieveMetaDataAsyncTask(BaseMainActivity.this, sharedSubject, sharedText, url,BaseMainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

            } else if (type.startsWith("image/") || type.startsWith("video/")) {

                if( !TootActivity.active){
                    Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    if (imageUri != null) {
                        Bundle b = new Bundle();
                        b.putParcelable("sharedUri", imageUri);
                        b.putInt("uriNumberMast", 1);
                        CrossActions.doCrossShare(BaseMainActivity.this, b);
                    }
                }else{
                    Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    if (imageUri != null) {
                        intent = new Intent(getApplicationContext(), TootActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent .putExtra("imageUri", imageUri.toString());
                        startActivity(intent );
                    }
                }

            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null ) {
            if (type.startsWith("image/")  || type.startsWith("video/")) {

                ArrayList<Uri> imageList = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if (imageList != null) {
                    Bundle b = new Bundle();
                    b.putParcelableArrayList("sharedUri", imageList);
                    b.putInt("uriNumberMast", imageList.size());
                    CrossActions.doCrossShare(BaseMainActivity.this, b);
                }
            }
        }else if (Intent.ACTION_VIEW.equals(action)) {
            String url = intent.getDataString();
            intent.replaceExtras(new Bundle());
            intent.setAction("");
            intent.setData(null);
            intent.setFlags(0);
            if( url == null)
                return;
            Matcher matcher;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
                matcher = Patterns.WEB_URL.matcher(url);
            else
                matcher = Helper.urlPattern.matcher(url);
            boolean isUrl = false;
            while (matcher.find()){
                isUrl = true;
            }
            if(!isUrl)
                return;
            //Here we know that the intent contains a valid URL
            new RetrieveRemoteDataAsyncTask(BaseMainActivity.this, url, BaseMainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        intent.replaceExtras(new Bundle());
        intent.setAction("");
        intent.setData(null);
        intent.setFlags(0);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            displayPeertube = null;
            //Hide search bar on back pressed
            if( !toolbar_search.isIconified()){
                toolbar_search.setIconified(true);
                return;
            }
            if( viewPager.getVisibility() == View.VISIBLE){
                if (stackBack.size() > 1) {
                    stackBack.pop();
                    viewPager.setCurrentItem(stackBack.lastElement());
                }else {
                    super.onBackPressed();
                }
            } else {
                Helper.switchLayout(BaseMainActivity.this);
                main_app_container.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);
                tabLayout.setVisibility(View.VISIBLE);
                toolbarTitle.setVisibility(View.GONE);
                delete_instance.setVisibility(View.GONE);
                delete_all.hide();

                add_new.hide();
                final NavigationView navigationView = findViewById(R.id.nav_view);
                unCheckAllMenuItems(navigationView);
                tootShow();
                switch (viewPager.getCurrentItem()){
                    case 1:
                        toot.hide();
                        break;
                }
            }

        }

    }

    @Override
    public void onResume(){
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isMainActivityRunning", true).apply();
        updateNotifCounter();
        updateHomeCounter();

        SQLiteDatabase db = Sqlite.getInstance(BaseMainActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        Account account = new AccountDAO(getApplicationContext(), db).getAccountByID(userId);

        //Proceeds to update of the authenticated account
        if(Helper.isLoggedIn(getApplicationContext())) {
            new UpdateAccountInfoByIDAsyncTask(getApplicationContext(), social, BaseMainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        if( lastHomeId != null && homeFragment != null){
            homeFragment.retrieveMissingToots(lastHomeId);
        }
        if( lastNotificationId != null && notificationsFragment != null){
            notificationsFragment.retrieveMissingNotifications(lastNotificationId);
        }
    }


    @Override
    public void onStart(){
        super.onStart();
        if( receive_federated_data != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receive_federated_data);
        receive_federated_data = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getExtras();
                assert b != null;
                userIdService = b.getString("userIdService", null);
                if( userIdService != null && userIdService.equals(userId)) {
                    Status status = b.getParcelable("data");
                    if (federatedFragment != null) {
                        federatedFragment.refresh(status);
                    }
                }
            }
        };
        if( receive_home_data != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receive_home_data);
        receive_home_data = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getExtras();
                assert b != null;
                userIdService = b.getString("userIdService", null);
                if( userIdService != null && userIdService.equals(userId)) {
                    Status status = b.getParcelable("data");
                    if (homeFragment != null) {
                        homeFragment.refresh(status);
                    }
                }
            }
        };
        if( receive_local_data != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receive_local_data);
        receive_local_data = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getExtras();
                assert b != null;
                userIdService = b.getString("userIdService", null);
                if( userIdService != null && userIdService.equals(userId)) {
                    Status status = b.getParcelable("data");
                    if (localFragment != null) {
                        localFragment.refresh(status);
                    }
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(receive_home_data, new IntentFilter(Helper.RECEIVE_HOME_DATA));
        LocalBroadcastManager.getInstance(this).registerReceiver(receive_federated_data, new IntentFilter(Helper.RECEIVE_FEDERATED_DATA));
        LocalBroadcastManager.getInstance(this).registerReceiver(receive_local_data, new IntentFilter(Helper.RECEIVE_LOCAL_DATA));

    }

    @Override
    public void onStop(){
        super.onStop();
        if( receive_home_data != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receive_home_data);
        if( receive_federated_data != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receive_federated_data);
        if( receive_local_data != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receive_local_data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isMainActivityRunning", false).apply();
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        if( receive_data != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receive_data);

        boolean backgroundProcess = sharedpreferences.getBoolean(Helper.SET_KEEP_BACKGROUND_PROCESS, true);
        if(!backgroundProcess)
            sendBroadcast(new Intent("StopLiveNotificationService"));
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isMainActivityRunning", false).apply();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if( id == R.id.nav_archive) {
            Intent myIntent = new Intent(BaseMainActivity.this, OwnerStatusActivity.class);
            startActivity(myIntent);
            return false;
        } else if( id == R.id.nav_about) {
            Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(intent);
            return false;
        } else if( id == R.id.nav_upload) {
            Intent intent = new Intent(getApplicationContext(), PeertubeUploadActivity.class);
            startActivity(intent);
            return false;
        } else if( id == R.id.nav_language) {
            Intent intent = new Intent(getApplicationContext(), LanguageActivity.class);
            startActivity(intent);
            return false;
        } else if( id == R.id.nav_partnership) {
            Intent intent = new Intent(getApplicationContext(), PartnerShipActivity.class);
            startActivity(intent);
            return false;
        }else if(id == R.id.nav_bug_report){
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"incoming+tom79/mastalab@incoming.gitlab.com"});
            try {
                startActivity(Intent.createChooser(i, getString(R.string.bug_report_mail)));
            } catch (android.content.ActivityNotFoundException ex) {
                Toasty.info(getApplicationContext(), getString(R.string.no_mail_client), Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        final NavigationView navigationView = findViewById(R.id.nav_view);
        unCheckAllMenuItems(navigationView);
        item.setChecked(true);
        //Remove the search bar
        if (!toolbar_search.isIconified()) {
            toolbar_search.setIconified(true);
        }
        toolbarTitle.setText(item.getTitle());
        DisplayStatusFragment statusFragment;
        DisplayAccountsFragment accountsFragment;
        Bundle bundle = new Bundle();
        FragmentManager fragmentManager = getSupportFragmentManager();
        String fragmentTag = null;

        main_app_container.setVisibility(View.VISIBLE);

        viewPager.setVisibility(View.GONE);
        tabLayout.setVisibility(View.GONE);
        toolbarTitle.setVisibility(View.VISIBLE);
        delete_instance.setVisibility(View.GONE);
        appBar.setExpanded(true);
        if (id != R.id.nav_drafts && id != R.id.nav_bookmarks && id != R.id.nav_peertube ) {
            delete_all.hide();
        }else{
            delete_all.show();
        }
        if( id != R.id.nav_list && id != R.id.nav_filters){
            add_new.hide();
        }else{
            add_new.show();
        }
        if (id == R.id.nav_settings) {
            toot.hide();
            TabLayoutSettingsFragment tabLayoutSettingsFragment= new TabLayoutSettingsFragment();
            fragmentTag = "TABLAYOUT_SETTINGS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, tabLayoutSettingsFragment, fragmentTag).commit();

        }else if (id == R.id.nav_peertube_settings) {
            toot.hide();
            SettingsPeertubeFragment settingsPeertubeFragment= new SettingsPeertubeFragment();
            fragmentTag = "TABLAYOUT_PEERTUBE_SETTINGS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, settingsPeertubeFragment, fragmentTag).commit();

        }else if (id == R.id.nav_favorites) {
            toot.hide();
            statusFragment = new DisplayStatusFragment();
            bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.FAVOURITES);
            statusFragment.setArguments(bundle);
            fragmentTag = "FAVOURITES";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, statusFragment, fragmentTag).commit();
        } else if (id == R.id.nav_my_video) {
            bundle = new Bundle();
            DisplayStatusFragment fragment = new DisplayStatusFragment();
            bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.MYVIDEOS);
            bundle.putString("instanceType","PEERTUBE");
            SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            String token = sharedpreferences.getString(Helper.PREF_KEY_OAUTH_TOKEN, null);
            SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            Account account = new AccountDAO(getApplicationContext(), db).getAccountByToken(token);
            bundle.putString("targetedid",account.getUsername());
            bundle.putBoolean("ownvideos", true);
            fragment.setArguments(bundle);
            fragmentTag = "MY_VIDEOS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, fragment, fragmentTag).commit();
        } else if (id == R.id.nav_blocked) {
            toot.hide();
            accountsFragment = new DisplayAccountsFragment();
            bundle.putSerializable("type", RetrieveAccountsAsyncTask.Type.BLOCKED);
            accountsFragment.setArguments(bundle);
            fragmentTag = "BLOCKS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, accountsFragment, fragmentTag).commit();
        }else if (id == R.id.nav_blocked_domains) {
            toot.hide();
            DisplayMutedInstanceFragment displayMutedInstanceFragment = new DisplayMutedInstanceFragment();
            fragmentTag = "BLOCKED_DOMAINS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, displayMutedInstanceFragment, fragmentTag).commit();
        }else if (id == R.id.nav_how_to) {
            toot.hide();
            DisplayHowToFragment displayHowToFragment = new DisplayHowToFragment();
            fragmentTag = "HOW_TO_VIDEOS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, displayHowToFragment, fragmentTag).commit();
        }else if (id == R.id.nav_muted) {
            toot.hide();
            accountsFragment = new DisplayAccountsFragment();
            bundle.putSerializable("type", RetrieveAccountsAsyncTask.Type.MUTED);
            accountsFragment.setArguments(bundle);
            fragmentTag = "MUTED";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, accountsFragment, fragmentTag).commit();
        }else if (id == R.id.nav_scheduled) {
            tootShow();
            TabLayoutScheduleFragment tabLayoutScheduleFragment = new TabLayoutScheduleFragment();
            fragmentTag = "SCHEDULED";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, tabLayoutScheduleFragment, fragmentTag).commit();
        }else if (id == R.id.nav_drafts) {
            DisplayDraftsFragment displayDraftsFragment = new DisplayDraftsFragment();
            fragmentTag = "DRAFTS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, displayDraftsFragment, fragmentTag).commit();
            toot.hide();
        }else if (id == R.id.nav_bookmarks) {
            DisplayBookmarksFragment displayBookmarksFragment = new DisplayBookmarksFragment();
            fragmentTag = "BOOKMARKS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, displayBookmarksFragment, fragmentTag).commit();
            toot.hide();
        }else if (id == R.id.nav_peertube) {
            DisplayFavoritesPeertubeFragment displayFavoritesPeertubeFragment = new DisplayFavoritesPeertubeFragment();
            fragmentTag = "BOOKMARKS_PEERTUBE";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, displayFavoritesPeertubeFragment, fragmentTag).commit();
            toot.hide();
        }else if (id == R.id.nav_peertube_fav) {
            DisplayFavoritesPeertubeFragment displayFavoritesPeertubeFragment = new DisplayFavoritesPeertubeFragment();
            fragmentTag = "BOOKMARKS_PEERTUBE";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, displayFavoritesPeertubeFragment, fragmentTag).commit();
            toot.hide();
        }else if( id == R.id.nav_follow_request){
            toot.hide();
            DisplayFollowRequestSentFragment followRequestSentFragment = new DisplayFollowRequestSentFragment();
            fragmentTag = "FOLLOW_REQUEST_SENT";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, followRequestSentFragment, fragmentTag).commit();
        }else if(id == R.id.nav_list){
            toot.hide();
            DisplayListsFragment displayListsFragment = new DisplayListsFragment();
            fragmentTag = "LISTS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, displayListsFragment, fragmentTag).commit();
        }else if(id == R.id.nav_filters){
            toot.hide();
            DisplayFiltersFragment displayFiltersFragment = new DisplayFiltersFragment();
            fragmentTag = "FILTERS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, displayFiltersFragment, fragmentTag).commit();
        }else if(id == R.id.nav_who_to_follow){
            toot.hide();
            WhoToFollowFragment whoToFollowFragment = new WhoToFollowFragment();
            fragmentTag = "WHO_TO_FOLLOW";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, whoToFollowFragment, fragmentTag).commit();
        }

        populateTitleWithTag(fragmentTag, item.getTitle().toString(), item.getItemId());
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void populateTitleWithTag(String tag, String title, int index){
        if( tag == null)
            return;
        if ( tagTile.get(tag) == null)
            tagTile.put(tag, title);
        if ( tagItem.get(tag) == null)
            tagItem.put(tag, index);
    }

    @Override
    public void setTitle(CharSequence title) {
        if(toolbarTitle != null )
            toolbarTitle.setText(title);
    }

    @Override
    public void onUpdateAccountInfo(boolean error) {
        if( error){
            //An error occurred,  the user is redirected to the login page
            Helper.logout(getApplicationContext());
            Intent myIntent = new Intent(BaseMainActivity.this, LoginActivity.class);
            startActivity(myIntent);
            finish();
        }else {
            SQLiteDatabase db = Sqlite.getInstance(BaseMainActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            Account account = new AccountDAO(getApplicationContext(), db).getAccountByID(userId);
            updateHeaderAccountInfo(activity, account, headerLayout);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //noinspection StatementWithEmptyBody
        if (requestCode == ERROR_DIALOG_REQUEST_CODE) {
            // Adding a fragment via GooglePlayServicesUtil.showErrorDialogFragment
            // before the instance state is restored throws an error. So instead,
            // set a flag here, which will cause the fragment to delay until
            // onPostResume.
        }
    }


    @Override
    public void onRetrieveMetaData(boolean error, String sharedSubject, String sharedText, String image, String title, String description) {
        Bundle b = new Bundle();
        if( !error) {
            b.putString("image", image);
            b.putString("title", title);
            b.putString("description", description);
        }
        b.putString("sharedSubject", sharedSubject);
        b.putString("sharedContent", sharedText);
        CrossActions.doCrossShare(BaseMainActivity.this, b);
    }

    @Override
    public void onRetrieveInstance(APIResponse apiResponse) {
        if( apiResponse.getError() != null){
            return;
        }
        if( apiResponse.getInstance() == null || apiResponse.getInstance().getVersion() == null || apiResponse.getInstance().getVersion().trim().length() == 0)
            return;
        Version currentVersion = new Version(apiResponse.getInstance().getVersion());
        Version minVersion = new Version("1.6");
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(Helper.INSTANCE_VERSION + userId + instance, apiResponse.getInstance().getVersion());
        editor.apply();
        Helper.canPin = (currentVersion.compareTo(minVersion) == 1 || currentVersion.equals(minVersion));
    }

    @Override
    public void onRetrieveRemoteAccount(Results results) {
        if (results == null)
            return;
        List<Account> accounts = results.getAccounts();
        List<Status> statuses = results.getStatuses();
        if( accounts !=null && accounts.size() > 0){
            Intent intent = new Intent(BaseMainActivity.this, ShowAccountActivity.class);
            Bundle b = new Bundle();
            b.putParcelable("account", accounts.get(0));
            intent.putExtras(b);
            startActivity(intent);
        }else if( statuses != null && statuses.size() > 0){
            Intent intent = new Intent(getApplicationContext(), ShowConversationActivity.class);
            Bundle b = new Bundle();
            b.putString("statusId", statuses.get(0).getId());
            intent.putExtras(b);
            startActivity(intent);
        }
    }

    @Override
    public void onRetrieveEmojiAccount(Account account) {
        TextView displayedName = headerLayout.findViewById(R.id.displayedName);
        displayedName.setText(account.getdisplayNameSpan(), TextView.BufferType.SPANNABLE);
    }

    @Override
    public void onActionDone(ManageFiltersAsyncTask.action actionType, APIResponse apiResponse, int statusCode) {
        if( apiResponse != null && apiResponse.getFilters() != null && apiResponse.getFilters().size() > 0){
            filters = apiResponse.getFilters();
        }
    }

    @Override
    public void onActionDone(ManageListsAsyncTask.action actionType, APIResponse apiResponse, int statusCode) {
        if( apiResponse.getError() != null){
            return;
        }
        if( actionType == ManageListsAsyncTask.action.GET_LIST && popup != null) {
            if (apiResponse.getLists() != null && apiResponse.getLists().size() > 0) {
                SubMenu submList = popup.getMenu().findItem(R.id.action_show_list).getSubMenu();
                int l = 0;
                for (fr.gouv.etalab.mastodon.client.Entities.List list : apiResponse.getLists()) {
                    MenuItem itemPlaceHolder = submList.findItem(R.id.list_instances);
                    if( itemPlaceHolder != null)
                        itemPlaceHolder.setVisible(false);
                    MenuItem item = submList.add(0, l, Menu.NONE, list.getTitle());
                    item.setIcon(R.drawable.ic_list_instance);
                    item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            Intent intent = new Intent(BaseMainActivity.this, ListActivity.class);
                            Bundle b = new Bundle();
                            b.putString("id", list.getId());
                            b.putString("title", list.getTitle());
                            intent.putExtras(b);
                            startActivity(intent);
                            return false;
                        }
                    });
                    l++;
                }
            }
        }
    }



    public static HashMap<String, DisplayStatusFragment> tagFragment = new HashMap<>();

    /**
     * Page Adapter for Mastodon & Peertube
     */
    public class PagerAdapter extends FragmentStatePagerAdapter  {
        int mNumOfTabs;

        private PagerAdapter(FragmentManager fm, int NumOfTabs) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
        }

        public void removeTabPage() {
            if( social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON) {
                this.mNumOfTabs--;
                notifyDataSetChanged();
            }
        }

        public void addTabPage(String title) {
            if( social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON) {
                TabLayout.Tab tab = tabLayout.newTab();
                tab.setText(title);
                this.mNumOfTabs++;
                notifyDataSetChanged();
            }
        }

        @Override
        public Fragment getItem(int position) {
            if( social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON) {
                //Remove the search bar
                if (!toolbar_search.isIconified()) {
                    toolbarTitle.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.VISIBLE);
                    toolbar_search.setIconified(true);
                }
                //Selection comes from another menu, no action to do
                DisplayStatusFragment statusFragment;
                Bundle bundle = new Bundle();
                if (position == 0) {
                    homeFragment = new DisplayStatusFragment();
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.HOME);
                    homeFragment.setArguments(bundle);
                    return homeFragment;
                } else if (position == 1) {
                    notificationsFragment = new DisplayNotificationsFragment();
                    return notificationsFragment;
                } else {
                    statusFragment = new DisplayStatusFragment();
                    bundle.putSerializable("type", typePosition.get(position));
                    if (typePosition.get(position) == RetrieveFeedsAsyncTask.Type.TAG) {
                        if (tabLayout.getTabAt(position) != null && tabLayout.getTabAt(position).getText() != null) {

                            SQLiteDatabase db = Sqlite.getInstance(BaseMainActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();

                            List<TagTimeline> tagTimelines = new SearchDAO(BaseMainActivity.this, db).getTabInfo(tabLayout.getTabAt(position).getText().toString());
                            String tag;
                            if (tagTimelines == null || tagTimelines.size() == 0)
                                tag = tabLayout.getTabAt(position).getText().toString();
                            else
                                tag = tagTimelines.get(0).getName();

                            bundle.putString("tag", tag);
                            tagFragment.put(tag, statusFragment);
                        }
                    }
                    statusFragment.setArguments(bundle);
                    return statusFragment;
                }
            }else if (social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE){
                //Remove the search bar
                if( !toolbar_search.isIconified() ) {
                    toolbarTitle.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.VISIBLE);
                    toolbar_search.setIconified(true);
                }
                //Selection comes from another menu, no action to do
                Bundle bundle = new Bundle();
                DisplayStatusFragment fragment = new DisplayStatusFragment();
                if (position == 0) {
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.PSUBSCRIPTIONS);
                }else if( position == 1) {
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.POVERVIEW);
                }else if( position == 2) {
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.PTRENDING);
                }else if( position == 3) {
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.PRECENTLYADDED);
                }else if( position == 4) {
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.PLOCAL);
                }
                bundle.putString("instanceType","PEERTUBE");
                fragment.setArguments(bundle);
                return fragment;
            }
            return null;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, createdFragment);
            if( social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON) {

                // save the appropriate reference depending on position
                if (position == 0) {
                    homeFragment = (DisplayStatusFragment) createdFragment;
                } else if (position == 1) {
                    notificationsFragment = (DisplayNotificationsFragment) createdFragment;
                } else {
                    if (display_local && position == tabPosition.get("local"))
                        localFragment = (DisplayStatusFragment) createdFragment;
                    else if (display_global && position == tabPosition.get("global"))
                        federatedFragment = (DisplayStatusFragment) createdFragment;
                    else if (display_art && position == tabPosition.get("art"))
                        artFragment = (DisplayStatusFragment) createdFragment;

                }

            }
            return createdFragment;
        }
        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }
        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }


    private void attacheDelete(int position){
        LinearLayout tabStrip = (LinearLayout) tabLayout.getChildAt(0);
        tabStrip.getChildAt(position).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String tabName = tabLayout.getTabAt(position).getText().toString().trim();

                SQLiteDatabase db = Sqlite.getInstance(BaseMainActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();

                List<TagTimeline> tagTimelines = new SearchDAO(BaseMainActivity.this, db).getTabInfo(tabName);
                String tag;
                if( tagTimelines == null || tagTimelines.size() == 0) {
                    tag = tabName;
                    if(tagTimelines == null)
                        tagTimelines = new SearchDAO(BaseMainActivity.this, db).getTimelineInfo(tag);
                }else
                    tag = tagTimelines.get(0).getName();
                PopupMenu popup = new PopupMenu(BaseMainActivity.this, tabStrip.getChildAt(position));
                popup.getMenuInflater()
                        .inflate(R.menu.option_tag_timeline, popup.getMenu());
                Menu menu = popup.getMenu();


                final MenuItem itemMediaOnly = menu.findItem(R.id.action_show_media_only);
                final MenuItem itemShowNSFW = menu.findItem(R.id.action_show_nsfw);


                final boolean[] changes = {false};
                final boolean[] mediaOnly = {false};
                final boolean[] showNSFW = {false};
                if( tagTimelines != null && tagTimelines.size() > 0 ) {
                    mediaOnly[0] = tagTimelines.get(0).isART();
                    showNSFW[0] = tagTimelines.get(0).isNSFW();
                }
                itemMediaOnly.setChecked(mediaOnly[0]);
                itemShowNSFW.setChecked(showNSFW[0]);
                popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu menu) {
                        if(changes[0]) {
                            FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
                            fragTransaction.detach(tagFragment.get(tag));
                            fragTransaction.attach(tagFragment.get(tag));
                            fragTransaction.commit();
                        }
                    }
                });

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                        item.setActionView(new View(getApplicationContext()));
                        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                            @Override
                            public boolean onMenuItemActionExpand(MenuItem item) {
                                return false;
                            }

                            @Override
                            public boolean onMenuItemActionCollapse(MenuItem item) {
                                return false;
                            }
                        });
                        changes[0] = true;
                        switch (item.getItemId()) {
                            case R.id.action_show_media_only:
                                TagTimeline tagTimeline = new TagTimeline();
                                mediaOnly[0] =!mediaOnly[0];
                                tagTimeline.setName(tag.trim());
                                tagTimeline.setART(mediaOnly[0]);
                                tagTimeline.setNSFW(showNSFW[0]);
                                itemMediaOnly.setChecked(mediaOnly[0]);
                                new SearchDAO(BaseMainActivity.this, db).updateSearch(tagTimeline, null,null, null, null);
                                break;
                            case R.id.action_show_nsfw:
                                showNSFW[0] = !showNSFW[0];
                                tagTimeline = new TagTimeline();
                                tagTimeline.setName(tag.trim());
                                tagTimeline.setART(mediaOnly[0]);
                                tagTimeline.setNSFW(showNSFW[0]);
                                itemShowNSFW.setChecked(showNSFW[0]);
                                new SearchDAO(BaseMainActivity.this, db).updateSearch(tagTimeline, null,null, null, null);
                                break;
                            case R.id.action_any:
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(BaseMainActivity.this, style);
                                LayoutInflater inflater = getLayoutInflater();
                                @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.tags_any, null);
                                dialogBuilder.setView(dialogView);
                                final EditText editText = dialogView.findViewById(R.id.filter_any);
                                List<TagTimeline> tagInfo = new SearchDAO(BaseMainActivity.this, db).getTimelineInfo(tag);
                                if( tagInfo != null && tagInfo.size() > 0 && tagInfo.get(0).getAny() != null) {
                                    String valuesTag = "";
                                    for(String val: tagInfo.get(0).getAny())
                                        valuesTag += val+" ";
                                    editText.setText(valuesTag);
                                    editText.setSelection(editText.getText().toString().length());
                                }

                                tagTimeline = new TagTimeline();
                                tagTimeline.setName(tag.trim());
                                tagTimeline.setART(mediaOnly[0]);
                                tagTimeline.setNSFW(showNSFW[0]);

                                dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        String[] values = editText.getText().toString().trim().split("\\s+");
                                        List<String> any =
                                                new ArrayList<>(Arrays.asList(values));
                                        new SearchDAO(BaseMainActivity.this, db).updateSearch(tagTimeline,null, any, null, null);
                                    }
                                });
                                AlertDialog alertDialog = dialogBuilder.create();
                                alertDialog.show();
                                break;
                            case R.id.action_all:
                                dialogBuilder = new AlertDialog.Builder(BaseMainActivity.this, style);
                                inflater = getLayoutInflater();
                                dialogView = inflater.inflate(R.layout.tags_all, null);
                                dialogBuilder.setView(dialogView);
                                final EditText editTextAll = dialogView.findViewById(R.id.filter_all);
                                tagInfo = new SearchDAO(BaseMainActivity.this, db).getTimelineInfo(tag);
                                if( tagInfo != null && tagInfo.size() > 0 && tagInfo.get(0).getAll() != null) {
                                    String valuesTag = "";
                                    for(String val: tagInfo.get(0).getAll())
                                        valuesTag += val+" ";
                                    editTextAll.setText(valuesTag);
                                    editTextAll.setSelection(editTextAll.getText().toString().length());
                                }
                                tagTimeline = new TagTimeline();
                                tagTimeline.setName(tag.trim());
                                tagTimeline.setART(mediaOnly[0]);
                                tagTimeline.setNSFW(showNSFW[0]);

                                dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        String[] values = editTextAll.getText().toString().trim().split("\\s+");
                                        List<String> all =
                                                new ArrayList<>(Arrays.asList(values));
                                        new SearchDAO(BaseMainActivity.this, db).updateSearch(tagTimeline, null,null, all, null);
                                    }
                                });
                                alertDialog = dialogBuilder.create();
                                alertDialog.show();
                                break;
                            case R.id.action_none:
                                dialogBuilder = new AlertDialog.Builder(BaseMainActivity.this, style);
                                inflater = getLayoutInflater();
                                dialogView = inflater.inflate(R.layout.tags_all, null);
                                dialogBuilder.setView(dialogView);
                                final EditText editTextNone = dialogView.findViewById(R.id.filter_all);
                                tagInfo = new SearchDAO(BaseMainActivity.this, db).getTimelineInfo(tag);
                                if( tagInfo != null && tagInfo.size() > 0 && tagInfo.get(0).getNone() != null) {
                                    String valuesTag = "";
                                    for(String val: tagInfo.get(0).getNone())
                                        valuesTag += val+" ";
                                    editTextNone.setText(valuesTag);
                                    editTextNone.setSelection(editTextNone.getText().toString().length());
                                }
                                tagTimeline = new TagTimeline();
                                tagTimeline.setName(tag.trim());
                                tagTimeline.setART(mediaOnly[0]);
                                tagTimeline.setNSFW(showNSFW[0]);

                                dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        String[] values = editTextNone.getText().toString().trim().split("\\s+");
                                        List<String> none =
                                                new ArrayList<>(Arrays.asList(values));
                                        new SearchDAO(BaseMainActivity.this, db).updateSearch(tagTimeline, null,null, null, none);
                                    }
                                });
                                alertDialog = dialogBuilder.create();
                                alertDialog.show();
                                break;
                            case R.id.action_displayname:
                                dialogBuilder = new AlertDialog.Builder(BaseMainActivity.this, style);
                                inflater = getLayoutInflater();
                                dialogView = inflater.inflate(R.layout.tags_name, null);
                                dialogBuilder.setView(dialogView);
                                final EditText editTextName = dialogView.findViewById(R.id.column_name);
                                tagInfo = new SearchDAO(BaseMainActivity.this, db).getTimelineInfo(tag);
                                if( tagInfo != null && tagInfo.size() > 0 && tagInfo.get(0).getDisplayname() != null) {
                                    editTextName.setText(tagInfo.get(0).getDisplayname());
                                    editTextName.setSelection(editTextName.getText().toString().length());
                                }
                                tagTimeline = new TagTimeline();
                                tagTimeline.setName(tag.trim());
                                tagTimeline.setART(mediaOnly[0]);
                                tagTimeline.setNSFW(showNSFW[0]);
                                dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        String values = editTextName.getText().toString();
                                        if( values.trim().length() == 0)
                                            values = tag;
                                        if( tabLayout.getTabAt(position) != null)
                                            tabLayout.getTabAt(position).setText(values);
                                        new SearchDAO(BaseMainActivity.this, db).updateSearch(tagTimeline, values,null, null, null);
                                    }
                                });
                                alertDialog = dialogBuilder.create();
                                alertDialog.show();
                                break;
                            case R.id.action_delete:
                                dialogBuilder = new AlertDialog.Builder(BaseMainActivity.this, style);
                                dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        new SearchDAO(BaseMainActivity.this, db).remove(tag);
                                        String tag;
                                        if( position > 0 && tabLayout.getTabAt(position - 1).getText() != null) {
                                            tag = tabLayout.getTabAt(position - 1).getText().toString();
                                        }else if( tabLayout.getTabCount() > 1 && tabLayout.getTabAt(1).getText() != null) {
                                            tag = tabLayout.getTabAt(1).getText().toString();
                                        }else //Last element
                                            tag = "";
                                        Helper.removeTab(tabLayout, adapter, position);
                                        adapter = new BaseMainActivity.PagerAdapter
                                                (getSupportFragmentManager(), tabLayout.getTabCount());
                                        viewPager.setAdapter(adapter);
                                        for(int i = 0; i < tabLayout.getTabCount() ; i++ ){
                                            if( tabLayout.getTabAt(i).getText() != null && tabLayout.getTabAt(i).getText().equals(tag.trim())){
                                                tabLayout.getTabAt(i).select();
                                                break;
                                            }

                                        }
                                    }
                                });
                                dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                                dialogBuilder.setMessage(getString(R.string.delete) + ": " + tag);
                                alertDialog = dialogBuilder.create();
                                alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
                                        //Hide keyboard
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        assert imm != null;
                                        imm.hideSoftInputFromWindow(viewPager.getWindowToken(), 0);
                                    }
                                });
                                if( alertDialog.getWindow() != null )
                                    alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                                alertDialog.show();
                                return false;
                        }
                        return false;
                    }
                });
                popup.show();
                return false;
            }
        });

    }

    public void updateHomeCounter(){
        if( tabLayout.getTabAt(0) == null )
            return;
        //noinspection ConstantConditions
        View tabHome = tabLayout.getTabAt(0).getCustomView();
        if( tabHome == null)
            return;
        TextView tabCounterHome = tabHome.findViewById(R.id.tab_counter);
        tabCounterHome.setText(String.valueOf(countNewStatus));
        if( countNewStatus> 0){
            //New data are available
            //The fragment is not displayed, so the counter is displayed
            tabCounterHome.setVisibility(View.VISIBLE);
        }else {
            tabCounterHome.setVisibility(View.GONE);
        }
    }


    public void updateTimeLine(RetrieveFeedsAsyncTask.Type type, int value){

        if( type == RetrieveFeedsAsyncTask.Type.DIRECT || type == RetrieveFeedsAsyncTask.Type.CONVERSATION){
            if (tabLayout.getTabAt(2) != null && display_direct) {
                View tabDirect = tabLayout.getTabAt(2).getCustomView();
                assert tabDirect != null;
                TextView tabCounterDirect = tabDirect.findViewById(R.id.tab_counter);
                tabCounterDirect.setText(String.valueOf(value));
                if (value > 0) {
                    tabCounterDirect.setVisibility(View.VISIBLE);
                } else {
                    tabCounterDirect.setVisibility(View.GONE);
                }
            }
        }else if( type == RetrieveFeedsAsyncTask.Type.LOCAL ){
            if( display_local) {
                if (tabLayout.getTabAt(2) != null && !display_direct) {
                    View tabLocal = tabLayout.getTabAt(2).getCustomView();
                    assert tabLocal != null;
                    TextView tabCounterLocal = tabLocal.findViewById(R.id.tab_counter);
                    tabCounterLocal.setText(String.valueOf(value));
                    if (value > 0) {
                        tabCounterLocal.setVisibility(View.VISIBLE);
                    } else {
                        tabCounterLocal.setVisibility(View.GONE);
                    }
                } else if (tabLayout.getTabAt(3) != null ) {
                    View tabLocal = tabLayout.getTabAt(3).getCustomView();
                    assert tabLocal != null;
                    TextView tabCounterLocal = tabLocal.findViewById(R.id.tab_counter);
                    tabCounterLocal.setText(String.valueOf(value));
                    if (value > 0) {
                        tabCounterLocal.setVisibility(View.VISIBLE);
                    } else {
                        tabCounterLocal.setVisibility(View.GONE);
                    }
                }
            }
        }else if( type == RetrieveFeedsAsyncTask.Type.PUBLIC ){
            if( display_global){
                if( tabLayout.getTabAt(2) != null && !display_local && !display_direct){
                    View tabPublic = tabLayout.getTabAt(2).getCustomView();
                    assert tabPublic != null;
                    TextView tabCounterPublic = tabPublic.findViewById(R.id.tab_counter);
                    tabCounterPublic.setText(String.valueOf(value));
                    if( value > 0){
                        tabCounterPublic.setVisibility(View.VISIBLE);
                    }else {
                        tabCounterPublic.setVisibility(View.GONE);
                    }
                }else if( tabLayout.getTabAt(3) != null && ((!display_local && display_direct) || (display_local && !display_direct) )){
                    View tabPublic = tabLayout.getTabAt(3).getCustomView();
                    assert tabPublic != null;
                    TextView tabCounterPublic = tabPublic.findViewById(R.id.tab_counter);
                    tabCounterPublic.setText(String.valueOf(value));
                    if( value > 0){
                        tabCounterPublic.setVisibility(View.VISIBLE);
                    }else {
                        tabCounterPublic.setVisibility(View.GONE);
                    }
                }else if( tabLayout.getTabAt(4) != null && display_local && display_direct){
                    View tabPublic = tabLayout.getTabAt(4).getCustomView();
                    assert tabPublic != null;
                    TextView tabCounterPublic = tabPublic.findViewById(R.id.tab_counter);
                    tabCounterPublic.setText(String.valueOf(value));
                    if( value > 0){
                        tabCounterPublic.setVisibility(View.VISIBLE);
                    }else {
                        tabCounterPublic.setVisibility(View.GONE);
                    }
                }
            }
        }else if( type == RetrieveFeedsAsyncTask.Type.ART ){
            if( display_art){
                if( tabLayout.getTabAt(2) != null && !display_local && !display_direct && !display_global){
                    View tabArt = tabLayout.getTabAt(2).getCustomView();
                    assert tabArt != null;
                    TextView tabCounterArt = tabArt.findViewById(R.id.tab_counter);
                    tabCounterArt.setText(String.valueOf(value));
                    if( value > 0){
                        tabCounterArt.setVisibility(View.VISIBLE);
                    }else {
                        tabCounterArt.setVisibility(View.GONE);
                    }
                }else if( tabLayout.getTabAt(3) != null && (
                        (!display_local && !display_direct && display_global) ||
                        (!display_global && !display_direct && display_local) ||
                        (!display_global && !display_local && display_direct)
                        )){
                    View tabArt = tabLayout.getTabAt(3).getCustomView();
                    assert tabArt != null;
                    TextView tabCounterArt = tabArt.findViewById(R.id.tab_counter);
                    tabCounterArt.setText(String.valueOf(value));
                    if( value > 0){
                        tabCounterArt.setVisibility(View.VISIBLE);
                    }else {
                        tabCounterArt.setVisibility(View.GONE);
                    }
                }else if( tabLayout.getTabAt(4) != null && (
                        (!display_direct && display_local && display_global) ||
                        (!display_local && display_direct && display_global) ||
                        (!display_global && display_local && display_direct)
                )){
                    View tabArt = tabLayout.getTabAt(4).getCustomView();
                    assert tabArt != null;
                    TextView tabCounterArt = tabArt.findViewById(R.id.tab_counter);
                    tabCounterArt.setText(String.valueOf(value));
                    if( value > 0){
                        tabCounterArt.setVisibility(View.VISIBLE);
                    }else {
                        tabCounterArt.setVisibility(View.GONE);
                    }
                }else if( tabLayout.getTabAt(5) != null && display_local && display_direct && display_global){
                    View tabArt = tabLayout.getTabAt(5).getCustomView();
                    assert tabArt != null;
                    TextView tabCounterArt = tabArt.findViewById(R.id.tab_counter);
                    tabCounterArt.setText(String.valueOf(value));
                    if( value > 0){
                        tabCounterArt.setVisibility(View.VISIBLE);
                    }else {
                        tabCounterArt.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    public void updateNotifCounter(){
        if(tabLayout.getTabAt(1) == null)
            return;
        //noinspection ConstantConditions
        View tabNotif = tabLayout.getTabAt(1).getCustomView();
        if( tabNotif == null)
            return;
        TextView tabCounterNotif = tabNotif.findViewById(R.id.tab_counter);
        tabCounterNotif.setText(String.valueOf(countNewNotifications));
        if( countNewNotifications > 0){
            tabCounterNotif.setVisibility(View.VISIBLE);
        }else {
            tabCounterNotif.setVisibility(View.GONE);
        }
    }


    public void startSreaming(){
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        boolean liveNotifications = sharedpreferences.getBoolean(Helper.SET_LIVE_NOTIFICATIONS, true);
        if( liveNotifications) {
            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            assert manager != null;
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (LiveNotificationService.class.getName().equals(service.service.getClassName())) {
                    return;
                }
            }
            Intent streamingIntent = new Intent(this, LiveNotificationService.class);
            startService(streamingIntent);
        }

    }

    public void manageFloatingButton(boolean display){
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        boolean displayFollowInstance = sharedpreferences.getBoolean(Helper.SET_DISPLAY_FOLLOW_INSTANCE, true);
        if( social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON) {
            if (display) {
                tootShow();
                if (!displayFollowInstance)
                    federatedTimelines.hide();
                else
                    federatedTimelinesShow();
            } else {
                toot.hide();
                federatedTimelines.hide();
            }
        }else {
            toot.hide();
            federatedTimelines.hide();
        }
    }
    public void tootShow(){
        if( social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON) {
            toot.show();
        }else{
            toot.hide();
        }
    }
    public void federatedTimelinesShow(){
        if( social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON) {
            federatedTimelines.show();
        }else{
            federatedTimelines.hide();
        }
    }



    public boolean getFloatingVisibility(){
        return toot.getVisibility() == View.VISIBLE;
    }

    public void refreshButton(){
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        FloatingActionButton federatedTimelines = findViewById(R.id.federated_timeline);
        boolean displayFollowInstance = sharedpreferences.getBoolean(Helper.SET_DISPLAY_FOLLOW_INSTANCE, true);
        if( !displayFollowInstance)
            federatedTimelines.hide();
        else
            federatedTimelinesShow();
    }
    public DisplayStatusFragment getHomeFragment(){
        return homeFragment;
    }



    private void displayFollowInstances(){

        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        SQLiteDatabase db = Sqlite.getInstance(BaseMainActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        federatedTimelines = findViewById(R.id.federated_timeline);

        federatedTimelinesShow();
        delete_instance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    String title = toolbarTitle.getText().toString();
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(BaseMainActivity.this, style);
                    dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            new InstancesDAO(BaseMainActivity.this, db).remove(instance_id);
                            BaseMainActivity.this.onBackPressed();
                        }
                    });
                    dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    dialogBuilder.setTitle(R.string.delete_instance);
                    dialogBuilder.setMessage(getString(R.string.warning_delete_instance, title));
                    AlertDialog alertDialog = dialogBuilder.create();
                    alertDialog.show();

                }catch (Exception e){
                    Toasty.error(BaseMainActivity.this, getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                }
            }
        });

        boolean displayFollowInstance = sharedpreferences.getBoolean(Helper.SET_DISPLAY_FOLLOW_INSTANCE, true);
        if( !displayFollowInstance)
            federatedTimelines.hide();
        federatedTimelines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON)
                    new ManageListsAsyncTask(BaseMainActivity.this, ManageListsAsyncTask.action.GET_LIST, null, null, null, null, BaseMainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                SQLiteDatabase db = Sqlite.getInstance(BaseMainActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                new InstancesDAO(BaseMainActivity.this, db).cleanDoublon();
                List<RemoteInstance> remoteInstances = new InstancesDAO(BaseMainActivity.this, db).getAllInstances();
                popup = new PopupMenu(BaseMainActivity.this, federatedTimelines);
                popup.getMenuInflater()
                        .inflate(R.menu.remote_instances, popup.getMenu());
                try {
                    @SuppressLint("PrivateApi") Method method = popup.getMenu().getClass().getDeclaredMethod("setOptionalIconsVisible", boolean.class);
                    method.setAccessible(true);
                    method.invoke(popup.getMenu(), true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(remoteInstances != null) {
                    SubMenu submMastodon = popup.getMenu().findItem(R.id.action_show_mastodon).getSubMenu();
                    SubMenu submPeertube = popup.getMenu().findItem(R.id.action_show_peertube).getSubMenu();
                    SubMenu submPixelfed = popup.getMenu().findItem(R.id.action_show_pixelfed).getSubMenu();
                    SubMenu submMisskey = popup.getMenu().findItem(R.id.action_show_misskey).getSubMenu();
                    SubMenu submChannel = popup.getMenu().findItem(R.id.action_show_channel).getSubMenu();
                    int i = 0, j = 0 , k = 0, l = 0 , m = 0;
                    for (RemoteInstance remoteInstance : remoteInstances) {
                        if (remoteInstance.getType() == null || remoteInstance.getType().equals("MASTODON")) {
                            MenuItem itemPlaceHolder = submMastodon.findItem(R.id.mastodon_instances);
                            if( itemPlaceHolder != null)
                                itemPlaceHolder.setVisible(false);
                            MenuItem item = submMastodon.add(0, i, Menu.NONE, remoteInstance.getHost());
                            item.setIcon(R.drawable.mastodon_icon_item);
                            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    DisplayStatusFragment statusFragment;
                                    Bundle bundle = new Bundle();
                                    statusFragment = new DisplayStatusFragment();
                                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE);
                                    bundle.putString("remote_instance", remoteInstance.getHost());
                                    statusFragment.setArguments(bundle);
                                    String fragmentTag = "REMOTE_INSTANCE";
                                    instance_id = remoteInstance.getDbID();
                                    FragmentManager fragmentManager = getSupportFragmentManager();
                                    fragmentManager.beginTransaction()
                                            .replace(R.id.main_app_container, statusFragment, fragmentTag).commit();
                                    main_app_container.setVisibility(View.VISIBLE);
                                    viewPager.setVisibility(View.GONE);
                                    tabLayout.setVisibility(View.GONE);
                                    toolbarTitle.setVisibility(View.VISIBLE);
                                    delete_instance.setVisibility(View.VISIBLE);
                                    toolbarTitle.setText(remoteInstance.getHost());
                                    return false;
                                }
                            });
                            i++;
                        }
                        if (remoteInstance.getType() == null || remoteInstance.getType().equals("PEERTUBE_CHANNEL")) {
                            MenuItem itemPlaceHolder = submChannel.findItem(R.id.channel_instances);
                            if( itemPlaceHolder != null)
                                itemPlaceHolder.setVisible(false);
                            MenuItem item = submChannel.add(0, k, Menu.NONE, remoteInstance.getId() + " - " +remoteInstance.getHost());
                            item.setIcon(R.drawable.ic_list_instance);
                            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    DisplayStatusFragment statusFragment;
                                    Bundle bundle = new Bundle();
                                    statusFragment = new DisplayStatusFragment();
                                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE);
                                    bundle.putString("remote_instance", remoteInstance.getHost());
                                    bundle.putString("remote_channel_name", remoteInstance.getId());
                                    statusFragment.setArguments(bundle);
                                    instance_id = remoteInstance.getDbID();
                                    String fragmentTag = "REMOTE_INSTANCE";
                                    FragmentManager fragmentManager = getSupportFragmentManager();
                                    fragmentManager.beginTransaction()
                                            .replace(R.id.main_app_container, statusFragment, fragmentTag).commit();
                                    main_app_container.setVisibility(View.VISIBLE);
                                    viewPager.setVisibility(View.GONE);
                                    tabLayout.setVisibility(View.GONE);
                                    toolbarTitle.setVisibility(View.VISIBLE);
                                    delete_instance.setVisibility(View.VISIBLE);
                                    toolbarTitle.setText(remoteInstance.getHost());
                                    return false;
                                }
                            });
                            k++;
                        }
                        if (remoteInstance.getType() == null || remoteInstance.getType().equals("PIXELFED")) {
                            MenuItem itemPlaceHolder = submPixelfed.findItem(R.id.pixelfed_instance);
                            if( itemPlaceHolder != null)
                                itemPlaceHolder.setVisible(false);
                            MenuItem item = submPixelfed.add(0, j, Menu.NONE, remoteInstance.getHost());
                            item.setIcon(R.drawable.pixelfed);
                            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    DisplayStatusFragment statusFragment;
                                    Bundle bundle = new Bundle();
                                    statusFragment = new DisplayStatusFragment();
                                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.PIXELFED);
                                    bundle.putString("remote_instance", remoteInstance.getHost());
                                    statusFragment.setArguments(bundle);
                                    String fragmentTag = "REMOTE_INSTANCE";
                                    instance_id = remoteInstance.getDbID();
                                    FragmentManager fragmentManager = getSupportFragmentManager();
                                    fragmentManager.beginTransaction()
                                            .replace(R.id.main_app_container, statusFragment, fragmentTag).commit();
                                    main_app_container.setVisibility(View.VISIBLE);
                                    viewPager.setVisibility(View.GONE);
                                    tabLayout.setVisibility(View.GONE);
                                    toolbarTitle.setVisibility(View.VISIBLE);
                                    delete_instance.setVisibility(View.VISIBLE);
                                    toolbarTitle.setText(remoteInstance.getHost());
                                    return false;
                                }
                            });
                            j++;
                        }
                        if (remoteInstance.getType() == null || remoteInstance.getType().equals("MISSKEY")) {
                            MenuItem itemPlaceHolder = submMisskey.findItem(R.id.misskey_instance);
                            if( itemPlaceHolder != null)
                                itemPlaceHolder.setVisible(false);
                            MenuItem item = submMisskey.add(0, l, Menu.NONE, remoteInstance.getHost());
                            item.setIcon(R.drawable.misskey);
                            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    DisplayStatusFragment statusFragment;
                                    Bundle bundle = new Bundle();
                                    statusFragment = new DisplayStatusFragment();
                                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE);
                                    bundle.putString("remote_instance", remoteInstance.getHost());
                                    statusFragment.setArguments(bundle);
                                    String fragmentTag = "REMOTE_INSTANCE";
                                    instance_id = remoteInstance.getDbID();
                                    FragmentManager fragmentManager = getSupportFragmentManager();
                                    fragmentManager.beginTransaction()
                                            .replace(R.id.main_app_container, statusFragment, fragmentTag).commit();
                                    main_app_container.setVisibility(View.VISIBLE);
                                    viewPager.setVisibility(View.GONE);
                                    tabLayout.setVisibility(View.GONE);
                                    toolbarTitle.setVisibility(View.VISIBLE);
                                    delete_instance.setVisibility(View.VISIBLE);
                                    toolbarTitle.setText(remoteInstance.getHost());
                                    return false;
                                }
                            });
                            l++;
                        }
                        if (remoteInstance.getType() == null || remoteInstance.getType().equals("PEERTUBE")) {
                            MenuItem itemPlaceHolder = submPeertube.findItem(R.id.peertube_instances);
                            if( itemPlaceHolder != null)
                                itemPlaceHolder.setVisible(false);
                            MenuItem item = submPeertube.add(0, m, Menu.NONE, remoteInstance.getHost());
                            item.setIcon(R.drawable.peertube_icon);
                            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    DisplayStatusFragment statusFragment;
                                    Bundle bundle = new Bundle();
                                    statusFragment = new DisplayStatusFragment();
                                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE);
                                    bundle.putString("remote_instance", remoteInstance.getHost());
                                    statusFragment.setArguments(bundle);
                                    String fragmentTag = "REMOTE_INSTANCE";
                                    instance_id = remoteInstance.getDbID();
                                    FragmentManager fragmentManager = getSupportFragmentManager();
                                    fragmentManager.beginTransaction()
                                            .replace(R.id.main_app_container, statusFragment, fragmentTag).commit();
                                    main_app_container.setVisibility(View.VISIBLE);
                                    viewPager.setVisibility(View.GONE);
                                    tabLayout.setVisibility(View.GONE);
                                    toolbarTitle.setVisibility(View.VISIBLE);
                                    delete_instance.setVisibility(View.VISIBLE);
                                    toolbarTitle.setText(remoteInstance.getHost());
                                    return false;
                                }
                            });
                            m++;
                        }
                    }
                }
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_add_instance:
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(BaseMainActivity.this, style);
                                LayoutInflater inflater = getLayoutInflater();
                                @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.search_instance, null);
                                dialogBuilder.setView(dialogView);

                                AutoCompleteTextView instance_list = dialogView.findViewById(R.id.search_instance);
                                //Manage download of attachments
                                RadioGroup radioGroup = dialogView.findViewById(R.id.set_attachment_group);

                                instance_list.setFilters(new InputFilter[]{new InputFilter.LengthFilter(60)});
                                dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                                        String instanceName = instance_list.getText().toString().trim();
                                        new Thread(new Runnable(){
                                            @Override
                                            public void run() {
                                                try {
                                                    if(radioGroup.getCheckedRadioButtonId() == R.id.mastodon_instance)
                                                        new HttpsConnection(BaseMainActivity.this).get("https://" + instanceName + "/api/v1/timelines/public?local=true", 10, null, null);
                                                    else  if( radioGroup.getCheckedRadioButtonId() == R.id.peertube_instance)
                                                        new HttpsConnection(BaseMainActivity.this).get("https://" + instanceName + "/api/v1/videos/", 10, null, null);
                                                    else  if( radioGroup.getCheckedRadioButtonId() == R.id.pixelfed_instance) {
                                                        new HttpsConnection(BaseMainActivity.this).get("https://" + instanceName + "/api/v1/timelines/public", 10, null, null);
                                                    }else  if( radioGroup.getCheckedRadioButtonId() == R.id.misskey_instance) {
                                                        new HttpsConnection(BaseMainActivity.this).post("https://" + instanceName + "/api/notes/local-timeline", 10, null, null);
                                                    }
                                                    runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            JSONObject resobj;
                                                            dialog.dismiss();
                                                            if(radioGroup.getCheckedRadioButtonId() == R.id.mastodon_instance)
                                                                new InstancesDAO(BaseMainActivity.this, db).insertInstance(instanceName, "MASTODON");
                                                            else  if( radioGroup.getCheckedRadioButtonId() == R.id.peertube_instance)
                                                                new InstancesDAO(BaseMainActivity.this, db).insertInstance(instanceName, "PEERTUBE");
                                                            else  if( radioGroup.getCheckedRadioButtonId() == R.id.pixelfed_instance)
                                                                new InstancesDAO(BaseMainActivity.this, db).insertInstance(instanceName, "PIXELFED");
                                                            else  if( radioGroup.getCheckedRadioButtonId() == R.id.misskey_instance)
                                                                new InstancesDAO(BaseMainActivity.this, db).insertInstance(instanceName, "MISSKEY");
                                                            DisplayStatusFragment statusFragment;
                                                            Bundle bundle = new Bundle();
                                                            statusFragment = new DisplayStatusFragment();
                                                            bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE);
                                                            bundle.putString("remote_instance", instanceName);
                                                            statusFragment.setArguments(bundle);
                                                            String fragmentTag = "REMOTE_INSTANCE";
                                                            FragmentManager fragmentManager = getSupportFragmentManager();
                                                            fragmentManager.beginTransaction()
                                                                    .replace(R.id.main_app_container, statusFragment, fragmentTag).commit();
                                                            main_app_container.setVisibility(View.VISIBLE);
                                                            viewPager.setVisibility(View.GONE);
                                                            delete_instance.setVisibility(View.VISIBLE);
                                                            tabLayout.setVisibility(View.GONE);
                                                            toolbarTitle.setVisibility(View.VISIBLE);
                                                            toolbarTitle.setText(instanceName);
                                                        }
                                                    });
                                                } catch (final Exception e) {
                                                    e.printStackTrace();
                                                    runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Toasty.warning(getApplicationContext(), getString(R.string.toast_instance_unavailable),Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                                }
                                            }
                                        }).start();
                                    }
                                });
                                AlertDialog alertDialog = dialogBuilder.create();
                                alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
                                        //Hide keyboard
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        assert imm != null;
                                        imm.hideSoftInputFromWindow(instance_list.getWindowToken(), 0);
                                    }
                                });
                                if( alertDialog.getWindow() != null )
                                    alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                                alertDialog.show();

                                instance_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
                                        oldSearch = parent.getItemAtPosition(position).toString().trim();
                                    }
                                });
                                instance_list.addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                    }

                                    @Override
                                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                                    }

                                    @Override
                                    public void afterTextChanged(Editable s) {
                                        Pattern host = Pattern.compile("([\\da-z\\.-]+\\.[a-z\\.]{2,12})");
                                        Matcher matcher = host.matcher(s.toString().trim());
                                        if( s.toString().trim().length() == 0 || !matcher.find()) {
                                            alertDialog.getButton(
                                                    AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                                        } else {
                                            // Something into edit text. Enable the button.
                                            alertDialog.getButton(
                                                    AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                        }
                                        if (s.length() > 2 && !isLoadingInstance) {
                                            final String action = "/instances/search";
                                            final HashMap<String, String> parameters = new HashMap<>();
                                            parameters.put("q", s.toString().trim());
                                            parameters.put("count", String.valueOf(1000));
                                            parameters.put("name", String.valueOf(true));
                                            isLoadingInstance = true;

                                            if( oldSearch == null || !oldSearch.equals(s.toString().trim()))
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        try {
                                                            final String response = new HttpsConnection(BaseMainActivity.this).get("https://instances.social/api/1.0" + action, 30, parameters, Helper.THEKINRAR_SECRET_TOKEN);
                                                            runOnUiThread(new Runnable() {
                                                                public void run() {
                                                                    isLoadingInstance = false;
                                                                    String[] instances;
                                                                    try {
                                                                        JSONObject jsonObject = new JSONObject(response);
                                                                        JSONArray jsonArray = jsonObject.getJSONArray("instances");
                                                                        if (jsonArray != null) {
                                                                            int length = 0;
                                                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                                                if( !jsonArray.getJSONObject(i).get("name").toString().contains("@") && jsonArray.getJSONObject(i).get("up").toString().equals("true"))
                                                                                    length++;
                                                                            }
                                                                            instances = new String[length];
                                                                            int j = 0;
                                                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                                                if( !jsonArray.getJSONObject(i).get("name").toString().contains("@") && jsonArray.getJSONObject(i).get("up").toString().equals("true")) {
                                                                                    instances[j] = jsonArray.getJSONObject(i).get("name").toString();
                                                                                    j++;
                                                                                }
                                                                            }
                                                                        } else {
                                                                            instances = new String[]{};
                                                                        }
                                                                        instance_list.setAdapter(null);
                                                                        ArrayAdapter<String> adapter =
                                                                                new ArrayAdapter<>(BaseMainActivity.this, android.R.layout.simple_list_item_1, instances);
                                                                        instance_list.setAdapter(adapter);
                                                                        if (instance_list.hasFocus() && !BaseMainActivity.this.isFinishing())
                                                                            instance_list.showDropDown();
                                                                        oldSearch = s.toString().trim();

                                                                    } catch (JSONException ignored) {
                                                                        isLoadingInstance = false;
                                                                    }
                                                                }
                                                            });

                                                        } catch (HttpsConnection.HttpsConnectionException e) {
                                                            isLoadingInstance = false;
                                                        } catch (Exception e) {
                                                            isLoadingInstance = false;
                                                        }
                                                    }
                                                }).start();
                                            else
                                                isLoadingInstance = false;
                                        }
                                    }
                                });
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

    }
}