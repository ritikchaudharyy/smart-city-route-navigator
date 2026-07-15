package com.smartcity.navigator.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * A generic, array-backed binary min-heap priority queue.
 * <p>
 * This is a from-scratch implementation (rather than relying on
 * {@link java.util.PriorityQueue}) to make the underlying data structure
 * that powers {@code DijkstraAlgorithm} explicit and inspectable — a key
 * requirement for a DSA-focused project.
 * <p>
 * The heap is stored in a {@link List} where, for a node at index
 * {@code i}: its parent is at {@code (i - 1) / 2}, its left child is at
 * {@code 2i + 1}, and its right child is at {@code 2i + 2}. The smallest
 * element (per {@link Comparable}) is always at index {@code 0}.
 * <p>
 * Both {@link #offer(Object)} and {@link #poll()} run in
 * {@code O(log n)} time, since each only needs to "bubble" an element
 * up or down a single root-to-leaf path. {@link #peek()} and
 * {@link #isEmpty()} are {@code O(1)}.
 *
 * @param <T> the element type, which must define a natural ordering
 * @author Smart City Route Navigator Team
 */
public class MinPriorityQueue<T extends Comparable<T>> {

    private final List<T> heap = new ArrayList<>();

    /**
     * Inserts an element into the queue.
     * <p>
     * Step 1: append the new element at the end of the heap array (the
     * next free leaf position).
     * Step 2: "sift up" — repeatedly compare it with its parent and swap
     * while it is smaller, restoring the min-heap property.
     * <p>
     * Time complexity: O(log n), since the heap has O(log n) levels and
     * the element moves up at most one level per comparison.
     *
     * @param element the element to insert, must not be null
     */
    public void offer(T element) {
        if (element == null) {
            throw new IllegalArgumentException("Cannot offer a null element");
        }
        heap.add(element);
        siftUp(heap.size() - 1);
    }

    /**
     * Removes and returns the smallest element in the queue.
     * <p>
     * Step 1: the smallest element is always the root, at index 0.
     * Step 2: move the last element in the heap into the root position
     * and shrink the list by one (this avoids shifting every element).
     * Step 3: "sift down" the new root — repeatedly swap it with its
     * smaller child until the min-heap property is restored.
     * <p>
     * Time complexity: O(log n), for the same reason as {@link #offer}.
     *
     * @return the smallest element, or {@code null} if the queue is empty
     */
    public T poll() {
        if (heap.isEmpty()) {
            return null;
        }
        T min = heap.get(0);
        T last = heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) {
            heap.set(0, last);
            siftDown(0);
        }
        return min;
    }

    /**
     * @return the smallest element without removing it, or {@code null} if empty
     */
    public T peek() {
        return heap.isEmpty() ? null : heap.get(0);
    }

    /**
     * @return {@code true} if the queue currently holds no elements
     */
    public boolean isEmpty() {
        return heap.isEmpty();
    }

    /**
     * @return the number of elements currently in the queue
     */
    public int size() {
        return heap.size();
    }

    /**
     * Moves the element at {@code index} up toward the root while it is
     * smaller than its parent, swapping as it goes. Terminates once the
     * element reaches the root or finds a parent no larger than itself.
     */
    private void siftUp(int index) {
        while (index > 0) {
            int parentIndex = (index - 1) / 2;
            if (heap.get(index).compareTo(heap.get(parentIndex)) >= 0) {
                break; // Min-heap property already satisfied.
            }
            swap(index, parentIndex);
            index = parentIndex;
        }
    }

    /**
     * Moves the element at {@code index} down toward the leaves by
     * repeatedly swapping with its smallest child, until both children
     * are no smaller than it (or it has no children).
     */
    private void siftDown(int index) {
        int size = heap.size();
        while (true) {
            int leftChild = 2 * index + 1;
            int rightChild = 2 * index + 2;
            int smallest = index;

            if (leftChild < size && heap.get(leftChild).compareTo(heap.get(smallest)) < 0) {
                smallest = leftChild;
            }
            if (rightChild < size && heap.get(rightChild).compareTo(heap.get(smallest)) < 0) {
                smallest = rightChild;
            }
            if (smallest == index) {
                break; // Both children (if any) are already >= this element.
            }
            swap(index, smallest);
            index = smallest;
        }
    }

    private void swap(int i, int j) {
        T temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }
}
