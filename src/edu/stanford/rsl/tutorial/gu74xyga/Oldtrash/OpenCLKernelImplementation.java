package edu.stanford.rsl.tutorial.gu74xyga.Oldtrash;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.opencl.OpenCLGrid2D;
import edu.stanford.rsl.conrad.opencl.OpenCLUtil;
import edu.stanford.rsl.conrad.phantom.renderer.PhantomRenderer;
import edu.stanford.rsl.conrad.utils.Configuration;

import java.io.IOException;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;

import java.nio.FloatBuffer;
import java.util.Random;

import static java.lang.System.*;
import static com.jogamp.opencl.CLMemory.Mem.*;
import static java.lang.Math.*;

//import ij.ImageJ;

public class OpenCLKernelImplementation {
	/*
		look up "recipe" in other tutorials and web
		important steps: configuration, devices, program ("somefile.cl"), 
						 kernel (that's where call functions from your cl-file)
		queue and some closing in the end
	*/
	
	static PhantomRenderer phantom1;
	static PhantomRenderer phantom2;
	protected static CLContext context = null;
	protected static CLProgram program = null;
	protected static CLDevice device = null;
	protected static CLKernel kernelFunction = null;
	protected static CLKernel kernelFunction2 = null;
	protected static CLCommandQueue commandQueue = null;
	protected static CLBuffer<FloatBuffer> phantomCL1 = null;
	protected static CLBuffer<FloatBuffer> phantomCL2 = null;	
	protected static CLBuffer<FloatBuffer> phantomCL3 = null;
	protected static CLBuffer<FloatBuffer> phantomCL4 = null;	
	protected static CLBuffer<FloatBuffer> phantomCLSummation = null;
	protected static Grid2D phantomCLGrid = null;


