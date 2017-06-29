package com.nebustack.file.fits;

public final class Complex {
	private final double re, im;

	public Complex(double re, double im) {
		this.re = re;
		this.im = im;
	}

	public double getReal() {
		return re;
	}

	public double getImaginary() {
		return im;
	}
}
