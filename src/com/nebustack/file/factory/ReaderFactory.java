package com.nebustack.file.factory;

import java.io.IOException;
import java.nio.file.Path;

import com.nebustack.file.Reader;

public abstract class ReaderFactory<T extends Reader> {
	public abstract T reader(Path path) throws IOException;
}
