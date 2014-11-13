package de.majortom.profisounder.thundersound.switches.statechanges.actions;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import de.majortom.profisounder.thundersound.ISounderInterface;
import de.majortom.profisounder.thundersound.switches.statechanges.AbstractAction;
import de.majortom.profisounder.thundersound.switches.statechanges.actions.playtypes.AudioPlayer;

@XmlAccessorType(XmlAccessType.NONE)
public class PlaySound extends AbstractAction {
	private class Fader extends Thread {
		private static final int FADE_STEP = 10;

		private float curVolume;
		private float targetVolume;

		private int fadeDurMs;

		private boolean fadeIn;
		private boolean fadeToDie;
		private boolean active = true;

		public Fader(float startVolume, float targetVolume, int fadeDurMs, boolean fadeToDie) {
			setName("Fader: " + soundFile);
			setDaemon(true);

			curVolume = startVolume;
			this.targetVolume = targetVolume;

			this.fadeDurMs = fadeDurMs;
			this.fadeToDie = fadeToDie;

			fadeIn = startVolume <= targetVolume;
		}

		public float getCurVolume() {
			synchronized (this) {
				return curVolume;
			}
		}

		@SuppressWarnings("unused")
		public int getFadeDurMs() {
			synchronized (this) {
				return fadeDurMs;
			}
		}

		public boolean isActive() {
			synchronized (this) {
				return active;
			}
		}

		public boolean isFadeToDie() {
			synchronized (this) {
				return fadeToDie;
			}
		}

		@Override
		public void run() {
			float fadeStep = Math.abs(targetVolume - curVolume) / ((float) fadeDurMs / (float) FADE_STEP);

			while (!isInterrupted()) {
				synchronized (lock) {
					if (fadeIn) {
						curVolume = Math.min(curVolume + fadeStep, 1);

						setCurVolume(curVolume);
						if (curVolume >= targetVolume) {
							finish();
							return;
						}
					} else {
						curVolume = Math.max(curVolume - fadeStep, 0);

						setCurVolume(curVolume);
						if (curVolume <= targetVolume) {
							if (fadeToDie) {
								clip.close();
							}

							finish();
							return;
						}
					}
				}

				fadeDurMs = Math.max(fadeDurMs - FADE_STEP, 0);
				try {
					sleep(FADE_STEP);
				} catch (InterruptedException ex) {
					active = false;
					return;
				}
			}
		}

		public void triggerCurVolume() {
			synchronized (this) {
				setCurVolume(curVolume);
			}
		}

		private void finish() {
			fadeDurMs = 0;
			curVolume = targetVolume;
		}
	}

	private Object lock = new Object();

	@XmlAttribute(required = true)
	private String soundFile;
	private File soundAsFile;

	@XmlAttribute(required = false)
	private Integer fadeInDuration = -1;
	@XmlAttribute(required = false)
	private Integer loopCount = 0;

	@XmlAttribute(required = false)
	private Float volume = 1f;

	private AudioPlayer clip = null;
	private FloatControl control = null;
	private BooleanControl mute = null;

	private Fader fader;

	private float gain = 1f;

	public PlaySound() {
	}

	@Override
	public void doAction(ISounderInterface thunderSounder, ResourceBundle messages) throws IOException {
		synchronized (lock) {
			logger.log(Level.FINE, MessageFormat.format(messages.getString("action.doaction.playsound"), soundFile, loopCount < 0 ? "infinite" : loopCount, fadeInDuration));

			try {
				// if Fading is in Progress, stop thread and rescue volume
				float oldVolume = 0;
				if ((fader != null) && fader.isActive()) {
					oldVolume = fader.getCurVolume();

					fader.interrupt();
					fader = null;
				}

				if ((clip == null) || !clip.isActive()) {
					clip = new AudioPlayer(soundAsFile, messages);

					control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

					if (clip.isControlSupported(BooleanControl.Type.MUTE))
						mute = (BooleanControl) clip.getControl(BooleanControl.Type.MUTE);
				}

				setCurVolume(oldVolume);

				if (loopCount < 0) {
					clip.setLoopCount(Clip.LOOP_CONTINUOUSLY);
				} else {
					clip.setLoopCount(loopCount);
				}

				// If fading, start Thread
				if (fadeInDuration > 0) {
					int fade = getRealFadeDuration(oldVolume, volume, 0, volume, fadeInDuration);

					fader = new Fader(oldVolume, volume, fade, false);
					fader.start();
				} else {
					setCurVolume(volume);
				}

				clip.start();
			} catch (IOException e) {
				throw new IOException(messages.getString("playsound.errors.unabletostartsound"), e);
			}
		}
	}

