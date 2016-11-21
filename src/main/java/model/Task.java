package model;

public class Task {
    private Operation first;
    private Operation second;

    public Task(Operation first, Operation second) {
        this.first = first;
        this.second = second;
    }

    public Operation getFirst() {
        return first;
    }

    public void setFirst(Operation first) {
        this.first = first;
    }

    public Operation getSecond() {
        return second;
    }

    public void setSecond(Operation second) {
        this.second = second;
    }

    @Override
    public String toString() {
        return "Task{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }
}
