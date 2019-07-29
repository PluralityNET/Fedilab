package app.fedilab.android.client.Entities;

import java.util.List;

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

public class Charts {

    private List<String> xLabels;
    private List<String> yLabels;
    private List<Long> xValues;
    private List<Integer> statuses;
    private List<Integer> boosts;
    private List<Integer> replies;

    public List<String> getxLabels() {
        return xLabels;
    }

    public void setxLabels(List<String> xLabels) {
        this.xLabels = xLabels;
    }

    public List<String> getyLabels() {
        return yLabels;
    }

    public void setyLabels(List<String> yLabels) {
        this.yLabels = yLabels;
    }

    public List<Long> getxValues() {
        return xValues;
    }

    public void setxValues(List<Long> xValues) {
        this.xValues = xValues;
    }

    public List<Integer> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<Integer> statuses) {
        this.statuses = statuses;
    }

    public List<Integer> getBoosts() {
        return boosts;
    }

    public void setBoosts(List<Integer> boosts) {
        this.boosts = boosts;
    }

    public List<Integer> getReplies() {
        return replies;
    }

    public void setReplies(List<Integer> replies) {
        this.replies = replies;
    }
}
