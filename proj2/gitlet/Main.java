package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args)  {
        if ( args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                try {Repository.setupPersistency() ;
                } catch (IOException e) {
                System.out.println("IOException catght:" + e.getMessage());
            }
                break;
            case "add":
                try {Repository.addIndex(args[1]) ;
                } catch (IOException e) {
                    System.out.println("IOException catght:" + e.getMessage());
                }
                break;
            case "rm":
                Repository.rmIndex(args[1]);
            case "commit":
                try {Repository.addCommit(args[1]) ;
                } catch (IOException e) {
                    System.out.println("IOException catght:" + e.getMessage());
                }
                break;
            case "checkout":
                if (args.length == 4 && args[2].equals("--") ) {
                        Repository.checkOut(args[1],args[3]); // commit id -- file name
                    } else if  (args.length == 3 && args[1].equals("--")) {
                        Repository.checkOutFile(args[2]); // -- file name
                    } //branch 
                    else if (args.length == 2) {
                        Repository.checkOutBranch(args[1]);  // branch name
                    } else {
                        System.out.println("Incorrect operands");
                    }          
                break;
            case "branch":
                Repository.branch(args[1]);
                break;
            case "log":
                Repository.log();
                break;
            case "global-log":
                Repository.gLog();
                break;
            case "find":
                Repository.find(args[1]);
                break;
            case "status":
                Repository.status();
                break;
            case "rm -branch":
                Repository.rmBranch(args[1]);
                break;
            case "merge":
                Repository.merge(args[1]);
                break;
            case "currentFile":
                Repository.showFiles();
                break;
        }
    } 
    }

