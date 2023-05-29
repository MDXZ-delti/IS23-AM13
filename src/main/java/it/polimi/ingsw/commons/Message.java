package it.polimi.ingsw.commons;

import it.polimi.ingsw.server.model.Board;
import it.polimi.ingsw.server.model.Bookshelf;
import it.polimi.ingsw.server.model.CommonGoal;
import it.polimi.ingsw.server.model.Item;
import it.polimi.ingsw.server.model.layouts.Layout;
import it.polimi.ingsw.utils.Color;
import it.polimi.ingsw.utils.Coordinates;
import it.polimi.ingsw.utils.SettingLoader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unchecked")
public class Message implements Serializable {

    private final JSONObject json;

    ///////////////////////////////////////////////////////CONSTRUCTORS////////////////////////////////////////////////////////

    /**
     * Constructor for a normal json message.
     *
     * @param json the json object
     */
    public Message(JSONObject json) {
        this.json = json;
    }

    /**
     * Constructor for the login message.
     * When called, only one of the parameters is used, the others are set to default values.
     *
     * @param category    category of the message (what's the message about)
     * @param username    username of the player
     * @param age         age of the player
     * @param isFirstGame if it's the first game of the player
     * @param numPlayer   number of players in the game
     */
    public Message(String category, String username, int age, boolean isFirstGame, int numPlayer) {
        json = new JSONObject();
        String ageString = Integer.toString(age);
        String numString = Integer.toString(numPlayer);
        json.put("category", category);
        json.put("argument", username);
        json.put("value", ageString);
        json.put("bool", isFirstGame);
        json.put("numPlayer", numString);
    }

    /**
     * Constructor for the startGame message.
     *
     * @param personalGoal   personal goal of the player
     * @param commonGoalList list of common goals (1 or 2)
     * @param bookshelves    hashmap of bookshelves (bookshelf, username of the player who owns it)
     * @param board          board of the game
     * @param topScoringList list of top scoring of common goals
     */

    public Message(int personalGoal, List<CommonGoal> commonGoalList, HashMap<Bookshelf, String> bookshelves, Board board, List<Integer> topScoringList) {
        json = new JSONObject();
        String personalGoalString = Integer.toString(personalGoal);
        SettingLoader.loadBookshelfSettings();
        json.put("category", "startGame");
        json.put("personal_goal", personalGoalString);
        for (int i = 0; i < commonGoalList.size(); i++) {
            Layout layout = commonGoalList.get(i).getLayout();
            json.put("commonGoalLayout " + i, layout.getName());

            if ("fullLine".equals(commonGoalList.get(i).getLayout().getName())) {
                json.put("occurrences " + i, layout.getOccurrences());
                json.put("horizontal " + i, layout.isHorizontal());
                json.put("size " + i, 0);
            } else if ("group".equals(layout.getName())) {
                json.put("occurrences " + i, layout.getOccurrences());
                json.put("horizontal " + i, false);
                json.put("size " + i, layout.getSize());
            } else {
                json.put("occurrences " + i, 0);
                json.put("horizontal " + i, false);
                json.put("size " + i, 0);
            }
        }
        JSONArray bookshelfArray = new JSONArray();
        for (Bookshelf bookshelf : bookshelves.keySet()) {
            JSONObject bookshelfJson = new JSONObject();
            bookshelfJson.put("bookshelf", bookshelfJson(bookshelf));
            bookshelfJson.put("username", bookshelves.get(bookshelf));
            bookshelfArray.add(bookshelfJson);
        }
        json.put("bookshelves", bookshelfArray);

        json.put("board", boardJson(board));

        JSONArray scoringList = new JSONArray();
        for (Integer integer : topScoringList) {
            JSONObject scoringJson = new JSONObject();
            scoringJson.put("score", integer);
            scoringList.add(scoringJson);
        }
        json.put("topScoringList", scoringList);
    }

    /**
     * Constructor for the update message.
     *
     * @param category    category of the message (what's the message about)
     * @param bookshelves hashmap of bookshelves (bookshelf, username of the player who owns it)
     * @param board       board of the game
     * @param score       list of current scoring of the player. Each element is a different type of scoring
     */

