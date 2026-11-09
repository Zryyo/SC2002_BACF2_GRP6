package restaurant;

public class Table {
    private final int number;
    private Customer occupant;

    public Table(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public Customer getOccupant() {
        return occupant;
    }

    public boolean isFree() {
        return occupant == null;
    }

    public void assign(Customer c) {
        if (!isFree()) {
            throw new IllegalStateException("Table " + number + " is already occupied.");
        }
        occupant = c;
    }

    public void release() {
        occupant = null;
    }
}
