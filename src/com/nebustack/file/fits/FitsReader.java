package com.nebustack.file.fits;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.nebustack.file.Frame;
import com.nebustack.file.FrameType;
import com.nebustack.file.Reader;

public final class FitsReader extends Reader {
	private final static double POW_8 = Math.pow(2, 8);
	private final static double POW_16 = Math.pow(2, 16);
	private final static double POW_48 = Math.pow(2, 48);

	private final Path path;
	private final DataInputStream in;

	private final boolean rgb;
	private final boolean offsetX, offsetY;

	public FitsReader(Path path, boolean rgb, boolean offsetX, boolean offsetY) throws IOException {
		this.path = path;
		this.in = new DataInputStream(new BufferedInputStream(Files.newInputStream(path), 65536));

		this.rgb = rgb;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	public Frame read(FrameType type) throws IOException {
		Map<String, Object> headers = readHeaders();

		boolean fp = false;
		int bits = ((Number) headers.get("BITPIX")).intValue();
		fp = bits < 0;
		bits = Math.abs(bits);
		int naxis = ((Number) headers.get("NAXIS")).intValue();

		long bzero = ((Number) headers.getOrDefault("BZERO", 0l)).longValue();
		double bscale = ((Number) headers.getOrDefault("BSCALE", 1d)).doubleValue();

		if (naxis != 2 && naxis != 3) throw new IOException("NAXIS != 2/3");
		if (!(bits == 8 || bits == 16 || bits == 32 || bits == 64)) throw new IOException("Invalid bit depth [" + bits + "]");

		int width = ((Number) headers.get("NAXIS1")).intValue();
		int height = ((Number) headers.get("NAXIS2")).intValue();

		int channels = 1;
		if (naxis == 3) channels =((Number) headers.get("NAXIS3")).intValue();

		if (!(channels == 1 || channels == 3)) throw new IOException("Channels != 1/3");

		String dtString = getDate(headers);
		LocalDateTime lcd = DateTimeFormatter.ISO_DATE_TIME.parse(dtString, LocalDateTime::from);
		System.out.println(lcd);

		double[][][] data = new double[channels][width][height];

		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		if (!fp) {
			for (int c = 0; c < channels; c++) {
				long[][] rawData = readIntegerData(in, width, height, bits);
	
				for (int x = 0; x < width; x++) {
					for (int y = 0; y < height; y++) {
						data[c][x][y] = (double) (bzero + (bscale * rawData[x][y]));
	
						if (bits == 8) {
							data[c][x][y] *= POW_8;
						} else if (bits == 32) {
							data[c][x][y] /= POW_16;
						} else if (bits == 64) {
							data[c][x][y] /= POW_48;
						}
	
						if (data[c][x][y] < min) min = data[c][x][y];
						if (data[c][x][y] > max) max = data[c][x][y];
					}
				}
			}
		} else {
			for (int c = 0; c < channels; c++) {
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						switch (bits) {
						case 32:
							data[c][x][y] = in.readFloat();
							break;
						case 64:
							data[c][x][y] = in.readDouble();
							break;
						}
						if (data[c][x][y] < min) min = data[c][x][y];
						if (data[c][x][y] > max) max = data[c][x][y];
					}
				}
			}
		}

		in.close();

		int wm1 = width - 1;
		int hm1 = height - 1;

