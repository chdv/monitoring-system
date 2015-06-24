package com.dch.app.monitor;

/**
 * Created by ִלטענטי on 23.06.2015.
 */
public class Task {

    private Status status = Status.NEW;

    public enum Status {
        NEW, SUCCESS, REJECTED, ERROR;
    }

    public void setStatus(Status s) {
        status = s;
    }

    public Status getStatus() {
        return status;
    }
}
