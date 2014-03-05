package com.gromstudio.treckar.model;

public class RectD {
	double l, t, r, b;
	public RectD(double left, double top, double right, double bottom) {
		l = left; t = top; r = right; b = bottom;
	}		
	public double width() { return r-l; }
	public double height() { return t-b; }
}