    public Message(String category, HashMap<Bookshelf, String> bookshelves, Board board, List<Integer> score) {
        json = new JSONObject();
        json.put("category", category);
        JSONArray bookshelfArray = new JSONArray();
        for (Bookshelf bookshelf : bookshelves.keySet()) {
            JSONObject bookshelfJson = new JSONObject();
            bookshelfJson.put("bookshelf", bookshelfJson(bookshelf));
            bookshelfJson.put("username", bookshelves.get(bookshelf));
            bookshelfArray.add(bookshelfJson);
        }
        json.put("bookshelves", bookshelfArray);

        json.put("board", boardJson(board));
        JSONArray points = new JSONArray();
        for (Integer integer : score) {
            JSONObject scoreJson = new JSONObject();
            scoreJson.put("score", integer);
            points.add(scoreJson);
        }
        json.put("currentPointsList", points);
    }

    /**
     * Constructor for an int content message.
     *
     * @param category category of the message (what's the message about)
     * @param name     name of the content
     * @param number   content of the message
     */
    public Message(String category, String name, int number) {
        json = new JSONObject();
        String numString = Integer.toString(number);
        json.put("category", category);
        json.put(name, numString);
    }

    /**
     * Constructor for the single message.
     * It is used for response messages from the server.
     *
     * @param singleMessage message to be sent
     */
    public Message(String singleMessage) {
        json = new JSONObject();
        json.put("category", singleMessage);
    }

    /**
     * Constructor for the winners message.
     *
     * @param size  size of the list of winners
     * @param names usernames of the winners
     */
    public Message(int size, List<String> names, List<Integer> scores) {
        json = new JSONObject();
        json.put("category", "winners");
        json.put("size", size);
        JSONArray winnersArray = new JSONArray();
        for (int i = 0; i < size; i++) {
            JSONObject winnerJson = new JSONObject();
            winnerJson.put("name", names.get(i));
            winnerJson.put("score", scores.get(i));
        }
        json.put("winners", winnersArray);
    }

    /**
     * Constructor for the pick message . (Coordinates of items picked from the board)
     *
     * @param from coordinates of the first item picked
     * @param to   coordinates of the last item picked
     */

    public Message(Coordinates from, Coordinates to) {
        json = new JSONObject();

        int startRow = from.x();
        int startColumn = from.y();
        int finalRow = to.x();
        int finalColumn = to.y();
        String startRowString = Integer.toString(startRow);
        String startColumnString = Integer.toString(startColumn);
        String finalRowString = Integer.toString(finalRow);
        String finalColumnString = Integer.toString(finalColumn);

        json.put("category", "pick");
        json.put("startRow", startRowString);
        json.put("startColumn", startColumnString);
        json.put("finalRow", finalRowString);
        json.put("finalColumn", finalColumnString);

        int size = 0;
        if (startRow == finalRow) {
            size = finalColumn - startColumn + 1;
        } else if (startColumn == finalColumn) {
            size = finalRow - startRow + 1;
        }
        json.put("size", String.valueOf(size));
    }

    /**
     * Constructor for the items picked message. (Items picked from the board)
     *
     * @param picked list of items picked
     */
    public Message(List<Item> picked) {
        json = new JSONObject();
        json.put("category", "picked");
        JSONArray ItemList = new JSONArray();
        for (Item item : picked) {
            JSONObject Item = new JSONObject();
            Item.put("color", item.color().toString());
            String valueString = Integer.toString(item.number());
            Item.put("value", valueString);
            ItemList.add(Item);
        }
        json.put("picked", ItemList);
    }

    /**
     * Constructor for the sort message. (the new order of the items picked)
     *
     * @param category category of the message (sort)
     * @param sort     list of the new order of the items picked
     */
    public Message(String category, List<Integer> sort) {
        json = new JSONObject();
        json.put("category", category);
        JSONArray array = new JSONArray();
        array.addAll(sort);
        json.put("sort", array);
    }

    /**
     * Constructor for the disconnection message. (the username/s of the player/s who disconnected)
     *
     * @param category     category of the message (disconnection)
     * @param disconnected list of the username/s of the player/s who disconnected
     */
    public Message(String category, String type, List<String> disconnected) {
        json = new JSONObject();
        json.put("category", category);
        JSONArray array = new JSONArray();
        array.addAll(disconnected);
        json.put("disconnected", array);
    }

    ///////////////////////////////////////////////////////DA SISTEMARE////////////////////////////////////////////////////////

    // la uso?
    public Message(String type, String argument) {
        json = new JSONObject();
        json.put("category", type);
        json.put("argument", argument);
    }

    // si può mettere in int message
    // è un turno
    public Message(String category, int position) {
        json = new JSONObject();
        String posString = Integer.toString(position);
        json.put("category", category);
        json.put("position", posString);
    }

    // è una insert
    // public Message(String category, String type, int n) {
    //     json = new JSONObject();
    //     json.put("category", category);
    //     json.put(type, n);
    // }

