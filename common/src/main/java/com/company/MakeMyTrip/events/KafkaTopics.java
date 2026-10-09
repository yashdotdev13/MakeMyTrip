package com.company.MakeMyTrip.events;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String USER_REGISTERED =
            "user.registered";

    public static final String USER_LOGGED_IN =
            "user.logged-in";

    public static final String USER_LOGGED_OUT =
            "user.logged-out";

    public static final String BOOKING_DEMAND = "booking.demand";

    public static final String PAYMENT_REQUESTED = "payment.requested";
    public static final String PAYMENT_COMPLETED = "payment.completed";
    public static final String PAYMENT_FAILED = "payment.failed";


    public static final String BOOKING_LIFECYCLE = "booking.lifecycle";
}