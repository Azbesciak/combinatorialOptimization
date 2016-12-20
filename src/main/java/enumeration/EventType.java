package enumeration;

public enum EventType {
	IDLE("idle"), MAINTENANCE("maint"), OPERATION("op");

	private final String name;

	EventType(String name) {
		this.name = name;
	}

	public String toString() {
		return this.name;
	}
}