	@Override
	public void finalize(ISounderInterface thunderSounder, ResourceBundle messages) {
		synchronized (lock) {
			if ((fader != null) && fader.isActive()) {
				fader.interrupt();
				fader = null;
			}

			if (clip != null) {
				clip.close();
			}
		}
	}

	public String getSoundFile() {
		return soundFile;
	}

	@Override
	public void initialize(ISounderInterface thunderSounder, ResourceBundle messages) {
		if (fadeInDuration > 3600000) {
			throw new IllegalArgumentException(messages.getString("errors.switch.fadeoutdurationinvalid"));
		}

		if ((volume < 0) || (volume > 1)) {
			throw new IllegalArgumentException(messages.getString("errors.switch.volumeinvalid"));
		}

		soundAsFile = new File(soundFile);
		if (!soundAsFile.exists()) {
			throw new IllegalArgumentException(MessageFormat.format(messages.getString("errors.switch.soundfileinvalid"), soundFile));
		}
	}

	@Override
	public boolean isActive() {
		synchronized (lock) {
			return ((clip != null) && clip.isActive() && ((fader == null) || !fader.isFadeToDie()));
		}
	}

	@Override
	public void setGain(float gain) {
		synchronized (lock) {
			this.gain = gain;

			if (fader != null && fader.isActive()) {
				fader.triggerCurVolume();
			}
		}
	}

	public void setVolume(float volume) {
		setVolume(volume, 0);
	}

	public void setVolume(float volume, int fadeDuration) {
		synchronized (lock) {
			float oldGlobalVoluem = this.volume;
			this.volume = volume;

			// do nothing here if nothing is played or the clip is currently being faded out
			if ((clip != null) && clip.isActive() && ((fader == null) || !fader.isFadeToDie())) {
				// if Fading is in Progress, stop thread and rescue volume and remaining time
				float oldVolume = oldGlobalVoluem;
				int remainingMS = 0;

				if ((fader != null) && fader.isActive()) {
					oldVolume = fader.getCurVolume();

					// TODO: Hier nochmal prüfen, ob das Addieren der Volume Fadezeit doch besser wirkt
					// remainingMS = fader.getFadeDurMs();

					fader.interrupt();
					fader = null;
				}

				// inc duration by fadeDuration
				remainingMS += fadeDuration;

				// If fading, start Thread
				if (remainingMS > 0) {
					fader = new Fader(oldVolume, volume, remainingMS, false);
					fader.start();
				} else {
					setCurVolume(volume);
				}
			}
		}
	}

	public void stopSound(int fadeOutDuration) {
		synchronized (lock) {
			if (clip == null) {
				return;
			}

			// if Fading is in Progress, stop thread and rescue volume
			float oldVolume = volume;
			if ((fader != null) && fader.isActive()) {
				oldVolume = fader.getCurVolume();

				fader.interrupt();
				fader = null;
			}

			// If fading, start Thread
			if (fadeOutDuration > 0) {
				setCurVolume(oldVolume);
				int fade = getRealFadeDuration(oldVolume, 0f, 0f, volume, fadeOutDuration);

				fader = new Fader(oldVolume, 0, fade, true);
				fader.start();
			} else {
				clip.close();
			}
		}
	}

	private int getRealFadeDuration(float curValue, float targetValue, float spanMin, float spanMax, int setDuration) {
		// TODO: Prüfen ob Buggy

		float spanRng = Math.abs(targetValue - curValue);
		float spanOrg = Math.abs(spanMax - spanMin);

		float ratio = spanRng / spanOrg;

		return (int) Math.min(setDuration, setDuration * ratio);
	}

	private void setCurVolume(float volume) {
		if (control == null) {
			return;
		}

		float vol = volume * gain;

		if (vol > 0) {
			if (mute != null && mute.getValue())
				mute.setValue(false);

			float value = (vol <= 0.0f) ? 0.0001f : ((vol > 2.0f) ? 2.0f : vol);
			float dB = (float) ((Math.log(value) / Math.log(10.0)) * 20.0);

			control.setValue(Math.max(-50, dB));
		} else {
			if (mute != null && !mute.getValue())
				mute.setValue(true);
		}
	}
}
