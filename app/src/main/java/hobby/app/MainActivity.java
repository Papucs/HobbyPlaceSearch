package hobby.app;

import android.app.ActionBar;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * kezdő képernyő és funkciók
 */
public class MainActivity extends Activity {
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
    private EditText et;

    /**
     * Grafikai elemek megjelenítése, internet hozzáférés ellenőrzése
     * Amennyiben nem találhatő hálózat, felugró értesítést ad.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setTheme(R.style.AppThemeNoBar);

        setContentView(R.layout.activity_main);


        et = (EditText) findViewById(R.id.otherOrigin);

        if(!checkInternet()){
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
     * @param v
     */
    public void startHobbiesActivtiy(View v){
        RadioGroup radioG = (RadioGroup) findViewById(R.id.radiusGroup);
        radioG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId){
                switch(checkedId){
                    case R.id.radio1:
                        frequentlyVisited =  true;
                        break;
                    case R.id.radio2:
                        frequentlyVisited = false;
                        break;
                }
            }
        });
        Intent intent = new Intent(getApplicationContext(),HobbiesActivity.class);
        startActivityForResult(intent, 10);
    }

    /**
     * A típusok Activity-étből való visszatérés után fogadja a beérkező eredményeket
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        TextView si = (TextView) findViewById(R.id.selectedItems);
        switch(requestCode){
            case (10):{
                if(resultCode == Activity.RESULT_OK){
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
     * @param v
     */
    public void startResultlistActivity(View v){

        if(selected.size()==0){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Válassz helyszínt")
                    .setMessage("Nem adtál eg egyetlen keresendő típust sem! ")
                    .setCancelable(true)
                    .setPositiveButton("Pótolom!",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //dialog.cancel();
                                    Intent intent = new Intent(getApplicationContext(),HobbiesActivity.class);
                                    startActivityForResult(intent, 10);

                                }
                            }
                    );

            AlertDialog alert = builder.create();
            alert.show();
        }else {
            Intent intent = new Intent(getApplicationContext(), ResultlistActivity.class);
            Bundle b = new Bundle();
            String s = et.getText().toString();
            if(otherOrigin && !s.isEmpty()) {
                LatLng orig = geoLocate(s);
                b.putDoubleArray("origin",new double[]{orig.latitude, orig.longitude});
            }else{
                b.putDoubleArray("origin",getCurrentLocation());
            }
            b.putStringArrayList("selectedTypes", selected);
            b.putBoolean("frequency", frequentlyVisited);
            intent.putExtras(b);
            startActivity(intent);
        }
    }

    /**
     * saját helyzetünk meghatározása
     * @return szélességi és hosszúsági koordináták tömb formájában
     */
    public double[] getCurrentLocation(){
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        String provider = LocationManager.NETWORK_PROVIDER;

        locationManager.requestLocationUpdates(provider, 0, 0, locationListener);

        Location l =locationManager.getLastKnownLocation(provider);
        return new double[]{l.getLatitude(), l.getLongitude()};
    }

    /**
     * A megadott cím geológiai koordinátákká alakítása
     * @param location A kiindulási cím
     * @return szélességi és hosszúsági koordináta a paraméterben kapott címhez
     *
     */
    public LatLng geoLocate(String location) {
        Geocoder gc = new Geocoder(this);
        List<Address> list = null;
        try {
            list = gc.getFromLocationName(location, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Address addr = list.get(0);
        double lat = addr.getLatitude();
        double lng = addr.getLongitude();

        LatLng ll = new LatLng(lat, lng);
        return ll;

    }

    /**
     * kiindulási helyszín megadását leehtővé tevő CheckBox állapotainak figyelése
     * @param v
     */
    public void onBoxChecked(View v){
        if(!otherOrigin) {
            et.setEnabled(true);
            otherOrigin = true;
        }else{
            et.setEnabled(false);
            otherOrigin=false;
        }
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

    }

}
