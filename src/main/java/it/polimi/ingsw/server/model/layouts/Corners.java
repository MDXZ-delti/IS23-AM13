package it.polimi.ingsw.server.model.layouts;

import it.polimi.ingsw.server.model.Bookshelf;
import it.polimi.ingsw.utils.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The common goal layout with four tiles of the same type in the four corners of the bookshelf.
 */
public class Corners extends Layout {

    /**
     * Creates a new Corner layout with the given parameters.
     * The width and height of the layout are set to 0 because the number of rows and columns is taken directly from the examined bookshelf.
     *
     * @param minDifferent the minimum number of different colors in a layout
     * @param maxDifferent the maximum number of different colors in a layout
     */
    public Corners(int minDifferent, int maxDifferent) {
        // Set width and height to 0 because it takes the number of rows and columns directly from the examined bookshelf
        super(0, 0, minDifferent, maxDifferent);
        description = "Four tiles of the same type in the four " + "corners of the bookshelf. ";
    }

    @Override
    public String getName() {
        return "corners";
    }

    /**
     * Checks if the bookshelf has no free cells in the first and last column and if the number of distinct colors in the four corners is between the minimum and the maximum.
     *
     * @param b the bookshelf to be checked
     * @return true if the bookshelf has no free cells in the first and last column and if the number of distinct colors in the four corners is between the minimum and the maximum, false otherwise
     * @throws IllegalArgumentException if the bookshelf is null
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public boolean check(Bookshelf b) throws IllegalArgumentException {
        // Check if the bookshelf is null
        if (b == null) {
            throw new IllegalArgumentException("The bookshelf cannot be null");
        }

        // Local list of distinct colors
        List<Color> colorList = new ArrayList<>();

        // Local color variable
        Color color;

        // Check if the bookshelf has no free cells in the first and last column
        if (b.getFreeCellsInColumn(0) != 0 || b.getFreeCellsInColumn(Bookshelf.getColumns() - 1) != 0) {
            return false;
        } else { // Add the colors of the four corners to the list
            // Bottom left
            color = b.getItemAt(0, 0).get().color();
            colorList.add(color);

            // Top left
            color = b.getItemAt(Bookshelf.getRows() - 1, 0).get().color();
            colorList.add(color);

            // Top right
            color = b.getItemAt(Bookshelf.getRows() - 1, Bookshelf.getColumns() - 1).get().color();
            colorList.add(color);

            // Bottom right
            color = b.getItemAt(0, Bookshelf.getColumns() - 1).get().color();
            colorList.add(color);

            // Remove duplicates
            colorList = colorList.stream().distinct().collect(Collectors.toList());

            // Check if the number of distinct colors is between the minimum and the maximum
            return colorList.size() >= getMinDifferent() && colorList.size() <= getMaxDifferent();
        }
    }
}
