package comp2402a3;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;

/**
 * An implementation of skiplists for searching
 *
 *
 * @param <T>
 */
public class SkippityFast<T> implements IndexedSSet<T> {
	protected Comparator<T> c;

	@SuppressWarnings("unchecked")
	protected static class Node<T> {
		T x;
		Node<T>[] next;
		int[] length;

		public Node(T ix, int h) {
			x = ix;
			next = (Node<T>[]) Array.newInstance(Node.class, h + 1);
			length = new int[h + 1];
		}

		public int height() {
			return next.length - 1;
		}
	}

	/**
	 * This node<T> sits on the left side of the skiplist
	 */
	protected Node<T> sentinel;

	/**
	 * The maximum height of any element
	 */
	int h;

	/**
	 * The number of elements stored in the skiplist
	 */
	int n;

	/**
	 * A source of random numbers
	 */
	Random rand;

	/**
	 * Used by add(x) method
	 */
	protected Node<T>[] stack;

	@SuppressWarnings("unchecked")
	public SkippityFast(Comparator<T> c) {
		this.c = c;
		n = 0;
		sentinel = new Node<T>(null, 32);
		stack = (Node<T>[]) Array.newInstance(Node.class, sentinel.next.length);
		h = 0;
		rand = new Random();
	}

	public SkippityFast() {
		this(new DefaultComparator<T>());
	}

	/**
	 * Simulate repeatedly tossing a coin until it comes up tails.
	 * Note, this code will never generate a height greater than 32
	 * 
	 * @return the number of coin tosses - 1
	 */
	protected int pickHeight() {
		int z = rand.nextInt();
		int k = 0;
		int m = 1;
		while ((z & m) != 0) {
			k++;
			m <<= 1;
		}
		return k;
	}

	/**
	 * Find the node<T> u that precedes the value x in the skiplist.
	 *
	 * @param x - the value to search for
	 * @return a node<T> u that maximizes u.x subject to
	 *         the constraint that u.x < x --- or sentinel if u.x >= x for
	 *         all node<T>s x
	 */
	protected Node<T> findPredNode(T x) {
		Node<T> u = sentinel;
		int r = h;
		while (r >= 0) {
			while (u.next[r] != null && c.compare(u.next[r].x, x) < 0)
				u = u.next[r]; // go right in list r
			r--; // go down into list r-1
		}
		return u;
	}

	public T find(T x) {
		Node<T> u = findPredNode(x);
		return u.next[0] == null ? null : u.next[0].x;
	}

	public T findGE(T x) {
		if (x == null) { // return first node<T>
			return sentinel.next[0] == null ? null : sentinel.next[0].x;
		}
		return find(x);
	}

	public T findLT(T x) {
		if (x == null) { // return last node<T>
			Node<T> u = sentinel;
			int r = h;
			while (r >= 0) {
				while (u.next[r] != null)
					u = u.next[r];
				r--;
			}
			return u.x;
		}
		return findPredNode(x).x;
	}

	public boolean add(T x) {
		Node<T> u = sentinel;
		int r = h;
		int i = 0;
		int comp = 0;
		while (r >= 0) {
			while (u.next[r] != null && (comp = c.compare(u.next[r].x, x)) < 0){
				i += u.length[r];
				u = u.next[r];
			}
			if (u.next[r] != null && comp == 0){
				return false;
			}
			stack[r--] = u; // going down, store u
		}
		Node<T> w = new Node<T>(x, pickHeight());
		// while (h < w.height())
		// stack[++h] = sentinel; // height increased
		// for (int i = 0; i < w.next.length; i++) {
		// w.next[i] = stack[i].next[i];
		// stack[i].next[i] = w;
		// }

		if (w.height() > h) {
			h = w.height();
		}
		Node<T> u_curr = sentinel;
		int k = w.height();
		int r_curr = h;
		int j = -1; // index of u
		while (r >= 0) {
			while (u_curr.next[r_curr] != null && j + u_curr.length[r_curr] < i) {
				j += u_curr.length[r_curr];
				u_curr = u_curr.next[r_curr];
			}
			u_curr.length[r_curr]++; // accounts for new node in list 0
			if (r_curr <= k) {
				w.next[r_curr] = u_curr.next[r_curr];
				u_curr.next[r_curr] = w;
				w.length[r_curr] = u_curr.length[r_curr] - (i - j);
				u_curr.length[r_curr] = i - j;
			}
			r_curr--;
		}
		n++;
		return true;
	}

	public boolean remove(T x) {
		boolean removed = false;
		Node<T> u = sentinel;
		int r = h;
		int comp = 0;
		while (r >= 0) {
			while (u.next[r] != null
					&& (comp = c.compare(u.next[r].x, x)) < 0) {
				u = u.next[r];
			}
			u.length[r]--;

			if (u.next[r] != null && comp == 0) {
				removed = true;
				u.length[r] += u.next[r].length[r];
				u.next[r] = u.next[r].next[r];
				if (u == sentinel && u.next[r] == null)
					h--; // height has gone down
			}
			r--;
		}
		if (removed)
			n--;
		return removed;
	}

    public T get(int i) {
        Node <T> u = sentinel;
        int r = h;
        int j = -1;   // index of the current node in list 0
        while (r >= 0) {
            while (u.next[r] != null && j + u.length[r] < i) {
                j += u.length[r];
                u = u.next[r];
            }
            r--;
        }
        return u.next[0].x;
    }

	public int rangecount(T x, T y) {
		int i1 = 0;
		int i2 = 0;
		// This is just a copy of the slow version
		// TODO: You need to rewrite this method so that it is faster
		if (c.compare(x, y) > 0) { // checks if it's the same element
			T temp = x;
			x = y;
			y = temp;
		}
		Node<T> u= sentinel;
		Node<T> u2 = sentinel;

		int r = h;
		int r2 = h;

		int i = 0;
		int comp = 0;
		int comp2 = 0;

		while (r >= 0) {
			while (u.next[r] != null && (comp = c.compare(u.next[r].x, x)) < 0){
				i += u.length[r];
				u = u.next[r];
			}
			while (u.next[r] != null && (comp2 = c.compare(u2.next[r2].y, y)) < 0){
				i += u.length[r];
				u = u.next[r];
			}

			
			if (u.next[r] != null && comp == 0){
				return false;
			}
			stack[r--] = u; // going down, store u
		}
		
		// int count = 0;
		// Iterator<T> it = this.iterator();
		// for (i = 0; i < n; i++) {
		// 	T item = it.next();
		// 	if (c.compare(item, x) >= 0 && c.compare(item, y) <= 0)
		// 		count++;
		// }
		// return count;
	}                                     

	public void clear() {
		n = 0;
		h = 0;
		Arrays.fill(sentinel.next, null);
	}

	public int size() {
		return n;
	}

	public Comparator<T> comparator() {
		return c;
	}

	/**
	 * Create a new iterator in which the next value in the iteration is u.next.x
	 * 
	 * @param u
	 * @return
	 */
	protected Iterator<T> iterator(Node<T> u) {
		class SkiplistIterator implements Iterator<T> {
			Node<T> u, prev;

			public SkiplistIterator(Node<T> u) {
				this.u = u;
				prev = null;
			}

			public boolean hasNext() {
				return u.next[0] != null;
			}

			public T next() {
				prev = u;
				u = u.next[0];
				return u.x;
			}

			public void remove() {
				// Not constant time

			}
		}
		return new SkiplistIterator(u);
	}

	public Iterator<T> iterator() {
		return iterator(sentinel);
	}

	public Iterator<T> iterator(T x) {
		return iterator(findPredNode(x));
	}
}
