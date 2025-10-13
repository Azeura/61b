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
    public static final File Refs = join(GITLET_DIR,"refs");

    public static final File Head = join(Refs,"Head.txt"); //contains file path of current branch
    public static final File Master = join(Refs,"Master.txt");

    public static final File Blobs = join(GITLET_DIR, "blobs");
    public static final File Commits = join(GITLET_DIR,"commits");
    public static final File Index = join(GITLET_DIR,"indice.txt");
    /*
    * .gitlet
     /blobs
     /commits
     /refs
     index
    * */
    /* git init */
    public static void setupPersistency() throws IOException {
        if (checkGitDir()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }

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
        System.out.println("Init ok.");
    }
    /*git init helper method*/
    public static void initCommit() throws IOException {
        // create init commit
        String msg = "initial commit";
        Date tm = new Date(0L);
        Map<String, String> fileTracked = new HashMap<>();
        String parents = null;
        Commit firstCommit = new Commit(msg, tm, fileTracked, parents);

//        // save commit and set head and master
        String shaValue = sha1ByObject(firstCommit);
        writeContents(Master, shaValue);
        writeContents(Head, Master.getPath()); // write master path into head
        firstCommit.saveCommit(shaValue);
    }

    /*git commit*/
    public static void addCommit(String paraMsg) throws IOException, ClassNotFoundException {
        if (!checkGitDir()) {
            System.out.println("Have not init yet.");
            System.exit(0);
        }
        String msg = paraMsg;
        if (paraMsg == null || paraMsg.trim().isEmpty()) {
            System.out.println("Please enter a message.");
            System.exit(0);
        }
        Date tm = new Date(0L);
        Map<String, String> fileTracked = new HashMap<>();
        Commit headCommit = loadHead();
        String headCommitId = loadHeadId();
        Map<String, String> newTrackedFiles = new HashMap<>(headCommit.getTrackedFiles());
        Stage nowStage = Stage.load();
        String currBranchFilePath = readContentsAsString(Head); // it could be master or other branch
        File currBranchFile = new File(currBranchFilePath); // read curr branch file path
        if (nowStage.isEmpty()) { // check if stage area empty
            System.out.println("No changes added to the commit."); // error message no content in index
            System.exit(0); //exit
        }
        if ( headCommit == null ) {
            System.out.println("fatal: Not a gitlet repository.");
            System.exit(0);
        }
        // update tracked files map and serialize new commit
        for (Map.Entry<String, String> entry : nowStage.getStagedForAddition().entrySet()) {
            newTrackedFiles.put(entry.getKey(), entry.getValue());
        }
        // Remove files from the staging area for removal
        for (String filePath : nowStage.getStagedForRemoval().keySet()) {
            newTrackedFiles.remove(filePath);
        }
        Commit newCommit = new Commit(paraMsg, tm, newTrackedFiles, headCommitId);
        String shaValue = Repository.saveCommit(newCommit);
        nowStage.clear();
        nowStage.save();
        // update the pointer file content(shavalue of commit)
       writeContents(currBranchFile, shaValue);
       System.out.println("New commit added.");
       System.out.println("Commit id is: " + shaValue);

    }

    /*git add*/
    public static void addIndex(String fileName) throws IOException, ClassNotFoundException {
        if (!checkGitDir()) {
            System.out.println("Have not init yet.");
            System.exit(0);
        }

        File fileToAdd = formCWDFile(fileName);
        String filePath = fileToAdd.getPath();
        if ( !fileToAdd.exists()  ) {
            System.out.println("File does not exist."); 
            System.exit(0);
        }

        // check if identical to current commit version
        Commit currCommit = loadHead();
        Stage currStage = Stage.load();
        String idOfTrackedFile = (String)currCommit.getTrackedFiles().get(filePath);
        String blobHash = sha1FileContent(fileToAdd);
        if ( idOfTrackedFile != null && idOfTrackedFile.equals(blobHash)) {
            currStage.removeFileOnly(filePath);
            System.out.println("file is identical to current version");
            System.exit(0);
        }

        // update stage for add and serialize blob 
        Stage nowStage = Stage.load();
        nowStage.addFile(filePath, blobHash);
        nowStage.save();
        File blob = formBlobFile(blobHash);
        blob.createNewFile();
        writeContents(blob,readContentsAsString(fileToAdd));
        System.out.println("Add file successfully.");
    }

    /*git rm*/
    public static void rmIndex(String fileName) throws IOException, ClassNotFoundException {
        if (!checkGitDir()) {
            System.out.println("Have not init yet.");
            System.exit(0);
        }

        File fileToRm = formCWDFile(fileName);
        if ( !restrictedDelete(fileToRm) ) {
            System.out.println("No reason to remove the file."); // it exits under cwd
            System.exit(0);
        }
        String filePath = fileToRm.getPath(); 
        Stage nowStage = Stage.load();
        String blobHash = sha1ByObject(fileToRm);
        nowStage.removeFile(filePath, blobHash);
        nowStage.save(); // update index 
        restrictedDelete(fileToRm);// delete in CWD
        File blobFileToRm = formBlobFile(blobHash);
        restrictedDelete(blobFileToRm); // delete blob file 
    }



    // git checkout
    public static void checkOutFile(String fileName) {
        if (!checkGitDir()) {
            System.out.println("Have not init yet.");
            System.exit(0);
        }

        File fileToCheck = formCWDFile(fileName);
        if (fileName == null && fileToCheck.exists() ) {
            System.out.println("no file name or not in work dir");
            System.exit(0);
        }

        // file checkout to current commit
        Commit Head = loadHead();
        File fileChecked = Head.getBlobFile(fileName); // get blob file 
        String fileContents = readContentsAsString(fileChecked);
        writeContents(fileToCheck,fileContents);

    }

    public static void checkOut(String commitId, String fileName) {
        if (!checkGitDir()) {
            System.out.println("Have not init yet.");
            System.exit(0);
        }

        File cwdFile = formCWDFile(fileName);
        // check if commit exits and file exits in commit map 
        Commit targetCommit = loadCommitById(commitId);
        File targetBlob = targetCommit.getBlobFile(fileName);
        if (!targetBlob.exists()) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            System.exit(0);
        }
        String targetContent = readContentsAsString(targetBlob);
        writeContents(cwdFile,targetContent);
    }

    public static void checkOutBranch(String branchName) throws IOException, ClassNotFoundException {
        if (!checkGitDir()) {
            System.out.println("Have not init yet.");
            System.exit(0);
        }
        // check if this branch exits 
        File branch = formRefsFile(branchName); // target branch ref file
        String headCommitId = loadHeadId();
        if (!branch.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        String checkedOutCommitId = readContentsAsString(branch);
        if (headCommitId == checkedOutCommitId) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        checkOutCommit(checkedOutCommitId);
        Commit currCommit = loadCommitById(checkedOutCommitId);
        Stage nowStage = Stage.load();
        nowStage.clear();
        nowStage.save();
        writeContents(Head,branch.getPath());

        System.out.println("checkout branch successfully");
    }

    // just create a new branch at haed commit 
    public static void branch(String branchName){
        if (!checkGitDir()) {
            System.out.println("Have not init yet.");
            System.exit(0);
        }
        File newBranch = formRefsFile(branchName);
        if(newBranch.exists()) {
            System.out.println("The branch has already existed.");
            return;
        }

        String currentCommitId = loadHeadId();
        Utils.writeContents(newBranch,currentCommitId);
        System.out.println("Successfully create branch");
    }

    public static void rmBranch(String branchName) {
        if (!checkGitDir()) {
            System.out.println("Have not init yet.");
            System.exit(0);
        }
        File targetBranchFile = formRefsFile(branchName);
        if (!targetBranchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        String branchPath = targetBranchFile.getPath();
        if (branchPath == readContentsAsString(Head)) { // head pointer to == target branch
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }

        // delete the ref file
        if (restrictedDelete(targetBranchFile) ) {
            System.out.println("remove branch successfully.");
        } else {
            System.out.println("failed to rm branch.");
        }

    }

    public static void reset(String commitId) throws IOException, ClassNotFoundException {
        if (!checkGitDir()) {
            System.out.println("Have not init yet.");
        }
        File targetCommitFile = formCommitFile(commitId);
        if (!targetCommitFile.exists()) {
            System.out.println("No commit with that id exists.");
        }
        if (checkOutCommit(commitId) ) {
            File currBranch = new File(readContentsAsString(Head));
            writeContents(currBranch, commitId);
            Stage nowStage = Stage.load();
            nowStage.clear();
            System.out.println("checkout to that commid successfully.");
        } else {
            System.out.println("Reset failed due to untracked files.");
        }

    }

    // list current branch commits tree backwards
    public static void log() {
        if (!checkGitDir()) {
            System.out.println("Have not init yet.");
        }
        // Start with the commit ID stored in the HEAD file.
        String currentCommitId = readContentsAsString(Head).trim();

        // Loop backwards through the commit chain.
        while (currentCommitId != null && !currentCommitId.isEmpty()) {
            Commit currentCommit = loadCommitById(currentCommitId);
            if (currentCommit == null) {break;} // Should not happen in a healthy repository
            commitInfoPrint(currentCommitId,currentCommit);

            // Get the parent of the current commit to continue the loop.
            List<String> parents = currentCommit.getParents();
            if ( parents.get(0) != null ) {
                currentCommitId = parents.get(0); // Move to the first parent.
            } else {
                System.out.println("No parent.");
            }
        }
    }

    // list all commits info regardless of order 
    public static void gLog() { 
        if (!checkGitDir()) {
            System.out.println("Have not init yet.");
        }
        List<String> allCommits = plainFilenamesIn(Commits); // commitId.txt 
        for(String commitName : allCommits) {
            File currentCommitPath = formCommitFile(commitName);
            Commit currentCommit = readObject(currentCommitPath,Commit.class);
            commitInfoPrint(commitName, currentCommit);
        }
    }

    public static void find(String msg) {
        if (!checkGitDir()) {
            System.out.println("Have not init yet.");
        }
        if (msg == null) {
            System.out.println("Please enter commit message.");
        }
        boolean foundMatch = false;
        List<String> allCommits = plainFilenamesIn(Commits); // get commit file names id.txt
        for(String commitName : allCommits) { // iterate and deserialize them to compare commit message
            File currentCommitPath = Utils.join(Commits,commitName); // commitddir/....txt
            Commit currentCommit = readObject(currentCommitPath,Commit.class);
            if (currentCommit.getMessage().equals(msg))
                System.out.println("Commit ID:" + commitName);
            foundMatch = true;
        }
        if (!foundMatch) {
            System.out.println("Found no commit with that message.");
        }
    }


    public static String getSplitPointId(String givenCommitId) {
        String currentCommitId = loadHeadId();

        // 1. Traverse the current branch history and store all commit IDs in a set.
        Set<String> currentHistory = new HashSet<>();
        String curr = currentCommitId;
        while (curr != null) {
            currentHistory.add(curr);
            Commit c = loadCommitById(curr);
            if (c == null || c.getParents().isEmpty() || c.getParents().get(0) == null) {
                break;
            }
            curr = c.getParents().get(0);
        }

        // 2. Traverse the given branch history until a commit is found in the set.
        String other = givenCommitId;
        while (other != null) {
            if (currentHistory.contains(other)) {
                return other; // Found the earliest common ancestor
            }
            Commit c = loadCommitById(other);
            if (c == null || c.getParents().isEmpty() || c.getParents().get(0) == null) {
                break;
            }
            other = c.getParents().get(0);
        }

        // This should generally not happen if the repository was correctly initialized,
        // as all histories converge on the initial commit.
        return null;
    }

    /* git merge */
    public static void merge(String givenBranchName) throws IOException, ClassNotFoundException {
        if (!checkGitDir()) {
            System.out.println("Have not init yet.");
            System.exit(0);
        }

        Stage nowStage = Stage.load();
        if (!nowStage.isEmpty() || !nowStage.getStagedForRemoval().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }

        File givenBranchFile = formRefsFile(givenBranchName);
        if (!givenBranchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        String currBranchFilePath = readContentsAsString(Head);
        if (formRefsFile(givenBranchName).getPath().equals(currBranchFilePath)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        // IDs of the three commits
        String currentCommitId = loadHeadId();
        String givenCommitId = readContentsAsString(givenBranchFile);
        String splitPointId = getSplitPointId(givenCommitId);

        Commit currentCommit = loadCommitById(currentCommitId);
        Commit givenCommit = loadCommitById(givenCommitId);
        Commit splitPoint = loadCommitById(splitPointId);

        // Fast-Forward and Already-Merged checks
        if (splitPointId.equals(givenCommitId)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        if (splitPointId.equals(currentCommitId)) {
            // Fast-Forward: Current is ancestor of given. Move current branch pointer to given.
            writeContents(new File(currBranchFilePath), givenCommitId);

            // Checkout the given commit (this updates CWD and clears stage)
            checkOutCommit(givenCommitId);

            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }

        // Conflict Check: Untracked files in CWD
        // Need to check untracked files in CWD that are tracked in givenCommit and NOT tracked in currentCommit.
        List<String> cwdFiles = plainFilenamesIn(CWD);
        for (String fileName : cwdFiles) {
            File cwdFile = formCWDFile(fileName);
            String filePath = cwdFile.getPath();

            // Check if file is untracked in current commit and exists in given commit
            if (!currentCommit.getTrackedFiles().containsKey(filePath) && givenCommit.getTrackedFiles().containsKey(filePath)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.out.println("The untracked File Path is:" + filePath);
                System.out.println("current tracked files are:" + currentCommit.getTrackedFiles());
                System.out.println("given commit tracked files are:" + givenCommit.getTrackedFiles());
                System.exit(0);
            }
        }

        // Three-Way Merge Logic

        // Get all unique files from all three commits (current, given, split)
        Set<String> allFiles = new HashSet<>();
        allFiles.addAll(currentCommit.getTrackedFiles().keySet());
        allFiles.addAll(givenCommit.getTrackedFiles().keySet());
        allFiles.addAll(splitPoint.getTrackedFiles().keySet());

        // Track if a conflict occurred
        boolean conflictOccurred = false;

        for (String filePath : allFiles) {
            String splitId = splitPoint.getTrackedFiles().get(filePath);
            String currentId = currentCommit.getTrackedFiles().get(filePath);
            String givenId = givenCommit.getTrackedFiles().get(filePath);

            // Normalize nulls for comparison (null is considered an "ID" for not present)
            splitId = (splitId == null) ? "" : splitId;
            currentId = (currentId == null) ? "" : currentId;
            givenId = (givenId == null) ? "" : givenId;

            // CASE 1: Modified in Given, not in Current (since split)
            if (!splitId.equals(givenId) && splitId.equals(currentId)) {
                // Restore the file to its version in the given branch and stage for addition.
                try {
                    writeFileFromBlob(filePath, givenId);
                    nowStage.addFile(filePath, givenId);
                } catch (IOException e) {
                    System.out.println("Error merging file " + filePath + ": " + e.getMessage());
                }

                // CASE 2: Modified in Current, not in Given (since split)
            } else if (splitId.equals(givenId) && !splitId.equals(currentId)) {
                // File stays as it is (in Current) and is implicitly staged for addition
                // if it wasn't already staged for addition from an earlier commit.
                // In Gitlet, we just leave it in the CWD as is. We don't stage it.
                // It will be included in the next commit from the current commit's map.

                // CASE 3: Deleted in Current, not in Given (since split)
            } else if (splitId.equals(givenId) && currentId.equals("") && !splitId.equals("")) {
                // If it was present in split, deleted in current, but present in given
                // This is a conflict in real Git, but often resolved by keeping the given change.
                // Based on simple spec: If file was not modified in given, but modified in current (deleted), keep current.
                // File is already deleted in current branch's map. No action needed for CWD.
                // Add to stage for removal (if it's not present, it was removed earlier)
                nowStage.getStagedForRemoval().put(filePath, splitId); // Need to use the split ID for removal stage

                // CASE 4: Deleted in Given, not in Current (since split)
            } else if (!splitId.equals(currentId) && givenId.equals("") && !splitId.equals("")) {
                // If it was present in split, deleted in given, but present in current
                // This is a conflict in real Git, but often resolved by keeping the current change.
                // File stays as it is in CWD. No action needed.

                // CASE 5: Conflict - Modified in both, different versions
            } else if (!splitId.equals(currentId) && !splitId.equals(givenId) && !currentId.equals(givenId)) {
                // Conflict: Write file in CWD with conflict markers and stage for addition.
                conflictOccurred = true;

                // Read content of current and given versions
                String currentContent = currentId.isEmpty() ? "" : readContentsAsString(formBlobFile(currentId));
                String givenContent = givenId.isEmpty() ? "" : readContentsAsString(formBlobFile(givenId));

                // Construct conflict content
                String conflictContent = "<<<<<<< HEAD\n"
                        + currentContent
                        + "=======\n"
                        + givenContent
                        + ">>>>>>>";

                writeContents(formCWDFile(filePath), conflictContent);
                nowStage.addFile(filePath, sha1(conflictContent)); // Stage the conflict file

                // CASE 6: Conflict - Deleted in one, modified in the other
            } else if ((!splitId.equals(currentId) && givenId.equals("") && !splitId.equals("")) ||
                    (!splitId.equals(givenId) && currentId.equals("") && !splitId.equals(""))) {

                // Deleted in current, modified in given (current is empty, given is not empty, split is not empty)
                // OR
                // Modified in current, deleted in given (current is not empty, given is empty, split is not empty)
                conflictOccurred = true;

                String currentContent = currentId.isEmpty() ? "" : readContentsAsString(formBlobFile(currentId));
                String givenContent = givenId.isEmpty() ? "" : readContentsAsString(formBlobFile(givenId));

                // Construct conflict content
                String conflictContent = "<<<<<<< HEAD\n"
                        + currentContent
                        + "=======\n"
                        + givenContent
                        + ">>>>>>>";

                writeContents(formCWDFile(filePath), conflictContent);
                nowStage.addFile(filePath, sha1(conflictContent)); // Stage the conflict file

                // CASE 7: Modified in both, but same version (no conflict, just stage)
            } else if (!splitId.equals(currentId) && !splitId.equals(givenId) && currentId.equals(givenId)) {
                // Files are modified but end up with the same content (same ID).
                // Use the given version (which is the same as current), no conflict.
                // We keep current version, so no action on CWD is strictly necessary.

                // CASE 8: Not modified from split point in either branch
            } else if (splitId.equals(currentId) && splitId.equals(givenId)) {
                // No change in either branch. Do nothing.
            }
        }

        nowStage.save(); // Save the updated staging area

        if (conflictOccurred) {
            System.out.println("Encountered a merge conflict.");
        }

        System.out.println("Merge complete. Please commit to finalize.");
 
    }

    // helper method
    public static Commit loadCommitById(String id) { // deserialize commit object by id 
        if (id == null || id.isEmpty()) { // check if this id valid 
            return null;
        }
        File commitFile = formCommitFile(id);
        if (!commitFile.exists()) { // check if this file exists
            // This is an internal error, so we throw an exception.
            throw new GitletException("Fatal: Commit with id " + id + " does not exist.");
        }
        return readObject(commitFile, Commit.class);
    }

    public static boolean checkOutCommit (String checkedOutBranchId) throws IOException {

        // check conflict that untracked currently and tracked in checked out
        Commit nowCommit = loadHead();
        Commit checkedOutCommit = loadCommitById(checkedOutBranchId);
        Map<String, String> nowCommitFiles = nowCommit.getTrackedFiles();
        Map<String, String> checkedOutBranchFiles = checkedOutCommit.getTrackedFiles();
        // Iterate through the files in the branch we want to check out.
        for (String filePath : checkedOutBranchFiles.keySet()) {

            // Check if the file is NOT tracked by the current commit.
            if (!nowCommitFiles.containsKey(filePath)) {
                File potentialConflict = new File(filePath);

                if (potentialConflict.exists()) {
                    // DANGER! This file is untracked locally but exists in the target branch.
                    // Checking out would overwrite it.
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    System.out.println("Untracked file: " + filePath);
                    return false;
                }
            }
        }
        alignCWDFiles(nowCommitFiles, checkedOutBranchFiles);
        return true;
    }

    public static void status() throws IOException, ClassNotFoundException {
        if (!checkGitDir()) {
            System.out.println("Have not init yet.");
            System.exit(0);
        }
        // show branches
        System.out.println("=== Branches ===");
        List<String> refNames = plainFilenamesIn(Refs); // Head.txt with extension 
        String headCommitId = loadHeadId();
        // printout all names and find out which branch is head by identical file content 
        for (String refName : refNames){
            String CommitId = readContentsAsString(Utils.join(Refs,refName));
            if (CommitId.equals(headCommitId) && refName != "Head.txt") { // head branch but not print head
                System.out.println("*"+ refName);
            } else if (refName != "Head.txt") { // other branches(not print head either)
                System.out.println(refName);
            }
        }


        // show staged files
        Stage nowStage = Stage.load();
        Map<String, String> stagedFilesAddition = nowStage.getStagedForAddition();
        Map<String, String> stagedFilesRemoval = nowStage.getStagedForRemoval();
        System.out.println("=== Staged Files ===");
        for (String fileName: stagedFilesAddition.keySet()) { // print absolute path 
            System.out.println(fileName);
        }
        // show files for removal 
        System.out.println("=== Removed Files ===");
        for(String fileName: stagedFilesRemoval.keySet()) {
            System.out.println(fileName);
        }
 
    }
    static public void showFiles() {
        Commit currCommit = loadHead();
        System.out.println("head commit is:" + readContentsAsString(Head));
        System.out.println(currCommit.getTrackedFiles());
    }


    static public Commit loadHead() {
        String currBranchFilePath = readContentsAsString(Head);
        File currBranchFile = new File(currBranchFilePath);
        String currCommitId = readContentsAsString(currBranchFile);
        File currCommit = formCommitFile(currCommitId);
        return readObject(currCommit,Commit.class);
    }

    static public String loadHeadId () {
        String currBranchFilePath = readContentsAsString(Head);
        File currBranchFile = new File(currBranchFilePath);
        String currCommitId = readContentsAsString(currBranchFile);
        return currCommitId;
    }


    // helper method
    public static String sha1ByObject(Serializable obj) {
        byte[] objBtyes = serialize(obj);
        return sha1(objBtyes);
    }

    public static String sha1FileContent(File file) {
        byte[] fileContent = readContents(file);
        return sha1(fileContent);
    }

    public static void commitInfoPrint(String commitId, Commit currentCommit) {
            // Print the commit information in the required format.
            System.out.println("===");
            System.out.println("commit " + commitId);

            // Format the date string as specified.
            Date currentDate = currentCommit.getTime();
            String formattedDate = String.format("Date: %ta %tb %td %tT %tY %tz",
                    currentDate, currentDate, currentDate,
                    currentDate, currentDate, currentDate);
//            System.out.println(formattedDate);
            System.out.println(currentCommit.getMessage());
            System.out.println(); // Extra newline for spacing.
    }

    private static String saveCommit(Commit commit) throws IOException {
        String shaValue = sha1ByObject(commit);
        File commitFile = join(Commits, shaValue + ".txt");
        commitFile.createNewFile();
        writeObject(commitFile, commit);
        return shaValue;
    }

    public static void writeFileFromBlob(String filePath, String blobId) throws IOException {
        File blobFile = formBlobFile(blobId); // Helper to find the blob in .gitlet/objects
        byte[] BlobContent = new byte[0]; 
        BlobContent = Utils.readContents(blobFile); // read contents from blob and write it into destination file 
        File destFile = new File(filePath);
        destFile.createNewFile();
        Utils.writeContents(destFile,  BlobContent);
    }

    public static File formBlobFile(String blobId) {
        return Utils.join(Blobs,blobId + ".txt");
    }
    public static File formRefsFile(String branchName) {
        return Utils.join(Refs,branchName + ".txt");
    }
    public static File formCommitFile(String commitId) {
        return Utils.join(Commits,commitId + ".txt");
    }
    public static File formCWDFile(String fileName) {
        return Utils.join(CWD ,fileName);
    }

    public static void alignCWDFiles(Map<String, String> nowCommitFiles, Map<String, String> checkedOutBranchFiles) throws IOException {
        // if everything is ok then replace cwd with checked out branch and move head
        for (String filePath : nowCommitFiles.keySet()) {
            if (!checkedOutBranchFiles.containsKey(filePath)) {// delete target commit untraced files in cwd
                File fileToDelete = new File(filePath);
                Utils.restrictedDelete(fileToDelete);
            }
        }

        for (Map.Entry<String, String> entry : checkedOutBranchFiles.entrySet()) {
            String filePath = entry.getKey();  
            String blobId = entry.getValue();  
            writeFileFromBlob(filePath, blobId ); // write files from target into CWD
        }
    }
    public static Boolean checkGitDir() {
        if (GITLET_DIR.exists()) {
            return true;

        } else {
            return false;
        }
    } 
}



