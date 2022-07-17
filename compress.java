
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class compress implements IHuffConstants {
	private HashMap<Integer, String> map;
	private int headerFormat;
	private int[] freq;
	private int numBitsSaved;
	private int compBits;
	private HuffTree tree;
	private boolean processed;

	// creates a compress object
	public compress(int headerFormat) throws IOException {
		map = new HashMap<>();
		this.headerFormat = headerFormat;
		freq = new int[ALPH_SIZE];
		processed = false;
	}

	// creates the tree needed for encoding
	public int preprocess(InputStream in) throws IOException {
		processed = true;
		BitInputStream bits = new BitInputStream(in);
		int inbits = bits.readBits(BITS_PER_WORD);
		// reads every bit and places in respective freq index
		while (inbits != -1) {
			freq[inbits]++;
			inbits = bits.readBits(BITS_PER_WORD);
		}
		bits.close();
		tree = new HuffTree(freq);
		// creates the tree needed for encoding
		tree.buildMap(map);
		// difference in compressed bits vs original bits
		numBitsSaved = countCompBits();
		return numBitsSaved;
	}

	// encodes the file
	public int encode(OutputStream out, boolean force, InputStream in) throws IOException {
		// if compressed bits is greater and force is on
		if ((numBitsSaved > 0 || (numBitsSaved < 0 && force)) && processed) {
			BitOutputStream output = new BitOutputStream(out);
			BitInputStream bits = new BitInputStream(in);
			// determines what header and writes respective header format
			header(output);
			int inbits = bits.readBits(BITS_PER_WORD);
			while (inbits != -1) {
				// writes out encoding inside the map
				writeBits(output, map.get(inbits));
				inbits = bits.readBits(BITS_PER_WORD);
			}
			// writes peof value 
			String peof = map.get(PSEUDO_EOF);
			writeBits(output, peof);
			output.close();
			bits.close();
			return compBits;
		} else {
			return 0;		
		}
	}

	// converts string into bit representation
	private void writeBits(BitOutputStream out, String s) {
		for (int i = 0; i < s.length(); i++) {
			char curr = s.charAt(i);
			if (curr == '0') {
				out.writeBits(1, curr);
			} else {
				out.writeBits(1, curr);
			}
		}
	}

	// writes out information for chosen header
	private void header(BitOutputStream out) throws IOException {
		if (headerFormat != STORE_COUNTS && headerFormat != STORE_TREE && headerFormat != STORE_CUSTOM) {
			throw new IOException();
		} 
		// writes out initial huff information
		out.writeBits(BITS_PER_INT, MAGIC_NUMBER);
		out.writeBits(BITS_PER_INT, headerFormat);
		if (headerFormat == STORE_COUNTS) {
			// writes out freq array
			for (int i = 0; i < freq.length; i++) {
				out.writeBits(BITS_PER_INT, freq[i]);
			}
		} else if (headerFormat == STORE_TREE) {
			out.writeBits(BITS_PER_INT, tree.countTree());
			tree.treeHeader(out);
		}
	}

	// calculates the difference of bits
	private int countCompBits() {
		int compBits = 0;
		// add the bits in the map
		for (HashMap.Entry<Integer, String> entry : map.entrySet()) {
			if (entry.getKey() != PSEUDO_EOF) {
				// frequency because of how many times we write it, and length because of
				// how our writeBits works
				compBits += freq[entry.getKey()] * entry.getValue().length();
			}
		}
		// accounts for main number and header format value
		compBits += BITS_PER_INT * 2;
		// adding the PEOF value bits
		compBits += map.get(PSEUDO_EOF).length();
		// add the bits depending on the header format
		if (headerFormat == STORE_COUNTS) {
			compBits += ALPH_SIZE * BITS_PER_INT;
		} else if (headerFormat == STORE_TREE) {
			compBits += BITS_PER_INT;
			compBits += tree.countTree();
		}
		// count the original number of bits
		int origBits = 0;
		for (int i = 0; i < freq.length; i++) {
			origBits += freq[i] * BITS_PER_WORD;
		}
		this.compBits = compBits;
		return origBits - compBits;
	}
}
