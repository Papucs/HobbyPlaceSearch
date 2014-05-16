package hobby.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class HobbiesActivity extends Activity {

    /**
     * A megjelenítendő típusokhoz tartozó angol elnevezések, későbbi API híváshoz szükségesek
     */
    private Map<String,String> types = new HashMap<String, String>();

    /**
     * A megjelenítendő típusok listája
     */
    private String[] names;

    /**
     * lista
     */
    private ListView listView;

    /**
     * fejléc kirajzolása
     */
    private void showActionBar() {
        LayoutInflater inflator = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.list_ab, null);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled (false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(v);
    }

    /**
     *grafikus elemek kirajzolása
     * a szülő Activitytől érkező adatok olvasása
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hobbies);
        showActionBar();
        Intent intent = getIntent();


       listView = (ListView)findViewById(R.id.hobbyList);

        names = new String[]{"Reptér", "Vidámpark", "Akvárium", "Galéria", "Pékség", "Bár", "Szépségszalon", "Kerékpár üzlet", "Könyvesbolt", "Bowling pálya", "Kávézó", "Autó kölcsönző",
            "Kaszinó","Temető","Templom", "Ruha üzlet", "Virágárus", "Étel", "Bútorbolt", "Edzőterem", "Fodrász", "Barkácsbolt", "Egészség", "Hindu templom", "Ékszer üzlet","Könyvtár", "Italbolt",
            "Videótéka","Mozi","Múzeum","Éjjeli szórakozóhely", "Park", "Kisállat kereskedés", "Kegyhely", "Étterem", "Cipőbolt", "Bevásárló központ", "Spa", "Stadion", "Vasútállomás", "Állatkert"};


        String[] values = new String[]{"airport", "amusement_park","aquarium","art_gallery", "bakery", "bar", "beauty_salon", "bicycle_store", "book_store", "bowling_alley","cafe","car_rental",
                "casino","cemetery","church","clothing_store","florist","food","furniture_store","gym","hair_care","hardware_store","health","hindu_temple","jewelry_store","library","liquor_store",
                "movie_rental","movie_theater","museum","night_club","park","pet_store","place_of_worship","restaurant","shoe_store","shopping_mall","spa","stadium","train_station", "zoo"};

        for(int i=0; i<names.length;++i){
            types.put(names[i],values[i]);
        }

        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < names.length; ++i) {
            list.add(names[i]);
        }
        final ArrayAdapter adapter = new ArrayAdapter(this,
               android.R.layout.simple_list_item_multiple_choice, list);
        listView.setAdapter(adapter);
    }

    /**
     * "Vissza" gomb lenyomásának eventje
     * @param v
     */
    public void back(View v){
        onBackPressed();
    }

    /**
     * A listában kiválasztott elemek kiválasztása és továbbítása a meghívásra ekrülő Activitynek
     * @param v
     */
    public void getSelectedItems(View v)
    {
        ArrayList<String> checked = new ArrayList<String>();
        ArrayList<String> checkedNames = new ArrayList<String>();
        int len =listView.getCount();
        SparseBooleanArray c=listView.getCheckedItemPositions();

        for (int i = 0; i<len; i++)
        {
            if (c.get(i)) {
                String item = types.get(names[i]);
                checkedNames.add(names[i]);
                checked.add(item);

            }
        }
        Intent resultIntent  = new Intent();
        resultIntent.putStringArrayListExtra("checkedNames", checkedNames);
        resultIntent.putStringArrayListExtra("CheckedItems", checked);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    public void onResume(){
        super.onResume();
    }

    public void onStop(){
        super.onStop();
    }

}
