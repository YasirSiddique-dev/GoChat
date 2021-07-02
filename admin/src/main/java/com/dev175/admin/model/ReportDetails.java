package com.dev175.admin.model;

import java.io.Serializable;

public class ReportDetails implements Serializable {
    private Report report;
    private User reportedTo;
    private User reportedBy;

    public ReportDetails() {
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public User getReportedTo() {
        return reportedTo;
    }

    public void setReportedTo(User reportedTo) {
        this.reportedTo = reportedTo;
    }

    public User getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(User reportedBy) {
        this.reportedBy = reportedBy;
    }
}
