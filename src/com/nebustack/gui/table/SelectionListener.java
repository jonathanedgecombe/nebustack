package com.nebustack.gui.table;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.nebustack.file.Frame;

public final class SelectionListener implements ListSelectionListener {
	private final TableModel model;
	private final JTable table;

	public SelectionListener(TableModel model, JTable table) {
		this.model = model;
		this.table = table;
	}

	@Override
	public void valueChanged(ListSelectionEvent event) {
		if (event.getValueIsAdjusting()) return;

		int row = table.getSelectedRow();
		if (row < 0) return;

		Frame frame = model.get(row);
		model.setSelectedFrame(frame);
	}
}
