package model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.abstracts.Event;

public class Maintenance extends Event{

    public Maintenance(int begin, int duration) {
        super(duration);
        setBegin(begin);
        setEnd(begin + duration);
    }
}
