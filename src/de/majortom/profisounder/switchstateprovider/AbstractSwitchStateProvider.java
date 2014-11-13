package de.majortom.profisounder.switchstateprovider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.event.EventListenerList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import de.majortom.profisounder.switchstateprovider.events.ISwitchStateListener;

@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractSwitchStateProvider {

	protected List<Integer> monitoredSwitches;

	protected boolean started = false;
	protected boolean initialized = false;

	protected int samplingRateMS;

	private EventListenerList listenerList = new EventListenerList();

	public AbstractSwitchStateProvider() {
	}

	public final void addSwitchStateListener(ISwitchStateListener l) {
		listenerList.add(ISwitchStateListener.class, l);
	}

	public final Map<Integer, Boolean> getCurrentState() throws IOException {
		if (!initialized) {
			throw new IllegalStateException("Must first initialize the Provider!");
		}

		return getCurrentStateInternal();
	}

	public final int getSamplingRateMS() {
		return samplingRateMS;
	}

	public final List<Integer> getSupportedSwitches() {
		if (!initialized) {
			throw new IllegalStateException("Must first initialize the Provider!");
		}

		return getSupportedSwitchesInternal();
	}

	public final void initialize() throws IOException {
		if (initialized)
			return;

		initializeInternal();
		initialized = true;
	}

	public final void removeSwitchStateListener(ISwitchStateListener l) {
		listenerList.remove(ISwitchStateListener.class, l);
	}

	public final void setMonitoredSwitches(Collection<Integer> set) {
		if (!initialized) {
			throw new IllegalStateException("Must first initialize the Provider!");
		}

		synchronized (this) {
			List<Integer> supportedSwitches = getSupportedSwitches();
			for (Integer ms : set) {
				if (!supportedSwitches.contains(ms)) {
					throw new IndexOutOfBoundsException("Switch " + ms + " is not supported!");
				}
			}

			monitoredSwitches = new ArrayList<Integer>(set);
		}
	}

	public final void setSamplingRateMS(int samplingRateMS) {
		this.samplingRateMS = samplingRateMS;
	}

	public final void startProvider() {
		synchronized (this) {
			if (started) {
				return;
			}

			if (!initialized) {
				throw new IllegalStateException("Must first initialize the Provider!");
			}

			if ((monitoredSwitches == null) || monitoredSwitches.isEmpty()) {
				throw new IllegalStateException("Must set monitored switches as an unempty list first!");
			}

			startProviderInternal();
			started = true;
		}
	}

	public final void stopProvider() {
		if (!initialized) {
			throw new IllegalStateException("Must first initialize the Provider!");
		}

		synchronized (this) {
			if (!started) {
				return;
			}

			stopProviderInternal();
			started = false;
		}
	}

	protected final void fireSwitchStateChanged(int switchNumber, boolean state) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ISwitchStateListener.class) {
				((ISwitchStateListener) listeners[i + 1]).switchStateChanged(switchNumber, state);
			}
		}
	}

	protected abstract Map<Integer, Boolean> getCurrentStateInternal() throws IOException;

	protected abstract List<Integer> getSupportedSwitchesInternal();

	protected void initializeInternal() throws IOException {
	}

	protected abstract void startProviderInternal();

	protected abstract void stopProviderInternal();
}
