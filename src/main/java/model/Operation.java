package model;

public class Operation {
    private int duration;
    private boolean isFinished;
    private Machine host;

    public Operation(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    public Machine getHost() {
        return host;
    }

    public void setHost(Machine host) {
        this.host = host;
    }

    @Override
    public String toString() {
        return "Operation{" +
                "duration=" + duration +
                ", isFinished=" + isFinished +
                ", host=" + host +
                '}';
    }
}
