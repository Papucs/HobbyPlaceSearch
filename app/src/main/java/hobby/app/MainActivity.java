package hobby.app;

import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class MainActivity extends Activity {

    private ArrayList<String> selected = new ArrayList<String>();
    private TextView si ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        si = (TextView) findViewById(R.id.selectedItems);
    }

    public void startHobbiesActivtiy(View v){
        Intent intent = new Intent(getApplicationContext(),HobbiesActivity.class);
        startActivityForResult(intent, 10);
        //startActivity(intent);
    }

    public void startResultlistActivity(View v){
        Intent intent = new Intent(getApplicationContext(), ResultlistActivity.class);
        intent.putStringArrayListExtra("selectedTypes",selected);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        switch(requestCode){
            case (10):{
                if(resultCode == Activity.RESULT_OK){
                    selected = data.getStringArrayListExtra("CheckedItems");
                    si.setText(selected.toString());
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
