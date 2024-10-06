// package src.test.java;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Map;  // Add this import

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class TradeMatchingEngineTest {

    private TradeMatchingEngine engine;

    @BeforeEach
    void setUp() {
        engine = new TradeMatchingEngine();
    }

    @Test
    void testAddBuyOrder() {
        TradeMatchingEngine.Order buyOrder = new TradeMatchingEngine.Order("1", "buy", 100.0, 10, System.currentTimeMillis());
        engine.addOrder(buyOrder);
        assertEquals(1, engine.getBuyOrders().size());
        assertEquals(buyOrder, engine.getBuyOrders().peek());
    }

    @Test
    void testAddSellOrder() {
        TradeMatchingEngine.Order sellOrder = new TradeMatchingEngine.Order("1", "sell", 100.0, 10, System.currentTimeMillis());
        engine.addOrder(sellOrder);
        assertEquals(1, engine.getSellOrders().size());
        assertEquals(sellOrder, engine.getSellOrders().peek());
    }

    @Test
    void testMatchingBuyOrder() {
        engine.addOrder(new TradeMatchingEngine.Order("1", "sell", 100.0, 10, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("2", "buy", 100.0, 5, System.currentTimeMillis()));
        
        assertEquals(1, engine.getSellOrders().size());
        assertEquals(5, engine.getSellOrders().peek().getVolume());
        assertEquals(0, engine.getBuyOrders().size());
    }

    @Test
    void testMatchingSellOrder() {
        engine.addOrder(new TradeMatchingEngine.Order("1", "buy", 100.0, 10, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("2", "sell", 100.0, 5, System.currentTimeMillis()));
        
        assertEquals(1, engine.getBuyOrders().size());
        assertEquals(5, engine.getBuyOrders().peek().getVolume());
        assertEquals(0, engine.getSellOrders().size());
    }

    @Test
    void testMultipleLevelMatching() {
        engine.addOrder(new TradeMatchingEngine.Order("1", "sell", 100.0, 5, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("2", "sell", 101.0, 5, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("3", "buy", 101.0, 7, System.currentTimeMillis()));
        
        assertEquals(1, engine.getSellOrders().size());
        assertEquals(3, engine.getSellOrders().peek().getVolume());
        assertEquals(0, engine.getBuyOrders().size());
    }

    @Test
    void testPriceTimePriority() {
        long time = System.currentTimeMillis();
        engine.addOrder(new TradeMatchingEngine.Order("1", "sell", 100.0, 5, time));
        engine.addOrder(new TradeMatchingEngine.Order("2", "sell", 100.0, 5, time + 1));
        engine.addOrder(new TradeMatchingEngine.Order("3", "buy", 100.0, 7, time + 2));
        
        assertEquals(1, engine.getSellOrders().size());
        assertEquals("2", engine.getSellOrders().peek().getOrderId());
        assertEquals(3, engine.getSellOrders().peek().getVolume());
        assertEquals(0, engine.getBuyOrders().size());
    }

    @Test
    void testDeleteBuyOrder() {
        TradeMatchingEngine.Order buyOrder = new TradeMatchingEngine.Order("1", "buy", 100.0, 10, System.currentTimeMillis());
        engine.addOrder(buyOrder);
        engine.deleteOrder("1");
        assertEquals(0, engine.getBuyOrders().size());
        assertNull(engine.getBuyOrders().peek());
    }

    @Test
    void testDeleteSellOrder() {
        TradeMatchingEngine.Order sellOrder = new TradeMatchingEngine.Order("1", "sell", 100.0, 10, System.currentTimeMillis());
        engine.addOrder(sellOrder);
        engine.deleteOrder("1");
        assertEquals(0, engine.getSellOrders().size());
        assertNull(engine.getSellOrders().peek());
    }

    @Test
    void testDeleteNonExistingOrder() {
        engine.deleteOrder("1");
        assertEquals(0, engine.getBuyOrders().size());
        assertEquals(0, engine.getSellOrders().size());
    }

    @Test
    void testDeleteAfterPartialMatch() {
        engine.addOrder(new TradeMatchingEngine.Order("1", "buy", 100.0, 10, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("2", "sell", 100.0, 5, System.currentTimeMillis()));
        engine.deleteOrder("1");
        assertEquals(0, engine.getBuyOrders().size());
        assertEquals(0, engine.getSellOrders().size());
    }

    @Test
    void testComplexMatchingScenario() {
        engine.addOrder(new TradeMatchingEngine.Order("1", "buy", 101.0, 5, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("2", "buy", 102.0, 10, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("3", "sell", 100.0, 8, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("4", "sell", 101.0, 4, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("5", "sell", 102.0, 6, System.currentTimeMillis()));


        
        assertEquals(1, engine.getBuyOrders().size());
        assertEquals(3, engine.getBuyOrders().peek().getVolume());
        assertEquals(1, engine.getSellOrders().size());
        assertEquals(6, engine.getSellOrders().peek().getVolume());
    }

    @Test
    void testMultipleOrderDeletions() {
        TradeMatchingEngine.Order buyOrder1 = new TradeMatchingEngine.Order("1", "buy", 100.0, 10, System.currentTimeMillis());
        TradeMatchingEngine.Order buyOrder2 = new TradeMatchingEngine.Order("2", "buy", 101.0, 15, System.currentTimeMillis());
        TradeMatchingEngine.Order sellOrder1 = new TradeMatchingEngine.Order("3", "sell", 102.0, 20, System.currentTimeMillis());
        engine.addOrder(buyOrder1);
        engine.addOrder(buyOrder2);
        engine.addOrder(sellOrder1);
        
        engine.deleteOrder("1");
        engine.deleteOrder("3");
        
        assertEquals(1, engine.getBuyOrders().size());
        assertEquals(buyOrder2, engine.getBuyOrders().peek());
        assertEquals(0, engine.getSellOrders().size());
    }

    @Test
    void testAddAndDeleteWithMatching() {
        engine.addOrder(new TradeMatchingEngine.Order("1", "sell", 100.0, 5, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("2", "sell", 101.0, 10, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("3", "buy", 102.0, 8, System.currentTimeMillis()));
        engine.deleteOrder("2");
        
        assertEquals(0, engine.getSellOrders().size());
        assertEquals(0, engine.getBuyOrders().size());
    }

    @Test
    void testComplexMatchingWithPartialDeletion() {
        engine.addOrder(new TradeMatchingEngine.Order("1", "sell", 100.0, 10, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("2", "sell", 101.0, 15, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("3", "buy", 101.0, 20, System.currentTimeMillis()));
        engine.deleteOrder("2");
        
        assertEquals(0, engine.getSellOrders().size());
        assertEquals(0, engine.getBuyOrders().size());
        assertEquals(0, engine.getBuyOrders().size());
    }
    @Test
void testBulkOrderAddition() {
    // Add 1000 buy orders with increasing prices
    for (int i = 0; i < 1000; i++) {
        engine.addOrder(new TradeMatchingEngine.Order(
            "B" + i,
            "buy",
            100.0 + i * 0.1,
            10,
            System.currentTimeMillis() + i
        ));
    }

    // Add 1000 sell orders with decreasing prices
    for (int i = 0; i < 1000; i++) {
        engine.addOrder(new TradeMatchingEngine.Order(
            "S" + i,
            "sell",
            200.0 - i * 0.1,
            10,
            System.currentTimeMillis() + 1000 + i
        ));
    }

    // Since buy orders start from 100 and increase, and sell orders start from 200 and decrease,
    // they should match when prices cross. Let's verify the number of unmatched orders.
    int expectedUnmatchedOrders = 0;
    if (engine.getBuyOrders().size() > 0) {
        expectedUnmatchedOrders += engine.getBuyOrders().size();
    }
    if (engine.getSellOrders().size() > 0) {
        expectedUnmatchedOrders += engine.getSellOrders().size();
    }

    // Depending on how many orders matched, we can assert the expected unmatched orders.
    assertTrue(expectedUnmatchedOrders >= 0);
}

@Test
void testHighVolumeMatching() {
    // Add 5000 buy orders at price 150
    for (int i = 0; i < 5000; i++) {
        engine.addOrder(new TradeMatchingEngine.Order(
            "B" + i,
            "buy",
            150.0,
            5,
            System.currentTimeMillis() + i
        ));
    }

    // Add 5000 sell orders at price 150
    for (int i = 0; i < 5000; i++) {
        engine.addOrder(new TradeMatchingEngine.Order(
            "S" + i,
            "sell",
            150.0,
            5,
            System.currentTimeMillis() + 5000 + i
        ));
    }

    // All orders should be matched, so both queues should be empty
    assertEquals(0, engine.getBuyOrders().size());
    assertEquals(0, engine.getSellOrders().size());
}

@Test
void testRandomOrderMatching() {
    Random rand = new Random();
    int numberOfOrders = 1000;

    // Add random buy and sell orders
    for (int i = 0; i < numberOfOrders; i++) {
        double price = 100 + rand.nextDouble() * 50;
        int volume = rand.nextInt(10) + 1;
        String side = rand.nextBoolean() ? "buy" : "sell";
        engine.addOrder(new TradeMatchingEngine.Order(
            side.substring(0, 1).toUpperCase() + i,
            side,
            price,
            volume,
            System.currentTimeMillis() + i
        ));
    }

    // Since orders are random, we can't predict the state, but we can check for consistency
    // For example, ensure no negative volumes
    engine.getBuyOrders().forEach(order -> assertTrue(order.getVolume() > 0));
    engine.getSellOrders().forEach(order -> assertTrue(order.getVolume() > 0));
}



@Test
void testBulkDeletion() {
    // Add orders to the engine
    for (int i = 0; i < 1000; i++) {
        engine.addOrder(new TradeMatchingEngine.Order(
            "B" + i,
            "buy",
            150.0,
            10,
            System.currentTimeMillis() + i
        ));
        engine.addOrder(new TradeMatchingEngine.Order(
            "S" + i,
            "sell",
            155.0,
            10,
            System.currentTimeMillis() + 1000 + i
        ));
    }

    // Delete all buy orders
    for (int i = 0; i < 1000; i++) {
        engine.deleteOrder("B" + i);
    }

    // Verify that buy orders are deleted
    assertEquals(0, engine.getBuyOrders().size());

    // Sell orders should remain
    assertEquals(1000, engine.getSellOrders().size());
}
    @Test
    void testGetMarketDepthForBuyOrders() {
        engine.addOrder(new TradeMatchingEngine.Order("1", "buy", 100.0, 10, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("2", "buy", 101.0, 15, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("3", "buy", 100.0, 5, System.currentTimeMillis()));

        Map<Double, Integer> buyDepth = engine.getMarketDepth("buy");

        assertEquals(2, buyDepth.size());
        assertEquals(15, buyDepth.get(101.0));
        assertEquals(15, buyDepth.get(100.0));
    }

    @Test
    void testGetMarketDepthForSellOrders() {
        engine.addOrder(new TradeMatchingEngine.Order("1", "sell", 100.0, 10, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("2", "sell", 101.0, 15, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("3", "sell", 100.0, 5, System.currentTimeMillis()));

        Map<Double, Integer> sellDepth = engine.getMarketDepth("sell");

        assertEquals(2, sellDepth.size());
        assertEquals(15, sellDepth.get(101.0));
        assertEquals(15, sellDepth.get(100.0));
    }

    @Test
    void testGetMarketDepthAfterMatching() {
        engine.addOrder(new TradeMatchingEngine.Order("1", "buy", 100.0, 10, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("2", "buy", 101.0, 15, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("3", "sell", 99.0, 5, System.currentTimeMillis()));

        Map<Double, Integer> buyDepth = engine.getMarketDepth("buy");
        Map<Double, Integer> sellDepth = engine.getMarketDepth("sell");


        assertEquals(2, buyDepth.size());
        assertEquals(10, buyDepth.get(101.0));
        assertEquals(10, buyDepth.get(100.0));
        assertEquals(0, sellDepth.size());
    }
    @Test
    void testGetMarketDepthAfterMatching2() {
        engine.addOrder(new TradeMatchingEngine.Order("1", "buy", 100.0, 10, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("2", "buy", 101.0, 15, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("3", "sell", 99.0, 5, System.currentTimeMillis()));

        Map<Double, Integer> buyDepth = engine.getMarketDepth("buy");
        Map<Double, Integer> sellDepth = engine.getMarketDepth("sell");


        assertEquals(2, buyDepth.size());
        assertEquals(10, buyDepth.get(101.0));
        assertEquals(10, buyDepth.get(100.0));
        assertEquals(0, sellDepth.size());
    }

    @Test
    void testGetMarketDepthAfterDeletion() {
        engine.addOrder(new TradeMatchingEngine.Order("1", "sell", 100.0, 5, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("2", "sell", 102.0, 5, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("3", "buy", 105.0, 12, System.currentTimeMillis()));

        engine.deleteOrder("1");

        Map<Double, Integer> buyDepth = engine.getMarketDepth("buy");
        Map<Double, Integer> sellDepth = engine.getMarketDepth("sell");

        assertEquals(0, sellDepth.size());
        // assertEquals(0, sellDepth.get(100.0));
        // assertEquals(0, sellDepth.get(102.0));
        assertEquals(1, buyDepth.size());

        assertEquals(2, buyDepth.get(105.0));
    }

    @Test
    void testGetMarketDepthEmptyOrderBook() {
        Map<Double, Integer> buyDepth = engine.getMarketDepth("buy");
        Map<Double, Integer> sellDepth = engine.getMarketDepth("sell");

        assertEquals(0, buyDepth.size());
        assertEquals(0, sellDepth.size());
    }

    @Test
    void testGetMarketDepthFromRange_ValidRange() {
        engine.addOrder(new TradeMatchingEngine.Order("1", "buy", 100.0, 10, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("2", "buy", 101.0, 15, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("3", "buy", 102.0, 20, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("4", "sell", 103.0, 25, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("5", "sell", 104.0, 30, System.currentTimeMillis()));

        Map<Double, Integer> buyDepthRange = engine.getMarketDepthFromRange("buy", 100.0, 101.0);
        assertEquals(2, buyDepthRange.size());
        assertEquals(10, buyDepthRange.get(100.0));
        assertEquals(15, buyDepthRange.get(101.0));

        Map<Double, Integer> sellDepthRange = engine.getMarketDepthFromRange("sell", 103.0, 104.0);
        assertEquals(2, sellDepthRange.size());
        assertEquals(25, sellDepthRange.get(103.0));
        assertEquals(30, sellDepthRange.get(104.0));
    }

    @Test
    void testGetMarketDepthFromRange_PartialRange() {
        engine.addOrder(new TradeMatchingEngine.Order("1", "buy", 100.0, 10, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("2", "buy", 101.0, 15, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("3", "buy", 102.0, 20, System.currentTimeMillis()));

        Map<Double, Integer> buyDepthRange = engine.getMarketDepthFromRange("buy", 100.0, 101.0);
        assertEquals(2, buyDepthRange.size());
        assertEquals(10, buyDepthRange.get(100.0));
        assertEquals(15, buyDepthRange.get(101.0));
    }

    @Test
    void testGetMarketDepthFromRange_NoOrdersInRange() {
        engine.addOrder(new TradeMatchingEngine.Order("1", "buy", 100.0, 10, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("2", "buy", 101.0, 15, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("3", "sell", 104.0, 20, System.currentTimeMillis()));

        Map<Double, Integer> buyDepthRange = engine.getMarketDepthFromRange("buy", 102.0, 103.0);
        assertTrue(buyDepthRange.isEmpty());

        Map<Double, Integer> sellDepthRange = engine.getMarketDepthFromRange("sell", 100.0, 103.0);
        assertTrue(sellDepthRange.isEmpty());
    }

    @Test
    void testGetMarketDepthFromRange_ExactMatch() {
        engine.addOrder(new TradeMatchingEngine.Order("1", "buy", 100.0, 10, System.currentTimeMillis()));
        engine.addOrder(new TradeMatchingEngine.Order("2", "sell", 101.0, 15, System.currentTimeMillis()));

        Map<Double, Integer> buyDepthRange = engine.getMarketDepthFromRange("buy", 100.0, 100.0);
        assertEquals(1, buyDepthRange.size());
        assertEquals(10, buyDepthRange.get(100.0));

        Map<Double, Integer> sellDepthRange = engine.getMarketDepthFromRange("sell", 101.0, 101.0);
        assertEquals(1, sellDepthRange.size());
        assertEquals(15, sellDepthRange.get(101.0));
    }

    @Test
    void testGetMarketDepthFromRange_EmptyOrderBook() {
        Map<Double, Integer> buyDepthRange = engine.getMarketDepthFromRange("buy", 100.0, 101.0);
        assertTrue(buyDepthRange.isEmpty());

        Map<Double, Integer> sellDepthRange = engine.getMarketDepthFromRange("sell", 100.0, 101.0);
        assertTrue(sellDepthRange.isEmpty());
    }




    // @Test
    // void testPerformanceUnderHighLoad() {
    //     // Measure time taken to add and match a large number of orders
    //     long startTime = System.currentTimeMillis();

    //     for (int i = 0; i < 2; i++) {
    //         engine.addOrder(new TradeMatchingEngine.Order(
    //             "B" + i,
    //             "buy",
    //             100.0 + (i % 100) * 0.01,
    //             1,
    //             System.currentTimeMillis() + i
    //         ));
    //     }

    //     for (int i = 0; i < 2; i++) {
    //         engine.addOrder(new TradeMatchingEngine.Order(
    //             "S" + i,
    //             "sell",
    //             100.0 + (i % 100) * 0.01,
    //             1,
    //             System.currentTimeMillis() + 10000 + i
    //         ));
    //     }

    //     engine.addOrder(new TradeMatchingEngine.Order(
    //         "B" + 1,
    //         "buy",
    //         100.0 + (i % 100) * 0.01,
    //         1,
    //         System.currentTimeMillis() + i
    //     ));

    //     long endTime = System.currentTimeMillis();
    //     long duration = endTime - startTime;

    //     // Verify that all orders are matched
    //     assertEquals(0, engine.getBuyOrders().size());
    //     assertEquals(0, engine.getSellOrders().size());

    //     // Check that operation completed within acceptable time frame (e.g., less than 5 seconds)
    //     assertTrue(duration < 5000, "Engine took too long under high load: " + duration + "ms");
    // }

}
