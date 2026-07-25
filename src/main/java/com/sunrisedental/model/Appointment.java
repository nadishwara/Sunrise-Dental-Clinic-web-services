package com.sunrisedental.model;

public class Appointment {
    private int appointmentId;
    private String customAppointmentId;
    private int requestId;
    private int patientId;
    private int dentistId;
    private int receptionistId;
    private String appointmentDate;
    private String appointmentTime;
    private String status;
    private String createdAt;

    public Appointment() {}

    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }

    public String getCustomAppointmentId() { return customAppointmentId; }
    public void setCustomAppointmentId(String customAppointmentId) { this.customAppointmentId = customAppointmentId; }

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getDentistId() { return dentistId; }
    public void setDentistId(int dentistId) { this.dentistId = dentistId; }

    public int getReceptionistId() { return receptionistId; }
    public void setReceptionistId(int receptionistId) { this.receptionistId = receptionistId; }

    public String getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getAppointmentTime() { return appointmentTime;}
    public void setAppointmentTime(String appointmentTime) {this.appointmentTime = appointmentTime;}

    }

