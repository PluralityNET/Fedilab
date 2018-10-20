/* Copyright 2018 Thomas Schneider
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
package fr.gouv.etalab.mastodon.client.Entities;

import java.util.Date;
import java.util.List;

/**
 * Created by Thomas on 29/09/2018.
 * Manage how to videos
 */
public class Peertube {

    private String id;
    private String uuid;
    private String name;
    private String description;
    private String thumbnailPath;
    private String previewPath;
    private String embedPath;
    private int view;
    private int like;
    private int dislike;
    private Date created_at;
    private int duration;
    private String instance;
    private Account account;
    private List<String> resolution;


    public Peertube() {
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getPreviewPath() {
        return previewPath;
    }

    public void setPreviewPath(String previewPath) {
        this.previewPath = previewPath;
    }

    public String getEmbedPath() {
        return embedPath;
    }

    public void setEmbedPath(String embedPath) {
        this.embedPath = embedPath;
    }


    public int getView() {
        return view;
    }

    public void setView(int view) {
        this.view = view;
    }

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public int getDislike() {
        return dislike;
    }

    public void setDislike(int dislike) {
        this.dislike = dislike;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getFileUrl(String resolution) {
        if( resolution == null)
            resolution = this.getResolution().get(0);
        if(resolution == null)
            return null;
        return "https://" + this.instance + "/static/webseed/" + getUuid()+ "-" + resolution + ".mp4";
    }


    public String getTorrentDownloadUrl(String resolution) {
        if( resolution == null)
            resolution = this.getResolution().get(0);
        if(resolution == null)
            return null;
        return "https://" + this.instance + "/download/torrents/" + getUuid()+ "-" + resolution + ".torrent";

    }
    public String getFileDownloadUrl(String resolution) {
        if( resolution == null)
            resolution = this.getResolution().get(0);
        if(resolution == null)
            return null;
        return "https://" + this.instance + "/download/videos/" + getUuid()+ "-" + resolution + ".mp4";
    }

    public List<String> getResolution() {
        return resolution;
    }

    public void setResolution(List<String> resolution) {
        this.resolution = resolution;
    }
}
