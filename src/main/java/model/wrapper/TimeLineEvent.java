package model.wrapper;

import enumeration.EventType;
import model.abstracts.Event;

public class TimeLineEvent extends Event {
	private String identifier;
	private EventType eventType;

	public TimeLineEvent(Event event, EventType eventType, int operationNumber, int taskId) {
		super(event.getDuration());
		setBegin(event.getBegin());
		this.eventType = eventType;
		setIdentifier(operationNumber, taskId);
	}

	public TimeLineEvent(Event event, EventType eventType, int operationNumber) {
		this(event.getBegin(), event.getDuration(), eventType, operationNumber);
	}

	public TimeLineEvent(int begin, int duration, EventType eventType, int operationNumber) {
		super(duration);
		setBegin(begin);
		this.eventType = eventType;
		setIdentifier(operationNumber);
	}

	private void setIdentifier(int operationNumber, int taskId) {
		setIdentifier(operationNumber);
		identifier += "_" + taskId;
	}

	private void setIdentifier(int operationNumber) {
		identifier = eventType.toString() + operationNumber;
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	@Override
	public String toString() {
		return identifier + ", " + getBegin() + ", " + getDuration() + "; ";
	}
}
