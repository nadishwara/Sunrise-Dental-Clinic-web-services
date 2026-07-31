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

    private String patientName;
    private String patientContact;
    private String dentistName;
    private String receptionistName;

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

    public String getStatus() {return status;}
    public void setStatus(String status) {this.status=status;}

    public String getCreatedAt() {return createdAt;}
    public void setCreatedAt(String createdAt) {this.createdAt = createdAt;}

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientContact() { return patientContact; }
    public void setPatientContact(String patientContact) { this.patientContact = patientContact; }

    public String getDentistName() { return dentistName; }
    public void setDentistName(String dentistName) { this.dentistName = dentistName; }

    public String getReceptionistName() { return receptionistName; }
    public void setReceptionistName(String receptionistName) { this.receptionistName = receptionistName; }

    }

