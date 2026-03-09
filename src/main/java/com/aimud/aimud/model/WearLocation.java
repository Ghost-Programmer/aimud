package com.aimud.aimud.model;

public enum WearLocation {
    HEAD("Head"),
    CHEST("Chest"),
    LEGS("Legs"),
    FEET("Feet"),
    ARMS("Arms"),
    HANDS("Hands"),
    RIGHT_FINGER("Right Finger"),
    LEFT_FINGER("Left Finger"),
    RIGHT_WRIST("Right Wrist"),
    LEFT_WRIST("Left Wrist"),
    NECK("Neck"),
    LEFT_EAR("Left Ear"),
    RIGHT_EAR("Right Ear"),
    FACE("Face"),
    WAIST("Waist"),
    PRIMARY("Primary"),
    OFFHAND("Offhand");

    private final String label;

    WearLocation(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
