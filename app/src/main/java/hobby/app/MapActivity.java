package hobby.app;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


class DistanceComparator implements Comparator<Place> {
    @Override
    public int compare(Place a, Place b) {
        return a.getDistance().compareTo(b.getDistance());
    }
}

public class MapActivity extends FragmentActivity {

    private GoogleMap map;
    private ArrayList<Place> selectedPlaces = new ArrayList<Place>();
    private LatLng currentLocation;
    private boolean modeNotAvailable = false;
    private AlertDialog alert;
    private ArrayList<CharSequence> writtenDirections = new ArrayList<CharSequence>();
    private List<Marker> markers = new ArrayList<Marker>();
    private boolean withBicycle = false;
    private boolean withCar = false;
    private boolean onFoot = false;
    private Polyline bikeLine, carLine, walkLine;
    private ImageButton ib;
    private CheckBox checked;
    private int numOfChecked = 0;
    private boolean onMapClicked = false;

    private void showActionBar() {
        LayoutInflater inflator = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.map_ab, null);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        //pb=(ProgressBar) findViewById(R.id.route_loading);
        actionBar.setCustomView(v);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        showActionBar();
        ib = (ImageButton) findViewById(R.id.dList);

        map = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map)).getMap();

        Bundle bundle = getIntent().getExtras();
        selectedPlaces = bundle.getParcelableArrayList("checked");

        double[] c = bundle.getDoubleArray("latlng");
        currentLocation = new LatLng(c[0], c[1]);


        Marker m = map.addMarker(new MarkerOptions()
                .title("Itt vagyok!")
                .position(currentLocation)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        markers.add(m);
        drawSpots();

        FrameLayout mapLayout = (FrameLayout) findViewById(R.id.map_layout);
        mapLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker marker : markers) {
                    builder.include(marker.getPosition());
                }
                LatLngBounds bounds = builder.build();
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, -10));
            }
        });

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (!onMapClicked) {
                    View mb = (RelativeLayout) findViewById(R.id.modeBar);
                    mb.setVisibility(View.GONE);
                    onMapClicked = true;
                } else {
                    View mb = (RelativeLayout) findViewById(R.id.modeBar);
                    onMapClicked = false;
                    mb.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void makeAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle("Mód nem elérhető");
        builder.setMessage("A térségben az általad választott mód nem elérhető, kérlek válassz másiakt! ");
        builder.setCancelable(true);
        builder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        checked.setChecked(false);
                        dialog.cancel();
                    }
                }
        );

        alert = builder.create();
    }

    public void drawSpots() {
        for (int i = 0; i < selectedPlaces.size(); ++i) {

            Marker m = map.addMarker(new MarkerOptions()
                    .title(selectedPlaces.get(i).getName() + "/n" + selectedPlaces.get(i).getAddress() )
                    .position(selectedPlaces.get(i).getCoord()));
            markers.add(m);
        }
    }

    public void listDirections(View v) {
        Intent intent = new Intent(MapActivity.this, DirectionsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putCharSequenceArrayList("directionsList", writtenDirections);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void onDriveClick(View v) {
        if (!withCar) {
            new DrawDirectionsAsyncTask().execute("drive");
            withCar = true;
            checked = (CheckBox) findViewById(R.id.modeDrive);
            ++numOfChecked;
        } else {
            withCar = false;
            carLine.remove();
            --numOfChecked;
            ib.setEnabled(false);

        }

    }

    public void onBikeClick(View v) {
        if (!withBicycle) {
            new DrawDirectionsAsyncTask().execute("bicycling");
            withBicycle = true;
            checked = (CheckBox) findViewById(R.id.modeBike);
            ++numOfChecked;
        } else {
            withBicycle = false;
            bikeLine.remove();
            --numOfChecked;
            ib.setEnabled(false);

        }
    }

    public void onWalkingClick(View v) {
        if (!onFoot) {
            new DrawDirectionsAsyncTask().execute("walking");
            onFoot = true;
            checked = (CheckBox) findViewById(R.id.modeWalking);
            ++numOfChecked;
        } else {
            onFoot = false;
            walkLine.remove();
            --numOfChecked;
            ib.setEnabled(false);
        }

    }

    public void back(View v) {
        onBackPressed();
    }


    public void getDistances() {

        Map<Integer, String> points = new HashMap<Integer, String>();
        points.put(0, "current");
        for (int i = 0; i < selectedPlaces.size(); ++i) {
            points.put(i + 1, selectedPlaces.get(i).getName());
        }

        Double[][] distances = new Double[points.size()][points.size()];
        Document doc = null;
        String coordinates;
        coordinates = currentLocation.latitude + "," + currentLocation.longitude;
        for (int i = 0; i < selectedPlaces.size(); ++i) {
            coordinates = coordinates + "|" + selectedPlaces.get(i).coordToString();
        }

        String uri = "http://maps.googleapis.com/maps/api/distancematrix/xml?origins=" + coordinates + "&destinations=" + coordinates + "&sensor=false";

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
        x = 0;
        NodeList rowContent = rows.item(0).getChildNodes();
        for (int j = 3; j < rowContent.getLength(); ++j) {
            if (rowContent.item(j).getNodeName().equals("element")) {
                Node e = rowContent.item(j);
                selectedPlaces.get(x).setDistance(Double.parseDouble(e.getChildNodes().item(5).getChildNodes().item(1).getTextContent()) / 1000);
                ++x;
            }

        }

    }


    public List<LatLng> getDirections(String mode) {
        getDistances();
        Collections.sort(selectedPlaces, new DistanceComparator());
        String orig = currentLocation.latitude + "," + currentLocation.longitude;
        Place tmp = selectedPlaces.get(selectedPlaces.size() - 1);
        String dest = tmp.getCoord().latitude + "," + tmp.getCoord().longitude;


        Document doc = null;
        String uri;
        if (selectedPlaces.size() == 1) {

            uri =
                    "http://maps.google.com/maps/api/directions/xml?origin=" + orig + "&destination=" + dest + "&language=HUNGARIAN&region=HU&sensor=false&mode=" + mode;
        } else {


            String wp = selectedPlaces.get(0).coordToString();
            for (int i = 1; i < selectedPlaces.size() - 1; ++i) {
                wp = wp + "|" + selectedPlaces.get(i).coordToString();
            }

            uri =
                    "http://maps.google.com/maps/api/directions/xml?origin=" + orig + "&destination=" + dest + "&waypoints=" + wp + "&language=hu5&region=HU&sensor=false&mode=" + mode;
        }
        try {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            URL u = new URL(uri);

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
        } catch (ModeNotAvailableException e) {
            modeNotAvailable = true;
            return new ArrayList<LatLng>();
        }

        NodeList pList = polyline.getChildNodes();

        String points = pList.item(1).getTextContent();

        NodeList wd = doc.getElementsByTagName("html_instructions");
        for (int j = 0; j < wd.getLength(); ++j) {
            CharSequence s = Html.fromHtml("<html>" + wd.item(j).getTextContent() + "</html>");
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

    private class DrawDirectionsAsyncTask extends AsyncTask<String, Void, List<LatLng>> {

        private String mode;

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected List<LatLng> doInBackground(String... params) {

            mode = params[0];
            return getDirections(params[0]);

        }

        @Override
        protected void onPostExecute(List<LatLng> result) {

            if (modeNotAvailable) {
                makeAlert();

                MapActivity.this.runOnUiThread(new java.lang.Runnable() {
                    public void run() {
                        alert.show();
                    }
                });
            } else {
                if (mode.equals("bicycling")) {
                    PolylineOptions lineOptions = new PolylineOptions()
                            .color(Color.GREEN)
                            .width(5);
                    bikeLine = map.addPolyline(lineOptions);
                    bikeLine.setPoints(result);
                } else if (mode.equals("drive")) {
                    PolylineOptions lineOptions = new PolylineOptions()
                            .color(Color.MAGENTA)
                            .width(5);
                    carLine = map.addPolyline(lineOptions);
                    carLine.setPoints(result);
                } else {
                    PolylineOptions lineOptions = new PolylineOptions()
                            .color(Color.BLUE)
                            .width(5);
                    walkLine = map.addPolyline(lineOptions);
                    walkLine.setPoints(result);
                }
                if (numOfChecked==1) {
                    ib.setEnabled(true);
                }else{
                    ib.setEnabled(false);
                }
            }
        }


    }

}
