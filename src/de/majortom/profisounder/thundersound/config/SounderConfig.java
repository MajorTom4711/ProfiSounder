package de.majortom.profisounder.thundersound.config;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.majortom.profisounder.thundersound.switches.Switch;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SounderConfig {
	@XmlElement(required = true)
	private ProviderWrapper switchStateProvider;

	@XmlElement(name = "switch", required = true)
	private List<Switch> switches;

	@XmlAttribute(required = false)
	private Integer defaultSampleRate = 5000;

	public SounderConfig() {
	}

	public int getDefaultSampleRate() {
		return defaultSampleRate;
	}

	public List<Switch> getSwitches() {
		return switches;
	}

	public ProviderWrapper getSwitchStateProvider() {
		return switchStateProvider;
	}
}
