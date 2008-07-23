package org.hypergraphdb.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;

import org.hypergraphdb.HGRandomAccessResult;

/**
 * 
 * <p>
 * An implementation <code>SortedSet</code> based on primitive arrays that grow
 * as needed without ever shrinking. Lookup is log(n), but insertion and removal
 * take longer obviously so this implementation is mainly suitable for small sets
 * of a few dozen elements, or for sets that are searched/iterated much more 
 * frequently than they are changed. The main reason for being of this implementation
 * is space efficiency: a red-black tree holds additional 12 bytes per datum. So while
 * for a large set, a tree should be used, the array-based implementation is preferable 
 * for many small sets like many of HyperGraphDB's incidence sets. 
 * </p>
 *
 * <p>
 * Some benchmarking experiments comparing this (rather simple) implementation to red-black
 * trees (both LLRBTree and the standard Java TreeSet): working with about up to 10000 elements,
 * insertion and removal have a comparable performance, with the array-based implementation
 * being about 15% slower (elements inserted/removed in random order). The LLRBTree implementation
 * is actually noticeably slower than TreeSet, probably due to recursion. However, in "read-mode", 
 * when iterating over the set, using it as a HGRandomAccessResult, the array-based implementation
 * is 8-10 faster. Understandable since here moving to the next element amounts to incrementing an integer
 * while in a tree a lookup for the successor must be performed (e.g. min(parent(current))). So for
 * set of this order of magnitude and/or sets that are being read more than they are modified, 
 * it is strongly advisable to use the ArrayBasedSet. 
 * </p>
 * 
 * @author Borislav Iordanov
 *
 * @param <E>
 */
@SuppressWarnings("unchecked")
public class ArrayBasedSet<E> implements SortedSet<E>
{
	Class<E> type;
	E [] array;
	Comparator<E> comparator = null;
	int size = 0;
	
	int lookup(E key)
	{
		int low = 0;
		int high = size-1;

		while (low <= high) 
		{
			int mid = (low + high) >> 1;
	    	E midVal = array[mid];
	    	int cmp = comparator.compare(midVal, key);
	    	if (cmp < 0)
	    		low = mid + 1;
	    	else if (cmp > 0)
	    		high = mid - 1;
	    	else
	    		return mid; // key found
		}
		return -(low + 1);  // key not found.		
	}
	
	public ArrayBasedSet(E [] A)
	{
		type = (Class<E>)A.getClass().getComponentType();
		array = (E[])java.lang.reflect.Array.newInstance(type, A.length);
		comparator = new Comparator<E>()
		{
			public int compare(E x, E y)
			{
				return ((Comparable)x).compareTo(y);
			}
		};
	}

	public ArrayBasedSet(E [] A, Comparator<E> comparator)
	{
		type = (Class<E>)A.getClass().getComponentType();
		array = (E[])java.lang.reflect.Array.newInstance(type, A.length);
		this.comparator = comparator;
	}
	
	public Comparator<? super E> comparator()
	{
		return comparator;
	}

	public E first()
	{
		if (size == 0)
			throw new NoSuchElementException();
		return array[0];
	}

	public SortedSet<E> headSet(E toElement)
	{
		throw new UnsupportedOperationException("...because of lazy implementor: this is a TODO.");
	}

	public E last()
	{
		if (size == 0)
			throw new NoSuchElementException();
		return array[size-1];
	}

	public SortedSet<E> subSet(E fromElement, E toElement)
	{
		throw new UnsupportedOperationException("...because of lazy implementor: this is a TODO.");
	}

	public SortedSet<E> tailSet(E fromElement)
	{
		throw new UnsupportedOperationException("...because of lazy implementor: this is a TODO.");
	}

	public boolean add(E o)
	{
		int idx = lookup((E)o);
		
		if (idx >= 0)
			return false;
		else 
			idx = -(idx + 1);
		if (size < array.length)
		{
			System.arraycopy(array, idx, array, idx + 1, size - idx);
			array[idx] = o;
		}
		else // need to grow the array...
		{
			E [] tmp = (E[])java.lang.reflect.Array.newInstance(type, (int)(1.5 * size) + 1);
			System.arraycopy(array, 0, tmp, 0, idx);
			tmp[idx] = o;
			System.arraycopy(array, idx, tmp, idx + 1, size - idx);
			array = tmp;
		}
		size++;
		return true;
	}

	public boolean addAll(Collection<? extends E> c)
	{
		boolean modified = false;
		for (Object x : c)
			if (add((E)x))
				modified = true;
		return modified;
	}

	public void clear()
	{
		size = 0;
	}

	public boolean contains(Object o)
	{
		return lookup((E)o) >= 0;
	}

	public boolean containsAll(Collection<?> c)
	{
		for (Object x  : c)
			if (!contains(x))
				return false;
		return true;
	}

	public boolean isEmpty()
	{
		return size == 0;
	}

	public Iterator<E> iterator()
	{
		return new ResultSet();
	}

	public HGRandomAccessResult<E> getSearchResult()
	{
		return new ResultSet();
	}
	
	public boolean remove(Object o)
	{
		int idx = lookup((E)o);
		if (idx < 0)
			return false;
		System.arraycopy(array, idx + 1, array, idx, size - idx);
		size--;
		return true;
	}

	public boolean removeAll(Collection<?> c)
	{
		boolean modified = false;
		for (Object x : c)
			if (remove((E)x))
				modified = true;
		return modified;
	}

	public boolean retainAll(Collection<?> c)
	{
		boolean modified = false;
		for (int i = 0; i < size; i++)
			if (!c.contains(array[i]))
			{
				System.arraycopy(array, i + 1, array, i, size - i);
				size--;
				modified = true;
			}
		return modified;
	}

	public int size()
	{
		return size;
	}

	public Object[] toArray()
	{
		return array;
	}

	public <T> T[] toArray(T[] a)
	{
		return (T[])array;
	}
	
	class ResultSet implements HGRandomAccessResult<E>
	{
		int pos = -1;
		
		public GotoResult goTo(E value, boolean exactMatch)
		{
			int idx = lookup(value);			
			if (idx >= 0)
			{
				pos = idx;
				return GotoResult.found;
			}
			else if (exactMatch)
				return GotoResult.nothing;
			else
			{
				idx = -(idx + 1);
				if (idx >= size)
					return GotoResult.nothing;
				pos = idx;
				return GotoResult.close;
			}
		}

		public boolean hasPrev()
		{
			return pos > 0;
		}

		public E prev()
		{
			return array[--pos];
		}

		public boolean hasNext()
		{
			return pos + 1 < size;
		}

		public E next()
		{
			return array[++pos];
		}

		public void remove()
		{
			throw new UnsupportedOperationException("...because of lazy implementor: this is a TODO.");
		}

		public void close()
		{
		}

		public E current()
		{
			if (pos < 0)
				throw new NoSuchElementException();
			return array[pos];
		}

		public boolean isOrdered()
		{
			return true;
		}		
	}
}