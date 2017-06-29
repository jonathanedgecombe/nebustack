package com.nebustack.file.factory;

import java.io.IOException;
import java.nio.file.Path;

import com.nebustack.file.fits.FitsReader;

public final class FitsReaderFactory extends ReaderFactory<FitsReader> {
	//private final FitsSettingsDialog settings;

	public FitsReaderFactory(/*FitsSettingsDialog settings*/) {
		//this.settings = settings;
	}

	@Override
	public FitsReader reader(Path path) throws IOException {
		return new FitsReader(path, /*settings.getChannelType()*/false, false, false);
	}
}
