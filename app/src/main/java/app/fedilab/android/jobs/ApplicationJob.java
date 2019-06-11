package app.fedilab.android.jobs;
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
import androidx.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.evernote.android.job.JobManager;

/**
 * Created by Thomas on 29/04/2017.
 * Notification job
 */

public class ApplicationJob implements JobCreator {
    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case NotificationsSyncJob.NOTIFICATION_REFRESH:
                return new NotificationsSyncJob();
            case ScheduledTootsSyncJob.SCHEDULED_TOOT:
                return new ScheduledTootsSyncJob();
            case ScheduledBoostsSyncJob.SCHEDULED_BOOST:
                return new ScheduledBoostsSyncJob();
            default:
                return null;
        }
    }

    @SuppressWarnings("unused")
    public static void cancelAllJob(String TAG){
        JobManager.instance().cancelAllForTag(TAG);
    }

    public static void cancelJob(int jobId) {
        JobManager.instance().cancel(jobId);
    }
}
