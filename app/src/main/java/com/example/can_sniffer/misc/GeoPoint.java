package com.example.can_sniffer.misc;

import android.hardware.GeomagneticField;
import android.location.Location;
import android.os.Build;

//класс данных с GPS
public class GeoPoint {
    private double longitude;
    private double latitude;
    private double altitude;//широта, долгота, высота
    private double speed;//скорость
    private double bearing;//направление (азимут)
    private double azDop;//точность азимута
    private double hDop;//точность позиции
    private double speedX;//скорость, разложенная по координатам
    private double speedY;//скорость, разложенная по координатам
    private long timeStamp;//таймстамп
    private double declination;
    private double speedDop;//точность скорости
    private double absX;//координаты в абсолютной системе координат
    private double absY;//координаты в абсолютной системе координат

    private GeoPoint(Location location){
        longitude=location.getLongitude();
        latitude=location.getLatitude();
        altitude=location.getAltitude();
        speed=location.getSpeed();
        bearing=location.getBearing();
        hDop=location.getAccuracy();
        double bearingRAD= Math.toRadians(bearing);
        speedY = speed * Math.cos(bearingRAD);
        speedX =  speed * Math.sin(bearingRAD);

        timeStamp = Utils.nano2milli(location.getElapsedRealtimeNanos());
        //timeStamp=location.getTime();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && location.hasSpeedAccuracy()) {//возьмем аккураси из объекта
            speedDop = location.getSpeedAccuracyMetersPerSecond();
        }
        else {//иначе просто приближение
            speedDop = hDop * 0.05;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O&&location.hasBearingAccuracy()) {
            azDop=location.getBearingAccuracyDegrees();
        }
        else
            azDop=-1.0;

        declination = (new GeomagneticField((float)latitude, (float)longitude,
                (float)altitude, System.currentTimeMillis())).getDeclination();//магнитное склонение для получения истинного направления на север
        calcAbsXY();
    }

    public GeoPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        calcAbsXY();
    }

    private void calcAbsXY(){
        this.absX= Coordinates.longitudeToMeters(this.longitude);
        this.absY= Coordinates.latitudeToMeters(this.latitude);
    }

    public static GeoPoint parse(Location location){
        return new GeoPoint(location);
    }

    public double getLongitude(){
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public double getSpeed() {
        return speed;
    }

    public double getBearing() {
        return bearing;
    }

    public double gethDop() {
        return hDop;
    }

    public double getSpeedX() {
        return speedX;
    }

    public double getSpeedY() {
        return speedY;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public double getDeclination() {
        return declination;
    }

    public double getSpeedDop() {
        return speedDop;
    }

    public double getAbsX() {
        return absX;
    }

    public double getAbsY() {
        return absY;
    }

    public double getAzDop() {
        return azDop;
    }
}
