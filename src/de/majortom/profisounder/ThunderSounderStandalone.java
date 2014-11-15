package de.majortom.profisounder;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import de.majortom.profisounder.gui.GUISettings;
import de.majortom.profisounder.gui.ProfisounderGUI;
import de.majortom.profisounder.notifications.IPersistentNotificationConsumer;
import de.majortom.profisounder.notifications.PersistentNotifications;
import de.majortom.profisounder.notifications.ProfiSounderLogAppender;
import de.majortom.profisounder.switchstateprovider.events.ISwitchStateListener;
import de.majortom.profisounder.thundersound.ThunderSounder;
import de.majortom.profisounder.types.Message;

public class ThunderSounderStandalone implements ISwitchStateListener {
	private class KeepAlive extends Thread {
		private Object mon = new Object();

		public KeepAlive() {
			setName("KeepAlive For Headless");
			setDaemon(false);
		}

		@SuppressWarnings("unused")
		public void exit() {
			synchronized (mon) {
				mon.notify();
			}
		}

		@Override
		public void run() {
			synchronized (mon) {
				while (true) {
					try {
						mon.wait();
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}

	private class TaskIconDisplayer extends Thread {
		public static final int ICON_ON_MS = 5000;

		private long waitDuration = -1;

		private Image defaultImage;
		private Image curImage;

		private volatile boolean active = true;

		public TaskIconDisplayer(Image defaultImage) {
			setName(TaskIconDisplayer.class.getSimpleName());
			setDaemon(true);

			this.defaultImage = defaultImage;
		}

		@Override
		public void run() {
			while (active) {
				synchronized (trayIcon) {
					if (curImage != null) {
						trayIcon.setImage(curImage);
						curImage = null;
					} else {
						trayIcon.setImage(defaultImage);
						waitDuration = -1;
					}
				}

				try {
					do {
						if (waitDuration > -1) {
							sleep(waitDuration);
						} else {
							sleep(Integer.MAX_VALUE);
						}
					} while (waitDuration < 0);
				} catch (InterruptedException ex) {
				}
			}
		}

		public void setActive(boolean active) {
			synchronized (trayIcon) {
				this.active = active;

				interrupt();
			}
		}

		public void setImage(Image img, long duration) {
			synchronized (trayIcon) {
				curImage = img;
				waitDuration = duration;

				interrupt();
			}
		}
	}

	public static void main(String[] args) {
		System.setProperty("java.net.preferIPv4Stack", "true");
		ThunderSounderStandalone main = new ThunderSounderStandalone();

		if (main.init()) {
			main.startSounder();
		}
	}

	private BufferedImage trayImage;
	private ProfisounderGUI gui;
	private TaskIconDisplayer tid;
	private SystemTray tray;
	private TrayIcon trayIcon;

	private KeepAlive keepAlive;

	private ResourceBundle messages;
	private ThunderSounder sounder;
	private GUISettings settings;

	private ProfiSounderLogAppender customLogAppender;

	private boolean headless;

	public ThunderSounderStandalone() {
		gui = null;
		headless = GraphicsEnvironment.isHeadless();
	}

	public boolean requestExit(Component parent) {
		if (!headless && JOptionPane.showConfirmDialog(parent, messages.getString("dialogs.exit.confirm"), messages.getString("dialogs.exit.confirm.title"), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
			return false;

		sounder.stop();

		if (!headless) {
			tid.setActive(false);
			tray.remove(trayIcon);
		}

		System.exit(0);
		return true;
	}

	public ThunderSounder getSounder() {
		return sounder;
	}

	@Override
	public void switchStateChanged(int switchNumber, boolean state) {
		if (headless) {
			return;
		}

		if (state) {
			tid.setImage(drawIconImage(trayImage, String.format("%02d", switchNumber), Color.GREEN), TaskIconDisplayer.ICON_ON_MS);
		} else {
			tid.setImage(drawIconImage(trayImage, String.format("%02d", switchNumber), Color.RED), TaskIconDisplayer.ICON_ON_MS);
		}
	}

	private BufferedImage createImage(String path, String description) throws IOException {
		URL imageURL = ThunderSounderStandalone.class.getResource(path);

		if (imageURL == null) {
			throw new IOException(messages.getString("errors.init.trayimagenotfound"));
		} else {
			return ImageIO.read(imageURL.openStream());
		}
	}

	private Image drawIconImage(BufferedImage source, String text, Color clr) {
		BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2d = img.createGraphics();
		g2d.drawImage(source, null, 0, 0);

		if ((text != null) && !text.isEmpty()) {
			g2d.setColor(Color.darkGray);
			g2d.fillRect(1, 1, 13, 13);

			g2d.setFont(new Font("Arial", Font.PLAIN, 12));
			g2d.setColor(clr);
			g2d.drawString(text, 1, 12);
		}

		g2d.dispose();

		return img;
	}

	private boolean init() {
		try {
			Logger global = Logger.getLogger(ThunderSounderStandalone.class.getPackage().getName());

			File logs = new File("logs");
			if (!logs.exists()) {
				logs.mkdirs();
			}

			ConsoleHandler consoleHandler = new ConsoleHandler();
			consoleHandler.setFormatter(new SimpleFormatter());

			FileHandler fileHandler = new FileHandler("logs/profisounder-log.%u.%g.txt", 1024 * 1024, 10, true);
			fileHandler.setFormatter(new SimpleFormatter());

			customLogAppender = new ProfiSounderLogAppender(50);

			global.addHandler(consoleHandler);
			global.addHandler(fileHandler);
			global.addHandler(customLogAppender);

			global.setLevel(Level.FINE);

			messages = ResourceBundle.getBundle("res.messages.messages", Locale.getDefault());
			Message.setResourceBundle(messages);

			sounder = new ThunderSounder(new File("config.xml"), messages);

			if (headless) {
				global.log(Level.FINE, messages.getString("main.start.headless"));
			} else {
				if (!SystemTray.isSupported()) {
					throw new Exception(messages.getString("errors.init.traynotsupported"));
				}

				settings = new GUISettings("config.ini");
				settings.readSettings();

				sounder.setGain(settings.muted ? 0 : settings.gain);

				initTrayIcon();
			}

			Logger.getLogger(this.getClass().getName()).log(Level.FINE, messages.getString("sounder.initialised"));
		} catch (Exception ex) {
			showException("Severe error, unable to continue!", ex, false);
			return false;
		}

		return true;
	}

	private void initTrayIcon() throws IOException {
		final PopupMenu popup = new PopupMenu();

		trayImage = createImage("/res/tray_main.png", "Thundersounder");

		tray = SystemTray.getSystemTray();
		trayIcon = new TrayIcon(trayImage);

		tid = new TaskIconDisplayer(trayImage);

		// Create a pop-up menu components
		MenuItem guiItem = new MenuItem(messages.getString("menu.opengui"));
		guiItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				synchronized (ThunderSounderStandalone.this) {
					if (gui != null && gui.isActive())
						return;

					gui = new ProfisounderGUI(settings, messages, customLogAppender, ThunderSounderStandalone.this);
					gui.setLocationRelativeTo(null);
					gui.setVisible(true);
				}
			}
		});

		MenuItem aboutItem = new MenuItem(messages.getString("menu.about"));
		aboutItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String msg = "<html><center><h1>ProfiSounder</h1></center><br><p>Copyright (C) 2014 Thomas Wagner</p><br>" + "<p>This program is free software; you can redistribute it and/or modify<br>it under the terms of the GNU General Public License as published by<br>the Free Software Foundation; either version 2 of the License, or<br>(at your option) any later version.</p><br>"
						+ "<p>This program is distributed in the hope that it will be useful,<br>but WITHOUT ANY WARRANTY; without even the implied warranty of<br>MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<br>GNU General Public License for more details.</p><br>"
						+ "<p>You should have received a copy of the GNU General Public License along<br>with this program; if not, write to the Free Software Foundation, Inc.<br>51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.</p><br></html>";

				JOptionPane.showMessageDialog(null, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		MenuItem exitItem = new MenuItem(messages.getString("menu.exit"));
		exitItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				synchronized (ThunderSounderStandalone.this) {
					requestExit(null);
				}
			}
		});

		// Add components to pop-up menu
		popup.add(guiItem);
		popup.add(aboutItem);
		popup.addSeparator();
		popup.add(exitItem);

		trayIcon.setPopupMenu(popup);
		trayIcon.setToolTip("ProfiSounder");

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			throw new IOException(messages.getString("errors.init.traynotsupported"));
		}
	}

	private void showException(String message, Throwable t, boolean exit) {
		Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, message, t);

		if (!headless) {
			String simpleName = MessageFormat.format(messages.getString("dialogs.error.header"), t.getClass().getSimpleName());
			String msg = t.getMessage() != null && !t.getMessage().isEmpty() ? "\n\n\"" + t.getMessage() + "\"" : "";

			JOptionPane.showMessageDialog(null, simpleName + msg, "Severe error!", JOptionPane.ERROR_MESSAGE);
		}

		if (exit) {
			System.exit(1);
		}
	}

	private void startSounder() {
		synchronized (ThunderSounderStandalone.this) {
			try {
				sounder.init(this);

				if (headless || settings.active)
					sounder.start();

				if (!headless) {
					tid.start();
				} else {
					keepAlive = new KeepAlive();
					keepAlive.start();
				}
			} catch (Exception ex) {
				showException(messages.getString("main.start.error"), ex, true);
			}
		}
	}
}
