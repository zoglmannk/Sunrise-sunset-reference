package sunrise.sunset;


public class Calculator {

	private static final int NEW_STANDARD_EPOC = 2451545; // January 1, 2000 at noon
	private static final int NUM_DAYS_IN_CENTURY = 36525; // 365 days * 100 years + 25 extra days for leap years
	private static final int HOURS_IN_DAY = 24;
	
	private static final double DR = Math.PI/180.0; //degrees to radians constant
	private static final double K1 = 15.0 * DR * 1.0027379;
	
	
	// Note that the definition of civil, nautical, and astronomical twilight is defined 
	// respectively as 6, 12, and 18 degrees below the horizon. I'm choosing a slightly 
	// different astronomical offset to better match published times over a wide range 
	// of dates. Negative values mean below the horizon. Positive are above the horizon.
	private static final double SUNRISE_SUNET_OFFSET = 0;
	private static final double CIVIL_TWILIGHT_OFFSET = -6;
	private static final double NAUTICAL_TWILIGHT_OFFSET = -12;
	private static final double ASTRONOMICAL_TWILIGHT_OFFSET= -17.8;
	private static final double GOLDEN_HOUR_OFFSET= 10.0;
	
	
	/**
	 * @param gps         location of user
	 * @param utcToLocal  offset from UTC (West is negative)
	 * @param date        date of interest for calculation
	 */
	public Result calculate (
			GpsCoordinate gps, 
			int utcToLocal, 
			SimpleDate date) {
		
		
		double timeZoneShift = -1  * ((double)utcToLocal)/HOURS_IN_DAY;
		
		int julianDate = calculateJulianDate(date);
		double daysFromEpoc = (julianDate - NEW_STANDARD_EPOC) + 0.5;

		
		double LST = calculateLST(daysFromEpoc, timeZoneShift, gps.longitude);
		daysFromEpoc = daysFromEpoc + timeZoneShift;
		
		
		Position today    = calculateSunPosition(daysFromEpoc);		
		Position tomorrow = calculateSunPosition(daysFromEpoc+1);
		
		
		if (tomorrow.rightAscention < today.rightAscention) {
			double ascention = tomorrow.rightAscention+2*Math.PI;
			tomorrow = new Position(ascention, tomorrow.declination);
		}
		

		Result sunriseSunset        = calculate(SUNRISE_SUNET_OFFSET, gps, LST, today, tomorrow);
		Result goldenHour           = calculate(GOLDEN_HOUR_OFFSET, gps, LST, today, tomorrow);
		Result civilTwilight        = calculate(CIVIL_TWILIGHT_OFFSET, gps, LST, today, tomorrow);
		Result nauticalTwilight     = calculate(NAUTICAL_TWILIGHT_OFFSET, gps, LST, today, tomorrow);
		Result astronomicalTwilight = calculate(ASTRONOMICAL_TWILIGHT_OFFSET, gps, LST, today, tomorrow);
		
		Result combined = sunriseSunset;
		combined.goldenHourBegin = goldenHour.sunRise;
		combined.goldenHourEnd   = goldenHour.sunSet;
		combined.civilTwilightBegin = civilTwilight.sunRise;
		combined.civilTwilightEnd  = civilTwilight.sunSet;
		combined.nauticalTwilightBegin = nauticalTwilight.sunRise;
		combined.nauticalTwilightEnd   = nauticalTwilight.sunSet;
		combined.astronomicalTwilightBegin = astronomicalTwilight.sunRise;
		combined.astronomicalTwilightEnd   = astronomicalTwilight.sunSet;
		
		return combined; 
	}

