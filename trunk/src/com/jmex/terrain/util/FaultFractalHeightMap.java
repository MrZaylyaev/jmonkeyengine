/*
 * Copyright (c) 2003-2008 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jmex.terrain.util;

import java.util.Random;
import java.util.logging.Logger;

import com.jme.system.JmeException;

/**
 * <code>FaultFractalHeightMap</code> creates a heightmap based on the
 * Fault Formation fractal generation algorithm based on Jason Shankel's
 * paper from "Game Programming Gems". Terrain is generated by simulating
 * Earth quakes, where a random line is drawn through the heightmap and
 * one side is lifted by a factor. This is then run through a FIR filter
 * to simulate water errosion.
 *
 * @author Mark Powell
 * @version $Id: FaultFractalHeightMap.java,v 1.5 2007/08/02 23:16:21 nca Exp $
 */
public class FaultFractalHeightMap extends AbstractHeightMap {
    private static final Logger logger = Logger
            .getLogger(FaultFractalHeightMap.class.getName());

	//Attributes of the fractal.
	private int iterations; //how many faults to generate.
	private int minDelta; //the range of height increases (min)
	private int maxDelta; //the range of height increases (max)

    /**
     * Constructor sets the attributes of the fault fractal system and
     * generates the heightmap.
     *
     * @param size the size of the terrain to be generated.
     * @param iterations the number of faults to generate.
     * @param minDelta the minimum varience in the height increase between
     *      faults.
     * @param maxDelta the maximum varience in the height increase between
     *      faults.
     * @param filter the filter used for erosion. Filter can be between 0 and
     *      1, where 0 is no filtering and 1 is extreme filtering. Suggested
     *      values are 0.2-0.4.
     *
     * @throws JmeException if size is not greater than zero,
     *      iterations is not greater than zero, minDelta is more than
     *      maxDelta and if filter is not between 0 and 1.
     */
    public FaultFractalHeightMap(
            int size,
            int iterations,
            int minDelta,
            int maxDelta,
            float filter) {
        this( size, iterations, minDelta, maxDelta, filter, System.currentTimeMillis() );
    }

    /**
	 * Constructor sets the attributes of the fault fractal system and
	 * generates the heightmap.
	 *
	 * @param size the size of the terrain to be generated.
	 * @param iterations the number of faults to generate.
	 * @param minDelta the minimum varience in the height increase between
	 *      faults.
	 * @param maxDelta the maximum varience in the height increase between
	 *      faults.
	 * @param filter the filter used for erosion. Filter can be between 0 and
	 *      1, where 0 is no filtering and 1 is extreme filtering. Suggested
	 *      values are 0.2-0.4.
     * @param seed randomizer seed
	 *
	 * @throws JmeException if size is not greater than zero,
	 *      iterations is not greater than zero, minDelta is more than
	 *      maxDelta and if filter is not between 0 and 1.
	 */
	public FaultFractalHeightMap(
		int size,
		int iterations,
		int minDelta,
		int maxDelta,
		float filter,
        long seed ) {

		if (size <= 0
			|| iterations <= 0
			|| minDelta > maxDelta
			|| (filter < 0 || filter >= 1)) {
			throw new JmeException(
				"Either size is not greater than"
					+ " zero, iterations is not greater than zero, minDelta is more "
					+ "than maxDelta and/or filter is not between 0 and 1.");
		}

		this.size = size;
		this.iterations = iterations;
		this.minDelta = minDelta;
		this.maxDelta = maxDelta;
		this.filter = filter;
        this.randomizer = new Random( seed );

        load();
	}

	/**
	 * <code>load</code> generates the heightfield using the Fault Fractal
	 * algorithm. <code>load</code> uses the latest attributes, so a call
	 * to <code>load</code> is recommended if attributes have changed using
	 * the set methods.
	 */
	public boolean load() {
		//amount to raise a slice of terrain.
		float heightVarience;
		//random points for the fault line
		int randomX1, randomZ1;
		int randomX2, randomZ2;
		//line directions
		int directionX1, directionZ1;
		int directionX2, directionZ2;

		//clean up data if needed.
		if (null != heightData) {
			unloadHeightMap();
		}

		//allocate new arrays
		heightData = new int[size*size];
		float[][] tempBuffer = new float[size][size];

		//generate faults for the number of iterations given.
		for (int i = 0; i < iterations; i++) {
			heightVarience =
				maxDelta - ((maxDelta - minDelta) * i) / iterations;

			//find two different random points.
			randomX1 = (int) (random() * size);
			randomZ1 = (int) (random() * size);

			do {
				randomX2 = (int) (random() * size);
				randomZ2 = (int) (random() * size);
			} while (randomX1 == randomX2 && randomZ1 == randomZ2);

			//calculate the direction of the line the two points create.
			directionX1 = randomX2 - randomX1;
			directionZ1 = randomZ2 - randomZ1;

			for (int x = 0; x < size; x++) {
				for (int z = 0; z < size; z++) {
					//calculate the direction of the line from the first
					//point to where we currently are.
					directionX2 = x - randomX1;
					directionZ2 = z - randomZ1;

					//If the direction between the two directions is positive,
					//the current point is above the line and should be
					//increased by the height varient.
					if ((directionX2 * directionZ1 - directionX1 * directionZ2)
						> 0) {
						tempBuffer[x][z] += heightVarience;
					}
				}
			}
		}

		erodeTerrain(tempBuffer);
		normalizeTerrain(tempBuffer);

		//transfer the new terrain into the height map.
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				setHeightAtPoint((int) tempBuffer[i][j], j, i);
			}
		}

		logger.info("Created Heightmap using Fault Fractal");
		return true;
	}

    private Random randomizer;

    private double random() {
        return randomizer.nextDouble();
    }

    /**
     * <code>setIterations</code> sets the number of faults to generated during
     * the construction of the heightmap.
     * @param iterations the number of fault lines to generate.
     * @throws JmeException if the number of faults is less than or
     *      equal to zero.
     */
    public void setIterations(int iterations) {
        if (iterations <= 0) {
            throw new JmeException(
                "iterations must be greater than " + "zero");
        }
        this.iterations = iterations;
    }

	/**
	 * <code>setMinDelta</code> sets the minimum height value for the
	 * fault line varience.
	 * @param minDelta the minimum height value for the fault line varience.
	 * @throws JmeException if minDelta is greater than maxDelta.
	 */
	public void setMinDelta(int minDelta) {
		if (minDelta > maxDelta) {
			throw new JmeException(
				"minDelta must be greater than " + "the current maxDelta");
		}
		this.minDelta = minDelta;
	}

	/**
	 * <code>setMaxDelta</code> sets the maximum height value for the fault
	 * line varience.
	 * @param maxDelta the maximum height value for the fault line varience.
	 * @throws JmeException if maxDelta is less than minDelta.
	 */
	public void setMaxDelta(int maxDelta) {
		if (maxDelta < minDelta) {
			throw new JmeException(
				"maxDelta must be greater than " + "current minDelta");
		}
		this.maxDelta = maxDelta;
	}

}
