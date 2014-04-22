package hobby.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

class DMElement{
    public float duration;
    public float distance;
    public List<LatLng> wayPoints = new ArrayList<LatLng>();
}

public class MapActivity extends FragmentActivity {

    private GoogleMap map;
    private ArrayList<Place> selectedPlaces = new ArrayList<Place>();
    private LatLng currentLocation,destination;
    private LocationManager locationManager;
    private String locationProvider;
    private Map<Integer,String> points = new HashMap<Integer,String>();
    private final String API_KEY = "AIzaSyBx0rWF_XU9agah1JdVQ9q_73RCRKTm6NI";
    private DMElement[][]distanceMatrix;
    private Polyline line;
    private boolean modeNotAvailable=false;
    private String mode;


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



        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));
        drawSpots();
        /*
        if(selectedPlaces.size()==1){
            destination=selectedPlaces.get(0).getCoord();
            new SingleDestinationAsyncTask().execute();
        }else{
            new MultipleDestinationsAsyncTask().execute();
        }*/
    }

    public void drawSpots(){
        for(int i=0;i<selectedPlaces.size();++i){
            map.addMarker(new MarkerOptions()
                    .title(selectedPlaces.get(i).getName()+" - "+i)
                    .position(selectedPlaces.get(i).getCoord()));
        }

    }

    public void onDriveClick(View v){
        mode = "drive";
        if(selectedPlaces.size()==1){
            destination=selectedPlaces.get(0).getCoord();
            new SingleDestinationAsyncTask().execute(this);
        }else{
            new MultipleDestinationsAsyncTask().execute("drive");
        }
    }
    public void onBikeClick(View v){
        mode="bicycling";
        if(selectedPlaces.size()==1){
            destination=selectedPlaces.get(0).getCoord();
            new SingleDestinationAsyncTask().execute(this);
        }else{
            new MultipleDestinationsAsyncTask().execute("drive");
        }
    }

    public void onWalkingClick(View v) {
        mode="walking";
        if (selectedPlaces.size() == 1) {
            destination = selectedPlaces.get(0).getCoord();
            new SingleDestinationAsyncTask().execute(this);
        } else {
            new MultipleDestinationsAsyncTask().execute("drive");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
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


    public DMElement[][] getDistances()throws ModeNotAvailableException{

        //List<Route> routes = new ArrayList<Route>();
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
        //coordinates=coordinates+"|"+selectedPlaces.get(selectedPlaces.size()-1);


        //http://maps.googleapis.com/maps/api/distancematrix/xml?origins=Vancouver+BC|Seattle&destinations=San+Francisco|Vancouver+BC&mode=bicycling&language=fr-FR&sensor=false&key=API_KEY
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
                    dme.duration=Float.parseFloat(e.getChildNodes().item(3).getChildNodes().item(1).getTextContent())/3600;
                    dme.distance=Float.parseFloat(e.getChildNodes().item(5).getChildNodes().item(1).getTextContent())/1000;
                    dm[i][x]=dme;
                    ++x;
                }
            }
        }

        List<Place> all = new ArrayList<Place>();
        Place curr = new Place();
        curr.setName("cuurent location");
        curr.setCoord(currentLocation.latitude, currentLocation.longitude);
        all.add(curr);
        all.addAll(selectedPlaces);
        for(int i=0;i<all.size();++i){
            for(int j=0; j<all.size();++j){
                Place a = all.get(i);
                Place b = all.get(j);
                     dm[i][j].wayPoints=getDirections(a.getCoord(),b.getCoord(), "drive");

            }
        }        return dm;
    }

    public void sortPlaces(){

        List<Place> sorted= new ArrayList<Place>();
        for(int i=0; i<selectedPlaces.size();++i){

            selectedPlaces.get(i).setDistance(distanceMatrix[0][i].distance);

        }

        QuickSort(selectedPlaces, 0, selectedPlaces.size()-1);

    }

    public void QuickSort(List<Place> p, int first, int last){
        if(first<last){
            int k = moveToPlace(p,first, last);

            QuickSort(p, first, k - 1);
            QuickSort(p,k+1, last);
        }


    }

    public int moveToPlace(List<Place> p,int u, int v){


        int i=u+1, j=v;

        while(i<=j){
            while(i<=v && p.get(i).getDistance()<=p.get(u).getDistance()){
                ++i;
            }
            while(u+1<=j && p.get(u).getDistance()<= p.get(j).getDistance()){
                --j;
            }
            if(i<j){
                change(i, j);
                ++i;
                --j;
            }
        }

        change(u,i-1);

        return i-1;

    }

    public void change(int a, int b){
        Place x = selectedPlaces.get(a);
        Place y = selectedPlaces.get(b);

        selectedPlaces.set(a,y);
        selectedPlaces.set(b,x);
    }

    public List<LatLng> getDirections(LatLng origin, LatLng destination, String mode) {

        String orig = origin.latitude + "," + origin.longitude;
        String dest = destination.latitude + "," + destination.longitude;
        Document doc = null;

        String uri =
                "http://maps.google.com/maps/api/directions/xml?origin=" + orig + "&destination=" + dest + "&sensor=false&mode="+mode;

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
            return null;
        }

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

    private class SingleDestinationAsyncTask extends AsyncTask<Activity, Void, List<LatLng>> {
        private Activity thisAct;
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected List<LatLng> doInBackground(Activity... params) {
            List<LatLng> result = new ArrayList<LatLng>();

            result = getDirections(currentLocation, destination, mode);
            thisAct=params[0];
            return result;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(List<LatLng> result) {
            if (modeNotAvailable) {
                //dafuq, rá kéne jönni
                AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                builder.setTitle("Mód nem elérhető");
                builder.setMessage("A térségben az általad választott mód nem elérhető, kérlek válassz másiakt! ");
                //.setCancelable(true)
                builder.setNegativeButton("Mégse",null
                        /*new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // dialog.cancel();

                            }
                        }*/
                );
                builder.setPositiveButton("Ok",null
                        /*new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //dialog.cancel();
                                finish();
                                startActivity(getIntent());
                            }
                        }*/
                );

                final AlertDialog alert = builder.create();
               thisAct.runOnUiThread(new java.lang.Runnable() {
                    public void run() {
                        //show AlertDialog
                        alert.show();
                    }
                });
            }
            List<LatLng> wayPoints = new ArrayList<LatLng>();
            wayPoints.addAll(result);
            //drawSpots();
            //útvonal kirajzolása

            if(!line.getPoints().equals(null)){
                line.remove();
            }
            PolylineOptions lineOptions = new PolylineOptions()
                    .color(Color.MAGENTA)
                    .width(5);
            line = map.addPolyline(lineOptions);
            line.setPoints(wayPoints);

        }
    }

    private class MultipleDestinationsAsyncTask extends AsyncTask<String,Void, DMElement[][]>{
        @Override
        protected void onPreExecute(){

        }

        @Override
        protected DMElement[][] doInBackground(String... params){
           DMElement[][] result = new DMElement[points.size()][points.size()];
            try{
                result = getDistances();
            }catch(ModeNotAvailableException e){

            }
            return result;

        }

        @Override
        protected void onPostExecute(DMElement[][] result){
            distanceMatrix=result;
            List<LatLng> graph = new ArrayList<LatLng>();
           /// sortPlaces();
            //drawSpots();
            /*
            graph=getDirections(currentLocation, selectedPlaces.get(0).getCoord());
            PolylineOptions lineOp = new PolylineOptions()
                    .color(Color.BLUE)
                    .width(5);
            Polyline l = map.addPolyline(lineOp);
            l.setPoints(graph);*/



        }


    }

}
