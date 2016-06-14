package edu.stanford.rsl.tutorial.gu74xyga;

import java.io.IOException;
import java.nio.FloatBuffer;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLImage2d;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.utils.Configuration;

public class OpenCLBP {
	protected static CLContext context = null;
	protected static CLProgram program = null;
	protected static CLDevice device = null;
	protected static CLKernel kernelFunction = null;
	protected static CLCommandQueue commandQueue = null;
	protected static CLBuffer<FloatBuffer> projectionTex = null; 
	protected static CLBuffer<FloatBuffer> projectionMatrix = null; // The global variable of the module which stores the view matrix
	
		
	public OpenCLBP(Grid2D input, CLContext context, CLDevice device, CLKernel kernelFunction, CLCommandQueue commandQueue, 
			CLImage2d<FloatBuffer> projectionTex, CLBuffer<FloatBuffer> projectionMatrix){
		super();
	}
	
	protected static void createProgram() throws IOException{
	// initialize the program, // load sources, create and build program
		if (program == null || !program.getContext().equals(context)){
			program = context.createProgram(OpenCLBP.class.getResourceAsStream("BP.cl")).build();
		}
	}
	
	protected static void init(){
	    context = CLContext.create(); // set up (uses default CLPlatform and creates context for all devices)
	    System.out.println("created "+context);
	    
	    try{
	    	device = context.getMaxFlopsDevice(); // select fastest device
	        System.out.println("using "+device);
	        
	        commandQueue = device.createCommandQueue();// create command queue on device
	        createProgram();
	                       
			kernelFunction = program.createCLKernel("BP"); // create the computing kernel
			
	        }catch (Exception e) {
	        	if (commandQueue != null)
	        		commandQueue.release();
	    		if (kernelFunction != null)
	    			kernelFunction.release();
	    		if (program != null)
	    			program.release();
	    		if (context != null) // destroy context
	    			context.release(); // cleanup all resources associated with this context.
	    		
	    		e.printStackTrace(); // TODO: handle exception
	    	  }
	}
	
    protected static int roundUp(int groupSize, int globalSize) {
        int r = globalSize % groupSize;
        if (r == 0) {
            return globalSize;
        } else {
            return globalSize + groupSize - r;
        }
    }
	
	public static void main(String[] args){
		Configuration.loadConfiguration();		
		init();
//		int detectorSize = 500;
//		int projectionNumber = 250;
//		int reconWidth = 250;
//		int reconHeight = 250;
//		double[] pixelSpacing = {1, 1};
//		Grid2D sinogram = Radon.sinogram(detectorSize, projectionNumber, reconWidth, reconHeight, pixelSpacing);
	}	
}

