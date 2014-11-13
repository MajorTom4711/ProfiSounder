package de.majortom.profisounder.switchstateprovider.impl.mapping;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class SwitchMapping {
	@XmlAttribute(required = true)
	private Integer switchNumber;
	@XmlAttribute(required = true)
	private Integer rpiPin;

	@XmlAttribute(required = false)
	private Integer rpiPinFeedbackHigh = -1;

	@XmlAttribute(required = false)
	private Boolean pullUp = true;

	public Integer getRpiPin() {
		return rpiPin;
	}

	public Integer getSwitchNumber() {
		return switchNumber;
	}

	public Integer getRpiPinFeedbackHigh() {
		return rpiPinFeedbackHigh;
	}

	public Boolean isPullUp() {
		return pullUp;
	}

	@Override
	public String toString() {
		return rpiPin + " -> " + switchNumber;
	}
}
