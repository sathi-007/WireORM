package com.mast.android.orm.realm.objects;

import io.realm.RealmObject;

/**
 * Created by sathish-n on 17/8/16.
 */

public class CommentaryList extends RealmObject {
    private String commText;
    private int timestamp;
    private double overNumber;
    private int inningsId;
    private String event;
    private OverSeperator overSeparator;

    public String getCommText() {
        return commText;
    }

    public void setCommText(String commText) {
        this.commText = commText;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public double getOverNumber() {
        return overNumber;
    }

    public void setOverNumber(double overNumber) {
        this.overNumber = overNumber;
    }

    public int getInningsId() {
        return inningsId;
    }

    public void setInningsId(int inningsId) {
        this.inningsId = inningsId;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public OverSeperator getOverSeparator() {
        return overSeparator;
    }

    public void setOverSeparator(OverSeperator overSeparator) {
        this.overSeparator = overSeparator;
    }
}
