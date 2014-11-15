package de.majortom.profisounder.switchstateprovider.events;

import java.util.EventListener;

public interface ISwitchStateListener extends EventListener {

	public void switchStateChanged(int switchNumber, boolean state);

}
