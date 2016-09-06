
// OpenCL Kernel Function for backprojection

__constant sampler_t sampler= CLK_NORMALIZED_COORDS_FALSE|CLK_ADDRESS_CLAMP_TO_EDGE|CLK_FILTER_LINEAR;

kernel void BP(__global float* resultImage,__read_only float spacingX,__read_only float spacingY,
__read_only image2d_t sinogram,__read_only float originX,__read_only float originY, int numElements, 
__read_only float sizeX,__read_only float sizeY){
    
    
    // get index into global data array
    const unsigned int iGID = get_global_id(0);
    const unsigned int xGID = get_global_id(1);
    const unsigned int yGID = get_global_id(2);
    const unsigned int sGID = get_global_id(3);
        
    if (iGID >= numElements)  {
        return;
    }
    
    double theta = iGID * spacingY;
    double cosTheta = cos(theta);
    double sinTheta = sin(theta);
    
    double2 dirDetector = {cosTheta, sinTheta};
   	//const int x = sizeX;
    //const int y = sizeY;
    
    //float2 gridXH = {x/2.f, y/2.f};
    
    if(xGID > sizeX || yGID > sizeY){
        return;
    }
    
    double2 pixel={xGID * spacingX + originX, yGID * spacingX + originY};
    
    double s=dot(pixel , dirDetector);
    
    double indexS= (s - originX) / spacingX;
    
    if(sGID > indexS){
        return;
    }
    
    resultImage[iGID * sGID] = read_imagef(sinogram, sampler,(float2) (theta,indexS)).x;
    

   }

 /*
    Grid2D grid = new Grid2D(sizeof(*0), sizeof(*1));
    grid.setSpacing(spacing);
    grid.setOrigin(-(grid.getSize()[0] * grid.getSpacing()[0]) / 2, -(grid.getSize()[1] * grid.getSpacing()[1]) / 2);
    for(int i = 0; i < sinogram.getSize()[1]; i++){
        double theta = sinogram.getSpacing()[1] * i;
        double cosTheta = cos(theta);
        double sinTheta = sin(theta);
        SimpleVector dirDetector = new SimpleVector(cosTheta,sinTheta);
        for(int x = 0; x < grid.getSize()[0]; x++){
            for(int y = 0; y < grid.getSize()[1]; y++){
                double* w = grid.indexToPhysical(x, y);
                SimpleVector pixel = new SimpleVector(w[0], w[1]);
                double s = SimpleOperators.multiplyInnerProd(pixel, dirDetector);  --- WRİTE OUT
                s = sinogram.physicalToIndex(s,theta)[0];  ----- PHYSCALİNDEX THNK ABOUT THAT SOME CALING OR TRANSLATION MAZBE
                Grid1D subgrid = sinogram.getSubGrid(i);  -----
                if (subgrid.getSize()[0] < s || s < 0){
                    continue;
                }
                float val = InterpolationOperators.interpolateLinear(subgrid, s); ---LOOK IT UP INTERPOLATION
                grid.addAtIndex(x, y, val);
            }
        }
    }
    NumericPointwiseOperators.divideBy(grid, (float) (sinogram.getSize()[1] / PI));
    return grid;		
}*/

