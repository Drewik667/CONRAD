package edu.stanford.rsl.tutorial.gu74xyga;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;

public class Sinogram {
	public Sinogram(Grid2D phantom){
		PProjection paralellProjection =new PProjection(phantom, 180, 256, 180,1.0d );
	}
	
	public static void main(String[] args) {
	}
}
