// represents menu item and quantity

public class OrderItem {

    // variables
    private final String itemID;
    private final int quantity;
    private final Float unitPrice;
    private final Float subtotal;

    // accessors
    public String GetItemID() {
        return itemID;
    }

    public int GetQuantity() {
        return quantity;
    }

    public Float GetUnitPrice() {
        return unitPrice;
    }

    public Float GetSubtotal() {
        return subtotal;
    }

    // methods
    public OrderItem(String itemID, int quantity, Float unitPrice, Float subtotal) {
        this.itemID = itemID;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public void calculateSubtotal() {

    }
}