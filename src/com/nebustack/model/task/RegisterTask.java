package com.nebustack.model.task;

import java.util.List;

import javax.swing.JFrame;
import com.nebustack.file.Frame;
import com.nebustack.file.FrameType;
import com.nebustack.gui.preview.Preview;
import com.nebustack.gui.table.TableModel;
import com.nebustack.model.Star;

public final class RegisterTask extends Task {
	private final double threshold;
	private final int size;
	private final TableModel model;
	private final Preview preview;

	public RegisterTask(JFrame parent, double threshold, int radius, TableModel model, Preview preview) {
		super(parent);
		this.threshold = threshold;
		this.size = radius;
		this.model = model;
		this.preview = preview;
	}

	@Override
	public void run() {
		setProgress(0, "Registering checked frames", EMPTY_MESSAGE);

		List<Frame> frames = model.getFrames();

		int count = 0;
		for (Frame frame : frames) if (frame.isChecked() && frame.getType().equals(FrameType.LIGHT)) count++;

		int i = 0;
		for (Frame frame : frames) {
			if (!frame.isChecked() || !frame.getType().equals(FrameType.LIGHT)) continue;
	
			setProgress((double) i / count, "Registering " + frame.getName(), EMPTY_MESSAGE);
			List<Star> stars = frame.register(threshold, size, this, i, count);
			frame.setStars(stars);

			if (preview.getFrame().equals(frame)) preview.update();

			i++;
		}
	}
}
