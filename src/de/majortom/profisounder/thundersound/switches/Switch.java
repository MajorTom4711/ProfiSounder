package de.majortom.profisounder.thundersound.switches;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import de.majortom.profisounder.thundersound.ISounderInterface;
import de.majortom.profisounder.thundersound.switches.statechanges.AbstractAction;
import de.majortom.profisounder.thundersound.switches.statechanges.OnStateChange;
import de.majortom.profisounder.thundersound.switches.statechanges.OnStateDuration;

@XmlAccessorType(XmlAccessType.NONE)
public class Switch implements Comparable<Switch> {
	@XmlAttribute(required = true)
	private Integer switchNumber;

	@XmlAttribute(required = false)
	private Integer order = 1;

	@XmlAttribute(required = false)
	private Integer samplingRateLow = -1;
	@XmlAttribute(required = false)
	private Integer samplingRateHigh = -1;

	@XmlElements({ @XmlElement(name = "onStateChange", type = OnStateChange.class), @XmlElement(name = "onStateDuration", type = OnStateDuration.class), })
	private List<AbstractStateChange> stateChanges;

	private List<AbstractAction> actions = new ArrayList<AbstractAction>();

	@Override
	public int compareTo(Switch o) {
		return new Integer(order).compareTo(o.order);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Switch)) {
			return false;
		}

		return new Integer(order).equals(((Switch) o).order);
	}

	public void finalize(ISounderInterface thunderSounder, ResourceBundle messages) {
		for (AbstractStateChange sc : stateChanges) {
			sc.finalize(thunderSounder, messages);
		}
	}

	public List<AbstractAction> getAllActions() {
		return actions;
	}

	public int getSwitchNumber() {
		return switchNumber;
	}

	public void initialize(ISounderInterface thunderSounder, ResourceBundle messages) {
		if (((samplingRateLow > -1) && ((samplingRateLow < 500) || (samplingRateLow > 3600000))) || ((samplingRateHigh > -1) && ((samplingRateHigh < 500) || (samplingRateHigh > 3600000)))) {
			throw new IllegalArgumentException(MessageFormat.format(messages.getString("errors.switch.samplingratesoutofrange"), 500, 3600000));
		}

		if (stateChanges == null) {
			return;
		}

		for (AbstractStateChange sc : stateChanges) {
			sc.initialize(thunderSounder, messages);
			actions.addAll(sc.getActions());
		}
	}

	public void setGain(float gain) {
		for (AbstractStateChange sc : stateChanges) {
			sc.setGain(gain);
		}
	}

	public int setInitialState(boolean state, ISounderInterface thunderSounder, ResourceBundle messages) throws IOException {
		for (AbstractStateChange sc : stateChanges) {
			sc.setInitialState(state, thunderSounder, messages);
		}

		if (state) {
			return samplingRateHigh;
		} else {
			return samplingRateLow;
		}
	}

	public int switchStateChanged(boolean state, ISounderInterface thunderSounder, ResourceBundle message) throws IOException {
		if (stateChanges != null) {
			for (AbstractStateChange sc : stateChanges) {
				sc.switchStateChanged(state, thunderSounder, message);
			}
		}

		if (state) {
			return samplingRateHigh;
		} else {
			return samplingRateLow;
		}
	}
}
