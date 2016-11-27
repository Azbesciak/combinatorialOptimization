package model;

public class Maintenance {
    private int begin;
    private int duration;

    public Maintenance(int begin, int duration) {
        this.begin = begin;
        this.duration = duration;
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
        return this.begin + this.duration;
    }
    @Override
    public String toString() {
        return "Maintenance{" +
                "begin=" + begin +
                ", duration=" + duration +
                '}';
    }

}
