package com.nebustack.model.task;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import com.nebustack.file.Frame;
import com.nebustack.file.FrameType;
import com.nebustack.file.Reader;
import com.nebustack.file.factory.FitsReaderFactory;
import com.nebustack.file.factory.ReaderFactory;
import com.nebustack.file.factory.SimpleReaderFactory;
import com.nebustack.file.factory.TiffReaderFactory;
import com.nebustack.gui.table.TableModel;

public final class FrameOpenTask extends Task {
	private final static Map<String, ReaderFactory<? extends Reader>> factories = new HashMap<>();
	private final static TiffReaderFactory TIFF_READER_FACTORY = new TiffReaderFactory();
	private final static FitsReaderFactory FITS_READER_FACTORY = new FitsReaderFactory();
	private final static SimpleReaderFactory SIMPLE_READER_FACTORY = new SimpleReaderFactory();

	static {
		factories.put("tiff", TIFF_READER_FACTORY);
		factories.put("tif", TIFF_READER_FACTORY);
		factories.put("fits", FITS_READER_FACTORY);
		factories.put("fit", FITS_READER_FACTORY);
		factories.put("png", SIMPLE_READER_FACTORY);
		factories.put("jpg", SIMPLE_READER_FACTORY);
		factories.put("jpeg", SIMPLE_READER_FACTORY);
		factories.put("gif", SIMPLE_READER_FACTORY);
	}

	private final List<Path> paths;
	private final JTable table;
	private final TableModel model;
	private final FrameType type;

	public FrameOpenTask(JFrame parent, List<Path> paths, JTable table, TableModel model, FrameType type) {
		super(parent);

		this.paths = paths;
		this.table = table;
		this.model = model;
		this.type = type;
	}

	@Override
	public void run() {
		setProgress(0, "Opening " + type.toString().toLowerCase() + " frames", EMPTY_MESSAGE);

		int i = 0;
		for (Path path : paths) {
			setProgress((double) i / paths.size(), path.getFileName().toString());
			i++;

			for (Entry<String, ReaderFactory<? extends Reader>> entry : factories.entrySet()) {
				if (!path.getFileName().toString().toLowerCase().endsWith(entry.getKey())) continue;
				ReaderFactory<? extends Reader> factory = entry.getValue();

				try {
					Frame frame = factory.reader(path).read(type);
					final boolean select = i == 1;
					SwingUtilities.invokeLater(() -> {
						model.add(frame);

						if (select) {
							int index = model.getFrames().indexOf(frame);
							table.setRowSelectionInterval(index, index);
						}
					});
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}

				break;
			}
		}
	}
}
