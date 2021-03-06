import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.StdDraw;
import edu.princeton.cs.algs4.Queue;

public class KdTree {
    private static final boolean VERTICAL = true;
    private static final boolean HORIZONTAL = false;
    private static final RectHV MAXRECT = new RectHV(0, 0, 1, 1);

    private Node root;
    private int size;

    /**
     * construct an empty set of points
     */
    public KdTree() {
        size = 0;
    }

    private class Node {
        Point2D point;
        Node left;
        Node right;
        boolean split;

        private Node(Point2D point, boolean split) {
            this.point = point;
            this.split = split;
        }

        private double getX() {
            return point.x();
        }

        private double getY() {
            return point.y();
        }

        private boolean isVertical() {
            return split == VERTICAL;
        }

        private int compareTo(Point2D that) {
            isLegal(that);

            if (that.equals(point)) {
                return 0;
            }
            if (isVertical()) {
                return Double.compare(that.x(), point.x());
            } else  {
                return Double.compare(that.y(), point.y());
            }
        }
    }

    /**
     * is the set empty?
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * number of points in the set
     */
    public int size() {
        return size;
    }

    /**
     * add the point to the set (if it is not already in the set)
     */
    public void insert(Point2D p) {
        isLegal(p);
        root = insert(root, p, HORIZONTAL);
    }

    private Node insert(Node node, Point2D p, boolean split) {
        if (node == null) {
            size += 1;
            return new Node(p, !split);
        }
        if (node.point.equals(p)) {
            return node;
        }

        int cmp = node.compareTo(p);
        if (cmp < 0) {
            node.left = insert(node.left, p, node.split);
        } else {
            node.right = insert(node.right, p, node.split);
        }
        return node;
    }

    /**
     * does the set contain point p?
     */
    public boolean contains(Point2D p) {
        isLegal(p);
        return contains(root, p);
    }

    private boolean contains(Node node, Point2D p) {
        if (node == null) {
            return false;
        }
        if (node.point.equals(p)) {
            return true;
        }

        double cmp = node.compareTo(p);
        if (cmp < 0) {
            return contains(node.left, p);
        } else {
            return contains(node.right, p);
        }
    }

    /**
     * draw all points to standard draw
     */
    public void draw() {
        StdDraw.clear();
        StdDraw.setPenRadius(0.001);
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.rectangle(0.5, 0.5, 0.5, 0.5);
        draw(root, MAXRECT);
    }

    private void draw(Node node, RectHV rect) {
        if (node != null) {
            drawLine(node, rect);
            draw(node.left, splitRect(node, rect, true));
            draw(node.right, splitRect(node, rect, false));
        }
    }

    private static void drawLine(Node node, RectHV rect) {
        double x0 = rect.xmin();
        double y0 = rect.ymin();
        double x1 = rect.xmax();
        double y1 = rect.ymax();

        StdDraw.setPenRadius(0.001);
        if (node.isVertical()) {
            x0 = node.getX();
            x1 = node.getX();
            StdDraw.setPenColor(StdDraw.RED);
        } else {
            y0 = node.getY();
            y1 = node.getY();
            StdDraw.setPenColor(StdDraw.BLUE);
        }
        StdDraw.line(x0, y0, x1, y1);
        StdDraw.setPenRadius(0.01);
        StdDraw.point(node.getX(), node.getY());
    }

    private static RectHV splitRect(Node node, RectHV nodeRect, boolean leftOrBottom) {
        if (node == null) {
            return null;
        }

        double xmin = nodeRect.xmin();
        double ymin = nodeRect.ymin();
        double xmax = nodeRect.xmax();
        double ymax = nodeRect.ymax();

        if (leftOrBottom) {
            if (node.isVertical()) {
                xmax = node.getX();
            } else {
                ymax = node.getY();
            }
        } else {
            if (node.isVertical()) {
                xmin = node.getX();
            } else {
                ymin = node.getY();
            }
        }
        return new RectHV(xmin, ymin, xmax, ymax);
    }

    /**
     * all points that are inside the rectangle (or on the boundary)
     */
    public Iterable<Point2D> range(RectHV rect) {
        isLegal(rect);
        Queue<Point2D> q = new Queue<>();
        range(root, rect, q, MAXRECT);
        return q;
    }

    private void range(Node node, RectHV rect, Queue<Point2D> q, RectHV nodeRect) {
        if (node == null) {
            return;
        }
        if (!rect.intersects(nodeRect)) {
            return;
        }
        if (rect.contains(node.point)) {
            q.enqueue(node.point);
        }

        range(node.left, rect, q, splitRect(node, nodeRect, true));
        range(node.right, rect, q, splitRect(node, nodeRect, false));
    }

    /**
     * a nearest neighbor in the set to point p; null if the set is empty
     */
    public Point2D nearest(Point2D p) {
        isLegal(p);
        if (root == null) {
            return null;
        }
        return nearest(root, p, MAXRECT, root.point, p.distanceSquaredTo(root.point));
    }

    private Point2D nearest(Node node, Point2D p, RectHV nodeRect, Point2D nearestP, double minDist) {
        if (node == null || minDist <= nodeRect.distanceSquaredTo(p)) {
            return nearestP;
        }

        double nodeDist = p.distanceSquaredTo(node.point);
        if (minDist > nodeDist) {
            nearestP = node.point;
            minDist = nodeDist;
        }

        double cmp = node.compareTo(p);
        if (cmp < 0) {
            nearestP = nearest(node.left, p, splitRect(node, nodeRect, true), nearestP, minDist);
            minDist = p.distanceSquaredTo(nearestP);
            nearestP = nearest(node.right, p, splitRect(node, nodeRect, false), nearestP, minDist);
        } else {
            nearestP = nearest(node.right, p, splitRect(node, nodeRect, false), nearestP, minDist);
            minDist = p.distanceSquaredTo(nearestP);
            nearestP = nearest(node.left, p, splitRect(node, nodeRect, true), nearestP, minDist);
        }

        return nearestP;
    }

    private void isLegal(Object object) {
        if (object == null) {
            throw new java.lang.IllegalArgumentException();
        }
    }

    /*
    public static void main(String[] args) {
        Point2D a = new Point2D(0.7, 0.2);
        Point2D b = new Point2D(0.5, 0.4);
        Point2D c = new Point2D(0.2, 0.3);
        Point2D d = new Point2D(0.4, 0.7);
        Point2D e = new Point2D(0.9, 0.6);

        Point2D f = new Point2D(0.2, 0.1);

        KdTree t = new KdTree();
        t.insert(a);
        t.insert(b);
        t.insert(c);
        t.insert(d);
        t.insert(e);
        System.out.println(t.contains(c));
        System.out.println(t.contains(f));
        //t.draw();
        //System.out.println(t.contains(f));
    }

     */
}

