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
package app.fedilab.android.activities;


import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;

import app.fedilab.android.R;
import app.fedilab.android.asynctasks.RetrieveAccountsAsyncTask;
import app.fedilab.android.asynctasks.RetrieveFeedsAsyncTask;
import app.fedilab.android.fragments.DisplayAccountsFragment;
import app.fedilab.android.fragments.DisplaySearchTagsFragment;
import app.fedilab.android.fragments.DisplayStatusFragment;
import app.fedilab.android.helper.Helper;
import es.dmoral.toasty.Toasty;


/**
 * Created by Thomas on 31/03/2019.
 * Show search results within tabs
 */

public class SearchResultTabActivity extends BaseActivity {


    private String search;
    private TabLayout tabLayout;
    private ViewPager search_viewpager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme) {
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme_Fedilab);
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


        Bundle b = getIntent().getExtras();
        if (b != null) {
            search = b.getString("search");
            if (search == null)
                Toasty.error(getApplicationContext(), getString(R.string.toast_error_search), Toast.LENGTH_LONG).show();
        } else {
            Toasty.error(getApplicationContext(), getString(R.string.toast_error_search), Toast.LENGTH_LONG).show();
        }
        if (search == null)
            finish();

        tabLayout = findViewById(R.id.search_tabLayout);
        tabLayout.setBackgroundColor(ContextCompat.getColor(SearchResultTabActivity.this, R.color.cyanea_primary));
        search_viewpager = findViewById(R.id.search_viewpager);


        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View view = inflater.inflate(R.layout.simple_bar, new LinearLayout(getApplicationContext()), false);
            view.setBackground(new ColorDrawable(ContextCompat.getColor(SearchResultTabActivity.this, R.color.cyanea_primary)));
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
        }
        setTitle(search);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tags)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.accounts)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.toots)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.action_cache)));

        PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        search_viewpager.setAdapter(mPagerAdapter);

        search_viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                TabLayout.Tab tab = tabLayout.getTabAt(position);
                if (tab != null)
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
                Fragment fragment;
                if (search_viewpager.getAdapter() != null) {
                    fragment = (Fragment) search_viewpager.getAdapter().instantiateItem(search_viewpager, tab.getPosition());
                    if (fragment instanceof DisplayAccountsFragment) {
                        DisplayAccountsFragment displayAccountsFragment = ((DisplayAccountsFragment) fragment);
                        displayAccountsFragment.scrollToTop();
                    } else if (fragment instanceof DisplayStatusFragment) {
                        DisplayStatusFragment displayStatusFragment = ((DisplayStatusFragment) fragment);
                        displayStatusFragment.scrollToTop();
                    } else if (fragment instanceof DisplaySearchTagsFragment) {
                        DisplaySearchTagsFragment displaySearchTagsFragment = ((DisplaySearchTagsFragment) fragment);
                        displaySearchTagsFragment.scrollToTop();
                    }
                }
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            switch (position) {
                case 0:
                    DisplaySearchTagsFragment displaySearchTagsFragment = new DisplaySearchTagsFragment();
                    displaySearchTagsFragment.setArguments(bundle);
                    bundle.putSerializable("search", search);
                    return displaySearchTagsFragment;
                case 1:
                    DisplayAccountsFragment displayAccountsFragment = new DisplayAccountsFragment();
                    bundle.putSerializable("type", RetrieveAccountsAsyncTask.Type.SEARCH);
                    bundle.putSerializable("tag", search);
                    displayAccountsFragment.setArguments(bundle);
                    return displayAccountsFragment;
                case 2:
                    DisplayStatusFragment displayStatusFragment = new DisplayStatusFragment();
                    bundle = new Bundle();
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.SEARCH);
                    bundle.putSerializable("tag", search);
                    displayStatusFragment.setArguments(bundle);
                    return displayStatusFragment;
                case 3:
                    displayStatusFragment = new DisplayStatusFragment();
                    bundle = new Bundle();
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.SEARCH);
                    bundle.putSerializable("tag", search + "_cache_");
                    displayStatusFragment.setArguments(bundle);
                    return displayStatusFragment;
            }
            return null;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

        }
        @Override
        public int getCount() {
            return 4;
        }
    }
}
