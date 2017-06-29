package com.nebustack.model.task;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import com.nebustack.file.Frame;
import com.nebustack.file.fits.FitsWriter;
import com.nebustack.gui.table.TableModel;
import com.nebustack.model.stack.CalibrationStacker;
import com.nebustack.model.stack.Stacker;

public final class StackTask extends Task {
	private final static CalibrationStacker CALIBRATION_STACKER = new CalibrationStacker();

	private final TableModel model;
	private final Stacker stacker;

	public StackTask(JFrame parent, TableModel model, Stacker stacker) {
		super(parent);
		this.model = model;
		this.stacker = stacker;
	}

	@Override
	public void run() {
		setProgress(0, "Stacking", EMPTY_MESSAGE);

		List<Frame> frames = model.getFrames();
		Frame reference = model.getReference();

		if (frames.size() <= 0) {
			setProgress(1, "Stacking", "No frames selected");
			return;
		}

		List<Frame> darks = new ArrayList<>();
		List<Frame> bias = new ArrayList<>();
		List<Frame> flats = new ArrayList<>();
		List<Frame> darkFlats = new ArrayList<>();

		List<Frame> lights = new ArrayList<>();

		for (Frame frame : frames) {
			if (!frame.isChecked())
				continue;

			switch (frame.getType()) {
			case DARK:
				darks.add(frame);
				break;
			case BIAS:
				bias.add(frame);
				break;
			case FLAT:
				flats.add(frame);
				break;
			case DARK_FLAT:
				darkFlats.add(frame);
				break;
			case LIGHT:
				lights.add(frame);
				break;
			}
		}

		if (lights.size() == 0) throw new RuntimeException("No light frames selected");

		Frame masterBias = null;
		if (bias.size() > 0) {
			setProgress(0, "Creating master bias", EMPTY_MESSAGE);
			masterBias = CALIBRATION_STACKER.stack(bias, this);
		}

		Frame masterDark = null;
		if (darks.size() > 0) {
			setProgress(0, "Creating master dark", EMPTY_MESSAGE);
			masterDark = CALIBRATION_STACKER.stack(darks, this);

			if (masterBias != null) {
				masterDark.sub(masterBias);
			}
		}

		Frame masterDarkFlat = null;
		if (darkFlats.size() > 0) {
			setProgress(0, "Creating master dark flat", EMPTY_MESSAGE);
			masterDarkFlat = CALIBRATION_STACKER.stack(darkFlats, this);
		}

		Frame masterFlat = null;
		if (flats.size() > 0) {
			setProgress(0, "Creating master flat", EMPTY_MESSAGE);
			masterFlat = CALIBRATION_STACKER.stack(flats, this);

			if (masterDarkFlat != null) {
				masterFlat.sub(masterDarkFlat);
			}

			masterFlat.normalize();
		}

		setProgress(0, "Stacking light frames", EMPTY_MESSAGE);
		Frame stacked = stacker.stack(lights, reference, this, masterDark, masterBias, masterFlat);

		try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(Paths.get("out.fits")))) {
			new FitsWriter(out).write(stacked);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		System.out.println("render");
		try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(Paths.get("stacked.png")))) {
			BufferedImage img = stacked.render(2, 0, null);
			ImageIO.write(img, "PNG", out);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
