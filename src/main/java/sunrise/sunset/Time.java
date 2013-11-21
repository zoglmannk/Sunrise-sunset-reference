package sunrise.sunset;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * The only reason this class exists is so that this reference
 * algorithm can be translated slightly more easily to another 
 * language.
 */
public class Time {
	
	public final int hour;
	public final int min;
	
	
	public Time(int hour, int min) {
		this.hour = hour;
		this.min = min;
	}
	
	public String toString() {
		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);
		
		writer.printf("%d:%02d", hour, min);
		
		writer.flush();
		return sw.getBuffer().toString();
	}
	
}
