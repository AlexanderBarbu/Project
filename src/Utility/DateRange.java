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

    public DateRange(String data) {
        String[] parts = data.split(";");
        this.from = LocalDateTime.parse(parts[0]);
        this.to = LocalDateTime.parse(parts[1]);
    }

    @Override
    public String toString() {
        return from.toString() + ";" + to.toString();
    }

    public LocalDateTime getFrom() {
        return this.from;
    }

    public LocalDateTime getTo() {
        return this.to;
    }

    public boolean intersects(DateRange dr) {
        return !(this.getTo().isBefore(dr.getFrom()) || dr.getTo().isBefore(this.getFrom()));
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        } else if (object == null) {
            return false;
        } else if (this.getClass() != object.getClass()) {
            return false;
        }
        DateRange other = (DateRange)object;
        return other.from.equals(this.from) && other.to.equals(this.to);
    }
}