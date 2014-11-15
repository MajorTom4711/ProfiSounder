package de.majortom.profisounder.gui.components;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

public class JTooltipTable extends JTable {
	private static final long serialVersionUID = -5130202043996080424L;

	public String getToolTipText(MouseEvent e) {
		Point p = e.getPoint();

		int rowIndex = rowAtPoint(p);
		int colIndex = columnAtPoint(p);

		try {
			return getValueAt(rowIndex, colIndex).toString();
		} catch (RuntimeException e1) {
			return null;
		}
	}
}