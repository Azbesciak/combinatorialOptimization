package model;

public class Task implements Comparable<Task>{

    private static int indexer = 0;

    private int id;
    private Operation first;
    private Operation second;

    public Task(Operation first, Operation second) {
        this.first = first;
        this.second = second;
        id = indexer++;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public static void resetIndexer() {
        indexer = 0;
    }

    @Override
    public String toString() {
        return "Task{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Task task = (Task) o;

        return id == task.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public int compareTo(Task o) {
        return Integer.compare(this.getFirst().getBegin(), o.getFirst().getBegin());
    }
}
