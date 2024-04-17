package Utility;

public class PriceRange {
    
    private float from = 0;
    private float to = 0;

    public PriceRange(float from, float to) {
        // Prevent negative values
        from = Math.max(0, from);
        to = Math.max(0, to);
        if (from > to) {
            float temp = from;
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

    public float getFrom() {
        return this.from;
    }

    public void setFrom(float from) {
        this.from = from;
    }

    public float getTo() {
        return this.to;
    }

    public void setTo(float to) {
        this.to = to;
    }

    public static PriceRange Any() {
        return new PriceRange(0, 2147483647);
    }

}
