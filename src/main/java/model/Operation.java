package model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Operation {
    private int duration;
    private int beginTime;
    private int endTime;

    public Operation(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public int getBeginTime() {
        return beginTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void startOperation(int startTime) {
        this.beginTime = startTime;
        this.endTime = startTime + this.duration;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }
}