	public static void main(String[] args) throws IOException {
	//	new ImageJ();
		Configuration.loadConfiguration();
		
//		BasicImageOperations phantom1 = new BasicImageOperations(250, 250, 1, 1);
//		OpenCLGrid2D phantomCL1 = new OpenCLGrid2D(phantom1,OpenCLUtil.getStaticContext(), OpenCLUtil.getStaticContext().getMaxFlopsDevice());
		
//		BasicImageOperations phantom2 = new BasicImageOperations(350, 350, 1, 1);
//		OpenCLGrid2D phantomCL2 = new OpenCLGrid2D(phantom2,OpenCLUtil.getStaticContext(), OpenCLUtil.getStaticContext().getMaxFlopsDevice());
		
		// set up (uses default CLPlatform and creates context for all devices)
        context = CLContext.create();
        System.out.println("created "+context);
        
        // make sure to release the context under all circumstances
        try{
            // select fastest device
            device = context.getMaxFlopsDevice();
            System.out.println("using "+device);

            // create command queue on device
            commandQueue = device.createCommandQueue();
                       
            int elementCount = 1444477;                                  // Length of arrays to process
            int localWorkSize = min(device.getMaxWorkGroupSize(), 256);  // Local work size dimensions
            int globalWorkSize = roundUp(localWorkSize, elementCount);   // rounded up to the nearest multiple of the localWorkSize
			
            // load sources, create and build program
            if (program==null || !program.getContext().equals(context)){
            program = context.createProgram(OpenCLKernelImplementation.class.getResourceAsStream("AddKernels.cl")).build();
            }
            
            // A, B are input buffers, C is for the result
			phantomCL1 = context.createFloatBuffer(globalWorkSize, READ_ONLY);
			phantomCL2 = context.createFloatBuffer(globalWorkSize, READ_ONLY);
			phantomCL3 = context.createFloatBuffer(globalWorkSize, READ_ONLY);
			phantomCL4 = context.createFloatBuffer(globalWorkSize, READ_ONLY);
			phantomCLSummation = context.createFloatBuffer(globalWorkSize, WRITE_ONLY);

          System.out.println("used device memory: "
              + (phantomCL1.getCLSize()+phantomCL2.getCLSize()+phantomCLSummation.getCLSize())/1000000 +"MB");

           
          // get a reference to the kernel function with the name 'AddKernels'
          // and map the buffers to its input parameters.
          kernelFunction = program.createCLKernel("AddKernels");
          kernelFunction.putArgs(phantomCL1, phantomCL2, phantomCLSummation).putArg(elementCount);  
          
          kernelFunction2 = program.createCLKernel("AddKernels");
          kernelFunction2.putArgs(phantomCL3, phantomCL4, phantomCLSummation).putArg(elementCount);
          
          BasicImageOperations phantom = new BasicImageOperations(250, 250, 1,1);  
          BasicImageOperations phantom2= new BasicImageOperations(500,500,1,1); 
          phantomCL1.getBuffer().put(phantom.getBuffer());
          phantomCL2.getBuffer().put(phantom.getBuffer());
          phantomCL1.getBuffer().rewind();
          phantomCL2.getBuffer().rewind();
          phantomCL3.getBuffer().put(phantom2.getBuffer());
          phantomCL4.getBuffer().put(phantom2.getBuffer());
          phantomCL3.getBuffer().rewind();
          phantomCL4.getBuffer().rewind();
      //  
      //    phantomCLSummation.getBuffer().get(phantom.getBuffer());
          
          
          phantomCLSummation.getBuffer().rewind();
          float[] bufferCollection = new float[250*250];
      	  //for (int i = 0; i < 10; ++i) {
      		  //phantom.getBuffer()[i] = 
      				  phantomCLSummation.getBuffer().get(bufferCollection);
      				phantomCLSummation.getBuffer().rewind();
      	//}        
                         
          // asynchronous write of data to GPU device, followed by blocking read to get the computed results back.
          long time = nanoTime();
          commandQueue.putWriteBuffer(phantomCL1, true)
               .putWriteBuffer(phantomCL2, true)
               .putWriteBuffer(phantomCLSummation, true)
               .finish()
               .put1DRangeKernel(kernelFunction, 0, globalWorkSize, localWorkSize)
               .finish()
               .putReadBuffer(phantomCLSummation, true)
               .finish();
          	 time = nanoTime() - time;
          	 
          // print first few elements of the resulting buffer to the console.
          System.out.println("phantomCL1 + phantomCL2 = phantomCLSummation results snapshot: ");
          Grid2D resultGrid= new Grid2D(250,250);
          for(int j=0;j<250;j++){
        	  for(int i = 0; i < 250; i++){
        		  float bufferValue=phantomCLSummation.getBuffer().get();
        		  System.out.println( bufferValue+ ", ");
        		  resultGrid.setAtIndex(i, j, bufferValue);
        		}
          }
          resultGrid.show();		
          phantomCLSummation.getBuffer().clear();
          phantomCLSummation.getBuffer().rewind();
          
          time = nanoTime();
          commandQueue.putWriteBuffer(phantomCL3, true)
               .putWriteBuffer(phantomCL4, true)
               .putWriteBuffer(phantomCLSummation, true)
               .finish()
               .put1DRangeKernel(kernelFunction2, 0, globalWorkSize, localWorkSize)
               .finish()
               .putReadBuffer(phantomCLSummation, true)
               .finish();
          	 time = nanoTime() - time;
          
          	Grid2D resultGrid2= new Grid2D(500,500);
            for(int j=0;j<500;j++){
          	  for(int i = 0; i < 500; i++){
          		  float bufferValue=phantomCLSummation.getBuffer().get();
          		  System.out.println( bufferValue+ ", ");
          		  resultGrid2.setAtIndex(i, j, bufferValue);
          		}
            }
            resultGrid2.show();

          System.out.println("...; " + phantomCLSummation.getBuffer().remaining() + " more");
		  System.out.println("computation took: "+(time/1000000)+"ms");
          
		  //OpenCLGrid2D phantomCL = new OpenCLGrid2D(phantomCLGrid,OpenCLUtil.getStaticContext(), OpenCLUtil.getStaticContext().getMaxFlopsDevice());
	
		    
		  //OpenCLGrid2D phantomGrid = new OpenCLGrid2D(phantomCLSummation, context, device);
		  
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

	public OpenCLKernelImplementation(Grid2D input, CLContext context, CLDevice device, 
			CLKernel kernelFunction, CLCommandQueue commandQueue,CLBuffer<FloatBuffer> phantomCL1, 
			CLBuffer<FloatBuffer> phantomCL2, CLBuffer<FloatBuffer> phantomCLSummation){
		super();
	}
	
    protected static void fillBuffer(FloatBuffer buffer, int seed) {
        Random rnd = new Random(seed);
        while(buffer.remaining() != 0)
            buffer.put(rnd.nextFloat()*100);
        buffer.rewind();
    }

    protected static int roundUp(int groupSize, int globalSize) {
        int r = globalSize % groupSize;
        if (r == 0) {
            return globalSize;
        } else {
            return globalSize + groupSize - r;
        }
    }
}