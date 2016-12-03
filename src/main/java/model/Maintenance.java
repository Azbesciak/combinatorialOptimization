package model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Maintenance {
    private int begin;
    private int duration;
    private int end;

    public Maintenance(int begin, int duration) {
        this.begin = begin;
        this.duration = duration;
        this.end = begin + duration;
    }

    public long getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getEndTime() {
        return this.end;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }
}
