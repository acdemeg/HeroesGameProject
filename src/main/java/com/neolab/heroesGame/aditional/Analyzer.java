package com.neolab.heroesGame.aditional;

import java.util.*;

public class Analyzer {
    private final Double heightWinOrLoseRate = 60.0;
    private final Map<String, Map<String, List<String>>> statisticMap;
    private final Map<String, Map<String, List<Double>>> allInformation;
    private final Set<String> firstArmies;
    private final Set<String> secondArmies;

    private Analyzer(final Map<String, Map<String, List<String>>> statisticMap, final Set<String> firstArmies,
                     final Set<String> secondArmies) {
        this.statisticMap = statisticMap;
        this.firstArmies = firstArmies;
        this.secondArmies = secondArmies;
        this.allInformation = analiseAll();
    }

    public static Analyzer createAnalyserForArmies() {
        final Optional<List<String[]>> statistic = StatisticReader.readArmiesWinStatistic();
        return createAnalyzer(statistic.orElse(Collections.emptyList()));
    }

    public static Analyzer createAnalyserForPlayers() {
        final Optional<List<String[]>> statistic = StatisticReader.readPlayersWinStatistic();
        return createAnalyzer(statistic.orElse(Collections.emptyList()));
    }

    private static Analyzer createAnalyzer(final List<String[]> statistic) {
        final Map<String, Map<String, List<String>>> statisticMap = new HashMap<>();
        final Set<String> setFirstArmies = new HashSet<>();
        final Set<String> setSecondArmies = new HashSet<>();
        if (!statistic.isEmpty()) {
            statistic.forEach(val -> {
                Map<String, List<String>> temp = Optional.ofNullable(statisticMap.get(val[0])).orElse(new HashMap<>());
                List<String> listTemp = Optional.ofNullable(temp.get(val[1])).orElse(new ArrayList<>());
                listTemp.add(val[2]);
                temp.put(val[1], listTemp);
                statisticMap.put(val[0], temp);
                setFirstArmies.add(val[0]);
                setSecondArmies.add(val[1]);
            });
        }
        return new Analyzer(statisticMap, setFirstArmies, setSecondArmies);
    }

    public Map<String, Map<String, List<Double>>> getAllInformation() {
        return allInformation;
    }

    public Map<String, List<String>> getInfoAboutPairPlayers(final String firstName, final String secondName) {
        Map<String, List<String>> result = new HashMap<>();
        List<String> temp = Optional.ofNullable(statisticMap.get(firstName)).
                orElse(Collections.emptyMap()).get(secondName);
        if (temp != null) {
            result.put(firstName, temp);
        }
        temp = Optional.ofNullable(statisticMap.get(secondName)).
                orElse(Collections.emptyMap()).get(firstName);
        if (temp != null) {
            result.put(firstName, temp);
        }
        return result;
    }

    public Map<String, Map<String, List<Double>>> getAnomalisticResults() {
        Map<String, Map<String, List<Double>>> anomalisticResults = new HashMap<>();
        for (String firstMovedArmy : firstArmies) {
            final Map<String, List<Double>> infoAboutOne = Optional.ofNullable(allInformation.get(firstMovedArmy))
                    .orElse(Collections.emptyMap());
            if (heightWin(infoAboutOne) || heightLose(infoAboutOne)) {
                anomalisticResults.put(firstMovedArmy, infoAboutOne);
            }
        }
        return anomalisticResults;
    }

    public void showAnomalisticResults() {
        Map<String, Map<String, List<Double>>> anomalisticResults = getAnomalisticResults();
        for (String army : anomalisticResults.keySet()) {
            System.out.printf("%s%16s%10s%10s\n", army, "win", "draw", "lose");
            for (String secondArmy : anomalisticResults.get(army).keySet()) {
                List<Double> winRate = anomalisticResults.get(army).get(secondArmy);
                if (!winRate.isEmpty()) {
                    System.out.printf("%12s%9.0f%%%9.0f%%%9.0f%% %10d матчей\n", secondArmy, winRate.get(0),
                            winRate.get(1), winRate.get(2), winRate.get(3).longValue());
                }
            }
        }
    }

    private boolean heightLose(final Map<String, List<Double>> results) {
        for (String key : results.keySet()) {
            if (!results.get(key).isEmpty() && results.get(key).get(2) < heightWinOrLoseRate) {
                return false;
            }
        }
        return true;
    }

    private boolean heightWin(final Map<String, List<Double>> results) {
        for (String key : results.keySet()) {
            if (!results.get(key).isEmpty() && results.get(key).get(0) < heightWinOrLoseRate) {
                return false;
            }
        }
        return true;
    }

    private Map<String, Map<String, List<Double>>> analiseAll() {
        Map<String, Map<String, List<Double>>> allInformation = new HashMap<>();
        for (String firstMovedArmy : firstArmies) {
            final Map<String, List<String>> infoAboutOne = Optional.ofNullable(statisticMap.get(firstMovedArmy))
                    .orElse(Collections.emptyMap());
            Map<String, List<Double>> information = new HashMap<>();
            for (String secondMovedArmy : secondArmies) {
                final List<String> results = Optional.ofNullable(infoAboutOne.get(secondMovedArmy))
                        .orElse(Collections.emptyList());
                List<Double> winRate = getListWinRate(results);
                information.put(secondMovedArmy, winRate);
            }
            allInformation.put(firstMovedArmy, information);
        }
        return allInformation;
    }

    private List<Double> getListWinRate(final List<String> results) {
        List<Double> winRate = new ArrayList<>();
        int counterWin = 0;
        int counterDraw = 0;
        int counterLose = 0;
        for (String result : results) {
            if (result.equals("Win")) {
                counterWin++;
            } else if (result.equals("Draw")) {
                counterDraw++;
            } else {
                counterLose++;
            }
        }
        if (!results.isEmpty()) {
            winRate.add(counterWin * 100.0 / results.size());
            winRate.add(counterDraw * 100.0 / results.size());
            winRate.add(counterLose * 100.0 / results.size());
            winRate.add((double) results.size());
        }
        return winRate;
    }
}
