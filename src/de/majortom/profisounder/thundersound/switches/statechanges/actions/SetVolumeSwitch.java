package de.majortom.profisounder.thundersound.switches.statechanges.actions;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import de.majortom.profisounder.thundersound.ISounderInterface;
import de.majortom.profisounder.thundersound.switches.statechanges.AbstractAction;

@XmlAccessorType(XmlAccessType.NONE)
public class SetVolumeSwitch extends AbstractAction {
	@XmlAttribute(required = true)
	private Float volume;

	@XmlAttribute(required = true)
	private Integer switchNumber;
	@XmlAttribute(required = false)
	private Integer fadeDuration = -1;

	public SetVolumeSwitch() {
	}

	@Override
	public synchronized void doAction(ISounderInterface thunderSounder, ResourceBundle message) {
		List<AbstractAction> actions = thunderSounder.getActionsForSwitch(switchNumber);

		logger.log(Level.FINE, MessageFormat.format(message.getString("action.doaction.setvolumeswitch"), switchNumber, volume, fadeDuration));

		for (AbstractAction ac : actions) {
			if (ac instanceof PlaySound) {
				PlaySound ps = (PlaySound) ac;
				ps.setVolume(volume, fadeDuration);
			}
		}
	}

	@Override
	public void finalize(ISounderInterface thunderSounder, ResourceBundle messages) {
	}

	@Override
	public void initialize(ISounderInterface thunderSounder, ResourceBundle messages) {
		if (fadeDuration > 3600000) {
			throw new IllegalArgumentException(messages.getString("errors.switch.fadedurationinvalid"));
		}
	}

	@Override
	public boolean isActive() {
		return false;
	}
}
