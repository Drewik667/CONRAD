__kernel void backprojectParallel(__global float* sino, __global float* result, int projectionNumber, int pixelNumber, int cols_rows) {
	size_t index = get_global_id(0);
	if (index >= (cols_rows * cols_rows)){
		return;
	}
	
	//Indices in result image
	float x = index % cols_rows;
	float y = index / cols_rows;
	float rw_isocenter_offset = -((float) cols_rows - 1) / 2;
	float value = 0;
	
	for (int projection = 0; projection < projectionNumber; projection++){
		float angle = 180.0 *  (float) projection / projectionNumber;
		float rw_x = x + rw_isocenter_offset;
		float rw_y = y + rw_isocenter_offset;
		float rw_pixel = rw_x * cos(radians(angle)) + rw_y * sin(radians(angle));
		float pixel = rw_pixel + (pixelNumber - 1) / 2;
		if (pixel < 0 || pixel > (pixelNumber - 1)){
			continue;
		}
		
		// Linear Interpolation this could be also done by using a texture but this way its just faster for us to implement
		float interpolation_value_1 = sino[(int) (trunc(pixel) * (float) projectionNumber + (float) projection)];
		float weightFactor_1 = 1.0 - (pixel - trunc(pixel));
		
		float interpolation_value_2 = sino[(int) (floor(pixel) * (float) projectionNumber + (float) projection)];
		float weightFactor_2 = (pixel - trunc(pixel));
		
		value += weightFactor_1 * interpolation_value_1 + weightFactor_2 * interpolation_value_2;
	}
	result[index] = value;
}
