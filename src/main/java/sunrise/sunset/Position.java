package sunrise.sunset;

import java.io.PrintWriter;
import java.io.StringWriter;


public class Position {
	private static final double DR = Math.PI/180.0; //degrees to radians constant
	
	public final double rightAscention, //in radians
						declination;    //in radians
	
	
	public Position(double rightAscention, double declination) {
		this.rightAscention = rightAscention;
		this.declination = declination;
	}
	
	public String toString() {
		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);
		
		writer.printf("Position (rightAscention %s, declination %s", 
				      new AstroHour(rightAscention/DR), 
				      new AstroDegrees(declination/DR));
		
		writer.flush();
		return sw.getBuffer().toString();
	}
			
	
	private static class AstroHour {
		final static int DEGRES_PER_HOUR = 15;
		
		final int hours, minutes;
		final double seconds;
		
		
		public AstroHour(double decimalDegrees) {
			double tmp = decimalDegrees/DEGRES_PER_HOUR;
			hours =  (int) tmp;
			
			tmp = (tmp - hours)*60;
			minutes = (int) tmp;
			
			seconds= (tmp - minutes)*60; 
		}
		
		public String toString() {
			StringWriter sw = new StringWriter();
			PrintWriter writer = new PrintWriter(sw);
			
			writer.printf("%dh %dm %(.2fs", hours, minutes, seconds);
			
			writer.flush();
			return sw.getBuffer().toString();
		}
		
	}
	
	private static class AstroDegrees {
		
		final int degrees, minutes;
		final double seconds;
		
		
		public AstroDegrees(double decimalDegrees) {
			double tmp = decimalDegrees;
			degrees =  (int) tmp;
			
			tmp = (tmp - degrees)*60;
			minutes = (int) tmp;
			
			seconds= (tmp - minutes)*60; 
		}
		
		public String toString() {
			StringWriter sw = new StringWriter();
			PrintWriter writer = new PrintWriter(sw);
			
			writer.printf("%ddegrees %dm %(.2fs)", degrees, minutes, seconds);
			
			writer.flush();
			return sw.getBuffer().toString();
		}
		
	}

}
