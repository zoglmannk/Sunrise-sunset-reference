package sunrise.sunset;

public class App {

	private static double[] A = new double[2];
	private static double[] D = new double[2];
	private static double Z0;
	
	private static final int NEW_STANDARD_EPOC = 2451545; // January 1, 2000 at noon
	private static final int NUM_DAYS_IN_CENTURY = 36525; // 365 days * 100 years + 25 extra days for leap years
	
	private static final double DR = Math.PI/180.0; //degrees to radians constant
	private static final double K1 = 15.0 * DR * 1.0027379;
	
	private static final double LATITUDE  = 39.185768; //Latitude of Manhattan, KS 
													   //(north latitudes positive)
	
	private static final double LONGITUDE = -96.575556; //Longitude of Manhattan, KS 
														//(west longitudes negative)

	
	private static final double H = 6; // DST offset from UTC (West is positive)
	
	private static final int MONTH = 11;
	private static final int DAY = 19;
	private static final int YEAR = 2013;
	
	
	public static void main(String[] args) {
		Z0 = H/24;
		
		int julianDate = calendarToJD(MONTH, DAY, YEAR);
		double daysFromEpoc = (julianDate - NEW_STANDARD_EPOC) + F;

		
		double LST = calculateLST(daysFromEpoc, LONGITUDE);
		daysFromEpoc = daysFromEpoc + Z0;
		
		
		calculateSunPosition(daysFromEpoc);
		A[0] = A5;
		D[0] = D5;
		
		
		calculateSunPosition(daysFromEpoc+1);
		A[1] = A5;
		D[1] = D5;
		
		
		if (A[1] < A[0]) {
			A[1] = A[1]+2*Math.PI;
		}
		double zenithDistance = DR * 90.833;
		
		double S = Math.sin(LATITUDE*DR);
		double C = Math.cos(LATITUDE*DR);
		
		double Z = Math.cos(zenithDistance);
		M8 = 0;
		W8 = 0;
		
		double A0 = A[0];
		double D0 = D[0];
		
		double DA = A[1] - A[0];
		double DD = D[1] - D[0];
		
		
		for(int hourOfDay=0; hourOfDay<=23; hourOfDay++) {
			double P = (hourOfDay+1) / 24.0;
			double A2 = A[0] + P*DA;
			double D2 = D[0] + P*DD;
					
			testHourForEvent(hourOfDay, A0, A2, D2, D0, C, Z, S, LST);
			
			A0=A2;
			D0=D2;
			V0=V2;
		}

		maybePrintSpecialMessage();
	}
	
	
	/**
	 * Special-message routine
	 */
	private static void maybePrintSpecialMessage() {
		if(M8==0 && W8==0) {
			if (V2 < 0) {
				System.out.println("Sun down all day");
			}
			
			if (V2 > 0) {
				System.out.println("Sun up all day");
			}
			
		} else {
			if(M8==0) {
				System.out.println("No sunrise this date");
			}
		
			if(W8==0) {
				System.out.println("No sunset this date");
			}
		}
	}
	
	
	/**
	 * Test an hour for an event
	 */
	private static double V0, V2;
	private static int M8, W8;
	private static void testHourForEvent(
			int hourOfDay, double A0, double A2,
			double D2, double D0, double C, 
			double Z, double S, double LST) {
		
		double L0 = LST + hourOfDay*K1;
		double L2 = L0 + K1;

		double H0 = L0 - A0;
		double H2 = L2 - A2;
		
		double H1 = (H2+H0) / 2.0; //  Hour angle,
		double D1 = (D2+D0) / 2.0; //  declination at half hour
		
		if (hourOfDay == 0) {
			V0 = S * Math.sin(D0) + C*Math.cos(D0)*Math.cos(H0)-Z;
		}

		V2 = S*Math.sin(D2) + C*Math.cos(D2)*Math.cos(H2) - Z;
		
		if(sgn(V0) != sgn(V2)) {
			double V1 = S*Math.sin(D1) + C*Math.cos(D1)*Math.cos(H1) - Z;
			
			double A = 2*V2 - 4*V1 + 2*V0;
			double B = 4*V1 - 3*V0 - V2;
			
			double D = B*B - 4*A*V0;
			if (D >= 0) {
				D = Math.sqrt(D);
				
				if (V0<0 && V2>0) {
					System.out.print("Sunrise at ");
					M8 = 1;
					
				}

				if (V0>0 && V2<0) {
					System.out.print("Sunset at ");
					W8 = 1;
				}

				double E = (-B+D) / (2*A);
				if (E>1 || E<0) {
					E = (-B-D) / (2*A);
				}

				double T3=hourOfDay + E + 1/120; //Round off
				int H3 = (int) T3;
				int M3 = (int) ((T3-H3)*60);
				System.out.format("%d:%d", H3, M3);
				
				
				double H7 = H0 + E*(H2-H0);
				double N7 = -1 * Math.cos(D1)*Math.sin(H7);
				double D7 = C*Math.sin(D1) - S*Math.cos(D1)*Math.cos(H7);
				double AZ = Math.atan(N7/D7)/DR;
				
				if(D7 < 0) {
					AZ = AZ+180;
				}

				if(AZ < 0) {
					AZ = AZ+360;
				}

				if(AZ > 360) {
					AZ = AZ-360;
				}

				System.out.format(", azimuth %(.1f \n", AZ);				
			}

		}
		
	}
	
