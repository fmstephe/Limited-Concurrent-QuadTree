package org.francis.quadtree;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Partially thread-safe quad-tree implementation. Specifically allows concurrent insertions,
 * but does not support concurrent deletes (<code>clearView</code>)
 *
 * @author Francis
 */
public class LinkedEuclidTree {
    
    public static final ThreadLocal<List<LinkedEuclidTree>> lockedTrees = new ThreadLocal<List<LinkedEuclidTree>> () {
        public List<LinkedEuclidTree> initialValue() {
            return new ArrayList<LinkedEuclidTree>();
        }
    };
    
    public LinkedEuclidTree(double leftX, double rightX, double topY, double bottomY) {
        this.leftX= leftX;
        this.rightX = rightX;
        this.topY = topY;
        this.bottomY = bottomY;
        this.state = new TreeState();
        this.lock = new ReentrantLock();
    }
    
    private volatile TreeState state;
    private final ReentrantLock lock;
    public final double leftX,rightX,topY,bottomY;
    
    public void getWithinSquare(View view, Set<Circle> circles) {
        if (!intersects(view)) return;
        // Rather than locking for reads we just cache the current state and treat that as our view (potentially ignoring concurrent writes)
        TreeState currentState = state;
        if (currentState.phase == TreeState.Phase.EMPTY) return;
        if (currentState.phase == TreeState.Phase.ONE_CIRCLE) {
            if (containsCircle(currentState.goldenCircle1,view))
                circles.add(currentState.goldenCircle1);
            return;
        }
        else if (currentState.phase == TreeState.Phase.TWO_CIRCLE) {
            if (containsCircle(currentState.goldenCircle1,view))
                circles.add(currentState.goldenCircle1);
            if (containsCircle(currentState.goldenCircle2,view))
                circles.add(currentState.goldenCircle2);
            return;
        }
        else if (currentState.phase == TreeState.Phase.BRANCH) {
            currentState.tree1.getWithinSquare(view, circles);
            currentState.tree2.getWithinSquare(view, circles);
            currentState.tree3.getWithinSquare(view, circles);
            currentState.tree4.getWithinSquare(view, circles);
        }
    }
    
    // Not thread safe
    public void clearView(View view) {
        if (!intersects(view)) return;
        if (containedBy(view)) {
            state = new TreeState();
        }
        else {
            if (state.phase == TreeState.Phase.BRANCH) {
                state.tree1.clearView(view);
                state.tree2.clearView(view);
                state.tree3.clearView(view);
                state.tree4.clearView(view);
            }
        }
    }
    
    public void lockTree() {
        List<LinkedEuclidTree> locked = lockedTrees.get();
        locked.add(this);
        lock.lock();
    }
    
    public void unlockTree() {
        List<LinkedEuclidTree> locked = lockedTrees.get();
        if (locked.get(locked.size()-1).equals(this)) {
            locked.remove(locked.size()-1);
        }
        else {
            throw new RuntimeException();
        }
        lock.unlock();
    }
    
