package edu.stanford.rsl.tutorial.gu74xyga;

import java.util.ArrayList;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.InterpolationOperators;
import edu.stanford.rsl.conrad.geometry.shapes.simple.Box;
import edu.stanford.rsl.conrad.geometry.shapes.simple.PointND;
import edu.stanford.rsl.conrad.geometry.shapes.simple.StraightLine;
import edu.stanford.rsl.conrad.numerics.SimpleVector;

public class PProjection {
	public double [] LineIntegral;
	public PProjection(Grid2D target, double angle, int detector_pixels, int projection_number, double detector_spacing){
		double angle_change= angle/projection_number;
		double internalSpacing=Math.sqrt(target.getSpacing()[0]*target.getSpacing()[0]+target.getSpacing()[1]*target.getSpacing()[1])/2;
		
		this.LineIntegral=new double [detector_pixels];
		int targetSize[]=target.getSize();
		Box phantomBox=new Box(targetSize[0]/2,targetSize[1]/2,0);
		
		PointND lowerCorner=new PointND(-targetSize[0]/2,-targetSize[1]/2,0);
		phantomBox.setLowerCorner(lowerCorner);
		for(int i=0;i<projection_number;i++){
			double theta=i*angle_change*2*Math.PI/360;
			double cosineTheta=Math.cos(theta);
			double sineTheta=Math.sin(theta);
				for (int detector_position=0;detector_position<=detector_pixels;detector_position++){
					double s=detector_position-detector_pixels/2;
					double x=s*cosineTheta;
					double y=s*sineTheta;
					double normalX=x-sineTheta;
					double normalY=y+cosineTheta;
					PointND p1=new PointND(x,y,0);
					PointND p2=new PointND(normalX,normalY,0);
					StraightLine curve=new StraightLine(p1,p2);
					ArrayList<PointND> pointsOnNormal=phantomBox.intersect(curve);
					double distanceBetweenIntersection=0;
					PointND direction=new PointND();
					switch(pointsOnNormal.size()){
					case 0:distanceBetweenIntersection=0;
					case 1:distanceBetweenIntersection=0;
					case 2: 
						PointND intersectionPoint1=pointsOnNormal.get(0);
						PointND intersectionPoint2=pointsOnNormal.get(1);
						distanceBetweenIntersection=intersectionPoint1.euclideanDistance(intersectionPoint2);
						direction=intersectionPoint1;
						direction.getAbstractVector().subtract(intersectionPoint2.getAbstractVector());
					}
					
					int numberOfLineSteps=(int)(distanceBetweenIntersection/internalSpacing);
					for(int j=0;j<numberOfLineSteps;j++){
						
					}
					//= phantomBox.i
					//InterpolationOperators.interpolateLinear(target, x, y);
				//for(int x=0;x<targetSize[0];x++){
					//for(int y=0;y<targetSize[1];y++){
						
						
						//if(y==){
							//target.getPixelValue(Math.sin(angle_change*i), )
						//}
				}
				
				
			}
			
		//}
	//}
	}
}