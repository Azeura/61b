package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Ref;
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

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static File Refs = join(GITLET_DIR,"refs");

    public static File Head = join(Refs,"Head.txt");
    public static File Master = join(Refs,"Master.txt");

    public static File Blobs = join(GITLET_DIR, "blobs");
    public static File Commits = join(GITLET_DIR,"commits");
    public static File Index = join(GITLET_DIR,"indice.txt");
    /*
    * .gitlet
     /blobs
     /commits
     /refs
     index
    * */
    /* git init */
    public static void setupPersistency() throws IOException {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(01);
        }

        // set up .gitlet directory
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

        // create init commit and serialize it
        initCommit();
        Stage thisStage = new Stage();
        thisStage.save();

    }
    /*git init helper method*/
    public static void initCommit() {
        // create init commit
        String msg = "initial commit";
        Date tm = new Date(0L);
        Map<String, String> fileTracked = new HashMap<>();
        String parents = null;
        Commit firstCommit = new Commit(msg, tm, fileTracked, parents);

//        // save commit and set head and master
        String shaValue = sha1ByObject(firstCommit);
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
        if ( parentCommit == null ) {
            System.out.println("not init yet ");
            System.exit(01);
        }
        // update tracked files map and serialize new commit
        parentCommit.updateMap(nowStage);
        parentCommit.changeMeta(paraMsg, tm, Head.getPath() );
        String shaValue = sha1ByObject(parentCommit);
        parentCommit.saveCommit(shaValue);
        // update the pointer
       writeContents(Head, shaValue);
       writeContents(Master, shaValue);
    }

    /*git add*/
    public static void addIndex(String fileName) throws IOException, ClassNotFoundException {
        File fileToAdd = Utils.join(CWD, fileName); // absolute path
        if ( !fileToAdd.exists()  ) {
            System.out.println("File does not exist.");
            System.exit(0)

        }

        // update stage for add and serialize blob 
        String filePath = fileToAdd.getPath(); // path of file to add
        String blobHash = sha1(readContentsAsString(fileToAdd)); 
        Stage addStage = Stage.load();
        addStage.addFile(filePath, blobHash); // here is relative path
        File blob = join(Blobs,blobHash,".txt");
        blob.createNewFile();
        writeContents(blob,readContentsAsString(fileToAdd));
    }

    /*git rm*/
    public static void rmIndex(String fileName) throws IOException, ClassNotFoundException {
        File fileToRm = Utils.join(CWD, fileName);
        if ( !fileToRm.exists()  ) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        String filePath = fileToRm.getPath();
        String blobHash = sha1ByObject(rmFile);
        Stage rmStage = Stage.load();
        rmStage.addFile(filePath, blobHash);
    }

    // helper method
    // load the head commit object
    static public Commit loadHead() {
        return readObject(Head,Commit.class);
    }

    // git checkout
    public static void checkOut(String fileName) {
        File fileToCheck = Utils.join(CWD,fileName + ".txt");
        if (fileName == null && fileToCheck.exists() ) {
            System.out.println("no file name or not in work dir");
        }

        // file checkout to current commit
        Commit Head = loadHead();
        File fileChecked = Head.getBlobFile(fileName);
        String fileContents = readContentsAsString(fileChecked);
        writeContents(fileToCheck,fileContents);

    }

    public static void checkOut(string branchName) {
        // check if this branch exits 
        File branch = join(Refs, branchName,".txt");
        String headCommitId = readContentsAsString(Utils.join(Refs,"Head.txt"));
        String checkedOutBranchId = readContentsAsString(branch);
        if (!branch.exists()) {
            System.out.println("No such branch exists.");
            System.exit(01);
        }
        if (headCommitId == checkedOutBranchId) {
            System.out.println("No need to checkout the current branch.");
            System.exit(01);
        }
        // check if untracked file exits and would be overwritten be checked out branch
        // so the conflict file must be same name and untracked current and tracked in checked out 
        Commit nowCommit = loadHead();
        Commit checkedOutBranch = loadCommitById(checkedOutBranchId);
        Map<String, String> nowCommitFiles = nowCommit.getTrackedFiles();
        Map<String, String> checkedOutBranchFiles = checkedOutBranch.getTrackedFiles();
        // Iterate through the files in the branch we want to check out.
        for (String fileName : checkedOutBranchFiles.keySet()) {

            // Check if the file is NOT tracked by the current commit.
            if (!nowCommitFiles.containsKey(fileName)) {
                File potentialConflict = new File(fileName);

                if (potentialConflict.exists()) {
                    // DANGER! This file is untracked locally but exists in the target branch.
                    // Checking out would overwrite it.
                    System.out.println("There is an untracked file in the way; "
                                     + "delete it, or add and commit it first.");
                    System.out.println("Untracked file: " + fileName);
                    System.exit(0); // Exit the program to prevent the checkout.
                }
            }
        }

        // if everything is ok then replace cwd with checked out branch and move head
        for (String fileName : nowCommitFiles.keySet()) {
            if (!checkedOutBranchFiles.containsKey(fileName)) {
                File fileToDelete = new File(CWD, fileName);
                Utils.restrictedDelete(fileToDelete);
            }
        }

        for (Map.Entry<String, String> entry : checkedOutBranchFiles.entrySet()) {
            String fileName = entry.getKey();
            String blobId = entry.getValue();
            File blobFile = join(Blobs,blobId,".txt"); // Helper to find the blob in .gitlet/objects
            byte[] blobContent = new byte[0]; 
            BlobContent = Utils.readContents(blobFile);
            File destFile = new File(CWD, fileName);
            Utils.writeContents(destFile,  BlobContent);
        }


    }

    // just create a new branch at haed commit 
    public static void branch(String branchName){
        FIle newBranch = Utils.join(Refs,branchName);

        try{
            Files.copy( Head.toPath(), newBranch.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Successfully created branch: " + branchName);

        } catch (IOException e) {
            System.err.println("Error creating branch: " + e.getMessage());
        }
    }

    // list current branch commits tree backwards
    public static void log() {
        // Start with the commit ID stored in the HEAD file.
        String currentCommitId = readContentsAsString(Head).trim();

        // Loop backwards through the commit chain.
        while (currentCommitId != null && !currentCommitId.isEmpty()) {
            Commit currentCommit = loadCommitById(currentCommitId);
            if (currentCommit == null) {
                break; // Should not happen in a healthy repository.
            }

            // Print the commit information in the required format.
            System.out.println("===");
            System.out.println("commit " + currentCommitId);

            // Format the date string as specified.
            Date currentDate = currentCommit.getTime();
            String formattedDate = String.format("Date: %ta %tb %td %tT %tY %tz",
                    currentDate, currentDate, currentDate,
                    currentDate, currentDate, currentDate);
            System.out.println(formattedDate);
            System.out.println(currentCommit.getMessage());
            System.out.println(); // Extra newline for spacing.

            // Get the parent of the current commit to continue the loop.
            List<String> parents = currentCommit.getParents();
            if (parents != null && !parents.isEmpty()) {
                currentCommitId = parents.get(0); // Move to the first parent.
            } else {
                currentCommitId = null; // Reached the initial commit, which has no parents.
            }
        }
    }

    // list all commits info regardless of order 
    public static void gLog() { 
        File CommitsDir = join(GITLET_DIR,"commits");
        List<String> allCommits = plainFilenamesIn(CommitsDir);
        for(String commitName : allCommits) {
            File currentCommitPath = Utils.join(CommitsDir,commitName);
            Commit currentCommit = readObject(currentCommitPath,Commit.class);
            System.out.println("===");
            System.out.println("commit " + commitName);

            // Format the date string as specified.
            Date currentDate = currentCommit.getTime();
            String formattedDate = String.format("Date: %ta %tb %td %tT %tY %tz",
                    currentDate, currentDate, currentDate,
                    currentDate, currentDate, currentDate);
            System.out.println(formattedDate);
            System.out.println(currentCommit.getMessage());
            System.out.println(); // Extra newline for spacing.

        }
    }

    public static void find(String msg) {
        if (msg == null) {
            System.out.println("Please enter commit message.");
            System.exit(01);
        }
        boolean foundMatch = false;
        File CommitsDir = join(GITLET_DIR,"commits");
        List<String> allCommits = plainFilenamesIn(CommitsDir); // get commit file names
        for(String commitName : allCommits) { // iterate and deserialize them to compare commit message
            File currentCommitPath = Utils.join(CommitsDir,commitName);
            Commit currentCommit = readObject(currentCommitPath,Commit.class);
            if (currentCommit.getMessage() == msg)
                System.out.println("Commit ID:" + currentCommit.getMessage());
            foundMatch = true;
        }
        if (!foundMatch) {
            System.out.println("Found no commit with that message.");
        }
    }

    // helper method
    public static Commit loadCommitById(String id) { // deserialize commit object by id 
        if (id == null || id.isEmpty()) { // check if this id valid 
            return null;
        }
        File commitFile = join(GITLET_DIR,"Commits", id);
        if (!commitFile.exists()) { // check if this file exists
            // This is an internal error, so we throw an exception.
            throw new GitletException("Fatal: Commit with id " + id + " does not exist.");
        }
        return readObject(commitFile, Commit.class);
    }

    public static void status() {
        // show branches
        System.out.println("=== Branches ===");
        List<String> refNames = plainFilenamesIn(Refs); // get branch file names list 
        String headCommitId = readContentsAsString(Utils.join(Refs,"Head.txt"));
        // printout all names and find out which branch is head by identical file content 
        for (String refName : refNames){
            String CommitId = readContentsAsString(Utils.join(Refs,refName));
            if (CommitId == headCommitId && refName != "Head.txt") { // head branch but not print head    
                System.out.println("*"+ refName);
            } else if (refName != "Head.txt") { // other branches(not print head either)
                System.out.println(refName);
            }
        }

        // show staged files
        Stage nowStage = Stage.load();
        Map<String, String> stagedFilesAddition = nowStage.getStagedForAddition;
        Map<String, String> stagedFilesRemoval = nowStage.getStagedForRemoval;
        System.out.println("=== Staged Files ===");
        for (String fileName: stagedFilesAddition.keySet()): { // print absolute path 
            System.out.println(fileName);
        }
        // show files for removal 
        System.out.println("=== Removed Files ===")
        for(string fileName: stagedFilesRemoval.keySet()): {
            System.out.println(fileName);
        }

        // modifications but not staged
        // untracked files 
    }

    // public static File getBlobFile( String fileId) {
    //     return join(Blobs,fileId);
    // }

    // helper method
    public static String sha1ByObject(Object obj) {
        byte[] objBtyes = serialize(obj);
        return sha1(objBtyes);
    } 



}
