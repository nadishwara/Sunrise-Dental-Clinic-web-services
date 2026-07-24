package com.sunrisedental.util;


public class IdGenerator {
    /** User role and auto increment are generate by base on SQL ID
     * @param role User Role ('ADMIN', 'DENTIST', 'RECEPTIONIST', 'PATIENT')
     * @param numericId Auto incremented database ID
     * @return Formatted String ID (e.g. DEN002, REP001, PTN005, ADM001)
     */
    public static String generateCustomId(String role, int numericId) {
        String prefix;
        switch (role.toUpperCase()) {
            case "DENTIST":
                prefix = "DEN";
                break;
            case "RECEPTIONIST":
                prefix = "REP";
                break;
            case "PATIENT":
                prefix = "PTN";
                break;
            case "ADMIN":
                prefix = "ADM";
                break;
            default:
                prefix = "USR";
                break;
        }
        return String.format("%s%03d", prefix, numericId);
    }
}
