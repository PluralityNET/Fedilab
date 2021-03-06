package app.fedilab.android.client;
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


import java.util.List;

import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.AccountAdmin;
import app.fedilab.android.client.Entities.Context;
import app.fedilab.android.client.Entities.Conversation;
import app.fedilab.android.client.Entities.Emojis;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.client.Entities.Filters;
import app.fedilab.android.client.Entities.HowToVideo;
import app.fedilab.android.client.Entities.IdentityProof;
import app.fedilab.android.client.Entities.Instance;
import app.fedilab.android.client.Entities.InstanceReg;
import app.fedilab.android.client.Entities.Notification;
import app.fedilab.android.client.Entities.Peertube;
import app.fedilab.android.client.Entities.PeertubeNotification;
import app.fedilab.android.client.Entities.PixelFedStory;
import app.fedilab.android.client.Entities.PixelFedStoryItem;
import app.fedilab.android.client.Entities.Playlist;
import app.fedilab.android.client.Entities.Relationship;
import app.fedilab.android.client.Entities.Report;
import app.fedilab.android.client.Entities.Results;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.client.Entities.StoredStatus;
import app.fedilab.android.client.Entities.Trends;

/**
 * Created by Thomas on 03/06/2017.
 * Hydrate response from the API
 */

public class APIResponse {

    private List<Account> accounts = null;
    private List<Status> statuses = null;
    private List<Conversation> conversations = null;
    private List<Notification> notifications = null;
    private List<Relationship> relationships = null;
    private String targetedId = null;
    private Results results = null;
    private List<HowToVideo> howToVideos = null;
    private List<Peertube> peertubes = null;
    private List<PeertubeNotification> peertubeNotifications = null;
    private List<Filters> filters = null;
    private List<Playlist> playlists = null;
    private List<String> domains = null;
    private List<app.fedilab.android.client.Entities.List> lists = null;
    private List<Emojis> emojis = null;
    private Error error = null;
    private String since_id, max_id;
    private Instance instance;
    private List<StoredStatus> storedStatuses;
    private boolean fetchmore = false;
    private List<String> playlistForVideos;
    private List<InstanceReg> instanceRegs = null;
    private List<AccountAdmin> accountAdmins = null;
    private List<Report> reports = null;
    private Context context = null;
    private List<PixelFedStory> pixelFedStories = null;
    private List<PixelFedStoryItem> pixelFedStoryItems = null;
    private PixelFedStory pixelFedStory = null;
    private List<Trends> trends = null;
    private List<IdentityProof> identityProofs = null;

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public List<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<Status> statuses) {
        this.statuses = statuses;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public String getMax_id() {
        return max_id;
    }

    public void setMax_id(String max_id) {
        this.max_id = max_id;
    }

    public String getSince_id() {
        return since_id;
    }

    public void setSince_id(String since_id) {
        this.since_id = since_id;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public List<Relationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<Relationship> relationships) {
        this.relationships = relationships;
    }

    public List<Emojis> getEmojis() {
        return emojis;
    }

    public void setEmojis(List<Emojis> emojis) {
        this.emojis = emojis;
    }

    public List<app.fedilab.android.client.Entities.List> getLists() {
        return lists;
    }

    public void setLists(List<app.fedilab.android.client.Entities.List> lists) {
        this.lists = lists;
    }

    public List<Filters> getFilters() {
        return filters;
    }

    public void setFilters(List<Filters> filters) {
        this.filters = filters;
    }

    public List<String> getDomains() {
        return domains;
    }

    public void setDomains(List<String> domains) {
        this.domains = domains;
    }

    public List<HowToVideo> getHowToVideos() {
        return howToVideos;
    }

    public void setHowToVideos(List<HowToVideo> howToVideos) {
        this.howToVideos = howToVideos;
    }

    public List<Peertube> getPeertubes() {
        return peertubes;
    }

    public void setPeertubes(List<Peertube> peertubes) {
        this.peertubes = peertubes;
    }

    public List<Conversation> getConversations() {
        return conversations;
    }

    public void setConversations(List<Conversation> conversations) {
        this.conversations = conversations;
    }

    public List<StoredStatus> getStoredStatuses() {
        return storedStatuses;
    }

    public void setStoredStatuses(List<StoredStatus> storedStatuses) {
        this.storedStatuses = storedStatuses;
    }

    public List<PeertubeNotification> getPeertubeNotifications() {
        return peertubeNotifications;
    }

    public void setPeertubeNotifications(List<PeertubeNotification> peertubeNotifications) {
        this.peertubeNotifications = peertubeNotifications;
    }

    public Results getResults() {
        return results;
    }

    public void setResults(Results results) {
        this.results = results;
    }

    public boolean isFetchmore() {
        return fetchmore;
    }

    public void setFetchmore(boolean fetchmore) {
        this.fetchmore = fetchmore;
    }

    public List<Playlist> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(List<Playlist> playlists) {
        this.playlists = playlists;
    }

    public List<String> getPlaylistForVideos() {
        return playlistForVideos;
    }

    public void setPlaylistForVideos(List<String> playlistForVideos) {
        this.playlistForVideos = playlistForVideos;
    }

    public List<InstanceReg> getInstanceRegs() {
        return instanceRegs;
    }

    public void setInstanceRegs(List<InstanceReg> instanceRegs) {
        this.instanceRegs = instanceRegs;
    }

    public List<AccountAdmin> getAccountAdmins() {
        return accountAdmins;
    }

    public void setAccountAdmins(List<AccountAdmin> accountAdmins) {
        this.accountAdmins = accountAdmins;
    }

    public List<Report> getReports() {
        return reports;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
    }

    public String getTargetedId() {
        return targetedId;
    }

    public void setTargetedId(String targetedId) {
        this.targetedId = targetedId;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }


    public List<PixelFedStory> getPixelFedStories() {
        return pixelFedStories;
    }

    public void setPixelFedStories(List<PixelFedStory> pixelFedStories) {
        this.pixelFedStories = pixelFedStories;
    }

    public List<PixelFedStoryItem> getPixelFedStoryItems() {
        return pixelFedStoryItems;
    }

    public void setPixelFedStoryItems(List<PixelFedStoryItem> pixelFedStoryItems) {
        this.pixelFedStoryItems = pixelFedStoryItems;
    }

    public List<Trends> getTrends() {
        return trends;
    }

    public void setTrends(List<Trends> trends) {
        this.trends = trends;
    }

    public List<IdentityProof> getIdentityProofs() {
        return identityProofs;
    }

    public void setIdentityProofs(List<IdentityProof> identityProofs) {
        this.identityProofs = identityProofs;
    }
}
