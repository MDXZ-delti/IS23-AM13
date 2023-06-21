package it.polimi.ingsw.client.view;

import it.polimi.ingsw.client.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;

public class EndGameGuiController {

    private final GuiView view;
    private final Client client;
    @FXML
    private Button homeButton;

    @FXML
    private Label player1;

    @FXML
    private Label player2;

    @FXML
    private Label player3;

    @FXML
    private Label player0;

    @FXML
    private Label points1;

    @FXML
    private Label points2;

    @FXML
    private Label points3;

    @FXML
    private Label points0;

    @FXML
    private Label winner;

    @FXML
    private Label winnerPhrase;

    public EndGameGuiController(GuiView guiView, Client client) {
        view = guiView;
        this.client = client;
    }

    @FXML
    void homeScreen(ActionEvent event) {
        view.loginProcedure();
    }

    public void setWinner(String winner) {

    }

    public void setWinner(List<String> winners, List<Integer> scores, List<String> losers, List<Integer> loserScores) {
        player0.setText("");
        points0.setText("");
        player1.setText("");
        points1.setText("");
        player2.setText("");
        points2.setText("");
        player3.setText("");
        points3.setText("");

        if (winners.contains(client.getUsername())) {
            this.winner.setText("You");
        } else {
            this.winner.setText(winners.get(0));
        }
        if (winners.size() > 1) {
            for (int i = 1; i < winners.size(); i++) {
                this.winner.setText(this.winner.getText() + ", " + winners.get(i));
            }
        }

        List<String> players = new ArrayList<>(winners);
        players.addAll(losers);

        List<Integer> scoresList = new ArrayList<>(scores);
        scoresList.addAll(loserScores);

        for (int i = 0; i < players.size(); i++) {
            switch (i) {
                case 0 -> {
                    player0.setText(players.get(i));
                    points0.setText(scoresList.get(i).toString());
                }
                case 1 -> {
                    player1.setText(players.get(i));
                    points1.setText(scoresList.get(i).toString());
                }
                case 2 -> {
                    player2.setText(players.get(i));
                    points2.setText(scoresList.get(i).toString());
                }
                case 3 -> {
                    player3.setText(players.get(i));
                    points3.setText(scoresList.get(i).toString());
                }
            }
        }
    }
}
