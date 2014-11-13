package de.majortom.profisounder.notifications;

import java.util.EventListener;

public interface ILogUpdatedListener extends EventListener {
	public void logUpdated(LogUpdatedEvent luEvent);
}
