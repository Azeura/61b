package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeAList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeAListConstruction();
    }

    public static void timeAListConstruction() {
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> opCounts = new AList<>();
        int testLength = 1000;
        for (int i = 0; i < 16; i++) {
            if (i > 0) {
                testLength *= 2;
            }
            Ns.addLast(testLength);
            Stopwatch sw = new Stopwatch();
            AList<Integer> testList = new AList<>();
            for (int p = 0; p < testLength ;  p++) {
                testList.addLast(p);
            }
            double secTimes = sw.elapsedTime();
            times.addLast(secTimes);
        }
        opCounts = Ns;
        printTimingTable(Ns, times, opCounts);

    }
}


// get three parameter: Ns the times func is called, times the seconds it takes to complete calls, opCounts is the same
// with the Ns, the fourth column is calculated by func so dont care about it
// 1000
