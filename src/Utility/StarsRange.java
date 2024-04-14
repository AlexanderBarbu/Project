package Utility;

public class StarsRange {

    private int from;
    private int to;

    public StarsRange(int from, int to) {
        if (from > StarsRange.this.to) {
            int temp = from;
            from = this.to;
            to = temp;
        }
        this.from = from;
        this.to = to;
    }

    public StarsRange(int stars) {
        this(stars, 5);
        if (stars > to) {
            to = stars;
        } else {
            from = stars;
        }
    }

    public StarsRange() {
        this(1, 5);
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }
}
