package com.example.can_sniffer.composer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.can_sniffer.CAN.CANDataManager;
import com.example.can_sniffer.CAN.CANPacket;
import com.example.can_sniffer.DataLogger;
import com.example.can_sniffer.filters.LocationCalculator;
import com.example.can_sniffer.misc.GeoPoint;
import com.example.can_sniffer.usb.UsbUartReader;

//класс будет главным собирателем данных с CAN и GPS
public class DataComposer implements LocationListener, UsbUartReader.CANDataListener {
    private LocationManager locationManager;
    private Location lastKnownLocation=null;
    private UsbUartReader arduinoReader=new UsbUartReader();
    private DataLogger logger=new DataLogger();
    private CANdataListener listener;
    private CANDataManager canDataManager=new CANDataManager();
    private LocationCalculator locationCalculator=new LocationCalculator();

    public interface CANdataListener{
        void onDataReady(int ID, int[] data);
        void onStringReady(String text);
        void onStatus(String text);
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
        locationCalculator.update(GeoPoint.parse(location));
    }

    @Override
    public void onCANDataReceived(CANPacket canPacket) {
        canDataManager.setCANPacket(canPacket);
        locationCalculator.predictCAN(canDataManager.getCanWheelSpeed());
        logger.writeData(canPacket, lastKnownLocation, locationCalculator.getPredictedLocationCAN());
        this.listener.onDataReady(canPacket.getID(), canPacket.getData());
    }

    @Override
    public void onStringReceived(String text) {
        listener.onStringReady(text);
    }

    @Override
    public void onStatusListener(String text) {
        listener.onStatus(text);
    }
}