    // si può mettere in int message

    public Message(int position) {
        json = new JSONObject();
        String posixString = Integer.toString(position);
        json.put("category", "index");
        json.put("position", posixString);
    }

    // da sostituire con l'altro

    /**
     * Constructor for the game message type (Goals, Bookshelf, and Board)
     *
     * @param personalGoal   personal goal of the player
     * @param commonGoalList list of common goals
     * @param bookshelves    bookshelf of the players
     * @param board          board of the game
     */
    public Message(int personalGoal, List<CommonGoal> commonGoalList, HashMap<Bookshelf, String> bookshelves, Board board) {
        json = new JSONObject();
        String personalGoalString = Integer.toString(personalGoal);
        SettingLoader.loadBookshelfSettings();
        json.put("category", "startGame");
        json.put("personal_goal", personalGoalString);
        for (int i = 0; i < commonGoalList.size(); i++) {
            Layout layout = commonGoalList.get(i).getLayout();
            json.put("commonGoalLayout " + i, layout.getName());

            if ("fullLine".equals(commonGoalList.get(i).getLayout().getName())) {
                json.put("occurrences " + i, layout.getOccurrences());
                json.put("horizontal " + i, layout.isHorizontal());
                json.put("size " + i, 0);
            } else if ("group".equals(layout.getName())) {
                json.put("occurrences " + i, layout.getOccurrences());
                json.put("horizontal " + i, false);
                json.put("size " + i, layout.getSize());
            } else {
                json.put("occurrences " + i, 0);
                json.put("horizontal " + i, false);
                json.put("size " + i, 0);
            }
        }
        JSONArray bookshelfArray = new JSONArray();
        for (Bookshelf bookshelf : bookshelves.keySet()) {
            JSONObject bookshelfJson = new JSONObject();
            bookshelfJson.put("bookshelf", bookshelfJson(bookshelf));
            bookshelfJson.put("username", bookshelves.get(bookshelf));
            bookshelfArray.add(bookshelfJson);
        }
        json.put("bookshelves", bookshelfArray);

        json.put("board", boardJson(board));
    }

    // la uso??

    /**
     * Constructor for the board message type.
     *
     * @param category category of the message (Board)
     * @param board    board of the game
     */
    public Message(String category, Board board) {
        json = new JSONObject();
        json.put("category", category);
        json.put("board", boardJson(board));
    }

    // da sostituire con l'altro

    public Message(String category, HashMap<Bookshelf, String> bookshelves, Board board, int score) {
        json = new JSONObject();
        json.put("category", category);
        JSONArray bookshelfArray = new JSONArray();
        for (Bookshelf bookshelf : bookshelves.keySet()) {
            JSONObject bookshelfJson = new JSONObject();
            bookshelfJson.put("bookshelf", bookshelfJson(bookshelf));
            bookshelfJson.put("username", bookshelves.get(bookshelf));
            bookshelfArray.add(bookshelfJson);
        }
        json.put("bookshelves", bookshelfArray);

        json.put("board", boardJson(board));
        json.put("score", score);
    }

    ///////////////////////////////////////////////////////GETTERS////////////////////////////////////////////////////////

    public List<String> getDisconnected() {
        List<String> disconnected = new ArrayList<>();
        JSONArray array = (JSONArray) json.get("disconnected");
        for (int i = 0; i < array.size(); i++) {
            String name = (String) json.get(i);
            disconnected.add(name);
        }
        return disconnected;
    }

    public List<Integer> getPick() {
        List<Integer> pick = new ArrayList<>();
        pick.add(Integer.parseInt((String) json.get("startRow")));
        pick.add(Integer.parseInt((String) json.get("startColumn")));
        pick.add(Integer.parseInt((String) json.get("finalRow")));
        pick.add(Integer.parseInt((String) json.get("finalColumn")));
        return pick;
    }

    public String getArgument() {
        return (String) json.get("argument");
    }

    public List<String> getWinners() {
        List<String> winners = new ArrayList<>();
        String sizeString = (String) json.get("size");
        int size = Integer.parseInt(sizeString);
        for (int i = 0; i < size; i++) {
            String name = (String) json.get("name" + i);
            winners.add(name);
        }
        return winners;
    }

    public List<Integer> getSort() {
        List<Integer> sort = new ArrayList<>();
        JSONArray array = (JSONArray) json.get("sort");
        for (Object object : array) {
            String objectString = object.toString();
            sort.add(Integer.parseInt(objectString));
        }
        return sort;
    }

