package org.francis.quadtree;

public class View {

    public final double leftX, rightX, topY, bottomY;
    
    public View(double leftX, double rightX, double topY, double bottomY) {
        this.leftX = leftX;
        this.rightX = rightX;
        this.topY = topY;
        this.bottomY = bottomY;
        this.assertCoherence();
    }
    
    public View shrinkBy(double amount) {
        View shrunkView = new View(leftX+amount,rightX-amount,topY+amount,bottomY-amount);
        shrunkView.assertCoherence();
        return shrunkView;
    }
    
    private void assertCoherence() {
        if (!isCoherent())
            throw new IllegalStateException("View not physically possible - "+this.toString());
    }
    
    private boolean isCoherent() {
        return leftX < rightX && topY < bottomY;
    }
    
    @Override
    public String toString() {
        return "View [leftX=" + leftX + ", rightX=" + rightX + ", topY=" + topY + ", bottomY=" + bottomY + "]";
    }
}