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

/**
 * Created by Thomas on 15/12/2018.
 * Manage Tags timeline settings
 */

public class TagTimeline {

    private String name;
    private boolean isART;
    private boolean isNSFW;



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isART() {
        return isART;
    }

    public void setART(boolean ART) {
        isART = ART;
    }

    public boolean isNSFW() {
        return isNSFW;
    }

    public void setNSFW(boolean NSFW) {
        isNSFW = NSFW;
    }
}
