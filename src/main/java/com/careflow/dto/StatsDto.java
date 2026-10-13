package com.careflow.dto;

import java.util.List;

public class StatsDto {

    private long totalPatients;
    private long totalDoctors;
    private long totalReceptionists;
    private long totalTechnicians;
    private long todayAppointments;
    private long pendingLabTests;
    private long pendingBills;
    private List<AuditLogDto> recentActivity;

    public StatsDto() {
    }

    public StatsDto(long totalPatients, long totalDoctors, long totalReceptionists, long totalTechnicians, long todayAppointments, long pendingLabTests, long pendingBills, List<AuditLogDto> recentActivity) {
        this.totalPatients = totalPatients;
        this.totalDoctors = totalDoctors;
        this.totalReceptionists = totalReceptionists;
        this.totalTechnicians = totalTechnicians;
        this.todayAppointments = todayAppointments;
        this.pendingLabTests = pendingLabTests;
        this.pendingBills = pendingBills;
        this.recentActivity = recentActivity;
    }

    public long getTotalPatients() {
        return totalPatients;
    }

    public void setTotalPatients(long totalPatients) {
        this.totalPatients = totalPatients;
    }

    public long getTotalDoctors() {
        return totalDoctors;
    }

    public void setTotalDoctors(long totalDoctors) {
        this.totalDoctors = totalDoctors;
    }

    public long getTotalReceptionists() {
        return totalReceptionists;
    }

    public void setTotalReceptionists(long totalReceptionists) {
        this.totalReceptionists = totalReceptionists;
    }

    public long getTotalTechnicians() {
        return totalTechnicians;
    }

    public void setTotalTechnicians(long totalTechnicians) {
        this.totalTechnicians = totalTechnicians;
    }

    public long getTodayAppointments() {
        return todayAppointments;
    }

    public void setTodayAppointments(long todayAppointments) {
        this.todayAppointments = todayAppointments;
    }

    public long getPendingLabTests() {
        return pendingLabTests;
    }

    public void setPendingLabTests(long pendingLabTests) {
        this.pendingLabTests = pendingLabTests;
    }

    public long getPendingBills() {
        return pendingBills;
    }

    public void setPendingBills(long pendingBills) {
        this.pendingBills = pendingBills;
    }

    public List<AuditLogDto> getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(List<AuditLogDto> recentActivity) {
        this.recentActivity = recentActivity;
    }
}
