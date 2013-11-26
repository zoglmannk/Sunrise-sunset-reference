package sunrise.sunset;

import java.io.PrintWriter;
import java.io.StringWriter;


public class Result {
	
	public Time sunRise, sunSet, solarNoon;
	public double riseAzmith, setAzmith;
	public TypeOfDay typeOfDay;
	
	public Time astronomicalTwilightBegin, astronomicalTwilightEnd;
	public Time nauticalTwilightBegin, nauticalTwilightEnd;
	public Time civilTwilightBegin, civilTwilightEnd;
	
	public Time goldenHourBegin, goldenHourEnd;
	
	public Time moonRiseToday, moonSetToday;
	public Time moonRiseTomorrow, moonSetTomorrow;
	
	
	public enum TypeOfDay {
		NORMAL_DAY,
		SUN_UP_ALL_DAY,
		SUN_DOWN_ALL_DAY,
		NO_SUNRISE,
		NO_SUNSET
	}
	
	public Time getLengthOfDay() {		
		
		switch(typeOfDay) {
		case SUN_UP_ALL_DAY:
			return new Time(24,0);
		case SUN_DOWN_ALL_DAY:
			return new Time(0,0);
		case NO_SUNRISE:
			return sunSet;
		case NO_SUNSET:
			Time midnight = new Time(23,59);
			return difference(midnight, sunRise);
		default:
			return difference(sunSet, sunRise);
		}
		
	}
	
	private Time difference(Time t1, Time t2) {
		int hour = t1.hour - t2.hour;
		int min = t1.min - t2.min;
		
		if(min < 0) {
			hour--;
			min+=60;
		}
		
		return new Time(hour,min);
	}
	
	
	public String toString() {
		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);
		
		if(sunRise != null) {
			writer.printf("Sunrise (%s, azmimuth %(.1f)", sunRise, riseAzmith);
		}
		
		if(sunSet != null) {
			if(sunRise != null) {
				writer.print(", ");
			}
			writer.printf("Sunset (%s, azmimuth %(.1f)", sunSet, setAzmith);
		}
		
		
		// potential special-message
		switch(typeOfDay) {
		case SUN_UP_ALL_DAY:
			writer.println("Sun up all day");
			break;
		case SUN_DOWN_ALL_DAY:
			writer.println("Sun down all day");
			break;
		case NO_SUNRISE:
			writer.println("No sunrise this date");
			break;
		case NO_SUNSET:
			writer.println("No sunset this date");
			break;
		default:
			//nothing
		}
		
		writer.printf(", Day Length: %s", getLengthOfDay());
		
		if(solarNoon==null) {
			writer.println("");
		} else {
			writer.println(", Solar Noon: "+solarNoon);
		}

		writer.printf("Golden Hour          : (sunrise to %s, %s to sunset)\n", 
				      (goldenHourBegin == null ? "null" : goldenHourBegin),
				      (goldenHourEnd == null ? "null" : goldenHourEnd));
		
		writer.printf("Civil Twilight       : (%s to sunrise, sunset to %s)\n", 
					  (civilTwilightBegin == null ? "null" : civilTwilightBegin),
					  (civilTwilightEnd == null ? "null" : civilTwilightEnd));
		
		writer.printf("Nautical Twilight    : (%s to sunrise, sunset to %s)\n", 
					  (nauticalTwilightBegin == null ? "null" : nauticalTwilightBegin),
					  (nauticalTwilightEnd == null ? "null" : nauticalTwilightEnd));
		
		writer.printf("Astronomical Twilight: (%s to sunrise, sunset to %s)\n", 
					  (astronomicalTwilightBegin == null ? "null" : astronomicalTwilightBegin),
					  (astronomicalTwilightEnd == null ? "null" : astronomicalTwilightEnd));

		writer.printf("Today   : Moonrise: %s   Moonset: %s\n",moonRiseToday, moonSetToday);
		writer.printf("Tomarrow: Moonrise: %s   Moonset: %s\n",moonRiseTomorrow, moonSetTomorrow);
		
		writer.flush();
		return sw.getBuffer().toString();
	}
	
}
