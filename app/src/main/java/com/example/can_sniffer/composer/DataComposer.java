package com.example.can_sniffer.composer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.can_sniffer.DataLogger;
import com.example.can_sniffer.usb.UsbUartReader;

//класс будет главным собирателем данных с CAN и GPS
public class DataComposer implements LocationListener, UsbUartReader.ArduinoCANListener {
    private LocationManager locationManager;
    private Location lastKnownLocation=null;
    private UsbUartReader arduinoReader=new UsbUartReader();
    private DataLogger logger=new DataLogger();
    private CANdataListener listener;

    public interface CANdataListener{
        void onDataReady(int ID, int[] data);
        void onStringReady(String text);
    };

    public DataComposer(){

    }

    public void startWork(Context context, @NonNull CANdataListener listener){
        this.listener=listener;
        logger.startWriter();
        locationManager=(LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0.0f, this);
        }
        arduinoReader.init(context,this);
        arduinoReader.connect();
    }

    public void stopWork(Context context){
        locationManager.removeUpdates(this);
        arduinoReader.release(context);
        logger.closeWriter();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        lastKnownLocation=location;
    }

    @Override
    public void onDataRecieved(int ID, int[] data) {
        logger.writeData(ID, data, lastKnownLocation);
        this.listener.onDataReady(ID, data);
    }

    @Override
    public void onStringRecieved(String text) {
        listener.onStringReady(text);
    }
}
