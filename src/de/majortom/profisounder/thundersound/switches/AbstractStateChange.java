package de.majortom.profisounder.thundersound.switches;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

import de.majortom.profisounder.thundersound.ISounderInterface;
import de.majortom.profisounder.thundersound.switches.statechanges.AbstractAction;
import de.majortom.profisounder.thundersound.switches.statechanges.actions.PlaySound;
import de.majortom.profisounder.thundersound.switches.statechanges.actions.SetVolumeSound;
import de.majortom.profisounder.thundersound.switches.statechanges.actions.SetVolumeSwitch;
import de.majortom.profisounder.thundersound.switches.statechanges.actions.StopSound;
import de.majortom.profisounder.thundersound.switches.statechanges.actions.StopSwitch;

@XmlType(name = "StateChange")
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractStateChange {
	@XmlElements({ @XmlElement(name = "setVolumeSound", type = SetVolumeSound.class), @XmlElement(name = "setVolumeSwitch", type = SetVolumeSwitch.class), @XmlElement(name = "playSound", type = PlaySound.class), @XmlElement(name = "stopSound", type = StopSound.class), @XmlElement(name = "stopSwitchSounds", type = StopSwitch.class), })
	protected List<AbstractAction> actions;

	protected List<Integer> actionNumbers;

	@XmlAttribute(required = false)
	protected Boolean oneOfAction = false;
	@XmlAttribute(required = false)
	protected Boolean skipActionIfActive = true;

	protected Logger logger = Logger.getLogger(AbstractStateChange.class.getName());

	private Random random;

	public AbstractStateChange() {
		random = new Random();
	}

	public void finalize(ISounderInterface thunderSounder, ResourceBundle messages) {
		for (AbstractAction ac : actions) {
			ac.finalize(thunderSounder, messages);
		}
	}

	public List<AbstractAction> getActions() {
		return actions;
	}

	public final void initialize(ISounderInterface thunderSounder, ResourceBundle messages) {
		initializeInternal(thunderSounder, messages);

		actionNumbers = new ArrayList<Integer>();
		for (int i = 0; i < actions.size(); i++) {
			actionNumbers.add(i);
		}
	}

	public void setGain(float gain) {
		for (AbstractAction ac : actions) {
			ac.setGain(gain);
		}
	}

	public void setInitialState(boolean state, ISounderInterface thunderSounder, ResourceBundle messages) throws IOException {
		// Does nothing here, 'cause IMHO for, e.g, Duration not usable
	}

	public abstract void switchStateChanged(boolean state, ISounderInterface thunderSounder, ResourceBundle messages) throws IOException;

	protected void doActions(ISounderInterface thunderSounder, ResourceBundle messages) throws IOException {
		if (!oneOfAction) {
			for (AbstractAction ac : actions) {
				if (!(skipActionIfActive && ac.isActive())) {
					ac.doAction(thunderSounder, messages);
				}
			}
		} else {
			Collections.shuffle(actionNumbers, random);

			if (!skipActionIfActive) {
				actions.get(actionNumbers.get(0)).doAction(thunderSounder, messages);
			} else {
				for (Integer i : actionNumbers) {
					if (!actions.get(i).isActive()) {
						actions.get(i).doAction(thunderSounder, messages);
						break;
					}
				}
			}
		}
	}

	protected abstract void initializeInternal(ISounderInterface thunderSounder, ResourceBundle messages);

}
