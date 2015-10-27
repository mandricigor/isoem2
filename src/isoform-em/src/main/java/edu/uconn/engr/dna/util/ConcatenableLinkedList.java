package edu.uconn.engr.dna.util;

import java.util.AbstractSequentialList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Should be made to implement the List interface soon
 * 
 * @author marius
 *
 * @param <T>
 */
public class ConcatenableLinkedList<T> extends AbstractSequentialList<T>
implements Iterable<T>, List<T> {
	private Node<T> head, tail;
	private int size;
	
	public ConcatenableLinkedList() {
	}

	public ConcatenableLinkedList(T singleItem) {
		head = tail = new Node<T>(singleItem);
		size = 1;
	}

	public boolean add(T item) {
		if (head == null) {
			head = tail = new Node<T>(item);
			size = 1;
		} else {
			tail.next = new Node<T>(item);
			tail = tail.next;
			++size;
		}
		return true;
	}
	
	public void addAll(ConcatenableLinkedList<T> other) {
		if (head == null) {
			head = other.head;
			tail = other.tail;
			size = other.size;
		} else {
			tail.next = other.head;
			tail = other.tail;
			size += other.size;
		}
	}
	
	@Override
	public Iterator<T> iterator() {
		return new NodeIterator();
	}
	
	public static class Node<E> {
		private final E info;
		Node<E> next;
		
		public Node(E info) {
			this.info = info;
		}
		
		public E getInfo() {
			return info;
		}

		public Node<E> getNext() {
			return next;
		}
	}
	
	class NodeIterator implements Iterator<T> {

		private Node<T> poz;
		private boolean done;

		public NodeIterator() {
			poz = head;
			done = (head == null);
			if (poz == null) {
				poz = null;
			}
		}

		@Override
		public boolean hasNext() {
			return !done;
		}

		@Override
		public T next() {
			T info = poz.info;
			if (poz == tail) {
				done = true;
			} else {
				poz = poz.next;
			}
			return info;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}

	public void clear() {
		head = tail = null;
	}

	public boolean isEmpty() {
		return head == null;
	}

	public boolean hasOneElement() {
		return head == tail && !isEmpty();
	}

	public T getFirst() {
		return head.info;
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return size;
	}

	public Node<T> getHead() {
		return head;
	}

	public Node<T> getTail() {
		return head;
	}

}
