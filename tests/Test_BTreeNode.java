import sun.reflect.generics.tree.Tree;

import java.util.ArrayList;

/**
 * test class for the BTreeNode
 */
public class Test_BTreeNode {
    public static void main(String[] args) {
        System.out.println("BTreeNode Test:");
        testAdd();
        testSearch();

    }

    static void testSearch() {
        System.out.println("\nsearchNode Tests:");
        int degree = 3;
        long[] key = {0, 2, 4, 6, 8};
        int[] children = {0, 1, 2, 3, 4, 5};

        BTreeNode tNode = initNode(key, children, -1, degree, false);
        BTreeNode.SearchResult result = null;

        //found
        result = tNode.searchNode(new TreeObject(2));
        BTest.testBoolean("Found", result.wasFound, true);

        //not found (not leaf)
        //beginning
        result = tNode.searchNode(new TreeObject(-1));
        BTest.testInt("Not found, not leaf, beginning", result.location, 0);

        //middle
        result = tNode.searchNode(new TreeObject(3));
        BTest.testInt("Not found, not leaf, middle", result.location, 2);

        //beginning
        result = tNode.searchNode(new TreeObject(10));
        BTest.testInt("Not found, not leaf, end", result.location, 5);

        //not found (leaf)
        tNode = initNode(key, children, -1, degree, true);
        result = tNode.searchNode(new TreeObject(10));
        BTest.testInt("Not found, leaf", result.location, -1);
    }

    static void testAdd() {
        System.out.println("\naddObject Tests:");
        int degree = 3;
        int[] fiveChildren = {-1, -1, -1, -1, -1};
        int[] sixChildren = {-1, -1, -1, -1, -1, -1};

        long[][] startKeys = {{0, 1, 3, 4},//add middle
                {0, 1, 2, 3}, //add end
                {1, 2, 3, 4}, //add beginning
                {0, 1, 2, 3}, //inc freq
                {0, 1, 2, 3, 4}}; //full

        long[] add = {2, 4, 0, 2, 6};

        long[] correct;
        //add middle end and beginning
        correct = new long[]{0, 1, 2, 3, 4};
        BTreeNode cNode = initNode(correct, sixChildren, -1, degree, true);

        //middle
        BTreeNode tNode = initNode(startKeys[0], fiveChildren, -1, degree, true);
        tNode.addObject(new TreeObject(add[0]));
        BTest.testString("Add to middle", tNode.toString(), cNode.toString());

        //end
        tNode = initNode(startKeys[1], fiveChildren, -1, degree, true);
        tNode.addObject(new TreeObject(add[1]));
        BTest.testString("Add to end", tNode.toString(), cNode.toString());

        //begin
        tNode = initNode(startKeys[2], fiveChildren, -1, degree, true);
        tNode.addObject(new TreeObject(add[2]));
        BTest.testString("Add to beginning", tNode.toString(), cNode.toString());

        //frequency
        cNode = initNode(startKeys[3], fiveChildren, -1, degree, true);
        cNode.incrementFrequency(2);

        tNode = initNode(startKeys[3], fiveChildren, -1, degree, true);
        tNode.addObject(new TreeObject(add[3]));
        BTest.testString("Increment Frequency", tNode.toString(), cNode.toString());

        //full exception
        final BTreeNode eNode = initNode(startKeys[4], sixChildren, -1, degree, true);
        BTest.testException("Add to Full", IllegalStateException.class, () -> {
            eNode.addObject(new TreeObject(0));
            return false;
        });

    }

    static BTreeNode initNode(long[] keys, int[] children, int parent, int degree, boolean leaf) {
        ArrayList<TreeObject> ary = new ArrayList<>();
        for (long l : keys) {
            ary.add(new TreeObject(l));
        }

        ArrayList<Integer> cAry = new ArrayList<>();
        for (int i : children) {
            cAry.add(i);
        }

        return new BTreeNode(ary, cAry, parent, degree, leaf);
    }
}
