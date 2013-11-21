package sunrise.sunset;



public class GpsCoordinate {
	
	public final double latitude; 
	public final double longitude;
	
	
	/**
	 * @param latitude   north latitudes positive
	 * @param longitude  west longitudes negative
	 */
	public GpsCoordinate(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
}
