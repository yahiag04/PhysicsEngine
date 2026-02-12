package it.unibs.pajc.core;

import java.io.Serializable;

public record Vector2D(double x, double y) implements Serializable {

    public static final Vector2D ZERO = new Vector2D(0, 0);
    public static final Vector2D UNIT_X = new Vector2D(1, 0);
    public static final Vector2D UNIT_Y = new Vector2D(0, 1);

    public static Vector2D zero() {
        return ZERO;
    }

    public static Vector2D fromPolar(double magnitude, double angleRadians) {
        return new Vector2D(
            magnitude * Math.cos(angleRadians),
            magnitude * Math.sin(angleRadians)
        );
    }

    public Vector2D add(Vector2D other) {
        return new Vector2D(x + other.x, y + other.y);
    }

    public Vector2D subtract(Vector2D other) {
        return new Vector2D(x - other.x, y - other.y);
    }

    public Vector2D multiply(double scalar) {
        return new Vector2D(x * scalar, y * scalar);
    }

    public Vector2D divide(double scalar) {
        if (scalar == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        return new Vector2D(x / scalar, y / scalar);
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public double magnitudeSquared() {
        return x * x + y * y;
    }

    public Vector2D normalize() {
        double mag = magnitude();
        if (mag == 0) {
            return ZERO;
        }
        return divide(mag);
    }

    public double dot(Vector2D other) {
        return x * other.x + y * other.y;
    }

    public double cross(Vector2D other) {
        return x * other.y - y * other.x;
    }

    public Vector2D rotate(double angleRadians) {
        double cos = Math.cos(angleRadians);
        double sin = Math.sin(angleRadians);
        return new Vector2D(
            x * cos - y * sin,
            x * sin + y * cos
        );
    }

    public double angle() {
        return Math.atan2(y, x);
    }

    public double angleTo(Vector2D other) {
        return Math.atan2(other.y - y, other.x - x);
    }

    public Vector2D perpendicular() {
        return new Vector2D(-y, x);
    }

    public Vector2D reflect(Vector2D normal) {
        Vector2D n = normal.normalize();
        return this.subtract(n.multiply(2 * this.dot(n)));
    }

    public double distanceTo(Vector2D other) {
        return this.subtract(other).magnitude();
    }

    public Vector2D negate() {
        return new Vector2D(-x, -y);
    }

    public Vector2D lerp(Vector2D other, double t) {
        return new Vector2D(
            x + (other.x - x) * t,
            y + (other.y - y) * t
        );
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f)", x, y);
    }
}
