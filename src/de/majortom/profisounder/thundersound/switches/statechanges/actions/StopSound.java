package de.majortom.profisounder.thundersound.switches.statechanges.actions;

import java.io.File;
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
public class StopSound extends AbstractAction {
	@XmlAttribute(required = true)
	private String soundFile;
	@XmlAttribute(required = false)
	private Integer fadeOutDuration = -1;

	public StopSound() {
	}

	@Override
	public synchronized void doAction(ISounderInterface thunderSounder, ResourceBundle message) {
		List<AbstractAction> allActions = thunderSounder.getAllActions();

		logger.log(Level.FINE, MessageFormat.format(message.getString("action.doaction.stopsound"), soundFile, fadeOutDuration));

		for (AbstractAction ac : allActions) {
			if (ac instanceof PlaySound) {
				PlaySound ps = (PlaySound) ac;

				if (ps.getSoundFile().equals(soundFile)) {
					ps.stopSound(fadeOutDuration);
				}
			}
		}
	}

	@Override
	public void finalize(ISounderInterface thunderSounder, ResourceBundle messages) {
	}

	@Override
	public void initialize(ISounderInterface thunderSounder, ResourceBundle messages) {
		if (fadeOutDuration > 3600000) {
			throw new IllegalArgumentException(messages.getString("errors.switch.fadeoutdurationinvalid"));
		}

		if (!new File(soundFile).exists()) {
			throw new IllegalArgumentException(messages.getString("errors.switch.soundfileinvalid"));
		}
	}

	@Override
	public boolean isActive() {
		return false;
	}
}
