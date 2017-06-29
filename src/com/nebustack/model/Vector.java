package com.nebustack.model;

public class Vector {
	private final double x, y;

	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getDistance(Vector v) {
		return Math.sqrt(((v.x - x) * (v.x - x)) + ((v.y - y) * (v.y - y)));
	}

	public Vector sub(Vector v) {
		return new Vector(x - v.x, y - v.y);
	}

	public double mag() {
		return Math.sqrt((x * x) + (y * y));
	}

	public Vector normalize() {
		double n = mag();
		return new Vector(x / n, y / n);
	}

	public double dot(Vector v) {
		return (x * v.x) + (y * v.y);
	}

	public double getAngle(Vector v1, Vector v2) {
		Vector d1 = v1.sub(this);
		Vector d2 = v2.sub(this);

		double dot = d1.dot(d2);
		double m1 = d1.mag();
		double m2 = d2.mag();

		return Math.acos(dot / (m1 * m2));
	}
}
