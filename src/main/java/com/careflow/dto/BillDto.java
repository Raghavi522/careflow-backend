package com.careflow.dto;

import com.careflow.entity.BillStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BillDto {

    private Long id;
    private Long patientId;
    private String patientName;
    private String patientEmail;
    private String description;
    private BigDecimal amount;
    private LocalDateTime billDate;
    private BillStatus status;
    private LocalDateTime paidAt;
    private String paymentMethod;

    public BillDto() {
    }

    public BillDto(Long id, Long patientId, String patientName, String patientEmail, String description, BigDecimal amount, LocalDateTime billDate, BillStatus status, LocalDateTime paidAt, String paymentMethod) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientEmail = patientEmail;
        this.description = description;
        this.amount = amount;
        this.billDate = billDate;
        this.status = status;
        this.paidAt = paidAt;
        this.paymentMethod = paymentMethod;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDateTime billDate) {
        this.billDate = billDate;
    }

    public BillStatus getStatus() {
        return status;
    }

    public void setStatus(BillStatus status) {
        this.status = status;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
