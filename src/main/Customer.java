import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.time.Instant.now;

public class Customer implements Runnable {
    private final Bakery bakery;
    private static final Random rnd = new Random(now().toEpochMilli());
    private final List<BreadType> shoppingCart;
    private final int shopTime;
    private final int checkoutTime;
    private static int nextCustomer = 1;
    private final int customerNo;

    /**
     * Initialize a customer object and randomize its shopping cart
     */
    public Customer(Bakery bakery) {
        this.bakery = bakery;
        shoppingCart = new ArrayList<>();
        shopTime = rnd.nextInt(10);
        checkoutTime = rnd.nextInt(10);
        customerNo = nextCustomer++;
    }

    /**
     * Run tasks for the customer
     */
    public void run() {
        fillShoppingCart();      // customer fills his shopping cart with up to three loaves
        try { TimeUnit.SECONDS.sleep(shopTime); }   // simulate shopping time
        catch (InterruptedException ignore) {}
        //
        bakery.addSales(getItemsValue());   // checkout with an available cashier
        try { TimeUnit.SECONDS.sleep(checkoutTime); }   // simulate checkout time
        catch (InterruptedException ignore) {}
        System.out.printf("%s purchased $%5.2f, shoppingCart=%s\n",
            this, getItemsValue(), Arrays.toString(shoppingCart.toArray()));
    }

    /**
     * Return a string representation of the customer
     */
    public String toString() {
        return "Customer %3d, shopTime=%3d, checkoutTime=%3d"
            .formatted(customerNo, shopTime, checkoutTime);
    }

    /**
     * Add a bread item to the customer's shopping cart
     */
    private boolean addItem(BreadType bread) {
        // do not allow more than 3 items, chooseItems() does not call more than 3 times
        if (shoppingCart.size() >= 3) {
            return false;
        }
        bakery.takeBread(bread);        // take a loaf from available shelf
        shoppingCart.add(bread);        // add the loaf to the shopping cart
        //System.out.printf("%s added a %s loaf to shopping cart\n", this, bread.toString());

        return true;
    }

    /**
     * Fill the customer's shopping cart with 1 to 3 random breads
     */
    private void fillShoppingCart() {
        int itemCnt = 1 + rnd.nextInt(3);
        while (itemCnt > 0) {
            BreadType type = BreadType.values()[rnd.nextInt(BreadType.values().length)];
            addItem(type);
            itemCnt--;
        }
    }

    /**
     * Calculate the total value of the items in the customer's shopping cart
     */
    private float getItemsValue() {
        float value = 0;
        for (BreadType bread : shoppingCart) {
            value += bread.getPrice();
        }
        return value;
    }
}