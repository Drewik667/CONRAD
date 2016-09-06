package edu.stanford.rsl.tutorial.gu74xyga;

import weka.estimators.MahalanobisEstimator;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.InterpolationOperators;

public class Fanogram extends BackProjector {
	float distanceSI;
	float distanceSD;

	public Fanogram(int pixels, int projections, float spacing,
			float distanceSI, float distanceSD) {
		super(pixels, projections, spacing);
		this.distanceSI = distanceSI;
		this.distanceSD = distanceSD;
	}

	public Grid2D getFanogram(CustomPhantom phantom) {
		float angleIncrement = getAngleIncrement360();
		Grid2D fanogram = new Grid2D(projections, pixels);
		fanogram.setOrigin(0, (-pixels / 2 + 0.5) * spacing);
		fanogram.setSpacing(1, spacing);

		// Source Position
		float projectionAngle = 0;
		for (int projection = 0; projection < projections; projection++) {
			projectionAngle = projection * angleIncrement;
			double[] sourcePosRW = {
					-1 * Math.sin(Math.toRadians(projectionAngle)) * distanceSI,
					Math.cos(Math.toRadians(projectionAngle)) * distanceSI };
			float distanceDI = distanceSD - distanceSI;
			for (int pixel = 0; pixel < pixels; pixel++) {
				double[] pixelPosRW = {
						Math.sin(Math.toRadians(projectionAngle))
								* distanceDI
								+ Math.cos(Math.toRadians(projectionAngle))
								* fanogram.indexToPhysical(projection, pixel)[1],
						-1
								* Math.cos(Math.toRadians(projectionAngle))
								* distanceDI
								+ Math.sin(Math.toRadians(projectionAngle))
								* fanogram.indexToPhysical(projection, pixel)[1] };
				double gradient = (pixelPosRW[1] - sourcePosRW[1])
						/ (pixelPosRW[0] - sourcePosRW[0]);
				double[][] intersects = new LineInBox(phantom.indexToPhysical(
						phantom.getWidth() - 1, phantom.getHeight() - 1)[0],
						phantom.indexToPhysical(phantom.getWidth() - 1,
								phantom.getHeight() - 1)[1],
						phantom.indexToPhysical(0, 0)[0],
						phantom.indexToPhysical(0, 0)[1], gradient, pixelPosRW)
						.getBoxIntersects();
				if (intersects[0][0] == -1 && intersects[0][1] == -1
						&& intersects[1][0] == -1 && intersects[1][1] == -1) {
					fanogram.setAtIndex(projection, pixel, 0);
					continue;
				}

				int steps = (int) Math.floor(Math.sqrt(Math.pow(
						(intersects[0][0] - intersects[1][0])
								/ phantom.getSpacing()[0], 2)
						+ Math.pow((intersects[0][1] - intersects[1][1])
								/ phantom.getSpacing()[1], 2)));
				if (steps == 0) {
					fanogram.setAtIndex((int) Math.round(fanogram
							.physicalToIndex(projection, pixel)[0]),
							(int) Math.round(fanogram.physicalToIndex(
									projection, pixel)[1]), 0f);
					continue;
				}
				double x_step = (intersects[1][0] - intersects[0][0]) / steps;
				double y_step = (intersects[1][1] - intersects[0][1]) / steps;
				float value = 0.0f;
				for (int element = 0; element < steps; element++) {
					double x_real = intersects[0][0] + element * x_step;
					double y_real = intersects[0][1] + element * y_step;
					value += InterpolationOperators.interpolateLinear(phantom,
							phantom.physicalToIndex(x_real, y_real)[0],
							phantom.physicalToIndex(x_real, y_real)[1]);
				}
				value = value / steps;
				fanogram.setAtIndex(projection, pixel, value);
			}
		}

		return fanogram;
	}

	private float getAngleIncrement180() {
		float detectorSize = pixels * spacing;
		float fanAngle = (float) (2 * Math.toDegrees(Math
				.atan((detectorSize - 0.5) / 2 / distanceSD)));
		float angleIncrement = (180 + fanAngle) / projections;
		return angleIncrement;
	}

	private float getAngleIncrement360() {
		float angleIncrement = 360f / projections;
		return angleIncrement;
	}

	public Grid2D rebinning180(Grid2D fanogram) {
		Grid2D sinogram = new Grid2D(fanogram.getWidth(), fanogram.getHeight());
		sinogram.setSpacing(fanogram.getSpacing());
		sinogram.setOrigin(fanogram.getOrigin());
		float detectorSize = pixels * spacing;
		float fanAngle = (float) (2 * Math.toDegrees(Math
				.atan((detectorSize - 1) / 2 / distanceSD)));

		for (int projection = 0; projection < sinogram.getWidth(); projection++) {
			for (int pixel = 0; pixel < sinogram.getHeight(); pixel++) {
				float angleSin = 180f * projection / projections;
				double[] test = sinogram.indexToPhysical(projection, pixel);
				float angleRay = (float) Math.toDegrees(Math.asin(sinogram
						.indexToPhysical(projection, pixel)[1] / distanceSI));
				float angleFan = angleSin - angleRay;
				float distanceFan = (float) (distanceSD * Math.tan(Math
						.toRadians(angleRay)));
				// INTERPOLATE!
				if (projection == 0 && pixel >= 275) {
					int x = 0;
				}
/*
				if (angleFan < 0) {
					angleFan = angleFan + 180 - (2 * angleRay);
					angleRay = -angleRay;
					distanceFan = (float) (distanceSD * Math.tan(Math
							.toRadians(angleRay)));
				} else if (angleFan > 180 + fanAngle) {
					angleFan = angleFan - 180 + (2 * angleRay);
					angleRay = -angleRay;
					distanceFan = (float) (distanceSD * Math.tan(Math
							.toRadians(angleRay)));
				}
*/
				if (angleFan < 0) {
					angleFan += 360;
				} else if (angleFan > 360) {
					angleFan -= 360;
				}
				float projIndexFan = angleFan * (projections - 1)
						/ 360;   // CHANGED FOR 360 DEGREES!!!

				double[] indexFan = fanogram.physicalToIndex(projIndexFan,
						distanceFan);
				float value = 0f;
				if (indexFan[0] > projections - 1 || indexFan[0] < 0) {
					if (indexFan[0] > projections - 1) {
						value = (float) (fanogram.getAtIndex(projections - 1,
								(int) indexFan[1]) * (1 - indexFan[0]
								- projections - 1))
								+ (float) (fanogram.getAtIndex(0,
										(int) indexFan[1]) * (indexFan[0]
										- projections - 1));
					} else {
						value = (float) (fanogram.getAtIndex(projections - 1,
								(int) indexFan[1]) * (Math.abs(indexFan[0])))
								+ (float) (fanogram.getAtIndex(0,
										(int) indexFan[1]) * (1 - Math
										.abs(indexFan[0])));
					}
				} else {
					value = InterpolationOperators.interpolateLinear(fanogram,
							indexFan[0], indexFan[1]);
				}

				sinogram.setAtIndex(projection, pixel, value);
			}
		}

		return sinogram;
	}
}