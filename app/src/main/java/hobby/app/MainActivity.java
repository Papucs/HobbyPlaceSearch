package hobby.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.app.Activity;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Hashtable;


public class MainActivity extends Activity {

    private ArrayList<String> selected = new ArrayList<String>();
    private TextView si ;
    private final Activity thisActivtiy = this;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        si = (TextView) findViewById(R.id.selectedItems);


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

    public boolean checkInternet() {
        ConnectivityManager connec = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        // Check if wifi or mobile network is available or not. If any of them is
        // available or connected then it will return true, otherwise false;
        return wifi.isConnected() || mobile.isConnected();
    }

    public void startHobbiesActivtiy(View v){
        Intent intent = new Intent(getApplicationContext(),HobbiesActivity.class);
        startActivityForResult(intent, 10);
        //startActivity(intent);
    }

    public void startResultlistActivity(View v){

        if(selected.size()==0){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Válassz helyszínt")
                    .setMessage("Nem adtál eg egyetlen keresendő típust sem! ")
                    .setCancelable(true)
                    .setNegativeButton("Mégse",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    //MainActivity.this.finish();
                                }
                            }
                    )
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
            intent.putStringArrayListExtra("selectedTypes", selected);
            startActivity(intent);
        }
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

    @Override
    public void onDestroy(){
        super.onDestroy();

    }

}
