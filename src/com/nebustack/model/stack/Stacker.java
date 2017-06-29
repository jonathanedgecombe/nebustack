package com.nebustack.model.stack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.nebustack.file.Frame;
import com.nebustack.file.FrameType;
import com.nebustack.model.AffineSolver;
import com.nebustack.model.Star;
import com.nebustack.model.Vector;
import com.nebustack.model.task.Task;

public abstract class Stacker {
	private final static double[][] DEFAULT_TRANSFORM = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};

	protected final int drizzle;

	public Stacker(int drizzle) {
		this.drizzle = drizzle;
	}

	public abstract Frame stackInternal(List<Frame> frames, Map<Frame, double[][]> transformations, int width, int height, double offsetX, double offsetY, int channels, Frame masterDark, Frame masterBias, Frame masterFlat);

	public Frame stack(List<Frame> frames, Task task) {
		return stack(frames, null, task, null, null, null);
	}

	public Frame stack(List<Frame> frames, Frame reference, Task task, Frame masterDark, Frame masterBias, Frame masterFlat) {
		if (frames.isEmpty()) return null;

		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;

		int channels = frames.get(0).getChannels();

		Map<Frame, double[][]> transformations = new HashMap<>();

		if (reference == null) reference = frames.get(0);

		int referenceIterations = 4;
		List<Group> referenceGroups = null;
		if (reference.getStars() != null) {
			referenceIterations = (int) (2000d / reference.getStars().size()) + 3;
			if (referenceIterations > 16) referenceIterations = 16;
			if (referenceIterations < 4) referenceIterations = 4;
			referenceGroups = getGroups(reference.getStars(), referenceIterations);
		}

		Iterator<Frame> iterator = frames.iterator();
		int i = 0, size = frames.size();
		while (iterator.hasNext()) {
			Frame frame = iterator.next();
			if (task != null) task.setProgress(0, "Aligning", frame.getName());

			if (frame.getChannels() != channels) throw new RuntimeException("Mismatching channel type");

			int iterations = 2;
			if (reference.getStars() != null && frame.getStars() != null) {
				iterations = (int) (1000d / Math.min(reference.getStars().size(), frame.getStars().size())) + 1;
				if (iterations > 8) iterations = 8;
				if (iterations < 2) iterations = 2;
			}

			double[][] transform = DEFAULT_TRANSFORM;
			if (reference.getStars() != null && frame != reference && frame.getType().equals(FrameType.LIGHT)) {
				List<Group> groups = getGroups(frame.getStars(), iterations);
				Map<Star, Star> map = solve(groups, referenceGroups, 0.005);
				transform = AffineSolver.solve(map);
			}
			transformations.put(frame, transform);

			Vector tl = bound(transform, 0, 0);
			Vector tr = bound(transform, frame.getWidth(), 0);
			Vector bl = bound(transform, 0, frame.getHeight());
			Vector br = bound(transform, frame.getWidth(), frame.getHeight());

			double lowX = Math.min(Math.min(tl.getX(), tr.getX()), Math.min(bl.getX(), br.getX()));
			double lowY = Math.min(Math.min(tl.getY(), tr.getY()), Math.min(bl.getY(), br.getY()));
			double bigX = Math.max(Math.max(tl.getX(), tr.getX()), Math.max(bl.getX(), br.getX()));
			double bigY = Math.max(Math.max(tl.getY(), tr.getY()), Math.max(bl.getY(), br.getY()));

			if (lowX < minX) minX = lowX;
			if (lowY < minY) minY = lowY;
			if (bigX > maxX) maxX = bigX;
			if (bigY > maxY) maxY = bigY;

			i++;
		}

		int stackedWidth = (int) (Math.ceil(maxX) - Math.floor(minX));
		int stackedHeight = (int) (Math.ceil(maxY) - Math.floor(minY));

		return stackInternal(frames, transformations, stackedWidth, stackedHeight, minX, minY, channels, masterDark, masterBias, masterFlat);
	}

	private static Vector bound(double[][] m, double w, double h) {
		double y = (m[0][0] * (m[1][2] - h) - m[0][2] * m[1][0] + m[1][0] * w) / (m[0][1] * m[1][0] - m[0][0] * m[1][1]);
		double x = (w - y * m[0][1] - m[0][2]) / m[0][0];
		return new Vector(x, y);
	}

	public final static Map<Star, Star> solve(List<Group> groups, List<Group> referenceGroups, double threshold) {
		Map<Star, Star> map = new HashMap<>();

		for (Group g : groups) {
			for (Group rg : referenceGroups) {
				double d = g.compare(rg);
				if (d < threshold) {
					for (int i = 0; i < 5; i++) {
						map.put(g.getStars()[i], rg.getStars()[i]);
					}
				}
			}
		}

		return map;
	}

	public final static List<Group> getGroups(List<Star> stars, int iterations) {
		List<Group> groups = new ArrayList<>();

		for (Star ref : stars) {
			List<Star> sorted = new ArrayList<>();
			sorted.addAll(stars);
			sorted.remove(ref);

			Collections.sort(sorted, (Vector s1, Vector s2) -> Double.compare(s1.sub(ref).mag(), s2.sub(ref).mag()));

			for (int i = 0; i < iterations && i < sorted.size() - 3; i++) {
				Star s1 = sorted.get(i);
				Star s2 = sorted.get(i + 1);
				Star s3 = sorted.get(i + 2);
				Star s4 = sorted.get(i + 2);
	
				double a1 = ref.getAngle(s1, s2);
				double a2 = ref.getAngle(s2, s3);
				double a3 = ref.getAngle(s3, s4);

				double d1 = ref.getDistance(s1);
				double d2 = ref.getDistance(s2);
				double d3 = ref.getDistance(s3);
				double d4 = ref.getDistance(s4);
	
				double r1 = d1 / d2;
				double r2 = d1 / d3;
				double r3 = d1 / d4;
	
				groups.add(new Group(a1, a2, a3, r1, r2, r3, ref, s1, s2, s3, s4));
			}
		}

		return groups;
	}

	public final static class Group {
		private final double a1, a2, a3, r1, r2, r3;
		private final Star[] star;

		public Group(double a1, double a2, double a3, double r1, double r2, double r3, Star... star) {
			this.a1 = a1;
			this.a2 = a2;
			this.a3 = a3;
			this.r1 = r1;
			this.r2 = r2;
			this.r3 = r3;
			this.star = star;
		}

		public double compare(Group pe) {
			return Math.abs(pe.a1 - a1) + Math.abs(pe.a2 - a2) + Math.abs(pe.a3 - a3) + Math.abs(pe.r1 - r1) + Math.abs(pe.r2 - r2) + Math.abs(pe.r3 - r3);
		}

		public Star[] getStars() {
			return star;
		}
	}
}
