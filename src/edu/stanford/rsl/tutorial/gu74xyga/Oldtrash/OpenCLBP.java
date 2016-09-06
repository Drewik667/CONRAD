package edu.stanford.rsl.tutorial.gu74xyga.Oldtrash;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Arrays;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLImage2d;
import com.jogamp.opencl.CLImageFormat;
import com.jogamp.opencl.CLImageFormat.ChannelOrder;
import com.jogamp.opencl.CLImageFormat.ChannelType;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory.Mem;
import com.jogamp.opencl.CLProgram;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.opencl.OpenCLGrid2D;
import edu.stanford.rsl.conrad.opencl.OpenCLUtil;
import edu.stanford.rsl.conrad.utils.Configuration;
import edu.stanford.rsl.tutorial.phantoms.SheppLogan;
import ij.ImageJ;

public class OpenCLBP {
	public Grid2D sinogram;
	public Grid2D filteredSinogram;
	public Grid2D image;
	
	public double angle;
	public int detector_pixels;
	public int projection_number;
	public double detector_spacing;
	
	public OpenCLBP(Sinogram sinogram, Grid2D image)
	{
		this.sinogram=sinogram.sinogram;
		this.angle=sinogram.angle;
		this.detector_pixels=sinogram.detector_pixels;
		this.projection_number=sinogram.projection_number;
		this.detector_spacing=sinogram.detector_spacing;
		this.image=image;
		
	}
	public Grid2D CLBP(int worksize) {
		CLContext context = OpenCLUtil.createContext();
		CLDevice[] devices = context.getDevices();
		CLDevice device = context.getMaxFlopsDevice();
		System.out.println("Device: " + device);

		CLProgram program = null;
		try {
			program = context.createProgram(
					OpenCLBP.class.getResourceAsStream("BackProjection.cl"))
					.build();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		int cols_rows = image.getHeight();
		

		CLBuffer<FloatBuffer> input = context.createFloatBuffer(
				sinogram.getHeight() * sinogram.getWidth(), Mem.READ_ONLY);
		for (int i = 0; i < sinogram.getHeight() * sinogram.getWidth(); i++) {
			input.getBuffer().put(sinogram.getBuffer()[i]);
		}
		input.getBuffer().rewind();

		CLBuffer<FloatBuffer> output = context.createFloatBuffer(
				cols_rows * cols_rows, Mem.WRITE_ONLY);

		int localWorkSize = Math.min(device.getMaxWorkGroupSize(), worksize);
		int globalWorkSize = OpenCLUtil.roundUp(localWorkSize, cols_rows * cols_rows);

		CLKernel kernel = program.createCLKernel("backprojectParallel");
		CLCommandQueue queue = device.createCommandQueue();
		kernel.putArg(input).putArg(output).putArg(projection_number).putArg(detector_pixels).putArg(cols_rows);
		queue.putWriteBuffer(input, true).finish()
				.put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize)
				.finish().putReadBuffer(output, true).finish();
		for (int i = 0; i < cols_rows * cols_rows; i++) {
			image.getBuffer()[i] = output.getBuffer().get();
		}

		queue.release();
		input.release();
		output.release();
		kernel.release();
		program.release();
		context.release();
		return image;
	}
	
	public static void main(String[] args){
		Configuration.loadConfiguration();	
		
		//init();
		int phantomSize=512;
		SheppLogan sheppPhantom=new SheppLogan(phantomSize);
		Sinogram sinogram=new Sinogram(sheppPhantom,180,phantomSize,300,1);
		sinogram.sinogram.show();
		Grid2D result = new Grid2D(600, 600);
		result.setSpacing(1f, 1f);
		result.setOrigin(-result.getSpacing()[0] * result.getWidth() / 2 + 0.5,
				-result.getSpacing()[1] * result.getHeight() / 2 + 0.5);
		OpenCLBP clBackProjection= new OpenCLBP(sinogram,result);
		result = clBackProjection.CLBP(16);
		result.show();
		
	}	
}




