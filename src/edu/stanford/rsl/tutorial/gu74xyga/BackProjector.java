package edu.stanford.rsl.tutorial.gu74xyga;


import java.io.IOException;
import java.nio.FloatBuffer;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;
import com.jogamp.opencl.CLMemory.Mem;

import edu.stanford.rsl.conrad.data.numeric.Grid1D;
import edu.stanford.rsl.conrad.data.numeric.Grid1DComplex;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.InterpolationOperators;
import edu.stanford.rsl.conrad.opencl.OpenCLUtil;

public class BackProjector {
	protected int pixels;
	protected int projections;
	protected float spacing;

	public BackProjector(int pixels, int projections, float spacing) {
		this.setPixels(pixels);
		this.setProjections(projections);
		this.setSpacing(spacing);
	}

	public int getPixels() {
		return pixels;
	}

	public void setPixels(int pixels) {
		this.pixels = pixels;
	}

	public int getProjections() {
		return projections;
	}

	public void setProjections(int projections) {
		this.projections = projections;
	}

	public float getSpacing() {
		return spacing;
	}

	public void setSpacing(float spacing) {
		this.spacing = spacing;
	}

	/*public Grid2D getSinogram(CustomPhantom phantom) {
		Grid2D sinogram = new Grid2D(projections, pixels);
		sinogram.setOrigin(0, (-pixels / 2 + 0.5) * spacing);
		sinogram.setSpacing(1, spacing);
		for (int projection = 0; projection < projections; projection++) {
			if (projection == 101) {
				int x = 1;
				x++;
			}
			float angle = 180f * projection / projections;
			double gradient = -Math.cos(Math.toRadians(angle))
					/ Math.sin(Math.toRadians(angle));
			double x_step = Math.abs(Math.sin(Math.toRadians(180 - angle))
					* phantom.getSpacing()[0]);
			double y_step = Math.sin(Math.toRadians(angle + 270))
					* phantom.getSpacing()[1];
			for (double pixel = sinogram.getOrigin()[1]; pixel <= sinogram
					.getOrigin()[1] + pixels * spacing; pixel += spacing) {
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
	}*/

/*	public Grid2D rampFilter(Grid2D sinogram) {
		sinogram = transpose(sinogram);
		Grid1DComplex filter = new Grid1DComplex(sinogram.getWidth(), true);
		int filter_size = filter.getSize()[0];
		float filter_spacing = 1 / (spacing * filter_size);
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
	}*/

	public Grid2D backproject(Grid2D sinogram, Grid2D result) {
		for (int i = 0; i < result.getWidth(); i++) {
			for (int j = 0; j < result.getHeight(); j++) {
				for (int projection = 0; projection < projections; projection++) {
					double angle = 180f * projection / projections;
					double[] indices = result.indexToPhysical(i, j);
					double x = indices[0];
					double y = indices[1];
					if (i == 100 && j == 300) {
						int asd = 1;
						asd++;
					}
					double s = x * Math.cos(Math.toRadians(angle)) + y
							* Math.sin(Math.toRadians(angle));
					double detector_index = sinogram.physicalToIndex(
							projection, s)[1];
					float value = InterpolationOperators.interpolateLinear(
							sinogram, projection, detector_index);
					result.addAtIndex(i, j, value);
				}
			}
		}
		return result;
	}
	
	public Grid2D clBackprojection(Grid2D sinogram, Grid2D result, int worksize) {
		CLContext context = OpenCLUtil.createContext();
		CLDevice[] devices = context.getDevices();
		CLDevice device = context.getMaxFlopsDevice();
		System.out.println("Device: " + device);

		CLProgram program = null;
		try {
			program = context.createProgram(
					BackProjector.class.getResourceAsStream("BackProjection.cl"))
					.build();
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
		kernel.putArg(input).putArg(output).putArg(projections).putArg(pixels).putArg(cols_rows);
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
}