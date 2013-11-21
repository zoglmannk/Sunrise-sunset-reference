package sunrise.sunset;

import java.io.PrintWriter;
import java.io.StringWriter;


public class Result {
	
	public Time sunRise, sunSet;
	public double riseAzmith, setAzmith;
	public TypeOfDay typeOfDay;
	
	
	public enum TypeOfDay {
		NORMAL_DAY,
		SUN_UP_ALL_DAY,
		SUN_DOWN_ALL_DAY,
		NO_SUNRISE,
		NO_SUNSET
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
			writer.printf("Sunset (%s, azmimuth %(.1f)\n", sunSet, setAzmith);
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
		
		writer.flush();
		return sw.getBuffer().toString();
	}
	
}
