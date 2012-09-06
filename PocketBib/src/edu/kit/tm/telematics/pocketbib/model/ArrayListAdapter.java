package edu.kit.tm.telematics.pocketbib.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import edu.kit.tm.telematics.pocketbib.model.library.Item;

import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.widget.BaseAdapter;

/**
 * A basic adapter class which uses a {@code List} as underlying data structure
 * and provides an Iterator for it.
 * 
 * @param <T>
 *            the data type
 */
public abstract class ArrayListAdapter<T> extends BaseAdapter implements Iterable<T> {

	/** the underlying list of items */
	protected List<T> items;
	
	protected List<T> originalOrder;
	
	private SparseBooleanArray itemSelected;

	protected Context context;

	/**
	 * Creates a new ArrayListAdapter.
	 * 
	 * @param items
	 *            the list of items
	 */
	public ArrayListAdapter(Context context, List<T> items) {
		if (context == null || items == null)
			throw new NullPointerException("Parameters can't be null.");

		this.context = context;
		this.items = new ArrayList<T>();
		this.originalOrder = new ArrayList<T>();
		
		for (int i = 0; i < items.size(); i++) {
			if(items.get(i) == null) {
				Log.e("ArrayListAdapter", "items[" + i + "] = null -> skipping");
			} else {
				this.items.add(items.get(i));
				this.originalOrder.add(items.get(i));
			}
		}
		
		this.itemSelected = new SparseBooleanArray(items.size());
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public T getItem(int position) {
		return items.get(position);
	}

	@Override
	public Iterator<T> iterator() {
		return new ReadOnlyIterator(items.iterator());
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ArrayListAdapter) {
			ArrayListAdapter<?> adapter = (ArrayListAdapter<?>) o;
			return adapter.items.equals(this.items);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return items.hashCode();
	}

	@Override
	public String toString() {
		return "ArrayListAdapter [count=" + getCount() + " list=" + items + "]";
	}

	/**
	 * Sorts the adapter's items with the given comparator.
	 * 
	 * @param comparator
	 *            the comparator
	 */
	public void sort(Comparator<T> comparator) {
		if(comparator == ItemRelevanceComparator.DESC || comparator == UserRelevanceComparator.DESC) {
			items = new ArrayList<T>(originalOrder);
		} else if(comparator == ItemRelevanceComparator.ASC || comparator == UserRelevanceComparator.ASC) {
			items = new ArrayList<T>(originalOrder);
			Collections.reverse(items);
		} else {
			Collections.sort(items, comparator);
		}
		
		notifyDataSetChanged();
	}

	/**
	 * A proxy iterator, which blocks {@code remove()} calls and forwards all
	 * other calls to an underlying iterator.
	 */
	public class ReadOnlyIterator implements Iterator<T> {

		/** the underlying real iterator */
		private Iterator<T> realIterator;

		public ReadOnlyIterator(Iterator<T> realIterator) {
			this.realIterator = realIterator;
		}

		public boolean hasNext() {
			return realIterator.hasNext();
		}

		public T next() {
			return realIterator.next();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	/**
	 * sets the actual position selected if deselected before sets the actual
	 * position deselected if seleteced before
	 * 
	 * @param position
	 *            position
	 */
	public void setSelected(int position) {
		if (itemSelected.get(position))
			itemSelected.put(position, false);
		else
			itemSelected.put(position, true);
		notifyDataSetChanged();

		// Loggingmessage
		String log = "";
		for (int i = 0; i < items.size(); i++) {
			log += i + "-" + checkSelected(i) + " ";
		}
		Log.i("WelcomeActivityTest", log + "itemsSelected size() = " + itemSelected.size());

	}

	/**
	 * Returns whether an item is selected or not
	 * 
	 * @param position
	 *            position of the item
	 * @return selectionstatus
	 */
	public boolean checkSelected(int position) {
		return itemSelected.get(position);
	}

	/**
	 * Returns a list of the selection status of all items in the list
	 * 
	 * @return ArrayList with boolean status of selection
	 */
	public ArrayList<T> getSelected() {

		ArrayList<T> selectedItems = new ArrayList<T>();
		for (int i = 0; i < items.size(); i++) {
			if (checkSelected(i)) {
				selectedItems.add(getItem(i));
			}
		}
		return selectedItems;
	}

	/**
	 * checks if a item of the list is selected
	 * 
	 * @return true if items selected
	 */
	public boolean itemSelected() {
		for (int i = 0; i < items.size(); i++) {
			if (checkSelected(i)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * wipes the list of selected items
	 */
	public void wipeSelectedItems() {
		this.itemSelected = new SparseBooleanArray(items.size());
	}
}