    public JSONArray bookshelfJson(Bookshelf bookshelf) {
        JSONArray bookshelfItemList = new JSONArray();
        for (int i = 0; i < Bookshelf.getRows(); i++) {
            for (int j = 0; j < Bookshelf.getColumns(); j++) {
                JSONObject bookshelfItem = new JSONObject();
                String rowString = String.valueOf(i);
                String columnString = String.valueOf(j);
                bookshelfItem.put("row", rowString);
                bookshelfItem.put("column", columnString);
                JSONObject item = new JSONObject();
                if (bookshelf.getItemAt(i, j).isPresent()) {
                    JSONArray itemThings = new JSONArray();
                    item.put("color", bookshelf.getItemAt(i, j).get().color().toString());
                    String valueString = Integer.toString(bookshelf.getItemAt(i, j).get().number());
                    item.put("value", valueString);
                    itemThings.add(item);
                    bookshelfItem.put("item", itemThings);
                } else {
                    bookshelfItem.put("item", null);
                }
                bookshelfItemList.add(bookshelfItem);
            }
        }
        return bookshelfItemList;
    }

    public JSONArray boardJson(Board board) {
        JSONArray boardMatrix = new JSONArray();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                JSONObject boardItem = new JSONObject();
                String rowString = String.valueOf(i);
                String columnString = String.valueOf(j);
                boardItem.put("row", rowString);
                boardItem.put("column", columnString);
                JSONObject item = new JSONObject();
                if (board.getItem(i, j) == null) {
                    boardItem.put("item", null);
                } else {
                    JSONArray itemThings = new JSONArray();
                    item.put("color", board.getItem(i, j).color().toString());
                    String valueString = Integer.toString(board.getItem(i, j).number());
                    item.put("value", valueString);
                    itemThings.add(item);
                    boardItem.put("item", itemThings);
                }
                boardMatrix.add(boardItem);
            }
        }
        return boardMatrix;
    }

    public int getTurn() {
        String turnString = json.get("turn").toString();
        return Integer.parseInt(turnString);
    }

    public int getIntMessage(String type) {
        String typeString = json.get(type).toString();
        return Integer.parseInt(typeString);
    }

    public int getInsert() {
        String insertString = json.get("insert").toString();
        return Integer.parseInt(insertString);
    }

    public String getCategory() {
        return (String) json.get("category");
    }

    public String getUsername() {
        return (String) json.get("argument");
    }

    public int getAge() {
        String ageString = (String) json.get("age");
        return Integer.parseInt(ageString);
    }

    public boolean getFirstGame() {
        return (boolean) json.get("bool");
    }

    public int getNumPlayer() {
        String numPlayer = (String) json.get("numOfPlayers");
        return Integer.parseInt(numPlayer);
    }

    public String getMessage() {
        return (String) json.get("category");
    }

    public int getPosition() {
        String position = (String) json.get("position");
        return Integer.parseInt(position);
    }

    public int getPersonalGoal() {
        String personalGoal = (String) json.get("personal_goal");
        return Integer.parseInt(personalGoal);
    }

    public List<Item> getPicked() {
        List<Item> picked = new ArrayList<>();
        JSONArray pickedJson = (JSONArray) json.get("picked");
        for (Object obj : pickedJson) {
            JSONObject item = (JSONObject) obj;
            String color = (String) item.get("color");
            String valueString = (String) item.get("value");
            int value = Integer.parseInt(valueString);
            picked.add(new Item(Color.valueOf(color), value));
        }
        return picked;
    }

    public Bookshelf getBookshelf() {
        // TODO: change JSON Bookshelf to and array of columns (array of arrays)
        Bookshelf bookshelf = new Bookshelf();
        JSONArray bookshelfJson = (JSONArray) json.get("bookshelf");
        for (Object obj : bookshelfJson) {
            JSONObject bookshelfItem = (JSONObject) obj;
            String rowString = (String) bookshelfItem.get("row");
            String columnString = (String) bookshelfItem.get("column");
            int row = Integer.parseInt(rowString);
            int column = Integer.parseInt(columnString);
            JSONArray itemJson = (JSONArray) bookshelfItem.get("item");
            if (itemJson == null) {
                bookshelf.setItem(row, column, Optional.empty());
            } else {
                JSONObject item = (JSONObject) itemJson.get(0);
                String color = (String) item.get("color");
                String valueString = (String) item.get("value");
                int value = Integer.parseInt(valueString);
                bookshelf.setItem(row, column, Optional.of(new Item(Color.valueOf(color), value)));
            }
        }
        return bookshelf;
    }

    public HashMap<Bookshelf, String> getAllBookshelves() {
        HashMap<Bookshelf, String> bookshelves = new HashMap<>();
        JSONArray bookshelfJson = (JSONArray) json.get("bookshelves");

        for (Object obj : bookshelfJson) {
            JSONObject bookshelfItem = (JSONObject) obj;
            String username = (String) bookshelfItem.get("username");
            Bookshelf bookshelf = new Bookshelf();
            JSONArray bookshelfArray = (JSONArray) bookshelfItem.get("bookshelf");
            for (Object obj2 : bookshelfArray) {
                JSONObject bookshelfItem2 = (JSONObject) obj2;
                String rowString = (String) bookshelfItem2.get("row");
                String columnString = (String) bookshelfItem2.get("column");
                int row = Integer.parseInt(rowString);
                int column = Integer.parseInt(columnString);
                JSONArray itemJson = (JSONArray) bookshelfItem2.get("item");
                if (itemJson == null) {
                    bookshelf.setItem(row, column, Optional.empty());
                } else {
                    JSONObject item = (JSONObject) itemJson.get(0);
                    String color = (String) item.get("color");
                    String valueString = (String) item.get("value");
                    int value = Integer.parseInt(valueString);
                    bookshelf.setItem(row, column, Optional.of(new Item(Color.valueOf(color), value)));
                }
            }
            bookshelves.put(bookshelf, username);
        }
        return bookshelves;
    }

    public Board getBoard() {
        Board board = new Board();
        JSONArray boardJson = (JSONArray) json.get("board");
        for (Object obj : boardJson) {
            JSONObject boardItem = (JSONObject) obj;
            String rowString = (String) boardItem.get("row");
            String columnString = (String) boardItem.get("column");
            int row = Integer.parseInt(rowString);
            int column = Integer.parseInt(columnString);
            JSONArray itemJson = (JSONArray) boardItem.get("item");
            if (itemJson == null) {
                board.setItem(row, column, null);
            } else {
                JSONObject item = (JSONObject) itemJson.get(0);
                String color = (String) item.get("color");
                String valueString = (String) item.get("value");
                int value = Integer.parseInt(valueString);
                board.setItem(row, column, new Item(Color.valueOf(color), value));
            }
        }
        return board;
    }

    public List<String> getCardType() {
        List<String> cardType = new ArrayList<>();
        int i = 0;
        while (json.get("commonGoalLayout " + i) != null) {
            cardType.add((String) json.get("commonGoalLayout " + i));
            i++;
        }
        return cardType;
    }

    public List<Integer> getCardOccurrences() {
        List<Integer> cardOccurrences = new ArrayList<>();
        int j = 0;
        while (json.get("occurrences " + j) != null) {
            cardOccurrences.add(Integer.parseInt(json.get("occurrences " + j).toString()));
            j++;
        }
        return cardOccurrences;
    }

    public List<Integer> getCardSize() {
        List<Integer> cardSize = new ArrayList<>();
        int j = 0;
        while (json.get("size " + j) != null) {
            cardSize.add(Integer.parseInt(json.get("size " + j).toString()));
            j++;
        }
        return cardSize;
    }

    public List<Boolean> getCardHorizontal() {
        List<Boolean> cardHorizontal = new ArrayList<>();
        int i = 0;
        while (json.get("horizontal " + i) != null) {
            cardHorizontal.add((boolean) json.get("horizontal " + i));
            i++;
        }
        return cardHorizontal;
    }

    public List<Integer> getTopScoring() {
        List<Integer> topScoring = new ArrayList<>();
        JSONArray topScoringJson = (JSONArray) json.get("topScoringList");
        for (Object obj : topScoringJson) {
            JSONObject topScoringItem = (JSONObject) obj;
            String scoreString = (String) topScoringItem.get("score");
            int score = Integer.parseInt(scoreString);
            topScoring.add(score);
        }
        return topScoring;
    }

    public List<Integer> getCurrentPoints() {
        List<Integer> currentPoints = new ArrayList<>();
        JSONArray currentPointsJson = (JSONArray) json.get("currentPointsList");
        for (Object obj : currentPointsJson) {
            JSONObject currentPointsItem = (JSONObject) obj;
            String scoreString = (String) currentPointsItem.get("score");
            int score = Integer.parseInt(scoreString);
            currentPoints.add(score);
        }
        return currentPoints;
    }

    public JSONObject getJson() {
        return json;
    }

    public String getJSONstring() {
        return json.toJSONString();
    }
}
