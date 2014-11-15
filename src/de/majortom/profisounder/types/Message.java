package de.majortom.profisounder.types;

import java.text.MessageFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class Message implements Comparable<Message> {

	public static void setResourceBundle(ResourceBundle bundle) {
		MESSAGES = bundle;
	}

	private static ResourceBundle MESSAGES;

	private Date date;

	private Level level;
	private String message;
	private Throwable exception;

	public Message(Date date, Level level, String message, Throwable exception, boolean useFormatter, Object... msgFormatParams) {
		this.date = date;
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

	public Date getDate() {
		return date;
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

	@Override
	public int compareTo(Message o) {
		return date.compareTo(o.date);
	}
}
