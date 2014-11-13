package de.majortom.profisounder.thundersound.switches.statechanges;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import de.majortom.profisounder.thundersound.ISounderInterface;

@XmlTransient
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractAction {
	protected Logger logger = Logger.getLogger(AbstractAction.class.getName());

	public abstract void doAction(ISounderInterface thunderSounder, ResourceBundle messages) throws IOException;

	public abstract void finalize(ISounderInterface thunderSounder, ResourceBundle messages);

	public abstract void initialize(ISounderInterface thunderSounder, ResourceBundle messages);

	public abstract boolean isActive();

	public void setGain(float factor) {
		// Does nothing by default
	}
}
