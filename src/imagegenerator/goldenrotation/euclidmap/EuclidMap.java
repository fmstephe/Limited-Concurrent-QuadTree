package imagegenerator.goldenrotation.euclidmap;

import java.util.Set;

public abstract class EuclidMap {

    public abstract Set<GoldenCircle> getWithinView(View view);

    public abstract boolean put(GoldenCircle putCircle);
    
    public abstract Set<GoldenCircle> getConflicts(GoldenCircle conflictCircle);
    
    public abstract void clearView(View clearedView);
}