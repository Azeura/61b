package IntList;

import static org.junit.Assert.*;
import org.junit.Test;

public class SquarePrimesTest {

    /**
     * Here is a test for isPrime method. Try running it.
     * It passes, but the starter code implementation of isPrime
     * is broken. Write your own JUnit Test to try to uncover the bug!
     */
    @Test
    public void testSquarePrimesSimple() {
        IntList lst = IntList.of(14, 15, 16, 17, 18);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("14 -> 15 -> 16 -> 289 -> 18", lst.toString());
        assertTrue(changed);
    }
    @Test
    public void testSquarePrimesTest1() {
        IntList lst = IntList.of(2,6,9,10,12);
        assertTrue(IntListExercises.squarePrimes(lst));
        assertEquals("4 -> 6 -> 9 -> 10 -> 12", lst.toString());
    }
    @Test
    public void testSquarePrimesTest2() {
        IntList lst = IntList.of(3,6,9,11,23);
        assertTrue(IntListExercises.squarePrimes(lst));
        assertEquals("9 -> 6 -> 9 -> 121 -> 529", lst.toString());
    }

}
