// package src.test.java;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


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
}
