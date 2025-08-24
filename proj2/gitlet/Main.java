package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // TODO: what if args is empty?
        if ( args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                Repository.setupPersistency();
                break;
            case "add":
                Repository.addIndex(args[1]);
                break;
            case "rm":
                Repository.rmIndex(args[1]);
            case "commit":
                Repository.addCommit(args[1]);
                break;
            case "checkout":
                break;
            case "branch":
                break;
        }
    }
}
