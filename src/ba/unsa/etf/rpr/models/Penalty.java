package ba.unsa.etf.rpr.models;

import java.util.Currency;
import java.util.Date;

public class Penalty {
    private Inspector sanctionedBy;
    private Business addressedTo;
    private InspectionFinding finding;
    private Date deadline;
    private Integer amount;
    private Integer missedDeadlinePenalty;
}