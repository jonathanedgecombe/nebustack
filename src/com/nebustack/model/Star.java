package com.nebustack.model;

public final class Star extends Vector {
	private final float radius;

	public Star(float x, float y, float radius) {
		super(x, y);
		this.radius = radius;
	}

	public final float getRadius() {
		return radius;
	}
}
