package com.company.MakeMyTrip.common.events;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String USER_REGISTERED = "user.registered";
    public static final String USER_LOGGED_IN = "user.logged-in";
    public static final String USER_LOGGED_OUT = "user.logged-out";
}