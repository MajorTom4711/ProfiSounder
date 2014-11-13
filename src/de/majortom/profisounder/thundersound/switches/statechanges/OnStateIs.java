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
public class OnStateIs extends AbstractStateChange {
	public enum CurrentState {
		LOW, HIGH
	}

	@XmlAttribute(required = false)
	private CurrentState currentState = CurrentState.HIGH;
	@XmlAttribute(required = false)
	private Boolean checkStateAtStartup = true;

	public OnStateIs() {
	}

	@Override
	public void initializeInternal(ISounderInterface thunderSounder, ResourceBundle messages) {
		for (AbstractAction ac : actions) {
			ac.initialize(thunderSounder, messages);
		}
	}

	@Override
	public void setInitialState(boolean state, ISounderInterface thunderSounder, ResourceBundle messages) throws IOException {
		if (checkStateAtStartup) {
			switchStateChanged(state, thunderSounder, messages);
		}
	}

	@Override
	public void switchStateChanged(boolean state, ISounderInterface thunderSounder, ResourceBundle messages) throws IOException {
		if ((state && (currentState == CurrentState.HIGH)) || (!state && (currentState == CurrentState.LOW))) {
			logger.log(Level.FINE, MessageFormat.format(messages.getString("statechange.change.performactions"), state));
			doActions(thunderSounder, messages);
		}
	}
}
