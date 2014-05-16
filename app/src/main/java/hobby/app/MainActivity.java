package hobby.app;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

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

/**
 * kezdő képernyő és funkciók
 */
public class MainActivity extends Activity implements AdapterView.OnItemClickListener{
    /**
     * API híváshoz szükséges kiválasztott típusok listája
     */
    private ArrayList<String> selected = new ArrayList<String>();

    /**
     * kiválasztott típusok megjelenítendő neveinek listája
     */
    private ArrayList<String> selectedNames = new ArrayList<String>();

    /**
     * Látogatás gyakoriságát jelző logikai változó
     */
    private boolean frequentlyVisited = false;

    /**
     * Kiindulási helyet jelző logikai változó
     */
    private boolean otherOrigin = false;

    /**
     * kiindulási hely megadására alkalmas beviteli mező
     */
    private AutoCompleteTextView orig;

    /**
     * API kulcs
     */
    private final String API_KEY="AIzaSyBx0rWF_XU9agah1JdVQ9q_73RCRKTm6NI";


    /**
     * Automatikus kiegészítési listából való elem választás
     * @param adapterView
     * @param view
     * @param position
     * @param id
     */
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            String str = (String) adapterView.getItemAtPosition(position);
            orig.setText(str);
        }

    /**
     * Grafikai elemek megjelenítése, internet hozzáférés ellenőrzése
     * Amennyiben nem találhatő hálózat, felugró értesítést ad.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setTheme(R.style.AppThemeNoBar);

        setContentView(R.layout.activity_main);


        orig = (AutoCompleteTextView) findViewById(R.id.otherOrigin);
        orig.setAdapter(new PlacesAutoCompleteAdapter(this, android.R.layout.simple_list_item_1));
        orig.setOnItemClickListener(this);

        if (!checkInternet()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Hálózat nem elérhető")
                    .setMessage("Engedélyezz valamilyen hálózati hozzáférést! ")
                    .setCancelable(true)
                    .setNegativeButton("Mégse",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // dialog.cancel();
                                    MainActivity.this.finish();
                                }
                            }
                    )
                    .setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                    startActivity(intent);

                                }
                            }
                    );

            AlertDialog alert = builder.create();
            alert.show();
        }


    }

    /**
     * Hálozati elérés ellenőrzése (WIFI, ill mobilnet)
     *
     * @return Logikai érték, annak fügvényében, érzékel-e valamilyen aktív hálózati hozzáférést
     */
    public boolean checkInternet() {
        ConnectivityManager connec = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return wifi.isConnected() || mobile.isConnected();
    }

    /**
     * Elindítja a típusok kiválasztására alkalmas Activity-t, hogy később fogadhassa az eredményeket tőle.
     *
     * @param v
     */
    public void startHobbiesActivtiy(View v) {
        RadioGroup radioG = (RadioGroup) findViewById(R.id.radiusGroup);
        radioG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio1:
                        frequentlyVisited = true;
                        break;
                    case R.id.radio2:
                        frequentlyVisited = false;
                        break;
                }
            }
        });
        Intent intent = new Intent(getApplicationContext(), HobbiesActivity.class);
        startActivityForResult(intent, 10);
    }

    /**
     * A típusok Activity-étből való visszatérés után fogadja a beérkező eredményeket
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        TextView si = (TextView) findViewById(R.id.selectedItems);
        switch (requestCode) {
            case (10): {
                if (resultCode == Activity.RESULT_OK) {
                    selected = data.getStringArrayListExtra("CheckedItems");
                    selectedNames = data.getStringArrayListExtra("checkedNames");
                    si.setText(selectedNames.toString());
                }
            }
        }
    }

    /**
     * A megadott típusok alapján elindítja a helyszínek kereséért felelős Activityt,
     * ha nem adtunk meg egyetlen típust sem, felugró értesítést ad és átnavigál a típusok listájához
     *
     * @param v
     */
    public void startResultlistActivity(View v) {

        if (selected.size() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Válassz helyszínt")
                    .setMessage("Nem adtál meg egyetlen keresendő típust sem! ")
                    .setCancelable(false)
                    .setPositiveButton("Pótolom!",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //dialog.cancel();
                                    Intent intent = new Intent(getApplicationContext(), HobbiesActivity.class);
                                    startActivityForResult(intent, 10);

                                }
                            }
                    );

            AlertDialog alert = builder.create();
            alert.show();
        } else {
            Intent intent = new Intent(getApplicationContext(), ResultlistActivity.class);
            Bundle b = new Bundle();
            String s = orig.getText().toString();
            if (otherOrigin && !s.isEmpty()) {
                LatLng orig = geoLocate(s);
                if (orig != null) {
                    b.putDoubleArray("origin", new double[]{orig.latitude, orig.longitude});
                    b.putStringArrayList("selectedTypes", selected);
                    b.putBoolean("frequency", frequentlyVisited);
                    intent.putExtras(b);
                    startActivity(intent);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setTitle("Hibás adat!")
                            .setMessage("Az általad megadott cím hibás vagy nem létezik! ")
                            .setCancelable(false)
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    }
                            );

                    AlertDialog alert = builder.create();
                    alert.show();
                }
            } else {
                b.putDoubleArray("origin", getCurrentLocation());
                b.putStringArrayList("selectedTypes", selected);
                b.putBoolean("frequency", frequentlyVisited);
                intent.putExtras(b);
                startActivity(intent);
            }

        }
    }

    /**
     * saját helyzetünk meghatározása
     *
     * @return szélességi és hosszúsági koordináták tömb formájában
     */
    public double[] getCurrentLocation() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        String provider = LocationManager.NETWORK_PROVIDER;

        locationManager.requestLocationUpdates(provider, 0, 0, locationListener);

        Location l = locationManager.getLastKnownLocation(provider);
        return new double[]{l.getLatitude(), l.getLongitude()};
    }

    /**
     * A megadott cím geológiai koordinátákká alakítása
     *
     * @param location A kiindulási cím
     * @return szélességi és hosszúsági koordináta a paraméterben kapott címhez
     */
    public LatLng geoLocate(String location) {
        Geocoder gc = new Geocoder(this);
        List<Address> list = null;
        try {
            list = gc.getFromLocationName(location, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!list.isEmpty()) {
            Address addr = list.get(0);
            double lat = addr.getLatitude();
            double lng = addr.getLongitude();

            LatLng ll = new LatLng(lat, lng);
            return ll;
        } else {
            return null;
        }
    }

    /**
     * kiindulási helyszín megadását leehtővé tevő CheckBox állapotainak figyelése
     *
     * @param v
     */
    public void onBoxChecked(View v) {
        if (!otherOrigin) {
            orig.setEnabled(true);
            otherOrigin = true;
        } else {
            orig.setEnabled(false);
            otherOrigin = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    /**
     * Lehetséges egyezéseket keres a kereső mezőbe írt kifejezés töredékre
     * @param input
     * @return Az automatikus kiegészítés által javasolt kifejezések listája
     */
    private ArrayList<String> autocomplete(String input) {
        ArrayList<String> resultList = new ArrayList<String>();

        Document doc = null;

        String uri =
                "https://maps.googleapis.com/maps/api/place/autocomplete/xml?input="+input+"&language=hu&key="+API_KEY;
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
        NodeList nList = doc.getElementsByTagName("description");
        int x = nList.getLength();
        for(int i=0; i<nList.getLength();++i){

            String result = nList.item(i).getFirstChild().getTextContent();

           resultList.add(result);
        }

        return resultList;
    }

    /**
     * Adapter osztály az automatikusan keigészítő szövegmezőhöz
     */
    private class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
        private ArrayList<String> resultList;

        public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        /**
         * Visszaadja a mezőbe írt kifejezeésre (töredékre) kapott találatok számát
         * @return a lista mérete
         */
        @Override
        public int getCount() {
            return resultList.size();
        }

        /**
         * a taláalti lista i-edik elemét adja meg
         * @param index
         * @return az i-edik lista elem
         */
        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        /**
         *
         * @return
         */
        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    }
                    else {
                        notifyDataSetInvalidated();
                    }
                }};
            return filter;
        }
    }

}
