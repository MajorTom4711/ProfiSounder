package de.majortom.profisounder.notifications;

import de.majortom.profisounder.types.Message;

public class LogUpdatedEvent {
	private IPersistentNotificationConsumer consumer;

	private Message message;

	private boolean logUpdated;
	private boolean notificationRemoved;

	public LogUpdatedEvent(IPersistentNotificationConsumer consumer, boolean logUpdated, boolean notificationRemoved, Message message) {
		this.consumer = consumer;
		this.message = message;

		this.logUpdated = logUpdated;
		this.notificationRemoved = notificationRemoved;
	}

	public IPersistentNotificationConsumer getConsumer() {
		return consumer;
	}

	public Message getMessage() {
		return message;
	}

	public boolean isLogUpdated() {
		return logUpdated;
	}

	public boolean isNotificationRemoved() {
		return notificationRemoved;
	}
}
