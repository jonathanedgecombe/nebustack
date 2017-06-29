package com.nebustack.file;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.nebustack.gui.preview.PreviewRenderTask;
import com.nebustack.model.Star;
import com.nebustack.model.Vector;
import com.nebustack.model.task.Task;

public final class Frame {
	private final double[][][] data;
	private final int width, height, channels;
	private final String name;

	private FrameType type;

	private boolean checked = true;
	private List<Star> stars = null;
	private double fwhm = 0;
	private double backgroundLevel = 0;

	private final String camera;
	private final String telescope;
	private final double exposure;
	private final int gain;
	private final LocalDateTime time;
	private final double temperature;

	public Frame(int width, int height, int channels, double[][][] data, FrameType type, String name, String camera, String telescope, double exposure, int gain, double temperature, LocalDateTime time) {
		this.width = width;
		this.height = height;
		this.channels = channels;
		this.data = data;
		this.type = type;
		this.name = name;
		this.camera = camera;
		this.telescope = telescope;
		this.exposure = exposure;
		this.gain = gain;
		this.temperature = temperature;
		this.time = time;

		this.backgroundLevel = extractBackgroundLevel(extractLuminance());
	}

	public Frame(int width, int height, int channels, double[][][] data, FrameType type) {
		this(width, height, channels, data, type, null, null, null, Double.NaN, 0, Double.NaN, null);
	}

	public synchronized boolean isChecked() {
		return checked;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public FrameType getType() {
		return type;
	}

	public void setType(FrameType type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public String getCamera() {
		return camera;
	}

	public String getTelescope() {
		return telescope;
	}

	public double getTemperature() {
		return temperature;
	}

	public String getCameraTelescope() {
		String c = camera == null ? "" : camera;
		if (telescope != null) if (!telescope.isEmpty()) c += " (" + telescope + ")";
		return c;
	}

	public double getExposure() {
		return exposure;
	}

	public int getGain() {
		return gain;
	}

	public String getDateTimeString() {
		return time == null ? "n/a" : time.toString();
	}

	public int getChannels() {
		return channels;
	}

	public String getColorFormat() {
		return channels == 1 ? "Mono" : "RGB";
	}

	public synchronized String getNumberOfStars() {
		if (stars == null) return "";
		return Integer.toString(stars.size());
	}

	public synchronized List<Star> getStars() {
		return stars;
	}

	public synchronized void setStars(List<Star> stars) {
		this.stars = stars;

		double fwhm = 0;
		for (Star star : stars) {
			fwhm += star.getRadius();
		}

		this.fwhm = 2 * fwhm / stars.size();
	}

	public double getFWHM() {
		return fwhm;
	}

	public String getResolution() {
		return width + " x " + height;
	}

	public synchronized void setChecked(boolean checked) {
		this.checked = checked;
	}

	public double[] getMinMax() {
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;

		double[][] d = data[0];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (d[x][y] < min) min = d[x][y];
				if (d[x][y] > max) max = d[x][y];
			}
		}

		return new double[] {min, max};
	}

	public BufferedImage render(double scale, double offset, PreviewRenderTask task) {
		double min = 0;
		double max = 65535;

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		int[] out = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();

		double r = max - min;
		double sub = offset * r;

		double s = min + sub;
		double sc = 255f * scale / r;

		if (channels == 1) {
			double[][] d = data[0];
	
			for (int x = 0; x < width; x++) {
				if (task != null) if (task.isCancelled()) return null;
	
				for (int y = 0; y < height; y++) {
					int v = (int) (sc * (d[x][y] - s));
	
					if (v > 255) v = 255;
					if (v < 0) v = 0;
	
					out[y * width + x] = 0xFF000000 | v << 16 | v << 8 | v;
				}
			}
		} else {
			double[][] dr = data[0];
			double[][] dg = data[1];
			double[][] db = data[2];
			
			for (int x = 0; x < width; x++) {
				if (task != null) if (task.isCancelled()) return null;
	
				for (int y = 0; y < height; y++) {
					int vr = (int) (sc * (dr[x][y] - s));
					if (vr > 255) vr = 255;
					if (vr < 0) vr = 0;

					int vg = (int) (sc * (dg[x][y] - s));
					if (vg > 255) vg = 255;
					if (vg < 0) vg = 0;

					int vb = (int) (sc * (db[x][y] - s));
					if (vb > 255) vb = 255;
					if (vb < 0) vb = 0;
	
					out[y * width + x] = 0xFF000000 | vr << 16 | vg << 8 | vb;
				}
			}
		}

		return img;
	}

