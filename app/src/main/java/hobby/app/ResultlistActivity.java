package hobby.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

class RateComparator implements Comparator<Place> {
    @Override
    public int compare(Place a, Place b){
        return a.getRating().compareTo(b.getRating());
    }
}

public class ResultlistActivity extends Activity {

    private ArrayList<String> types;
    private ArrayList<Map<String,String>> results = new ArrayList<Map<String, String>>();
    private List<Place> resultPlaces = new ArrayList<Place>();
    private ListView listView;
    private ArrayList<String> res = new ArrayList<String>();
    private String myLocation;
    private Location currentLocation;
    private final String API_KEY = "AIzaSyBx0rWF_XU9agah1JdVQ9q_73RCRKTm6NI";
    private boolean frequentlyVisited;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultlist);
        Intent intent = getIntent();
        types=intent.getStringArrayListExtra("selectedTypes");
        frequentlyVisited=intent.getBooleanExtra("frequency", false);

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                //makeUseOfNewLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        String provider = LocationManager.NETWORK_PROVIDER;

        locationManager.requestLocationUpdates(provider, 0, 0, locationListener);

        currentLocation=locationManager.getLastKnownLocation(provider);

        myLocation = currentLocation.getLatitude()+","+currentLocation.getLongitude();

        new getPlacesAsyncTask().execute();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.resultlist, menu);
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

    public List<Place> getPlaces() {

        //String myLocation = getMyLocation();
        String t = types.get(0);
        String radius="";
        for(int i=1; i<types.size();++i){
            t=t+"|"+types.get(i);
        }
        Document doc = null;

        List<Place> places= new ArrayList<Place>();
        if(frequentlyVisited){
            radius="4000";
        }else{
            radius="10000";
        }
        //https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&radius=500&types=food&name=harbour&sensor=false&key=AddYourOwnKeyHere
        String uri =
                "https://maps.googleapis.com/maps/api/place/nearbysearch/xml?location=" + myLocation + "&radius="+radius+"&types="+ t +"&sensor=true&key="+API_KEY;

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

        NodeList nList = doc.getElementsByTagName("result");
        for(int i=0; i<nList.getLength();++i){

            Node result = nList.item(i);
            NodeList attribs = result.getChildNodes();

            Place p = new Place();

            for(int j=1; j<attribs.getLength();j+=2){
                String str = attribs.item(j).getNodeName();
                if(str.equals("name")){
                    p.setName(attribs.item(j).getTextContent());
                }else if(str.equals("vicinity")){
                    p.setAddress(attribs.item(j).getTextContent());
                }else if(str.equals("geometry")) {
                    NodeList loc = attribs.item(j).getChildNodes();
                    String s = loc.item(1).getNodeName();
                    NodeList coords = loc.item(1).getChildNodes();
                    double lat = Double.parseDouble(coords.item(1).getTextContent());
                    double lng = Double.parseDouble(coords.item(3).getTextContent());
                    p.setCoord(lat, lng);
                }else if(str.equals("rating")) {
                    p.setRating(Double.parseDouble(attribs.item(j).getTextContent()));
                }
            }
            places.add(p);
        }
        Collections.sort(places, new CustomComparator());
        return places;


    }

    public void getSelectedItems(View v)
    {
        //---toggle the check displayed next to the item---
        ArrayList<Place> checked = new ArrayList<Place>();
        int len =listView.getCount();
        SparseBooleanArray c=listView.getCheckedItemPositions();

        for (int i = 0; i<len; i++)
        {
            if (c.get(i)) {
                Place item = resultPlaces.get(i);
                checked.add(item);

            }
        }

        Intent intent  = new Intent(ResultlistActivity.this,MapActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("checked", checked);
        double[] x =new double[]{currentLocation.getLatitude(), currentLocation.getLongitude()};
        bundle.putDoubleArray("latlng",x );
        intent.putExtras(bundle);
        startActivity(intent);

    }


    private class getPlacesAsyncTask extends AsyncTask<Void, Void, List<Place>> {
        private ProgressDialog pdia;
        @Override
        protected void onPreExecute(){

        }

        @Override
        protected List<Place> doInBackground(Void... params) {
            return getPlaces();
        }

        @Override
        protected void onPostExecute(List<Place> result){
            resultPlaces.addAll(result);
           // ArrayList<Item> items = new ArrayList<Item>();

            for (int i=0; i<resultPlaces.size();++i){
                Map<String, String> m = new HashMap<String, String>();
                m.put("FirstLine", resultPlaces.get(i).getName());
                m.put("SecondLine",resultPlaces.get(i).getAddress());
                results.add(m);
                //Place p = resultPlaces.get(i);
                //items.add(new Item(p.getName(), p.getAddress(), p.getRating().toString()));
            }


            for(int j=0; j<resultPlaces.size();++j){
                res.add(resultPlaces.get(j).getName());
            }

            listView = (ListView)findViewById(R.id.resultList);



            final ArrayAdapter adapter = new ArrayAdapter(getBaseContext(),
                    android.R.layout.simple_list_item_multiple_choice,res);

            /*
            ArrayAdapter<MyObject> adapter = new ArrayAdapter<MyObject>(this, android.R.layout.simple_list_item_2, android.R.id.text1, result) {
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
        TextView text2 = (TextView) view.findViewById(android.R.id.text2);

        text1.setText(result.get(position).OperatingSystem);
        text2.setText(result.get(position).Platform);
        return view;
    }
};
             */
            /*
            MyAdapter adapter = new MyAdapter(getBaseContext(), items);*/



            listView.setAdapter(adapter);

        }
    }

}
