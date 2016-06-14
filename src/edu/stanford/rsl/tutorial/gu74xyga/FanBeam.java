package edu.stanford.rsl.tutorial.av21ufoc;

import java.util.ArrayList;

import ij.ImageJ;
import edu.stanford.rsl.conrad.data.numeric.Grid1D;
import edu.stanford.rsl.conrad.data.numeric.Grid1DComplex;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.InterpolationOperators;
import edu.stanford.rsl.conrad.data.numeric.NumericPointwiseOperators;
import edu.stanford.rsl.conrad.geometry.shapes.simple.Box;
import edu.stanford.rsl.conrad.geometry.shapes.simple.PointND;
import edu.stanford.rsl.conrad.geometry.shapes.simple.StraightLine;
import edu.stanford.rsl.conrad.geometry.transforms.Transform;
import edu.stanford.rsl.conrad.geometry.transforms.Translation;
import edu.stanford.rsl.conrad.numerics.SimpleOperators;
import edu.stanford.rsl.conrad.numerics.SimpleVector;
import edu.stanford.rsl.tutorial.filters.RamLakKernel;

public class FanBeam {
	
	public static final double[] pixelSpacing = {1, 1};
	public static final int maxT = 500;
	public static final int maxBeta = 400;
	public static final int maxTheta = 400;
	public static final int width = 350;
	public static final int height = 350;
	public static final double deltaBeta = 2.0 * Math.PI / maxBeta;
	public static final double deltaT = 1;
	public static final int maxBetaIndex = (int) (maxBeta / deltaBeta);
	public static final int maxTIndex = (int) (maxT / deltaT);
	public static final double samplingRate = 3;
	public static float dSI = 800;
	public static float dSD = 1200; // dSD = focal length
	
	public static Grid2D filter(Grid2D sinogram){
		Grid2D sino = new Grid2D(sinogram);
		double ds = sino.getSpacing()[0];
		for(int i = 0; i < sino.getHeight(); i++){
			Grid1DComplex s = new Grid1DComplex(sino.getSubGrid(i), true);
			s.transformForward();
			int K = s.getSize()[0];
			float df = 1.0f / (K * (float) ds);
			for (int idx = 0; idx < K; idx++){
				s.multiplyAtIndex(idx, Math.min(idx, K - idx - 1) * df);
			}
			s.transformInverse();
			for(int j = 0; j < K; j++){
				sino.setAtIndex(j, i, s.getRealAtIndex(j));
			}
		}
		return sino;
	}
	
	public static Grid2D ramlak(Grid2D sinogram){
		Grid2D sino = new Grid2D(sinogram);
		double ds = sino.getSpacing()[0];
		for(int i = 0; i < sino.getHeight(); i++){
			Grid1D s = sino.getSubGrid(i);
			RamLakKernel r = new RamLakKernel(s.getSize()[0], ds);
			r.applyToGrid(s);
			for(int j = 0; j < sino.getWidth(); j++){
				sino.setAtIndex(j, i, s.getAtIndex(j));
			}
		}
		return sino;
	}
	
	public static Grid2D backprojection(Grid2D sinogram, int[] size, double[] spacing){
		Grid2D grid = new Grid2D(size[0], size[1]);
		grid.setSpacing(spacing);
		grid.setOrigin(-(grid.getSize()[0] * grid.getSpacing()[0]) / 2, -(grid.getSize()[1] * grid.getSpacing()[1]) / 2);
		for(int i = 0; i < sinogram.getSize()[1]; i++){
			double theta = sinogram.getSpacing()[1] * i;
			double cosTheta = Math.cos(theta);
			double sinTheta = Math.sin(theta);
			SimpleVector dirDetector = new SimpleVector(cosTheta,sinTheta);
			for(int x = 0; x < grid.getSize()[0]; x++){
				for(int y = 0; y < grid.getSize()[1]; y++){
					double[] w = grid.indexToPhysical(x, y);
					SimpleVector pixel = new SimpleVector(w[0], w[1]);
					double s = SimpleOperators.multiplyInnerProd(pixel, dirDetector);
					s = sinogram.physicalToIndex(s,theta)[0];
					Grid1D subgrid = sinogram.getSubGrid(i);
					if (subgrid.getSize()[0] < s || s < 0){
						continue;
					}
					float val = InterpolationOperators.interpolateLinear(subgrid, s);
					grid.addAtIndex(x, y, val);
				}
			}
		}
		NumericPointwiseOperators.divideBy(grid, (float) (sinogram.getSize()[1] / Math.PI));
		return grid;		
	}
	
