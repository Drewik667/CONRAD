package edu.stanford.rsl.tutorial.gu74xyga;

import com.sun.org.apache.xpath.internal.operations.Number;

import thredds.wcs.v1_1_0.XMLwriter.ExceptionCodes;

public class LineInBox {
	private double box_right;
	private double box_top;
	private double box_bottom;
	private double box_left;
	private double gradient;
	private double[] intersect;

	public LineInBox(double box_right, double box_top, double box_left, double box_bottom, double gradient,
			double[] intersect) {
		this.box_right = box_right;
		this.box_top = box_top;
		this.gradient = gradient;
		this.intersect = intersect;
		this.box_bottom = box_bottom;
		this.box_left = box_left;
	}

	public double[][] getBoxIntersects() {
		double[][] boxIntersects = { { 0, 0 }, { 0, 0 } };
		double intersect_y = intersect[1] - gradient * intersect[0];
		
		// First Point
		
		boxIntersects[0][1] = box_left * gradient + intersect_y;
		boxIntersects[0][0] = box_left;
		if (Double.isInfinite(gradient)) {
			if (intersect[0] >= box_left && intersect[0] <= box_right) {
				boxIntersects[0][0] = intersect[0];
				boxIntersects[0][1] = box_top;
				boxIntersects[1][0] = intersect[0];
				boxIntersects[1][1] = box_bottom;
				return boxIntersects;
			} else {
				return notInBox();
			}
		}
		if (boxIntersects[0][1] < box_bottom || boxIntersects[0][1] > box_top) {

			if (gradient > 0) {
				boxIntersects[0][0] = (box_bottom - intersect_y) / gradient;
				boxIntersects[0][1] = box_bottom;
			} else {
				boxIntersects[0][0] = (box_top - intersect_y) / gradient;
				boxIntersects[0][1] = box_top;
			}
			if (boxIntersects[0][0] < box_left || boxIntersects[0][0] > box_right) {
				return notInBox();
			}
		}

		// Second Point
		if (boxIntersects[0][0] == box_left) {
			if (gradient > 0) {
				if (!((box_top - intersect_y) / gradient < box_left || (box_top - intersect_y)
						/ gradient > box_right)) {
					boxIntersects[1][0] = (box_top - intersect_y) / gradient;
					boxIntersects[1][1] = box_top;
					return boxIntersects;
				} else {
					boxIntersects[1][0] = box_right;
					boxIntersects[1][1] = gradient * box_right + intersect_y;
					return boxIntersects;
				}
			} else {
				if (!((box_bottom - intersect_y) / gradient < box_left || (box_bottom - intersect_y)
						/ gradient > box_right)) {
					boxIntersects[1][0] = (box_bottom - intersect_y) / gradient;
					boxIntersects[1][1] = box_bottom;
				} else {
					boxIntersects[1][0] = box_right;
					boxIntersects[1][1] = gradient * box_right + intersect_y;
				}
			}
		} else {
			if (boxIntersects[0][1] == box_bottom) {
				boxIntersects[1][1] = box_right * gradient + intersect_y;
				boxIntersects[1][0] = box_right;
				if (boxIntersects[1][1] < box_bottom || boxIntersects[1][1] > box_top) {
					if (gradient > 0) {
						boxIntersects[1][1] = box_top;
						boxIntersects[1][0] = (box_top - intersect_y)
								/ gradient;
					} else {
						boxIntersects[1][1] = box_bottom;
						boxIntersects[1][0] = (box_bottom - intersect_y) / gradient;
					}
				}
			} else {
				boxIntersects[1][1] = box_right * gradient + intersect_y;
				boxIntersects[1][0] = box_right;
				if (boxIntersects[1][1] < box_bottom || boxIntersects[1][1] > box_top) {
					if (gradient < 0) {
						boxIntersects[1][1] = box_bottom;
						boxIntersects[1][0] = (box_bottom - intersect_y)
								/ gradient;
					} else {
						boxIntersects[1][1] = box_top;
						boxIntersects[1][0] = (box_top - intersect_y) / gradient;
					}
				}
			}
		}
		return boxIntersects;
	}

	private double[][] notInBox() {
		double[][] boxIntersects = { { -1, -1 }, { -1, -1 } };
		return boxIntersects;
	}
}