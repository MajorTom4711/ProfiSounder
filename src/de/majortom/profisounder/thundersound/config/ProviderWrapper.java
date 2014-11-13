package de.majortom.profisounder.thundersound.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import de.majortom.profisounder.switchstateprovider.AbstractSwitchStateProvider;
import de.majortom.profisounder.switchstateprovider.impl.RpiGPIOStateProvider;
import de.majortom.profisounder.switchstateprovider.impl.RpiGPIOStateProviderNoRoot;
import de.majortom.profisounder.switchstateprovider.impl.SwitchStateNetPoller;
import de.majortom.profisounder.switchstateprovider.impl.SwitchStateNetPollerOldMethod;

public class ProviderWrapper {
	@XmlElements({ @XmlElement(name = "netPoller", type = SwitchStateNetPoller.class), @XmlElement(name = "gpioProvider", type = RpiGPIOStateProvider.class), @XmlElement(name = "gpioProviderNoRoot", type = RpiGPIOStateProviderNoRoot.class), @XmlElement(name = "netPollerOld", type = SwitchStateNetPollerOldMethod.class) })
	private AbstractSwitchStateProvider switchStateprovider;

	public AbstractSwitchStateProvider getSwitchStateprovider() {
		return switchStateprovider;
	}
}
