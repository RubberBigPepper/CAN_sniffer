package com.example.can_sniffer.misc;


public class Quaternion {//класс кватерниона и математики кватернионов
    public double W = 1.0;//косинус поворота (скалярная часть)
    public double X, Y, Z;//данные по осям

    public Quaternion(final double w,
                      final double x,
                      final double y,
                      final double z) {
        this.W = w;
        this.X = x;
        this.Y = y;
        this.Z = z;
        //  normalize();
    }

    /**
     * Returns the conjugate quaternion of the instance.
     *
     * @return the conjugate quaternion
     */
    public Quaternion getConjugate() {
        return new Quaternion(W, -X, -Y, -Z);
    }

    /**
     * Returns the Hamilton product of two quaternions.
     *
     * @param q1 First quaternion.
     * @param q2 Second quaternion.
     * @return the product {@code q1} and {@code q2}, in that order.
     */
    public static Quaternion multiply(final Quaternion q1, final Quaternion q2) {
        // Components of the product.
        final double w = q1.W * q2.W - q1.X * q2.X - q1.Y * q2.Y - q1.Z * q2.Z;
        final double x = q1.W * q2.X + q1.X * q2.W + q1.Y * q2.Z - q1.Z * q2.Y;
        final double y = q1.W * q2.Y - q1.X * q2.Z + q1.Y * q2.W + q1.Z * q2.X;
        final double z = q1.W * q2.Z + q1.X * q2.Y - q1.Y * q2.X + q1.Z * q2.W;

        return new Quaternion(w, x, y, z);
    }

    //умножение кватерниона на вектор или кватернион
    public static Quaternion multiply(final Quaternion q1, final double[] vector) {
        switch (vector.length) {
            case 4://дан не вектор,а кватернион
                return multiply(q1, new Quaternion(vector[0], vector[1], vector[2], vector[3]));
            case 3://дан вектор
                return multiply(q1, vector[0], vector[1], vector[2]);
            default:
                return null;
        }
    }

    //умножение кватерниона на вектор
    public static Quaternion multiply(final Quaternion q1,
                                      final double X, final double Y, final double Z) {
        return multiply(q1, new Quaternion(0.0, X, Y, Z));
    }

    /**
     * Returns the Hamilton product of the instance by a quaternion.
     *
     * @param q Quaternion.
     * @return the product of this instance with {@code q}, in that order.
     */
    public Quaternion multiply(final Quaternion q) {
        return multiply(this, q);
    }

    /**
     * Computes the sum of two quaternions.
     *
     * @param q1 Quaternion.
     * @param q2 Quaternion.
     * @return the sum of {@code q1} and {@code q2}.
     */
    public static Quaternion add(final Quaternion q1, final Quaternion q2) {
        return new Quaternion(q1.W + q2.W, q1.X + q2.X, q1.Y + q2.Y, q1.Z + q2.Z);
    }

    /**
     * Computes the sum of the instance and another quaternion.
     *
     * @param q Quaternion.
     * @return the sum of this instance and {@code q}
     */
    public Quaternion add(final Quaternion q) {
        return add(this, q);
    }

    /**
     * Subtracts two quaternions.
     *
     * @param q1 First Quaternion.
     * @param q2 Second quaternion.
     * @return the difference between {@code q1} and {@code q2}.
     */
    public static Quaternion subtract(final Quaternion q1, final Quaternion q2) {
        return new Quaternion(q1.W - q2.W, q1.X - q2.X, q1.Y - q2.Y, q1.Z - q2.Z);
    }

    /**
     * Subtracts a quaternion from the instance.
     *
     * @param q Quaternion.
     * @return the difference between this instance and {@code q}.
     */
    public Quaternion subtract(final Quaternion q) {
        return subtract(this, q);
    }

