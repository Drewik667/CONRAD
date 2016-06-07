package edu.stanford.rsl.tutorial.gu74xyga;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;

public class ParallelBackProjection {
	
	public ParallelBackProjection(Grid2D sinogram){
		//System.out.println("1stval: "+ sinogram.getSize()[0]);
		//System.out.println("2ndval: "+ sinogram.getSize()[1]);
		RampFilterMine ramp= new RampFilterMine(sinogram.getSize()[0],1);
		int numberOfProjections=sinogram.getSize()[1];
		Grid2D filteredSinogram= new Grid2D(sinogram.getSize()[0],sinogram.getSize()[1]);
		for(int theta=0;theta<numberOfProjections;theta++){
			
		}
	}
	
	
	public static void main(String[] args) {
		my_phantom phantom= new my_phantom(512,512,1.0d);
		Sinogram sinogram=new Sinogram(phantom);
		sinogram.sinogram.show();
		ParallelBackProjection paraBackProj= new ParallelBackProjection(sinogram.sinogram);
		
	}

}
