package Utility;

public class PriceRange {
    
    private int from = 0;
    private int to = 0;

    public PriceRange(int from, int to) {
        // Prevent negative values
        from = Math.max(0, from);
        to = Math.max(0, to);
        if (from > to) {
            int temp = from;
            from = to;
            to = temp;
        }
        this.from = from;
        this.to = to;
    }
    public PriceRange(int price) {
        this(price, 1000);
        if (price > to) {
            to = price;
        } else {
            from = price;
        }
    }

    public int getFrom() {
        return this.from;
    }

    public int getTo() {
        return this.to;
    }

}
