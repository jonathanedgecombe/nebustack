package com.nebustack.model.task;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import com.nebustack.file.Frame;
import com.nebustack.gui.RegisterDialog;
import com.nebustack.gui.table.TableModel;
import com.nebustack.model.Star;

public final class TestRegisterTask extends Task {
	private final double threshold;
	private final int size;
	private final JTable table;
	private final TableModel model;
	private final RegisterDialog registerDialog;
	private final JLabel starsLabel;

	public TestRegisterTask(JFrame parent, double threshold, int radius, JTable table, TableModel model, RegisterDialog registerDialog, JLabel starsLabel) {
		super(parent);
		this.threshold = threshold;
		this.size = radius;
		this.table = table;
		this.model = model;
		this.registerDialog = registerDialog;
		this.starsLabel = starsLabel;
	}

	@Override
	public void run() {
		setProgress(0, "Registering", EMPTY_MESSAGE);
	
		List<Frame> frames = model.getFrames();

		Frame frame = null;
		if (table.getSelectedRowCount() > 0) {
			frame = model.get(table.getSelectedRow());
		} else for (Frame f : frames) {
			if (!f.isChecked()) continue;
			frame = f;
			break;
		}

		if (frame != null) {
			setProgress(0, "Registering " + frame.getName(), EMPTY_MESSAGE);
			List<Star> stars = frame.register(threshold, size, this, 0, 1);

			SwingUtilities.invokeLater(() -> {
				starsLabel.setText(Integer.toString(stars.size()) + " star" + ((stars.size() == 1) ? "" : "s") + " detected");
				registerDialog.repaint();
			});
		} else {
			starsLabel.setText(" ");
			registerDialog.repaint();
		}

		registerDialog.setEnabled(true);
		registerDialog.toFront();
		registerDialog.repaint();
	}
}
