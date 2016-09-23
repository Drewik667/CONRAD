package edu.stanford.rsl.tutorial.gu74xyga;

import edu.stanford.rsl.conrad.data.numeric.Grid1D;
import edu.stanford.rsl.conrad.data.numeric.Grid1DComplex;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;

public class CustomFilter {
	public float detector_spacing;
	public int detector_pixels;
	public int projection_number;
	
	CustomFilter(int projection_number, int detector_pixels,float detector_spacing){
		this.detector_pixels=detector_pixels;
		this.projection_number=projection_number;
		this.detector_spacing=detector_spacing;
	}
	
	public Grid2D rampFilter(Grid2D sinogram) {
		sinogram = transpose(sinogram);
		Grid1DComplex filter = new Grid1DComplex(sinogram.getWidth(), true);
		int filter_size = filter.getSize()[0];
		float filter_spacing = 1 / (detector_spacing * filter_size);
		for (int i = 0; i < filter_size / 2; i++) {
			filter.setAtIndex(i, filter_spacing * i);
			filter.setAtIndex(filter_size - i - 1, filter_spacing * i);
		}
		for (int projection = 0; projection < sinogram.getHeight(); projection++) {
			Grid1DComplex projection_complex = new Grid1DComplex(
					sinogram.getSubGrid(projection), true);
			projection_complex.transformForward();
			for (int i = 0; i < filter_size; i++) {
				projection_complex.setRealAtIndex(
						i,
						projection_complex.getRealAtIndex(i)
								* filter.getRealAtIndex(i));
				projection_complex.setImagAtIndex(
						i,
						projection_complex.getImagAtIndex(i)
								* filter.getRealAtIndex(i));
			}
			projection_complex.transformInverse();
			for (int i = 0; i < sinogram.getWidth(); i++) {
				sinogram.setAtIndex(i, projection,
						projection_complex.getRealAtIndex(i));
			}
		}
		sinogram = transpose(sinogram);
		return sinogram;
	}

	public Grid2D transpose(Grid2D grid) {
		Grid2D grid_transposed = new Grid2D(grid.getHeight(), grid.getWidth());
		grid_transposed.setOrigin(grid.getOrigin()[1], grid.getOrigin()[0]);
		grid_transposed.setSpacing(grid.getSpacing()[1], grid.getSpacing()[0]);
		for (int i = 0; i < grid.getHeight(); i++) {
			for (int j = 0; j < grid.getWidth(); j++) {
				grid_transposed.setAtIndex(i, j, grid.getAtIndex(j, i));
			}
		}
		return grid_transposed;
	}
	
	
	public Grid2D ramLakFilter(Grid2D sinogram) {
		// initialize filter
		Grid1D filter_spatial = new Grid1D(sinogram.getHeight());
		filter_spatial.setSpacing(sinogram.getSpacing()[1]);
		Grid1DComplex filter_frequency = new Grid1DComplex(filter_spatial, true);
		filter_frequency.setAtIndex(0, (float) (1 / (4 *  Math.pow(sinogram.getSpacing()[1], 2))));
		for (int i = 1; i < filter_frequency.getSize()[0] / 2; i++) {
			if (i % 2 == 1) {
				filter_frequency.setAtIndex(i,
						(float) (-1 / (Math.pow(Math.PI, 2) * Math.pow(i * sinogram.getSpacing()[1], 2))));
				filter_frequency.setAtIndex(filter_frequency.getSize()[0] - i,
						(float) (-1 / (Math.pow(Math.PI, 2) * Math.pow(i * sinogram.getSpacing()[1], 2))));
			} else {
				filter_frequency.setAtIndex(i, 0f);
				filter_frequency.setAtIndex(filter_frequency.getSize()[0] - i,
						0f);
			}
		}
		
		filter_frequency.transformForward();
//		sinogram = transpose(sinogram);
		for (int projection = 0; projection < sinogram.getHeight(); projection++) {
			Grid1DComplex projection_complex = new Grid1DComplex(
					sinogram.getSubGrid(projection), true);
			projection_complex.transformForward();
			for (int i = 0; i < filter_frequency.getSize()[0]; i++) {
				projection_complex.setRealAtIndex(
						i,
						projection_complex.getRealAtIndex(i)
								* filter_frequency.getRealAtIndex(i));
				projection_complex.setImagAtIndex(
						i,
						projection_complex.getImagAtIndex(i)
								* filter_frequency.getRealAtIndex(i));
			}
			projection_complex.transformInverse();
			for (int i = 0; i < sinogram.getWidth(); i++) {
				sinogram.setAtIndex(i, projection,
						projection_complex.getRealAtIndex(i));
			}
		}
		sinogram = transpose(sinogram);
		return sinogram;
	}
}
