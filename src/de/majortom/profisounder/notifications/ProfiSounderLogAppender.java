package de.majortom.profisounder.notifications;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.event.EventListenerList;

import org.apache.commons.collections15.buffer.CircularFifoBuffer;

import de.majortom.profisounder.types.Message;

public class ProfiSounderLogAppender extends Handler implements IPersistentNotificationConsumer {
	private static final int MAX_BUFFERSIZE = 1 << 12;

	private EventListenerList listenerList = new EventListenerList();

	private Map<Long, Message> persistentMessages = new TreeMap<>();
	private CircularFifoBuffer<Message> logBuffer;

	private volatile Long pmIndex = Long.MIN_VALUE;

	private volatile boolean active = true;

	public ProfiSounderLogAppender(int logBufferSize) {
		if (logBufferSize < 1 || logBufferSize > MAX_BUFFERSIZE)
			throw new IllegalArgumentException("logBufferSize must be between 1 and " + MAX_BUFFERSIZE + "!");

		PersistentNotifications.setConsumer(this);
		logBuffer = new CircularFifoBuffer<>(logBufferSize);
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() throws SecurityException {
		active = false;
	}

	@Override
	public void publish(LogRecord record) {
		if (!active)
			throw new RuntimeException("This handler is closed!");

		Message msg = new Message(record.getLevel(), record.getMessage(), record.getThrown(), false, (Object[]) null);
		logBuffer.add(msg);

		fireLogUpdated(true, false, msg);
	}

	@Override
	public long notification(boolean set, Level level, String message, Throwable exception) {
		return notification(set, level, message, exception, null);
	}

	@Override
	public long notification(boolean set, Level level, String message, Throwable exception, Object[] msgFormatParams) {
		Message msg = new Message(level, message, exception, true, msgFormatParams);
		Long key = null;

		if (!set) {
			logBuffer.add(msg);
			fireLogUpdated(false, false, msg);
		} else {
			synchronized (this) {
				key = pmIndex++;
			}

			persistentMessages.put(key, msg);
			fireLogUpdated(false, false, msg);
		}

		return key;
	}

	@Override
	public void reset(long key, String message) {
		reset(key, message, null);
	}

	@Override
	public void reset(long key, String message, Object[] msgFormatParams) {
		Message removed = persistentMessages.remove(key);

		if (removed != null) {
			fireLogUpdated(false, true, removed);
		}

		if (message != null && !message.isEmpty()) {
			Message msg = new Message(Level.FINE, message, null, true, msgFormatParams);
			logBuffer.add(msg);

			fireLogUpdated(true, false, msg);
		}
	}

	public void addFooListener(ILogUpdatedListener l) {
		listenerList.add(ILogUpdatedListener.class, l);
	}

	public void removeFooListener(ILogUpdatedListener l) {
		listenerList.remove(ILogUpdatedListener.class, l);
	}

	protected void fireLogUpdated(boolean logUpdated, boolean notificationRemoved, Message msg) {
		Object[] listeners = listenerList.getListenerList();

		LogUpdatedEvent luEvent = new LogUpdatedEvent(this, logUpdated, notificationRemoved, msg);
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ILogUpdatedListener.class) {
				((ILogUpdatedListener) listeners[i + 1]).logUpdated(luEvent);
			}
		}
	}
}
