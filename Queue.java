import java.util.LinkedList;

public class Queue<E extends Comparable<? super E>> {

	LinkedList<E> con;

	// creates a fair priority queue
	public Queue() {
		con = new LinkedList<>();
	}

	// adds the element in corresponding place in the list
	public void add(E node) {
		if (con.size() == 0) {
			con.addFirst(node);
		} else {
			int index = 0;
			boolean notInserted = true;
			while (index < con.size() && notInserted) {
				E curr = con.get(index);
				if (node.compareTo(curr) < 0) {
					con.add(index, node);
					notInserted = false;
				}
				index++;
			}
			if (notInserted) {
				con.addLast(node);
			}
		}
	}

	// removes the first element of the list
	public E remove() {
		return con.removeFirst();
	}

	// return size of the queue
	public int size() {
		return con.size();
	}
}
