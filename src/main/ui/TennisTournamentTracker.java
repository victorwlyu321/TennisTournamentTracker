package ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import model.Player;
import model.Tournament;
import persistence.JsonReader;
import persistence.JsonWriter;

// Referenced TellerApp and Flashcard Reviewer for UI structure, logic flow, and design
// Referenced WorkRoomApp in JsonSerializationDemo for incorporating json reading and writing

// A Tennis Tournament Tracker that allows users to add tennis players to a tournament, 
// view the list of players, specify the winner and loser of a match, and view players' win-loss records
public class TennisTournamentTracker {
    private Tournament tournament;
    private Scanner input;
    private boolean isRunning;
    private JsonReader jsonReader;
    private JsonWriter jsonWriter;
    private static final String JSON_FILE = "./data/TennisTournamentTracker.json";

    // EFFECTS: runs the tennis tournament tracker
    public TennisTournamentTracker() throws FileNotFoundException {
        init();

        printDivider();
        System.out.println("Welcome to the Tennis Tournament Tracker!");
        printDivider();

        while (this.isRunning) {
            displayMenu();
            String command = this.input.nextLine();
            command = command.toLowerCase();

            processCommands(command);
        }
    }

    // MODIFIES: this
    // EFFECTS: initializes tennis tournament with starting values
    public void init() throws FileNotFoundException {
        this.tournament = new Tournament();
        this.input = new Scanner(System.in);
        this.isRunning = true;
        jsonReader = new JsonReader(JSON_FILE);
        jsonWriter = new JsonWriter(JSON_FILE);
    }

    // EFFECTS: displays menu options to user
    private void displayMenu() {
        System.out.println("Please select from the following options:\n");
        System.out.println("a: Add a new tennis player to the tournament");
        System.out.println("v: View all players in the tournament");
        System.out.println("p: Specify the winner and loser of a match");
        System.out.println("r: View players' win-loss records");
        System.out.println("s: Save Tennis Tournament Tracker to file");
        System.out.println("l: Load Tennis Tournament Tracker from file");
        System.out.println("q: Exit the application");
    }

    // MODIFIES: this
    // EFFECTS: processes user's input
    private void processCommands(String input) {
        if (input.equals("a")) {
            addNewPlayer();
        } else if (input.equals("v")) {
            displayPlayers();
        } else if (input.equals("p")) {
            specifyPlayer();
        } else if (input.equals("r")) {
            displayPlayerRecord();
        } else if (input.equals("s")) {
            saveTournament();
        } else if (input.equals("l")) {
            loadTournament();
        } else if (input.equals("q")) {
            quitTracker();
        } else {
            System.out.println("Sorry, please choose a valid option from the menu.");
            printDivider();
        }
    }

    // MODIFIES: this
    // EFFECTS: creates and adds a tennis player to the list of players in the tournament
    //          if the player is not already in the tournament
    private void addNewPlayer() {
        System.out.println("Please enter the tennis player's name.");
        String newPlayerName = this.input.nextLine();
        if (tournament.addPlayer(newPlayerName)) {
            System.out.println(newPlayerName + " has been successfully added to the tournament!");
            printDivider();
        } else {
            System.out.println("The player entered is already in the tournament.");
            printDivider();
        }
    }

    // EFFECTS: displays the list of players in the tournament
    private void displayPlayers() {
        ArrayList<Player> players = tournament.getPlayers();
        printDivider();
        if (players.isEmpty()) {
            System.out.println("There are no players in the tournament!");
        } else {
            System.out.println("Here is the list of players in the tournament:");
            for (Player p : players) {
                System.out.println(p.getName());
            }
        }
        printDivider();
    }

    // MODIFIES: this
    // EFFECTS: allows users to specify the winner and loser of a match if there are enough players 
    //          in the tournament for a match to be played
    private void specifyPlayer() {
        if (!enoughPlayers()) {
            printNotEnoughPlayers();
            printDivider();
        } else {
            displayPlayers();
            String previousWinner = specifyWinner();
            specifyLoser(previousWinner);
            System.out.println("The winner and loser of the match have been successfully recorded.");
            printDivider();
        }
    }

