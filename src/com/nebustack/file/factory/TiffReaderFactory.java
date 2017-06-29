package com.nebustack.file.factory;

import java.io.IOException;
import java.nio.file.Path;

import com.nebustack.file.tiff.TiffReader;

public final class TiffReaderFactory extends ReaderFactory<TiffReader> {
	@Override
	public TiffReader reader(Path path) throws IOException {
		return new TiffReader(path);
	}
}
