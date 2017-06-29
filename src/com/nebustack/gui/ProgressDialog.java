package com.nebustack.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.nebustack.model.task.Task;

@SuppressWarnings("serial")
public final class ProgressDialog extends JDialog {
	private final static Dimension PROGRESS_BAR_SIZE = new Dimension(420, 16);

	private final JLabel label = new JLabel(Task.EMPTY_MESSAGE);
	private final JLabel subLabel = new JLabel(Task.EMPTY_MESSAGE);
	private final JProgressBar progressBar = new JProgressBar(0, 420);

	private final JFrame parent;

	public ProgressDialog(JFrame parent) {
		super(parent);

		this.parent = parent;

		setTitle(Task.EMPTY_MESSAGE);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setLayout(new BorderLayout());
		setResizable(false);

		populate();
		pack();

		setLocationRelativeTo(parent);
	}

	private void populate() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(new EmptyBorder(12, 24, 16, 24));

		label.setBorder(new EmptyBorder(0, 0, 4, 0));
		label.setHorizontalAlignment(JLabel.HORIZONTAL);
		panel.add(label, BorderLayout.NORTH);

		subLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
		subLabel.setHorizontalAlignment(JLabel.HORIZONTAL);
		panel.add(subLabel, BorderLayout.CENTER);

		progressBar.setMinimumSize(PROGRESS_BAR_SIZE);
		progressBar.setMaximumSize(PROGRESS_BAR_SIZE);
		progressBar.setPreferredSize(PROGRESS_BAR_SIZE);
		panel.add(progressBar, BorderLayout.SOUTH);

		add(panel, BorderLayout.CENTER);
	}

	public void showDialog() throws InvocationTargetException, InterruptedException {
		SwingUtilities.invokeAndWait(() -> {
			setLocationRelativeTo(parent);
			setVisible(true);
		});
	}

	public void hideDialog() throws InvocationTargetException, InterruptedException {
		SwingUtilities.invokeAndWait(() -> {
			setVisible(false);
			parent.toFront();
			parent.repaint();
		});
	}

	public synchronized void update(double progress, String message, String subMessage) {
		SwingUtilities.invokeLater(() -> {
			progressBar.setValue(Math.min(420, Math.max(0, (int) (progress * 420))));
			label.setText(message);
			subLabel.setText(subMessage);
			setTitle(message);
		});
	}
}