	public static Grid2D fanogram(Grid2D grid){
		Grid2D sino = new Grid2D(maxT, maxBeta);
		sino.setSpacing(deltaT, deltaBeta);
		Translation trans = new Translation(-grid.getSize()[0] / 2.0, -grid.getSize()[1] / 2.0, -1);
		Transform inverse = trans.inverse();
		Box b = new Box(grid.getSize()[0], grid.getSize()[1], 2);
		b.applyTransform(trans);
		for (int i = 0; i < maxBeta; i++) {
			double beta = deltaBeta * i; 
			for (int tIndex = 0; tIndex < maxTIndex; tIndex ++) {
				double t = tIndex * deltaT + deltaT/2 - maxTIndex/2 * deltaT;
				PointND sourcePoint = new PointND(dSI * Math.cos(beta), dSI * Math.sin(beta), 0.d);
				PointND detectorPoint = new PointND(-t * Math.sin(beta) + -dSD * Math.cos(beta), t * Math.cos(beta) + -dSD * Math.sin(beta), 0.d);
				
				StraightLine line = new StraightLine(sourcePoint, detectorPoint);
				ArrayList<PointND> points = b.intersect(line);
				if (2 != points.size()) {
					if (points.size() == 0){
						line.getDirection().multiplyBy(-1.d);
						points = b.intersect(line);
						if (points.size() == 0){
							continue;
						}
					} else {
						continue;
					}
				}
				PointND start = points.get(0);
				PointND end = points.get(1);
				SimpleVector increment = new SimpleVector(end.getAbstractVector());
				increment.subtract(start.getAbstractVector());
				double distance = increment.normL2();
				increment.divideBy(distance * samplingRate);
				double sum = .0;
				start = inverse.transform(start);
				for (double tLine = 0.0; tLine < distance * samplingRate; ++tLine) {
					PointND current = new PointND(start);
					current.getAbstractVector().add(increment.multipliedBy(tLine));
					if (grid.getSize()[0] <= current.get(0) + 1
							|| grid.getSize()[1] <= current.get(1) + 1
							|| current.get(0) < 0 || current.get(1) < 0)
						continue;
					sum += InterpolationOperators.interpolateLinear(grid, current.get(0), current.get(1));
				}
				sum /= samplingRate;
				sino.setAtIndex(tIndex, i, (float) sum);
			}
		}
		return sino;
	}
	
//	for loop s and theta
//	build that 
//	consider what u should put inside considering formulas
//	-- u have to compute gamma
//	double gamma = Math.atan2(maxTIndex, dSD);
//	double s = dSI * Math.sin(gamma);
//	double theta = gamma + beta;
	
	public static Grid2D rebinning(Grid2D fano){
		Grid2D rebinnedSino = new Grid2D(maxT,maxBeta);
		rebinnedSino.setSpacing(deltaT, deltaBeta);
		
		double theta = 0, gamma = 0, value;
		double beta;
		double s,t;
			
		for (int i = 0; i < maxT; i++) {
		//	double t= i * deltaT + deltaT/2 - maxTIndex/2 * deltaT;
			s = i * deltaT + deltaT/2 - maxTIndex/2 * deltaT;
			t = dSD * Math.tan(Math.asin(s/dSI));
			gamma = Math.atan2(t, dSD);
			
			
			for (int j = 0; j < maxTheta; j ++) { //maxBeta equal to maxTheta
				theta = j * deltaBeta; 
				beta = theta - gamma;
				value = InterpolationOperators.interpolateLinear(fano,(t+maxTIndex/2)/deltaT, beta/deltaBeta);
				rebinnedSino.setAtIndex(i, j, (float)value);
			}
		}

		return rebinnedSino;
	}
	
	public static void main(String[] args) {
		new ImageJ();
		BasicImageOperations grid = new BasicImageOperations(width, height, pixelSpacing[0], pixelSpacing[1]);
		// create fanogram
		Grid2D g = fanogram(grid);
		g.show("Fanogram");
		// fırst rebın, then fılter 
		Grid2D g2 = rebinning(g);
		Grid2D gFilter = filter(g2);
		g2.show("Rebinned fanogram");
		gFilter.show("Filtered Fanogram");
		// backproject rebınned fanogram
		Grid2D backProjFano = backprojection(gFilter, new int[]{width, height}, pixelSpacing);
		backProjFano.show("Backprojection Rebinning");
		
		/* sınogram */
		// create sınogram
		Grid2D h = Radon.sinogram(maxT, maxBeta, width, height, pixelSpacing);
		h.show("Sinogram");
		// fılter sınogram
		Grid2D hFilter  = filter(h);
		
	
//		Grid2D g3 = (Grid2D) NumericPointwiseOperators.subtractedBy(g2,g);
//		g3.show("Difference");	
		
		Grid2D backProjFano2 = backprojection(hFilter, new int[]{width, height}, pixelSpacing);
		backProjFano2.show("Backprojection Sinogram");
		
		
	}
}