package de.majortom.profisounder.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.majortom.profisounder.thundersound.Consts;

public class GUISettings {
	public boolean active = true;
	public boolean muted = false;

	public float gain = 1f;

	private String path;
	private Logger logger = Logger.getLogger(this.getClass().getName());

	public GUISettings(String path) {
		this.path = path;
	}

	public void readSettings() {
		Properties prop = new Properties();

		try {
			File file = new File(path);
			if (!file.exists())
				saveSettings();

			try (InputStream inputStream = new FileInputStream(file)) {
				prop.load(inputStream);

				gain = Float.parseFloat(prop.getProperty("volume", "1.0"));
				if (gain < 0 || gain > Consts.MAX_GAIN) {
					gain = 1;
					throw new IllegalArgumentException("gain must be between 0 and " + Consts.MAX_GAIN);
				}

				active = Boolean.parseBoolean(prop.getProperty("active", "true"));
				muted = Boolean.parseBoolean(prop.getProperty("muted", "false"));
			} catch (Exception e) {
				throw e;
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Unable to store settings!", e);
		}
	}

	public void saveSettings() {
		Properties prop = new Properties();

		try {
			File file = new File(path);
			try (OutputStream outputStream = new FileOutputStream(file)) {
				prop.setProperty("active", Boolean.toString(active));
				prop.setProperty("muted", Boolean.toString(muted));

				prop.setProperty("volume", Float.toString(gain));

				prop.store(outputStream, "");
			} catch (Exception e) {
				throw e;
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Unable to store settings!", e);
		}
	}
}
