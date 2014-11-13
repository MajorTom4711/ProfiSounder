package de.majortom.profisounder.switchstateprovider.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.gpio.event.PinEventType;

import de.majortom.profisounder.switchstateprovider.AbstractSwitchStateProvider;
import de.majortom.profisounder.switchstateprovider.impl.mapping.SwitchMapping;

@XmlAccessorType(XmlAccessType.NONE)
public class RpiGPIOStateProvider extends AbstractSwitchStateProvider {
	private class SwitchChangeFeedback extends Thread {
		private GPIOPin sourcePin;
		private GpioPinDigitalOutput outputPin;

		public SwitchChangeFeedback(GPIOPin sourcePin, GpioPinDigitalOutput outputPin) {
			this.sourcePin = sourcePin;
			this.outputPin = outputPin;
		}

		@Override
		public void run() {
			Future<?> blink = outputPin.blink(100, 1000);
			try {
				blink.get();
			} catch (Exception e) {
			} finally {
				outputPin.setState(sourcePin.getStateNF());
			}
		}
	}

	private class GPIOPin implements GpioPinListenerDigital {
		private SwitchMapping mapping;

		private GpioPinDigitalInput pin;
		private GpioPinDigitalOutput pinFeedback = null;

		public GPIOPin(SwitchMapping mapping) {
			this.mapping = mapping;

			Pin pinForNumber = getPinForNumber(mapping.getRpiPin());
			if (pinForNumber == null)
				throw new IllegalArgumentException("Wrong Settings for configured Pin: " + mapping + "!");

			pin = gpio.provisionDigitalInputPin(pinForNumber, PinPullResistance.OFF);

			Pin pinFBForNumber = getPinForNumber(mapping.getRpiPinFeedbackHigh());
			if (pinFBForNumber != null)
				pinFeedback = gpio.provisionDigitalOutputPin(pinFBForNumber, PinState.LOW);
		}

		public boolean getState() {
			doFeedback();
			return getStateNF();
		}

		public boolean getStateNF() {
			return getState(pin.isHigh());
		}

		@Override
		public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
			if (started && event.getEventType() == PinEventType.DIGITAL_STATE_CHANGE) {
				fireSwitchStateChanged(mapping.getSwitchNumber(), getState(event.getState().isHigh()));
				doFeedback();
			}
		}

		public void setListenerEnabled(boolean enabled) {
			if (enabled)
				pin.addListener(this);
			else
				pin.removeListener(this);
		}

		private boolean getState(boolean physicalIsHigh) {
			return (mapping.isPullUp() && !physicalIsHigh) || (!mapping.isPullUp() && physicalIsHigh);
		}

		private void doFeedback() {
			if (pinFeedback != null) {
				SwitchChangeFeedback bf = new SwitchChangeFeedback(this, pinFeedback);
				bf.start();
			}
		}
	}

	@XmlElement(name = "switchMapping", required = true)
	private List<SwitchMapping> switchMappings;

	private GpioController gpio;
	private Map<Integer, GPIOPin> switchPinMapping;

	@Override
	public Map<Integer, Boolean> getCurrentStateInternal() {
		Map<Integer, Boolean> currentState = new HashMap<Integer, Boolean>();
		for (Integer monitored : monitoredSwitches) {
			currentState.put(monitored, switchPinMapping.get(monitored).getState());
		}

		return currentState;
	}

	@Override
	public List<Integer> getSupportedSwitchesInternal() {
		List<Integer> supportedSwitches = new ArrayList<Integer>();
		for (SwitchMapping sm : switchMappings) {
			supportedSwitches.add(sm.getSwitchNumber());
		}

		return supportedSwitches;
	}

	@Override
	protected void initializeInternal() throws IOException {
		gpio = GpioFactory.getInstance();

		switchPinMapping = new HashMap<Integer, GPIOPin>();
		for (SwitchMapping sm : switchMappings) {
			if (sm.getRpiPin() < 0 || sm.getRpiPin() > 20)
				throw new IndexOutOfBoundsException("Config invalid! Raspi Pin Number must be between 0 and 20!");

			switchPinMapping.put(sm.getSwitchNumber(), new GPIOPin(sm));
		}
	}

	@Override
	protected void startProviderInternal() {
		for (Integer mSwitch : monitoredSwitches) {
			switchPinMapping.get(mSwitch).setListenerEnabled(true);
		}
	}

	@Override
	protected void stopProviderInternal() {
		for (GPIOPin pin : switchPinMapping.values()) {
			pin.setListenerEnabled(false);
		}
	}

	private Pin getPinForNumber(int number) {
		switch (number) {
		case 0:
			return RaspiPin.GPIO_00;
		case 1:
			return RaspiPin.GPIO_01;
		case 2:
			return RaspiPin.GPIO_02;
		case 3:
			return RaspiPin.GPIO_03;
		case 4:
			return RaspiPin.GPIO_04;
		case 5:
			return RaspiPin.GPIO_05;
		case 6:
			return RaspiPin.GPIO_06;
		case 7:
			return RaspiPin.GPIO_07;
		case 8:
			return RaspiPin.GPIO_08;
		case 9:
			return RaspiPin.GPIO_09;
		case 10:
			return RaspiPin.GPIO_10;
		case 11:
			return RaspiPin.GPIO_11;
		case 12:
			return RaspiPin.GPIO_12;
		case 13:
			return RaspiPin.GPIO_13;
		case 14:
			return RaspiPin.GPIO_14;
		case 15:
			return RaspiPin.GPIO_15;
		case 16:
			return RaspiPin.GPIO_16;
		case 17:
			return RaspiPin.GPIO_17;
		case 18:
			return RaspiPin.GPIO_18;
		case 19:
			return RaspiPin.GPIO_19;
		case 20:
			return RaspiPin.GPIO_20;
		}

		return null;
	}
}
