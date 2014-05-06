package hobby.app;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

class DMElement{
    public double duration;
    public double distance;
    public List<LatLng> wayPoints = new ArrayList<LatLng>();
}


class CustomComparator implements Comparator<Place> {
    @Override
    public int compare(Place a, Place b){
        return a.getDistance().compareTo(b.getDistance());
    }
}

public class MapActivity extends FragmentActivity{

    private GoogleMap map;
    private ArrayList<Place> selectedPlaces = new ArrayList<Place>();
    private LatLng currentLocation,destination;
    private Map<Integer,String> points = new HashMap<Integer,String>();
    private final String API_KEY = "AIzaSyBx0rWF_XU9agah1JdVQ9q_73RCRKTm6NI";
    private DMElement[][]distanceMatrix;
    private Polyline line;
    private boolean modeNotAvailable=false;
    private AlertDialog alert;
    private ArrayList<CharSequence> writtenDirections = new ArrayList<CharSequence>();

    private void showActionBar() {
        LayoutInflater inflator = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.map_ab, null);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled (false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(v);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        showActionBar();

        map = ((SupportMapFragment) getSupportFragmentManager()
             .findFragmentById(R.id.map)).getMap();

        Bundle bundle = getIntent().getExtras();
        selectedPlaces=bundle.getParcelableArrayList("checked");

        double [] c = bundle.getDoubleArray("latlng");
        currentLocation= new LatLng(c[0],c[1]);


        map.addMarker(new MarkerOptions()
                .title("Itt vagyok!")
                .position(currentLocation));

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));

        //LatLngBounds llb = new LatLngBounds(currentLocation,selectedPlaces.get(selectedPlaces.size()-1).getCoord());
        //map.moveCamera(CameraUpdateFactory.newLatLngBounds(llb,10));
    }

    public void makeAlert(){
        AlertDialog.Builder  builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle("Mód nem elérhető");
        builder.setMessage("A térségben az általad választott mód nem elérhető, kérlek válassz másiakt! ");
        builder.setCancelable(true);
       /* builder.setNegativeButton("Mégse",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                         dialog.cancel();

                    }
                }
        );*/
        builder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //dialog.cancel();
                        finish();
                        //startActivity(getIntent());
                    }
                }
        );

        alert = builder.create();
    }

    public void drawSpots(){
        for(int i=0;i<selectedPlaces.size();++i){
            map.addMarker(new MarkerOptions()
                    .title(selectedPlaces.get(i).getName()+" - "+i)
                    .position(selectedPlaces.get(i).getCoord()));
        }

    }

    public void listDirections(View v){
        Intent intent  = new Intent(MapActivity.this,Directions.class);
        Bundle bundle = new Bundle();
        bundle.putCharSequenceArrayList("directionsList", writtenDirections);
       // bundle.putStringArrayList("directionsList", writtenDirections);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void onDriveClick(View v){

        if(selectedPlaces.size()==1){
            destination=selectedPlaces.get(0).getCoord();
            new SingleDestinationAsyncTask().execute("drive");
        }else{
            new MultipleDestinationsAsyncTask().execute("drive");
        }
    }
    public void onBikeClick(View v){
        if(selectedPlaces.size()==1){
            destination=selectedPlaces.get(0).getCoord();
            new SingleDestinationAsyncTask().execute("bicycling");
        }else{
            new MultipleDestinationsAsyncTask().execute("drive");
        }
    }

    public void onWalkingClick(View v) {
        if (selectedPlaces.size() == 1) {
            destination = selectedPlaces.get(0).getCoord();
            new SingleDestinationAsyncTask().execute("walking");
        } else {
            new MultipleDestinationsAsyncTask().execute("drive");
        }
    }

    public void back(View v){
        onBackPressed();
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

    public DMElement[][] getDistances(){


        points.put(0,"current");
        for(int i=0; i<selectedPlaces.size();++i){
            points.put(i+1,selectedPlaces.get(i).getName());
        }
        DMElement[][] dm = new DMElement[points.size()][points.size()];
        Document doc=null;
        String coordinates;
        coordinates =currentLocation.latitude+","+currentLocation.longitude;
        for(int i=0; i<selectedPlaces.size();++i){
            coordinates=coordinates+"|"+selectedPlaces.get(i).coordToString();
        }

        String uri="http://maps.googleapis.com/maps/api/distancematrix/xml?origins="+coordinates+"&destinations="+coordinates+"&sensor=false";

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

        NodeList rows = doc.getElementsByTagName("row");
        int x;
        int c = rows.getLength();
        for(int i=0;i<rows.getLength();++i){
            x=0;
            NodeList rowContent = rows.item(i).getChildNodes();
            for(int j=0; j<rowContent.getLength();++j){
                if(rowContent.item(j).getNodeName().equals("element")){
                    Node e = rowContent.item(j);
                    DMElement dme=new DMElement();
                    dme.duration=Double.parseDouble(e.getChildNodes().item(3).getChildNodes().item(1).getTextContent())/3600;
                    dme.distance=Double.parseDouble(e.getChildNodes().item(5).getChildNodes().item(1).getTextContent())/1000;
                    dm[i][x]=dme;
                    ++x;
                }
            }
        }

        List<Place> all = new ArrayList<Place>();
        return dm;
    }

    public void sortPlaces(){

        List<Place> sorted= new ArrayList<Place>();
        sorted.addAll(selectedPlaces);

        for(Place p : selectedPlaces){
            p.setDistance(distanceMatrix[0][selectedPlaces.indexOf(p)+1].distance);
        }

        Collections.sort(selectedPlaces, new CustomComparator());
    }



    public List<LatLng> getDirections(LatLng origin, LatLng destination, String mode ) {

        String orig = origin.latitude + "," + origin.longitude;
        String dest = destination.latitude + "," + destination.longitude;
        Document doc = null;
        String uri;
        if(selectedPlaces.size()==1) {
            uri =
                    "http://maps.google.com/maps/api/directions/xml?origin=" + orig + "&destination=" + dest + "&language=HUNGARIAN&region=HU&sensor=false&mode=" + mode;
        }else{
            String wp = selectedPlaces.get(0).coordToString();
            for(int i=1; i<selectedPlaces.size()-1;++i){
                wp=wp+"|"+selectedPlaces.get(i).coordToString();
            }

            uri =
                    "http://maps.google.com/maps/api/directions/xml?origin=" + orig + "&destination=" + dest + "&waypoints="+wp+"&language=HUNGARIAN&region=HU&sensor=false&mode=" + mode;
        }
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
        try {
            if (nList.getLength() == 0) {
                throw new ModeNotAvailableException();
            }
        }catch(ModeNotAvailableException e){
            modeNotAvailable=true;
            return new ArrayList<LatLng>();
        }

            NodeList pList = polyline.getChildNodes();

           String points = pList.item(1).getTextContent();

        NodeList wd = doc.getElementsByTagName("html_instructions");
        for(int j=0;j<wd.getLength();++j){
            CharSequence s =Html.fromHtml("<html>"+wd.item(j).getTextContent()+"</html>");
            writtenDirections.add(s);
        }

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

    private class SingleDestinationAsyncTask extends AsyncTask<String, Void, List<LatLng>> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected List<LatLng> doInBackground(String... params) {
            List<LatLng> result = new ArrayList<LatLng>();
            result = getDirections(currentLocation, destination, params[0]);

            return result;

        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(List<LatLng> result) {
            if(modeNotAvailable){
                makeAlert();

                MapActivity.this.runOnUiThread(new java.lang.Runnable() {
                    public void run() {
                        //show AlertDialog
                        alert.show();
                    }
                });
            }else {
                drawSpots();
                //útvonal kirajzolása
                PolylineOptions lineOptions = new PolylineOptions()
                        .color(Color.MAGENTA)
                        .width(5);
                line = map.addPolyline(lineOptions);
                line.setPoints(result);
                ImageButton ib = (ImageButton) findViewById(R.id.dList);
                ib.setVisibility(View.VISIBLE);
            }

        }

    }

    private class MultipleDestinationsAsyncTask extends AsyncTask<String,Void, List<LatLng>>{


        @Override
        protected void onPreExecute(){

        }

        @Override
        protected List<LatLng> doInBackground(String... params){

            distanceMatrix = getDistances();
            sortPlaces();
            return getDirections(currentLocation, selectedPlaces.get(selectedPlaces.size()-1).getCoord(),params[0]);

        }

        @Override
        protected void onPostExecute(List<LatLng> result){

            if(modeNotAvailable){
                makeAlert();

                MapActivity.this.runOnUiThread(new java.lang.Runnable() {
                    public void run() {
                        //show AlertDialog
                        alert.show();
                    }
                });
            }else {
                drawSpots();
                PolylineOptions lineOptions = new PolylineOptions()
                        .color(Color.MAGENTA)
                        .width(5);
                line = map.addPolyline(lineOptions);
                line.setPoints(result);
                ImageButton ib = (ImageButton) findViewById(R.id.dList);
                ib.setVisibility(View.VISIBLE);
            }
        }


    }

}
