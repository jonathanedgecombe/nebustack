package com.nebustack.file.simple;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import com.nebustack.file.Frame;
import com.nebustack.file.FrameType;
import com.nebustack.file.Reader;
import com.nebustack.file.tiff.TiffReader;

public final class SimpleReader extends Reader {
	private final Path path;
	private final InputStream in;

	public SimpleReader(Path path) throws IOException {
		this.path = path;
		this.in = new BufferedInputStream(Files.newInputStream(path), 65536);
	}

	@Override
	public Frame read(FrameType type) throws IOException {
		BufferedImage image = ImageIO.read(in);
		Raster raster = image.getData();

		ColorSpace colorSpace = image.getColorModel().getColorSpace();
		if (!TiffReader.COLOR_SPACES.contains(colorSpace.getType())) throw new IOException("Unsupported colour space [" + colorSpace.getType() + "]");

		int width = raster.getWidth();
		int height = raster.getHeight();

		int bits = image.getColorModel().getComponentSize(0);
		double scale = Math.pow(2, bits - 16);

		boolean isMono = colorSpace.getNumComponents() == 1;

		int channels = raster.getNumDataElements();
		double[] rawData = raster.getPixels(0, 0, width, height, new double[channels * width * height]);
		double[][][] data = new double[isMono ? 1 : 3][width][height];

		if (isMono) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					data[0][x][y] = rawData[y * width + x] / scale;
				}
			}
		} else {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					data[0][x][y] = rawData[(y * width + x) * 3    ] / scale;
					data[1][x][y] = rawData[(y * width + x) * 3 + 1] / scale;
					data[2][x][y] = rawData[(y * width + x) * 3 + 2] / scale;
				}
			}
		}

		return new Frame(width, height, channels, data, type, path.getFileName().toString(), null, null, Double.NaN, 0, Double.NaN, null);
	}
}
