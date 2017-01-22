package net.igeneric.rcracing;

import android.support.annotation.NonNull;

class Players implements Comparable<Players> {

    private int index = 0;

    private int id;
    private int position;
    private String name;
    private int totalGates;
    private int totalKills;
    private int totalDeaths;
    private int totalLaps;
    private int nextGate;
    private int lives;
    private int lastLap;
    private boolean finish;
    private boolean dead;
    private long currentLap;

    Players(int id) {
        this.id = id;
        this.position = index;
        index++;
        this.name = "TRUCK " + id;
        this.totalGates = 0;
        this.totalKills = 0;
        this.totalDeaths = 0;
        this.totalLaps = -1;
        this.nextGate = 1;
        this.finish = false;
        this.dead = false;
        this.lives = MainActivity.raceLivesNumber;
        this.currentLap = 0;
        this.lastLap = 0;
    }

    @Override
    public int compareTo(@NonNull Players players) {
        if (MainActivity.raceType < 3) {
            int i = 0;
            if (players.totalGates < this.totalGates) i--;
            if (players.totalGates > this.totalGates) i++;
            players.position += i;
            return i;
        } else {
            int i = 0;
            if (players.totalKills < this.totalKills) i--;
            if (players.totalKills > this.totalKills) i++;
            players.position += i;
            return i;
        }
    }

    boolean checkId(int i) {
        return this.id == i;
    }

    String getName() {
        return name;
    }

    void addTotalGates(int gate) {
        if (this.nextGate == 1 && gate == 1) {
            this.totalLaps++;
            if (this.currentLap != 0) {
                this.lastLap = (int)((System.currentTimeMillis() - this.currentLap)/1000);
                MainActivity.ttsArray.add("Truck number " + this.id + " finish lap " + this.totalLaps + " in " + this.lastLap + "seconds");
                this.currentLap = System.currentTimeMillis();
            } else this.currentLap = System.currentTimeMillis();
            if (this.totalLaps == MainActivity.raceLapsNumber) this.finish = true;
            else this.nextGate++;
            this.totalGates++;
        } else if (this.nextGate == gate) {
            this.totalGates++;
            this.nextGate++;
            if (this.nextGate > MainActivity.raceGatesNumber) this.nextGate = 1;
        }
    }

    int getTotalKills() {
        return totalKills;
    }

    void addTotalKills() {
        this.totalKills++;
        if (this.totalKills == MainActivity.raceKillsNumber) this.finish = true;
    }

    int getTotalDeaths() {
        return totalDeaths;
    }

    void addTotalDeaths() {
        this.totalDeaths++;
        if (this.lives > 0) this.lives--;
        if (this.lives == 0) {
            this.dead = true;
        }
    }

    int getTotalLaps() {
        return totalLaps;
    }

    int getNextGate() {
        return nextGate;
    }

    boolean isFinish() {
        return finish;
    }

    int getLives() {
        return lives;
    }

    long getLastLap() {
        return lastLap;
    }

    public boolean isDead() {
        return dead;
    }
}
