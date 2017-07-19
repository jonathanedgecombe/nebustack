package com.nebustack.gui.preview;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

import com.nebustack.file.Frame;
import com.nebustack.model.Star;

@SuppressWarnings("serial")
public final class Preview extends JPanel {
	private final static Color BACKGROUND_COLOR = new Color(128, 128, 128);
	private final static ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(0, 4, 4, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(256));

	private final Histogram histogram;

	private Frame frame = null;
	private BufferedImage renderedFrame = null;

	private int zoomIndex = 0;
	private double x = 0, y = 0;

	private boolean mouseDown = false;
	private int mx, my;

	private double scale = 1, offset = 0;

	public Preview(Histogram histogram) {
		this.histogram = histogram;
		initialize();
	}

	public void initialize() {
		addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				mouseDown = false;
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				mouseDown = true;
				mx = e.getX();
				my = e.getY();
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				mouseDown = false;
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {}
			
			@Override
			public void mouseClicked(MouseEvent e) {}
		});

		addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				mouseDragged(e);
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				if (mouseDown) {
					int nx = e.getX();
					int ny = e.getY();

					double zoom = Math.pow(2, (double) zoomIndex / 2);
					x += (nx - mx) / zoom;
					y += (ny - my) / zoom;

					mx = nx;
					my = ny;

					repaint();
				}
			}
		});

		addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.getWheelRotation() < 0) {
					if (zoomIndex < 12) zoomIndex++;
				} else {
					if (zoomIndex > -6) zoomIndex--;
				}

				repaint();
			}
		});
	}

	public synchronized void setFrame(Frame frame) {
		this.frame = frame;
		update();
	}

	public synchronized void setScaleOffset(int scale, int offset) {
		if (scale == this.scale && offset == this.offset) return;

		this.scale = Math.pow(((double) (scale - 1) / 8) + 1, 1.25);
		this.offset = Math.tanh((50.0 - (double) offset) / 50) / (this.scale * 16);

		double sign = 1;
		if (this.offset < 0) sign = -1;
		this.offset = 2 * Math.pow(Math.abs(this.offset), 0.5) * sign;

		update();
	}

	public synchronized void update() {
		if (frame == null) {
			renderedFrame = null;
			repaint();
			return;
		}

		for (Runnable task : EXECUTOR.getQueue()) {
			((RenderTask) task).cancel();
		}

		EXECUTOR.execute(new PreviewRenderTask(frame, scale, offset, this));
		EXECUTOR.execute(new HistogramRenderTask(frame, histogram));
		repaint();
	}

	public synchronized void setRenderedFrame(BufferedImage renderedFrame) {
		this.renderedFrame = renderedFrame;
		repaint();
	}

	@Override
	public void paint(Graphics gr) {
		Graphics2D g = (Graphics2D) gr;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		BufferedImage img;
		Frame f;
		synchronized (this) {
			img = renderedFrame;
			f = frame;
		}

		g.setColor(BACKGROUND_COLOR);
		g.fillRect(0, 0, getWidth(), getHeight());

		if (renderedFrame == null) return;

		double zoom = Math.pow(2, (double) zoomIndex / 2);

		double centerX = getWidth() / 2;
		double centerY = getHeight() / 2;

		double offsetX = zoom * img.getWidth() / 2;
		double offsetY = zoom * img.getHeight() / 2;

		double width = img.getWidth() * zoom;
		double height = img.getHeight() * zoom;

		g.drawImage(img, (int) (centerX - offsetX + (x * zoom)), (int) (centerY - offsetY + (y * zoom)), (int) width, (int) height, null);

		List<Star> stars = f.getStars();
		if (stars == null) return;

		g.setColor(Color.RED);
		for (Star s : stars) {
			double sx = ((s.getX() + 0.5) * zoom) + (int) (centerX - offsetX + (x * zoom));
			double sy = ((s.getY() + 0.5) * zoom) + (int) (centerY - offsetY + (y * zoom));

			double r = (s.getRadius() + 1) * zoom;
			g.draw(new Ellipse2D.Double(sx - r, sy - r, r * 2, r * 2));
		}
	}

	public synchronized Frame getFrame() {
		return frame;
	}
}
