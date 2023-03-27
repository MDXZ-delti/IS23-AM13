package it.polimi.ingsw;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CalculateGroupsTest {

    /**
     * 1 group
     */
    @Test
    void calculateBluGropus() {
        Bookshelf b = new Bookshelf();
        List<Item> items = new ArrayList<>();
        items.add(new Item(Color.BLUE, 1));
        items.add(new Item(Color.BLUE, 1));
        b.insert(0, items);
        List<Item> items1 = new ArrayList<>();
        items1.add(new Item(Color.YELLOW, 1));
        items1.add(new Item(Color.BLUE, 1));
        items1.add(new Item(Color.BLUE, 1));
        b.insert(1, items1);
        b.print();
        ClasseTemporanea T = new ClasseTemporanea();
        int score = 0;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                if (b.getItemAt(i, j).isPresent()) {
                    score += T.calculatePoints(T.adjacentGroups(b, b.getItemAt(i, j).get().getColor(), i, j));
                }
            }
        }
        assertEquals(score, 3);


    }

    /**
     * 2 groups diiferent color
     */
    @Test
    void calculatebluGroups2() {
        Bookshelf b = new Bookshelf();

        List<Item> items = new ArrayList<>();
        items.add(new Item(Color.BLUE, 1));
        items.add(new Item(Color.BLUE, 1));
        b.insert(0, items);
        List<Item> items1 = new ArrayList<>();
        items1.add(new Item(Color.YELLOW, 1));
        items1.add(new Item(Color.BLUE, 1));
        items1.add(new Item(Color.BLUE, 1));
        b.insert(1, items1);

        List<Item> items2 = new ArrayList<>();
        items2.add(new Item(Color.YELLOW, 1));
        items2.add(new Item(Color.BLUE, 1));
        b.insert(2, items2);

        List<Item> items3 = new ArrayList<>();
        items3.add(new Item(Color.YELLOW, 1));
        items3.add(new Item(Color.BLUE, 1));
        items3.add(new Item(Color.BLUE, 1));
        b.insert(3, items3);


        List<Item> items4 = new ArrayList<>();
        items4.add(new Item(Color.YELLOW, 1));
        items4.add(new Item(Color.YELLOW, 1));
        b.insert(4, items4);

        int score = 0;
        ClasseTemporanea T = new ClasseTemporanea();
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                if (b.getItemAt(i, j).isPresent()) {
                    score += T.calculatePoints(T.adjacentGroups(b, b.getItemAt(i, j).get().getColor(), i, j));
                }
            }
        }
        assertEquals(score, 13);

    }

    /**
     * 2 groups different color with another color in the middle
     */
    @Test
    void calculatebluGroups3() {
        Bookshelf b = new Bookshelf();

        List<Item> items = new ArrayList<>();
        items.add(new Item(Color.BLUE, 1));
        items.add(new Item(Color.BLUE, 1));
        b.insert(0, items);
        List<Item> items1 = new ArrayList<>();
        items1.add(new Item(Color.YELLOW, 1));
        items1.add(new Item(Color.BLUE, 1));
        items1.add(new Item(Color.BLUE, 1));
        b.insert(1, items1);

        List<Item> items2 = new ArrayList<>();
        items2.add(new Item(Color.GREEN, 1));
        items2.add(new Item(Color.BLUE, 1));
        b.insert(2, items2);

        List<Item> items3 = new ArrayList<>();
        items3.add(new Item(Color.YELLOW, 1));
        items3.add(new Item(Color.BLUE, 1));
        items3.add(new Item(Color.BLUE, 1));
        b.insert(3, items3);


        List<Item> items4 = new ArrayList<>();
        items4.add(new Item(Color.YELLOW, 1));
        items4.add(new Item(Color.YELLOW, 1));
        b.insert(4, items4);


        ClasseTemporanea T = new ClasseTemporanea();
        int score = 0;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                if (b.getItemAt(i, j).isPresent()) {
                    score += T.calculatePoints(T.adjacentGroups(b, b.getItemAt(i, j).get().getColor(), i, j));
                }
            }
        }
        assertEquals(score, 10);
    }


    @Test
    void calculatebluGroups4() {
        Bookshelf b = new Bookshelf();

        List<Item> items = new ArrayList<>();
        items.add(new Item(Color.BLUE, 1));
        items.add(new Item(Color.BLUE, 1));
        items.add(new Item(Color.WHITE, 1));
        items.add(new Item(Color.BLUE, 1));
        items.add(new Item(Color.BLUE, 1));
        items.add(new Item(Color.BLUE, 1));
        b.insert(0, items);


        List<Item> items1 = new ArrayList<>();
        items1.add(new Item(Color.YELLOW, 1));
        items1.add(new Item(Color.BLUE, 1));
        items1.add(new Item(Color.BLUE, 1));
        items1.add(new Item(Color.WHITE, 1));
        items1.add(new Item(Color.BLUE, 1));
        items1.add(new Item(Color.BLUE, 1));
        b.insert(1, items1);

        List<Item> items2 = new ArrayList<>();
        items2.add(new Item(Color.GREEN, 1));
        items2.add(new Item(Color.BLUE, 1));
        items2.add(new Item(Color.GREEN, 1));
        items2.add(new Item(Color.PINK, 1));
        items2.add(new Item(Color.PINK, 1));
        items2.add(new Item(Color.PINK, 1));
        b.insert(2, items2);

        List<Item> items3 = new ArrayList<>();
        items3.add(new Item(Color.YELLOW, 1));
        items3.add(new Item(Color.BLUE, 1));
        items3.add(new Item(Color.BLUE, 1));
        items3.add(new Item(Color.PINK, 1));
        items3.add(new Item(Color.BLUE, 1));
        items3.add(new Item(Color.PINK, 1));
        b.insert(3, items3);


        List<Item> items4 = new ArrayList<>();
        items4.add(new Item(Color.YELLOW, 1));
        items4.add(new Item(Color.YELLOW, 1));
        items3.add(new Item(Color.WHITE, 1));
        items4.add(new Item(Color.PINK, 1));
        items4.add(new Item(Color.PINK, 1));
        items4.add(new Item(Color.PINK, 1));
        b.insert(4, items4);

        b.print();
        ClasseTemporanea T = new ClasseTemporanea();
        int score = 0;
        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 6; i++) {
                if (b.getItemAt(i, j).isPresent()) {
                    score += T.calculatePoints(T.adjacentGroups(b, b.getItemAt(i, j).get().getColor(), i, j));

                }
            }
        }
        assertEquals(score, 23);

    }
}
