package restaurant;

public abstract class Staff {
    private boolean actedThisTurn;

    public abstract String getRole();

    public boolean hasActed() {
        return actedThisTurn;
    }

    protected void markActed() {
        actedThisTurn = true;
    }

    public void resetForNewTurn() {
        actedThisTurn = false;
    }
}
