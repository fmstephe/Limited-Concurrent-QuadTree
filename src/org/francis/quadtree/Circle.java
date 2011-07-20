package org.francis.quadtree;

public class Circle {
    public final double x;
    public final double y;
    public final double radius;
    
    public Circle(double x, double y, double radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public boolean overlaps(Circle circle) {
        double distance = distance(circle);
        return distance < (radius+circle.radius);
    }
    
    public double distance(Circle circle) {
        double xDiff = x-circle.x;
        double yDiff = y-circle.y;
        return Math.sqrt(Math.pow(xDiff,2)+Math.pow(yDiff,2));
    }

    @Override
    public String toString() {
        return x+", "+y+", "+radius;
    }
}
