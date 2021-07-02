package com.dev175.admin.model;

import java.io.Serializable;

public class Report implements Serializable {
    private String id;
    private String reportedBy;
    private String reportedTo;
    private String reason;

    public Report() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }

    public String getReportedTo() {
        return reportedTo;
    }

    public void setReportedTo(String reportedTo) {
        this.reportedTo = reportedTo;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
