package app.fedilab.android.activities;
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
import android.content.SharedPreferences;
import android.os.StrictMode;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import androidx.preference.PreferenceManager;

import com.evernote.android.job.JobManager;
import com.franmontiel.localechanger.LocaleChanger;
import com.jaredrummler.cyanea.Cyanea;
import com.jaredrummler.cyanea.prefs.CyaneaTheme;

import net.gotev.uploadservice.UploadService;

import org.acra.ACRA;
import org.acra.annotation.AcraNotification;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.LimiterConfigurationBuilder;
import org.acra.config.MailSenderConfigurationBuilder;
import org.acra.data.StringFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import app.fedilab.android.BuildConfig;
import app.fedilab.android.R;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.jobs.ApplicationJob;
import app.fedilab.android.jobs.BackupNotificationsSyncJob;
import app.fedilab.android.jobs.BackupStatusesSyncJob;
import app.fedilab.android.jobs.NotificationsSyncJob;
import es.dmoral.toasty.Toasty;

import static app.fedilab.android.helper.Helper.initNetCipher;

/**
 * Created by Thomas on 29/04/2017.
 * Main application, jobs are launched here.
 */

@AcraNotification(
        resIcon = R.mipmap.ic_launcher_bubbles, resTitle = R.string.crash_title, resChannelName = R.string.set_crash_reports, resText = R.string.crash_message)

public class MainApplication extends MultiDexApplication {


    private static MainApplication app;

    public static MainApplication getApp() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        //System.setProperty("java.net.preferIPv4Stack" , "true");
        JobManager.create(this).addJobCreator(new ApplicationJob());
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);

        ApplicationJob.cancelAllJob(NotificationsSyncJob.NOTIFICATION_REFRESH);
        if (Helper.liveNotifType(getApplicationContext()) == Helper.NOTIF_NONE) {
            NotificationsSyncJob.schedule(false);
        }

        Cyanea.init(this, super.getResources());
        List<CyaneaTheme> list = CyaneaTheme.Companion.from(getAssets(), "themes/cyanea_themes.json");

        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if (theme == Helper.THEME_LIGHT) {
            list.get(0).apply(Cyanea.getInstance());
        } else if (theme == Helper.THEME_BLACK) {
            list.get(2).apply(Cyanea.getInstance());
        } else {
            list.get(1).apply(Cyanea.getInstance());
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int accent = prefs.getInt("theme_accent", -1);
        int primary = prefs.getInt("theme_primary", -1);
        int pref_color_background = prefs.getInt("pref_color_background", -1);
        boolean pref_color_navigation_bar = prefs.getBoolean("pref_color_navigation_bar", true);
        boolean pref_color_status_bar = prefs.getBoolean("pref_color_status_bar", true);
        Cyanea.Editor editor = Cyanea.getInstance().edit();
        if (primary != -1) {
            editor.primary(primary);
        }
        if (accent != -1) {
            editor.accent(accent);
        }
        if (pref_color_background != -1) {
            editor
                    .background(pref_color_background)
                    .backgroundLight(pref_color_background)
                    .backgroundDark(pref_color_background).apply();
        }
        editor.shouldTintStatusBar(pref_color_status_bar).apply();
        editor.shouldTintNavBar(pref_color_navigation_bar).apply();

        ApplicationJob.cancelAllJob(BackupStatusesSyncJob.BACKUP_SYNC);
        BackupStatusesSyncJob.schedule(false);
        ApplicationJob.cancelAllJob(BackupNotificationsSyncJob.BACKUP_NOTIFICATIONS_SYNC);
        BackupNotificationsSyncJob.schedule(false);


        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        try {
            List<Locale> SUPPORTED_LOCALES = new ArrayList<>();

            String defaultLocaleString = sharedpreferences.getString(Helper.SET_DEFAULT_LOCALE_NEW, null);
            if (defaultLocaleString != null) {
                Locale defaultLocale;
                if (defaultLocaleString.equals("zh-CN"))
                    defaultLocale = Locale.SIMPLIFIED_CHINESE;
                else if (defaultLocaleString.equals("zh-TW"))
                    defaultLocale = Locale.TRADITIONAL_CHINESE;
                else
                    defaultLocale = new Locale(defaultLocaleString);
                SUPPORTED_LOCALES.add(defaultLocale);
            } else {
                SUPPORTED_LOCALES.add(Locale.getDefault());
            }
            LocaleChanger.initialize(getApplicationContext(), SUPPORTED_LOCALES);
        } catch (Exception ignored) {
        }


        boolean send_crash_reports = sharedpreferences.getBoolean(Helper.SET_SEND_CRASH_REPORTS, false);
        if (send_crash_reports) {
            CoreConfigurationBuilder ACRABuilder = new CoreConfigurationBuilder(this);
            ACRABuilder.setBuildConfigClass(BuildConfig.class).setReportFormat(StringFormat.KEY_VALUE_LIST);
            int versionCode = BuildConfig.VERSION_CODE;
            ACRABuilder.getPluginConfigurationBuilder(MailSenderConfigurationBuilder.class).setReportAsFile(false).setMailTo("hello@fedilab.app").setSubject("[Fedilab] - Crash Report " + versionCode).setEnabled(true);
            ACRABuilder.getPluginConfigurationBuilder(LimiterConfigurationBuilder.class).setEnabled(true);
            ACRA.init(this, ACRABuilder);
        }


        //Initialize upload service
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
        initNetCipher(this);
        Toasty.Config.getInstance()
                .allowQueue(false)
                .apply();
        Toasty.Config.getInstance().apply();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(MainApplication.this);
    }
}
