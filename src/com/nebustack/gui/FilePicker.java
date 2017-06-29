package com.nebustack.gui;

import java.io.File;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTable;
import com.nebustack.file.FrameType;
import com.nebustack.gui.table.TableModel;
import com.nebustack.model.task.FrameOpenTask;
import com.nebustack.model.task.Scheduler;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;

public final class FilePicker {
	static {
		new JFXPanel();
		Platform.setImplicitExit(false);
	}

	public static void open(JTable table, TableModel model, JFrame parent, Scheduler scheduler, FrameType type) {
		Platform.runLater(() -> {
			FileChooser fileChooser = new FileChooser();
			FileChooser.ExtensionFilter filterAll = new FileChooser.ExtensionFilter("All image files", "*.fits", "*.fit", "*.tiff", "*.tif", "*.png", "*.jpg", "*.jpeg", "*.gif");
			FileChooser.ExtensionFilter filterFITS = new FileChooser.ExtensionFilter("FITS files (*.fits, *.fit)", "*.fits", "*.fit");
			FileChooser.ExtensionFilter filterTIFF = new FileChooser.ExtensionFilter("TIFF files (*.tif, *.tiff)", "*.tiff", "*.tif");
			FileChooser.ExtensionFilter filterSimple = new FileChooser.ExtensionFilter("Image files (*.png, *.jpg, *.jpeg, *.gif)", "*.png", "*.jpg", "*.jpeg", "*.gif");
			fileChooser.getExtensionFilters().addAll(filterAll, filterFITS, filterTIFF, filterSimple);

			List<File> files = fileChooser.showOpenMultipleDialog(null);

			if (files == null) return;

			List<Path> paths = new ArrayList<>();
			for (File file : files) {
				paths.add(Paths.get(file.getAbsolutePath()));
			}

			scheduler.execute(new FrameOpenTask(parent, paths, table, model, type));
		});
	}
}

