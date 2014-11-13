package de.majortom.profisounder.thundersound.switches.statechanges;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import de.majortom.profisounder.thundersound.ISounderInterface;
import de.majortom.profisounder.thundersound.switches.AbstractStateChange;

@XmlAccessorType(XmlAccessType.NONE)
public class OnStateChanged extends AbstractStateChange {
	public enum PreviousState {
		LOW, HIGH
	}

	@XmlAttribute(required = false)
	private Integer durationFromMS = -1;
	@XmlAttribute(required = false)
	private Integer durationToMS = -1;
	@XmlAttribute()
	private PreviousState previousState = PreviousState.HIGH;

	private long durationTimer = -1;

	public OnStateChanged() {
	}

	@Override
	public void initializeInternal(ISounderInterface thunderSounder, ResourceBundle messages) {
		if ((durationFromMS < -1) || (durationToMS < -1) || ((durationToMS > -1) && (durationFromMS > durationToMS))) {
			throw new IllegalArgumentException(messages.getString("errors.statechangeduration.durationsinvalid"));
		}

		for (AbstractAction ac : actions) {
			ac.initialize(thunderSounder, messages);
		}
	}

	@Override
	public void switchStateChanged(boolean state, ISounderInterface thunderSounder, ResourceBundle messages) throws IOException {
		boolean check = false;
		if (previousState == PreviousState.HIGH) {
			if ((durationTimer < 0) || state) {
				durationTimer = System.currentTimeMillis();
			} else {
				check = true;
			}
		} else if (previousState == PreviousState.LOW) {
			if ((durationTimer < 0) || !state) {
				durationTimer = System.currentTimeMillis();
			} else {
				check = true;
			}
		}

		if (check) {
			int curTimeDiff = (int) (System.currentTimeMillis() - durationTimer);

			if ((curTimeDiff >= durationFromMS) && ((durationToMS < 0) || (curTimeDiff <= durationToMS))) {
				logger.log(Level.FINE, MessageFormat.format(messages.getString("statechange.duration.performactions"), previousState, curTimeDiff));
				doActions(thunderSounder, messages);
			}
		}
	}
}
