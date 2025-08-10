package capers;

import java.io.File;
import java.io.IOException;

import static capers.Utils.*;

/** A repository for Capers 
 * @author TODO
 * The structure of a Capers Repository is as follows:
 *
 * .capers/ -- top level folder for all persistent data in your lab12 folder
 *    - dogs/ -- folder containing all of the persistent data for dogs
 *    - story -- file containing the current story
 *
 * TODO: change the above structure if you do something different.
 */
public class CapersRepository {
    /** Current Working Directory. */
    static final File CWD = new File(System.getProperty("user.dir"));

    /** Main metadata folder. */
    static final File CAPERS_FOLDER = Utils.join(CWD,".capers"); // TODO Hint: look at the `join`
                                            //      function in Utils
    static final File story = Utils.join(CAPERS_FOLDER,"story.txt");

    /**
     * Does required filesystem operations to allow for persistence.
     * (creates any necessary folders or files)
     * Remember: recommended structure (you do not have to follow):
     *
     * .capers/ -- top level folder for all persistent data in your lab12 folder
     *    - dogs/ -- folder containing all of the persistent data for dogs
     *    - story -- file containing the current story
     */
    public static void setupPersistence() {
        if (!CAPERS_FOLDER.exists() && !CAPERS_FOLDER.mkdir()) {
            throw new RuntimeException("Could not create .capers directory.");
        }
        if (!Dog.DOG_FOLDER.exists() && !Dog.DOG_FOLDER.mkdir()) {
            throw new RuntimeException("Could not create dogs directory.");
        }
        try {
            story.createNewFile();
        } catch (IOException e) {
            // Handle the exception here. For example, you might print an error message.
            System.err.println("Failed to create the story file: " + e.getMessage());
            // You could also throw a custom RuntimeException if the program cannot proceed.
            throw new RuntimeException("Could not set up persistence folders and files.", e);
        }
    }

    /**
     * Appends the first non-command argument in args
     * to a file called `story` in the .capers directory.
     * @param text String of the text to be appended to the story
     */
    public static void writeStory(String text) {
        // TODO
        String existingStory = readContentsAsString(story);
        String newStory = existingStory + text + "\n";
        writeContents(story, newStory);
        System.out.println(newStory);
    }

    /**
     * Creates and persistently saves a dog using the first
     * three non-command arguments of args (name, breed, age).
     * Also prints out the dog's information using toString().
     */
    public static void makeDog(String name, String breed, int age) throws IOException {
        // TODO
        // create a dog object, write it into a file, and prints info
        Dog aDog = new Dog(name,breed,age);
        aDog.saveDog();
        String newString = aDog.toString();
        System.out.println(newString);
    }

    /**
     * Advances a dog's age persistently and prints out a celebratory message.
     * Also prints out the dog's information using toString().
     * Chooses dog to advance based on the first non-command argument of args.
     * @param name String name of the Dog whose birthday we're celebrating.
     */
    public static void celebrateBirthday(String name) throws IOException {
        // TODO
        // choose the file by the name, deserialize it, and add one age number, save it, and then print info
        Dog birDog = Dog.fromFile(name);
        birDog.haveBirthday();
        birDog.saveDog();
    }
}
