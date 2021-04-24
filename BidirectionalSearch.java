import java.util.*;

/**
 * A Bi-Directional Uniform Cost Search executed by agents. The following program takes two inputs
 * as Start City and Destination City and the first agent starts from the Start City and implements 
 * Uniform Cost Search to reach the destination whereas the second agent starts from the Destination
 * City and implements Uniform Cost Search to work its way back to the start city
 * @author Sk Nasimul Alahi
 * Date: 02/12/2021
 */

public class BidirectionalSearch {
	public static PriorityQueue<Node> queueA = 
			new PriorityQueue<Node>(20, new Comparator<Node>() {
		//Override compare method
		public int compare(Node i, Node j) {
			if(i.pathCost > j.pathCost) { return 1; }
			else if (i.pathCost < j.pathCost) { return -1; }
			else { return 0; }
		}
	});
	
	public static PriorityQueue<Node> queueB = 
			new PriorityQueue<Node>(20, new Comparator<Node>() {
		//Override compare method
		public int compare(Node i, Node j) {
			if(i.pathCost > j.pathCost) { return 1; }
			else if (i.pathCost < j.pathCost) { return -1; }
			else { return 0; }
		}
	});
	
	/**
	 * SearchAgent1 is responsible for finding low cost path from Start City to
	 * Destination City
	 * @author Sk Nasimul Alahi
	 */
	public static class SearchAgent1 extends Thread {
		/**
		 * Finds low cost path from source to goal
		 * @param source Start City
		 * @param goal Destination City
		 */
		public void UniformCostSearch (Node source, Node goal) {
			source.pathCost = 0;
			queueA.add(source);
			Set<Node> explored = new HashSet<Node>();
			boolean found = false;
			//While frontier is not empty
			do{
				Node current = queueA.poll();
				explored.add(current);
				//End if path is found
				if(current.value.equals(goal.value)){
					found = true;
				} else if (queueB.peek() != null && current.value.equals(queueB.peek().value)) {
					found = true;
				}
				for(Edge e: current.adjacencies){
					Node child = e.target;
					int cost = e.cost;
					//Add node to queue if node has not been explored
					if(!explored.contains(child) && !queueA.contains(child)) {
						child.pathCost = current.pathCost + cost;
						child.parent = current;
						queueA.add(child);		
					}
					//Current path is shorter than previous path found
					else if((queueA.contains(child))&&(child.pathCost>(current.pathCost+cost))) {
						child.parent=current;
						child.pathCost = current.pathCost + cost;
						queueA.remove(child);
						queueA.add(child);
					}
				}			
			} while (!queueA.isEmpty()&& (found==false));
		}
		
		/**
		 * Prints path to target
		 * @param target Destination City
		 * @return List path
		 */
		public List<Node> printPath(Node target) {
			List<Node> path = new LinkedList<Node>();
			for(Node node = target; node!=null; node = node.parent) {
				path.add(node);
				if (path.size() >= 3 && path.get(path.size()-1).equals(path.get(path.size()-3))) {
					path.remove(path.size()-1);
					break;
				}
			}
			Collections.reverse(path);
			return path;
		}
		
		/**
		 * Calculates path cost to target
		 * @param target Destination City
		 * @return int cost
		 */
		public int pathCost(Node target) {
			List<Node> path = printPath(target);
			int cost = path.get(path.size()-1).pathCost;
			return cost;
		}
	}
	
