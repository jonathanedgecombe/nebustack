package com.nebustack.model.stack;

import java.util.List;
import java.util.Map;

import com.nebustack.file.Frame;
import com.nebustack.file.FrameType;

public final class MeanStacker extends Stacker {
	public MeanStacker(int drizzle) {
		super(drizzle);
	}

	@Override
	public Frame stackInternal(List<Frame> frames, Map<Frame, double[][]> transformations, int width, int height, double offsetX, double offsetY, int channels, Frame masterDark, Frame masterBias, Frame masterFlat) {
		width *= drizzle;
		height *= drizzle;

		double[][][] data = new double[channels][width][height];
		int[][][] n = new int[channels][width][height];

		double[][][] darkData = masterDark == null ? null : masterDark.getData();
		double[][][] biasData = masterBias == null ? null : masterBias.getData();
		double[][][] flatData = masterFlat == null ? null : masterFlat.getData();

		for (Frame frame : frames) {
			double[][][] d = frame.getData().clone();

			if (darkData != null)
				for (int c = 0; c < channels; c++)
					for (int x = 0; x < width; x++)
						for (int y = 0; y < height; y++)
							d[c][x][y] -= darkData[c][x][y];

			if (biasData != null)
				for (int c = 0; c < channels; c++)
					for (int x = 0; x < width; x++)
						for (int y = 0; y < height; y++)
							d[c][x][y] -= biasData[c][x][y];

			if (flatData != null)
				for (int c = 0; c < channels; c++)
					for (int x = 0; x < width; x++)
						for (int y = 0; y < height; y++)
							d[c][x][y] /= flatData[c][x][y];

			double[][] transform = transformations.get(frame);
			int w = frame.getWidth();
			int h = frame.getHeight();

			for (int c = 0; c < channels; c++) {
				for (int x = 0; x < width; x++) {
					for (int y = 0; y < height; y++) {
						double ox = x/ drizzle + offsetX;
						double oy = y / drizzle + offsetY;

						double tx = ox * transform[0][0] + oy * transform[0][1] + transform[0][2];
						double ty = ox * transform[1][0] + oy * transform[1][1] + transform[1][2];

						int itx = (int) tx;
						int ity = (int) ty;

						if (itx < 0 || ity < 0 || itx >= w || ity >= h) continue;

						data[c][x][y] += d[c][itx][ity];
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
