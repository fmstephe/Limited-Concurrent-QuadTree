package imagegenerator.goldenrotation.euclidmap;

import java.util.HashSet;
import java.util.Set;

public class LowMemoryEuclidMap extends EuclidMap {

    private final LowMemoryEuclidTree tree;
    
    public LowMemoryEuclidMap(double leftX, double rightX, double topY, double bottomY) {
        tree = new LowMemoryEuclidTree();
    }
    
    @Override
    public Set<GoldenCircle> getWithinView(View view) {
        Set<GoldenCircle> circles = new HashSet<GoldenCircle>();
        tree.getWithinSquare(view.leftX, view.rightX, view.topY, view.bottomY, circles);
        return circles;
    }

    @Override
    public boolean put(GoldenCircle putCircle) {
        return tree.put(putCircle);
    }

    @Override
    public void clearView(View clearedView) {
        // TODO Auto-generated method stub
    }
}