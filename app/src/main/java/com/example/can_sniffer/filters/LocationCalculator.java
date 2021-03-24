package com.example.can_sniffer.filters;

import android.location.Location;

import com.example.can_sniffer.misc.Coordinates;
import com.example.can_sniffer.misc.Utils;
import com.example.can_sniffer.misc.GeoPoint;
import com.example.can_sniffer.CAN.CANWheelSpeed;

public class LocationCalculator {
    private double declinationPrev = 0.0f;//магнитное склонение
    private GeoPoint lastGeoPoint = null;
    private LocationSpeedKF locSpdKFCAN;
    private double longitudeKoef = 1.0;//коэффициент на сколько нужно умножить координату по X (по долготе) из-за уменьшения длины окружности в зависимости от широты
    private boolean canUpdate=true;

    public LocationCalculator() {
    }

    private void makeLongitudeKoef() {//расчет увеличения коэффициента по глобальному X, в зависимости от уменьшения длины окружности долготы от градуса широты
        if (lastGeoPoint == null)
            longitudeKoef = 1.0;
        double latLeng = Math.cos(Math.toRadians(lastGeoPoint.getLatitude()));
        if (latLeng != 0.0d)//при нуле (на полюсах) неопределенное значение
            longitudeKoef = 1.0 / latLeng;
    }

    public void predictCAN(CANWheelSpeed canWheelSpeed) {
        if (locSpdKFCAN != null) {
            locSpdKFCAN.predict(canWheelSpeed.getPrevTime(), canWheelSpeed.getSpeedX()*longitudeKoef, canWheelSpeed.getSpeedY());
            canUpdate=true;
        }
    }

    public void update(GeoPoint point) {
        if(!canUpdate)
            return;
        canUpdate=false;
        this.declinationPrev = Math.toRadians(point.getDeclination());
        lastGeoPoint = point;
        makeLongitudeKoef();

        if (locSpdKFCAN == null) {
            locSpdKFCAN = new LocationSpeedKF(
                    point.getAbsX(),
                    point.getAbsY(),
                    Utils.DEFAULT_SPD_FACTOR,
                    point.gethDop(),
                    point.getTimeStamp(),
                    Utils.DEFAULT_POS_FACTOR
            );
        } else {
            locSpdKFCAN.update(point.getTimeStamp(), point.getAbsX(), point.getAbsY(), point.gethDop());
        }
    }

    public Location getPredictedLocationCAN() {
        if (lastGeoPoint == null || locSpdKFCAN == null)
            return null;
        Location loc = new Location("CAN");
        GeoPoint predictedPoint = Coordinates.metersToGeoPoint(locSpdKFCAN.getCurrentX(),
                locSpdKFCAN.getCurrentY());
        loc.setLatitude(predictedPoint.getLatitude());
        loc.setLongitude(predictedPoint.getLongitude());
        loc.setAltitude(lastGeoPoint.getAltitude());
        double speedX = locSpdKFCAN.getCurrentXVel();
        double speedY = locSpdKFCAN.getCurrentYVel();
        double speed = Math.sqrt(speedX * speedX + speedY * speedY); //scalar speed without bearing
        loc.setBearing((float) lastGeoPoint.getBearing());
        loc.setSpeed((float) speed);
        loc.setTime(System.currentTimeMillis());
        loc.setElapsedRealtimeNanos(System.nanoTime());
        loc.setAccuracy((float) lastGeoPoint.gethDop());
        return loc;
    }

    public double getDeclinationPrev() {
        return declinationPrev;
    }
}
