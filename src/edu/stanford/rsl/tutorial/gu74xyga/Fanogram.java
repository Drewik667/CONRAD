package edu.stanford.rsl.tutorial.gu74xyga;

import java.util.ArrayList;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.InterpolationOperators;
import edu.stanford.rsl.conrad.geometry.shapes.simple.Box;
import edu.stanford.rsl.conrad.geometry.shapes.simple.PointND;
import edu.stanford.rsl.conrad.geometry.shapes.simple.StraightLine;
import edu.stanford.rsl.conrad.geometry.transforms.Transform;
import edu.stanford.rsl.conrad.geometry.transforms.Translation;
import edu.stanford.rsl.conrad.numerics.SimpleVector;
import edu.stanford.rsl.tutorial.phantoms.SheppLogan;

public class Fanogram {
	public Grid2D fanogram;
	public double fan_angle;
	public int detector_pixels;
	public int projection_number;
	public double detector_spacing;
	public double distance_isocenter;
	public double distance_detector;
	public Fanogram(Grid2D phantom, float detector_spacing,int detector_pixels, double fan_angle, int projection_number, double distance_isocenter, double distance_detector){
		this.detector_pixels=detector_pixels;
		this.projection_number=projection_number;
		this.detector_spacing=detector_spacing;
		this.distance_isocenter=distance_isocenter;
		this.distance_detector=distance_detector;
		this.fan_angle=fan_angle;
		
		fanogram= new Grid2D(this.detector_pixels, this.projection_number);
		fanogram.setSpacing(detector_spacing, this.fan_angle);

		double detectorSize=detector_spacing*detector_pixels;
		double samplingRate=2.d;
		// create translation to the grid origin
		Translation trans = new Translation(-phantom.getSize()[0] / 2.0,
				-phantom.getSize()[1] / 2.0, -1);
		// build the inverse translation
		Transform inverse = trans.inverse();
		
		// set up image bounding box and translate to origin
		Box b = new Box(phantom.getSize()[0], phantom.getSize()[1], 2);
		b.applyTransform(trans);
		
		for (int i = 0; i < this.projection_number; i++) {
			// compute the current rotation angle and its sine and cosine
			double beta = fan_angle * i*2 * Math.PI / 360;
			double cosBeta = Math.cos(beta);
			double sinBeta = Math.sin(beta);
//			System.out.println(beta / Math.PI * 180);
			// compute source position
			PointND a = new PointND(distance_isocenter * cosBeta, distance_isocenter
					* sinBeta, 0.d);
			// compute end point of detector
			PointND p0 = new PointND(-detectorSize / 2.f * sinBeta, detectorSize / 2.f
					* cosBeta, 0.d);

			// create an unit vector that points along the detector
			SimpleVector dirDetector = p0.getAbstractVector().multipliedBy(-1);
			dirDetector.normalizeL2();

			// iterate over the detector elements
			for (int t = 0; t < detector_pixels; t++) {
				// calculate current bin position
				// the detector elements' position are centered
				double stepsDirection = 0.5f * detector_spacing + t * detector_spacing;
				PointND p = new PointND(p0);
				p.getAbstractVector().add(dirDetector.multipliedBy(stepsDirection));
				
				// create a straight line between detector bin and source
				StraightLine line = new StraightLine(a, p);
				
				// find the line's intersection with the box
				ArrayList<PointND> points = b.intersect(line);
				
				// if we have two intersections build the integral 
				// otherwise continue with the next bin
				if (2 != points.size()) {
					if (points.size() == 0) {
						line.getDirection().multiplyBy(-1.d);
						points = b.intersect(line);
						if (points.size() == 0)
							continue;
					} else {
						continue; // last possibility:
						 // a) it is only one intersection point (exactly one of the boundary vertices) or
						 // b) it are infinitely many intersection points (along one of the box boundaries).
						 // c) our code is wrong
					}
					
				}

				// Extract intersections
				PointND start = points.get(0);
				PointND end = points.get(1);

				// get the normalized increment
				SimpleVector increment = new SimpleVector(end.getAbstractVector());
				increment.subtract(start.getAbstractVector());
				double distance = increment.normL2();
				increment.divideBy(distance * samplingRate);

				double sum = .0;
				start = inverse.transform(start);

				double incrementLength = increment.normL2();
				
				for (double tLine = 0.0; tLine < distance * samplingRate; ++tLine) {
					PointND current = new PointND(start);
					current.getAbstractVector().add(increment.multipliedBy(tLine));
					if (phantom.getSize()[0] <= current.get(0) + 1
							|| phantom.getSize()[1] <= current.get(1) + 1
							|| current.get(0) < 0 || current.get(1) < 0)
						continue;
					
					sum += InterpolationOperators.interpolateLinear(phantom,
							current.get(0), current.get(1));
				}

				sum /= samplingRate;
				fanogram.setAtIndex(t, i, (float) sum);
			}
		}

		
	}
	
	public static void main(String[] args) {
		my_phantom phantom= new my_phantom(512,512,1.0d);
			SheppLogan sheppPhantom=new SheppLogan(256);
			Fanogram fanogram=new Fanogram(sheppPhantom,1.0f,512,8,196,20.d,10.d);
			sheppPhantom.show();
			phantom.show();
			fanogram.fanogram.show();

	}

}
