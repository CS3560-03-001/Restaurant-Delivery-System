public class MenuItem {
    // Vars
    private final String itemID;
    public int quantity;
    private final float unitPrice;

    // Accessors
    public String GetItemID() {
        return itemID;
    }

    public float GetUnitPrice() {
        return unitPrice;
    }

    public float GetSubtotal() {
        // We can do tax ccalculations here in the future
        return unitPrice * quantity;
    }

    // Methods
    public MenuItem(String itemID, float unitPrice) {
        this.itemID = itemID;
        this.unitPrice = unitPrice;
        this.quantity = 1;
    }
}