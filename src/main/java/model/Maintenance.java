package model;

public class Maintenance {
    private long begin;
    private long end;

    public Maintenance(long begin, long end) {
        this.begin = begin;
        this.end = end;
    }

    public long getBegin() {
        return begin;
    }

    public void setBegin(long begin) {
        this.begin = begin;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getTotalTime() {
        return end - begin;
    }

    @Override
    public String toString() {
        return "Maintenance{" +
                "begin=" + begin +
                ", end=" + end +
                '}';
    }
}
