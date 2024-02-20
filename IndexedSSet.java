package comp2402a3;

/**
 * The SSet<T> interface is a simple interface that allows a class to implement
 * all the functionality of the (more complicated) SortedSet<T> interface. Any
 * class that implements SSet<T> can be wrapped in a SortedSSet<T> to obtain an
 * implementation of SortedSet<T>
 *
 *
 * @param <T>
 * @see SortedSSet<T>
 */
public interface IndexedSSet<T> extends SSet<T> {
	/**
	 * Get the element at index i in the SSet. This is the value x in the SSet
	 * such that the SSet contains exactly i values less than x
	 *
	 * @param x
	 * @return the element at index i
	 */
	public T get(int i);

	/**
	 * Count the number of elements between the x and y inclusive
	 *
	 * @param x
	 * @return the number of elements that are larger or equal to x
	 * and smaller or equal to y
	 */
	public int rangecount(T x, T y);
}
