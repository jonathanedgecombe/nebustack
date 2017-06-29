package com.nebustack.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.orangepalantir.leastsquares.Fitter;
import org.orangepalantir.leastsquares.Function;
import org.orangepalantir.leastsquares.fitters.LinearFitter;

public final class AffineSolver {
	private final static double SD_PRUNE_THRESHOLD = 2;
	private final static int MIN_PLATE_ENTRIES = 4;
	private final static int SOLVE_ITERATIONS = 2;

	public static double[][] solve(Map<Star, Star> points) {
		int size = points.size();
		if (size < MIN_PLATE_ENTRIES) throw new RuntimeException("Too few stars to match");

		double[][] m = {{1, 0, 0}, {0, 1, 0}};

		for (int i = 0; i < SOLVE_ITERATIONS; i++) {
			size = points.size();
			if (size < MIN_PLATE_ENTRIES) break;

			double[][] inputs = new double[size][2];
			double[] xp = new double[size];
			double[] yp = new double[size];

			int n = 0;
			for (Entry<Star, Star> entry : points.entrySet()) {
				inputs[n][0] = entry.getValue().getX();
				inputs[n][1] = entry.getValue().getY();
				xp[n] = entry.getKey().getX();
				yp[n] = entry.getKey().getY();
				n++;
			}

			Fitter fitX = new LinearFitter(Transform.INSTANCE);
			fitX.setData(inputs, xp);
			fitX.setParameters(m[0]);
			fitX.fitData();

			Fitter fitY = new LinearFitter(Transform.INSTANCE);
			fitY.setData(inputs, yp);
			fitY.setParameters(m[1]);
			fitY.fitData();

			m[0] = fitX.getParameters();
			m[1] = fitY.getParameters();

			Map<Vector, Double> error = new HashMap<>();
			double meanError = 0;

			n = 0;
			for (Entry<Star, Star> entry : points.entrySet()) {
				double inX = entry.getValue().getX();
				double inY = entry.getValue().getY();
				double outX = entry.getKey().getX();
				double outY = entry.getKey().getY();
				n++;

				double tX = inX * m[0][0] + inY * m[0][1] + m[0][2];
				double tY = inX * m[1][0] + inY * m[1][1] + m[1][2];

				double dX = tX - outX;
				double dY = tY - outY;

				double e = Math.sqrt((dX * dX) + (dY * dY));
				error.put(entry.getKey(), e);
				meanError += e;
			}

			meanError /= n;

			double sd = 0;
			for (Double e : error.values()) {
				double d = e - meanError;
				sd += d * d;
			}

			sd = Math.sqrt(sd / n);

			Iterator<Entry<Star, Star>> it = points.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Star, Star> e = it.next();
				double d = Math.abs(error.get(e.getKey()) - meanError);
				if (d / sd > SD_PRUNE_THRESHOLD) {
					it.remove();
				}
			}
		}

		return m;
	}

	public static class Transform implements Function {
		public static final Transform INSTANCE = new Transform();

		@Override
		public int getNParameters() {
			return 3;
		}

		@Override
		public int getNInputs() {
			return 2;
		}

		@Override
		public double evaluate(double[] values, double[] parameters) {
			double m1 = parameters[0];
			double m2 = parameters[1];
			double t = parameters[2];

			double x = values[0];
			double y = values[1];

			return m1 * x + m2 * y + t;
		}
	}
}
