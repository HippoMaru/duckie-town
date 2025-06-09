package com.hippomaru.douckieTown.model.map;

import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CellGrid {
    private final int width;
    private final int height;
    private final Cell[][] grid;


    public CellGrid(String filePath){
        this(parseFileToEncodedGrid(filePath));
    }

    public CellGrid(int[][] encodedGrid){
        width = encodedGrid.length;
        height = encodedGrid[0].length;
        grid = new Cell[width][height];
        for (var x=0; x < width; x++){
            for (var y=0; y < height; y++){
                CellType cellType = CellType.getTypeByCode(encodedGrid[x][y]);
                Coords coords = new Coords(x, y);
                grid[x][y] = new Cell(cellType, coords);
            }
        }
    }

    private static int[][] parseFileToEncodedGrid(String filePath) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }

        if (lines.isEmpty()) {
            return new int[0][0];
        }

        int height = lines.size();
        int width = lines.get(0).length();
        int[][] grid = new int[height][width];

        for (int i = 0; i < height; i++) {
            String line = lines.get(i);
            if (line.length() != width) {
                throw new IllegalArgumentException("Inconsistent row length at line " + (i + 1));
            }
            for (int j = 0; j < width; j++) {
                char c = line.charAt(j);
                if (c < '0' || c > '4') {
                    throw new IllegalArgumentException("Invalid character '" + c + "' at (" + (i + 1) + "," + (j + 1) + ")");
                }
                grid[i][j] = c - '0';
            }
        }
        return grid;
    }
}
