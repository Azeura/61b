package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.security.Timestamp;
import java.util.*;

import static gitlet.Utils.readObject;


/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private Date time;
    private Map<String,String> TrackedFiles;
    private List<String>parents;

    Commit(String msg, Date tm, Map fileTracked,String pare) {
        this.message = msg;
        this.time= tm;
        this.TrackedFiles = fileTracked;
        this.parents.add(pare);
    }

    void saveCommit(String id) {
        File commitFileName = Utils.join(Repository.CWD, id);
        Utils.writeObject(commitFileName,this);
    }

    public void changeMeta(String msg, Date tm,String pare) {
        this.message = msg;
        this.time= tm;
        this.parents.add(pare);
    }


    boolean isEmpty() {
        if (this.TrackedFiles.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    // get this commit tracked files
    public Map getTrackedFiles(){
        return this.TrackedFiles;
    }

//    //get the string id commit tracked files
//    public Map getTrackedFiles(String id){
//        File parentCommit = Utils.join(Repository.CWD, id);
//        Commit parent = readObject(parentCommit,Commit.class);
//        return parent.getTrackedFiles();
//    } comment it for i think commit shouldnt care other commit

//    // get first parent id
//    public String getFirstParentID() {
//        if (parents.isEmpty()) {
//            return null;
//        }
//        return parents.get(0);
//    } comment it for i think commit shouldnt care other commit

//    public Commit loadCommit(String id){
//        File parentCommit = Utils.join(Repository.CWD, id);
//        Commit parent = readObject(parentCommit,Commit.class);
//        return parent;
//    } comment it for i think commit shouldnt care other commit


    // commit process, after check index and commit msg if empty
    public void updateMap(Stage index) {
        Map<String, String> newFileMap = new HashMap<>();


        newFileMap.putAll(index.getStagedForAddition());

        for (String fileToRemove : index.getStagedForRemoval().keySet()) {
            newFileMap.remove(fileToRemove);

        this.TrackedFiles = newFileMap;

        this.time = new Date(0L);
        index.clear();
        }
    }
}
