package Utility;

import java.time.LocalDateTime;

public class DateRange {

    private LocalDateTime from = null;
    private LocalDateTime to = null;

    public DateRange(LocalDateTime from, LocalDateTime to) {
        if (from.isAfter(to)) {
            LocalDateTime temp = from;
            from = to;
            to = temp;
        }    
        this.from = from;
        this.to = to;
    }

    public LocalDateTime getFrom() {
        return this.from;
    }

    public LocalDateTime getTo() {
        return this.to;
    }

}