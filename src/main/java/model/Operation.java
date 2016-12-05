package model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import enumeration.Machine;
import model.abstracts.Event;

public class Operation extends Event{

    private Machine machine;

    public Operation(int duration) {
        super(duration);
    }

    public void startOperation(int startTime) {
        setBegin(startTime);
        setEnd(startTime + getDuration());
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }
}
