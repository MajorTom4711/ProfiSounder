package de.majortom.profisounder.thundersound.switches.statechanges.actions.playtypes;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Control;
import javax.sound.sampled.Control.Type;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioPlayer {
	private class AudiostreamPreloader extends Thread {
		private AudioInputStream audioInputStream = null;
		private AudioInputStream oldstream = null;

		public AudiostreamPreloader(AudioInputStream oldstream) {
			setName("Audiostream Preloader: " + audioFile.getPath());
			setDaemon(true);

			this.oldstream = oldstream;
		}

		public void close() {
			synchronized (this) {
				close(oldstream);
				close(audioInputStream);
			}
		}

		public AudioInputStream getAudioInputStream() {
			return audioInputStream;
		}

		@Override
		public void run() {
			synchronized (this) {
				try {
					close(oldstream);

					AudioInputStream stream = AudioSystem.getAudioInputStream(audioFile.toURI().toURL());
					audioInputStream = AudioSystem.getAudioInputStream(outFormat, stream);
				} catch (Exception ex) {
					Logger logger = Logger.getLogger(AudioPlayer.class.getName());
					logger.log(Level.SEVERE, messages.getString("errors.playsound.loading"), ex);
				}
			}
		}

		private void close(AudioInputStream stream) {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private class PlaybackThread extends Thread {
		public PlaybackThread() {
			setName("Playback Audiofile: " + audioFile.getPath());
			setDaemon(true);
		}

		@Override
		public void run() {
			if (line != null) {
				try {
					line.start();
					int loop = -1;

					AudioInputStream stream = null;
					AudioInputStream oldstream = null;

					AudiostreamPreloader preloader = new AudiostreamPreloader(null);
					preloader.start();

					while (!stopped && ((loopCount == Clip.LOOP_CONTINUOUSLY) || (loop++ < loopCount))) {
						preloader.join();
						stream = preloader.getAudioInputStream();

						preloader = new AudiostreamPreloader(oldstream);
						preloader.start();

						stream(stream, line);
						oldstream = stream;
					}

					playing = false;
					stopped = true;

					line.stop();
					line.close();

					preloader.close();
				} catch (Exception e) {
					Logger logger = Logger.getLogger(AudioPlayer.class.getName());
					logger.log(Level.SEVERE, messages.getString("errors.playsound.playback"), e);
				}
			}
		}

		private void stream(AudioInputStream in, SourceDataLine line) throws IOException {
			int fs = line.getFormat().getFrameSize();

			final byte[] buffer = new byte[4096];
			for (int n = 0; n != -1; n = in.read(buffer, 0, buffer.length)) {
				if (stopped) {
					return;
				}

				int numberToWrite = (n / fs) * fs;
				line.write(buffer, 0, numberToWrite);
			}
		}
	}

	private ResourceBundle messages;
	private File audioFile;

	private SourceDataLine line;
	private AudioFormat outFormat;

	private int loopCount = 0;

	private volatile boolean stopped = false;
	private volatile boolean playing = false;

	public AudioPlayer(File audioFile, ResourceBundle messages) throws IOException {
		this.messages = messages;
		try (AudioInputStream in = AudioSystem.getAudioInputStream(audioFile.toURI().toURL())) {
			this.audioFile = audioFile;

			outFormat = getOutFormat(in.getFormat());
			Info info = new Info(SourceDataLine.class, outFormat);

			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(outFormat);
		} catch (UnsupportedAudioFileException | LineUnavailableException e) {
			throw new IOException(e);
		}
	}

	public void close() {
		synchronized (this) {
			stopped = true;
		}
	}

	public Control getControl(Type control) {
		return line.getControl(control);
	}

	public boolean isActive() {
		synchronized (this) {
			return playing;
		}
	}

	public boolean isControlSupported(Type control) {
		return line.isControlSupported(control);
	}

	public void setLoopCount(int loopCount) {
		this.loopCount = loopCount;
	}

	public void start() {
		synchronized (this) {
			if (playing || stopped) {
				return;
			}

			playing = true;
			new PlaybackThread().start();
		}
	}

	private AudioFormat getOutFormat(AudioFormat inFormat) {
		int ch = inFormat.getChannels();
		float rate = inFormat.getSampleRate();

		return new AudioFormat(Encoding.PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
	}
}
