package edu.stanford.rsl.tutorial.gu74xyga;

import ij.ImageJ;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;


public class my_phantom extends Grid2D {

	public my_phantom(int x, int y,double spacing) {
		super(x, y);
		float valueIntensity1;
		float valueIntensity2;
		float valueIntensity3;
		valueIntensity1 = 300.0f;
		valueIntensity2 = 200.0f;
		valueIntensity3 = 100.0f;
		// spacing=1.0d;
		this.setOrigin(-(double)x/2*spacing,-(double)y/2*spacing);
		this.setSpacing(spacing,spacing);
		this.setAtIndex(x, y, valueIntensity1);
		for (int i = 0; i < x / 4; i++) {
			for (int j = 0; j < y / 4; j++) {
				this.setAtIndex(i, j, valueIntensity1);
			}
		}
		for (int i = (x / 2 + x / 4); i < x / 2 + 2 * x / 4; i++) {
			for (int j = y / 4; j < y / 4 + y / 8; j++) {
				this.setAtIndex(i, j, valueIntensity2);
			}
		}
		for (int i = (x / 4); i < x / 2 + x / 4; i++) {
			for (int j = y / 2 + y / 8; j < y / 2 + 2 * y / 8; j++) {
				this.setAtIndex(i, j, valueIntensity3);
			}
		}
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++){
				int point_position=(i-x/4)*(i-x/4)+(j-(y / 2 + y / 8))*(j-(y / 2 + y / 8));
				if(point_position<(x/8*x/8)){
					this.setAtIndex(i,j, valueIntensity2);
				}
			}
		}
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++){
			this.indexToPhysical(i, j);	
			}		
		}
	}

	public static void main(String[] args) {
		ImageJ ImageJUI=new ImageJ();
		my_phantom minePhantom = new my_phantom(512,512,0.5d);
		double[] physical_value=minePhantom.indexToPhysical(0, 0);
		System.out.println(physical_value[1]);
		minePhantom.show();
	}

}