	public double[][] extractLuminance() {
		double[][] luminance = new double[width][height];

		for (int c = 0; c < channels; c++) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					luminance[x][y] += data[c][x][y];
				}
			}
		}

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				luminance[x][y] /= channels;
			}
		}

		return luminance;
	}

	public double extractBackgroundLevel(double[][] luminance) {
		double[] minMax = getMinMax();
		double min = minMax[0];
		double max = minMax[1];

		double range = max - min;
		double lq = min + (range / 50);

		for (int i = 0; i < 12; i++) {
			int above = 0;

			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					if (luminance[x][y] > lq) above++;
				}
			}

			range /= 2;
			if (above > ((width * height) / 4)) {
				lq += range / 2;
			} else {
				lq -= range / 2;
			}
		}

		return lq;
	}

	public void normalize() {
		for (int c = 0; c < channels; c++) {
			double mean = 0;
			double div = width * height;

			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					mean += data[c][x][y] / div;
				}
			}

			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					data[c][x][y] /= mean;
				}
			}
		}
	}

	public List<Star> register(double threshold, int radius, Task registerTask, int taskNum, int count) {
		if (registerTask != null) registerTask.setProgress(((double) taskNum / count), "Extracting luminance");

		double[][] l = extractLuminance();

		List<Star> stars = new ArrayList<>();

		double[][] du = new double[width][height];
		for (int x = 1; x < width - 1; x++) {
			for (int y = 1; y < height - 1; y++) {
				du[x][y] = (l[x + 1][y + 1] - l[x - 1][y - 1]) + (l[x + 1][y] - l[x - 1][y]) + (l[x][y + 1] - l[x][y - 1]);
			}
		}

		if (registerTask != null) registerTask.setProgress(((double) taskNum / count) + (0.02 / count), "Extracting differentials");

		double[][] d = new double[width][height];
		int dSmooth = 1;
		int div = ((dSmooth * 2) + 1) * ((dSmooth * 2) + 1);

		for (int x = dSmooth; x < width - dSmooth; x++) {
			for (int y = dSmooth; y < height - dSmooth; y++) {
				double ds = 0;

				for (int dx = -dSmooth; dx <= dSmooth; dx++) {
					for (int dy = -dSmooth; dy <= dSmooth; dy++) {
						ds += du[x + dx][y + dy];
					}
				}

				d[x][y] = ds / div;
			}
		}

		if (registerTask != null) registerTask.setProgress(((double) taskNum / count) + (0.05 / count), "Extracting background levels");

		double dThreshold = threshold / (2 * radius);
		double lThreshold = threshold;

		int blocksize = radius * 4;
		double[][] background = new double[(width + blocksize - 1) / blocksize][(height + blocksize - 1) / blocksize];

		for (int bx = 0; bx < background.length; bx++) {
			for (int by = 0; by < background[0].length; by++) {
				int n = 0;

				for (int tx = 0; tx < blocksize; tx++) {
					for (int ty = 0; ty < blocksize; ty++) {
						int x = (bx * blocksize) + tx;
						int y = (by * blocksize) + ty;

						if (x >= width || y >= height) continue;

						background[bx][by] += l[x][y];
						n++;
					}
				}

				background[bx][by] /= n;
			}
		}

		if (registerTask != null) registerTask.setProgress(((double) taskNum / count) + (0.1 / count), "Applying filters");

		double[][] ls = new double[width][height];
		int lSmooth = 3;
		int lSize = (lSmooth * 2) + 1;

		double[][] m = new double[lSize][lSize];
		double t = 0;
		for (int x = 0; x < lSize; x++) {
			for (int y = 0; y < lSize; y++) {
				double dx = x - lSmooth;
				double dy = y - lSmooth;
				m[x][y] = Math.tanh((1f/36f) / (0.01f + ((dx/lSmooth)*(dx/lSmooth) + (dy/lSmooth)*(dy/lSmooth))));
				t += m[x][y];
			}
		}

		if (registerTask != null) registerTask.setProgress(((double) taskNum / count) + (0.15 / count), "Applying filters");

		for (int x = lSmooth; x < width - lSmooth; x++) {
			for (int y = lSmooth; y < height - lSmooth; y++) {
				double lsa = 0;

				for (int dx = -lSmooth; dx <= lSmooth; dx++) {
					for (int dy = -lSmooth; dy <= lSmooth; dy++) {
						lsa += l[x + dx][y + dy] * m[dx + lSmooth][dy + lSmooth];
					}
				}

				ls[x][y] = lsa / t;
			}
		}

		if (registerTask != null) registerTask.setProgress(((double) taskNum / count) + (0.2 / count), "0 stars");

		int walkRadius = radius / 2;

		for (int x = 1; x < width - 1; x++) {
			if (registerTask != null) registerTask.setProgress(((double) taskNum / count) + ((0.2 + (0.8 * ((double) x / width))) / count), Integer.toString(stars.size()) + " star" + ((stars.size() == 1) ? "" : "s"));

			loop: for (int y = 1; y < height - 1; y++) {
				double bg = background[x / blocksize][y / blocksize];

				if (!(Integer.signum((int) d[x][y]) > Integer.signum((int) d[x + 1][y + 1])
						|| Integer.signum((int) d[x][y]) > Integer.signum((int) d[x + 1][y])
						|| Integer.signum((int) d[x][y]) > Integer.signum((int) d[x][y + 1]))) {
					continue;
				}

				if (d[x][y] - d[x + 1][y + 1] < dThreshold && d[x][y] - d[x + 1][y] < dThreshold && d[x][y] - d[x][y + 1] < dThreshold) {
					continue;
				}

				if (l[x][y] - bg < lThreshold) {
					continue;
				}

				int tx = x;
				int ty = y;

				for (int i = 0; i < radius; i++) {
					int px = tx;
					int py = ty;

					outer: for (int dx = -walkRadius; dx <= walkRadius; dx++) {
						for (int dy = -walkRadius; dy <= walkRadius; dy++) {
							int qx = tx + dx;
							int qy = ty + dy;

							if (qx < 0 || qy < 0 || qx >= width || qy >= height) continue;

							if (ls[qx][qy] > ls[tx][ty]) {
								tx += dx;
								ty += dy;
								break outer;
							}
						}
					}

					if (px == tx && py == ty) break;
					if (i == radius - 1) continue loop;
				}

				if (tx < radius || ty < radius || tx >= width - radius || ty >= height - radius) continue;

				Vector ns1 = new Vector(tx, ty);
				for (Star star : stars) {
					if (star.getDistance(ns1) < radius) {
						continue loop;
					}
				}

				int pixels = 0;
				float ax = 0, ay = 0;
				double w = 0;
				for (int dx = -radius; dx <= radius; dx++) {
					outer: for (int dy = -radius; dy <= radius; dy++) {
						int qx = tx + dx;
						int qy = ty + dy;
						double lum = l[qx][qy];

						int abs = 1 + (int) Math.sqrt(dx * dx + dy * dy);
						for (int i = 1; i < abs; i++) {
							double s = ((float) i) / abs;
							if (ls[tx + (int) (dx * s)][ty + (int) (dy * s)] - bg < lThreshold) {
								continue outer;
							}
						}

						if (lum - bg > lThreshold) {
							ax += lum * qx;
							ay += lum * qy;
							w += lum;
							pixels++;
						}
					}
				}

				ax /= w;
				ay /= w;

				float r = (float) Math.sqrt(pixels / Math.PI);

				Star ns2 = new Star(ax, ay, r);
				for (Star star : stars) {
					if (star.getDistance(ns2) < radius) {
						continue loop;
					}
				}

				if (pixels > 1 && w > lThreshold * 1.2 && r < radius) {
					stars.add(ns2);
				}
			}
		}

		return stars;
	}

	public boolean isMono() {
		return channels == 1;
	}

	public double[][][] getData() {
		return data;
	}

	public void sub(Frame f) {
		if (f.width != width || f.height != height || f.channels != channels) throw new RuntimeException("Mismatching frames");

		for (int i = 0; i < channels; i++) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					data[i][x][y] -= f.data[i][x][y];
				}
			}
		}
	}

	public void div(Frame f) {
		if (f.width != width || f.height != height || f.channels != channels) throw new RuntimeException("Mismatching frames");

		for (int i = 0; i < channels; i++) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					data[i][x][y] = 65536d * data[i][x][y] / f.data[i][x][y];
				}
			}
		}
	}
}
