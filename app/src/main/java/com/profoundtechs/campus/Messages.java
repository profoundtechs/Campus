package com.profoundtechs.campus;

public class Messages {
    private String message, type, from, seen;
    private long time;

    public Messages(String message, String type, String from, long time, String seen) {
        this.message = message;
        this.type = type;
        this.from = from;
        this.time = time;
        this.seen = seen;
    }

    public Messages(){

    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
