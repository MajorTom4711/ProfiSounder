package de.majortom.profisounder.notifications;

import java.util.logging.Level;

public interface IPersistentNotificationConsumer {
	public long notification(boolean set, Level level, String message, Throwable exception);

	public long notification(boolean set, Level level, String message, Throwable exception, Object[] msgFormatParams);

	public void reset(long key, String message);

	public void reset(long key, String message, Object[] msgFormatParams);
}
