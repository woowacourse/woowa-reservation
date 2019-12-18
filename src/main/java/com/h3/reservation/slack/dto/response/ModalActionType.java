package com.h3.reservation.slack.dto.response;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ModalActionType {
    UPDATE("update"),
    CLEAR("clear"),
    ERRORS("errors");

    @JsonValue
    private String type;

    ModalActionType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
