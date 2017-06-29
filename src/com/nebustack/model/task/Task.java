package com.nebustack.model.task;

import javax.swing.JFrame;

public abstract class Task implements Runnable {
	public final static String EMPTY_MESSAGE = " ";

	protected final JFrame parent;

	private double progress = 0;
	private String message = EMPTY_MESSAGE, subMessage = EMPTY_MESSAGE;

	public Task(JFrame parent) {
		this.parent = parent;
	}

	public synchronized void setProgress(double progress, String message, String subMessage) {
		this.progress = progress;
		this.message = message;
		this.subMessage = subMessage;
	}

	public synchronized void setProgress(double progress, String subMessage) {
		this.progress = progress;
		this.subMessage = subMessage;
	}

	public synchronized double getProgress() {
		return progress;
	}

	public synchronized String getMessage() {
		return message;
	}

	public synchronized String getSubMessage() {
		return subMessage;
	}
}