    /**
     * Computes the dot-product of two quaternions.
     *
     * @param q1 Quaternion.
     * @param q2 Quaternion.
     * @return the dot product of {@code q1} and {@code q2}.
     */
    public static double dotProduct(final Quaternion q1,
                                    final Quaternion q2) {
        return q1.W * q2.W + q1.X * q2.X + q1.Y * q2.Y + q1.Z * q2.Z;
    }

    /**
     * Computes the dot-product of the instance by a quaternion.
     *
     * @param q Quaternion.
     * @return the dot product of this instance and {@code q}.
     */
    public double dotProduct(final Quaternion q) {
        return dotProduct(this, q);
    }

    /**
     * Computes the norm of the quaternion.
     *
     * @return the norm.
     */
    public double getNorm() {
        return Math.sqrt(W * W + X * X + Y * Y + Z * Z);
    }

    //поворот (трансформация вектора) кватернионом
    public static double[] quatVectorTransform(Quaternion q, double[] vector) {
        if (vector.length >= 3) {//только для трехмерных векторов!
            Quaternion resQ = multiply(q, vector);
            resQ = multiply(resQ, q.getInverse());
            return new double[]{resQ.X, resQ.Y, resQ.Z};
        }
        return null;
    }

    //поворот (трансформация вектора) кватернионом
    public static double[] quatVectorTransform(Quaternion q, float[] vector) {
        if (vector.length >= 3) {//только для трехмерных векторов!
            Quaternion resQ = multiply(q, vector[0], vector[1], vector[2]);
            resQ = multiply(resQ, q.getInverse());
            return new double[]{resQ.X, resQ.Y, resQ.Z};
        }
        return null;
    }

