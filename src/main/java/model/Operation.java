package model;

import enumeration.Machine;
import model.abstracts.Event;

public class Operation extends Event{

    private Machine machine;
    private int readyTime;

    public Operation(int duration) {
        super(duration);
        this.readyTime = 0;
    }

    public Operation(int duration,  int readyTime) {
        super(duration);
        this.readyTime = readyTime;
    }

    public void startOperation(int startTime) {
        setBegin(startTime);
        setEnd(startTime + getDuration());
    }

    public int startOperationWhenPossibleAndGetEnd(int possibleStartTime) {
        startOperation(Math.max(possibleStartTime, readyTime));
        return getEnd();
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public int getReadyTime() {
        return readyTime;
    }

    @Override
    public String toString() {
        return "Operation{" +
                "machine=" + machine +
                ", readyTime=" + readyTime +
                "} " + super.toString() + "\n";
    }
}
