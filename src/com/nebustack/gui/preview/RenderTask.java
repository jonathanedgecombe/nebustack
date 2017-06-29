package com.nebustack.gui.preview;

public abstract class RenderTask implements Runnable {
	protected boolean cancelled = false;

	public synchronized void cancel() {
		this.cancelled = true;
	}

	public synchronized boolean isCancelled() {
		return cancelled;
	}
}
