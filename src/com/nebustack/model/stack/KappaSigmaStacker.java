package com.nebustack.model.stack;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.nebustack.file.Frame;
import com.nebustack.file.FrameType;

public final class KappaSigmaStacker extends Stacker {
	private final double threshold;
	private final int iterations;
	private final int subsample;

	public KappaSigmaStacker(int drizzle, int subsample, double threshold, int iterations) {
		super(drizzle);
		this.threshold = threshold;
		this.iterations = iterations;
		this.subsample = subsample;
	}

	@Override
	public Frame stackInternal(List<Frame> frames, Map<Frame, double[][]> transformations, int width, int height, double offsetX, double offsetY, int channels, Frame masterDark, Frame masterBias, Frame masterFlat) {
		width *= drizzle;
		height *= drizzle;

		double[][][] lastMean = new double[channels][width][height];
		double[][][] lastDev = new double[channels][width][height];
		int[][][] n = new int[channels][width][height];

		for (int c = 0; c < channels; c++) {
			for (int x = 0; x < width; x++) {
				Arrays.fill(lastDev[c][x], Double.MAX_VALUE);
			}
		}

		double[][][] darkData = masterDark == null ? null : masterDark.getData();
		double[][][] biasData = masterBias == null ? null : masterBias.getData();
		double[][][] flatData = masterFlat == null ? null : masterFlat.getData();

		double sub = subsample;
		double invSub = 1 / sub;
		double offset = drizzle == 1 ? 0 : 0.5;

		for (Frame frame : frames) {
			int cw = frame.getWidth();
			int ch = frame.getHeight();

			System.out.println(frame.getName());

			double[][][] d = new double[channels][cw][ch];
			double[][][] temp = frame.getData();
			for (int c = 0; c < channels; c++) {
				for (int x = 0; x < cw; x++) {
					System.arraycopy(temp[c][x], 0, d[c][x], 0, ch);
				}
			}

			if (darkData != null)
				for (int c = 0; c < channels; c++)
					for (int x = 0; x < cw; x++)
						for (int y = 0; y < ch; y++)
							d[c][x][y] -= darkData[c][x][y];

			if (biasData != null)
				for (int c = 0; c < channels; c++)
					for (int x = 0; x < cw; x++)
						for (int y = 0; y < ch; y++)
							d[c][x][y] -= biasData[c][x][y];

			if (flatData != null)
				for (int c = 0; c < channels; c++)
					for (int x = 0; x < cw; x++)
						for (int y = 0; y < ch; y++)
							d[c][x][y] /= flatData[c][x][y];

			double[][] transform = transformations.get(frame);
			int w = frame.getWidth();
			int h = frame.getHeight();

			for (int c = 0; c < channels; c++) {
				for (int x = 0; x < width; x++) {
					for (int y = 0; y < height; y++) {
						double vt = 0;
						double dx = 0;
						for (int ix = 0; ix < subsample; ix++, dx += invSub) {
							double dy = 0;
							for (int iy = 0; iy < subsample; iy++, dy += invSub) {
								double ox = (x + dx - offset) / drizzle + offsetX;
								double oy = (y + dy - offset) / drizzle + offsetY;

								double tx = ox * transform[0][0] + oy * transform[0][1] + transform[0][2];
								double ty = ox * transform[1][0] + oy * transform[1][1] + transform[1][2];

								int itx = (int) Math.round(tx);
								int ity = (int) Math.round(ty);

								if (itx < 0 || ity < 0 || itx >= w || ity >= h) continue;

								vt += d[c][itx][ity];
								n[c][x][y]++;
							}
						}

						lastMean[c][x][y] += vt;
					}
				}
			}
		}

		for (int c = 0; c < channels; c++) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					lastMean[c][x][y] /= n[c][x][y];
				}
			}
		}

		for (int i = 0; i < iterations; i++) {
			for (int c = 0; c < channels; c++) {
				for (int x = 0; x < width; x++) {
					Arrays.fill(n[c][x], 0);
				}
			}

			double[][][] mean = new double[channels][width][height];
			double[][][] dev = new double[channels][width][height];
			int[][][] ndv = new int[channels][width][height];

			for (Frame frame : frames) {
				int cw = frame.getWidth();
				int ch = frame.getHeight();

				System.out.println(frame.getName() + " #" + i);

				double[][][] d = new double[channels][cw][ch];
				double[][][] temp = frame.getData();
				for (int c = 0; c < channels; c++) {
					for (int x = 0; x < cw; x++) {
						System.arraycopy(temp[c][x], 0, d[c][x], 0, ch);
					}
				}
	
				if (darkData != null)
					for (int c = 0; c < channels; c++)
						for (int x = 0; x < cw; x++)
							for (int y = 0; y < ch; y++)
								d[c][x][y] -= darkData[c][x][y];
	
				if (biasData != null)
					for (int c = 0; c < channels; c++)
						for (int x = 0; x < cw; x++)
							for (int y = 0; y < ch; y++)
								d[c][x][y] -= biasData[c][x][y];
	
				if (flatData != null)
					for (int c = 0; c < channels; c++)
						for (int x = 0; x < cw; x++)
							for (int y = 0; y < ch; y++)
								d[c][x][y] /= flatData[c][x][y];
	
				double[][] transform = transformations.get(frame);
				int w = frame.getWidth();
				int h = frame.getHeight();
	
				for (int c = 0; c < channels; c++) {
					for (int x = 0; x < width; x++) {
						for (int y = 0; y < height; y++) {
							int dn = 0;
							double vt = 0;
							double dx = 0;
							for (int ix = 0; ix < subsample; ix++, dx += invSub) {
								double dy = 0;
								for (int iy = 0; iy < subsample; iy++, dy += invSub) {
									double ox = (x + dx - offset) / drizzle + offsetX;
									double oy = (y + dy - offset) / drizzle + offsetY;
	
									double tx = ox * transform[0][0] + oy * transform[0][1] + transform[0][2];
									double ty = ox * transform[1][0] + oy * transform[1][1] + transform[1][2];
	
									int itx = (int) Math.round(tx);
									int ity = (int) Math.round(ty);
	
									if (itx < 0 || ity < 0 || itx >= w || ity >= h) continue;
	
									vt += d[c][itx][ity];
									dn++;
								}
							}

							double dv = (vt / dn) - lastMean[c][x][y];
							if (i != 0) {
								double delta = Math.abs(dv) / lastDev[c][x][y];
								if (delta > threshold) continue;
							}

							dev[c][x][y] += dv * dv;
							ndv[c][x][y]++;

							mean[c][x][y] += vt;
							n[c][x][y] += dn;
						}
					}
				}
			}
	
			for (int c = 0; c < channels; c++) {
				for (int x = 0; x < width; x++) {
					for (int y = 0; y < height; y++) {
						mean[c][x][y] /= n[c][x][y];
						dev[c][x][y] = Math.sqrt(dev[c][x][y] / ndv[c][x][y]);
					}
				}
			}

			lastMean = mean;
			lastDev = dev;
		}

		return new Frame(width, height, channels, lastMean, FrameType.LIGHT);
	}
}
