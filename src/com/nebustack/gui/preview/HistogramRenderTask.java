package com.nebustack.gui.preview;

import java.awt.image.BufferedImage;

import com.nebustack.file.Frame;

public final class HistogramRenderTask extends RenderTask {
	private final Frame frame;
	private final Histogram histogram;

	public HistogramRenderTask(Frame frame, Histogram histogram) {
		this.frame = frame;
		this.histogram = histogram;
	}

	@Override
	public void run() {
		if (cancelled) return;

		BufferedImage img = new BufferedImage(200, 64, BufferedImage.TYPE_INT_ARGB);

		double[][][] data = frame.getData();
		if (frame.isMono()) {
			double[][] l = data[0];
			int bl[] = new int[200];

			for (int x = 0; x < frame.getWidth(); x++) {
				for (int y = 0; y < frame.getHeight(); y++) {
					int i = (int) (l[x][y] * 200 / 65536);
					if (i < 0) i = 0;
					if (i >= 200) i = 199;

					bl[i]++;
				}
			}

			int max = 0;
			for (int i = 0; i < 200; i++) {
				if (bl[i] > max) max = bl[i];
			}

			double scale = 60d / Math.sqrt(max);
			for (int i = 0; i < 200; i++) {
				int tl = (int) (scale * Math.sqrt(bl[i]));

				for (int y = 0; y < 64; y++) {
					int rgb = 0xFF222222;
					if (y <= tl) rgb |= 0xFFFFFF;
					img.setRGB(i, 63 - y, rgb);
				}
			}
		} else {
			double[][] r = data[0];
			double[][] g = data[1];
			double[][] b = data[2];
			int[] br = new int[200];
			int[] bg = new int[200];
			int[] bb = new int[200];

			for (int x = 0; x < frame.getWidth(); x++) {
				for (int y = 0; y < frame.getHeight(); y++) {
					int ir = (int) (r[x][y] * 200 / 65536);
					if (ir < 0) ir = 0;
					if (ir >= 200) ir = 199;

					int ig = (int) (g[x][y] * 200 / 65536);
					if (ig < 0) ig = 0;
					if (ig >= 200) ig = 199;

					int ib = (int) (b[x][y] * 200 / 65536);
					if (ib < 0) ib = 0;
					if (ib >= 200) ib = 199;

					br[ir]++;
					bg[ig]++;
					bb[ib]++;
				}
			}

			int max = 0;
			for (int i = 0; i < 200; i++) {
				if (br[i] > max) max = br[i];
				if (bg[i] > max) max = bg[i];
				if (bb[i] > max) max = bb[i];
			}

			double scale = 60d / Math.sqrt(max);
			for (int i = 0; i < 200; i++) {
				int tr = (int) (scale * Math.sqrt(br[i]));
				int tg = (int) (scale * Math.sqrt(bg[i]));
				int tb = (int) (scale * Math.sqrt(bb[i]));

				for (int y = 0; y < 64; y++) {
					int rgb = 0xFF222222;
					if (y <= tr) rgb |= 0xFF0000;
					if (y <= tg) rgb |= 0xFF00;
					if (y <= tb) rgb |= 0xFF;
					img.setRGB(i, 63 - y, rgb);
				}
			}
		}

		histogram.setImage(img);
	}
}
