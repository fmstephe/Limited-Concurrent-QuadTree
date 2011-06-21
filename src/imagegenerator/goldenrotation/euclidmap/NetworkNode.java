package imagegenerator.goldenrotation.euclidmap;

public class NetworkNode {

    public final GoldenCircle circle;
    public volatile NetworkNode topLeft;
    public volatile NetworkNode bottomLeft;
    public volatile NetworkNode topRight;
    public volatile NetworkNode bottomRight;
    
    public NetworkNode(GoldenCircle circle, NetworkNode topLeft, NetworkNode bottomLeft, NetworkNode topRight, NetworkNode bottomRight) {
        this.circle = circle;
        this.topLeft = topLeft;
        this.bottomLeft = bottomLeft;
        this.topRight = topRight;
        this.bottomRight = bottomRight;
    }
    
    
}