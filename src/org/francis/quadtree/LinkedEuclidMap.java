package org.francis.quadtree;

import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of EuclidMap, wrapping an instance of LinkedEuclidTree.
 *
 * @author Francis
 */
public class LinkedEuclidMap extends EuclidMap {
    
    private final LinkedEuclidTree tree;
    
    public LinkedEuclidMap(double leftX, double rightX, double topY, double bottomY) {
        this.tree = new LinkedEuclidTree(leftX, rightX, topY, bottomY);
    }
    
    @Override
    public Set<Circle> getWithinView(View view) {
        Set<Circle> circles = new HashSet<Circle>();
        tree.getWithinSquare(view, circles);
        return circles;
    }
    
    @Override
    public boolean put(Circle putCircle) {
        try {
            return tree.put(putCircle);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public Set<Circle> getConflicts(Circle conflictCircle) {
        Set<Circle> conflicts = new HashSet<Circle>();
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
