package com.nebustack.gui.preview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public final class Histogram extends JPanel {
	private final static Dimension SIZE = new Dimension(200, 64);
	private final static Color BACKGROUND = new Color(0x22, 0x22, 0x22);

	private BufferedImage img = null;

	public Histogram() {
		setPreferredSize(SIZE);
		setMinimumSize(SIZE);
		setSize(SIZE);
		setMinimumSize(SIZE);
	}

	public void setImage(BufferedImage img) {
		this.img = img;
		repaint();
	}

	@Override
	public void paint(Graphics g) {
		g.setColor(BACKGROUND);
		g.fillRect(0, 0, getWidth(), getHeight());
		if (img != null) g.drawImage(img, 0, 0, null);
	}
}
