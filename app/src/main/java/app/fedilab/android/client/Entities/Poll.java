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
package app.fedilab.android.client.Entities;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.List;

public class Poll implements Parcelable {

    public static final Parcelable.Creator<Poll> CREATOR = new Parcelable.Creator<Poll>() {
        @Override
        public Poll createFromParcel(Parcel source) {
            return new Poll(source);
        }

        @Override
        public Poll[] newArray(int size) {
            return new Poll[size];
        }
    };
    private String id;
    private Date expires_at;
    private int expires_in;
    private boolean expired;
    private boolean multiple;
    private int votes_count;
    private int voters_count;
    private boolean voted;
    private List<PollOptions> optionsList;

    public Poll() {
    }

    protected Poll(Parcel in) {
        this.id = in.readString();
        long tmpExpires_at = in.readLong();
        this.expires_at = tmpExpires_at == -1 ? null : new Date(tmpExpires_at);
        this.expires_in = in.readInt();
        this.expired = in.readByte() != 0;
        this.multiple = in.readByte() != 0;
        this.votes_count = in.readInt();
        this.voted = in.readByte() != 0;
        this.optionsList = in.createTypedArrayList(PollOptions.CREATOR);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getExpires_at() {
        return expires_at;
    }

    public void setExpires_at(Date expires_at) {
        this.expires_at = expires_at;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public int getVotes_count() {
        return votes_count;
    }

    public void setVotes_count(int votes_count) {
        this.votes_count = votes_count;
    }

    public boolean isVoted() {
        return voted;
    }

    public void setVoted(boolean voted) {
        this.voted = voted;
    }

    public List<PollOptions> getOptionsList() {
        return optionsList;
    }

    public void setOptionsList(List<PollOptions> optionsList) {
        this.optionsList = optionsList;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeLong(this.expires_at != null ? this.expires_at.getTime() : -1);
        dest.writeInt(this.expires_in);
        dest.writeByte(this.expired ? (byte) 1 : (byte) 0);
        dest.writeByte(this.multiple ? (byte) 1 : (byte) 0);
        dest.writeInt(this.votes_count);
        dest.writeByte(this.voted ? (byte) 1 : (byte) 0);
        dest.writeTypedList(this.optionsList);
    }

    public int getVoters_count() {
        return voters_count;
    }

    public void setVoters_count(int voters_count) {
        this.voters_count = voters_count;
    }
}
