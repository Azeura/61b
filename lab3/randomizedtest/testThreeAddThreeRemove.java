package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;



public class testThreeAddThreeRemove {
    AListNoResizing<Integer> aTest = new AListNoResizing<>();
    BuggyAList<Integer> bTest = new BuggyAList<>();

    @Test
    public void testEqual() {
        aTest.addLast(4);
        aTest.addLast(5);
        aTest.addLast(6);
        bTest.addLast(4);
        bTest.addLast(5);
        bTest.addLast(6);
        assertEquals(aTest.removeLast(),bTest.removeLast());
        assertEquals(aTest.removeLast(),bTest.removeLast());
        assertEquals(aTest.removeLast(),bTest.removeLast());
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> B = new BuggyAList<>();

        int N = 10000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0 ,3);
            if (operationNumber > 0 ) {
                if (operationNumber == 1) {
                    int randVal = StdRandom.uniform(0, 100);
                    L.addLast(randVal);
                    B.addLast(randVal);

                } else if (operationNumber == 2 && L.size() != 0) {
                    int remvValue = L.removeLast();
                    int remvValue2 = B.removeLast();

                }
            } else {
                int size = L.size();
                int size2 = B.size();

            }
        }
    }
}