	/**
	 * SearchAgent2 is responsible for finding low cost path from Destination City to
	 * Start City
	 * @author Sk Nasimul Alahi
	 */
	public static class SearchAgent2 extends Thread {
		/**
		 * Finds low cost path from source to goal
		 * @param source Destination City
		 * @param goal Start City
		 */
		public void UniformCostSearch (Node source, Node goal) {
			source.pathCost = 0;
			queueB.add(source);
			Set<Node> explored = new HashSet<Node>();
			boolean found = false;
			//While frontier is not empty
			do{
				Node current = queueB.poll();
				explored.add(current);
				//End if path is found
				if(current.value.equals(goal.value)){
					found = true;
				} else if (queueA.peek() != null && current.value.equals(queueA.peek().value)) {
					found = true;
				}
				for(Edge e: current.adjacencies){
					Node child = e.target;
					int cost = e.cost;
					//Add node to queue if node has not been explored
					if(!explored.contains(child) && !queueB.contains(child)){
						child.pathCost = current.pathCost + cost;
						child.parent = current;
						queueB.add(child);		
					}
					//Current path is shorter than previous path found
					else if((queueB.contains(child))&&(child.pathCost>(current.pathCost+cost))){
						child.parent=current;
						child.pathCost = current.pathCost + cost;
						queueB.remove(child);
						queueB.add(child);
					}
				}			
			} while (!queueB.isEmpty()&& (found==false));
		}
		
		/**
		 * Prints path to target
		 * @param target Start City
		 * @return List path
		 */
		public List<Node> printPath(Node target) {
			List<Node> path = new LinkedList<Node>();
			for(Node node = target; node!=null; node = node.parent) {
				path.add(node);
				if (path.size() >= 3 && path.get(path.size()-1).equals(path.get(path.size()-3))) {
					path.remove(path.size()-1);
					break;
				}
			}
			return path;
		}
		
		/**
		 * Calculates path cost to target
		 * @param target Start City
		 * @return int cost
		 */
		public int pathCost(Node target) {
			List<Node> path = printPath(target);
			int cost = path.get(0).pathCost;
			return cost;
		}
	}
	
	//.................................................//
					//Helper Classes//
	//.................................................//
	public static class Node {
		public final String value;
		public int pathCost;
		public Edge[] adjacencies;
		public Node parent;
		public Node (String val) { value = val; }
		//Override
		public String toString(){
			return value;
		}
	}
	
	public static class Edge {
		public final int cost;
		public final Node target;
		public Edge (Node targetNode, int costVal) {
			cost = costVal;
			target = targetNode;
		}
	}
	
