package de.majortom.profisounder.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import de.majortom.profisounder.ThunderSounderStandalone;
import de.majortom.profisounder.gui.components.JTooltipTable;
import de.majortom.profisounder.notifications.ILogUpdatedListener;
import de.majortom.profisounder.notifications.LogUpdatedEvent;
import de.majortom.profisounder.notifications.ProfiSounderLogAppender;
import de.majortom.profisounder.types.Message;

public class ProfisounderGUI extends JDialog implements ILogUpdatedListener {
	private static final long serialVersionUID = -6094576807363459690L;

	@SuppressWarnings("serial")
	private class MessageTableModel extends AbstractTableModel {
		private final DateFormat df = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS");
		private List<Message> messageLog;

		public MessageTableModel(List<Message> messageLog) {
			this.messageLog = messageLog;
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public int getRowCount() {
			return messageLog.size();
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return messages.getString("dialogs.gui.logtables.colTime");
			case 1:
				return messages.getString("dialogs.gui.logtables.colLevel");
			default:
				return messages.getString("dialogs.gui.logtables.colMessage");
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Message msg = messageLog.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return df.format(msg.getDate());
			case 1:
				return msg.getLevel();
			default:
				return msg.getMessage();
			}
		}

		public void addMessage(Message msg) {
			messageLog.add(msg);
			Collections.sort(messageLog);

			fireTableDataChanged();
		}
	}

	private JPanel contentPane;

	private ThunderSounderStandalone standalone;
	private ProfiSounderLogAppender appender;
	private ResourceBundle messages;
	private GUISettings settings;

	private DecimalFormat df = new DecimalFormat("0.0");
	private boolean ignoreSliderAction = false;
	private boolean windowActive = true;

	private JToggleButton tglbtnMute;
	private JToggleButton tglbtnSounderActive;
	private JTooltipTable tableLogMessages;
	private JTooltipTable tableCriticalMessages;

	private MessageTableModel mtmCriticalMessages;
	private MessageTableModel mtmLogMessages;

	public ProfisounderGUI(GUISettings settings, ResourceBundle messages, ProfiSounderLogAppender appender, ThunderSounderStandalone exitRequests) {
		this.settings = settings;
		this.messages = messages;

		this.appender = appender;
		this.standalone = exitRequests;

		appender.addLockUpdatedListener(this);

		initGUI();
	}

	@Override
	public void logUpdated(LogUpdatedEvent luEvent) {
		if (luEvent.isLogUpdated())
			mtmLogMessages.addMessage(luEvent.getMessage());
		else
			mtmCriticalMessages.addMessage(luEvent.getMessage());
	}

	public GUISettings getSettings() {
		return settings;
	}

	public boolean isWindowActive() {
		return windowActive;
	}

	private void initGUI() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(0, 0, 600, 500);
		setMinimumSize(new Dimension(600, 500));

