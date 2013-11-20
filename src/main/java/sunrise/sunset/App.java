package sunrise.sunset;

public class App {

	private static double[] A = new double[2];
	private static double[] D = new double[2];
	
	private static final double DR = Math.PI/180.0;
	private static final double K1 = 15.0 * DR * 1.0027379;
	
	private static double B5 = 39.185768; //Latitude of Manhattan, KS 
										  //(north latitudes positive)
	
	private static double L5 = -96.575556; //Longitude of Manhattan, KS 
										   //(west longitudes negative)

	
	private static double H = 6; // DST offset from UTC (West is positive)
	
	private static int YEAR = 2013;
	private static int MONTH = 11;
	private static int DAY = 19;
	
	
	private static double Z0;
	
	public static void main(String[] args) {
		L5 = L5/360;
		Z0 = H/24;
		
		calendarToJD();
		double T = (J-2451545)+F;
		double TT = T/36525+1; // centuries from 1900.0
		
		calculateLST(T); //calculates T0
		T = T + Z0;
		
		
		calculateSunPosition(T, TT);
		A[0] = A5;
		D[0] = D5;
		
		
		T = T+1;
		calculateSunPosition(T, TT);
		A[1] = A5;
		D[1] = D5;
		
		
		if (A[1] < A[0]) {
			A[1] = A[1]+2*Math.PI;
		}
		double Z1 = DR * 90.833; // Zenith dist.
		
		double S = Math.sin(B5*DR);
		double C = Math.cos(B5*DR);
		
		double Z = Math.cos(Z1);
		M8 = 0;
		W8 = 0;
		
		double A0 = A[0];
		double D0 = D[0];
		
		double DA = A[1] - A[0];
		double DD = D[1] - D[0];
		
		
		for(int C0=0; C0<=23; C0++) {
			double P = (C0+1) / 24.0;
			double A2 = A[0] + P*DA;
			double D2 = D[0] + P*DD;
					
			testHourForEvent(C0, A0, A2, D2, D0, C, Z, S);
			
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
			int C0, double A0, double A2,
			double D2, double D0, double C, 
			double Z, double S) {
		
		double L0 = T0 + C0*K1;
		double L2 = L0 + K1;

		double H0 = L0 - A0;
		double H2 = L2 - A2;
		
		double H1 = (H2+H0) / 2.0; //  Hour angle,
		double D1 = (D2+D0) / 2.0; //  declination at half hour
		
		if (C0 == 0) {
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

				double T3=C0 + E + 1/120; //Round off
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
	private static void calculateSunPosition(double T, double TT) {
		//   Fundamental arguments
		//     (Van Flandern &
		//     Pulkkinen, 1979)
		double L = .779072 + .00273790931*T;
		double G = .993126 + .0027377785*T;

		L = L - ((int) L);
		G = G - ((int) G);

		L = L*2*Math.PI;
		G = G*2*Math.PI;

		double V = .39785 * Math.sin(L);
		V = V - .01000 * Math.sin(L-G);
		V = V + .00333 * Math.sin(L+G);
		V = V - .00021 * TT * Math.sin(L);
		
		double U = 1 - .03349 * Math.cos(G);
		U = U - .00014 * Math.cos(2*L);
		U = U + .00008 * Math.cos(L);

		double W = -.00010 - .04129 * Math.sin(2*L);
		W = W + .03211 * Math.sin(G);
		W = W + .00104 * Math.sin(2*L-G);
		W = W - .00035 * Math.sin(2*L+G);
		W = W - .00008 * TT * Math.sin(G);
		
		
		//    Compute Sun's RA and Dec		
		double S = W / Math.sqrt(U - V*V);
		A5 = L + Math.atan(S / Math.sqrt(1 - S*S));
		
		S = V / Math.sqrt(U);
		D5 = Math.atan(S / Math.sqrt(1 - S*S));
		
		//System.err.println("calculateSunPosition: ("+A5+","+D5+")");
	}
	
	/**
	 * calculate LST at 0h zone time
	 */
	private static double T0;
	private static void calculateLST(double T) {		
		T0 = T/36525.0;

		double S;
		S = 24110.5 + 8640184.813*T0;
		S = S + 86636.6*Z0 + 86400*L5;
		
		S = S/86400.0;
		S = S - ((int) S);
		
		T0 = S * 360.0 * DR;
		//System.err.println("T0 at end of calculateLST: "+T0);
	}
	
	/**
	 * Compute Julian Date
	 */
	private static int J;
	private static double F;
	private static void calendarToJD() {
		int Y = YEAR;
		int M = MONTH;
		double D = DAY;
		
		int G=1;
		if (Y<1583) {
			G=0;
		}
		
		int D1 = (int) D;
		F = D - ((double) D1) - .5;
		
		J = -1 * (int) ( 7 * (((M+9)/12)+Y) / 4);
		
		
		int J3 = 0;
		if(G!=0) {
			int S = sgn(M-9);
			int A = Math.abs(M-9);
			
			J3 = Y + S * (A/7);
			J3 = -1 * ( (J3/100) +1) * 3/4;
		}
		
		J = J + (275*M/9) + D1 + G*J3;
		J = J + 1721027 + 2*G + 367*Y;
		
		if (F<0) {
			F = F+1;
			J = J-1;
		}
		
		//System.out.println("Julian date: "+J);
	}

}
