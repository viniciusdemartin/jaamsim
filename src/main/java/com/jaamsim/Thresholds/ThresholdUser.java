/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2014 Ausenco Engineering Canada Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package com.jaamsim.Thresholds;

import java.util.ArrayList;

public interface ThresholdUser {

	/**
	 * Returns the Thresholds used by this object.
	 * @return the Threshold list.
	 */
	public abstract ArrayList<Threshold> getThresholds();

	/**
	 * Called whenever one of the Thresholds used by this object has changed
	 * its state from either open to closed or from closed to open.
	 */
	public abstract void thresholdChanged();
}
