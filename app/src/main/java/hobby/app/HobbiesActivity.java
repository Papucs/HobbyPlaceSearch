package hobby.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;


public class HobbiesActivity extends FragmentActivity {
    /**
     * A megjelenítendő típusokhoz tartozó angol elnevezések, későbbi API híváshoz szükségesek
     */
    private Map<String,String> types = new HashMap<String, String>();

    private Map<String,String> types2 = new HashMap<String, String>();

    /**
     * A megjelenítendő típusok listája
     */
    private String[] names, names2;

    /**
     * lista
     */
    private ListView listView1, listView2;
    private ListView lv1, lv2;

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

/*
        listView1 = (ListView)findViewById(R.id.hobbyList);
        listView2 = (ListView)findViewById(R.id.ineList);*/

        lv1 = new ListView(this);
        lv2 = new ListView(this);

       Vector<View> pages =new Vector<View>();
        pages.add(lv1);
        pages.add(lv2);

        names = new String[]{"Reptér", "Vidámpark", "Akvárium", "Galéria", "Pékség", "Bár", "Szépségszalon", "Kerékpár üzlet", "Könyvesbolt", "Bowling pálya", "Kávézó", "Autó kölcsönző",
            "Kaszinó","Temető","Templom", "Ruha üzlet", "Virágárus", "Étel", "Bútorbolt", "Edzőterem", "Fodrász", "Barkácsbolt", "Egészség", "Hindu templom", "Ékszer üzlet","Könyvtár", "Italbolt",
            "Videótéka","Mozi","Múzeum","Éjjeli szórakozóhely", "Park", "Kisállat kereskedés", "Kegyhely", "Étterem", "Cipőbolt", "Bevásárló központ", "Spa", "Stadion", "Vasútállomás", "Állatkert"};


        String[] values = new String[]{"airport", "amusement_park","aquarium","art_gallery", "bakery", "bar", "beauty_salon", "bicycle_store", "book_store", "bowling_alley","cafe","car_rental",
                "casino","cemetery","church","clothing_store","florist","food","furniture_store","gym","hair_care","hardware_store","health","hindu_temple","jewelry_store","library","liquor_store",
                "movie_rental","movie_theater","museum","night_club","park","pet_store","place_of_worship","restaurant","shoe_store","shopping_mall","spa","stadium","train_station", "zoo"};

        for(int i=0; i<names.length;++i){
            types.put(names[i],values[i]);
        }

        names2 = new String[]{"Egyetem", "Park", "Vasútállomás"};
        String[] values2 = new String[]{"Egyetem", "Park", "Vasútállomás"};

        for(int i=0; i<names2.length;++i){
            types2.put(names2[i],values2[i]);
        }

        /*
        final ArrayAdapter adapter = new ArrayAdapter(this,
               android.R.layout.simple_list_item_multiple_choice, list);
        listView.setAdapter(adapter);*/

        ViewPager vp = (ViewPager) findViewById(R.id.viewpager);
        CustomPagerAdapter adapter = new CustomPagerAdapter(getApplicationContext(), pages);
        vp.setAdapter(adapter);

        lv1.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice,names));
        lv2.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice,names2));

        lv1.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        lv1.setBackgroundResource(R.drawable.list_bgr);

        lv2.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        lv2.setBackgroundResource(R.drawable.list_bgr);



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
        int len =lv1.getCount();
        SparseBooleanArray c=lv1.getCheckedItemPositions();

        for (int i = 0; i<len; i++)
        {
            if (c.get(i)) {
                String item = types.get(names[i]);
                checkedNames.add(names[i]);
                checked.add(item);

            }
        }

        ArrayList<String> checked2 = new ArrayList<String>();
        ArrayList<String> checkedNames2 = new ArrayList<String>();
        SparseBooleanArray c2=lv2.getCheckedItemPositions();

        for (int i = 0; i<lv2.getCount(); i++)
        {
            if (c2.get(i)) {
                String item = types2.get(names2[i]);
                checkedNames2.add(names2[i]);
                checked2.add(item);

            }
        }


        Intent resultIntent  = new Intent();
        resultIntent.putStringArrayListExtra("checkedNames", checkedNames);
        resultIntent.putStringArrayListExtra("CheckedItems", checked);
        resultIntent.putStringArrayListExtra("checkedNames2", checkedNames2);
        resultIntent.putStringArrayListExtra("CheckedItems2", checked2);
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
