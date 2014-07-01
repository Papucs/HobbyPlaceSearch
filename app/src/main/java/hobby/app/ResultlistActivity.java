package hobby.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

/**
 * Helyszíntalálatok megjelenítése
 */
public class ResultlistActivity extends Activity {

    /**
     * a keresendő típusok listája
     */
    private ArrayList<String> types, types2;

    /**
     * keresés által kapott helyek listája
     */
    private List<Place> resultPlaces = new ArrayList<Place>();

    /**
     * lista megjelenítő
     */
    private ListView listView;

    /**
     * aktuálsi helyzet koordinátái
     */
    private double[] currentLocation;

    /**
     * GoogleAPI kulcs
     */
    private final String API_KEY = "AIzaSyBx0rWF_XU9agah1JdVQ9q_73RCRKTm6NI";

    /**
     * helyek látogatottsága
     */
    private boolean frequentlyVisited;

    /**
     * folyamatjelző
     */
    private ProgressBar pb;

    private String ineKey;
    private long ineId;


    /**
     * fejléc megjelenítése
     */
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

    /**
     * grafikus elemek megjelenítése, Activity létrehozása, a hívó Activtiytől érkező adatok kiolvasása
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultlist);
        pb=(ProgressBar) findViewById(R.id.pb_loading);
        showActionBar();
        Intent intent = getIntent();
        types=intent.getExtras().getStringArrayList("selectedTypes");
        types2=intent.getExtras().getStringArrayList("selectedTypes2");
        currentLocation = intent.getExtras().getDoubleArray("origin");
        frequentlyVisited=intent.getExtras().getBoolean("frequency", false);
        ineKey=intent.getExtras().getString("ineTrackKey");
        ineId = intent.getExtras().getLong("ineTrackId");

        new getPlacesAsyncTask().execute();

    }



    /**
     * A megadott paraméterek alapján találati lista lekérése
     * @return találati lista
     */
    public List<Place> getPlaces() {

        List<Place> places= new ArrayList<Place>();


        if(types.size()!=0) {
            String t = types.get(0);
            String radius = "";
            for (int i = 1; i < types.size(); ++i) {
                t = t + "|" + types.get(i);
            }
            Document doc = null;

            if (frequentlyVisited) {
                radius = "4000";
            } else {
                radius = "10000";
            }

            String uri =
                    "https://maps.googleapis.com/maps/api/place/nearbysearch/xml?location=" + currentLocation[0] + "," + currentLocation[1] + "&radius=" + radius + "&types=" + t + "&sensor=true&key=" + API_KEY;

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
            if (nList.getLength() != 0) {
                for (int i = 0; i < nList.getLength(); ++i) {

                    Node result = nList.item(i);
                    NodeList attribs = result.getChildNodes();

                    Place p = new Place();

                    for (int j = 1; j < attribs.getLength(); j += 2) {
                        String str = attribs.item(j).getNodeName();
                        if (str.equals("name")) {
                            p.setName(attribs.item(j).getTextContent());
                        } else if (str.equals("vicinity")) {
                            p.setAddress(attribs.item(j).getTextContent());
                        } else if (str.equals("geometry")) {
                            NodeList loc = attribs.item(j).getChildNodes();
                            String s = loc.item(1).getNodeName();
                            NodeList coords = loc.item(1).getChildNodes();
                            double lat = Double.parseDouble(coords.item(1).getTextContent());
                            double lng = Double.parseDouble(coords.item(3).getTextContent());
                            p.setCoord(lat, lng);
                        } else if (str.equals("rating")) {
                            p.setRating(Double.parseDouble(attribs.item(j).getTextContent()));
                        }
                    }
                    places.add(p);
                }

            }
        }
            if(types2.size()!=0) {
                JSONObject jo;
                List<JSONObject> pois = new ArrayList<JSONObject>();
                String url = "https://beta-api.inetrack.com/api?command=ObjectListCommand&descriptorName=userPoisDescName&searchParameters.descriptorName=userPoisDescName&searchParameters.userId=" + ineId + "&apikey=" + ineKey;

                try {
                    jo = new JSONObjectMaker(url).getObject();

                    if (jo.getBoolean("success")) {

                        JSONArray jaa = jo.getJSONArray("list");
                        for(int i=0; i<jaa.length();++i){
                            pois.add(jaa.getJSONObject(i));

                        }

                        for (JSONObject j : pois) {
                            String type = j.getJSONObject("relationValues").getJSONObject("group").getString("displayName");
                            if(types2.contains(type)) {
                                Place p = new Place();
                                p.setAddress("null");
                                p.setName(j.getJSONObject("stringValues").getString("name"));

                                String coord = j.getJSONObject("stringValues").getString("fence");
                                String[] c = coord.split("\\(\\(");
                                String cc = c[1].split("\\,")[0];
                                String[] ccc = cc.split("\\s");
                                p.setCoord(Double.parseDouble(ccc[0]), Double.parseDouble(ccc[1]));
                                places.add(p);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        if(places.size()!=0){
            Collections.sort(places, new RateComparator());
            Collections.reverse(places);

            return places;
        }else{
           return null;
        }
    }

    /**
     * vissza az előző Activityre
     * @param v
     */
    public void back(View v){
        onBackPressed();
    }

    /**
     * A listából kiválasztott elemek kiolvasása, továbbítás a meghívásra kerülő MapActivtiynek
     * @param v
     */
    public void getSelectedItems(View v)
    {
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

        if(checked.size()==0){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Válassz úticélt!")
                    .setMessage("Legalább egy úticél megadása szükséges! ")
                    .setCancelable(true)
                    .setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();

                                }
                            }
                    );

            AlertDialog alert = builder.create();
            alert.show();
        }else {

            Intent intent = new Intent(ResultlistActivity.this, MapActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("checked", checked);
            bundle.putDoubleArray("latlng", currentLocation);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }


    /**
     * adatok lekérése
     */
    private class getPlacesAsyncTask extends AsyncTask<Void, Void, List<Place>> {

        /**
         * folyamatjelző megjelenítése
         */
        @Override
        protected void onPreExecute(){
            pb.setVisibility(View.VISIBLE);
        }

        /**
         * Itt történik meg tényelgesen az API hívás
         * @param params
         * @return találati lista
         */
        @Override
        protected List<Place> doInBackground(Void... params) {
            return getPlaces();
        }

        /**
         * visszatérés az UI-hoz, folyamatjelző elrejtése, lista megjelenítése
         * @param result
         */
        @Override
        protected void onPostExecute(List<Place> result){
            if(result!=null) {
                resultPlaces.addAll(result);



                List<CharSequence> res = new ArrayList<CharSequence>();

                for (int j = 0; j < resultPlaces.size(); ++j) {
                    CharSequence cs = Html.fromHtml("<html><b>" + resultPlaces.get(j).getName() +
                            "</b><br>" + resultPlaces.get(j).getRating());
                    res.add(cs);
                }

                listView = (ListView) findViewById(R.id.resultList);


                final ArrayAdapter adapter = new ArrayAdapter(getBaseContext(),
                        R.layout.list_item, res);

                listView.setAdapter(adapter);
                pb.setVisibility(View.GONE);
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(ResultlistActivity.this);

                builder.setTitle("Nincs találat")
                        .setMessage("Nincs a környéken az általad keresett típusú helyszín. ")
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        onBackPressed();
                                    }
                                }
                        );

                final AlertDialog alert = builder.create();
                ResultlistActivity.this.runOnUiThread(new java.lang.Runnable() {
                    public void run() {
                        alert.show();
                    }
                });
            }

        }
    }

}
