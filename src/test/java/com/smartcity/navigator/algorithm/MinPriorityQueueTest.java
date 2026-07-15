package com.smartcity.navigator.algorithm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link MinPriorityQueue}, verifying the binary min-heap
 * always pops elements in ascending order regardless of insertion order,
 * and that its edge cases (empty queue, single element) behave correctly.
 *
 * @author Smart City Route Navigator Team
 */
class MinPriorityQueueTest {

    @Test
    void newQueue_isEmpty() {
        MinPriorityQueue<Integer> queue = new MinPriorityQueue<>();
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
        assertNull(queue.peek());
        assertNull(queue.poll());
    }

    @Test
    void offerThenPoll_returnsSmallestFirst() {
        MinPriorityQueue<Integer> queue = new MinPriorityQueue<>();
        queue.offer(5);
        queue.offer(1);
        queue.offer(3);
        queue.offer(4);
        queue.offer(2);

        // Regardless of insertion order, poll() must yield ascending values.
        assertEquals(1, queue.poll());
        assertEquals(2, queue.poll());
        assertEquals(3, queue.poll());
        assertEquals(4, queue.poll());
        assertEquals(5, queue.poll());
        assertTrue(queue.isEmpty());
    }

    @Test
    void peek_doesNotRemoveElement() {
        MinPriorityQueue<Integer> queue = new MinPriorityQueue<>();
        queue.offer(10);
        queue.offer(20);

        assertEquals(10, queue.peek());
        assertEquals(2, queue.size()); // peek must not shrink the queue
        assertEquals(10, queue.poll());
        assertEquals(1, queue.size());
    }

    @Test
    void offer_rejectsNullElement() {
        MinPriorityQueue<Integer> queue = new MinPriorityQueue<>();
        assertThrows(IllegalArgumentException.class, () -> queue.offer(null));
    }

    @Test
    void size_tracksInsertionsAndRemovals() {
        MinPriorityQueue<Integer> queue = new MinPriorityQueue<>();
        assertEquals(0, queue.size());
        queue.offer(1);
        queue.offer(2);
        queue.offer(3);
        assertEquals(3, queue.size());
        queue.poll();
        assertEquals(2, queue.size());
    }

    @Test
    void offerDuplicateValues_areAllRetained() {
        MinPriorityQueue<Integer> queue = new MinPriorityQueue<>();
        queue.offer(7);
        queue.offer(7);
        queue.offer(3);

        assertEquals(3, queue.poll());
        assertEquals(7, queue.poll());
        assertEquals(7, queue.poll());
        assertTrue(queue.isEmpty());
    }

    @Test
    void ordersCustomComparableNode() {
        // Mirrors real usage inside DijkstraAlgorithm: Node is ordered by distance.
        MinPriorityQueue<com.smartcity.navigator.model.Node> queue = new MinPriorityQueue<>();
        queue.offer(new com.smartcity.navigator.model.Node("C", 30.0));
        queue.offer(new com.smartcity.navigator.model.Node("A", 5.0));
        queue.offer(new com.smartcity.navigator.model.Node("B", 15.0));

        assertEquals("A", queue.poll().getLocationId());
        assertEquals("B", queue.poll().getLocationId());
        assertEquals("C", queue.poll().getLocationId());
    }
}
