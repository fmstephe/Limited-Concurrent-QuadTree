package imagegenerator.goldenrotation.euclidmap;

import java.util.HashSet;
import java.util.Set;

public class LinkedEuclidMap extends EuclidMap {
    
    private final LinkedEuclidTree tree;
    
    public LinkedEuclidMap(double leftX, double rightX, double topY, double bottomY) {
        this.tree = new LinkedEuclidTree(leftX, rightX, topY, bottomY);
    }
    
    /* (non-Javadoc)
     * @see imagegenerator.goldenrotation.euclidmap.IEuclideMap#getWithinSquare(double, double, double, double)
     */
    @Override
    public Set<GoldenCircle> getWithinView(View view) {
        Set<GoldenCircle> circles = new HashSet<GoldenCircle>();
        tree.getWithinSquare(view, circles);
        return circles;
    }
    
    /* (non-Javadoc)
     * @see imagegenerator.goldenrotation.euclidmap.IEuclideMap#put(imagegenerator.goldenrotation.euclidmap.GoldenCircle)
     */
    @Override
    public boolean put(GoldenCircle putCircle) {
        try {
            return tree.put(putCircle);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public Set<GoldenCircle> getConflicts(GoldenCircle conflictCircle) {
        Set<GoldenCircle> conflicts = new HashSet<GoldenCircle>();
        tree.getConflicts(conflictCircle,conflicts);
        return conflicts;
    }

    /**
     * Not thread safe!
     */
    @Override
    public void clearView(View clearedView) {
        tree.clearView(clearedView);
    }
}
