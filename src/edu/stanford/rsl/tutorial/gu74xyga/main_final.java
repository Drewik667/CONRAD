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
import edu.stanford.rsl.tutorial.gu74xyga.Sinogram;
import edu.stanford.rsl.tutorial.phantoms.SheppLogan;
import ij.ImageJ;


public class main_final {

	public static void main(String args[]) {
		new ImageJ();
		CustomPhantom phantom1 = new CustomPhantom(400, 400);
		
		phantom1.setSpacing(1.0f, 1.0f);
		phantom1.setOrigin(-phantom1.getSpacing()[0] * phantom1.getWidth() / 2
				+ 0.5, -phantom1.getSpacing()[1] * phantom1.getHeight() / 2
				+ 0.5);

		OpenCLGrid2D ocl_phantom = new OpenCLGrid2D(phantom1);

		
		BackProjector BackProjectorDetector = new BackProjector(500, 300, 1f);
		
		Sinogram sino= new Sinogram(phantom1,180,BackProjectorDetector.getPixels(),BackProjectorDetector.getProjections(),1.0f);
		Grid2D sinogram = sino.sinogram;
		sinogram.show("sinogram before filtering");

		
		ParallelBackProjection paraBackProj= new ParallelBackProjection(sino,400,400,1.0f,1.0f);

		paraBackProj.image.show("Original BackprojectionReconstruction");		
		
		Grid2D sinogram_after_filter=paraBackProj.filteredSinogram;

		sinogram_after_filter.show("Sinogram after filter");
		Grid2D sinogram_after_filter_trans=paraBackProj.parallel_back_projection_filter.transpose(sinogram_after_filter);

		Grid2D result_image1 = new Grid2D(400, 400);
		result_image1.setSpacing(1f, 1f);
		result_image1.setOrigin(-result_image1.getSpacing()[0] * result_image1.getWidth() / 2 + 0.5,
				-result_image1.getSpacing()[1] * result_image1.getHeight() / 2 + 0.5);
		result_image1 = BackProjectorDetector.clBackprojection(sinogram_after_filter_trans, result_image1, 16);
		phantom1.show("Phantom");
		result_image1.show("OpenCL BP");
		
		
	}

}
