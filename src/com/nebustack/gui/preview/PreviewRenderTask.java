package com.nebustack.gui.preview;

import java.awt.image.BufferedImage;

import com.nebustack.file.Frame;

public final class PreviewRenderTask extends RenderTask {
	private final Frame frame;
	private final double scale, offset;
	private final Preview preview;

	public PreviewRenderTask(Frame frame, double scale, double offset, Preview preview) {
		this.frame = frame;
		this.scale = scale;
		this.offset = offset;
		this.preview = preview;
	}

	@Override
	public void run() {
		if (cancelled) return;
		BufferedImage renderedFrame = frame.render(scale, offset, this);
		if (!cancelled && renderedFrame != null) preview.setRenderedFrame(renderedFrame);
	}
}
