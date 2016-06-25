package com.mizzick.speedtop;

/**
 * Unit converter
 */
public class UnitConverter {
	private final static float METERS_TO_FEET_COEFF = 3.28083989501312f;
	private final static double METERS_TO_FEET_COEFF_DOUBLE = 3.28083989501312d;
	private final static float METERS_IN_SEC_TO_MILES_IN_HOUR_COEFF = 2.2369362920544f;
	private final static float METERS_IN_SEC_TO_KM_IN_HOUR_COEFF = 3.6f;
	private final static float KM_IN_HOUR_TO_MILES_IN_HOUR_COEFF = 0.621371192f;

	public static float convertMetersToFeet(float value) {
		return value * METERS_TO_FEET_COEFF;
	}

	public static double convertMetersToFeet(double value) {
		return value * METERS_TO_FEET_COEFF_DOUBLE;
	}

	public static float convertMetersInSecToMilesInHour(float value) {
		return value * METERS_IN_SEC_TO_MILES_IN_HOUR_COEFF;
	}

	public static float convertMetersInSecondToKmInHour(float value) {
		return value * METERS_IN_SEC_TO_KM_IN_HOUR_COEFF;
	}

	public static float convertKmInHourToMilesInHour(float value) {
		return value * KM_IN_HOUR_TO_MILES_IN_HOUR_COEFF;
	}
}
