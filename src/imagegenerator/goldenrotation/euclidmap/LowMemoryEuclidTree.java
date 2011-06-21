package imagegenerator.goldenrotation.euclidmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class LowMemoryEuclidTree {
    
    public static final ThreadLocal<List<LowMemoryEuclidTree>> lockedTrees = new ThreadLocal<List<LowMemoryEuclidTree>> () {
        public List<LowMemoryEuclidTree> initialValue() {
            return new ArrayList<LowMemoryEuclidTree>();
        }
    };
    
    public LowMemoryEuclidTree() {
        this.state = null;
        this.lock = new ReentrantLock();
    }
    
    private volatile Branches state;
    private final ReentrantLock lock;
    
    public void getWithinSquare(double aLeftX, double aRightX, double aTopY, double aBottomY, Set<GoldenCircle> circles) {
    }
    
    public void lockTree() {
        List<LowMemoryEuclidTree> locked = lockedTrees.get();
        locked.add(this);
        lock.lock();
    }
    
    public void unlockTree() {
        List<LowMemoryEuclidTree> locked = lockedTrees.get();
        if (locked.get(locked.size()-1).equals(this)) {
            locked.remove(locked.size()-1);
        }
        else {
            throw new RuntimeException();
        }
        lock.unlock();
    }
    
    public boolean put(GoldenCircle putCircle) {
        return false;
    }

    private List<Object> collectChildren(GoldenCircle putCircle, View view, Class clazz) {
        List<Object> children = new ArrayList<Object>(4);
        if (state.child1 != null && clazz.isInstance(state.child1)) children.add(state.child1);
        if (state.child2 != null && clazz.isInstance(state.child2)) children.add(state.child2);
        if (state.child3 != null && clazz.isInstance(state.child3)) children.add(state.child3);
        if (state.child4 != null && clazz.isInstance(state.child4)) children.add(state.child4);
        return children;
    }
    
    public String toString() {
        return lockedTrees.get().toString();
    }

    private static class Branches {
        
        public volatile Object child1,child2,child3,child4;
        
        public Branches(Object child1, Object child2, Object child3, Object child4) {
            this.child1 = child1;
            this.child2 = child2;
            this.child3 = child3;
            this.child4 = child4;
        }
    }
}