package com.nebustack.gui.table;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import com.nebustack.file.FrameType;

@SuppressWarnings("serial")
public final class TableContextMenu extends JPopupMenu {
	private final boolean referenceEnabled;

	public TableContextMenu(boolean referenceEnabled, JTable table, TableModel model) {
		this.referenceEnabled = referenceEnabled;
		initialize(table, model);
	}

	private void initialize(JTable table, TableModel model) {
		JMenuItem setReference = new JMenuItem("Set as reference");
		setReference.setEnabled(referenceEnabled);
		setReference.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				if (row >= 0) model.setReference(model.get(row));
			}
		});
		add(setReference);

		addSeparator();

		JMenuItem check = new JMenuItem("Check");
		check.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] rows = table.getSelectedRows();
				for (int row : rows) {
					model.get(row).setChecked(true);
					model.fireTableRowsUpdated(row, row);
				}
			}
		});
		add(check);

		JMenuItem uncheck = new JMenuItem("Uncheck");
		uncheck.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] rows = table.getSelectedRows();
				for (int row : rows) {
					model.get(row).setChecked(false);
					model.fireTableRowsUpdated(row, row);
				}
			}
		});
		add(uncheck);

		addSeparator();

		JMenu changeMenu = new JMenu("Change to...");

		JMenuItem changeLight = new JMenuItem("Light frame");
		changeLight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] rows = table.getSelectedRows();
				for (int row : rows) {
					model.get(row).setType(FrameType.LIGHT);
					model.fireTableRowsUpdated(row, row);
				}
			}
		});
		changeMenu.add(changeLight);

		JMenuItem changeDark = new JMenuItem("Dark frame");
		changeDark.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] rows = table.getSelectedRows();
				for (int row : rows) {
					model.get(row).setType(FrameType.DARK);
					model.get(row).setStars(null);
					model.fireTableRowsUpdated(row, row);
				}
			}
		});
		changeMenu.add(changeDark);

		JMenuItem changeBias = new JMenuItem("Bias frame");
		changeBias.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] rows = table.getSelectedRows();
				for (int row : rows) {
					model.get(row).setType(FrameType.BIAS);
					model.get(row).setStars(null);
					model.fireTableRowsUpdated(row, row);
				}
			}
		});
		changeMenu.add(changeBias);

		JMenuItem changeFlat = new JMenuItem("Flat frame");
		changeFlat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] rows = table.getSelectedRows();
				for (int row : rows) {
					model.get(row).setType(FrameType.FLAT);
					model.get(row).setStars(null);
					model.fireTableRowsUpdated(row, row);
				}
			}
		});
		changeMenu.add(changeFlat);

		JMenuItem changeDarkFlat = new JMenuItem("Dark flat frame");
		changeDarkFlat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] rows = table.getSelectedRows();
				for (int row : rows) {
					model.get(row).setType(FrameType.DARK_FLAT);
					model.get(row).setStars(null);
					model.fireTableRowsUpdated(row, row);
				}
			}
		});
		changeMenu.add(changeDarkFlat);

		add(changeMenu);

		addSeparator();

		JMenuItem remove = new JMenuItem("Remove");
		remove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model.remove(table.getSelectedRows());
			}
		});
		add(remove);
	}
}
