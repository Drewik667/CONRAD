package edu.stanford.rsl.tutorial.gu74xyga;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;

public class CustomPhantom extends Grid2D {
	private int width;
	private int height;

	public CustomPhantom(int width, int height) {
		super(width, height);
		this.width = width;
		this.height = height;

		int[] offset = { width / 2, height / 2 };
		float intensities[] = { 0.2f, 0.6f, 1.0f };
		int ring_radius_radius = 100;
		int square_size = 160;
		int[] line_pos = { 50, 100 };

		configure(offset, intensities, ring_radius_radius, square_size,
				line_pos);
		// TODO Auto-generated constructor stub
	}

	public void configure(int[] offset, float[] intensities,
			int ring_radius_radius, int square_size, int[] line_pos) {

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				offset[0] = width / 2;
				offset[1] = height / 3 * 2;
				boolean circle = Math.sqrt(Math.pow(i - offset[0], 2)
						+ Math.pow(j - offset[1], 2)) <= ring_radius_radius
						&& Math.sqrt(Math.pow(i - offset[0], 2)
								+ Math.pow(j - offset[1], 2)) >= ring_radius_radius / 2;
				offset[0] = width / 5;
				offset[1] = height / 7 * 3;
				boolean square = (i >= offset[0] && i <= (offset[0] + square_size))
						&& (j >= offset[1] && j <= (offset[1] + square_size));
				float gradient = 1.0f;
				int intersect = (int) (line_pos[0] - gradient * line_pos[1]);
				boolean line = j >= i * gradient + intersect
						&& j <= i * gradient + intersect + 3 && i >= width / 10
						&& j >= width / 10 && i <= width / 10 * 9
						&& j <= width / 10 * 9;
				float intensity = 0.0f;
				if (circle) {
					intensity += intensities[0];
				}
				if (square) {
					intensity += intensities[1];
				}
				if (line) {
					intensity += intensities[2];
				}
				setAtIndex(i, j, intensity);
			}
		}
		// TODO Auto-generated method stub

	}
}