	private Result calculate(
			double horizonOffset, //in degrees
			GpsCoordinate gps, 
			double LST, 
			Position today,
			Position tomorrow) {
		
		double previousAscention = today.rightAscention;
		double previousDeclination = today.declination;
		
		double changeInAscention   = tomorrow.rightAscention - today.rightAscention;
		double changeInDeclination = tomorrow.declination    - today.declination;
		
		double previousV = 0; //arbitrary initial value
		
		Result result = new Result();
		TestResult testResult = null;
		
		
		for(int hourOfDay=0; hourOfDay<HOURS_IN_DAY; hourOfDay++) {
			
			double fractionOfDay = (hourOfDay+1) / ((double)HOURS_IN_DAY);
			double asention    = today.rightAscention + fractionOfDay*changeInAscention;
			double declination = today.declination    + fractionOfDay*changeInDeclination;
					
			testResult =  testHourForEvent(hourOfDay, horizonOffset,
										   previousAscention,   asention, 
										   previousDeclination, declination,
										   previousV, gps, LST);
			
			previousAscention   = asention;
			previousDeclination = declination;
			previousV           = testResult.V;
			
			if(testResult.sunRise != null) {
				result.sunRise    = testResult.sunRise;
				result.riseAzmith = testResult.riseAzmith;
			}
			
			if(testResult.sunSet != null) {
				result.sunSet    = testResult.sunSet;
				result.setAzmith = testResult.setAzmith;
			}
			
		}
		
		
		result.typeOfDay = findTypeOfDay(result, testResult.V);
		setSolarNoon(result);
		return result;
	}
	
	private void setSolarNoon(Result result) {
		switch(result.typeOfDay) {
		case NORMAL_DAY:
			Time lengthOfDay = result.getLengthOfDay();
			int totalMins = (lengthOfDay.hour*60 + lengthOfDay.min)/2;
			
			int hour = result.sunRise.hour + (totalMins/60);
			int min  = result.sunRise.min  + (totalMins%60);
			
			if(min > 60) {
				hour++;
				min = min - 60;
			}
			result.solarNoon = new Time(hour, min);
			break;
		default:
			result.solarNoon = null;
		}
	}
	
	
	private Result.TypeOfDay findTypeOfDay(Result result, double lastV) {

		if(result.sunRise==null && result.sunSet==null) {
			if (lastV < 0) {
				return Result.TypeOfDay.SUN_DOWN_ALL_DAY;
			} else {
				return Result.TypeOfDay.SUN_UP_ALL_DAY;
			}
			
		} else if(result.sunRise==null) {
			return Result.TypeOfDay.NO_SUNRISE;
			
		} else if(result.sunSet==null) {
			return Result.TypeOfDay.NO_SUNSET;
			
		} else {
			return Result.TypeOfDay.NORMAL_DAY;
		}

	}
	
	
	private static class TestResult {
		Time sunRise, sunSet;
		double riseAzmith, setAzmith;
		double V;
	}
	
	
	/**
	 * Test an hour for an event
	 */	
	private TestResult testHourForEvent(
			int hourOfDay, double degreeOffset,
			double previousAscention, double ascention,
			double previousDeclination, double declination, 
			double previousV, 
			GpsCoordinate gps, double LST) {

		
		TestResult ret = new TestResult();
		
		//90.833 is for atmospheric refraction when sun is at the horizon.
		//ie the sun slips below the horizon at sunset before you actually see it go below the horizon
		double zenithDistance = DR * (degreeOffset == 0 ? 90.833 : 90.0); 
		
		double S = Math.sin(gps.latitude*DR);
		double C = Math.cos(gps.latitude*DR);
		double Z = Math.cos(zenithDistance) + degreeOffset*DR;
		
		double L0 = LST + hourOfDay*K1;
		double L2 = L0 + K1;

		double H0 = L0 - previousAscention;
		double H2 = L2 - ascention;
		
		double H1 = (H2+H0) / 2.0; //  Hour angle,
		double D1 = (declination+previousDeclination) / 2.0; //  declination at half hour
		
		if (hourOfDay == 0) {
			previousV = S * Math.sin(previousDeclination) + C*Math.cos(previousDeclination)*Math.cos(H0)-Z;
		}

		double V = S*Math.sin(declination) + C*Math.cos(declination)*Math.cos(H2) - Z;
		
		if(sunCrossedHorizon(previousV, V)) {
			double V1 = S*Math.sin(D1) + C*Math.cos(D1)*Math.cos(H1) - Z;
			
			double A = 2*V - 4*V1 + 2*previousV;
			double B = 4*V1 - 3*previousV - V;
			double D = B*B - 4*A*previousV;

			if (D >= 0) {
				D = Math.sqrt(D);
				
				double E = (-B+D) / (2*A);
				if (E>1 || E<0) {
					E = (-B-D) / (2*A);
				}
				
				double H7 = H0 + E*(H2-H0);
				double N7 = -1 * Math.cos(D1)*Math.sin(H7);
				double D7 = C*Math.sin(D1) - S*Math.cos(D1)*Math.cos(H7);
				double azmith = Math.atan(N7/D7)/DR;
				
				if(D7 < 0) {
					azmith = azmith+180;
				}

				if(azmith < 0) {
					azmith = azmith+360;
				}

				if(azmith > 360) {
					azmith = azmith-360;
				}
				

				double T3=hourOfDay + E + 1/120; //Round off
				int hour = (int) T3;
				int min = (int) ((T3-hour)*60);
				
				
				if (previousV<0 && V>0) {
					ret.sunRise = new Time(hour, min);
					ret.riseAzmith = azmith;
				}

				if (previousV>0 && V<0) {
					ret.sunSet = new Time(hour, min);
					ret.setAzmith = azmith;
				}				
							
			}

		}
		
		ret.V = V;
		return ret;
		
	}


