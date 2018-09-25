package com.profoundtechs.campus;

/**
 * Created by HP on 4/19/2018.
 */

public class Friends {

    public long date;
    public String online;

    public Friends(){

    }

    public Friends(long date, String online) {
        this.date = date;
        this.online = online;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }
}
