package de.majortom.profisounder.thundersound;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXB;

import de.majortom.profisounder.switchstateprovider.AbstractSwitchStateProvider;
import de.majortom.profisounder.switchstateprovider.events.ISwitchStateListener;
import de.majortom.profisounder.thundersound.config.SounderConfig;
import de.majortom.profisounder.thundersound.switches.Switch;
import de.majortom.profisounder.thundersound.switches.statechanges.AbstractAction;

public class ThunderSounder implements ISounderInterface, ISwitchStateListener {
	private ResourceBundle messages;
	private AbstractSwitchStateProvider atsp;

	private Map<Integer, Switch> switches = new HashMap<Integer, Switch>();
	private Map<Integer, Integer> switchSamplingRates = new HashMap<Integer, Integer>();

	private List<AbstractAction> actions = new ArrayList<AbstractAction>();

	private int defaultSamplingRate;
	private boolean started = false;
	private float gain = 1f;

	private Logger logger = Logger.getLogger(ThunderSounder.class.getName());

	public ThunderSounder(File xmlConfigFile, ResourceBundle messages) {
		this.messages = messages;

		System.out.println(xmlConfigFile.getAbsolutePath());
		SounderConfig config = JAXB.unmarshal(xmlConfigFile, SounderConfig.class);

		try {
			// Sort out config
			atsp = config.getSwitchStateProvider().getSwitchStateprovider();
			atsp.setSamplingRateMS(config.getDefaultSampleRate());
			atsp.initialize();

			defaultSamplingRate = config.getDefaultSampleRate();

			List<Integer> supportedSwitches = atsp.getSupportedSwitches();
			for (Switch sw : config.getSwitches()) {
				if (!supportedSwitches.contains(sw.getSwitchNumber())) {
					throw new IllegalArgumentException(MessageFormat.format(messages.getString("errors.switch.notsupported"), sw.getSwitchNumber()));
				}

				sw.initialize(this, messages);
				sw.setGain(gain);

				switches.put(sw.getSwitchNumber(), sw);
				actions.addAll(sw.getAllActions());
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage() == null ? messages.getString("errors.init.configuration") : ex.getMessage(), ex);
		}
	}

	@Override
	public List<AbstractAction> getActionsForSwitch(int switchNumber) {
		Switch theSwitch = switches.get(switchNumber);
		if (theSwitch != null) {
			return theSwitch.getAllActions();
		}

		return new ArrayList<AbstractAction>();
	}

	@Override
	public List<AbstractAction> getAllActions() {
		return actions;
	}

	public float getGain() {
		return gain;
	}

	public void init(ISwitchStateListener parent) {
		atsp.setMonitoredSwitches(switches.keySet());
		atsp.addSwitchStateListener(this);
		atsp.addSwitchStateListener(parent);
	}

	public boolean isStarted() {
		synchronized (this) {
			return started;
		}
	}

	public void setGain(float gain) {
		if (gain < 0 || gain > 2)
			throw new IllegalArgumentException("The volume must be between 0.0001 and 2!");

		for (Switch sw : switches.values()) {
			sw.setGain(gain);
		}
	}

	public void start() {
		synchronized (this) {
			try {
				Map<Integer, Boolean> currentState = atsp.getCurrentState();

				List<Switch> switchesSort = new ArrayList<Switch>(switches.values());
				Collections.sort(switchesSort);

				for (Switch theSwitch : switchesSort) {
					try {
						int samplingRate = theSwitch.setInitialState(currentState.get(theSwitch.getSwitchNumber()), this, messages);
						switchSamplingRates.put(theSwitch.getSwitchNumber(), samplingRate);
					} catch (Exception ex) {
						logger.log(Level.SEVERE, messages.getString("switch.stateevent.error"), ex);
					}
				}
			} catch (Exception ex) {
				logger.log(Level.SEVERE, messages.getString("switch.stateevent.error"), ex);
			}

			adjustSamplingRate();
			atsp.startProvider();

			if (atsp.getSamplingRateMS() == defaultSamplingRate) {
				logger.log(Level.FINE, MessageFormat.format(messages.getString("sounder.startsampling"), defaultSamplingRate));
			} else {
				logger.log(Level.FINE, MessageFormat.format(messages.getString("sounder.startsampling.nodefault"), atsp.getSamplingRateMS()));
			}

			started = true;
		}
	}

	public void stop() {
		synchronized (this) {
			started = false;

			atsp.stopProvider();

			for (Switch sw : switches.values()) {
				sw.finalize(this, messages);
			}
		}
	}

	@Override
	public void switchStateChanged(int switchNumber, boolean state) {
		logger.log(Level.FINE, MessageFormat.format(messages.getString("switch.stateevent.occured"), switchNumber, state));

		Switch theSwitch = switches.get(switchNumber);
		if (theSwitch != null) {
			try {
				int samplingRate = theSwitch.switchStateChanged(state, this, messages);
				switchSamplingRates.put(switchNumber, samplingRate);
			} catch (Exception ex) {
				logger.log(Level.SEVERE, messages.getString("switch.stateevent.error"), ex);
			}
		}

		adjustSamplingRate();
	}

	private void adjustSamplingRate() {
		int sampling = defaultSamplingRate;
		for (int sr : switchSamplingRates.values()) {
			if (sr > 0) {
				sampling = Math.min(sampling, sr);
			}
		}

		if (atsp.getSamplingRateMS() != sampling) {
			logger.log(Level.FINE, MessageFormat.format(messages.getString("sounder.samplingratechanged"), sampling));
		}

		atsp.setSamplingRateMS(sampling);
	}
}
