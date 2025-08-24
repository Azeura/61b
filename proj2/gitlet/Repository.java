package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *  It manages the behaviour of git init, add, and commit.
 *  and it should be pointers created by commit, branch, and checkout
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    public static File Head = join(GITLET_DIR,"head.txt");
    public static File Master = join(GITLET_DIR,"Master.txt");

    /*
    * .gitlet
     /blobs
     /commits
     /refs
     index
    * */
    /* git init */
    public static void setupPersistency() throws IOException {
        if (!GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        File Blobs = join(GITLET_DIR, "blobs");
        File Commits = join(GITLET_DIR,"commits");
        File Refs = join(GITLET_DIR,"refs");
        File Index = join(GITLET_DIR,"indice.txt");
        GITLET_DIR.mkdir();
        Blobs.mkdir();
        Commits.mkdir();
        Refs.mkdir();
        Index.createNewFile();
        Head.createNewFile();
        Master.createNewFile();
        initCommit();
        Stage thisStage = new Stage();
        thisStage.save();

    }
    /*git init helper method*/
    public static void initCommit() {
        String msg = "initial commit";
        Date tm = new Date(0L);
        Map<String, String> fileTracked = new HashMap<>();
        String parents = null;
        Commit firstCommit = new Commit(msg, tm, fileTracked, parents);
        String shaValue = sha1(firstCommit);
        writeContents(Master, shaValue);
        writeContents(Head, shaValue);
        firstCommit.saveCommit(shaValue);
    }

    /*git commit*/
    public static void addCommit(String paraMsg) throws IOException, ClassNotFoundException {

        String msg = paraMsg;
        if (paraMsg == null || paraMsg.trim().isEmpty()) {
            System.out.println("Commit failed: Please enter a commit message.");
        }
        Date tm = new Date(0L);
        Map<String, String> fileTracked = new HashMap<>();
        Commit parentCommit = loadHead();
        Stage nowStage = Stage.load();
        if (nowStage.isEmpty()) { // check if stage area empty
            System.out.println(""); // error message no content in index
            System.exit(01); //exit
        }
        parentCommit.updateMap(nowStage);
        parentCommit.changeMeta(paraMsg, tm, Head.getPath() );
    }

    /*git add*/
    public static void addIndex(String fileName) throws IOException, ClassNotFoundException {
        File addFile = Utils.join(CWD, fileName);
        if ( !addFile.exists()  ) {
            System.out.println("File does not exist.");
        }
        /*System.exit(0);*/

        String filePath = addFile.getPath();
        String blobHash = sha1(addFile);
        Stage addStage = Stage.load();
        addStage.addFile(filePath, blobHash);
    }

    /*git rm*/
    public static void rmIndex(String fileName) throws IOException, ClassNotFoundException {
        File rmFile = Utils.join(CWD, fileName);
        if ( !rmFile.exists()  ) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        String filePath = rmFile.getPath();
        String blobHash = sha1(rmFile);
        Stage rmStage = Stage.load();
        rmStage.addFile(filePath, blobHash);
    }

    // helper method
    // load the head commit object
    static public Commit loadHead() {
        return readObject(Head,Commit.class);
    }

}
