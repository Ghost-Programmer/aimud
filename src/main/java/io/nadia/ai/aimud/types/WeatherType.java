package io.nadia.ai.aimud.types;

/**
 * Enumeration representing the atmospheric weather states.
 */
public enum WeatherType {
    SUNNY("The sky clears up, and the sun shines brightly.", "The sky remains clear and sunny."),
    CLOUDY("Clouds gather overhead, blocking out the sun.", "The sky is overcast and cloudy."),
    RAIN("It starts to rain.", "A steady rain continues to fall."),
    STORMS("The rain intensifies into a torrential storm!", "A fierce storm rages around you."),
    THUNDERSTORMS("Lightning flashes across the sky, and thunder rumbles!", "Thunder cracks overhead in a violent thunderstorm."),
    SNOW("Snowflakes begin to drift down from the sky.", "A quiet snowfall blankets the area.");

    private final String transitionMessage;
    private final String activeMessage;

    WeatherType(String transitionMessage, String activeMessage) {
        this.transitionMessage = transitionMessage;
        this.activeMessage = activeMessage;
    }

    public String getTransitionMessage() {
        return transitionMessage;
    }

    public String getActiveMessage() {
        return activeMessage;
    }
}
