// OpenCL Kernel Function for kernel addition
kernel void AddKernels(global const float* phantomCL1, global const float* phantomCL2, global float* phantomCLSummation, int numElements) {
    
    // get index into global data array
    int iGID = get_global_id(0);
    
    // bound check, equivalent to the limit on a 'for' loop
    if (iGID >= numElements)  {
        return;
    }
    
    // add the elements
    phantomCLSummation[iGID] = phantomCL1[iGID] + phantomCL2[iGID];
}