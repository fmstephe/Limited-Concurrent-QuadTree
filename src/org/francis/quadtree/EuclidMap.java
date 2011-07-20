package org.francis.quadtree;

import java.util.Set;

public abstract class EuclidMap {

    public abstract Set<Circle> getWithinView(View view);

    public abstract boolean put(Circle putCircle);
    
    public abstract Set<Circle> getConflicts(Circle conflictCircle);
    
    public abstract void clearView(View clearedView);
}