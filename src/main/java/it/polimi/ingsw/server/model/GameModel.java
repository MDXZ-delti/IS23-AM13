package it.polimi.ingsw.server.model;

import it.polimi.ingsw.utils.SettingLoader;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameModel {

    // This value is tied to the switch statement in the PersonalGoal class
    private static final int personalGoalDeckSize = 12;
    private final List<PersonalGoal> personalGoalDeck;
    private final List<Player> players;
    private final List<Integer> topScoringPoints;
    private Board livingRoom;
    private List<CommonGoal> commonGoalDeck;
    private Player currentPlayer;
    private boolean lastRound;
    private boolean isTheGameEnded;

    public GameModel(List<Player> players) {
        SettingLoader.loadBookshelfSettings();
        this.players = new ArrayList<>();
        this.players.addAll(players);
        livingRoom = new Board(players.size());

        Player.setBoard(livingRoom);

        commonGoalDeck = new ArrayList<>();
        personalGoalDeck = new ArrayList<>();

        try {
            fillCommonGoalDeck(players.size());
            fillPersonalGoalDeck();
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e + ", error in loading the goal decks");
        }
        isTheGameEnded = false;
        topScoringPoints = new ArrayList<>();
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players.clear();
        this.players.addAll(players);
    }

    public void setGame(Board board, List<CommonGoal> commonGoals) {
        Player.setBoard(board);
        Player.setCommonGoal(commonGoals);
        this.livingRoom = board;
    }

    /**
     * @return the top of the scoring list of each common goal card
     */
    public List<Integer> getTopScoringPoints() {
        topScoringPoints.clear();
        for (CommonGoal cg : Player.getCommonGoals()) {
            topScoringPoints.add(cg.getScoringList().get(0));
        }

        return topScoringPoints;
    }

    public void setTopScoringPoints(List<Integer> topScoringPoints) {
        this.topScoringPoints.clear();
        this.topScoringPoints.addAll(topScoringPoints);
    }

    public int getTopOfScoring() {
        return topScoringPoints.get(0);
    }

    /**
     * @param player the player to get the points of
     * @return a list of all the points that the player has earned
     * <ul>
     *   <li>first element(index 0):the points of the personal goal</li>
     *   <li>second element(index 1):the points of the common goal/s</li>
     *   <li>third element(index 2):the points of the bookshelf</li>
     *   <li>fourth element(index 3):the total of all points</li>
     * </ul>
     */
    public List<Integer> getAllPoints(Player player) {
        List<Integer> allPoints = new ArrayList<>();

        allPoints.add(player.getPersonalGoalPoints());
        allPoints.add(player.getCommonGoalPoints());
        allPoints.add(player.getBookshelf().getPoints());
        allPoints.add(player.calculateScore());

        return allPoints;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public Board getLivingRoom() {
        return livingRoom;
    }

    // drawPersonalGoal() and drawCommonGoals() are basically the same method,
    // we could parametrize them and use a single method for both.

    /**
     * Creates the <code>personalGoalDeck</code>, the <code>commonGoalDeck</code> and the <code>livingRoom</code>.
     */
    public void start() {
        // Draw a personal goal card for each player
        boolean isFirstGame = false;
        for (Player player : players) {

            player.setBookshelf(new Bookshelf(Bookshelf.getRows(), Bookshelf.getColumns()));

            player.setPersonalGoal(drawPersonalGoal());
            if (player.isFirstPlayer()) {
                currentPlayer = player;
            }
            if (player.isFirstGame()) {
                isFirstGame = true;
            }
        }
        // Draw two common goal cards
        Player.setCommonGoal(drawCommonGoals(isFirstGame));

        livingRoom.fill();
    }

    /**
     * Draws a personal goal card from the personalGoalDeck for the player.
     *
     * @return the personal goal card drawn
     */
    public PersonalGoal drawPersonalGoal() {
        Random randomNumberGenerator = new Random();
        int randomPersonalGoalIndex = randomNumberGenerator.nextInt(personalGoalDeck.size());
        // Remove the extracted personal goal from the deck so that it can't be drawn again.
        PersonalGoal picked = personalGoalDeck.get(randomPersonalGoalIndex);
        personalGoalDeck.remove(randomPersonalGoalIndex);
        return picked;
    }

    /**
     * Draw the appropriate number of common goal cards from the commonGoalDeck and set their scoring according to the number of players.
     * The number of common goal cards is 1 if it is the first game for any of the players, otherwise it is 2.
     *
     * @return the list of common goal cards drawn
     */
    public List<CommonGoal> drawCommonGoals(boolean isFirstGame) {
        Random randomNumberGenerator = new Random();
        List<CommonGoal> extracted = new ArrayList<>();

        int commonGoalNumber = isFirstGame ? 1 : 2;

        for (int i = 0; i < commonGoalNumber; i++) {
            int randomLayoutIndex = randomNumberGenerator.nextInt(commonGoalDeck.size());
            extracted.add(commonGoalDeck.get(randomLayoutIndex));
            // Remove the extracted common goal from the deck so that it can't be drawn again.
            commonGoalDeck.remove(randomLayoutIndex);
        }

        return extracted;
    }

    /**
     * Creates a deck with all the possible layouts for the common goals.
     */
    private void fillCommonGoalDeck(int numOfPlayers) throws IOException, ParseException {
        // int dimension = Math.min(Bookshelf.getColumns(), Bookshelf.getRows());
        commonGoalDeck = SettingLoader.commonGoalLoader(numOfPlayers);
    }

    /**
     * Creates a deck with all the possible personal goals.
     * A personal goal is a matrix with highlighted spaces with the corresponding item tiles
     * that players have to replicate in their bookshelves to get points.
     */
    public void fillPersonalGoalDeck() throws IOException, ParseException {
        //        personalGoalDeck = SettingLoader.personalGoalLoader();
        for (int i = 0; i < 12; i++) {
            personalGoalDeck.add(SettingLoader.loadSpecificPersonalGoal(i));
        }
    }

    /**
     * Moves column or a row of items from the board to the player's bookshelf. If the bookshelf is full, the player gets the end game card and
     * the last round starts. At the end, the turn is changed.
     *
     * @param items  the list of coordinates of the items to be moved
     * @param column the index of the bookshelf's column where the items will be moved
     */
    public void move(List<Item> items, int column) {
        if (!currentPlayer.getBookshelf().isBookshelfFull()) {
            currentPlayer.move(items, column);
            if (currentPlayer.getBookshelf().isBookshelfFull()) {
                if (!lastRound) {
                    currentPlayer.setHasEndGameCard(true);
                    lastRound = true;
                }
            }
        }
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    /**
     * Changes the turn to the next player.
     */

    public List<CommonGoal> getCommonGoalDeck() {
        return commonGoalDeck;
    }

    public List<PersonalGoal> getPersonalGoalDeck() {
        return personalGoalDeck;
    }

    public boolean isTheGameEnded() {
        return isTheGameEnded;
    }

    public void setTheGameEnded(boolean theGameEnded) {
        isTheGameEnded = theGameEnded;
    }

    public boolean isLastRound() {
        return lastRound;
    }

    public void setLastRound(boolean lastRound) {
        this.lastRound = lastRound;
    }
}
