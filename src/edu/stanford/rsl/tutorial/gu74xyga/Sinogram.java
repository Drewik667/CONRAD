package edu.stanford.rsl.tutorial.gu74xyga;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.InterpolationOperators;

public class Sinogram {

	private int width;
	private int height;

	public Grid2D sinogram;
	public int projection_number;
	public int detector_pixels;
	public float detector_spacing;
	public CustomPhantom phantom;
	
	public Sinogram(CustomPhantom phantom,int detector_pixels,int projection_number,float detector_spacing) 
	{
		this.projection_number=projection_number;
		this.detector_pixels=detector_pixels;
		this.detector_spacing=detector_spacing;
		this.phantom=phantom;
		sinogram = new Grid2D(projection_number, detector_pixels);
		sinogram.setOrigin(0, (-detector_pixels / 2 + 0.5) * detector_spacing);
		sinogram.setSpacing(1, detector_spacing);
	}
	public Grid2D getSinogram(){
		
		for (int projection = 0; projection < projection_number; projection++) {
			if (projection == 101) {
				int x = 1;
				x++;
			}
			float angle = 180f * projection / projection_number;
			double gradient = -Math.cos(Math.toRadians(angle))
					/ Math.sin(Math.toRadians(angle));
			double x_step = Math.abs(Math.sin(Math.toRadians(180 - angle))
					* phantom.getSpacing()[0]);
			double y_step = Math.sin(Math.toRadians(angle + 270))
					* phantom.getSpacing()[1];
			for (double pixel = sinogram.getOrigin()[1]; pixel <= sinogram
					.getOrigin()[1] + detector_pixels * detector_spacing; pixel += detector_spacing) {
				double[] pixel_pos = { Math.cos(Math.toRadians(angle)) * pixel,
						Math.sin(Math.toRadians(angle)) * pixel };
				double[][] intersects = new LineInBox(phantom.indexToPhysical(
						phantom.getWidth() - 1, phantom.getHeight() - 1)[0],
						phantom.indexToPhysical(phantom.getWidth() - 1,
								phantom.getHeight() - 1)[1],
						phantom.indexToPhysical(0, 0)[0],
						phantom.indexToPhysical(0, 0)[1], gradient, pixel_pos)
						.getBoxIntersects();
				if (intersects[0][0] == -1 && intersects[0][1] == -1
						&& intersects[1][0] == -1 && intersects[1][1] == -1) {
					sinogram.setAtIndex(
							(int) sinogram.physicalToIndex(projection, pixel)[0],
							(int) sinogram.physicalToIndex(projection, pixel)[1],
							0);
					continue;
				}

				int steps = (int) Math.floor(Math.sqrt(Math.pow(
						(intersects[0][0] - intersects[1][0])
								/ phantom.getSpacing()[0], 2)
						+ Math.pow((intersects[0][1] - intersects[1][1])
								/ phantom.getSpacing()[1], 2)));
				if (steps == 0) {
					sinogram.setAtIndex((int) Math.round(sinogram
							.physicalToIndex(projection, pixel)[0]),
							(int) Math.round(sinogram.physicalToIndex(
									projection, pixel)[1]), 0f);
					continue;
				}
				float value = 0.0f;
				for (int element = 0; element < steps; element++) {
					double x_real = intersects[0][0] + element * x_step;
					double y_real = intersects[0][1] + element * y_step;
					value += InterpolationOperators.interpolateLinear(phantom,
							phantom.physicalToIndex(x_real, y_real)[0],
							phantom.physicalToIndex(x_real, y_real)[1]);
				}
				value = value / steps;
				sinogram.setAtIndex((int) Math.round(sinogram.physicalToIndex(
						projection, pixel)[0]), (int) Math.round(sinogram
						.physicalToIndex(projection, pixel)[1]), value);
			}
		}
		return sinogram;
	}
	
	
	
}
