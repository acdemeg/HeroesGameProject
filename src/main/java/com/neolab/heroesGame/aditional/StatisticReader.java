package com.neolab.heroesGame.aditional;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.neolab.heroesGame.aditional.StatisticWriter.ARMY_STATISTIC_FILE_PATH;
import static com.neolab.heroesGame.aditional.StatisticWriter.PLAYER_STATISTIC_FILE_PATH;

public class StatisticReader {

    public static Optional<List<String[]>> readArmiesWinStatistic() {
        final File csvInputFile = new File(ARMY_STATISTIC_FILE_PATH);
        if (!csvInputFile.exists()) {
            return Optional.empty();
        }
        List<String[]> result = Collections.emptyList();
        try (final CSVReader reader = new CSVReader(new FileReader(csvInputFile))) {
            result = reader.readAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(result);
    }

    public static Optional<List<String[]>> readPlayersWinStatistic() {
        final File csvInputFile = new File(PLAYER_STATISTIC_FILE_PATH);
        if (!csvInputFile.exists()) {
            return Optional.empty();
        }
        List<String[]> result = Collections.emptyList();
        try (final CSVReader reader = new CSVReader(new BufferedReader(new FileReader(csvInputFile)))) {
            result = reader.readAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(result);
    }
}
