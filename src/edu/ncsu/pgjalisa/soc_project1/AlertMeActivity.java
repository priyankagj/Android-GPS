package edu.ncsu.pgjalisa.soc_project1;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class AlertMeActivity extends MapActivity {

  LocationManager locationManager;
  MapController mapController;
  AlertsPositionOverlay positionOverlay;

  @Override
  protected boolean isRouteDisplayed() {
    return false;
  }

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    setContentView(R.layout.main);

    MapView myMapView = (MapView) findViewById(R.id.myMapView);
    mapController = myMapView.getController();

    // Configure the map display options
    myMapView.setSatellite(true);

    // Zoom in
    mapController.setZoom(17);

    // Add the AlertsPositionOverlay
    positionOverlay = new AlertsPositionOverlay(this);
    List<Overlay> overlays = myMapView.getOverlays();
    overlays.add(positionOverlay);

    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    Criteria criteria = new Criteria();
    criteria.setAccuracy(Criteria.ACCURACY_FINE);
    criteria.setAltitudeRequired(false);
    criteria.setBearingRequired(false);
    criteria.setCostAllowed(true);
    criteria.setPowerRequirement(Criteria.POWER_LOW);

    String provider = locationManager.getBestProvider(criteria, true);
    LocationListener loclis = new LocationListener(){
    			
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			updateWithNewLocation(location);
		}
	};
	
	locationManager.requestLocationUpdates(provider, 0, 0, loclis); 
	
    Location location = locationManager.getLastKnownLocation(provider);

    if (location != null)
      updateWithNewLocation(location);
  }

  /** Update UI with a new location */
  private void updateWithNewLocation(Location location) {
    TextView myLocationText = (TextView) findViewById(R.id.myLocationText);
    String latLongString;
    if (location != null) {
      // Update the map location.
      Double geoLat = location.getLatitude() * 1E6;
      Double geoLng = location.getLongitude() * 1E6;
      GeoPoint point = new GeoPoint(geoLat.intValue(), geoLng.intValue());

      mapController.animateTo(point);

      // update my position marker
      positionOverlay.setLocation(location);

      double lat = location.getLatitude();
      double lng = location.getLongitude();

      latLongString = "Lat:" + lat + "\nLong:" + lng;
    } 
    else {
      latLongString = "No location found";
    }
    
    /** The code below queries the Content Provider named AlertProvider and fetches the
     *  latitude, longitude and the alert columns.    **/
    
    
    String[] colsArray = new String[] {AlertProvider.KEY_PLACE_LAT, AlertProvider.KEY_PLACE_LNG, AlertProvider.KEY_ALERT};
    Uri Alerts = AlertProvider.CONTENT_URI;
    Cursor alertcur = managedQuery(Alerts, colsArray, null, null, null);
    
    if (alertcur.moveToFirst()) {
    	int latColIndex = alertcur.getColumnIndex(AlertProvider.KEY_PLACE_LAT);
    	int lngColIndex = alertcur.getColumnIndex(AlertProvider.KEY_PLACE_LNG);
    	int alertMsgColIndex = alertcur.getColumnIndex(AlertProvider.KEY_ALERT);
    	double dist;
    	
    	/** This code compares the point co-ordinates sent via DDMS with all the point 
    	 * co-ordinates present in the Content Provider. If the distance is less than 50m 
    	 * and if any alert is available, it will be displayed.  **/
    	
    	do{
    		double latit = alertcur.getDouble(latColIndex);
    		double longit = alertcur.getDouble(lngColIndex);
    		String alertMsg = alertcur.getString(alertMsgColIndex);
    		
    		dist= getDistance(location.getLatitude(), location.getLongitude(), latit, longit);
    		
    		if( dist < 50.00){
    			 myLocationText.setText("Your Current Position is:\n" + latLongString +'\n'+ alertMsg);
    			 break;
    		}
    		else{
    			myLocationText.setText("Your Current Position is:\n" + latLongString);
    		}
    	} while (alertcur.moveToNext());
    }
    else{
    	myLocationText.setText("Your Current Position is:\n" + latLongString);
    } 
  }

  
  /**
   * Finds distance between two coordinate pairs.
   *
   * @param lat1 First latitude in degrees
   * @param lon1 First longitude in degrees
   * @param lat2 Second latitude in degrees
   * @param lon2 Second longitude in degrees
   * @return distance in meters
   */
  public static double getDistance(double lat1, double lon1, double lat2, double lon2) {

    final double Radius = 6371 * 1E3; // Earth's mean radius

    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
        * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return Radius * c;
  }
}