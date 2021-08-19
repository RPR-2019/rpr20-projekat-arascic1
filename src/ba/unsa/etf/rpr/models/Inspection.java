package ba.unsa.etf.rpr.models;

import java.util.Date;

public class Inspection {
    private String sanctionedBy;
    private Business addressedTo;
    private Date deadline;
    private Penalty penalty;
    private Date issuedAt;

    public String getSanctionedBy() {
        return sanctionedBy;
    }

    public void setSanctionedBy(String sanctionedBy) {
        this.sanctionedBy = sanctionedBy;
    }

    public Business getAddressedTo() {
        return addressedTo;
    }

    public void setAddressedTo(Business addressedTo) {
        this.addressedTo = addressedTo;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public Penalty getPenalty() {
        return penalty;
    }

    public void setPenalty(Penalty penalty) {
        this.penalty = penalty;
    }

    public Date getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Date issuedAt) {
        this.issuedAt = issuedAt;
    }
}
