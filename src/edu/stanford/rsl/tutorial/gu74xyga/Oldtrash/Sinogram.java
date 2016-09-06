package edu.stanford.rsl.tutorial.gu74xyga.Oldtrash;

import java.util.ArrayList;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.InterpolationOperators;
import edu.stanford.rsl.conrad.data.numeric.NumericPointwiseOperators;
import edu.stanford.rsl.conrad.geometry.shapes.simple.Box;
import edu.stanford.rsl.conrad.geometry.shapes.simple.PointND;
import edu.stanford.rsl.conrad.geometry.shapes.simple.StraightLine;
import edu.stanford.rsl.conrad.geometry.transforms.Transform;
import edu.stanford.rsl.conrad.geometry.transforms.Translation;
import edu.stanford.rsl.conrad.numerics.SimpleOperators;
import edu.stanford.rsl.conrad.numerics.SimpleVector;
import edu.stanford.rsl.tutorial.phantoms.SheppLogan;

public class Sinogram {
	public Grid2D sinogram;
	public double angle;
	public int detector_pixels;
	public int projection_number;
	public double detector_spacing;
	public Sinogram(Grid2D phantom) {
		this.sinogram = this.PProjection(phantom, 180, 512, 180, 1.0d);
	}
	public Sinogram(Grid2D phantom,double angle,int detector_pixels,int projection_number,double detector_spacing){
		this.angle=angle;
		this.projection_number=projection_number;
		this.detector_pixels=detector_pixels;
		this.detector_spacing=detector_spacing;
		this.sinogram=this.PProjection(phantom, this.angle, this.detector_pixels, this.projection_number, this.detector_spacing);
	}

	public Grid2D PProjection(Grid2D target, double angle, int detector_pixels,
			int projection_number, double detector_spacing) {
		double angle_change = angle / projection_number;
		double samplingRate = 2.d; 
		double detectorSize=detector_pixels*detector_spacing;
		
		Grid2D sinogram = new Grid2D(detector_pixels, projection_number);
		sinogram.setSpacing(detector_spacing, angle_change);

		// set up image bounding box in WC
		Translation trans = new Translation(
				-(target.getSize()[0] * target.getSpacing()[0])/2, -(target.getSize()[1] * target.getSpacing()[1])/2, -1
			);
		Transform inverse = trans.inverse();

		Box b = new Box((target.getSize()[0] * target.getSpacing()[0]), (target.getSize()[1] * target.getSpacing()[1]), 2);
		b.applyTransform(trans);

		for(int e=0; e<projection_number; ++e){
			// compute theta [rad] and angular functions.
			double theta = angle_change * e*2 * Math.PI / 360;
			double cosTheta = Math.cos(theta);
			double sinTheta = Math.sin(theta);

			for (int i = 0; i < detector_pixels; ++i) {
				// compute s, the distance from the detector edge in WC [mm]
				double s = detector_spacing * i - detectorSize / 2;
				
				double normalX = -sinTheta + (s * cosTheta);
				double normalY = (s * sinTheta) + cosTheta;
				
				PointND point1 = new PointND(s * cosTheta, s * sinTheta, .0d);
				PointND point2 = new PointND(normalX,
						normalY, .0d);
				
				StraightLine curve = new StraightLine(point1, point2);
				
				ArrayList<PointND> points = b.intersect(curve);

				
				if (2 != points.size()){
					if(points.size() == 0) {
						curve.getDirection().multiplyBy(-1.d);
						points = b.intersect(curve);
					}
					if(points.size() == 0)
						continue;
				}

				PointND intersectionStartPoint = points.get(0); 
				PointND intersectionEndPoint = points.get(1);   

				// get the normalized increment
				SimpleVector increment = new SimpleVector(
						intersectionEndPoint.getAbstractVector());
				increment.subtract(intersectionStartPoint.getAbstractVector());
				double distance = increment.normL2();
				increment.divideBy(distance * samplingRate);

				double sum = .0;
				intersectionStartPoint = inverse.transform(intersectionStartPoint);
				for (double t = 0.0; t < distance * samplingRate; ++t) {
					PointND current = new PointND(intersectionStartPoint);
					current.getAbstractVector().add(increment.multipliedBy(t));

					double x = current.get(0) / target.getSpacing()[0],
							y = current.get(1) / target.getSpacing()[1];

					if (target.getSize()[0] <= x + 1
							|| target.getSize()[1] <= y + 1
							|| x < 0 || y < 0)
						continue;

					sum += InterpolationOperators.interpolateLinear(target, x, y);
				}
				sum /= samplingRate;
				sinogram.setAtIndex(i, e, (float)sum);
			}
		}
		return sinogram;
	}

	//public static void main(String[] args) {
	//	my_phantom phantom= new my_phantom(512,512,1.0d);
	//	SheppLogan sheppPhantom=new SheppLogan(256);
	//	Sinogram sinogram=new Sinogram(sheppPhantom);
	///	sheppPhantom.show();
	//	phantom.show();
	//	sinogram.sinogram.show();
//	}
}
