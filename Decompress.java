import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class Decompress implements IHuffConstants {

	private BitInputStream in;
	private BitOutputStream out;
	private IHuffViewer viewer;
	private int[] freq;

	// creates a decompression object
	public Decompress(InputStream in, OutputStream out, IHuffViewer viewer) {
		this.in = new BitInputStream(in);
		this.out = new BitOutputStream(out);
		this.viewer = viewer;
		freq = new int[ALPH_SIZE];
	}

	// reads information needed for decompress with chosen header
	public int decompress() throws IOException {
		int count = 0;
		int magic = in.readBits(BITS_PER_INT);
		if (magic != MAGIC_NUMBER) {
			viewer.showError(
					"Error reading compressed file. \n" + "File did not start with the " 
			+ "huff magic number.");
			return -1;
		} else {
			magic = in.readBits(BITS_PER_INT);
			if (magic == STORE_COUNTS) {
				count = countDecompress();
			} else {
				count = treeDecompress();
			}
		}
		return count;
	}

	// decompress with count header
	private int countDecompress() throws IOException {
		int index = 0;
		// creates freq array
		while (index < ALPH_SIZE) {
			int inbits = in.readBits(BITS_PER_INT);
			if (inbits == -1) {
				throw new IOException();
			}
			freq[index] = inbits;
			index++;
		}
		// creates tree for decompress traversal using freq array
		HuffTree tree = new HuffTree(freq);
		return tree.decompressTraverseTree(in, out);
	}

	// decompress with tree header
	private int treeDecompress() throws IOException {
		in.readBits(BITS_PER_INT); // reads and skips the size of tree
		// creates tree for decompress traversal
		HuffTree tree = new HuffTree(in);
		return tree.decompressTraverseTree(in, out);
	}
}
