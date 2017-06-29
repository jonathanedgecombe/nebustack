package com.nebustack.gui.table;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.nebustack.file.Frame;

@SuppressWarnings("serial")
public class TableModel extends AbstractTableModel {
	private final static String[] COLUMN_NAMES = {"", "#", "Type", "File", "Date/Time", "Camera", "Channel(s)", "ISO/Gain", "Temperature", "Size", "Exposure", "Stars", "FWHM"};

	private final static DecimalFormat DF = new DecimalFormat("#.##");

	private final List<Frame> frames = new ArrayList<>();
	private Frame reference = null;

	public synchronized List<Frame> getFrames() {
		List<Frame> clone = new ArrayList<>();
		clone.addAll(frames);
		return clone;
	}

	public synchronized Frame getReference() {
		if (!frames.contains(reference)) reference = null;
		return reference;
	}

	public synchronized void setReference(Frame reference) {
		this.reference = reference;
		int index = frames.indexOf(reference);
		fireTableCellUpdated(index, 3);
	}

	public synchronized void add(Frame entry) {
		frames.add(entry);
		fireTableDataChanged();
	}

	public synchronized void clear() {
		frames.clear();
		reference = null;
		fireTableDataChanged();
	}

	public synchronized Frame get(int index) {
		return frames.get(index);
	}

	public synchronized void update(Frame descriptor) {
		int index = frames.indexOf(descriptor);
		if (index == -1) return;

		fireTableRowsUpdated(index, index);
	}

	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	@Override
	public synchronized int getRowCount() {
		return frames.size();
	}

	@Override
	public Object getValueAt(int row, int column) {
		Frame frame = get(row);

		switch (column) {
		case 0:
			return frame.isChecked();
		case 1:
			return row + 1;
		case 2:
			return frame.getType().toString();
		case 3:
			String name = frame.getName();
			if (frame.equals(reference)) name = "* " + name;
			return name;
		case 4:
			return frame.getDateTimeString();
		case 5:
			return frame.getCameraTelescope();
		case 6:
			return frame.getColorFormat();
		case 7:
			return frame.getGain() == 0 ? "-" : Integer.toString(frame.getGain());
		case 8:
			return Double.isNaN(frame.getTemperature()) ? "-" : (DF.format(frame.getTemperature()) + "\u00B0c");
		case 9:
			return frame.getResolution();
		case 10:
			return Double.isNaN(frame.getExposure()) ? "-" : (DF.format(frame.getExposure()) + "s");
		case 11:
			return frame.getStars() == null ? "-" : Integer.toString(frame.getStars().size());
		case 12:
			return frame.getStars() == null ? "-" : DF.format(frame.getFWHM());
		}

		throw new RuntimeException("Invalid column");
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return column == 0;
	}

	@Override
	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}

	@Override
	public Class<?> getColumnClass(int column) {
		switch (column) {
		case 0:
			return Boolean.class;
		}

		return Object.class;
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
		if (column != 0) throw new RuntimeException("Editing invalid column");
		Frame entry = get(row);
		entry.setChecked(!entry.isChecked());
		fireTableCellUpdated(row, column);
	}

	public void remove(int[] rows) {
		List<Frame> toRemove = new ArrayList<>();
		for (int row : rows) {
			toRemove.add(get(row));
		}

		for (Frame f : toRemove) {
			frames.remove(f);
		}

		fireTableDataChanged();
	}
}
