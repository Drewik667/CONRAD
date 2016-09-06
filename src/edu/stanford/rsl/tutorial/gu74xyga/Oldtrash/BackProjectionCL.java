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

public class BackProjectionCL {
	private int pixelNumber;
	private int projectionNumber;
	private float spacing;
	public BackProjectionCL(int pixelNumber, int projectionNumber, float spacing){
		this.pixelNumber=pixelNumber;
		this.projectionNumber=projectionNumber;
		this.spacing=spacing;
	}
	
	public Grid2D openCLresultBP(Grid2D sinogram, Grid2D result, int worksize) {
		CLContext context = OpenCLUtil.createContext();
		CLDevice[] devices = context.getDevices();
		CLDevice device = context.getMaxFlopsDevice();
		System.out.println("Device: " + device);

		CLProgram program = null;
		try {
			program = context.createProgram("BackProjection.cl");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		int cols_rows = result.getHeight();
		

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
		kernel.putArg(input).putArg(output).putArg(projectionNumber).putArg(pixelNumber).putArg(cols_rows);
		queue.putWriteBuffer(input, true).finish()
				.put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize)
				.finish().putReadBuffer(output, true).finish();
		for (int i = 0; i < cols_rows * cols_rows; i++) {
			result.getBuffer()[i] = output.getBuffer().get();
		}

		queue.release();
		input.release();
		output.release();
		kernel.release();
		program.release();
		context.release();
		return result;
	}
	public static void main(String[] args) {
		new ImageJ();
		
		my_phantom phantom= new my_phantom(512,512,1.0d);
		int phantomSize=256;
		SheppLogan sheppPhantom=new SheppLogan(phantomSize);
		Sinogram sinogram=new Sinogram(sheppPhantom,180,phantomSize,180,1);
		sinogram.sinogram.show();
		BackProjectionCL();
		
	}	
}