		setTitle(messages.getString("dialogs.gui.title"));
		setResizable(true);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 1.0, 0.01, 0.01 };
		gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0 };
		contentPane.setLayout(gbl_contentPane);

		JScrollPane scrollPaneCritical = new JScrollPane();
		scrollPaneCritical.setBorder(new TitledBorder(messages.getString("dialogs.gui.border.critical")));
		GridBagConstraints gbc_scrollPaneCritical = new GridBagConstraints();
		gbc_scrollPaneCritical.gridheight = 4;
		gbc_scrollPaneCritical.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPaneCritical.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneCritical.gridx = 0;
		gbc_scrollPaneCritical.gridy = 0;
		contentPane.add(scrollPaneCritical, gbc_scrollPaneCritical);

		tableCriticalMessages = new JTooltipTable();
		scrollPaneCritical.setViewportView(tableCriticalMessages);

		tglbtnSounderActive = new JToggleButton();
		tglbtnSounderActive.setSelected(settings.active && standalone.getSounder().isStarted());

		GridBagConstraints gbc_tglbtnSounderActive = new GridBagConstraints();
		gbc_tglbtnSounderActive.anchor = GridBagConstraints.NORTH;
		gbc_tglbtnSounderActive.gridwidth = 2;
		gbc_tglbtnSounderActive.fill = GridBagConstraints.HORIZONTAL;
		gbc_tglbtnSounderActive.insets = new Insets(0, 0, 5, 0);
		gbc_tglbtnSounderActive.gridx = 1;
		gbc_tglbtnSounderActive.gridy = 0;
		contentPane.add(tglbtnSounderActive, gbc_tglbtnSounderActive);

		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(new Integer(0), new JLabel("0"));
		labelTable.put(new Integer(4), new JLabel("0.4"));
		labelTable.put(new Integer(8), new JLabel("0.8"));
		labelTable.put(new Integer(12), new JLabel("1.2"));
		labelTable.put(new Integer(16), new JLabel("1.6"));
		labelTable.put(new Integer(20), new JLabel("2"));

		tglbtnMute = new JToggleButton();
		tglbtnMute.setSelected(settings.muted);

		GridBagConstraints gbc_tglbtnMute = new GridBagConstraints();
		gbc_tglbtnMute.anchor = GridBagConstraints.NORTH;
		gbc_tglbtnMute.fill = GridBagConstraints.HORIZONTAL;
		gbc_tglbtnMute.gridwidth = 2;
		gbc_tglbtnMute.insets = new Insets(0, 0, 5, 0);
		gbc_tglbtnMute.gridx = 1;
		gbc_tglbtnMute.gridy = 1;
		contentPane.add(tglbtnMute, gbc_tglbtnMute);

		final JLabel lblGain = new JLabel(messages.getString("dialogs.gui.gain.label"));
		GridBagConstraints gbc_lblGain = new GridBagConstraints();
		gbc_lblGain.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblGain.gridwidth = 2;
		gbc_lblGain.insets = new Insets(5, 0, 5, 0);
		gbc_lblGain.gridx = 1;
		gbc_lblGain.gridy = 2;
		contentPane.add(lblGain, gbc_lblGain);

		final JSlider sliderVolume = new JSlider();
		sliderVolume.setLabelTable(labelTable);

		sliderVolume.setMinorTickSpacing(1);
		sliderVolume.setMajorTickSpacing(4);
		sliderVolume.setMaximum(20);
		sliderVolume.setPaintLabels(true);
		sliderVolume.setPaintTicks(true);
		sliderVolume.setSnapToTicks(true);

		GridBagConstraints gbc_sliderVolume = new GridBagConstraints();
		gbc_sliderVolume.gridwidth = 2;
		gbc_sliderVolume.insets = new Insets(0, 0, 5, 0);
		gbc_sliderVolume.fill = GridBagConstraints.HORIZONTAL;
		gbc_sliderVolume.anchor = GridBagConstraints.NORTH;
		gbc_sliderVolume.gridx = 1;
		gbc_sliderVolume.gridy = 3;
		contentPane.add(sliderVolume, gbc_sliderVolume);

		sliderVolume.setValue((int) (settings.muted ? 0 : settings.gain * 10));
		sliderVolume.setEnabled(!settings.muted);

		lblGain.setText(messages.getString("dialogs.gui.gain.label") + ": " + df.format(sliderVolume.getValue() / 10f));

		JScrollPane scrollPaneMsgs = new JScrollPane();
		scrollPaneMsgs.setBorder(new TitledBorder(messages.getString("dialogs.gui.border.messages")));
		GridBagConstraints gbc_scrollPaneMsgs = new GridBagConstraints();
		gbc_scrollPaneMsgs.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPaneMsgs.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneMsgs.gridx = 0;
		gbc_scrollPaneMsgs.gridy = 4;
		contentPane.add(scrollPaneMsgs, gbc_scrollPaneMsgs);

		tableLogMessages = new JTooltipTable();
		scrollPaneMsgs.setViewportView(tableLogMessages);

		JButton btnHide = new JButton("Hide");
		GridBagConstraints gbc_btnHide = new GridBagConstraints();
		gbc_btnHide.anchor = GridBagConstraints.SOUTH;
		gbc_btnHide.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnHide.insets = new Insets(0, 0, 0, 5);
		gbc_btnHide.gridx = 1;
		gbc_btnHide.gridy = 4;
		contentPane.add(btnHide, gbc_btnHide);

		JButton btnExit = new JButton("Exit");
		GridBagConstraints gbc_btnExit = new GridBagConstraints();
		gbc_btnExit.anchor = GridBagConstraints.SOUTH;
		gbc_btnExit.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnExit.gridx = 2;
		gbc_btnExit.gridy = 4;
		contentPane.add(btnExit, gbc_btnExit);

		mtmCriticalMessages = new MessageTableModel(appender.getPersistentMessages());
		mtmLogMessages = new MessageTableModel(appender.getLogBuffer());

		tableCriticalMessages.setModel(mtmCriticalMessages);
		tableLogMessages.setModel(mtmLogMessages);

		tableCriticalMessages.setAutoCreateRowSorter(true);
		tableLogMessages.setAutoCreateRowSorter(true);

		changeActiveLabel();
		changeMuteLabel();

		tglbtnSounderActive.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				changeActiveLabel();
				if (tglbtnSounderActive.isSelected()) {
					settings.active = true;
					standalone.getSounder().start();
				} else {
					settings.active = false;
					standalone.getSounder().stop();
				}
			}
		});

		tglbtnMute.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ignoreSliderAction = true;

				changeMuteLabel();
				if (tglbtnMute.isSelected()) {
					settings.muted = true;
					standalone.getSounder().setGain(0);

					sliderVolume.setValue(0);
				} else {
					settings.muted = false;
					standalone.getSounder().setGain(settings.gain);

					sliderVolume.setValue((int) (settings.gain * 10));
				}

				sliderVolume.setEnabled(!settings.muted);
				ignoreSliderAction = false;
			}
		});

		sliderVolume.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if (!ignoreSliderAction) {
					standalone.getSounder().setGain(sliderVolume.getValue() / 10f);

					settings.gain = sliderVolume.getValue() / 10f;
					lblGain.setText(messages.getString("dialogs.gui.gain.label") + ": " + df.format(sliderVolume.getValue() / 10f));
				}
			}
		});

		btnExit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				close(true);
			}
		});

		btnHide.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				close(false);
			}
		});

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				windowActive = false;
			}
		});
	}

	private void close(boolean exitTool) {
		settings.saveSettings();
		appender.removeLockUpdatedListener(this);

		if (exitTool)
			standalone.requestExit(ProfisounderGUI.this);
		else
			dispose();
	}

	private void changeMuteLabel() {
		if (tglbtnMute.isSelected())
			tglbtnMute.setText(messages.getString("dialogs.gui.mutebutton.active"));
		else
			tglbtnMute.setText(messages.getString("dialogs.gui.mutebutton.inactive"));
	}

	private void changeActiveLabel() {
		if (tglbtnSounderActive.isSelected())
			tglbtnSounderActive.setText(messages.getString("dialogs.gui.sounderactivebutton.active"));
		else
			tglbtnSounderActive.setText(messages.getString("dialogs.gui.sounderactivebutton.inactive"));
	}
}
