package com.example.wardrobeai.logic;

import com.example.wardrobeai.data.ClothingItem;
import com.example.wardrobeai.data.Outfit;

import java.util.ArrayList;
import java.util.List;

/**
 * Max-heap (by compatibility score) using a Binomial Heap.
 * Used to rank AI-suggested outfits so the best ones surface first.
 *
 * Scoring: number of compatible item pairs in the outfit (via CompatibilityGraph).
 * Max possible score for a 4-item outfit = 6 pairs (every pair compatible).
 */
public class BinomialHeap {

    // ── Node ─────────────────────────────────────────────────────────────────

    public static class Node {
        public Outfit outfit;
        public int score;
        int degree;
        Node parent;
        Node child;   // leftmost child
        Node sibling; // next sibling (right)

        Node(Outfit outfit, int score) {
            this.outfit = outfit;
            this.score = score;
            this.degree = 0;
            this.parent = null;
            this.child = null;
            this.sibling = null;
        }
    }

    // ── Fields ────────────────────────────────────────────────────────────────

    private Node head; // head of the root list
    private int size;

    public BinomialHeap() {
        head = null;
        size = 0;
    }

    // ── Core operations ───────────────────────────────────────────────────────

    public void insert(Outfit outfit, int score) {
        BinomialHeap temp = new BinomialHeap();
        temp.head = new Node(outfit, score);
        head = union(this, temp).head;
        size++;
    }

    /** Returns (but does not remove) the outfit with the highest score. */
    public Node peekMax() {
        return findMax();
    }

    /** Removes and returns the outfit with the highest score. */
    public Node extractMax() {
        if (head == null) return null;

        Node maxPrev = null;
        Node max = head;
        Node prev = null;
        Node curr = head;

        // find max in root list
        while (curr != null) {
            if (curr.score > max.score) {
                max = curr;
                maxPrev = prev;
            }
            prev = curr;
            curr = curr.sibling;
        }

        // remove max from root list
        if (maxPrev != null) maxPrev.sibling = max.sibling;
        else head = max.sibling;

        // reverse max's children into a new heap
        BinomialHeap childHeap = new BinomialHeap();
        Node child = max.child;
        while (child != null) {
            Node next = child.sibling;
            child.sibling = childHeap.head;
            child.parent = null;
            childHeap.head = child;
            child = next;
        }

        head = union(this, childHeap).head;
        size--;
        return max;
    }

    public boolean isEmpty() {
        return head == null;
    }

    public int size() {
        return size;
    }

    // ── Scoring helper ────────────────────────────────────────────────────────

    /**
     * Scores an outfit by counting compatible item pairs via the graph.
     * Call this before inserting to get the score.
     */
    public static int scoreOutfit(Outfit outfit, CompatibilityGraph graph) {
        List<ClothingItem> items = outfit.getItems();
        int score = 0;
        for (int i = 0; i < items.size(); i++) {
            for (int j = i + 1; j < items.size(); j++) {
                if (graph.areCompatible(items.get(i), items.get(j))) score++;
            }
        }
        return score;
    }

    /**
     * Convenience: insert a list of outfits into a new heap, scored automatically.
     */
    public static BinomialHeap fromOutfits(List<Outfit> outfits, CompatibilityGraph graph) {
        BinomialHeap heap = new BinomialHeap();
        for (Outfit outfit : outfits) {
            int score = scoreOutfit(outfit, graph);
            heap.insert(outfit, score);
        }
        return heap;
    }

    /**
     * Drains the heap and returns outfits sorted best-first.
     */
    public List<Outfit> drainSorted() {
        List<Outfit> sorted = new ArrayList<>();
        while (!isEmpty()) {
            sorted.add(extractMax().outfit);
        }
        return sorted;
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /** Links two binomial trees of the same degree. y becomes child of z (max-heap: larger key stays as root). */
    private void link(Node y, Node z) {
        y.parent = z;
        y.sibling = z.child;
        z.child = y;
        z.degree++;
    }

    /** Merges the root lists of two heaps in order of increasing degree. */
    private Node mergeRootLists(BinomialHeap h1, BinomialHeap h2) {
        if (h1.head == null) return h2.head;
        if (h2.head == null) return h1.head;

        Node result;
        Node a = h1.head;
        Node b = h2.head;

        if (a.degree <= b.degree) { result = a; a = a.sibling; }
        else                      { result = b; b = b.sibling; }

        Node curr = result;
        while (a != null && b != null) {
            if (a.degree <= b.degree) { curr.sibling = a; a = a.sibling; }
            else                      { curr.sibling = b; b = b.sibling; }
            curr = curr.sibling;
        }
        curr.sibling = (a != null) ? a : b;
        return result;
    }

    /** Unions two heaps and returns a new heap containing all nodes. */
    private BinomialHeap union(BinomialHeap h1, BinomialHeap h2) {
        BinomialHeap result = new BinomialHeap();
        result.head = mergeRootLists(h1, h2);
        if (result.head == null) return result;

        Node prev = null;
        Node curr = result.head;
        Node next = curr.sibling;

        while (next != null) {
            boolean differentDegree = curr.degree != next.degree;
            boolean threeInRow = next.sibling != null && next.sibling.degree == curr.degree;

            if (differentDegree || threeInRow) {
                prev = curr;
                curr = next;
            } else {
                // same degree: link (max-heap — larger root wins)
                if (curr.score >= next.score) {
                    curr.sibling = next.sibling;
                    link(next, curr);
                } else {
                    if (prev == null) result.head = next;
                    else prev.sibling = next;
                    link(curr, next);
                    curr = next;
                }
            }
            next = curr.sibling;
        }
        return result;
    }

    private Node findMax() {
        if (head == null) return null;
        Node max = head;
        Node curr = head.sibling;
        while (curr != null) {
            if (curr.score > max.score) max = curr;
            curr = curr.sibling;
        }
        return max;
    }

    // ── Visualization support ─────────────────────────────────────────────────

    /** Flat snapshot of a node for drawing the heap trees on screen. */
    public static class NodeSnapshot {
        public final String outfitName;
        public final int score;
        public final int degree;
        public final boolean hasChild;
        public final boolean hasSibling;

        NodeSnapshot(String outfitName, int score, int degree, boolean hasChild, boolean hasSibling) {
            this.outfitName = outfitName;
            this.score = score;
            this.degree = degree;
            this.hasChild = hasChild;
            this.hasSibling = hasSibling;
        }
    }

    /** Returns a level-order snapshot of all trees in the heap for visualization. */
    public List<NodeSnapshot> getSnapshots() {
        List<NodeSnapshot> list = new ArrayList<>();
        Node curr = head;
        while (curr != null) {
            collectTree(curr, list);
            curr = curr.sibling;
        }
        return list;
    }

    private void collectTree(Node n, List<NodeSnapshot> list) {
        if (n == null) return;
        list.add(new NodeSnapshot(n.outfit.getName(), n.score, n.degree, n.child != null, n.sibling != null));
        collectTree(n.child, list);
    }
}