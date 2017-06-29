package com.nebustack.file.factory;

import java.io.IOException;
import java.nio.file.Path;

import com.nebustack.file.simple.SimpleReader;

public final class SimpleReaderFactory extends ReaderFactory<SimpleReader> {
	@Override
	public SimpleReader reader(Path path) throws IOException {
		return new SimpleReader(path);
	}
}
