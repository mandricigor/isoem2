package edu.uconn.engr.dna.util;


import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: marius
 * Date: Jun 19, 2010
 * Time: 8:49:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReadClassCompactAndClusterParameterRunnable<T> implements ParameterRunnable<Iterable<T>, List<List<T>>> {

	private Map<Object, Node> ids;
	private BreadCrumb bc;
	private Converter<T, Iterator<Object>> iteratorFactory;
	private final BinaryOperator<T, T, Void> conflictResolver; // called as compute(objectToBeRemoved, objectToBeKept)
																// should transfer to objectToBeKept any data needed from objectToBeRemoved

	public ReadClassCompactAndClusterParameterRunnable(Converter<T, Iterator<Object>> iteratorFactory,
			BinaryOperator<T, T, Void> conflictResolver) {
		ids = new HashMap<Object, Node>();
		bc = new BreadCrumb();
		this.iteratorFactory = iteratorFactory;
		this.conflictResolver = conflictResolver;
	}

	@Override
	public void run(Iterable<T> isoformLists) {
		performUnions(isoformLists);
	}

	public List<T> collectAllReadClasses() {
		List<T> result = new ArrayList<T>();
		for (Node node : ids.values()) {
			Map<T, T> rc = node.getReadClasses();
			if (rc != null) {
				result.addAll(rc.values());
			}
		}
		return result;
	}

	@Override
	public List<List<T>> done() {
		for (Node node : ids.values()) {
			collapseReadClassesToRoot(node);
		}
		List<List<T>> result = getRootClusters();
		ids = new HashMap<Object, Node>();
		bc = new BreadCrumb();
		return result;
	}

	private void performUnions(Iterable<T> isoformLists) {
		Node[] lca = new Node[1];
		for (T isoformList : isoformLists) {
			Iterator<Object> iterator = iteratorFactory.convert(isoformList);
			if (!iterator.hasNext()) {
				continue;
			}
			performUnion(iterator, lca);
			addTToNode(lca[0], isoformList);
		}
	}

	private Node collapseReadClassesToRoot(Node node) {
		if (node.isRoot()) {
			if (node.hasMap()) {
				List<T> vals = new ArrayList<T>(node.getReadClasses().values());
				node.setReadClassesCollection(vals);
			}
			return node;
		}
		Node r = collapseReadClassesToRoot(node.getParent());
		Collection<T> rootClasses = r.getReadClassesList();
		Map<T, T> rc = node.getReadClasses();
		if (rc != null) {
			Collection<T> vals = rc.values();
			rootClasses.addAll(vals);
			node.setReadClasses(null);
		}
		// compress path
		node.setParent(r);
		return r;
	}

//	private int count(Collection<T> vals) {
//		int s = 0;
//		for (T i : vals) {
//			s += i.getMultiplicity();
//		}
//		return s;
//	}

	private List<List<T>> getRootClusters() {
		List<List<T>> rcClusters = new ArrayList<List<T>>();
		for (Node node : ids.values()) {
			if (node.isRoot()) {
				rcClusters.add(node.getReadClassesList()/* not null */);
			}
		}
		return rcClusters;
	}

	private boolean performUnion(Iterator<Object> iterator, Node[] output) {
		Node index1 = getNode(iterator.next());
		if (!iterator.hasNext()) {
			// unique reads: report LCA
			output[0] = index1;
			return false;
		}

		index1 = bc.putBreadCrumbsUpToRoot(index1);
		int largestBreadCrumb = 0;
		Node lca = index1;
		boolean performedUnion = false;
		do {
			Node index2 = getNode(iterator.next());
			if (performedUnion) {
				// performed at least one union before
				// so we no longer care about the lca
				index2 = findRoot(index2);
				if (index2 != index1) {
					index1 = union(index1, index2);
				}
				continue;
			}

			int breadCrumb = bc.findFirstAncestorWithBreadCrumb(index2, output);
			if (breadCrumb == BreadCrumb.InternalNodeOnSecondaryPath) {
				continue;
			}

			if (breadCrumb == BreadCrumb.DifferentTree) {
				index1 = union(index1, output[0]);
				performedUnion = true;
			} else if (breadCrumb > largestBreadCrumb) {
				// found the LCA of the current nodes
				lca = output[0];
				largestBreadCrumb = breadCrumb;
			}
		} while (iterator.hasNext());

		if (bc.getNextBreadCrumb() > 2000000000) // unlikely: look for exceeding integer
		{
			bc.cleanAll(ids.values());
		}

		if (performedUnion) {
			output[0] = index1;
		} else {
			output[0] = lca;
		}
		return performedUnion;
	}

	private Node getNode(Object i) {
		Node id = ids.get(i);
		if (id == null) {
			ids.put(i, id = new Node());
		}
		return id;
	}

	private Node findRoot(Node k) {
		while (!k.isRoot()) {
			k = k.getParent();
		}
		return k;
	}

	private Node union(Node root1, Node root2) {
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

	private void addTToNode(Node node, T isoList) {
		Map<T, T> oldList = node.getReadClasses();
		if (oldList == null) {
			node.setReadClasses(oldList = new HashMap<T, T>());
		}
		T existing = oldList.put(isoList, isoList);
		if (existing != null) {
			conflictResolver.compute(existing, isoList);
		}
	}

	static class Node {

		private Node parent;
		private Object readClasses;
		private int subTreeHeight;
		private int breadCrumb;

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

		boolean hasMap() {
			return readClasses instanceof Map;
		}

		public Map getReadClasses() {
			return (Map) readClasses;
		}

		public void setReadClasses(Map readClasses) {
			this.readClasses = readClasses;
		}

		int getBreadCrumb() {
			return breadCrumb;
		}

		void setBreadCrumb(int bc) {
			this.breadCrumb = bc;
		}

		public void setReadClassesCollection(List isoformLists) {
			this.readClasses = isoformLists;
		}

		public List getReadClassesList() {
			return (List) readClasses;
		}
	}

	/**
	 * Manages the tagging of nodes during LCA search
	 */
	static class BreadCrumb {

		public static final int InternalNodeOnSecondaryPath = 1;
		public static final int DifferentTree = 2;
		private int firstCleanBreadCrumb;
		private int secondaryBreadCrumb;

		public BreadCrumb() {
			firstCleanBreadCrumb = 3;
		}

		public Node putBreadCrumbsUpToRoot(Node node) {
			secondaryBreadCrumb = firstCleanBreadCrumb++;
			while (!node.isRoot()) {
				node.setBreadCrumb(firstCleanBreadCrumb++);
				node = node.getParent();
			}
			node.setBreadCrumb(firstCleanBreadCrumb++);
			return node;
		}

		public int findFirstAncestorWithBreadCrumb(Node node, Node[] output) {
			do {
				int bc = node.getBreadCrumb();
				if (bc == secondaryBreadCrumb) {
					return InternalNodeOnSecondaryPath;
				}

				if (bc > secondaryBreadCrumb) {
					output[0] = node;
					return bc;
				}

				if (node.isRoot()) {
					// reached the root of a different tree
					output[0] = node;
					return DifferentTree;
				}

				node.setBreadCrumb(secondaryBreadCrumb);
				node = node.getParent();
			} while (true);
		}

		int getNextBreadCrumb() {
			return firstCleanBreadCrumb;
		}

		public void cleanAll(Collection<Node> nodes) {
			for (Node node : nodes) {
				node.setBreadCrumb(0);
			}
			firstCleanBreadCrumb = 3;
		}
	}
}
