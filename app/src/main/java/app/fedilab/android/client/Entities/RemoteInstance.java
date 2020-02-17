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
package app.fedilab.android.client.Entities;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Thomas on 05/10/2018.
 * Manages following instances
 */

public class RemoteInstance implements Parcelable {

    public static final Creator<RemoteInstance> CREATOR = new Creator<RemoteInstance>() {
        @Override
        public RemoteInstance createFromParcel(Parcel source) {
            return new RemoteInstance(source);
        }

        @Override
        public RemoteInstance[] newArray(int size) {
            return new RemoteInstance[size];
        }
    };
    private String host;
    private String type;
    private String id;
    private String dbID;
    private List<String> tags;
    private String filteredWith;


    public RemoteInstance() {
    }

    protected RemoteInstance(Parcel in) {
        this.host = in.readString();
        this.type = in.readString();
        this.id = in.readString();
        this.dbID = in.readString();
        this.tags = in.createStringArrayList();
        this.filteredWith = in.readString();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDbID() {
        return dbID;
    }

    public void setDbID(String dbID) {
        this.dbID = dbID;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getFilteredWith() {
        return filteredWith;
    }

    public void setFilteredWith(String filteredWith) {
        this.filteredWith = filteredWith;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.host);
        dest.writeString(this.type);
        dest.writeString(this.id);
        dest.writeString(this.dbID);
        dest.writeStringList(this.tags);
        dest.writeString(this.filteredWith);
    }
}
