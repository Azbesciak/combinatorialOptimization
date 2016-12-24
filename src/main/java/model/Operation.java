package model;

import enumeration.Machine;
import model.abstracts.Event;

public class Operation extends Event{

    private Machine machine;
    private int readyTime;

//    public Operation(int duration, Machine machine) {
//        super(duration);
//        this.machine = machine;
//        this.readyTime = 0;
//    }

    private Operation(int duration,  int readyTime, Machine machine) {
        super(duration);
        this.readyTime = readyTime;
        this.machine = machine;
    }

    public static Operation createFirstMachineOperation(int duration,  int readyTime) {
        return new Operation(duration, readyTime, Machine.ONE);
    }

    public static Operation createSecondMachineOperation(int duration) {
        return new Operation(duration, 0, Machine.TWO);
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
