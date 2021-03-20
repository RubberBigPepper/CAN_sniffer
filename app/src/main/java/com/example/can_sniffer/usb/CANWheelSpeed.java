package com.example.can_sniffer.usb;

//класс будет вычислять азимут движения и скорости по осям, основываясь на скорости по колесам
public class CANWheelSpeed {
    private double azimuthGPS = 0.0;//азимут движения с ГНСС, в радианах

    private double baseFront = 1.55;//база передних колес
    private double baseRear = 1.55;//база задних колес
    private double weightFront = 0.5;//весовой коэффициент передней оси в расчетах
    private double weightRear = 0.5;//весовой коэффициент задней оси в расчетах

    private double speedX = 0.0;//разложение вектора скорости по осям
    private double speedY = 0.0;
    private double speed = 0.0;//общая скорость
    private double azimuth = 0.0;//азимут движения

    private long prevTime = 0;//предыдущая метка времени, для расчета приращения

    //получение векторов скорости, если даны скорости с колес (в м/с)
    //time - метка времени, в миллисекундах,
    // speedFL,speedFR,speedRL, speedRR - мгновенные скорости колес в м/с
    public void calc(long time, double speedFL, double speedFR, double speedRL, double speedRR) {
        if (prevTime == 0) {//для первого значения вычислять не из чего
            prevTime = time;
            return;
        }
        double deltaTime = 0.001 * (time - prevTime);
        double angleRear = (speedRL - speedRR) / baseRear;//мгновенный угол
        double angleFront = (speedFL - speedFR) / baseFront;//поворота осей, в радианах
        azimuth = deltaTime * (angleFront * weightFront + angleRear * weightRear) + azimuthGPS;//угол поворота автомобиля в мировой системе координат
        speed = 0.5 * ((speedFL + speedFR) * weightFront + (speedRL + speedRR) * weightRear) * deltaTime;//средняя скорость движения автомобиля
        speedX = Math.sin(azimuth) * speed;
        speedY = Math.cos(azimuth) * speed;
        prevTime = time;
    }

    public double getAzimuthGPS() {
        return azimuthGPS;
    }

    public void setAzimuthGPS(double azimuthGPS) {
        this.azimuthGPS = azimuthGPS;
    }

    //если азимут от GPS задан градусами
    public void setAzimuthGPSdeg(double azimuthGPSdeg) {
        this.azimuthGPS = Math.PI * azimuthGPSdeg / 180.0;
    }

    public double getBaseFront() {
        return baseFront;
    }

    public void setBaseFront(double baseFront) {
        this.baseFront = baseFront;
    }

    public double getBaseRear() {
        return baseRear;
    }

    public void setBaseRear(double baseRear) {
        this.baseRear = baseRear;
    }

    public double getWeightFront() {
        return weightFront;
    }

    public void setWeightFront(double weightFront) {
        this.weightFront = weightFront;
    }

    public double getWeightRear() {
        return weightRear;
    }

    public void setWeightRear(double weightRear) {
        this.weightRear = weightRear;
    }

    public double getSpeedX() {
        return speedX;
    }

    public double getSpeedY() {
        return speedY;
    }

    public double getSpeed() {
        return speed;
    }

    public double getAzimuth() {
        return azimuth;
    }

    public long getPrevTime() {
        return prevTime;
    }
}
