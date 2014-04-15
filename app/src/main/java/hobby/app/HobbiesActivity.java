package hobby.app;

import android.app.ListActivity;
import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HobbiesActivity extends Activity {


    private Map<String,String> pois = new HashMap<String, String>();
    private String[] names, values;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hobbies);
        Intent intent = getIntent();

       listView = (ListView)findViewById(R.id.hobbyList);

        names = new String[]{"Reptér", "Vidámpark", "Akvárium", "Galéria", "Pékség", "Bár", "Szépségszalon", "Kerékpár üzlet", "Könyvesbolt", "Bowling pálya", "Kávézó", "Autó kölcsönző",
            "Kaszinó","Temető","Templom", "Ruha üzlet", "Virágárus", "Étel", "Bútorbolt", "Edzőterem", "Fodrász", "Barkácsbolt", "Egészség", "Hindu templom", "Ékszer üzlet","Könyvtár", "Italbolt",
            "Videótéka","Mozi","Múzeum","Éjjeli szórakozóhely", "Park", "Kisállat kereskedés", "Kegyhely", "Étterem", "Cipőbolt", "Bevásárló központ", "Spa", "Stadion", "Vasútállomás", "Állatkert"};


        values = new String[]{"airport", "amusement_park","aquarium","art_gallery", "bakery", "bar", "beauty_salon", "bicycle_store", "book_store", "bowling_alley","cafe","car_rental",
                "casino","cemetery","church","clothing_store","florist","food","furniture_store","gym","hair_care","hardware_store","health","hindu_temple","jewelry_store","library","liquor_store",
                "movie_rental","movie_theater","museum","night_club","park","pet_store","place_of_worship","restaurant","shoe_store","shopping_mall","spa","stadium","train_station", "zoo"};

        for(int i=0; i<names.length;++i){
            pois.put(names[i],values[i]);
        }

        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < names.length; ++i) {
            list.add(names[i]);
        }
        final ArrayAdapter adapter = new ArrayAdapter(this,
               android.R.layout.simple_list_item_multiple_choice, list);
        listView.setAdapter(adapter);
    }

    public void getSelectedItems(View v)
    {
        //---toggle the check displayed next to the item---
        ArrayList<String> checked = new ArrayList<String>();
        int len =listView.getCount();
        SparseBooleanArray c=listView.getCheckedItemPositions();

        for (int i = 0; i<len; i++)
        {
            if (c.get(i)) {
                String item = pois.get(names[i]);
                checked.add(item);

            }
        }
        Intent resultIntent  = new Intent();
        resultIntent.putStringArrayListExtra("CheckedItems", checked);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
        //Toast.makeText(this,"Selected Items- " + s,Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hobbies, menu);
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

}
