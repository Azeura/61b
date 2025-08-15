package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.security.Timestamp;
import java.util.*;


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

    /* TODO: fill in the rest of this class. */
    Commit(String msg, Date tm, String iid, Map fileTracked,List<String> pare) {
        this.message = msg;
        this.time= tm;
        this.TrackedFiles = fileTracked;
        this.parents = pare;
    }

    void saveCommit(String id) {
        File commitFileName = Utils.join(Repository.CWD, id);
        Utils.writeObject(commitFileName,this);
    }

}
