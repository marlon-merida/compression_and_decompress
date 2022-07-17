import java.io.IOException;
import java.util.HashMap;


public class HuffTree implements IHuffConstants {
	private TreeNode root;
	
	// creates a tree using freq array
	public HuffTree(int[] freq) {
		// creates a queue using freq array
		Queue<TreeNode> q = new Queue<>();
		for (int i = 0; i < freq.length; i++) {
			if (freq[i] != 0) {
				TreeNode curr = new TreeNode(i, freq[i]);
				q.add(curr);
			}
		}
		q.add(new TreeNode(PSEUDO_EOF, 1));
		// creates tree
		while (q.size() > 1) {
			TreeNode first = q.remove();
			TreeNode next = q.remove();
			TreeNode result = new TreeNode(first, first.getFrequency() + next.getFrequency(), next);
			q.add(result);
		}
		root = q.remove();
	}

	//  creates tree for tree header
	public HuffTree(BitInputStream in) throws IOException {
		root = constructTree(root, in);
	}

	// creates tree using tree header 
	private TreeNode constructTree(TreeNode n, BitInputStream in) throws IOException {
		// read bit one at a time
		int bit = in.readBits(1);
		if (bit == 1) {
			int value = in.readBits(BITS_PER_WORD + 1);
			TreeNode newNode = new TreeNode(value, 1);
			return newNode;
			//
		} else if (bit == 0) {
			TreeNode newNode = new TreeNode(-1, 0); // -1 represents empty
			newNode.setLeft(constructTree(newNode.getLeft(), in));
			newNode.setRight(constructTree(newNode.getRight(), in));
			return newNode;
		} else {
			throw new IOException("-1 error in rebuilding tree");
		}
	}

	// writes the tree header
	public void treeHeader(BitOutputStream out) {
		treeHeaderHelper(root, out);
	}
	
	// writes the tree header
	private void treeHeaderHelper(TreeNode n, BitOutputStream out) {
		if (n.isLeaf()) {
			// base case: we are at leaf so add value and encode
			out.writeBits(1, 1);
			out.writeBits(BITS_PER_WORD + 1, n.getValue());
		} else {
			out.writeBits(1, 0);
			treeHeaderHelper(n.getLeft(), out);
			treeHeaderHelper(n.getRight(), out);
		}
	}
	
	// counts number of bits in tree
	public int countTree() {
		return countTreeHelper(root);
	}
	
	// counts number of bits in tree
	private int countTreeHelper(TreeNode n) {
		if (n.isLeaf()) {
			// need to return at least 1 because this is a node
			return 1 + (BITS_PER_WORD + 1);
		} else {
			// return 1 + bits per word for value inside node
			return 1 + countTreeHelper(n.getLeft()) + countTreeHelper(n.getRight());
		}
	}
	
	// decompressing using the tree
	public int decompressTraverseTree(BitInputStream in, BitOutputStream out) 
			throws IOException {
		int count = 0;
		TreeNode n = root;
		boolean notEnd = true;
		while (notEnd) {
			if (!n.isLeaf()) {
				int inbits = in.readBits(1);
				if (inbits == -1) {
					throw new IOException();
				}
				// go left
				if (inbits == 0) {
					n = n.getLeft();
					// go right
				} else if (inbits == 1) {
					n = n.getRight();
				}
			} else {
				if (n.getValue() == PSEUDO_EOF) {
					notEnd = false;
				} else {
					out.writeBits(BITS_PER_WORD, n.getValue());
					count += BITS_PER_WORD;
					n = root;
				}
			}
		}
		return count;
	}
	
	// build hashamap from tree
	public void buildMap(HashMap<Integer, String> map) {
		map(root, "", map);
	}
	
	// creates the hashmap from the tree of values and the 
	// respective encoding value
	private void map(TreeNode n, String s, HashMap<Integer, String> map) {
		if (n.getRight() == null && n.getLeft() == null) {
			// base case, we are at child node so add value and encoding
			map.put(n.getValue(), s);
		} else if (!n.isLeaf()) {
			map(n.getLeft(), s + '0', map);
			map(n.getRight(), s + '1', map);
		}
	}
}
