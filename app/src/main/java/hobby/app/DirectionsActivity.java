package hobby.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Az útvonal szöveges megjelenítéséért felelős Activity
 */
public class DirectionsActivity extends Activity {
    /**
     * az útvonal egyes pontjait tartalmazó lista
     */
    private ArrayList<String> directions = new ArrayList<String>();

    /**
     * fejléc megjlenítése
     */
    private void showActionBar() {
        LayoutInflater inflator = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.directions_ab, null);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled (false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(v);
    }

    /**
     * grafikus elemek kirajzolása, a szülő Activtytől érkező adatok fogadása
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);
        showActionBar();

        ListView listView = (ListView)findViewById(R.id.directionsList);

        Intent intent = getIntent();
        directions=intent.getStringArrayListExtra("directionsList");

        final ArrayAdapter adapter = new ArrayAdapter(getBaseContext(),
                android.R.layout.simple_list_item_1,directions);

        listView.setAdapter(adapter);
    }

    /**
     * Visszatérés a térképes megjelenítséhez
     * @param v
     */
    public void getBackMap(View v){
        finish();
    }

    public void onStop(){
        super.onStop();
    }

    public void onResume(){
        super.onResume();
    }

}
