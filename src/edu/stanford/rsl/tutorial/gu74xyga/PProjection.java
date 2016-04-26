package edu.stanford.rsl.tutorial.gu74xyga;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.InterpolationOperators;

public class PProjection {
	public double [] LineIntegral;
	public PProjection(Grid2D target, double angle, int detector_pixels, int projection_number, double detector_spacing){
		double angle_change= angle/projection_number;
		this.LineIntegral=new double [detector_pixels];
		int targetSize[]=target.getSize();
		for(int i=0;i<projection_number;i++){
			double theta=i*angle_change;
			for (int detector_position=0;detector_position<=detector_pixels;detector_position++){
				for(int x=0;x<targetSize[0];x++){
					for(int y=0;y<targetSize[1];y++)
						InterpolationOperators.interpolateLinear(target, x*Math.tan(theta+90)+detector_position, y);
						
						if(y==){
							target.getPixelValue(Math.sin(angle_change*i), )
						}
				}
				
				
			}
			
		}
	}
}
