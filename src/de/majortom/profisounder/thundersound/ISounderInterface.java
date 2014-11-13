package de.majortom.profisounder.thundersound;

import java.util.List;

import de.majortom.profisounder.thundersound.switches.statechanges.AbstractAction;

public interface ISounderInterface {

	public List<AbstractAction> getActionsForSwitch(int switchNumber);

	public List<AbstractAction> getAllActions();
}
