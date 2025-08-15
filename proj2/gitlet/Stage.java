package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.io.File;
import java.io.IOException;

import static gitlet.Utils.join;

/*a index that has two maps to represent indexA and indexB*/
public class Stage implements Serializable {
    private static File Index = join(Repository.GITLET_DIR,"indice.txt");
    private Map<String, String> stagedForAddition;
    private Map<String, String> stagedForRemoval;

    public Stage() {
        stagedForAddition = new HashMap<>();
        stagedForRemoval = new HashMap<>();
    }

    public void addFile(String filePath, String blobHash) {
        // If a file is being added, it should no longer be staged for removal.
        // The addition command overrides the removal command.
        stagedForRemoval.remove(filePath);
        // Stage the file for addition. This will overwrite any previous entry
        // for the same file, which is the correct behavior.
        stagedForAddition.put(filePath, blobHash);
    }

    /*Stages a file for removal from the next commit.*/
    public void removeFile(String filePath, String blobHash) {
        // If a file is to be removed, it should not be in the staged for addition area.
        stagedForAddition.remove(filePath);
        // Stage the file for removal.
        stagedForRemoval.put(filePath, blobHash);
    }

    /** Clears both staging areas after a successful commit. */
    public void clear() {
        stagedForAddition.clear();
        stagedForRemoval.clear();
    }

    /**
     * Returns the map of files staged for addition.
     * @return a Map of file paths to blob hashes.
     */
    public Map<String, String> getStagedForAddition() {
        return stagedForAddition;
    }

    /**
     * Returns the map of files staged for removal.
     * @return a Map of file paths to blob hashes.
     */
    public Map<String, String> getStagedForRemoval() {
        return stagedForRemoval;
    }

    /**
     * Checks if the staging area is empty.
     * @return true if both staging areas are empty, false otherwise.
     */
    public boolean isEmpty() {
        return stagedForAddition.isEmpty() && stagedForRemoval.isEmpty();
    }

    public void save() throws IOException {
        Utils.writeObject(Index, this);
    }

    public static Stage load() throws IOException, ClassNotFoundException {
        return Utils.readObject(Index, Stage.class);
    }

}
