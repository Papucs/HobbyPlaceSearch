package hobby.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
    private double[] currentLocation;
    private final String API_KEY = "AIzaSyBx0rWF_XU9agah1JdVQ9q_73RCRKTm6NI";
    private boolean frequentlyVisited;

    private void showActionBar() {
        LayoutInflater inflator = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.list_ab2, null);
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
        setContentView(R.layout.activity_resultlist);
        showActionBar();
        Intent intent = getIntent();
        types=intent.getExtras().getStringArrayList("selectedTypes");
        currentLocation = intent.getExtras().getDoubleArray("origin");
        frequentlyVisited=intent.getExtras().getBoolean("frequency", false);

        new getPlacesAsyncTask().execute();

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
                "https://maps.googleapis.com/maps/api/place/nearbysearch/xml?location=" + currentLocation[0]+","+currentLocation[1] + "&radius="+radius+"&types="+ t +"&sensor=true&key="+API_KEY;

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
        Collections.sort(places, new RateComparator());
        Collections.reverse(places);
        return places;


    }

    public void back(View v){
        onBackPressed();
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
        bundle.putDoubleArray("latlng",currentLocation );
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

            }
           List<CharSequence> res = new ArrayList<CharSequence>();

            for(int j=0; j<resultPlaces.size();++j){
               // res.add(resultPlaces.get(j).getName());
                // CharSequence s =Html.fromHtml("<html>"+wd.item(j).getTextContent()+"</html>");
                CharSequence cs = Html.fromHtml("<html><b>" + resultPlaces.get(j).getName()+
                        "</b><br>" + resultPlaces.get(j).getRating());
                res.add(cs);
            }

            listView = (ListView)findViewById(R.id.resultList);



            final ArrayAdapter adapter = new ArrayAdapter(getBaseContext(),
                    android.R.layout.simple_list_item_multiple_choice,res);

            listView.setAdapter(adapter);

        }
    }

}