	public static void main (String[] args) {
		Node n1 = new Node ("Arad");
		Node n2 = new Node ("Zerind");
		Node n3 = new Node ("Oradea");
		Node n4 = new Node ("Sibiu");
		Node n5 = new Node ("Fagaras");
		Node n6 = new Node ("Rimnicu Vilcea");
		Node n7 = new Node ("Pitesti");
		Node n8 = new Node ("Timisoara");
		Node n9 = new Node ("Lugoj");
		Node n10 = new Node ("Mehadia");
		Node n11 = new Node ("Dobreta");
		Node n12 = new Node ("Craiova");
		Node n13 = new Node ("Bucharest");
		Node n14 = new Node ("Giurgiu");
		Node n15 = new Node ("Urziceni");
		Node n16 = new Node ("Hirsova");
		Node n17 = new Node ("Eforie");
		Node n18 = new Node ("Vaslui");
		Node n19 = new Node ("Iasi");
		Node n20 = new Node ("Neamt");
		
		List<Node> nodes = new ArrayList<Node>();
		nodes.add(n1); nodes.add(n2); nodes.add(n3); nodes.add(n4); nodes.add(n5); nodes.add(n6);
		nodes.add(n7); nodes.add(n8); nodes.add(n9); nodes.add(n10); nodes.add(n11); nodes.add(n12);
		nodes.add(n13); nodes.add(n14); nodes.add(n15); nodes.add(n16); nodes.add(n17); nodes.add(n18);
		nodes.add(n19); nodes.add(n20);
		
		//Initialize the edges
		//Arad
		n1.adjacencies = new Edge[] { new Edge(n2,75), new Edge(n4,140), new Edge(n8,118) };
 		//Zerind
		n2.adjacencies = new Edge[] { new Edge(n1,75), new Edge(n3,71) };
 		//Oradea
		n3.adjacencies = new Edge[] { new Edge(n2,71), new Edge(n4,151) };
 		//Sibiu
		n4.adjacencies = new Edge[] { new Edge(n1,140), new Edge(n5,99), new Edge(n3,151), 
						 new Edge(n6,80) };
 		//Fagaras
		n5.adjacencies = new Edge[] { new Edge(n4,99), new Edge(n13,211) };
 		//Rimnicu Vilcea
		n6.adjacencies = new Edge[] { new Edge(n4,80), new Edge(n7,97), new Edge(n12,146) };
 		//Pitesti
		n7.adjacencies = new Edge[] { new Edge(n6,97), new Edge(n13,101), new Edge(n12,138) };
 		//Timisoara
		n8.adjacencies = new Edge[] { new Edge(n1,118), new Edge(n9,111) };
 		//Lugoj
		n9.adjacencies = new Edge[] { new Edge(n8,111), new Edge(n10,70) };
 		//Mehadia
		n10.adjacencies = new Edge[] { new Edge(n9,70), new Edge(n11,75) };
 		//Dobreta
		n11.adjacencies = new Edge[] { new Edge(n10,75), new Edge(n12,120) };
 		//Craiova
		n12.adjacencies = new Edge[] { new Edge(n11,120), new Edge(n6,146), new Edge(n7,138) };
		//Bucharest
		n13.adjacencies = new Edge[] { new Edge(n7,101), new Edge(n14,90), new Edge(n5,211),
						  new Edge(n15, 85) };
 		//Giurgiu
		n14.adjacencies = new Edge[] { new Edge(n13,90) };
		//Urziceni
		n15.adjacencies = new Edge[] { new Edge(n13,85), new Edge(n16,98), new Edge(n18,142) };
		//Hirsova
		n16.adjacencies = new Edge[] { new Edge(n15,98), new Edge(n17,86) };
		//Eforie
		n17.adjacencies = new Edge[] { new Edge(n16,86) };
		//Vaslui
		n18.adjacencies = new Edge[] { new Edge(n15,142), new Edge(n19,92) };
		//Iasi
		n19.adjacencies = new Edge[] { new Edge(n18,92), new Edge(n20,87) };
		//Neamt
		n20.adjacencies = new Edge[] { new Edge(n19,87) };
		
		System.out.println("Welcome to Bi-Directional Uniform Cost Search Using Agents!!!");
		System.out.println("Please provide a single city name in each line");
		var console = System.console();
		Node startCity = null;
		Node destCity = null;
		String command1 = console.readLine("Start City: ");
		for (int i = 0; i < nodes.size(); i++) {
			if (command1.equals(nodes.get(i).value)) {
				startCity = nodes.get(i);
			}
		}
		String command2 = console.readLine("Destination City: ");
		for (int i = 0; i < nodes.size(); i++) {
			if (command2.equals(nodes.get(i).value)) {
				destCity = nodes.get(i);
			}
		}
		if (startCity == null || destCity == null) {
			throw new IllegalArgumentException("CITY NOT FOUND!!!");
		}
		
		SearchAgent1 A = new SearchAgent1();
		SearchAgent2 B = new SearchAgent2();
		A.start();
		B.start();
		B.UniformCostSearch(destCity,startCity);
		if (B.printPath(startCity).get(B.printPath(startCity).size()-1).equals(destCity)) {
			System.out.println("The path from Start City to Destination City is: " + 
								B.printPath(startCity));
			System.out.println("The total mileage from Start City to Destination City is: " + 
								B.pathCost(startCity) + " KM");
		} else {
			A.UniformCostSearch(startCity,destCity);
			System.out.println("The path from Start City to Destination City is: " + 
								A.printPath(destCity));
			System.out.println("The total mileage from Start City to Destination City is: " + 
								A.pathCost(destCity) + " KM");
		}	
	}	
}