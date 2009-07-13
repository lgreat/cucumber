package gs.web.content;

import gs.web.BaseTestCase;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class TargetSupplyListTest extends BaseTestCase {

    public void testCounts() {
        assertEquals(22, TargetSupplyList.getElementaryItems().length);
        assertEquals(37, TargetSupplyList.getMiddleItems().length);
        assertEquals(23, TargetSupplyList.getHighItems().length);
        assertEquals(17, TargetSupplyList.getElementaryMiddleItems().length);
    }

    public void testRandomElementary() {
        TargetSupplyList.TargetSupplyItem[] items = TargetSupplyList.getRandomElementaryItems();
        assertEquals(TargetSupplyList.NUM_RANDOM_ITEMS, items.length);
        for (TargetSupplyList.TargetSupplyItem item: items) {
            assertTrue("Expect items to be in e category", item.isE());
        }
    }

    public void testRandomMiddle() {
        TargetSupplyList.TargetSupplyItem[] items = TargetSupplyList.getRandomMiddleItems();
        assertEquals(TargetSupplyList.NUM_RANDOM_ITEMS, items.length);
        for (TargetSupplyList.TargetSupplyItem item: items) {
            assertTrue("Expect items to be in m category", item.isM());
        }
    }

    public void testRandomGeneric() {
        TargetSupplyList.TargetSupplyItem[] items = TargetSupplyList.getRandomGenericItems();
        assertEquals(TargetSupplyList.NUM_RANDOM_ITEMS, items.length);
        for (TargetSupplyList.TargetSupplyItem item: items) {
            assertTrue("Expect items to be in e/m category", item.isEM());
        }
    }

    public void testRandomHigh() {
        TargetSupplyList.TargetSupplyItem[] items = TargetSupplyList.getRandomHighItems();
        assertEquals(TargetSupplyList.NUM_RANDOM_ITEMS, items.length);
        for (TargetSupplyList.TargetSupplyItem item: items) {
            assertTrue("Expect items to be in h category", item.isH());
        }
    }
    
    public void testElementary() {
        TargetSupplyList.TargetSupplyItem[] elementaryItems = TargetSupplyList.getElementaryItems();
        for (TargetSupplyList.TargetSupplyItem item: elementaryItems) {
            assertTrue("Expect item to be in e category: " + item, item.isE());
        }
    }

    public void testMiddle() {
        TargetSupplyList.TargetSupplyItem[] middleItems = TargetSupplyList.getMiddleItems();
        for (TargetSupplyList.TargetSupplyItem item: middleItems) {
            assertTrue("Expect item to be in m category: " + item, item.isM());
        }
    }

    public void testHigh() {
        TargetSupplyList.TargetSupplyItem[] high_items = TargetSupplyList.getHighItems();
        for (TargetSupplyList.TargetSupplyItem item: high_items) {
            assertTrue("Expect item to be in h category: " + item, item.isH());
        }

    }
}
