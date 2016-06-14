// OpenCL Kernel Function for backprojection

kernel void BP(global float* sinogram, global const int* size, global const double* spacing, int numElements){
    
    
    // get index into global data array
    const unsigned int iGID = get_global_id(0);
    const unsigned int xGID = get_global_id(1);

    
    if (iGID >= numElements)  {
        return;
    }
    
    double cosTheta = cos(theta);
    double sinTheta = sin(theta);
    
    if (xGID >= numElements)  {
        return;
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

