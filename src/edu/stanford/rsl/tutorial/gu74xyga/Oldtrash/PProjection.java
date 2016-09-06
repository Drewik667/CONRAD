package edu.stanford.rsl.tutorial.gu74xyga.Oldtrash;

import java.util.ArrayList;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.InterpolationOperators;
import edu.stanford.rsl.conrad.geometry.shapes.simple.Box;
import edu.stanford.rsl.conrad.geometry.shapes.simple.PointND;
import edu.stanford.rsl.conrad.geometry.shapes.simple.StraightLine;
import edu.stanford.rsl.conrad.geometry.transforms.Transform;
import edu.stanford.rsl.conrad.geometry.transforms.Translation;
import edu.stanford.rsl.conrad.numerics.SimpleOperators;
import edu.stanford.rsl.conrad.numerics.SimpleVector;
//////////////////////////////////////////////////////////////////////////////////////////////////
///// THIS IS A BACKUP FILE IN WHICH THE WRONG SOLUTIONS ARE STORED!! NOTHING TO LOOK FOR IN HERE  
//THIS IS NOT THE CODE THAT YOU'RE LOOKING FOR!!
////
///

double angle_change = angle / projection_number;
double detector_size=detector_pixels*detector_spacing;
double internalSpacing = 2.d; 

Grid2D sinogram = new Grid2D(detector_pixels,projection_number);
sinogram.setSpacing(detector_spacing,angle_change);
int targetSize[] = target.getSize();

target.setOrigin(-target.getSize()[0]*target.getSpacing()[0]/2,target.getSize()[1]*target.getSpacing()[1]/2);

Box phantomBox = new Box((target.getSize()[0] * target.getSpacing()[0]),(target.getSize()[1] * target.getSpacing()[1]), 1);

Translation Box_translation = new Translation(
		-(target.getSize()[0] * target.getSpacing()[0])/2, (target.getSize()[1] * target.getSpacing()[1])/2, 0
	);
phantomBox.applyTransform(Box_translation);
/*PointND lowerCorner = new PointND(-targetSize[0] / 2*target.getSpacing()[0],
		-targetSize[1] / 2*target.getSpacing()[1], 0);
PointND upperCorner = new PointND(-targetSize[0] / 2*target.getSpacing()[0],
		targetSize[1] / 2*target.getSpacing()[1], 0);
phantomBox.setLowerCorner(lowerCorner);
phantomBox.setUpperCorner(upperCorner);
*/
//THIS IS NOT THE CODE THAT YOU'RE LOOKING FOR!!
//projection_number
for (int i = 0; i < projection_number; i++) {
	double theta = i * angle_change * 2 * Math.PI / 360;
	double cosineTheta = Math.cos(theta);
	double sineTheta = Math.sin(theta);
	
	//float sum = 0;
	for (int detector_position = 0; detector_position < detector_pixels; detector_position++) {
		//System.out.println(i);
		//System.out.println(detector_position);
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

		//THIS IS NOT THE CODE THAT YOU'RE LOOKING FOR!!
		PointND intersectionStartPoint=new PointND();
		PointND intersectionEndPoint=new PointND();
		
		//double distanceBetweenIntersection = 0;
		//PointND direction = new PointND();
		//System.out.println(pointsOnNormal.size());
		if (2 != pointsOnNormal.size()){
			if(pointsOnNormal.size() == 0) {
				curve.getDirection().multiplyBy(-1.d);
				pointsOnNormal = phantomBox.intersect(curve);
			}
			if(pointsOnNormal.size() == 0)
				continue;
		}
		intersectionStartPoint=pointsOnNormal.get(0);
		intersectionEndPoint=pointsOnNormal.get(1);
		
		SimpleVector intersectionLineIncrement=new SimpleVector(intersectionEndPoint.getAbstractVector());
		intersectionLineIncrement.subtract(intersectionStartPoint.getAbstractVector());
		double intersectionDistance=intersectionLineIncrement.normL2();
		intersectionLineIncrement.divideBy(intersectionDistance*internalSpacing);
		//THIS IS NOT THE CODE THAT YOU'RE LOOKING FOR!!
		// check if both directions are equal
		SimpleVector tmp = new SimpleVector(p2.getAbstractVector());
		tmp.subtract(p1.getAbstractVector());
		tmp.normalizeL2();
		double testVal = SimpleOperators.multiplyInnerProd(intersectionLineIncrement,tmp);
		System.out.println("Scalar product of both computed directions" + testVal);
		
		double numberOfLineSteps = intersectionDistance*internalSpacing;
		double lineIntegral=0.d;
		for (double j = 0.0d; j < numberOfLineSteps; j++) {
			PointND nextPoint = new PointND(intersectionStartPoint);
			nextPoint.getAbstractVector().add(intersectionLineIncrement.multipliedBy(j));
			//System.out.println(nextPoint.get(0));
			//System.out.println(target.getSpacing()[0]);
			
			double xNextPoint=nextPoint.get(0);///target.getSpacing()[0];
			double yNextPoint=nextPoint.get(1);//target.getSpacing()[1];
			double [] targetIndex=target.physicalToIndex(nextPoint.get(0), nextPoint.get(1));
			//double xNextPoint=targetIndex[0];
			//double yNextPoint=targetIndex[1];
			//System.out.println(xNextPoint);
			//System.out.println(yNextPoint);
			if (target.getSize()[0] <= xNextPoint + 1
					|| target.getSize()[1] <= yNextPoint + 1
					|| xNextPoint < 0 || yNextPoint < 0)
				continue;
			//System.out.println(lineIntegral);
			lineIntegral = lineIntegral + InterpolationOperators.interpolateLinear(
					target, xNextPoint,yNextPoint);
					
			
		}
		lineIntegral=lineIntegral/internalSpacing;
		//System.out.println(i);
		//System.out.println(detector_position);
		//System.out.println(lineIntegral);
		sinogram.setAtIndex(detector_position, i, (float)lineIntegral);
		/*
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
		sinogram.setAtIndex(detector_position, i, sum);*/
	}

}







