import java.util.Map;
import java.util.concurrent.*;

public class Bakery implements Runnable {
    private static final int TOTAL_CUSTOMERS = 200;
    private static final int ALLOWED_CUSTOMERS = 25;
    private static final int FULL_BREAD = 20;
    private Map<BreadType, Integer> availableBread;
    private final ExecutorService executor;
    private float sales = 0;
    private final Semaphore[] breadShelves;
    private final Semaphore cashiers;
    private final int[] loavesSold;

    // TODO
    public Bakery() {
        System.out.println("Fly's Bakery is open.");
        breadShelves = new Semaphore[BreadType.values().length];
        cashiers = new Semaphore(4, true);
        executor = Executors.newFixedThreadPool(ALLOWED_CUSTOMERS);
        loavesSold = new int[BreadType.values().length];
    }

    /**
     * Remove a loaf from the available breads and restock if necessary
     */
    public void takeBread(BreadType bread) {
        try {
            breadShelves[bread.ordinal()].acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        System.out.println(bread + " loaf taken");
        int breadLeft = availableBread.get(bread);
        if (breadLeft > 0) {
            availableBread.put(bread, breadLeft - 1);
        } else {
            System.out.println("No " + bread.toString() + " bread left! Restocking...");
            // restock by preventing access to the bread stand for some time
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            availableBread.put(bread, FULL_BREAD - 1);
        }
        loavesSold[bread.ordinal()]++;
        breadShelves[bread.ordinal()].release();
    }

    /**
     * Add to the total sales
     */
    public void addSales(float value) {
        try {
            cashiers.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sales += value;
        cashiers.release();
    }

    /**
     * Run all customers in a fixed thread pool
     */
    public void run() {
        availableBread = new ConcurrentHashMap<>();
        availableBread.put(BreadType.RYE, FULL_BREAD);
        availableBread.put(BreadType.SOURDOUGH, FULL_BREAD);
        availableBread.put(BreadType.WONDER, FULL_BREAD);

        // TODO
        breadShelves[BreadType.RYE.ordinal()] = new Semaphore(1, true);
        breadShelves[BreadType.SOURDOUGH.ordinal()] = new Semaphore(1, true);
        breadShelves[BreadType.WONDER.ordinal()] = new Semaphore(1, true);

        for ( int i = 0; i < TOTAL_CUSTOMERS; i++ ) {
            Customer c = new Customer(this);
            executor.submit(c);
        }

        executor.shutdown();
        try { executor.awaitTermination(5, TimeUnit.MINUTES); }
        catch (InterruptedException ignore) {}

        System.out.printf("Fly's Bakery is closed after servicing %d customers.\n", TOTAL_CUSTOMERS );
        System.out.printf("Total sales: $%,.2f\n", sales);
        for ( int i = 0; i < BreadType.values().length; i++ ) {
            System.out.printf("%d %s loaves sold.\n", loavesSold[i], BreadType.values()[i]);
        }
    }
}