    // MODIFIES: this
    // EFFECTS: specifies the winner, increases the number of match wins for that player and returns the winner
    //          if the winner entered is in the tournament
    private String specifyWinner() {
        boolean playerNotFound = true;
        String winner = "";

        while (playerNotFound) {
            System.out.println("Please enter the name of the winning player:");
            winner = this.input.nextLine();
            Player player = tournament.findPlayer(winner);
            if (player != null) {
                player.increaseMatchWin();
                playerNotFound = false;
            } else {
                printPlayerNotInTournament();
                displayPlayers();
            }
        }
        return winner;
    }

    // REQUIRES: a non-zero length string from user's previous winner input
    // MODIFIES: this
    // EFFECTS: specifies the loser and increases the number of match losses for that player
    //          if the winner from previous input and loser are not the same player and
    //          if the loser entered is in the tournament
    private void specifyLoser(String prevWinner) {
        boolean playerNotFound = true;

        while (playerNotFound) {
            System.out.println("Please enter the name of the losing player:");
            String loser = this.input.nextLine();
            if (loser.equals(prevWinner)) {
                System.out.println("The winner and loser of the match cannot be the same player.");
                continue;
            }
            Player player = tournament.findPlayer(loser);
            if (player != null) {
                player.increaseMatchLoss();
                playerNotFound = false;
            } else {
                printPlayerNotInTournament();
                displayPlayers();
            }
        }
    }

    // EFFECTS: prints out a player's win-loss record if there are enough players in the tournament
    private void displayPlayerRecord() {
        if (!enoughPlayers()) {
            printNotEnoughPlayers();
            printDivider();
        } else {
            displayPlayers();
            System.out.println("Please select a player from the list:");
            String selectedPlayer = this.input.nextLine();
            Player player = tournament.findPlayer(selectedPlayer);
            if (player != null) {
                System.out.println(player.getName() + " - W-L: " 
                                + player.getMatchWins() + "-" + player.getMatchLosses());
                printDivider();
            } else {
                printPlayerNotInTournament();
                displayPlayerRecord();
            }
        }
    }

    // MODIFIES: this
    // EFFECTS: prints out program quitting messages and sets application to not running
    private void quitTracker() {
        printDivider();
        System.out.println("Game, set, match!");
        System.out.println("Thank you for using the Tennis Tournament Tracker!");
        this.isRunning = false;
    }

    // EFFECTS: saves the tournament to file
    private void saveTournament() {
        try {
            jsonWriter.open();
            jsonWriter.write(tournament);
            jsonWriter.close();
            System.out.println("Your Tennis Tournament Tracker has been saved to " + JSON_FILE + "!");
        } catch (FileNotFoundException e) {
            System.out.println("Saving Tennis Tournament Tracker to " + JSON_FILE + "was UNSUCCESSFUL.");
        }
    }

    // MODIFIES: this
    // EFFECTS: loads tournament from file
    private void loadTournament() {
        try {
            tournament = jsonReader.read();
            System.out.println("Your Tennis Tournament Tracker from " + JSON_FILE + "has been successfully loaded!");
        } catch (IOException e) {
            System.out.println("Loading Tennis Tournament Tracker from " + JSON_FILE + "was UNSUCCESSFULL.");
        }
    }

    // EFFECTS: prints out lines as dividers in console
    private void printDivider() {
        System.out.println("=============================================");
    }

    // EFFECTS: prints out player not in tournament message
    private void printPlayerNotInTournament() {
        System.out.println("Sorry, the player you entered is not in the tournament.");
    }

    // EFFECTS: prints out not enough players message
    private void printNotEnoughPlayers() {
        System.out.println("There are not enough players in the tournament for a match to be played.");
    }

    // EFFECTS: returns true if there 2 or more players in the tournament
    private boolean enoughPlayers() {
        return this.tournament.getPlayers().size() >= 2;
    }
}
