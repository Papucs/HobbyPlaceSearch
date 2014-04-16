package hobby.app;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class MapActivity extends FragmentActivity {

    private GoogleMap map;
    private ArrayList<Place> selectedPlaces = new ArrayList<Place>();
    private LatLng currentLocation,destination;
    private LocationManager locationManager;
    private String locationProvider;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        map = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map)).getMap();

        Intent intent = getIntent();
        selectedPlaces = intent.getParcelableArrayListExtra("checked");

        double [] c = intent.getDoubleArrayExtra("latlng");
        currentLocation= new LatLng(c[0],c[1]);

        map.addMarker(new MarkerOptions()
                    .title("Itt vagyok!")
                    .position(currentLocation));

        for(int i=0;i<selectedPlaces.size();++i){
            map.addMarker(new MarkerOptions()
                    .title(selectedPlaces.get(i).getName())
                    .position(selectedPlaces.get(i).getCoord()));
        }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));

        if(selectedPlaces.size()==1){
            destination=selectedPlaces.get(0).getCoord();
            new HttpAsyncTask().execute();
        }
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    public String getLocation(){
        LatLng myLocation;


       // return myLocation.latitude()+","+ myLocation.longitude();
        return "cica";
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public List<LatLng> getDirections(LatLng origin, LatLng destination) {

        String orig = origin.latitude + "," + origin.longitude;
        String dest = destination.latitude + "," + destination.longitude;
        Document doc = null;

        String uri =
                "http://maps.google.com/maps/api/directions/xml?origin=" + orig + "&destination=" + dest + "&sensor=false&mode=drive";

        try {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(new URL(uri).openStream());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        NodeList nList = doc.getElementsByTagName("overview_polyline");
        Node polyline = nList.item(0);
        NodeList pList = polyline.getChildNodes();

        String points = pList.item(1).getTextContent();

        return decodePoly(points);

    }

    //code source: http://www.geekyblogger.com/2010/12/decoding-polylines-from-google-maps.html
    //a két végpont közti útvonal pontjait dekódolja a google directions apija áltla visszaadott vonnalláncból
    public static List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        double lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            double dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            double dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng(((lat / 1E5)),
                    ((lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    private class HttpAsyncTask extends AsyncTask<Void, Void, List<LatLng>> {
        @Override
        protected List<LatLng> doInBackground(Void... params) {

            return getDirections(currentLocation, destination);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(List<LatLng> result) {
            List<LatLng> wayPoints = new ArrayList<LatLng>();
            wayPoints.addAll(result);

            //útvonal kirajzolása
            PolylineOptions lineOptions = new PolylineOptions()
                    .color(Color.MAGENTA)
                    .width(5);
            Polyline line = map.addPolyline(lineOptions);
            line.setPoints(wayPoints);

        }
    }

}
