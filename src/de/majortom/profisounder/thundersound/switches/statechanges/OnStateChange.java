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
public class OnStateChange extends AbstractStateChange {
	public enum StateChanged {
		TO_LOW, TO_HIGH
	}

	@XmlAttribute(required = false)
	private StateChanged stateChanged = StateChanged.TO_HIGH;
	@XmlAttribute(required = false)
	private Boolean checkStartupSate = true;

	public OnStateChange() {
	}

	@Override
	public void initializeInternal(ISounderInterface thunderSounder, ResourceBundle messages) {
		for (AbstractAction ac : actions) {
			ac.initialize(thunderSounder, messages);
		}
	}

	@Override
	public void setInitialState(boolean state, ISounderInterface thunderSounder, ResourceBundle messages) throws IOException {
		if (checkStartupSate) {
			switchStateChanged(state, thunderSounder, messages);
		}
	}

	@Override
	public void switchStateChanged(boolean state, ISounderInterface thunderSounder, ResourceBundle messages) throws IOException {
		if ((state && (stateChanged == StateChanged.TO_HIGH)) || (!state && (stateChanged == StateChanged.TO_LOW))) {
			logger.log(Level.FINE, MessageFormat.format(messages.getString("statechange.change.performactions"), state));
			doActions(thunderSounder, messages);
		}
	}
}
