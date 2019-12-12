package com.h3.reservation.slack.dto.request.viewsubmission;

import com.h3.reservation.slack.dto.request.viewsubmission.values.ReserveValues;

public class ReserveView {
    class State {
        private ReserveValues values;

        public State() {
        }

        public State(ReserveValues values) {
            this.values = values;
        }

        public ReserveValues getValues() {
            return values;
        }
    }

    private String callbackId;
    private State state;

    public ReserveView() {
    }

    public ReserveView(String callbackId, State state) {
        this.callbackId = callbackId;
        this.state = state;
    }

    public String getCallbackId() {
        return callbackId;
    }

    public State getState() {
        return state;
    }

    public ReserveValues getValues() {
        return state.getValues();
    }
}
