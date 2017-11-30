import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Test class for testing a small degree 2 tree
 */
public class Test_SmallTree {
    public static void main(String[] args) throws IOException{
        int degree = 2;
        long[] add = {
                100, 200, 300, 150, 125, 120,
                110, 325, 350, 400, 105
        };

        long[] sorted = Arrays.copyOf(add, add.length);
        Arrays.sort(sorted);
        for (long l : sorted) {
            System.out.println(l + " " + DNAUtil.convertLongToString(l, 7));
        }

        BTreeFile f = BTreeFile.createNewBTreeFile("dumps/smallTree.tree", BTree.METADATA_BYTE_SIZE,
                BTreeNode.getByteSize(degree));
        BTree tree = new BTree(f, 7, degree, 100);
        for (long l : add) {
            tree.add(l);
        }
        tree.dumpDataToFile("dumps/smallTree_beforeClose");
        tree.dumpInOrderToFile("dumps/dump");
        tree.close();

        f = BTreeFile.openBTreeFile("dumps/smallTree.tree", BTree.METADATA_BYTE_SIZE);
        tree = new BTree(f);
        tree.dumpDataToFile("dumps/smallTree_afterClose");

    }
}
