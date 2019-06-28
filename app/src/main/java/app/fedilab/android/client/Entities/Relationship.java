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
package app.fedilab.android.client.Entities;

/**
 * Created by Thomas on 23/04/2017.
 * Manage relationship between the authenticated account and another account
 */

public class Relationship {

    private String id;
    private boolean following;
    private boolean followed_by;
    private boolean blocking;
    private boolean muting;
    private boolean requested;
    private boolean muting_notifications;
    private boolean endorsed;
    private boolean showing_reblogs;
    private boolean blocked_by;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isFollowing() {
        return following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
    }

    public boolean isFollowed_by() {
        return followed_by;
    }

    public void setFollowed_by(boolean followed_by) {
        this.followed_by = followed_by;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    public boolean isMuting() {
        return muting;
    }

    public void setMuting(boolean muting) {
        this.muting = muting;
    }

    public boolean isRequested() {
        return requested;
    }

    public void setRequested(boolean requested) {
        this.requested = requested;
    }

    public boolean isMuting_notifications() {
        return muting_notifications;
    }

    public void setMuting_notifications(boolean muting_notifications) {
        this.muting_notifications = muting_notifications;
    }

    public boolean isEndorsed() {
        return endorsed;
    }

    public void setEndorsed(boolean endorsed) {
        this.endorsed = endorsed;
    }

    public boolean isShowing_reblogs() {
        return showing_reblogs;
    }

    public void setShowing_reblogs(boolean showing_reblogs) {
        this.showing_reblogs = showing_reblogs;
    }

    public boolean isBlocked_by() {
        return blocked_by;
    }

    public void setBlocked_by(boolean blocked_by) {
        this.blocked_by = blocked_by;
    }
}
