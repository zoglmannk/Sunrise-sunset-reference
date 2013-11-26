package sunrise.sunset;

import java.io.PrintWriter;
import java.io.StringWriter;


public class Result {
	
	public Event sun;
	public Event astronomicalTwilight, nauticalTwilight, civilTwilight;
	public Event goldenHour;
	
	public Event moonToday, moonTomorrow;
	

	public static class Event {
		public Time rise, set;
		public double riseAzimuth, setAzimuth;
		public HorizonToHorizonCrossing type;
		
		public Time meridianCrossing, antimeridianCrossing;
		public Time risenAmount, setAmount;
	}
	
	public enum HorizonToHorizonCrossing {
		RISEN_AND_SET,
		NO_CHANGE_PREVIOUSLY_RISEN,
		NO_CHANGE_PREVIOUSLY_SET,
		ONLY_SET,
		ONLY_RISEN
	}
		
	
	public String toString() {
		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);
		
		if(sun.rise != null) {
			writer.printf("Sunrise %s", formatTimeAndAzimuth(sun.rise, sun.riseAzimuth));
		}
		
		if(sun.set != null) {
			if(sun.rise != null) {
				writer.print(", ");
			}
			writer.printf("Sunset %s\n", formatTimeAndAzimuth(sun.set, sun.setAzimuth));
		}
		
		
		// potential special-message
		switch(sun.type) {
		case NO_CHANGE_PREVIOUSLY_RISEN:
			writer.println("Sun up all day");
			break;
		case NO_CHANGE_PREVIOUSLY_SET:
			writer.println("Sun down all day");
			break;
		case ONLY_SET:
			writer.println("No sunrise this date");
			break;
		case ONLY_RISEN:
			writer.println("No sunset this date");
			break;
		default:
			//nothing
		}
		
		writer.printf("Day Length  : %s", sun.risenAmount);
		if(sun.meridianCrossing==null) {
			writer.println("");
		} else {
			writer.println(", Solar Noon    : "+sun.meridianCrossing);
		}
		
		writer.printf("Night Length: %s", sun.setAmount);
		if(sun.antimeridianCrossing==null) {
			writer.println("");
		} else {
			writer.println(", Solar Midnight: "+sun.antimeridianCrossing);
		}

		writer.printf("Golden Hour          : (sunrise to %s, %s to sunset)\n", 
				      replaceNull(goldenHour.rise),
				      replaceNull(goldenHour.set));

		
		writer.printf("Civil Twilight       : (%s to sunrise, sunset to %s)",
					  replaceNull(civilTwilight.rise), 
					  replaceNull(civilTwilight.set));
		writer.printf(", Civil Night Length       : %s\n",
					  replaceNull(civilTwilight.setAmount));
		
		writer.printf("Nautical Twilight    : (%s to sunrise, sunset to %s)",
					  replaceNull(nauticalTwilight.rise),
					  replaceNull(nauticalTwilight.set));
		writer.printf(", Nautical Night Length    : %s\n",
					  replaceNull(nauticalTwilight.setAmount));
		
		writer.printf("Astronomical Twilight: (%s to sunrise, sunset to %s)",
					  replaceNull(astronomicalTwilight.rise),
					  replaceNull(astronomicalTwilight.set));
		writer.printf(", Astronomical Night Length: %s\n", 
					  replaceNull(astronomicalTwilight.setAmount));

		
		writer.printf("Today's Moonrise: %s   Moonset: %s\n",
					  formatTimeAndAzimuth(moonToday.rise, moonToday.riseAzimuth),
					  formatTimeAndAzimuth(moonToday.set , moonToday.setAzimuth));
		writer.printf("Tomorrow's Moonrise: %s   Moonset: %s\n",
					  formatTimeAndAzimuth(moonTomorrow.rise, moonTomorrow.riseAzimuth),
					  formatTimeAndAzimuth(moonTomorrow.set,  moonTomorrow.setAzimuth));
		
		writer.flush();
		return sw.getBuffer().toString();
	}
	
	private String formatTimeAndAzimuth(Time t, double azimuth) {
		if(t == null) {
			return "--None--";
		}
		
		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);
		
		writer.printf("(%s, azimuth %(.1f)",
				  replaceNull(t), azimuth);
		
		writer.flush();
		return sw.getBuffer().toString();
		
	}
	
	private String replaceNull(Time s) {
		return s == null ? "--" : s.toString();
	}
	
	
}