    public boolean put(Circle putCircle) {
        List<LinkedEuclidTree> leafList = new ArrayList<LinkedEuclidTree>();
        try {
            if (!collectAndLockLeaves(putCircle,leafList)) return false;
            for (LinkedEuclidTree leaf : leafList) {
                leaf.putInternal(putCircle);
            }
            return true;
        } finally {
            try {
                for (int i = leafList.size()-1; i >= 0; i--) {
                    leafList.get(i).unlockTree();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean collectAndLockLeaves(Circle putCircle, List<LinkedEuclidTree> leafList) {
        if (!containsCircle(putCircle)) return true;
        if (state.phase != TreeState.Phase.BRANCH) {
            this.lockTree();
            if (state.phase != TreeState.Phase.BRANCH) {
                leafList.add(this);
                if (state.goldenCircle1 != null && putCircle.overlaps(state.goldenCircle1)) {
                    return false; // Indicate a conflict was detected - caller is responsible for unlocking collected leaves
                }
                if (state.goldenCircle2 != null && putCircle.overlaps(state.goldenCircle2)) {
                    return false; // Indicate a conflict was detected - caller is responsible for unlocking collected leaves
                }
            }
            else {
                this.unlockTree();
            }
        }
        if (state.phase == TreeState.Phase.BRANCH) {
            if(!state.tree1.collectAndLockLeaves(putCircle,leafList)) return false;
            if(!state.tree2.collectAndLockLeaves(putCircle,leafList)) return false;
            if(!state.tree3.collectAndLockLeaves(putCircle,leafList)) return false;
            if(!state.tree4.collectAndLockLeaves(putCircle,leafList)) return false;
        }
        return true;
    }
    
    private void putInternal(Circle putCircle) {
        if (state.phase == TreeState.Phase.EMPTY) {
            putEmpty(putCircle);
        }
        else if (state.phase == TreeState.Phase.ONE_CIRCLE) {
            putSingle(putCircle);
        }
        else if (state.phase == TreeState.Phase.TWO_CIRCLE) {
            putDouble(putCircle);
        }
        else {
            throw new RuntimeException();
        }
    }
    
    private void putEmpty(Circle putCircle) {
        assert containsCircle(putCircle);
        assert lock.isHeldByCurrentThread();
        TreeState newState = new TreeState(putCircle);
        state = newState;
    }
    
    private void putSingle(Circle putCircle) {
        assert containsCircle(putCircle);
        assert lock.isHeldByCurrentThread();
        TreeState newState = new TreeState(state.goldenCircle1,putCircle);
        state = newState;
    }
    
    private void putDouble(Circle putCircle) {
        assert containsCircle(putCircle);
        assert lock.isHeldByCurrentThread();
        TreeState newState = branchTreeState(putCircle);
        state = newState;
    }
    
    private TreeState branchTreeState(Circle putCircle) {
        double midX = rightX - ((rightX-leftX)/2);
        double midY = bottomY - ((bottomY-topY)/2);
        LinkedEuclidTree tree1 = new LinkedEuclidTree(leftX,midX,topY,midY);
        LinkedEuclidTree tree2 = new LinkedEuclidTree(leftX,midX,midY,bottomY);
        LinkedEuclidTree tree3 = new LinkedEuclidTree(midX,rightX,topY,midY);
        LinkedEuclidTree tree4 = new LinkedEuclidTree(midX,rightX,midY,bottomY);
        tree1.put(putCircle);
        tree1.put(state.goldenCircle1);
        tree1.put(state.goldenCircle2);
        tree2.put(putCircle);
        tree2.put(state.goldenCircle1);
        tree2.put(state.goldenCircle2);
        tree3.put(putCircle);
        tree3.put(state.goldenCircle1);
        tree3.put(state.goldenCircle2);
        tree4.put(putCircle);
        tree4.put(state.goldenCircle1);
        tree4.put(state.goldenCircle2);
        return new TreeState(tree1,tree2,tree3,tree4);
    }
    
    public void getConflicts(Circle conflictCircle, Set<Circle> conflicts) {
        if (!containsCircle(conflictCircle)) return;
        TreeState currentState = state;
        // Rather than locking for reads we just cache the current state and treat that as our view (potentially ignoring concurrent writes)
        if (currentState.phase == TreeState.Phase.EMPTY) return;
        if (currentState.phase != TreeState.Phase.BRANCH) {
            if (currentState.goldenCircle1 != null && currentState.goldenCircle1.overlaps(conflictCircle)) {
                conflicts.add(currentState.goldenCircle1);
            }
            if (currentState.goldenCircle2 != null && currentState.goldenCircle2.overlaps(conflictCircle)) {
                conflicts.add(currentState.goldenCircle2);
            }
            return;
        }
        // currentState.phase == TreeState.Phase.BRANCH)
        currentState.tree1.getConflicts(conflictCircle, conflicts);
        currentState.tree2.getConflicts(conflictCircle, conflicts);
        currentState.tree3.getConflicts(conflictCircle, conflicts);
        currentState.tree4.getConflicts(conflictCircle, conflicts);
        return;
    }

    private boolean intersects(View view) {
        if (contains(view.leftX,view.topY)) return true;
        if (contains(view.rightX,view.bottomY)) return true;
        if (contains(leftX,topY,view.leftX,view.rightX,view.topY,view.bottomY)) return true;
        if (contains(rightX,bottomY,view.leftX,view.rightX,view.topY,view.bottomY)) return true;
        return false;
    }
    
    private boolean containedBy(View view) {
        if (!(leftX >= view.leftX)) return false;
        if (!(rightX <= view.rightX)) return false;
        if (!(topY >= view.topY)) return false;
        if (!(bottomY <= view.bottomY)) return false;
        return true;
    }
    
    private boolean contains(double x, double y) {
        return contains(x,y,leftX,rightX,topY,bottomY);
    }
    
    private boolean contains(double x, double y, double lX, double rX, double tY, double bY) {
        return x >= lX && x <= rX && y >= tY && y <= bY;
    }
    
    private boolean containsCircle(double x, double y, double radius) {
        return containsCircle(x,y,radius,leftX,rightX,topY,bottomY);
    }
    
    private boolean containsCircle(Circle circle) {
        return containsCircle(circle.x,circle.y,circle.radius);
    }

    private boolean containsCircle(Circle circle, View view) {
        return containsCircle(circle.x,circle.y,circle.radius,view.leftX,view.rightX,view.topY,view.bottomY);
    }
    
    private boolean containsCircle(double x, double y, double radius, double lX, double rX, double tY, double bY) {
        // If the centre of the circle is within the square
        if (contains(x,y,lX,rX,tY,bY)) return true;
        // If any of the four corners of the square are contained by the circle
        if (distance(x,y,leftX,topY) < radius) return true;
        if (distance(x,y,rightX,topY) < radius) return true;
        if (distance(x,y,leftX,bottomY) < radius) return true;
        if (distance(x,y,rightX,bottomY) < radius) return true;
        // If the circle overlaps any side of the square
        if ((distance(x,y,leftX,y) < radius) && between(y,topY,bottomY)) return true;
        if (distance(x,y,rightX,y) < radius && between(y,topY,bottomY)) return true;
        if (distance(x,y,x,topY) < radius && between(x,leftX,rightX)) return true;
        if (distance(x,y,x,bottomY) < radius && between(x,leftX,rightX)) return true;
        // Otherwise
        return false;
    }
    
    private boolean between(double y, double low, double high) {
        return y >= low && y <= high;
    }

    private double distance(double x1, double y1, double x2, double y2) {
        double xDiff = x1-x2;
        double yDiff = y1-y2;
        return Math.sqrt(Math.pow(xDiff,2)+Math.pow(yDiff,2));
    }

    public String toString() {
        return leftX+", "+rightX+", "+topY+", "+bottomY+", "+state.toString();
    }

    private static class TreeState {
        enum Phase {EMPTY,ONE_CIRCLE,TWO_CIRCLE,BRANCH};
        
        public final Circle goldenCircle1,goldenCircle2;
        public final LinkedEuclidTree tree1,tree2,tree3,tree4;
        public final Phase phase;
        
        public TreeState() {
            this.phase = Phase.EMPTY;
            this.goldenCircle1 = null;
            this.goldenCircle2 = null;
            this.tree1 = null;
            this.tree2 = null;
            this.tree3 = null;
            this.tree4 = null;
        }
        
        public TreeState(Circle goldenCircle1) {
            if (goldenCircle1 == null) throw new IllegalArgumentException();
            this.phase = Phase.ONE_CIRCLE;
            this.goldenCircle1 = goldenCircle1;
            this.goldenCircle2 = null;
            this.tree1 = null;
            this.tree2 = null;
            this.tree3 = null;
            this.tree4 = null;
        }
        
        public TreeState(Circle goldenCircle1, Circle goldenCircle2) {
            if (goldenCircle1 == null) throw new IllegalArgumentException();
            this.phase = Phase.TWO_CIRCLE;
            this.goldenCircle1 = goldenCircle1;
            this.goldenCircle2 = goldenCircle2;
            this.tree1 = null;
            this.tree2 = null;
            this.tree3 = null;
            this.tree4 = null;
        }
        
        public TreeState(LinkedEuclidTree tree1, LinkedEuclidTree tree2, LinkedEuclidTree tree3, LinkedEuclidTree tree4) {
            if (tree1 == null || tree2 == null || tree3 == null || tree4 == null) throw new IllegalArgumentException();
            this.phase = Phase.BRANCH;
            this.goldenCircle1 = null;
            this.goldenCircle2 = null;
            this.tree1 = tree1;
            this.tree2 = tree2;
            this.tree3 = tree3;
            this.tree4 = tree4;
        }
        
        public String toString() {
            return phase.toString();
        }
    }
}