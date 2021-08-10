package ba.unsa.etf.rpr.models;

import java.util.Date;

public class InspectionFinding {
    private Date issuedAt;
    private String content;

    public InspectionFinding(Date issuedAt, String content) {
        this.issuedAt = issuedAt;
        this.content = content;
    }

    public Date getIssuedAt() {
        return issuedAt;
    }

    public String getContent() {
        return content;
    }
}
