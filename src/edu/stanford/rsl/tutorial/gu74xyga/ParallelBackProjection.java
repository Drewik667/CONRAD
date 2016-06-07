package edu.stanford.rsl.tutorial.gu74xyga;

import java.util.ArrayList;

import edu.stanford.rsl.conrad.data.numeric.Grid1D;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.InterpolationOperators;
import edu.stanford.rsl.conrad.data.numeric.NumericPointwiseOperators;
import edu.stanford.rsl.conrad.geometry.shapes.simple.Box;
import edu.stanford.rsl.conrad.geometry.shapes.simple.PointND;
import edu.stanford.rsl.conrad.geometry.shapes.simple.StraightLine;
import edu.stanford.rsl.conrad.geometry.transforms.Transform;
import edu.stanford.rsl.conrad.geometry.transforms.Translation;
import edu.stanford.rsl.conrad.numerics.SimpleVector;
import edu.stanford.rsl.tutorial.phantoms.SheppLogan;

public class ParallelBackProjection {
	
	public Grid2D filteredSinogram;
	public Grid2D image;
	
	public double angle;
	public int detector_pixels;
	public int projection_number;
	public double detector_spacing;
	
	public ParallelBackProjection(Sinogram sinogram, int image_size_X, int image_size_Y, float image_pixel_size_x,float image_pixel_size_y){
		double sampling_rate=2.d;
		this.angle=sinogram.angle;
		this.projection_number=sinogram.projection_number;
		this.detector_pixels=sinogram.detector_pixels;
		this.detector_spacing=sinogram.detector_spacing;
		double angle_change = angle / projection_number;
		double detectorSize=detector_pixels*detector_spacing;
							
		image =new Grid2D(image_size_X,image_size_Y);
		image.setSpacing(image_pixel_size_x,image_pixel_size_y);
		
		image.setOrigin(-(image.getSize()[0]*image.getSpacing()[0]) / 2.0, -(image.getSize()[1]*image.getSpacing()[1]) / 2.0);

		RampFilterMine ramp= new RampFilterMine(sinogram.sinogram.getSize()[0],1);
		int numberOfProjections=sinogram.sinogram.getSize()[1];
		filteredSinogram= new Grid2D(sinogram.sinogram.getSize()[0],sinogram.sinogram.getSize()[1]);
		for(int theta=0;theta<numberOfProjections;theta++){
			Grid1D filteredSingleProjection=ramp.GridFiltering(sinogram.sinogram.getSubGrid(theta));
			NumericPointwiseOperators.copy(filteredSinogram.getSubGrid(theta), filteredSingleProjection);
		}
		
					
		// set up image bounding box in WC
		Translation trans = new Translation(
				-(image.getSize()[0] * image.getSpacing()[0])/2, -(image.getSize()[1] * image.getSpacing()[1])/2, -1
				);
		Transform inverse = trans.inverse();

		Box b = new Box((image.getSize()[0] * image.getSpacing()[0]), (image.getSize()[1] * image.getSpacing()[1]), 2);
		b.applyTransform(trans);

		for(int e=0; e<projection_number; ++e){
			// compute theta [rad] and angular functions.
			double deltaTheta = angle_change * e*2 * Math.PI / 360;
			
			double cosTheta = Math.cos(deltaTheta);
			double sinTheta = Math.sin(deltaTheta);

			for (int i = 0; i < detector_pixels; ++i) {

				double s = detector_spacing * i - detectorSize / 2;

				PointND p1 = new PointND(s * cosTheta, s * sinTheta, .0d);
				PointND p2 = new PointND(-sinTheta + (s * cosTheta),
						(s * sinTheta) + cosTheta, .0d);

				StraightLine line = new StraightLine(p1, p2);

				ArrayList<PointND> points = b.intersect(line);

				if (2 != points.size()){
					if(points.size() == 0) {
						line.getDirection().multiplyBy(-1.d);
						points = b.intersect(line);
					}
					if(points.size() == 0)
						continue;
				}

				PointND start = points.get(0); 
				PointND end = points.get(1);   
				
				SimpleVector increment = new SimpleVector(end.getAbstractVector());
				increment.subtract(start.getAbstractVector());
				double distance = increment.normL2();
				increment.divideBy(distance * sampling_rate);

				float val = filteredSinogram.getAtIndex(i, e);
				start = inverse.transform(start);

				for (double t = 0.0; t < distance * sampling_rate; ++t) {
					PointND current = new PointND(start);
					current.getAbstractVector().add(increment.multipliedBy(t));

					double x = current.get(0) / image.getSpacing()[0],
							y = current.get(1) / image.getSpacing()[1];

					if (x + 1 > image.getSize()[0] || y + 1 >= image.getSize()[1]|| x < 0 || y < 0)
						continue;

					InterpolationOperators.addInterpolateLinear(image, x, y, val);
				}

			}
		}
		float normalizationFactor = (float) ((float) sampling_rate * projection_number / detector_spacing / Math.PI);
		NumericPointwiseOperators.divideBy(image, normalizationFactor);
			
	}
	
	
	public static void main(String[] args) {
		my_phantom phantom= new my_phantom(512,512,1.0d);
		int phantomSize=256;
		SheppLogan sheppPhantom=new SheppLogan(phantomSize);
		Sinogram sinogram=new Sinogram(sheppPhantom,180,phantomSize,180,1);
		sinogram.sinogram.show();
		ParallelBackProjection paraBackProj= new ParallelBackProjection(sinogram,512,512,(float)phantom.getSpacing()[0],(float)phantom.getSpacing()[1]);
		paraBackProj.filteredSinogram.show();
		paraBackProj.image.show();
	}

}
