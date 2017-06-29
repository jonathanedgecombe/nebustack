package com.nebustack.file.tiff;

import java.awt.color.ColorSpace;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import com.nebustack.file.Frame;
import com.nebustack.file.FrameType;
import com.nebustack.file.Reader;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codecimpl.TIFFImage;

public final class TiffReader extends Reader {
	public final static List<Integer> COLOR_SPACES = Arrays.asList(new Integer[] {
			ColorSpace.TYPE_GRAY, ColorSpace.CS_GRAY, 
			ColorSpace.TYPE_RGB, ColorSpace.CS_LINEAR_RGB, ColorSpace.CS_sRGB, ColorSpace.TYPE_3CLR
			});

	private final Path path;

	public TiffReader(Path path) {
		this.path = path;
	}

	@Override
	public Frame read(FrameType type) throws IOException {
		try (SeekableStream s = new FileSeekableStream(path.toFile())) {
			ImageDecoder decoder = ImageCodec.createImageDecoder("TIFF", s, null);
			TIFFImage image = (TIFFImage) decoder.decodeAsRenderedImage();
			Raster raster = image.getData();

			ColorSpace colorSpace = image.getColorModel().getColorSpace();
			if (!COLOR_SPACES.contains(colorSpace.getType())) throw new IOException("Unsupported colour space [" + colorSpace.getType() + "]");

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
}
