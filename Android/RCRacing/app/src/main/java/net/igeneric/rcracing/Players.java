package net.igeneric.rcracing;

import android.support.annotation.NonNull;

public class Players implements Comparable<Players> {

    private int id;
    private String name;
    private int totalGates;
    private int totalKills;
    private int totalDeaths;
    private int totalLaps;
    private int nextGate;
    private boolean finish;

    public Players(int id) {
        this.id = id;
        this.name = "TRUCK " + id;
        this.totalGates = 0;
        this.totalKills = 0;
        this.totalDeaths = 0;
        this.totalLaps = -1;
        this.nextGate = 1;
        this.finish = false;
    }

    @Override
    public int compareTo(@NonNull Players players) {
        if (MainActivity.raceType < 3) {
            if (players.totalGates < this.totalGates) return -1;
            if (players.totalGates > this.totalGates) return 1;
        } else {
            if (players.totalKills < this.totalKills) return -1;
            if (players.totalKills > this.totalKills) return 1;
        }
        return 0;
    }

    public boolean checkId(int i) {
        if (this.id == i) return true;
        else return false;
    }

    public String getName() {
        return name;
    }

    public void addTotalGates(int gate) {
        if (this.nextGate == 1 && gate == 1) {
            this.totalLaps++;
            if (this.totalLaps == MainActivity.raceLapsNumber) this.finish = true;
            else this.nextGate++;
        } else if (this.nextGate == gate) {
            this.totalGates++;
            this.nextGate++;
            if (this.nextGate > MainActivity.raceGatesNumber) this.nextGate = 1;
        }
    }

    public int getTotalKills() {
        return totalKills;
    }

    public void addTotalKills() {
        this.totalKills++;
        if (this.totalKills == MainActivity.raceKillsNumber) this.finish = true;
    }

    public int getTotalDeaths() {
        return totalDeaths;
    }

    public void addTotalDeaths() {
        this.totalDeaths++;
    }

    public int getTotalLaps() {
        return totalLaps;
    }

    public int getNextGate() {
        return nextGate;
    }

    public boolean isFinish() {
        return finish;
    }
}
