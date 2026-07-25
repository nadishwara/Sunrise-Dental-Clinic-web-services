package com.sunrisedental.model;

public class AppointmentRequest {
    private int requestId;
    private int patientUserId;
    private String patientCustomId;
    private String preferredDate;
    private String preferredTimeSlot;
    private Integer preferredDentistId;
    private String notes;
    private String status;
    private String createdAt;
//    Default Constructor
    public AppointmentRequest() {}

    public AppointmentRequest(int requestId, int patientUserId, String patientCustomId,
                              String preferredDate, String preferredTimeSlot,
                              Integer preferredDentistId, String notes, String status) {
        this.requestId = requestId;
        this.patientUserId = patientUserId;
        this.patientCustomId = patientCustomId;
        this.preferredDate = preferredDate;
        this.preferredTimeSlot = preferredTimeSlot;
        this.preferredDentistId = preferredDentistId;
        this.notes = notes;
        this.status = status;
    }
    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getPatientUserId() {
        return patientUserId;
    }

    public void setPatientUserId(int patientUserId) {
        this.patientUserId = patientUserId;
    }

    public String getPatientCustomId() {
        return patientCustomId;
    }

    public void setPatientCustomId(String patientCustomId) {
        this.patientCustomId = patientCustomId;
    }

    public String getPreferredDate() {
        return preferredDate;
    }

    public void setPreferredDate(String preferredDate) {
        this.preferredDate = preferredDate;
    }

    public String getPreferredTimeSlot() {
        return preferredTimeSlot;
    }

    public void setPreferredTimeSlot(String preferredTimeSlot) {
        this.preferredTimeSlot = preferredTimeSlot;
    }

    public Integer getPreferredDentistId() {
        return preferredDentistId;
    }

    public void setPreferredDentistId(Integer preferredDentistId) {
        this.preferredDentistId = preferredDentistId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