		if (rgb) {
			double[][][] colourData = new double[3][width][height];
			double[][] d0 = data[0];

			int rox = offsetX ? 1 : 0;
			int roy = offsetY ? 1 : 0;
			int box = 1 - rox;
			int boy = 1 - roy;

			double[][] r = colourData[0];
			double[][] g = colourData[1];
			double[][] b = colourData[2];

			double[][] g1 = new double[width][height];
			double[][] g2 = new double[width][height];

			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					if ((x + rox) % 2 == 0 && (y + roy) % 2 == 0) {
						r[x][y] = d0[x][y];
					}

					if ((x + box) % 2 == 0 && (y + roy) % 2 == 0) {
						g1[x][y] = d0[x][y];
					}

					if ((x + box) % 2 == 0 && (y + boy) % 2 == 0) {
						b[x][y] = d0[x][y];
					}

					if ((x + rox) % 2 == 0 && (y + boy) % 2 == 0) {
						g2[x][y] = d0[x][y];
					}
				}
			}

			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					if ((x + rox + y + roy) % 2 == 1) {
						double t = 0;
						int n = 0;

						if ((y + roy) % 2 == 0) {
							if (x > 0) {
								t += r[x - 1][y];
								n++;
							}

							if (x < wm1) {
								t += r[x + 1][y];
								n++;
							}
						} else {
							if (y > 0) {
								t += r[x][y - 1];
								n++;
							}

							if (y < hm1) {
								t += r[x][y + 1];
								n++;
							}
						}

						r[x][y] = t / n;
					}

					if ((x + rox + y + boy) % 2 == 1) {
						double t = 0;
						int n = 0;

						if ((y + roy) % 2 == 0) {
							if (x > 0) {
								t += g1[x - 1][y];
								n++;
							}

							if (x < wm1) {
								t += g1[x + 1][y];
								n++;
							}
						} else {
							if (y > 0) {
								t += g1[x][y - 1];
								n++;
							}

							if (y < hm1) {
								t += g1[x][y + 1];
								n++;
							}
						}

						g1[x][y] = t / n;
					}

					if ((x + box + y + boy) % 2 == 1) {
						double t = 0;
						int n = 0;

						if ((y + boy) % 2 == 0) {
							if (x > 0) {
								t += b[x - 1][y];
								n++;
							}

							if (x < wm1) {
								t += b[x + 1][y];
								n++;
							}
						} else {
							if (y > 0) {
								t += b[x][y - 1];
								n++;
							}

							if (y < hm1) {
								t += b[x][y + 1];
								n++;
							}
						}

						b[x][y] = t / n;
					}

					if ((x + box + y + roy) % 2 == 1) {
						double t = 0;
						int n = 0;

						if ((y + boy) % 2 == 0) {
							if (x > 0) {
								t += g2[x - 1][y];
								n++;
							}

							if (x < wm1) {
								t += g2[x + 1][y];
								n++;
							}
						} else {
							if (y > 0) {
								t += g2[x][y - 1];
								n++;
							}

							if (y < hm1) {
								t += g2[x][y + 1];
								n++;
							}
						}

						g2[x][y] = t / n;
					}
				}
			}

			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					if ((x + box) % 2 == 0 && (y + boy) % 2 == 0) {
						double t = 0;
						int n = 0;

						if (x > 0) {
							t += r[x - 1][y];
							n++;
						}

						if (y > 0) {
							t += r[x][y - 1];
							n++;
						}

						if (x < wm1) {
							t += r[x + 1][y];
							n++;
						}

						if (y < hm1) {
							t += r[x][y + 1];
							n++;
						}

						r[x][y] = t / n;
					}

					if ((x + rox) % 2 == 0 && (y + boy) % 2 == 0) {
						double t = 0;
						int n = 0;

						if (x > 0) {
							t += g1[x - 1][y];
							n++;
						}

						if (y > 0) {
							t += g1[x][y - 1];
							n++;
						}

						if (x < wm1) {
							t += g1[x + 1][y];
							n++;
						}

						if (y < hm1) {
							t += g1[x][y + 1];
							n++;
						}

						g1[x][y] = t / n;
					}

					if ((x + rox) % 2 == 0 && (y + roy) % 2 == 0) {
						double t = 0;
						int n = 0;

						if (x > 0) {
							t += b[x - 1][y];
							n++;
						}

						if (y > 0) {
							t += b[x][y - 1];
							n++;
						}

						if (x < wm1) {
							t += b[x + 1][y];
							n++;
						}

						if (y < hm1) {
							t += b[x][y + 1];
							n++;
						}

						b[x][y] = t / n;
					}

					if ((x + box) % 2 == 0 && (y + roy) % 2 == 0) {
						double t = 0;
						int n = 0;

						if (x > 0) {
							t += g2[x - 1][y];
							n++;
						}

						if (y > 0) {
							t += g2[x][y - 1];
							n++;
						}

						if (x < wm1) {
							t += g2[x + 1][y];
							n++;
						}

						if (y < hm1) {
							t += g2[x][y + 1];
							n++;
						}

						g2[x][y] = t / n;
					}
				}
			}

			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					g[x][y] = (g1[x][y] + g2[x][y]) / 2;
				}
			}

			return new Frame(width, height, 3, colourData, type, path.getFileName().toString(), getCamera(headers), getTelescope(headers), getExposure(headers), getGain(headers), getTemperature(headers), lcd);
		} else {
			return new Frame(width, height, channels, data, type, path.getFileName().toString(), getCamera(headers), getTelescope(headers), getExposure(headers), getGain(headers), getTemperature(headers), lcd);
		}
	}

	public static String getDate(Map<String, Object> headers) {
		return getOrDefaultM(headers, "", FitsKeys.DATE);
	}

	public static String getCamera(Map<String, Object> headers) {
		return getOrDefaultM(headers, "", FitsKeys.CAMERA).trim();
	}

	public static String getTelescope(Map<String, Object> headers) {
		return getOrDefaultM(headers, "", FitsKeys.TELESCOPE).trim();
	}

	public static double getExposure(Map<String, Object> headers) {
		return getOrDefaultDouble(headers, Double.NaN, FitsKeys.EXPOSURE);
	}

	public static int getGain(Map<String, Object> headers) {
		return getOrDefaultInt(headers, 0, FitsKeys.GAIN);
	}

	public static double getTemperature(Map<String, Object> headers) {
		return getOrDefaultDouble(headers, Double.NaN, FitsKeys.TEMPERATURE);
	}

	@SuppressWarnings("unchecked")
	public static <T> T get(Map<String, Object> headers, String key, Class<T> type) {
		return (T) headers.get(key);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getOrDefault(Map<String, Object> headers, String key, T defaultValue) {
		return (T) headers.getOrDefault(key, defaultValue);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getOrDefaultM(Map<String, Object> headers, T defaultValue, String... keys) {
		for (String key : keys) {
			T value = (T) headers.get(key);
			if (value != null) return value;
		}
		return defaultValue;
	}

	public static int getOrDefaultInt(Map<String, Object> headers, int defaultValue, String... keys) {
		for (String key : keys) {
			Number value = (Number) headers.get(key);
			if (value != null) return value.intValue();
		}
		return defaultValue;
	}

	public static double getOrDefaultDouble(Map<String, Object> headers, double defaultValue, String... keys) {
		for (String key : keys) {
			Number value = (Number) headers.get(key);
			if (value != null) return value.doubleValue();
		}
		return defaultValue;
	}

	private long[][] readIntegerData(DataInputStream in, int width, int height, int bits) throws IOException {
		long[][] data = new long[width][height];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				switch (bits) {
				case 8:
					data[x][y] = in.readByte() & 0xFF;
					break;
				case 16:
					data[x][y] = in.readShort();
					break;
				case 32:
					data[x][y] = in.readInt();
					break;
				case 64:
					data[x][y] = in.readLong();
					break;
				}
			}
		}

		return data;
	}

	private Map<String, Object> readHeaders() throws IOException {
		Map<String, Object> headers = new HashMap<>();

		while (true) {
			Map<String, Object> newHeaders = readHeaderSection();
			headers.putAll(newHeaders);

			if (newHeaders.containsKey("END")) break;
		}

		return headers;
	}

	private Map<String, Object> readHeaderSection() throws IOException {
		Map<String, Object> headers = new HashMap<>();

		for (int i = 0; i < 36; i++) {
			String line = readHeaderLine();

			String key = line.substring(0, 8).trim().toUpperCase();
			String valueIndicator = line.substring(8, 10);

			if (key.isEmpty()) continue;

			if (valueIndicator.equals("= ")) {
				Object value = parseHeaderValue(line.substring(10, 80));
				headers.put(key, value);
			} else {
				String value = line.substring(10, 80).trim();
				headers.put(key, value);
			}
		}

		return headers;
	}

	private String readHeaderLine() throws IOException {
		byte[] data = new byte[80];
		in.readFully(data);
		return new String(data, StandardCharsets.US_ASCII);
	}

	private Object parseHeaderValue(String string) throws IOException {
		StringStream ss = new StringStream(string);

		while (ss.hasNext()) {
			char c = ss.next();

			if (c == ' ') {
				continue;
			} else if (c == '\'') {
				return readString(ss);
			} else if (Character.isDigit(c) || c == '-') {
				return readNumber(ss);
			} else if (c == '(') {
				return readComplex(ss);
			} else if (c == 'T' || c == 't') {
				return true;
			} else if (c == 'F' || c == 'f') {
				return false;
			} else {
				//throw new IOException("Unrecognised type for character '" + c + "'");
				return null;
			}
		}

		throw new IOException();
	}

	private Complex readComplex(StringStream ss) throws IOException {
		double real = 0, imaginary = 0;

		while (ss.hasNext()) {
			char c = ss.next();

			if (Character.isDigit(c) || c == '-') {
				real = readNumber(ss).doubleValue();
				break;
			}
		}

		while (ss.hasNext()) {
			char c = ss.next();

			if (Character.isDigit(c) || c == '-') {
				imaginary = readNumber(ss).doubleValue();
				break;
			}
		}

		return new Complex(real, imaginary);
	}

	private Number readNumber(StringStream ss) throws IOException {
		StringBuffer sb = new StringBuffer();
		sb.append(ss.previous());

		while (ss.hasNext()) {
			char c = ss.next();

			if (Character.isDigit(c) || c == '.') {
				sb.append(c);
			} else {
				String string = sb.toString();

				if (string.contains(".")) {
					return Double.parseDouble(string);
				} else {
					if (string.startsWith("-")) {
						return Long.parseLong(string);
					} else {
						return Long.parseUnsignedLong(string);
					}
				}
			}
		}

		throw new IOException();
	}

	private String readString(StringStream ss) throws IOException {
		StringBuffer sb = new StringBuffer();

		while (ss.hasNext()) {
			char c = ss.next();
			if (c == '\'') {
				if (ss.hasNext()) {
					if (ss.peek() == '\'') {
						ss.next();
					} else {
						return sb.toString();
					}
				} else {
					return sb.toString();
				}
			}
			sb.append(c);
		}

		throw new IOException("Unterminated string");
	}

	private class StringStream {
		private final String string;
		private int index = 0;

		public StringStream(String string) {
			this.string = string;
		}

		public boolean hasNext() {
			return index < string.length();
		}

		public char next() {
			return string.charAt(index++);
		}

		public char peek(int delta) {
			return string.charAt(index + delta);
		}

		public char previous() {
			return string.charAt(index - 1);
		}

		public char peek() {
			return peek(0);
		}
	}
}
