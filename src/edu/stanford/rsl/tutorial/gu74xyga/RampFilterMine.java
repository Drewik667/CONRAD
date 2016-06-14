package edu.stanford.rsl.tutorial.gu74xyga;


import edu.stanford.rsl.conrad.data.numeric.Grid1D;
import edu.stanford.rsl.conrad.data.numeric.Grid1DComplex;

public class RampFilterMine extends Grid1DComplex{
	
	public RampFilterMine(int size, int pixel_dim){
		super(size);
		int zero_padded_size=getSize()[0];
		setAtIndex(0,0.25f/pixel_dim);
		float odd_values = -1.f / ((float) (Math.PI * Math.PI * pixel_dim));
		for(int i=1;i<zero_padded_size/2;i++){
			if (1 == (i % 2))
				setAtIndex(i, odd_values / (i * i));
		}
		for(int i=zero_padded_size/2;i<zero_padded_size;i++){
			int down_step=zero_padded_size-i;
			if (1 == (down_step % 2))
				setAtIndex(i, odd_values / (down_step * down_step));
			//setRealAtIndex(i,(float)down_step/(pixel_dim*zero_padded_size));
		}
		transformForward();
	}
	public Grid1D GridFiltering(Grid1D input_signal){
		Grid1DComplex ComplexSignal = new Grid1DComplex(input_signal);
		
		ComplexSignal.transformForward();
		int SignalSize=ComplexSignal.getSize()[0];
		for(int j=0;j<SignalSize;j++){
			float FilterImagAtIndex=this.getImagAtIndex(j);
			float FilterRealAtIndex=this.getRealAtIndex(j);
			ComplexSignal.multiplyAtIndex(j, FilterRealAtIndex, FilterImagAtIndex);
		}
		ComplexSignal.transformInverse();
		
		Grid1D filteredRealSignal= ComplexSignal.getRealSubGrid(0, SignalSize);
		return filteredRealSignal;
	}
	public static void main(String[] args) {
		
		RampFilterMine ramp=new RampFilterMine(512,1);
		ramp.show();
	}

}
