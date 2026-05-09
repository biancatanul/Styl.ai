package com.example.wardrobeai.logic;

import com.example.wardrobeai.data.ClothingItem;

import java.util.ArrayList;
import java.util.List;

public class RedBlackTree {

    private static final boolean RED = true;
    private static final boolean BLACK = false;
    private final List<VisNode> insertionSnapshots = new ArrayList<>();
    private final List<String> insertionOrder = new ArrayList<>();
    private class Node {
        ClothingItem item;
        Node left, right, parent;
        boolean color;

        Node(ClothingItem item) {
            this.item = item;
            this.color = BLACK;
            this.left = NIL;
            this.right = NIL;
            this.parent = NIL;
        }
    }

    private final Node NIL;
    private Node root;

    public RedBlackTree() {
        NIL = new Node(null);
        NIL.color = BLACK;
        root = NIL;
    }

    private boolean isNil(Node n) {
        return n == NIL;
    }

    private String key(Node n) {
        return n.item.getName().toLowerCase();
    }

    // ── Rotations ────────────────────────────────────────────────────────────

    private void leftRotate(Node x) {
        Node y = x.right;
        x.right = y.left;
        if (!isNil(y.left)) y.left.parent = x;
        y.parent = x.parent;
        if (isNil(x.parent)) root = y;
        else if (x == x.parent.left) x.parent.left = y;
        else x.parent.right = y;
        y.left = x;
        x.parent = y;
    }

    private void rightRotate(Node y) {
        Node x = y.left;
        y.left = x.right;
        if (!isNil(x.right)) x.right.parent = y;
        x.parent = y.parent;
        if (isNil(y.parent)) root = x;
        else if (y == y.parent.left) y.parent.left = x;
        else y.parent.right = x;
        x.right = y;
        y.parent = x;
    }

    // ── Insert ───────────────────────────────────────────────────────────────