	private boolean sunCrossedHorizon(double previousV, double V) {
		return sgn(previousV) != sgn(V);
	}
	
	private int sgn(double val) {
		return val == 0 ? 0 : (val > 0 ? 1 : 0);
	}
	
	private static class Position {
		final double rightAscention,
		             declination;
		
		
		public Position(double rightAscention, double declination) {
			this.rightAscention = rightAscention;
			this.declination = declination;
		}
		
	}
	
	/**
	 * drops any full revolutions and then converts revolutions to radians 
	 */
	private double revolutionsToRadians(double revolutions) {
		return 2*Math.PI*(revolutions - ((int) revolutions));
	}
	
	private Position calculateSunPosition(double daysFromEpoc) {
		double numCenturiesSince1900 = daysFromEpoc/NUM_DAYS_IN_CENTURY + 1;
		
		//   Fundamental arguments 
		//   (Van Flandern & Pulkkinen, 1979)
		double meanLongitudinalOfSun = revolutionsToRadians(.779072 + .00273790931*daysFromEpoc);
		double meanAnomalyOfSun      = revolutionsToRadians(.993126 + .00273777850*daysFromEpoc);
		
		double meanLongitudinalOfMoon        = revolutionsToRadians(.606434 + .03660110129*daysFromEpoc);
		double longitudeOfLunarAscendingNode = revolutionsToRadians(.347343 - .00014709391*daysFromEpoc);
		double meanAnomalyOfVenus            = revolutionsToRadians(.140023 + .00445036173*daysFromEpoc);
		double meanAnomalyOfMars             = revolutionsToRadians(.053856 + .00145561327*daysFromEpoc);
		double meanAnomalyOfJupiter          = revolutionsToRadians(.056531 + .00023080893*daysFromEpoc);


		double V;
		V =     .39785 * Math.sin(meanLongitudinalOfSun);
		V = V - .01000 * Math.sin(meanLongitudinalOfSun-meanAnomalyOfSun);
		V = V + .00333 * Math.sin(meanLongitudinalOfSun+meanAnomalyOfSun);
		V = V - .00021 * numCenturiesSince1900 * Math.sin(meanLongitudinalOfSun);
		V = V + .00004 * Math.sin(meanLongitudinalOfSun+2*meanAnomalyOfSun);
		V = V - .00004 * Math.cos(meanLongitudinalOfSun);
		V = V - .00004 * Math.sin(longitudeOfLunarAscendingNode-meanLongitudinalOfSun);
		V = V + .00003 * numCenturiesSince1900 * Math.sin(meanLongitudinalOfSun-meanAnomalyOfSun);
		
		double U;
		U = 1 - .03349 * Math.cos(meanAnomalyOfSun);
		U = U - .00014 * Math.cos(2*meanLongitudinalOfSun);
		U = U + .00008 * Math.cos(meanLongitudinalOfSun);
		U = U - .00003 * Math.sin(meanAnomalyOfSun-meanAnomalyOfJupiter);

		double W;
		W =    -.04129 * Math.sin(2*meanLongitudinalOfSun);
		W = W + .03211 * Math.sin(meanAnomalyOfSun);
		W = W + .00104 * Math.sin(2*meanLongitudinalOfSun-meanAnomalyOfSun);
		W = W - .00035 * Math.sin(2*meanLongitudinalOfSun+meanAnomalyOfSun);
		W = W - .00010;
		W = W - .00008 * numCenturiesSince1900 * Math.sin(meanAnomalyOfSun);
		W = W - .00008 * Math.sin(longitudeOfLunarAscendingNode);
		W = W + .00007 * Math.sin(2*meanAnomalyOfSun);
		W = W + .00005 * numCenturiesSince1900 * Math.sin(2*meanLongitudinalOfSun);
		W = W + .00003 * Math.sin(meanLongitudinalOfMoon-meanLongitudinalOfSun);
		W = W - .00002 * Math.cos(meanAnomalyOfSun-meanAnomalyOfJupiter);
		W = W + .00002 * Math.sin(4*meanAnomalyOfSun-8*meanAnomalyOfMars+3*meanAnomalyOfJupiter);
		W = W - .00002 * Math.sin(meanAnomalyOfSun-meanAnomalyOfVenus);
		W = W - .00002 * Math.cos(2*meanAnomalyOfSun-2*meanAnomalyOfVenus);
		
		
		//    Compute Sun's RA and Dec		
		double S = W / Math.sqrt(U - V*V);
		double rightAscention = meanLongitudinalOfSun + Math.atan(S / Math.sqrt(1 - S*S));
		
		S = V / Math.sqrt(U);
		double declination = Math.atan(S / Math.sqrt(1 - S*S));
		
		//System.err.println("calculateSunPosition: ("+rightAscention+","+declination+")");
		return new Position(rightAscention, declination);
	}
	
	
	/**
	 * calculate LST at 0h zone time
	 */
	private double calculateLST(double daysFromEpoc, double timeZoneShift, double longitude) {
		double L = longitude/360;
		double ret = daysFromEpoc/36525.0;

		double S;
		S = 24110.5 + 8640184.813*ret;
		S = S + 86636.6*timeZoneShift + 86400*L;
		
		S = S/86400.0;
		S = S - ((int) S);
		
		ret = S * 360.0 * DR;
		
		//System.err.println("calculateLST: "+ret);
		return ret;
	}
	
	/**
	 * Compute truncated Julian Date.
	 * 
	 * @result add +0.5 for non-truncated Julian Date
	 */
	private int calculateJulianDate(SimpleDate date) {		
		int julianDate = -1 * (int) ( 7 * (((date.month+9)/12)+date.year) / 4);
		
		
		int offset = 0;
		boolean after1583 = date.year >= 1583;
		if(after1583) {
			int S = sgn(date.month-9);
			int A = Math.abs(date.month-9);
			
			offset = date.year + S * (A/7);
			offset = -1 * ( (offset/100) +1) * 3/4;
		}
		
		
		julianDate = julianDate + (275*date.month/9) + date.day + offset;
		julianDate = julianDate + 1721027 + (after1583 ? 2 : 0) + 367*date.year;

		julianDate--; //truncate
		
		
		//System.out.println("Julian date: "+julianDate);
		return julianDate;
	}
	
}
