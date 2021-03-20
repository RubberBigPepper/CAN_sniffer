package com.example.can_sniffer.filters;

import com.example.can_sniffer.misc.Matrix;

public class LocationSpeedKF {//этот фильтр будет по скорости определять местоположение
    public static final String TAG = "LocationSpeedKF";

    private long m_timeStampMsPredict;
    private long m_timeStampMsUpdate;
    private int m_predictCount;
    private KalmanFilter m_kf;
    private double m_spdSigma=0.5;//видится такое приближение к шуму скорости
    private double mPosFactor = 0.1;

    public LocationSpeedKF(double x, double y,
                                double spdDev, double posDev,
                           long timeStampMs,
                                double posFactor) {
        int mesDim = 2;

        m_kf = new KalmanFilter(2, mesDim, 2);
        m_timeStampMsPredict = m_timeStampMsUpdate = timeStampMs;
        m_spdSigma = spdDev;
        m_predictCount = 0;
        m_kf.Xk_k.setData(x, y);

        m_kf.H.setIdentityDiag(); //state has 4d and measurement has 4d too. so here is identity
        m_kf.Pk_k.setIdentity();
        m_kf.Pk_k.scale(posDev);
        mPosFactor = posFactor;
    }

    private void rebuildF(double dtPredict) {
        /*double f[] = {
                1.0, 0.0, dtPredict, 0.0,
                0.0, 1.0, 0.0, dtPredict,
                0.0, 0.0, 1.0, 0.0,
                0.0, 0.0, 0.0, 1.0
        };*/
        double f[] = {
                1.0, 0.0,
                0.0, 1.0,
        };
        m_kf.F.setData(f);
    }

    private void rebuildU(double xVel,
                          double yVel) {
        m_kf.Uk.setData(xVel, yVel);
    }

    private void rebuildB(double dtPredict) {
        /*double dt2 = 0.5*dtPredict*dtPredict;
        double b[] = {
                dt2, 0.0,
                0.0, dt2,
                dtPredict, 0.0,
                0.0, dtPredict
        };
                m_kf.B.setData(b);
*/
        m_kf.B.setIdentity();
        m_kf.B.scale(dtPredict);
    }

    private void rebuildR(double posSigma) {

        posSigma *= mPosFactor;//так в работе

        //posSigma *= posSigma * mPosFactor;//так в теории

        m_kf.R.setIdentity();
        m_kf.R.scale(posSigma);
    }

    private void rebuildQ(double dtUpdate,
                          double velDev) {
//        now we use predictCount. but maybe there is way to use dtUpdate.
        //m_kf.Q.setIdentity();
        //m_kf.Q.scale(accSigma * dtUpdate);

//        double velDev = accDev * m_predictCount;//так было в оригинальной работе
  //      double posDev = velDev * m_predictCount / 2;

        double posDev = velDev * dtUpdate;//так в теории
        double posSig = posDev * posDev;

        double Q[] = { //так изначально в работе
                posSig, 0.0,
                0.0, posSig
        };

        m_kf.Q.setData(Q);
    }

    public void predict(long timeNowMs,
                        double xVel,
                        double yVel) {
        double dtPredict = (timeNowMs - m_timeStampMsPredict) / 1000.0;
        double dtUpdate = (timeNowMs - m_timeStampMsUpdate) / 1000.0;
        rebuildF(dtPredict);
        rebuildB(dtPredict);
        rebuildU(xVel, yVel);

        ++m_predictCount;
        rebuildQ(dtUpdate, m_spdSigma);

        m_timeStampMsPredict = timeNowMs;
        m_kf.predict();
        Matrix.matrixCopy(m_kf.Xk_km1, m_kf.Xk_k);
    }

    public void update(long timeStamp,
                       double x,
                       double y,
                       double posDev) {
        m_predictCount = 0;
        m_timeStampMsUpdate = timeStamp;
        rebuildR(posDev);
        m_kf.Zk.setData(x, y);
        m_kf.update();
    }

    public double getCurrentX() {
        return m_kf.Xk_k.data[0][0];
    }
    public double getCurrentY() {
        return m_kf.Xk_k.data[1][0];
    }

    public double getCurrentXVel() {
        return m_kf.Uk.data[0][0];
    }
    public double getCurrentYVel() {
        return m_kf.Uk.data[1][0];
    }
}
