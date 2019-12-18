package com.h3.reservation.common;

import java.util.Objects;

public class ReservationDetails {
    private final MeetingRoom meetingRoom;
    private final String booker;
    private final String description;

    private ReservationDetails(final MeetingRoom meetingRoom, final String booker, final String description) {
        this.meetingRoom = meetingRoom;
        this.booker = booker;
        this.description = description;
    }

    public static ReservationDetails of(final MeetingRoom meetingRoom, final String booker, final String description) {
        return new ReservationDetails(meetingRoom, booker, description);
    }

    public boolean isSameBooker(String booker) {
        return this.booker.equals(booker);
    }

    public MeetingRoom getMeetingRoom() {
        return meetingRoom;
    }

    public String getBooker() {
        return booker;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReservationDetails that = (ReservationDetails) o;
        if (meetingRoom != that.meetingRoom) return false;
        if (!Objects.equals(booker, that.booker)) return false;
        return Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        int result = meetingRoom != null ? meetingRoom.hashCode() : 0;
        result = 31 * result + (booker != null ? booker.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ReservationDetails{" +
                "meetingRoom=" + meetingRoom +
                ", booker='" + booker + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
