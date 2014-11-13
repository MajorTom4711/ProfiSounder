package de.majortom.profisounder.types;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class Message {

	public static void setResourceBundle(ResourceBundle bundle) {
		MESSAGES = bundle;
	}

	private static ResourceBundle MESSAGES;

	private Level level;
	private String message;
	private Throwable exception;

	public Message(Level level, String message, Throwable exception, boolean useFormatter, Object... msgFormatParams) {
		this.level = level;
		this.exception = exception;

		if (useFormatter && MESSAGES != null) {
			if (msgFormatParams != null)
				this.message = MessageFormat.format(MESSAGES.getString(message), msgFormatParams);
			else
				this.message = MESSAGES.getString(message);
		} else {
			this.message = message;
		}
	}

	public Throwable getException() {
		return exception;
	}

	public Level getLevel() {
		return level;
	}

	public String getMessage() {
		return message;
	}
}
