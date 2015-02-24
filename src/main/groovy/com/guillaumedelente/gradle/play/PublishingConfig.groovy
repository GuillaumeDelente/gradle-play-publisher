package com.guillaumedelente.gradle.play

/**
 * Created by guillaume on 2/25/15.
 */
class PublishingConfig {

    final String name

    String serviceAccountEmail

    File pk12File

    boolean uploadImages = false

    private String track = 'alpha'

    private Double userFraction = null;

    PublishingConfig(String name) {
        this.name = name
    }

    void setTrack(String track) {
        if (!(track in ['alpha', 'beta', 'production', 'rollout'])) {
            throw new IllegalArgumentException("Track has to be one of 'alpha', 'beta', 'production' or 'rollout'.")
        }
        this.track = track
    }

    void setUserFraction(Double userFraction) {
        if (!(userFraction in [0.05D, 0.1D, 0.2D, 0.5D])) {
            throw new IllegalArgumentException("UserFraction has to be one of 0.05, 0.1, 0.2 or 0.5.")
        }
        this.userFraction = userFraction
    }

    def getTrack() {
        return track
    }

    def getUserFraction() {
        return userFraction;
    }

}
