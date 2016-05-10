package edu.stanford.rsl.tutorial.gu74xyga;

import java.util.ArrayList;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.InterpolationOperators;
import edu.stanford.rsl.conrad.geometry.shapes.simple.Box;
import edu.stanford.rsl.conrad.geometry.shapes.simple.PointND;
import edu.stanford.rsl.conrad.geometry.shapes.simple.StraightLine;
import edu.stanford.rsl.tutorial.phantoms.SheppLogan;

public class Sinogram {
	public Grid2D sinogram;
	public Sinogram(Grid2D phantom) {
		this.sinogram = this.PProjection(phantom, 180, 256, 180, 1.0d);
	}
	public Sinogram(Grid2D phantom,double angle,int detector_pixels,int projection_number,double detector_spacing){
		this.sinogram=this.PProjection(phantom, angle, detector_pixels, projection_number, detector_spacing);
	}

	public Grid2D PProjection(Grid2D target, double angle, int detector_pixels,
			int projection_number, double detector_spacing) {
		double angle_change = angle / projection_number;
		double internalSpacing = Math.sqrt(target.getSpacing()[0]
				* target.getSpacing()[0] + target.getSpacing()[1]
				* target.getSpacing()[1]) / 2;

		Grid2D sinogram = new Grid2D(detector_pixels, projection_number);
		int targetSize[] = target.getSize();
		Box phantomBox = new Box(targetSize[0]*target.getSpacing()[0], targetSize[1]*target.getSpacing()[1], 0);

		PointND lowerCorner = new PointND(-targetSize[0] / 2*target.getSpacing()[0],
				-targetSize[1] / 2*target.getSpacing()[1], 0);
		PointND upperCorner = new PointND(targetSize[0] / 2*target.getSpacing()[0],
				targetSize[1] / 2*target.getSpacing()[1], 0);
		phantomBox.setLowerCorner(lowerCorner);
		phantomBox.setUpperCorner(upperCorner);
		
		//
		for (int i = 0; i < projection_number; i++) {
			double theta = i * angle_change * 2 * Math.PI / 360;
			double cosineTheta = Math.cos(theta);
			double sineTheta = Math.sin(theta);
			float sum = 0;
			for (int detector_position = 0; detector_position < detector_pixels; detector_position++) {
				double s = detector_position - detector_pixels / 2;
				s*=detector_spacing;
				double x = s * cosineTheta;
				double y = s * sineTheta;
				double normalX = x - sineTheta;
				double normalY = y + cosineTheta;

				PointND p1 = new PointND(x, y, 0);
				PointND p2 = new PointND(normalX, normalY, 0);

				StraightLine curve = new StraightLine(p1, p2);
				ArrayList<PointND> pointsOnNormal = phantomBox.intersect(curve);

				PointND intersectionPoint1 = new PointND();
				PointND intersectionPoint2 = new PointND();

				double distanceBetweenIntersection = 0;
				PointND direction = new PointND();
				switch (pointsOnNormal.size()) {
				case 0:
					distanceBetweenIntersection = 0;
				case 1:
					distanceBetweenIntersection = 0;
				case 2:
					intersectionPoint1 = pointsOnNormal.get(0);
					intersectionPoint2 = pointsOnNormal.get(1);
					System.out.println(pointsOnNormal);
					//distanceBetweenIntersection = intersectionPoint1
						//	.euclideanDistance(intersectionPoint2);
					direction = intersectionPoint2;
					direction.getAbstractVector().subtract(
							intersectionPoint1.getAbstractVector());
					System.out.println(direction);
					distanceBetweenIntersection=direction.getAbstractVector().normL2();
					

				}
				direction.getAbstractVector().divideBy(distanceBetweenIntersection);
				System.out.println(direction);
				double sampling_rate=1/internalSpacing;
				int numberOfLineSteps = (int) (distanceBetweenIntersection *sampling_rate);
				System.out.println(numberOfLineSteps);
				sum=0;
				
				for (int j = 0; j < numberOfLineSteps; j++) {
					PointND nextPoint = new PointND(intersectionPoint1);
					//PointND nextDirection = new PointND(direction);
					direction.getAbstractVector().multipliedBy(j);
					nextPoint.getAbstractVector().add(
							direction.getAbstractVector());
					// double[]
					// coordinatesNextPoint=nextPoint.getCoordinates();
					double xNextPoint = nextPoint.get(0);
					double yNextPoint = nextPoint.get(1);

					sum = sum
							+ InterpolationOperators.interpolateLinear(target,
									xNextPoint, yNextPoint);

				}
				sinogram.setAtIndex(detector_position, i, sum);
			}

		}
		return sinogram;
	}

	public static void main(String[] args) {
		my_phantom phantom= new my_phantom(256,256,1.0d);
		SheppLogan sheppPhantom=new SheppLogan(256);
		Sinogram sinogram=new Sinogram(sheppPhantom);
		sheppPhantom.show();
		phantom.show();
		sinogram.sinogram.show();
	}
}
