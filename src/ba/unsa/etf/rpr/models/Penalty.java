package ba.unsa.etf.rpr.models;

import ba.unsa.etf.rpr.DAO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Currency;
import java.util.Date;

public class Penalty {
    private Inspector sanctionedBy;
    private Business addressedTo;
    private InspectionFinding finding;
    private Date deadline;
    private Integer amount;
    private Integer missedDeadlinePenalty;

    public Inspector getSanctionedBy() {
        return sanctionedBy;
    }

    public void setSanctionedBy(Inspector sanctionedBy) {
        this.sanctionedBy = sanctionedBy;
    }

    public Business getAddressedTo() {
        return addressedTo;
    }

    public void setAddressedTo(Business addressedTo) {
        this.addressedTo = addressedTo;
    }

    public InspectionFinding getFinding() {
        return finding;
    }

    public void setFinding(InspectionFinding finding) {
        this.finding = finding;
    }

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
}