package diver;

import datastructures.PQueue;
import datastructures.SlowPQueue;
import game.*;
import graph.ShortestPaths;

import java.util.*;




/** This is the place for your implementation of the {@code SewerDiver}.
 */
public class McDiver implements SewerDiver {


    /**
     * See {@code SewerDriver} for specification.
     */
    @Override
    public void seek(SeekState state) {
        HashSet<Long> visitedStates = new HashSet<>();
        depthFirstSearch(state, visitedStates);
    }

    /**
     * Executes a depth-first search (DFS) algorithm to find a specific item, referred to as 'the ring'.
     * If the ring is found, it updates the location of McDiver to the ring's location.
     *
     * Depth-first search is a recursive algorithm for traversing or searching tree or graph data structures.
     * This implementation specifically focuses on finding the shortest path to the ring. Once found,
     * it ceases further exploration and updates McDiver's current location to that of the ring.
     *
     * @param currentState The current state of the search. This includes information about McDiver's
     *                     current location and the distance to the ring. The state is updated as the
     *                     search progresses. The state's neighbors() method is used to get adjacent nodes
     *                     in the search space, and its moveTo() method is used to change McDiver's location.
     *
     * @param visitedStates A set of already visited state identifiers (Long). This set is used to avoid
     *                      revisiting states, which is crucial for optimizing the search and preventing
     *                      infinite loops. Each state's identifier should uniquely represent its location
     *                      in the search space.
     */

    private void depthFirstSearch(SeekState currentState, HashSet<Long> visitedStates) {
        long placeholder = currentState.currentLocation();
        if (currentState.distanceToRing() == 0) {
            return;
        }
        visitedStates.add(currentState.currentLocation());

        for (NodeStatus neighbor : currentState.neighbors()) {
            if (!visitedStates.contains(neighbor.getId())) {
                currentState.moveTo(neighbor.getId());
                depthFirstSearch(currentState, visitedStates);
                if (currentState.distanceToRing() == 0) {
                    return;
                }
                currentState.moveTo(placeholder);
            }
        }

    }


    /**
     * See {@code SewerDriver} for specification.
     */
    @Override
    public void scram(ScramState state) {
        coinThief(state);
    }




    /**
     * An optimized algorithm designed to guide McDiver through a maze-like structure to an exit,
     * prioritizing the collection of coins based on a negative coin value-to-path distance ratio.
     * This method strategically navigates McDiver, accumulating coins while ensuring a path to the exit is feasible.
     *
     * The algorithm uses Dijkistra's shortest path strategy, considering both the distance to collect coins and the
     * distance to the exit. It assigns priorities to nodes (locations in the maze) based on the coin value and
     * the distance to reach them. Nodes with higher coin values and shorter distances are given higher priority.
     * The method aims to maximize coin collection while ensuring McDiver can reach the exit within a given number
     * of steps.
     *
     * @param state The current state of McDiver in the maze. This state includes information about all nodes in the maze,
     *              McDiver's current location, the exit location, and the number of steps remaining to reach the exit.
     */
    private void coinThief(ScramState state) {
    ShortestPaths<Node, Edge> scramPath = new ShortestPaths<>(new Maze((Set<Node>) state.allNodes()));
    scramPath.singleSourceDistances(state.currentNode());
    ShortestPaths<Node, Edge> exitPath = new ShortestPaths<>(new Maze((Set<Node>) state.allNodes()));
    PQueue<Node> weightedCoinNodes = new SlowPQueue<>();
    Collection<Node> allNodes = state.allNodes();
    Collection<Node> visitedNodes = new HashSet<>();


    for (Node n : allNodes) {
        List<Edge> path = scramPath.bestPath(n);
        int pathDistance = calculatePathDistance(path);
        int coins = n.getTile().coins();
        int coinVal = n.getTile().originalCoinValue();


        if (coins <= 0) {
            continue;
        }

        // Base priority calculation
        int priority = 1000;

        // Adjust priority based on coin value
        if (coinVal > 400) {
            priority = -(coinVal / pathDistance);
        } else if (coinVal > 200) {
            priority = -(coinVal / pathDistance);
        } else if (coinVal > 10) {
            priority = -(coinVal / pathDistance);
        }
        weightedCoinNodes.add(n, priority);

    }
    while (!weightedCoinNodes.isEmpty()
            && !state.currentNode().equals(state.exit())) {
        Node destination = weightedCoinNodes.extractMin();
        List<Edge> pathToDestination = scramPath.bestPath(destination);
         for (Edge edge : pathToDestination) {
            Node nextNode = edge.destination();
                if (state.currentNode().getNeighbors().contains(nextNode)
                        && calculateRoundTrip(exitPath, scramPath,nextNode, state.exit(), state.currentNode(), nextNode)
                        < state.stepsToGo()) {
                    
                    state.moveTo(nextNode);
                    visitedNodes.add(nextNode);
                    updatePriority(state, weightedCoinNodes, scramPath, visitedNodes);


                }else {
                    scramPath.singleSourceDistances(state.currentNode());
                    List<Edge> shortcut = scramPath.bestPath(state.exit());
                    for (Edge path : shortcut) {
                        Node neighborNode = path.destination();
                        if (state.currentNode().getNeighbors().contains(neighborNode)) {
                            state.moveTo(neighborNode);
                        }
                    }
                }
            }
        }



    scramPath.singleSourceDistances(state.currentNode());
    List<Edge> pathToExit = scramPath.bestPath(state.exit());
    for (Edge path : pathToExit) {
        state.moveTo(path.destination());
    }
}

