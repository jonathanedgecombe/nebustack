package com.nebustack.model.stack;

import java.util.List;
import java.util.Map;

import com.nebustack.file.Frame;
import com.nebustack.file.FrameType;

public final class CalibrationStacker extends Stacker {
	public CalibrationStacker() {
		super(1);
	}

	@Override
	public Frame stackInternal(List<Frame> frames, Map<Frame, double[][]> transformations, int width, int height, double offsetX, double offsetY, int channels, Frame masterDark, Frame masterBias, Frame masterFlat) {
		double[][][] data = new double[channels][width][height];
		int[][][] n = new int[channels][width][height];

		for (Frame frame : frames) {
			double[][][] d = frame.getData();

			for (int c = 0; c < channels; c++) {
				for (int x = 0; x < width; x++) {
					for (int y = 0; y < height; y++) {
						data[c][x][y] += d[c][x][y];
						n[c][x][y]++;
					}
				}
			}
		}

		for (int c = 0; c < channels; c++) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					data[c][x][y] /= n[c][x][y];
				}
			}
		}

		return new Frame(width, height, channels, data, FrameType.LIGHT);
	}
}
