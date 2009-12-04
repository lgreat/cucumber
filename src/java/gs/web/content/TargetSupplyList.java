package gs.web.content;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class TargetSupplyList {
    private static final int LIST_E = 1;
    private static final int LIST_M = 2;
    private static final int LIST_H = 4;
    private static Set<TargetSupplyItem> E_ONLY_ITEMS = new HashSet<TargetSupplyItem>();
    private static Set<TargetSupplyItem> M_ONLY_ITEMS = new HashSet<TargetSupplyItem>();
    private static Set<TargetSupplyItem> H_ONLY_ITEMS = new HashSet<TargetSupplyItem>();
    private static Set<TargetSupplyItem> E_M_ITEMS = new HashSet<TargetSupplyItem>();
    public static final int NUM_RANDOM_ITEMS = 4;

    static {
        addToLists(new TargetSupplyItem("B000QDY7E6", "Crayola 54ct Trayola Colored Pencils", LIST_E));
        addToLists(new TargetSupplyItem("B000H6B0IS", "Crayola Trayola Washable Markers", LIST_E | LIST_M));
        addToLists(new TargetSupplyItem("B000J0C6D0", "Sharpie Permanent Marker Set - 12pc Set", LIST_M | LIST_H));
        addToLists(new TargetSupplyItem("B0014E4642", "Straight Scissors - 8\"", LIST_E | LIST_M));
        addToLists(new TargetSupplyItem("B00005C3YY", "450\" Magic Tape Dispenser- 12 Rolls", LIST_E | LIST_M));
        addToLists(new TargetSupplyItem("B0002XI1RO", "Self-Stick Page Marker - 6-pk.", LIST_E | LIST_M | LIST_H));
        addToLists(new TargetSupplyItem("B000BL2UKQ", "Recycled Post-it Note Pads 12-pk.", LIST_E | LIST_M));
        addToLists(new TargetSupplyItem("B001COOB76", "Gourmet Scented Pencils Set of 10", LIST_E | LIST_M));
        addToLists(new TargetSupplyItem("B0006HUSRW", "- Evidence Recycled Writing Pads Wh", LIST_E | LIST_M));
        addToLists(new TargetSupplyItem("B000RJ4JL0", "Colored Pencil Set in Kraft Tube", LIST_E | LIST_M));
        addToLists(new TargetSupplyItem("B0006A30KQ", "Desktop Organizer-4 Shelf- Black", LIST_E | LIST_M));
        addToLists(new TargetSupplyItem("B000PULZ4A", "Planning Calendar With Stickers", LIST_E | LIST_M));
        addToLists(new TargetSupplyItem("B001NN70LA", "- Identity Theft- ShredderShark", LIST_M | LIST_H));
        addToLists(new TargetSupplyItem("B0000AQOCM", "- BIC Mark-It Permanent Marker Blue", LIST_M | LIST_H));
        addToLists(new TargetSupplyItem("B001QT1CVA", "Transformers Blue Backpack", LIST_E | LIST_M));
        addToLists(new TargetSupplyItem("B001OVT30C", "iCarly Pixilate Backpack", LIST_E | LIST_M));
        addToLists(new TargetSupplyItem("B001QT1CSI", "Pink Ni Hao, Kai-lan Let's Go Backpack", LIST_E | LIST_M));
        addToLists(new TargetSupplyItem("B001OVLMJW", "Bakugan Pyrus World Backpack", LIST_E | LIST_M));
        addToLists(new TargetSupplyItem("B001QT3E7A", "Nick Jr. Go Diego Go! Blue Wild Backpack", LIST_E));
        addToLists(new TargetSupplyItem("B001QT1DDW", "SWGR Backpack Black/Charcoal", LIST_E | LIST_M));
        addToLists(new TargetSupplyItem("B001QTHZM0", "Embark Fun Friends Rolling Backpack", LIST_E | LIST_M));
        addToLists(new TargetSupplyItem("B000TCZ252", "Texas Instruments Scientific Calculator - Pink (30XIIS)", LIST_M | LIST_H));
        addToLists(new TargetSupplyItem("B0002S07VM", "Dane-Elec 1 GB SD Flash Card", LIST_M | LIST_H));
        addToLists(new TargetSupplyItem("B000A3IAHM", "Casio Scientific Calculator Silver", LIST_M | LIST_H));
        addToLists(new TargetSupplyItem("B00004TVDO", "Casio FX-115MS-SR Solar Calculator", LIST_M | LIST_H));
        addToLists(new TargetSupplyItem("B000TXX7W6", "Dane-Elec 4GB USB Drive - Red", LIST_M | LIST_H));
        addToLists(new TargetSupplyItem("B001GT32S6", "Sennheiser Sport II Water Resistance Headphones", LIST_M | LIST_H));
        addToLists(new TargetSupplyItem("B0023B138W", "Acer Aspire One 10.1\" UltraThin Netbook Computer - Blue (AOD250-1165)", LIST_M | LIST_H));
        addToLists(new TargetSupplyItem("B0016637SY", "Casio FX9750GAPlus Graphing Calculator", LIST_M | LIST_H));
        addToLists(new TargetSupplyItem("B001COBZWU", "Lexar 2GB SD Memory Card - SD2GB-60-740", LIST_M | LIST_H));
        addToLists(new TargetSupplyItem("B0009JFDHM", "Casio Graphing Calculator Gray", LIST_M | LIST_H));
        addToLists(new TargetSupplyItem("B0001EMM0Q", "TI 84 PLU SE TI 84+ Silver Edition", LIST_M | LIST_H));
        addToLists(new TargetSupplyItem("B001FA1NRI", "2nd Gen Shuffle Blue 1GB", LIST_M | LIST_H));
        addToLists(new TargetSupplyItem("B001FA1NSC", "2nd Gen Shuffle Pink 1GB", LIST_M | LIST_H));
        addToLists(new TargetSupplyItem("B001RPNFOU", "Memorex travel ipod speaker", LIST_M | LIST_H));
        addToLists(new TargetSupplyItem("B002CX3VM2", "Simone Legno for Target Composition Book", LIST_E | LIST_M));
        addToLists(new TargetSupplyItem("B001QTC2IW", "Embark Backpack", LIST_E));
        addToLists(new TargetSupplyItem("B001QTC2JG", "Embark Backpack", LIST_M | LIST_H));
        addToLists(new TargetSupplyItem("B0019665DK", "Crayola Pip-squeaks Skinnies Washable Markers", LIST_E));
        addToLists(new TargetSupplyItem("B002CX3VDG", "Simone Legno for Target 5 Count Mechanical Pencils", LIST_E));
        addToLists(new TargetSupplyItem("B002CX7PGA", "Simone Legno for Target Portfolio", LIST_M | LIST_H));
        addToLists(new TargetSupplyItem("B002CX931K", "Simone Legno for Target 1\" Binder", LIST_M | LIST_H));

        addToLists(new TargetSupplyItem("B000BMBUAG", "5-ColorAccent Highlighter Pens- 2 Sets", LIST_H));
        addToLists(new TargetSupplyItem("B001S41SP8", "Pilot VBall Roller Ball Pen Collection", LIST_H));
    }

    private static void addToLists(TargetSupplyItem item) {
        if (item.isE()) {
            E_ONLY_ITEMS.add(item);
        }
        if (item.isM()) {
            M_ONLY_ITEMS.add(item);
        }
        if (item.isEM()) {
            E_M_ITEMS.add(item);
        }
        if (item.isH()) {
            H_ONLY_ITEMS.add(item);
        }
    }

    protected static TargetSupplyItem[] getElementaryItems() {
        return E_ONLY_ITEMS.toArray(new TargetSupplyItem[E_ONLY_ITEMS.size()]);
    }

    protected static TargetSupplyItem[] getMiddleItems() {
        return M_ONLY_ITEMS.toArray(new TargetSupplyItem[M_ONLY_ITEMS.size()]);
    }

    protected static TargetSupplyItem[] getHighItems() {
        return H_ONLY_ITEMS.toArray(new TargetSupplyItem[H_ONLY_ITEMS.size()]);
    }

    protected static TargetSupplyItem[] getElementaryMiddleItems() {
        return E_M_ITEMS.toArray(new TargetSupplyItem[E_M_ITEMS.size()]);
    }

    public static TargetSupplyItem[] getRandomElementaryItems() {
        return getRandomItemsFromCollection(E_ONLY_ITEMS);
    }

    public static TargetSupplyItem[] getRandomMiddleItems() {
        return getRandomItemsFromCollection(M_ONLY_ITEMS);
    }

    public static TargetSupplyItem[] getRandomHighItems() {
        return getRandomItemsFromCollection(H_ONLY_ITEMS);
    }

    public static TargetSupplyItem[] getRandomGenericItems() {
        return getRandomItemsFromCollection(E_M_ITEMS);
    }

    protected static TargetSupplyItem[] getRandomItemsFromCollection(Set<TargetSupplyItem> items) {
        Set<TargetSupplyItem> uniqueItems = new HashSet<TargetSupplyItem>(NUM_RANDOM_ITEMS);
        int index = RandomUtils.nextInt(items.size());
        TargetSupplyItem[] itemsToChooseFrom = items.toArray(new TargetSupplyItem[items.size()]);
        uniqueItems.add(itemsToChooseFrom[index]);
        while (uniqueItems.size() < NUM_RANDOM_ITEMS) {
            index = RandomUtils.nextInt(items.size());
            uniqueItems.add(itemsToChooseFrom[index]);
        }
        return uniqueItems.toArray(new TargetSupplyItem[NUM_RANDOM_ITEMS]);
    }

    public static class TargetSupplyItem {
        private String _itemIdentifier;
        private String _link;
        private String _text;
        private int _listCategory;

        public TargetSupplyItem(String itemIdentifier, String text, int listCategory) {
            _itemIdentifier = itemIdentifier;
            _link = "http://www.target.com/dp/" + itemIdentifier;
            _text = text;
            _listCategory = listCategory;
        }
        public String getImageName() {
            return _itemIdentifier;
        }
        public String getLink() {
            return _link;
        }
        public String getText() {
            return StringUtils.replace(_text, "\"", "&quot;");
        }
        public boolean isE() {
            return (_listCategory & LIST_E) == LIST_E;
        }
        public boolean isM() {
            return (_listCategory & LIST_M) == LIST_M;
        }
        public boolean isEM() {
            return (_listCategory & (LIST_E | LIST_M)) == (LIST_E | LIST_M);
        }
        public boolean isH() {
            return (_listCategory & LIST_H) == LIST_H;
        }
        public boolean isMH() {
            return (_listCategory & (LIST_M | LIST_H)) == (LIST_M | LIST_H);
        }
        @Override
        public String toString() {
            return _itemIdentifier + ":" + _text;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TargetSupplyItem that = (TargetSupplyItem) o;
            return _itemIdentifier.equals(that._itemIdentifier);
        }
        @Override
        public int hashCode() {
            return _itemIdentifier.hashCode();
        }
    }
}