    public void insert(ClothingItem item) {
        Node z = new Node(item);
        Node y = NIL;
        Node x = root;

        while (!isNil(x)) {
            y = x;
            x = (key(z).compareTo(key(x)) < 0) ? x.left : x.right;
        }

        z.parent = y;
        if (isNil(y)) root = z;
        else if (key(z).compareTo(key(y)) < 0) y.left = z;
        else y.right = z;

        z.left = NIL;
        z.right = NIL;
        z.color = RED;
        insertFixup(z);

        insertionSnapshots.add(getVisTree());
        insertionOrder.add(item.getName());
    }
    public List<VisNode> getInsertionSnapshots() { return insertionSnapshots; }
    public List<String> getInsertionOrder() { return insertionOrder; }
    private void insertFixup(Node z) {
        while (z.parent.color == RED) {
            if (z.parent == z.parent.parent.left) {
                Node y = z.parent.parent.right; // uncle
                if (y.color == RED) {           // case 1: uncle is red
                    z.parent.color = BLACK;
                    y.color = BLACK;
                    z.parent.parent.color = RED;
                    z = z.parent.parent;
                } else {
                    if (z == z.parent.right) {  // case 2: uncle is black, z is right child
                        z = z.parent;
                        leftRotate(z);
                    }
                    z.parent.color = BLACK;     // case 3
                    z.parent.parent.color = RED;
                    rightRotate(z.parent.parent);
                }
            } else {
                Node y = z.parent.parent.left;  // uncle
                if (y.color == RED) {           // case 1 mirrored
                    z.parent.color = BLACK;
                    y.color = BLACK;
                    z.parent.parent.color = RED;
                    z = z.parent.parent;
                } else {
                    if (z == z.parent.left) {   // case 2 mirrored
                        z = z.parent;
                        rightRotate(z);
                    }
                    z.parent.color = BLACK;     // case 3 mirrored
                    z.parent.parent.color = RED;
                    leftRotate(z.parent.parent);
                }
            }
        }
        root.color = BLACK;
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    public void delete(ClothingItem item) {
        Node z = search(root, item.getName());
        if (!isNil(z)) del(z);
    }

    private void del(Node z) {
        Node y = (isNil(z.left) || isNil(z.right)) ? z : successor(z);
        Node x = !isNil(y.left) ? y.left : y.right;
        x.parent = y.parent;

        if (isNil(y.parent)) root = x;
        else if (y == y.parent.left) y.parent.left = x;
        else y.parent.right = x;

        if (y != z) z.item = y.item;

        if (y.color == BLACK) deleteFixup(x);
    }

    private void deleteFixup(Node x) {
        while (x != root && x.color == BLACK) {
            if (x == x.parent.left) {
                Node w = x.parent.right;
                if (w.color == RED) {
                    w.color = BLACK;
                    x.parent.color = RED;
                    leftRotate(x.parent);
                    w = x.parent.right;
                }
                if (w.left.color == BLACK && w.right.color == BLACK) {
                    w.color = RED;
                    x = x.parent;
                } else {
                    if (w.right.color == BLACK) {
                        w.left.color = BLACK;
                        w.color = RED;
                        rightRotate(w);
                        w = x.parent.right;
                    }
                    w.color = x.parent.color;
                    x.parent.color = BLACK;
                    w.right.color = BLACK;
                    leftRotate(x.parent);
                    x = root;
                }
            } else {
                Node w = x.parent.left;
                if (w.color == RED) {
                    w.color = BLACK;
                    x.parent.color = RED;
                    rightRotate(x.parent);
                    w = x.parent.left;
                }
                if (w.left.color == BLACK && w.right.color == BLACK) {
                    w.color = RED;
                    x = x.parent;
                } else {
                    if (w.left.color == BLACK) {
                        w.right.color = BLACK;
                        w.color = RED;
                        leftRotate(w);
                        w = x.parent.left;
                    }
                    w.color = x.parent.color;
                    x.parent.color = BLACK;
                    w.left.color = BLACK;
                    rightRotate(x.parent);
                    x = root;
                }
            }
        }
        x.color = BLACK;
    }

    // ── Search ───────────────────────────────────────────────────────────────

    private Node search(Node w, String name) {
        if (isNil(w) || w.item.getName().equalsIgnoreCase(name)) return w;
        return search(name.toLowerCase().compareTo(key(w)) < 0 ? w.left : w.right, name);
    }

    public ClothingItem search(String name) {
        Node result = search(root, name);
        return isNil(result) ? null : result.item;
    }

    // ── Traversal ────────────────────────────────────────────────────────────

    /**
     * Returns all items sorted alphabetically by name (inorder traversal).
     */
    public List<ClothingItem> inorder() {
        List<ClothingItem> result = new ArrayList<>();
        inorder(root, result);
        return result;
    }

    private void inorder(Node n, List<ClothingItem> result) {
        if (!isNil(n)) {
            inorder(n.left, result);
            result.add(n.item);
            inorder(n.right, result);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Node minimum(Node w) {
        Node x = w;
        while (!isNil(x.left)) x = x.left;
        return x;
    }

    private Node successor(Node w) {
        if (isNil(w)) return w;
        if (!isNil(w.right)) return minimum(w.right);
        Node y = w.parent;
        while (!isNil(y) && w == y.right) {
            w = y;
            y = y.parent;
        }
        return y;
    }

    public boolean isEmpty() {
        return isNil(root);
    }

    /**
     * For visualization: returns the root node's item (entry point for tree drawing).
     */
    public ClothingItem getRoot() {
        return isNil(root) ? null : root.item;
    }

    // ── Visualization support ─────────────────────────────────────────────────

    /**
     * A flat snapshot of the tree for the visualization screen.
     * Each entry contains: item, depth, isRed, hasLeft, hasRight.
     */
    public static class NodeSnapshot {
        public final ClothingItem item;
        public final int depth;
        public final boolean isRed;
        public final boolean hasLeft;
        public final boolean hasRight;

        NodeSnapshot(ClothingItem item, int depth, boolean isRed, boolean hasLeft, boolean hasRight) {
            this.item = item;
            this.depth = depth;
            this.isRed = isRed;
            this.hasLeft = hasLeft;
            this.hasRight = hasRight;
        }
    }

    public List<NodeSnapshot> getSnapshots() {
        List<NodeSnapshot> list = new ArrayList<>();
        collectSnapshots(root, 0, list);
        return list;
    }

    private void collectSnapshots(Node n, int depth, List<NodeSnapshot> list) {
        if (!isNil(n)) {
            collectSnapshots(n.left, depth + 1, list);
            list.add(new NodeSnapshot(n.item, depth, n.color == RED, !isNil(n.left), !isNil(n.right)));
            collectSnapshots(n.right, depth + 1, list);
        }
    }

    public static class VisNode {
        public final ClothingItem item;
        public final boolean isRed;
        public final VisNode left;
        public final VisNode right;

        VisNode(ClothingItem item, boolean isRed, VisNode left, VisNode right) {
            this.item = item;
            this.isRed = isRed;
            this.left = left;
            this.right = right;
        }
    }

    public VisNode getVisTree() {
        return buildVisNode(root);
    }

    private VisNode buildVisNode(Node n) {
        if (isNil(n)) return null;
        return new VisNode(n.item, n.color == RED, buildVisNode(n.left), buildVisNode(n.right));
    }
}