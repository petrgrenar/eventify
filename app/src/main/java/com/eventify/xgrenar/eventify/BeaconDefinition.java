package com.eventify.xgrenar.eventify;

public abstract class BeaconDefinition {

    private int majorNumber;
    private int minorNumber;
    private boolean codeExecuted;

    public BeaconDefinition(int majorNumber, int minorNumber) {
        this.majorNumber = majorNumber;
        this.minorNumber = minorNumber;
    }

    public int getMajorNumber() {
        return majorNumber;
    }

    public void setMajorNumber(int majorNumber) {
        this.majorNumber = majorNumber;
    }

    public int getMinorNumber() {
        return minorNumber;
    }

    public void setMinorNumber(int minorNumber) {
        this.minorNumber = minorNumber;
    }

    public abstract void execute();

    public boolean isCodeExecuted() {
        return codeExecuted;
    }

    public void setCodeExecuted(boolean codeExecuted) {
        this.codeExecuted = codeExecuted;
    }
}