    /**
     * Computes the normalized quaternion (the versor of the instance).
     * The norm of the quaternion must not be zero.
     *
     * @return a normalized quaternion.
     * @throws Exception if the norm of the quaternion is zero.
     */
    public void normalize() {
        double norm = getNorm();
        if (norm != 0.0) {
            W /= norm;
            X /= norm;
            Y /= norm;
            Z /= norm;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof Quaternion) {
            final Quaternion q = (Quaternion) other;
            return W == q.W && X == q.X && Y == q.Y && Z == q.Z;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        // "Effective Java" (second edition, p. 47).
        int result = 17;
        for (double comp : new double[]{W, X, Y, Z}) {
            final int c = Double.hashCode(comp);
            result = 31 * result + c;
        }
        return result;
    }

    private boolean isEqualEps(final double v, final double w, final double eps) {
        return Math.abs(v - w) <= eps;
    }

    /**
     * Checks whether this instance is equal to another quaternion
     * within a given tolerance.
     *
     * @param q   Quaternion with which to compare the current quaternion.
     * @param eps Tolerance.
     * @return {@code true} if the each of the components are equal
     * within the allowed absolute error.
     */
    public boolean equals(final Quaternion q, final double eps) {
        return isEqualEps(W, q.W, eps) &&
                isEqualEps(X, q.X, eps) &&
                isEqualEps(Y, q.Y, eps) &&
                isEqualEps(Z, q.Z, eps);
    }

    /**
     * Checks whether the instance is a unit quaternion within a given
     * tolerance.
     *
     * @param eps Tolerance (absolute error).
     * @return {@code true} if the norm is 1 within the given tolerance,
     * {@code false} otherwise
     */
    public boolean isUnitQuaternion(double eps) {
        return isEqualEps(getNorm(), 1d, eps);
    }

    /**
     * Checks whether the instance is a pure quaternion within a given
     * tolerance.
     *
     * @param eps Tolerance (absolute error).
     * @return {@code true} if the scalar part of the quaternion is zero.
     */
    public boolean isPureQuaternion(double eps) {
        return Math.abs(W) <= eps;
    }


    /**
     * Returns the inverse of this instance.
     * The norm of the quaternion must not be zero.
     *
     * @return the inverse.
     */
    public Quaternion getInverse() {
        final double squareNorm = W * W + X * X + Y * Y + Z * Z;

        return new Quaternion(W / squareNorm,
                -X / squareNorm,
                -Y / squareNorm,
                -Z / squareNorm);
    }

    /**
     * Gets the scalar part of the quaternion.
     *
     * @return the scalar part.
     */
    public double getScalarPart() {
        return W;
    }

    /**
     * Gets the three components of the vector part of the quaternion.
     *
     * @return the vector part.
     */
    public double[] getVectorPart() {
        return new double[]{X, Y, Z};
    }

    public double getVectorPartLength() {
        return Math.sqrt(X * X + Y * Y + Z * Z);
    }

    /**
     * Multiplies the instance by a scalar.
     *
     * @param alpha Scalar factor.
     * @return a scaled quaternion.
     */
    public Quaternion multiply(final double alpha) {
        return new Quaternion(alpha * W,
                alpha * X,
                alpha * Y,
                alpha * Z);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final String sp = " ";
        final StringBuilder s = new StringBuilder();
        s.append("[")
                .append(W).append(sp)
                .append(X).append(sp)
                .append(Y).append(sp)
                .append(Z)
                .append("]");

        return s.toString();
    }

    public double[] toEulerAngles() {
        double[] e = new double[3];
        double sqw = W * W;
        double sqx = X * X;
        double sqy = Y * Y;
        double sqz = Z * Z;
        //на самом деле это углы А.Н. Крылова, а не Эйлера
        /*e[0] = Math.atan2(sqw - sqx - sqy + sqz, 2.0 * (W * X - Z * Y));
        e[1] = Math.asin(2.0 * (W * Y - X * Z));
        e[2] = Math.atan2(sqw + sqx - sqy - sqz, 2.0 * (W * Z - X * Y));*/

        //а это Эйлера
        e[0] = -Math.atan2(W * X - Y * Z, W * Y + X * Z); //это yaw - поворот вокруг глобальной оси Z, по умолчанию считается  против часовой стрелки, а азимут по - поэтому знак минус
        e[1] = Math.acos(sqw - sqx - sqy + sqz);
        e[2] = Math.atan2(W * X + Y * Z, W * Y - X * Z);

        //e[0] = Math.atan2(2.0 * (Y * Z + X * W), 1.0 - 2.0 * (sqx + sqy)); // -sqx - sqy + sqz + sqw);
        //e[1] = Math.asin(-2.0 * (X * Z - Y * W));
        //e[2] = Math.atan2(2.0 * (X * Y + Z * W), 1.0 - 2.0 * (sqy + sqz)); //sqx - sqy - sqz + sqw);
        return e;
    }

    //единичный кватернион - скаляр
    public static Quaternion identityQuaternion() {
        return new Quaternion(1.0, 0.0, 0.0, 0.0);
    }

    //кватернион поворота от вектора from (f1,f2,f3) к вектору to (t1,t2,t3)
    public static Quaternion getRotatingFromTo(double[] from, double[] to) {
        if (from.length != 3 || to.length != 3)//работаем только для трехмерных векторов
            return identityQuaternion();
        Quaternion fromQ = new Quaternion(0.0, -from[0], -from[1], -from[2]);//это сопряженный (conjuate) кватернион
        fromQ.normalize();
        Quaternion toQ = new Quaternion(0.0, to[0], to[1], to[2]);
        toQ.normalize();
        Quaternion v = multiply(toQ, fromQ);
        double W = Math.sqrt((1.0 + v.W) * 0.5);
        double vecLeng = v.getVectorPartLength();
        if (vecLeng == 0.0)
            return identityQuaternion();
        vecLeng = Math.sqrt((1.0 - v.W) * 0.5) / vecLeng;
        Quaternion res=new Quaternion(W, v.X * vecLeng, v.Y * vecLeng, v.Z * vecLeng);
        res.normalize();
        return res;
    }
}
