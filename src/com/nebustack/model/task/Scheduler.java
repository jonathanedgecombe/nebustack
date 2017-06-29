package com.nebustack.model.task;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.JFrame;

import com.nebustack.gui.ProgressDialog;

public final class Scheduler implements Runnable {
	private final JFrame parent;
	private final ProgressDialog dialog;
	private final BlockingQueue<Task> tasks = new ArrayBlockingQueue<>(65536);
	private Task task = null;

	public Scheduler(JFrame parent, ProgressDialog dialog) {
		this.parent = parent;
		this.dialog = dialog;

		new Thread(this).start();

		new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

				dialog.update(getProgress(), getMessage(), getSubMessage());
			}
		}).start();
	}

	public synchronized void execute(Task task) {
		tasks.offer(task);
	}

	public synchronized double getProgress() {
		if (task != null) return task.getProgress();
		return 0;
	}

	public synchronized String getMessage() {
		if (task != null) return task.getMessage();
		return Task.EMPTY_MESSAGE;
	}

	public synchronized String getSubMessage() {
		if (task != null) return task.getSubMessage();
		return Task.EMPTY_MESSAGE;
	}

	@Override
	public void run() {
		while (true) {
			try {
				final Task t = tasks.take();

				synchronized (this) {
					task = t;
				}

				parent.setEnabled(false);
				dialog.showDialog();
				t.run();
				dialog.hideDialog();
				parent.setEnabled(true);
				parent.toFront();
			} catch (InterruptedException | InvocationTargetException | RuntimeException e) {
				e.printStackTrace();
				System.exit(0); // TODO
				//throw new RuntimeException(e);
			}
		}
	}
}