	private static int sgn(double val) {
		return val == 0 ? 0 : (val > 0 ? 1 : 0);
	}
	
	
	private static double A5, D5;
	private static void calculateSunPosition(double daysFromEpoc) {
		
		double numCenturiesSince1900 = daysFromEpoc/NUM_DAYS_IN_CENTURY + 1;
		
		//   Fundamental arguments 
		//   (Van Flandern & Pulkkinen, 1979)
		double meanLongitudinal = .779072 + .00273790931*daysFromEpoc;
		double meanAnomaly      = .993126 + .00273777850*daysFromEpoc;

		meanLongitudinal = meanLongitudinal - ((int) meanLongitudinal);
		meanAnomaly      = meanAnomaly      - ((int) meanAnomaly);

		meanLongitudinal = meanLongitudinal * 2*Math.PI;
		meanAnomaly      = meanAnomaly      * 2*Math.PI;

		double V;
		V = .39785 * Math.sin(meanLongitudinal);
		V = V - .01000 * Math.sin(meanLongitudinal-meanAnomaly);
		V = V + .00333 * Math.sin(meanLongitudinal+meanAnomaly);
		V = V - .00021 * numCenturiesSince1900 * Math.sin(meanLongitudinal);
		
		double U;
		U = 1 - .03349 * Math.cos(meanAnomaly);
		U = U - .00014 * Math.cos(2*meanLongitudinal);
		U = U + .00008 * Math.cos(meanLongitudinal);

		double W;
		W = -.00010 - .04129 * Math.sin(2*meanLongitudinal);
		W = W + .03211 * Math.sin(meanAnomaly);
		W = W + .00104 * Math.sin(2*meanLongitudinal-meanAnomaly);
		W = W - .00035 * Math.sin(2*meanLongitudinal+meanAnomaly);
		W = W - .00008 * numCenturiesSince1900 * Math.sin(meanAnomaly);
		
		
		//    Compute Sun's RA and Dec		
		double S = W / Math.sqrt(U - V*V);
		A5 = meanLongitudinal + Math.atan(S / Math.sqrt(1 - S*S));
		
		S = V / Math.sqrt(U);
		D5 = Math.atan(S / Math.sqrt(1 - S*S));
		
		//System.err.println("calculateSunPosition: ("+A5+","+D5+")");
	}
	
	/**
	 * calculate LST at 0h zone time
	 */
	private static double calculateLST(double daysFromEpoc, double longitude) {
		double L = longitude/360;
		double ret = daysFromEpoc/36525.0;

		double S;
		S = 24110.5 + 8640184.813*ret;
		S = S + 86636.6*Z0 + 86400*L;
		
		S = S/86400.0;
		S = S - ((int) S);
		
		ret = S * 360.0 * DR;
		//System.err.println("T0 at end of calculateLST: "+T0);
		return ret;
	}
	
	/**
	 * Compute Julian Date
	 */
	private static double F;
	private static int calendarToJD(int month, int day, int year) {
		double D = day;
		
		int G=1;
		if (year<1583) {
			G=0;
		}
		
		int D1 = (int) D;
		F = D - ((double) D1) - .5;
		
		int julianDate = -1 * (int) ( 7 * (((month+9)/12)+year) / 4);
		
		
		int J3 = 0;
		if(G!=0) {
			int S = sgn(month-9);
			int A = Math.abs(month-9);
			
			J3 = year + S * (A/7);
			J3 = -1 * ( (J3/100) +1) * 3/4;
		}
		
		julianDate = julianDate + (275*month/9) + D1 + G*J3;
		julianDate = julianDate + 1721027 + 2*G + 367*year;
		
		if (F<0) {
			F = F+1;
			julianDate = julianDate-1;
		}
		
		//System.out.println("Julian date: "+julianDate);
		return julianDate;
	}

}
