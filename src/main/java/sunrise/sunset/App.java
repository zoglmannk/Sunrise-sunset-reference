package sunrise.sunset;


public class App {
	
	private static final double LATITUDE  = 39.185768; //Latitude of Manhattan, KS 
													   //(north latitudes positive)

	private static final double LONGITUDE = -96.575556; //Longitude of Manhattan, KS 
														//(west longitudes negative)

	private static final int    UTC_TO_DST = -5; // DST offset from UTC (West is negative)
	
	

	public static void main(String[] args) { 
		GpsCoordinate gps = new GpsCoordinate(LATITUDE, LONGITUDE);
		
		int month=11, day=1, year=2013;
		
		SimpleDate date = new SimpleDate(month, day, year);
		System.out.printf("**Calculations for %d/%d/%d****\n", month,day,year);
		
		Calculator calculator = new Calculator();
		Result result = calculator.calculate(gps, UTC_TO_DST, date);

		System.out.println(result.toString());
	}
	

}
