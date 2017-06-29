package com.nebustack.file.fits;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.nebustack.file.Frame;

public final class FitsWriter {
	private final DataOutputStream out;

	public FitsWriter(OutputStream out) {
		this.out = new DataOutputStream(out);
	}

	public void write(Frame frame) throws IOException {
		boolean rgb = frame.getChannels() == 3;

		writeHeader("SIMPLE", "T");
		writeHeader("BITPIX", "16");
		writeHeader("NAXIS", rgb ? "3" : "2");
		writeHeader("NAXIS1", Integer.toString(frame.getWidth()));
		writeHeader("NAXIS2", Integer.toString(frame.getHeight()));
		if (rgb) writeHeader("NAXIS3", "3");
		writeHeader("BZERO", "32768");
		writeHeader("SWCREATE", "'Nebustack'");
		writeHeader("END", null);
		for (int i = 0; i < (rgb ? 27 : 28); i++) {
			writeHeader("", null);
		}

		// TODO FIX FOR RGB
		double[][][] data = frame.getData();

		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (int c = 0; c < (rgb ? 3 : 1); c++) {
			for (int y = 0; y < frame.getHeight(); y++) {
				for (int x = 0; x < frame.getWidth(); x++) {
					double v = data[c][x][y];
					if (v > max) max = v;
					if (v < min) min = v;
				}
			}
		}

		double r = max - min;

		for (int c = 0; c < (rgb ? 3 : 1); c++) {
			for (int y = 0; y < frame.getHeight(); y++) {
				for (int x = 0; x < frame.getWidth(); x++) {
					int v = Math.max(0, Math.min((int) (65535 * ((data[c][x][y] - min) / r)), 65535));
					out.writeShort(v - 32768);
				}
			}
		}

		out.close();
	}

	private void writeHeader(String key, String value) throws IOException {
		while (key.length() < 8) key = key + " ";
		String all = key;
		if (value != null) all += "= " + value + " / ";
		while (all.length() < 80) all += " ";

		byte[] data = all.getBytes(StandardCharsets.US_ASCII);
		if (data.length != 80) throw new IOException("Invalid header size");

		out.write(data);
	}
}
