package edu.uconn.engr.dna.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: marius
 * Date: Jun 19, 2010
 * Time: 8:49:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class UnionFindClustering<T> implements ParameterRunnable<Iterable<T>, List<List<T>>> {

	private Map<Object, Node> ids;
	private Converter<T, Iterator<Object>> iteratorFactory;

	public UnionFindClustering(Converter<T, Iterator<Object>> iteratorFactory) {
		ids = new HashMap<Object, Node>();
		this.iteratorFactory = iteratorFactory;
	}

	@Override
	public void run(Iterable<T> iterableObjects) {
		performUnions(iterableObjects);
	}

	@Override
	public List<List<T>> done() {
		for (Node node : ids.values()) {
			collapseReadClassesToRoot(node);
		}
		List<List<T>> result = getRootClusters();
		ids = new HashMap<Object, Node>();
		return result;
	}

	private void performUnions(Iterable<T> iterableObjects) {
		for (T iterable : iterableObjects) {
//			System.out.println(iterable);
			Iterator<Object> iterator = iteratorFactory.convert(iterable);
			if (!iterator.hasNext()) {
				continue;
			}
			Node root = performUnion(iterator);
			addIterableToNode(root, iterable);
		}
	}

	private Node collapseReadClassesToRoot(Node node) {
		if (node.isRoot()) {
			return node;
		}
		Node r = collapseReadClassesToRoot(node.getParent());
		Collection<T> rootClasses = r.getClassesList();
		List<T> rc = node.getClassesList();
		if (rc != null) {
			rootClasses.addAll(rc);
			node.setClassCollection(null);
		}
		// compress path
		node.setParent(r);
		return r;
	}

	private List<List<T>> getRootClusters() {
		List<List<T>> rcClusters = new ArrayList<List<T>>();
		for (Node node : ids.values()) {
			if (node.isRoot()) {
				rcClusters.add(node.getClassesList()/* not null */);
			}
		}
		return rcClusters;
	}

	private Node performUnion(Iterator<Object> iterator) {
		Node index1 = getNode(iterator.next());
		if (!iterator.hasNext()) {
			return index1;
		}

		index1 = findRoot(index1);
		do {
			Node index2 = getNode(iterator.next());
			index2 = findRoot(index2);
			if (index2 != index1) {
				index1 = union(index1, index2);
			}
		} while (iterator.hasNext());
		return index1;
	}

	private Node getNode(Object i) {
		Node id = ids.get(i);
		if (id == null) {
			ids.put(i, id = new Node());
		}
		return id;
	}

	private Node findRoot(Node k) {
		if (k.isRoot()) {
			return k;
		} else {
			Node r = findRoot(k.getParent());
			k.setParent(r); // path compression
			return r;
		}
//        while (!k.isRoot())
//            k = k.getParent();
//        return k;
	}

	private Node union(Node root1, Node root2) {
		if (root1 == root2) {
			throw new IllegalStateException("Union of identical nodes!");
		}
		if (root1.getSubTreeHeight() < root2.getSubTreeHeight()) {
			Node tmp = root1;
			root1 = root2;
			root2 = tmp;
		}

		root2.setParent(root1);
		if (root1.getSubTreeHeight() == root2.getSubTreeHeight()) {
			root1.setSubTreeHeight(1 + root1.getSubTreeHeight());
		}

		return root1;
	}

	private void addIterableToNode(Node node, T isoList) {
		List<T> oldList = node.getClassesList();
		if (oldList == null) {
			node.setClassCollection(oldList = new ArrayList<T>());
		}
		oldList.add(isoList);
	}

	class Node {

		private Node parent;
		private Object classes;
		private int subTreeHeight;

		public Node() {
			subTreeHeight = 1;
		}

		public int getSubTreeHeight() {
			return subTreeHeight;
		}

		public void setSubTreeHeight(int subTreeSize) {
			this.subTreeHeight = subTreeSize;
		}

		public Node getParent() {
			return parent;
		}

		public void setParent(Node parent) {
			this.parent = parent;
		}

		public boolean isRoot() {
			return parent == null;
		}

		public void setClassCollection(List<T> list) {
			this.classes = list;
		}

		public List<T> getClassesList() {
			return (List) classes;
		}
	}
}
