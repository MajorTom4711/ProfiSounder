package de.majortom.profisounder.switchstateprovider.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import de.majortom.profisounder.notifications.PersistentNotifications;
import de.majortom.profisounder.switchstateprovider.AbstractSwitchStateProvider;
import de.majortom.profisounder.switchstateprovider.impl.mapping.SwitchMapping;
import de.majortom.profisounder.switchstateprovider.impl.tools.WiringPiRuntimeExec;

@XmlAccessorType(XmlAccessType.NONE)
public class RpiGPIOStateProviderNoRoot extends AbstractSwitchStateProvider {
	private class GPIOPoller extends Thread {
		public GPIOPoller() {
			setName("Listener: " + SwitchStateNetPollerOldMethod.class.getSimpleName());
			setDaemon(true);
		}

		@Override
		public void run() {
			Map<Integer, Boolean> oldStates = null;
			Long wasError = null;

			while (!isInterrupted()) {
				try {
					Map<Integer, Boolean> switchState = getPinStates();

					if (oldStates != null) {
						for (Integer ms : monitoredSwitches) {
							if (switchState.get(ms) != oldStates.get(ms)) {
								fireSwitchStateChanged(ms, switchState.get(ms));
							}
						}
					}

					oldStates = switchState;
					if (wasError != null) {
						PersistentNotifications.get().reset(wasError, "Querying was successful");
						wasError = null;
					}
				} catch (Exception ex) {
					wasError = PersistentNotifications.get().notification(true, Level.SEVERE, "An exception occured while querying GPIO Pins!", ex);
				}

				try {
					sleep(samplingRateMS);
				} catch (InterruptedException e) {
					return;
				}
			}
		}
	}

	@XmlElement(name = "switchMapping", required = true)
	private List<SwitchMapping> switchMappings;

	private GPIOPoller tl;
	private WiringPiRuntimeExec runtimeExec;

	private Map<Integer, Integer> switchPinMapping;

	public RpiGPIOStateProviderNoRoot() {
		super();

		runtimeExec = new WiringPiRuntimeExec();
	}

	@Override
	protected Map<Integer, Boolean> getCurrentStateInternal() throws IOException {
		return getPinStates();
	}

	@Override
	protected List<Integer> getSupportedSwitchesInternal() {
		List<Integer> supportedSwitches = new ArrayList<Integer>();
		for (SwitchMapping sm : switchMappings) {
			supportedSwitches.add(sm.getSwitchNumber());
		}

		return supportedSwitches;
	}

	@Override
	protected void initializeInternal() throws IOException {
		switchPinMapping = new HashMap<Integer, Integer>();
		for (SwitchMapping sm : switchMappings) {
			if (sm.getRpiPin() < 0 || sm.getRpiPin() > 20)
				throw new IndexOutOfBoundsException("Config invalid! Raspi Pin Number must be between 0 and 20!");

			switchPinMapping.put(sm.getSwitchNumber(), sm.getRpiPin());
		}

		if (!runtimeExec.testRuntime()) {
			throw new IOException("gpio executable cannot be accessed flawlessly!");
		}
	}

	@Override
	protected void startProviderInternal() {
		tl = new GPIOPoller();
		tl.start();
	}

	@Override
	protected void stopProviderInternal() {
		tl.interrupt();
	}

	private Map<Integer, Boolean> getPinStates() throws IOException {
		Map<Integer, Boolean> retVal = new HashMap<>();
		for (Integer mSwitch : monitoredSwitches) {
			int readVal = runtimeExec.readPin(switchPinMapping.get(mSwitch));
			retVal.put(mSwitch, readVal == 1);
		}

		return retVal;
	}
}
