package com.sunrisedental.model;

public class Staff {
    private int staffId;
    private int userId;
    private String fullName;
    private String contactNo;
    private String specialization;
    private String customStaffId;

    public Staff() {}

    public Staff(int userId, String fullName, String contactNo, String specialization) {
        this.userId =userId;
        this.fullName=fullName;
        this.contactNo=contactNo;
        this.specialization=specialization;
    }

    public int getStaffId() { return staffId; }
    public void setStaffId(int staffId) {this.staffId = staffId;}

    public int getUserId() { return userId;}
    public void setUserId(int userId) {this.userId = userId;}

    public String getFullName() {return fullName;}
    public void setFullName(String fullName) {this.fullName=fullName;}

    public String getContactNo() {return contactNo;}
    public void setContactNo(String contactNo) {this.contactNo=contactNo;}

    public String getSpecialization() {return specialization;}
    public void setSpecialization(String specialization) {this.specialization=specialization;}

    public String getCustomStaffId() {return customStaffId;}
    public void setCustomStaffId(String customStaffId) {this.customStaffId = customStaffId;}
}
