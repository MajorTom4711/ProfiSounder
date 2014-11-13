package de.majortom.profisounder.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.majortom.profisounder.ThunderSounderStandalone;
import de.majortom.profisounder.thundersound.ThunderSounder;

public class ProfisounderGUI extends JFrame {
	private static final long serialVersionUID = -6094576807363459690L;

	private JPanel contentPane;

	private ThunderSounderStandalone standalone;
	private ResourceBundle messages;

	private ThunderSounder sounder;
	private GUISettings settings;

	private DecimalFormat df = new DecimalFormat("0.0");
	private boolean ignoreSliderAction = false;

	private JToggleButton tglbtnMute;
	private JToggleButton tglbtnSounderActive;

	public ProfisounderGUI(ThunderSounder sounder, GUISettings settings, ResourceBundle messages, ThunderSounderStandalone exitRequests) {
		this.sounder = sounder;
		this.settings = settings;

		this.messages = messages;
		this.standalone = exitRequests;

		initGUI();
	}

	public GUISettings getSettings() {
		return settings;
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

		JTextPane textPaneCritical = new JTextPane();
		textPaneCritical.setEditable(false);
		scrollPaneCritical.setViewportView(textPaneCritical);

		tglbtnSounderActive = new JToggleButton();
		tglbtnSounderActive.setSelected(settings.active && sounder.isStarted());

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
		lblGain.setText(messages.getString("dialogs.gui.gain.label") + ": " + df.format(sliderVolume.getValue() / 10f));

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

		JScrollPane scrollPaneMsgs = new JScrollPane();
		scrollPaneMsgs.setBorder(new TitledBorder(messages.getString("dialogs.gui.border.messages")));
		GridBagConstraints gbc_scrollPaneMsgs = new GridBagConstraints();
		gbc_scrollPaneMsgs.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPaneMsgs.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneMsgs.gridx = 0;
		gbc_scrollPaneMsgs.gridy = 4;
		contentPane.add(scrollPaneMsgs, gbc_scrollPaneMsgs);

		JTextPane textPaneMsgs = new JTextPane();
		scrollPaneMsgs.setViewportView(textPaneMsgs);

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

		changeActiveLabel();
		changeMuteLabel();

		tglbtnSounderActive.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				changeActiveLabel();
				if (tglbtnSounderActive.isSelected()) {
					settings.active = true;
					sounder.start();
				} else {
					settings.active = false;
					sounder.stop();
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
					sounder.setGain(0);

					sliderVolume.setValue(0);
				} else {
					settings.muted = false;
					sounder.setGain(settings.gain);

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
					sounder.setGain(sliderVolume.getValue() / 10f);

					settings.gain = sliderVolume.getValue() / 10f;
					lblGain.setText(messages.getString("dialogs.gui.gain.label") + ": " + df.format(sliderVolume.getValue() / 10f));
				}
			}
		});

		btnExit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				standalone.requestExit(ProfisounderGUI.this);
			}
		});

		btnHide.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ProfisounderGUI.this.settings.saveSettings();
				dispose();
			}
		});
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
