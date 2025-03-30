package workshop05code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
// Included for the logging exercise
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class App {
    // Start code for logging exercise
    static {
        try {
            // Load logging configuration
            LogManager.getLogManager().readConfiguration(new FileInputStream("resources/logging.properties"));
        } catch (SecurityException | IOException e1) {
            // CHANGED: Log exception instead of printing stack trace
            Logger.getLogger(App.class.getName()).log(Level.WARNING, "Failed to load logging configuration", e1);
        }
    }

    private static final Logger logger = Logger.getLogger(App.class.getName());
    // End code for logging exercise
    
    public static void main(String[] args) {
        SQLiteConnectionManager wordleDatabaseConnection = new SQLiteConnectionManager("words.db");

        wordleDatabaseConnection.createNewDatabase("words.db");
        // CHANGED: Removed non-game related console output; log connection status instead.
        if (wordleDatabaseConnection.checkIfConnectionDefined()) {
            logger.log(Level.INFO, "Wordle created and connected.");
        } else {
            System.out.println("Not able to connect. Sorry!");
            return;
        }
        if (wordleDatabaseConnection.createWordleTables()) {
            logger.log(Level.INFO, "Wordle structures in place.");
        } else {
            System.out.println("Not able to launch. Sorry!");
            return;
        }

        // CHANGED: Modified file reading block to remove printing words to console.
        // Valid words (exactly 4 letters) are logged at INFO and added to the database.
        // Invalid words are logged at SEVERE.
        try (BufferedReader br = new BufferedReader(new FileReader("resources/data.txt"))) {
            String line;
            int i = 1;
            while ((line = br.readLine()) != null) {
                if (line.length() == 4) {
                    // Log valid word (do not print to console)
                    logger.log(Level.INFO, "Valid word from file: " + line);
                    wordleDatabaseConnection.addValidWord(i, line);
                    i++;
                } else {
                    // Log invalid word at SEVERE level
                    logger.log(Level.SEVERE, "Invalid word in data.txt: " + line);
                }
            }
        } catch (IOException e) {
            // CHANGED: Log exception instead of printing trace
            logger.log(Level.WARNING, "Not able to load data.txt.", e);
            System.out.println("Not able to load data. Sorry!");
            return;
        }

        // Game loop – only game-related info is printed to the console.
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter a 4 letter word for a guess or q to quit: ");
            String guess = scanner.nextLine();

            while (!guess.equals("q")) {
                System.out.println("You've guessed '" + guess + "'.");
                if (wordleDatabaseConnection.isValidWord(guess)) {
                    System.out.println("Success! It is in the list.\n");
                } else {
                    System.out.println("Sorry. This word is NOT in the list.\n");
                    // CHANGED: Log invalid guesses at WARNING level
                    logger.log(Level.WARNING, "Invalid guess entered: " + guess);
                }
                System.out.print("Enter a 4 letter word for a guess or q to quit: ");
                guess = scanner.nextLine();
            }
        } catch (NoSuchElementException | IllegalStateException e) {
            // CHANGED: Log exception instead of printing trace
            logger.log(Level.WARNING, "Exception reading input.", e);
        }
    }
}