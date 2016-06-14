package edu.stanford.rsl.tutorial.gu74xyga;

import edu.stanford.rsl.conrad.data.numeric.*;

public class BasicImageOperations extends Grid2D {

	public BasicImageOperations(int width, int height, double spaceX, double spaceY){
		super(width, height);
		//square
		for(int i = width / 4; i < 3 * width / 4; i++){
			for(int j = height / 4; j < 3 * height / 4; j++){
				setAtIndex(i, j, 0.25f);
			}
		}

		// circle
		for(int i = width / 4; i < 3 * width / 4; i++){
			for(int j = height / 4; j < 3 * height / 4; j++){
				if((i - width / 2) * (i - width / 2) + (j - height / 2) * (j - height / 2) < 100){
					setAtIndex(i, j, 0.5f);
				}
			}
		}
		
		// circle
		int offsetX = 10;
		int offsetY = 20;
		for(int i = width / 4; i < 3 * width / 4; i++){
			for(int j = height / 4; j < 3 * height / 4; j++){
				if((i - offsetX - width / 2) * (i - offsetX - width / 2) + (j - offsetY - height / 2) * (j - offsetY - height / 2) < 100){
					setAtIndex(i, j, 0.5f);
				}
			}
		}
		
	//line
		for(int i = width / 10; i < 9 * width / 10; i++){
			setAtIndex(i, width / 2, 0.75f);
		}
		this.setSpacing(spaceX, spaceY);
		this.setOrigin(- (width - 1) * spaceX / 2.0, - (height - 1) * spaceY / 2.0);
	}
	
	public static void main(String[] args){
		BasicImageOperations op = new BasicImageOperations(500, 500, 0.5, 0.5);
		op.show();
	}

}
/*
import edu.stanford.rsl.conrad.data.numeric.*;
import ij.ImageJ;

public class BasicImageOperations extends Grid2D {
	
	public BasicImageOperations(int width, int height){
		super(width, height);
		for(int i = width / 4; i < 3 * width / 4; i++){
			for(int j = height / 4; j < 3 * height / 4; j++){
				setAtIndex(i, j, 0.25f);
			}
		}
		for(int i = width / 4; i < 3 * width / 4; i++){
			for(int j = height / 4; j < 3 * height / 4; j++){
				if((i - width / 2) * (i - width / 2) + (j - height / 2) * (j - height / 2) < 100){
					setAtIndex(i, j, 0.5f);
				}
			}
		}
	
		for(int i = width / 10; i < 9 * width / 10; i++){
			setAtIndex(i, width / 2, 0.75f);
		}
		
		this.setSpacing(0.5, 0.5);
	}
	
	
	
	public static void main(String[] args){
		//ImageJ im = new ImageJ();
		BasicImageOperations op = new BasicImageOperations(500, 500);
		//BasicImageOperations op2 = new BasicImageOperations(500, 500);
		//NumericPointwiseOperators.subtractBy(op, op2);
		op.show();
		
//		int numberOfProjections, detectorSpacing, numberOfDetectorPixels;
		
	}

}*/