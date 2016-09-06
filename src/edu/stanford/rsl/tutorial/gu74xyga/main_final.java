package edu.stanford.rsl.tutorial.gu74xyga;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory.Mem;
import com.jogamp.opencl.CLProgram;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.NumericGridOperator;
import edu.stanford.rsl.conrad.data.numeric.NumericPointwiseOperators;
import edu.stanford.rsl.conrad.data.numeric.opencl.OpenCLGrid2D;
import edu.stanford.rsl.conrad.opencl.OpenCLUtil;
import ij.ImageJ;

public class main_final {

	public static void main(String args[]) {
		new ImageJ();
		CustomPhantom phantom1 = new CustomPhantom(400, 400);
		phantom1.setSpacing(1.0f, 1.0f);
		phantom1.setOrigin(-phantom1.getSpacing()[0] * phantom1.getWidth() / 2
				+ 0.5, -phantom1.getSpacing()[1] * phantom1.getHeight() / 2
				+ 0.5);

		CustomPhantom phantom2 = new CustomPhantom(400, 400);
		phantom2.configure(new int[] { 100, 100 },
				new float[] { 0.2f, 0.6f, 1f }, 200, 50, new int[] { 50, 50 });

		OpenCLGrid2D ocl_phantom = new OpenCLGrid2D(phantom1);

		//long time = System.currentTimeMillis();
		
		
		BackProjector BackProjectorDetector = new BackProjector(500, 300, 1f);
		
		Sinogram sino= new Sinogram(phantom1,BackProjectorDetector.getPixels(),BackProjectorDetector.getProjections(),BackProjectorDetector.getSpacing());
		
		Grid2D sinogram = sino.getSinogram();
		sinogram.show();
		
		CustomFilter main_filter= new CustomFilter(BackProjectorDetector.getPixels(),BackProjectorDetector.getProjections(),BackProjectorDetector.getSpacing());
		sinogram = main_filter.ramLakFilter(sinogram);
				
		Grid2D result_image2 = new Grid2D(400, 400);
		result_image2.setSpacing(1f, 1f);
		result_image2.setOrigin(-result_image2.getSpacing()[0] * result_image2.getWidth() / 2 + 0.5,
				-result_image2.getSpacing()[1] * result_image2.getHeight() / 2 + 0.5);
		result_image2 = BackProjectorDetector.backproject(sinogram, result_image2);
		//result = detector.backproject(sinogram, result);
		//phantom2.show();
		result_image2.show();
		
		
		//sinogram.show();
		Grid2D result_image1 = new Grid2D(400, 400);
		result_image1.setSpacing(1f, 1f);
		result_image1.setOrigin(-result_image1.getSpacing()[0] * result_image1.getWidth() / 2 + 0.5,
				-result_image1.getSpacing()[1] * result_image1.getHeight() / 2 + 0.5);
		result_image1 = BackProjectorDetector.clBackprojection(sinogram, result_image1, 16);
		//result = detector.backproject(sinogram, result);
		phantom1.show();
		result_image1.show();
	}

}