public class PProjection {

	public Grid2D PProjection(Grid2D target, double angle, int detector_pixels,
			int projection_number, double detector_spacing) {
		double detector_size=detector_pixels*detector_spacing;
		double angle_change = angle / projection_number;
		double internalSpacing = 2.d; 

		Grid2D sinogram= new Grid2D(new float [projection_number*detector_pixels],projection_number,detector_pixels);
//		this.LineIntegral = new double[detector_pixels];
		sinogram.setSpacing(detector_spacing,angle_change);
		int targetSize[] = target.getSize();
		
		Translation Box_translation = new Translation(
				-(target.getSize()[0] * target.getSpacing()[0])/2, -(target.getSize()[1] * target.getSpacing()[1])/2, -1
			);
		Transform inverse_Box_translation = Box_translation.inverse();
		
		Box phantomBox = new Box((target.getSize()[0] * target.getSpacing()[0])/2,(target.getSize()[1] * target.getSpacing()[1])/2, 2);
		phantomBox.applyTransform(Box_translation);
		
		//PointND lowerCorner = new PointND(-targetSize[0] / 2,
			//	-targetSize[1] / 2, 0);
		//phantomBox.setLowerCorner(lowerCorner);
		for (int i = 0; i < projection_number; i++) {
			
			double theta = i * angle_change * 2 * Math.PI / 360;
			double cosineTheta = Math.cos(theta);
			double sineTheta = Math.sin(theta);
			
			
			float sum = 0;
			for (int detector_position = 0; detector_position <= detector_pixels; detector_position++) {
				double s = detector_position*detector_spacing - detector_size / 2;
				double x = s * cosineTheta;
				double y = s * sineTheta;
				double normalX = x - sineTheta;
				double normalY = y + cosineTheta;

				PointND p1 = new PointND(x, y, 0);
				PointND p2 = new PointND(normalX, normalY, 0);

				StraightLine curve = new StraightLine(p1, p2);
				ArrayList<PointND> pointsOnNormal = phantomBox.intersect(curve);

				PointND intersectionStartPoint=new PointND();
				PointND intersectionEndPoint=new PointND();
				
				double distanceBetweenIntersection = 0;
				PointND direction = new PointND();
				if (2 != pointsOnNormal.size()){
					if(pointsOnNormal.size() == 0) {
						curve.getDirection().multiplyBy(-1.d);
						pointsOnNormal = phantomBox.intersect(curve);
					}
					if(pointsOnNormal.size() == 0)
						continue;
				}
				intersectionStartPoint=pointsOnNormal.get(0);
				intersectionEndPoint=pointsOnNormal.get(1);
				
				SimpleVector intersectionLineIncrement=new SimpleVector(intersectionEndPoint.getAbstractVector());
				intersectionLineIncrement.subtract(intersectionStartPoint.getAbstractVector());
				double intersectionDistance=intersectionLineIncrement.normL2();
				intersectionLineIncrement.divideBy(intersectionDistance*internalSpacing);
				/*switch (pointsOnNormal.size()) {
				case 0:
					distanceBetweenIntersection = 0;
				case 1:
					distanceBetweenIntersection = 0;
				case 2:
					intersectionPoint1 = 
					intersectionPoint2 = pointsOnNormal.get(1);
					distanceBetweenIntersection = intersectionPoint1
							.euclideanDistance(intersectionPoint2);
					direction = intersectionPoint1;
					direction.getAbstractVector().subtract(
							intersectionPoint2.getAbstractVector());


				}*/
				double numberOfLineSteps = intersectionDistance*internalSpacing;
				intersectionStartPoint.applyTransform(inverse_Box_translation);
				double lineIntegral=0.d;
				for (double j = 0.0d; j < numberOfLineSteps; j++) {
					PointND nextPoint = new PointND(intersectionStartPoint);
					nextPoint.getAbstractVector().add(intersectionLineIncrement.multipliedBy(j));
					double xNextPoint=nextPoint.get(0)/target.getSpacing()[0];
					double yNextPoint=nextPoint.get(1)/target.getSpacing()[1];
					
					if (target.getSize()[0] <= x + 1
							|| target.getSize()[1] <= y + 1
							|| x < 0 || y < 0)
						continue;
					//nextDirection.getAbstractVector().multiplyBy(
					//		distanceBetweenIntersection * j);
					//nextPoint.getAbstractVector().add(
					//		nextDirection.getAbstractVector());
					// double[]
					// coordinatesNextPoint=nextPoint.getCoordinates();
					//double xNextPoint = nextPoint.get(0);
					//double yNextPoint = nextPoint.get(1);

					lineIntegral = lineIntegral + InterpolationOperators.interpolateLinear(
							target, xNextPoint, yNextPoint);
							
					
				}
				lineIntegral=lineIntegral/internalSpacing;
				//sinogram.setAtIndex(detector_position, i, sum);
				
				//return sinogram;
				// = phantomBox.i
				//InterpolationOperators.interpolateLinear(target, x, y);
				// for(int x=0;x<targetSize[0];x++){
				// for(int y=0;y<targetSize[1];y++){

				// if(y==){
				// target.getPixelValue(Math.sin(angle_change*i), )
				// }
			}

		}
		return sinogram;

		// }
		// }
	}
}