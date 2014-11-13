package de.majortom.profisounder.notifications;

public class PersistentNotifications {
	private static IPersistentNotificationConsumer consumer = null;

	public static synchronized IPersistentNotificationConsumer get() {
		if (PersistentNotifications.consumer == null)
			throw new IllegalStateException("Default consumer not set!");

		return consumer;
	}

	public static synchronized void setConsumer(IPersistentNotificationConsumer consumer) {
		if (PersistentNotifications.consumer != null)
			throw new IllegalAccessError("Not allowed!");

		PersistentNotifications.consumer = consumer;
	}
}
