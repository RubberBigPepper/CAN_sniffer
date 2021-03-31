package com.example.can_sniffer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.can_sniffer.composer.DataComposer;
import com.example.can_sniffer.misc.BinaryParserData;
import com.example.can_sniffer.obd.OBDManager;
import com.example.can_sniffer.view.DataAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{
    public static final int PERMISSION_REQUEST_CODE = 12367;

    public static OBDManager obdManager=null;
    private DataAdapter adapter;
    private DataComposer dataComposer=new DataComposer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     //   BinaryParserData.testParse();
        CheckPermissionsOrRun();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()){
   //         obdManager.StopWork();
            dataComposer.stopWork(this);
        }
    }

    private void showContent(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        adapter=new DataAdapter();
        RecyclerView recyclerView=findViewById(R.id.recyclerView);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(adapter);
        dataComposer.startWork(this, canDataListener);
     /*   obdManager=new OBDManager(obdSensorListener);
        obdManager.StartWork();*/
    }

    private void CheckPermissionsOrRun() {
        ArrayList<String> strArNeedPermission = new ArrayList<String>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            strArNeedPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED)
            strArNeedPermission.add(Manifest.permission.ACCESS_NETWORK_STATE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED)
            strArNeedPermission.add(Manifest.permission.BLUETOOTH);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED)
            strArNeedPermission.add(Manifest.permission.BLUETOOTH_ADMIN);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            strArNeedPermission.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            strArNeedPermission.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (strArNeedPermission.size() > 0) {
            //спрашиваем пермишен у пользователя
            requestPermission(strArNeedPermission);
        } else {
            showContent();
        }
    }

    public void requestPermission(ArrayList<String> strArNeedPermission) {
        String[] strArList = new String[strArNeedPermission.size()];
        strArNeedPermission.toArray(strArList);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, strArList, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0) {
            boolean bWrite = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            boolean bNetwork = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED;
            boolean bBT = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED;
            boolean bBTAdmin = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED;
            boolean bCoarse = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            boolean bFine = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            for (int n = 0; n < permissions.length; n++) {
                if (grantResults[n] == PackageManager.PERMISSION_GRANTED) {
                    if (permissions[n].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                        bWrite = true;
                    if (permissions[n].equals(Manifest.permission.ACCESS_NETWORK_STATE))
                        bNetwork = true;
                    if (permissions[n].equals(Manifest.permission.BLUETOOTH))
                        bBT = true;
                    if (permissions[n].equals(Manifest.permission.BLUETOOTH_ADMIN))
                        bBTAdmin = true;
                    if (permissions[n].equals(Manifest.permission.ACCESS_COARSE_LOCATION))
                        bCoarse = true;
                    if (permissions[n].equals(Manifest.permission.ACCESS_FINE_LOCATION))
                        bFine = true;
                }
            }
            if (bWrite &&  bNetwork && bBT && bBTAdmin&&(bFine||bCoarse)) {
                showContent();
            } else {
                AlertDialog.Builder cBuilder = new AlertDialog.Builder(this);
                cBuilder.setTitle(R.string.app_name);
                cBuilder.setMessage("Не все нужные разрешения даны программе. Программа завершается");
                cBuilder.setCancelable(false);
                cBuilder.create().show();
                cBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                });
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private DataComposer.CANdataListener canDataListener=new DataComposer.CANdataListener() {
        @Override
        public void onDataReady(int ID, int[] data) {
            StringBuffer buffer=new StringBuffer();
            for (int value:data){
                buffer.append(String.format("%02X",value));
                buffer.append(";");
            }
            adapter.setItemValue(String.format("%04X;", ID), buffer.toString(), false);
            runOnUiThread(updaterUI);
        }

        @Override
        public void onStringReady(String text) {
            adapter.setItemValue(text,null, false);
            runOnUiThread(updaterUI);
        }

        @Override
        public void onStatus(String text) {
            Toast.makeText(MainActivity.this,text, Toast.LENGTH_SHORT).show();
        }

        private Runnable updaterUI=new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        };
    };

    private OBDManager.OBDSensorListener obdSensorListener=new OBDManager.OBDSensorListener() {
        @Override
        public void onErrorConnection() {

        }

        @Override
        public void onErrorOBDInit() {

        }

        @Override
        public void onOneReadTickEnd(String data) {
            if (data.length()>0) {
                String mask = data.substring(0, 4);
                data = data.substring(4);
                adapter.setItemValue(mask,data);
            }
            else
                adapter.setItemValue("short",data);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onConnectionLost() {

        }

        @Override
        public void onOBDConnected() {

        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Resources cRes = getResources();
        switch (item.getItemId()) {
            case R.id.menuSettings:
                startActivity(new Intent(this, OBDSettingsActivity.class));
                return true;
        }
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if(intent.getAction().equals("android.hardware.usb.action.USB_DEVICE_ATTACHED")) {
            dataComposer.stopWork(this);
            dataComposer.startWork(this, canDataListener);
            //uartReader.connect();
        }
        super.onNewIntent(intent);
    }

}