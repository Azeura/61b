package deque;

import org.junit.Test;
import static org.junit.Assert.*;
import edu.princeton.cs.algs4.StdRandom;


public class ArrayDequeTest {

    @Test
    public void addAndRemoveTest () {
        ArrayDeque<Integer> Atest = new ArrayDeque<>();
        for (int i = 0; i < 500; i ++) {
            Atest.addLast(i);
        }
        Atest.printDeque();
        for (int i = 0; i < 250; i++) {
            Atest.removeLast();
        }
        Atest.printDeque();
    }

    @Test
    public void randomizedTest() {
        ArrayDeque<Integer> Atest = new ArrayDeque<>();
        LinkedListDeque<Integer> Ltest = new LinkedListDeque<>();
        for (int i = 0; i < 10000; i++) {
            int operationNumber = StdRandom.uniform(0, 3);
            if (operationNumber > 0) {
                int randVal = StdRandom.uniform(0, 100);
                if (operationNumber == 1) {
                    Atest.addFirst(randVal);
                    Ltest.addFirst(randVal);
                    assertEquals("Size mismatch after addFirst", Atest.size(), Ltest.size());
                } else if (operationNumber == 2 && Atest.size() != 0) {
                    Atest.removeLast();
                    Ltest.removeLast();
                    assertEquals("Size mismatch after removeLast", Atest.size(), Ltest.size());
                }
            } else {
                if (i % 1000 == 0) {
                    System.out.println("At iteration " + i + ", Size is (" + Atest.size() + ")");
                }            }
        }
    }
}
