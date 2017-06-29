package com.nebustack.gui.table;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.nebustack.file.Frame;
import com.nebustack.gui.preview.Preview;

public final class SelectionListener implements ListSelectionListener {
	private final TableModel model;
	private final Preview preview;
	private final JTable table;

	public SelectionListener(TableModel model, Preview preview, JTable table) {
		this.model = model;
		this.preview = preview;
		this.table = table;
	}

	@Override
	public void valueChanged(ListSelectionEvent event) {
		if (event.getValueIsAdjusting()) return;

		int row = table.getSelectedRow();
		if (row < 0) return;

		Frame frame = model.get(row);
		preview.setFrame(frame);
	}
}
