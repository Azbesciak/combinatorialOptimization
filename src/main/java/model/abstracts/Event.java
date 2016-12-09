package model.abstracts;

public abstract class Event {
	private int begin;
	private int duration;
	private int end;

	public Event(int duration) {
		this.duration = duration;
	}

	public int getBegin() {
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

	protected void setEnd(int end) {
		this.end = end;
	}

	public int getEnd() {
		return end;
	}

	@Override
	public String toString() {
		return "Event{" +
				"begin=" + begin +
				", duration=" + duration +
				", end=" + end +
				'}';
	}
}
