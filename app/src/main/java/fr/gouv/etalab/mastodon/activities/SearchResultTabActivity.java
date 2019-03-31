/* Copyright 2019 Thomas Schneider
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveAccountsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveSearchAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.Entities.Results;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.drawers.SearchListAdapter;
import fr.gouv.etalab.mastodon.drawers.SearchTagsAdapter;
import fr.gouv.etalab.mastodon.fragments.DisplayAccountsFragment;
import fr.gouv.etalab.mastodon.fragments.DisplaySearchTagsFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayStatusFragment;
import fr.gouv.etalab.mastodon.fragments.TabLayoutTootsFragment;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveSearchInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveSearchStatusInterface;

import static fr.gouv.etalab.mastodon.helper.Helper.THEME_LIGHT;


/**
 * Created by Thomas on 31/03/2019.
 * Show search results within tabs
 */

public class SearchResultTabActivity extends BaseActivity implements OnRetrieveSearchInterface, OnRetrieveSearchStatusInterface {


    private String search;
    private ListView lv_search;
    private RelativeLayout loader;
    private TabLayout tabLayout;
    private ViewPager search_viewpager;
    private DisplayStatusFragment displayStatusFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme){
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme);
                break;
            case Helper.THEME_DARK:
                setTheme(R.style.AppThemeDark);
                break;
            case Helper.THEME_BLACK:
                setTheme(R.style.AppThemeBlack);
                break;
            default:
                setTheme(R.style.AppThemeDark);
        }

        setContentView(R.layout.activity_search_result_tabs);

        loader = findViewById(R.id.loader);
        lv_search = findViewById(R.id.lv_search);
        tabLayout = findViewById(R.id.search_tabLayout);
        search_viewpager = findViewById(R.id.search_viewpager);

        Bundle b = getIntent().getExtras();
        if(b != null){
            search = b.getString("search");
            if( search != null)
                new RetrieveSearchAsyncTask(getApplicationContext(), search.trim(), SearchResultTabActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else
                Toasty.error(this,getString(R.string.toast_error_search),Toast.LENGTH_LONG).show();
        }else{
            Toasty.error(this,getString(R.string.toast_error_search),Toast.LENGTH_LONG).show();
        }
        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            toolbar_title.setText(search);
            if (theme == THEME_LIGHT){
                Toolbar toolbar = actionBar.getCustomView().findViewById(R.id.toolbar);
                Helper.colorizeToolbar(toolbar, R.color.black, SearchResultTabActivity.this);
            }
        }
        setTitle(search);
        loader.setVisibility(View.VISIBLE);
        lv_search.setVisibility(View.GONE);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onRetrieveSearch(Results results, Error error) {
        loader.setVisibility(View.GONE);

        if( error != null){
            Toasty.error(getApplicationContext(), error.getError(),Toast.LENGTH_LONG).show();
            return;
        }
        if( results == null || (results.getAccounts().size() == 0 && results.getStatuses().size() == 0 && results.getHashtags().size() == 0)){
            RelativeLayout no_result = findViewById(R.id.no_result);
            no_result.setVisibility(View.VISIBLE);
            return;
        }
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tags)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.accounts)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.toots)));

        PagerAdapter mPagerAdapter = new SearchResultTabActivity.ScreenSlidePagerAdapter(getSupportFragmentManager());

        search_viewpager.setAdapter(mPagerAdapter);

        search_viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                TabLayout.Tab tab = tabLayout.getTabAt(position);
                if( tab != null)
                    tab.select();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                search_viewpager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Fragment fragment = null;
                if( search_viewpager.getAdapter() != null)
                    fragment = (Fragment) search_viewpager.getAdapter().instantiateItem(search_viewpager, tab.getPosition());
                if( fragment != null) {
                    if( fragment instanceof DisplayAccountsFragment) {
                        DisplayAccountsFragment displayAccountsFragment = ((DisplayAccountsFragment) fragment);
                        displayAccountsFragment.scrollToTop();
                    }else if (fragment instanceof DisplayStatusFragment){
                        DisplayStatusFragment displayStatusFragment = ((DisplayStatusFragment) fragment);
                        displayStatusFragment.scrollToTop();
                    }else if (fragment instanceof DisplaySearchTagsFragment){
                        DisplaySearchTagsFragment displaySearchTagsFragment = ((DisplaySearchTagsFragment) fragment);
                        displaySearchTagsFragment.scrollToTop();
                    }
                }
            }
        });

    }


    /**
     * Pager adapter for the 4 fragments
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            switch (position){
                case 0:
                    DisplaySearchTagsFragment displaySearchTagsFragment = new DisplaySearchTagsFragment();
                    bundle.putSerializable("tagsOnly", true);
                    displaySearchTagsFragment.setArguments(bundle);
                    return displaySearchTagsFragment;
                case 1:
                    DisplayAccountsFragment displayAccountsFragment = new DisplayAccountsFragment();
                    bundle.putSerializable("type", RetrieveAccountsAsyncTask.Type.SEARCH);
                    displayAccountsFragment.setArguments(bundle);
                    return displayAccountsFragment;
                case 2:
                    DisplayStatusFragment displayStatusFragment = new DisplayStatusFragment();
                    bundle = new Bundle();
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.SEARCH);
                    displayStatusFragment.setArguments(bundle);
                    return displayStatusFragment;


            }
            return null;
        }


        @Override
        public int getCount() {
            return 3;
        }
    }

    @Override
    public void onRetrieveSearchStatus(APIResponse apiResponse, Error error) {
        loader.setVisibility(View.GONE);
        if( apiResponse.getError() != null){
            Toasty.error(getApplicationContext(), error.getError(),Toast.LENGTH_LONG).show();
            return;
        }
        lv_search.setVisibility(View.VISIBLE);
        List<String> tags = new ArrayList<>();
        List<Account> accounts = new ArrayList<>();
        List<Status> statuses = apiResponse.getStatuses();

        SearchListAdapter searchListAdapter = new SearchListAdapter(SearchResultTabActivity.this, statuses, accounts, tags);
        lv_search.setAdapter(searchListAdapter);
        searchListAdapter.notifyDataSetChanged();
    }
}