    /**
     * Returns length of a path
     * @param path
     * @return
     */
    private int calculatePathDistance(List<Edge> path){
        int totalDistance = 0;
        for(Edge way : path){
            totalDistance += way.length;
        }
        return totalDistance;
    }

    /**
     * Returns distance between nodes
     * @param exitPath
     * @param startNode
     * @param exitNode
     * @return
     */
    private int calculateDistanceBetweenNodes(ShortestPaths<Node, Edge> exitPath, Node startNode, Node exitNode) {
        exitPath.singleSourceDistances(startNode);
        List<Edge> pathToExit = exitPath.bestPath(exitNode);
        return calculatePathDistance(pathToExit);
    }

    /**
     * Returns the total distance between current node to target node summed with distance between target node to exit
     * @param exitPath
     * @param scramPath
     * @param startNode
     * @param exitNode
     * @param currentNode
     * @param destination
     * @return int
     */
    private int calculateRoundTrip(ShortestPaths<Node, Edge> exitPath, ShortestPaths<Node, Edge> scramPath,
                                   Node startNode, Node exitNode, Node currentNode, Node destination){
        return calculateDistanceBetweenNodes(exitPath, startNode, exitNode) +
                calculateDistanceBetweenNodes(scramPath, currentNode, destination );
    }

    /**
     * Changes the priority of a node in the given PQueue with a negative coinVal to pathDistance ratio
     * @param state
     * @param weightedCoinNodes
     * @param scramPath
     * @param visitedNodes
     */
    private void updatePriority(ScramState state, PQueue<Node> weightedCoinNodes, ShortestPaths<Node, Edge> scramPath,
                                Collection<Node> visitedNodes){
        scramPath.singleSourceDistances(state.currentNode());
        Collection<Node> allNodes = state.allNodes();
        for(Node n : allNodes){
            if(!visitedNodes.contains(n)){
                List<Edge> path = scramPath.bestPath(n);
                int pathDistance = calculatePathDistance(path);
                int coins = n.getTile().coins();
                int coinVal = n.getTile().originalCoinValue();

                if (coins <= 0) {
                    continue;
                }

                int priority = 1000;

                // Adjust priority based on coin value
                if (coinVal > 450) {
                    priority = -((coinVal / pathDistance)*8);
                } else if (coinVal > 100) {
                    priority = -(coinVal / pathDistance);
                } else if (coinVal > 10) {
                    priority = -(coinVal / pathDistance);
                }
                try{
                    weightedCoinNodes.add(n, priority);

                }catch (IllegalArgumentException e) {
                    weightedCoinNodes.changePriority(n, priority);
                }
            }
        }

    }

}