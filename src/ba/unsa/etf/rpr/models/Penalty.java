package ba.unsa.etf.rpr.models;

import java.util.Date;

public class Penalty {
    private String report;
    private Date deadline;
    private Integer amount;
    private Integer missedDeadlinePenalty;
    private Integer ceaseOperation;

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getMissedDeadlinePenalty() {
        return missedDeadlinePenalty;
    }

    public void setMissedDeadlinePenalty(Integer missedDeadlinePenalty) {
        this.missedDeadlinePenalty = missedDeadlinePenalty;
    }

    public Integer getCeaseOperation() {
        return ceaseOperation;
    }

    public void setCeaseOperation(Integer ceaseOperation) {
        this.ceaseOperation = ceaseOperation;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }
}