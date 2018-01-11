package antimonypidgey.untangle;

import java.util.ArrayList;
import java.util.Random;

/**
 * Primary game class. Contains most mechanic related code.
 *
 * Variables:
 * int xSize - Horizontal size of the field in pixels.
 * int ySize - Vertical size of the field in pixels.
 * ArrayList<Node> nodes - An arraylist containing references to the nodes on the game board.
 * ArrayList<Node[]> connections - An arraylist containing pairs of nodes representing connections.
 *
 */
public class Game {

    public int xSize;
    public int ySize;
    public int nodeRadius;
    int nodeCount;
    int connectionPasses;
    Random randomGen;
    public ArrayList<Node> nodes;
    public ArrayList<Node[]> connections;
    public TimerContainer timer;


    /**
     * Constructor:
     * int xSize/ySize - The x/y dimensions of the game board.
     * int nodeCount - The number of nodes on the board.
     * int maxConnections - Maximum generated connections per Node.
     * int seed - A seed for the level generator's RNG.
     * int nodeRadius - the radius in pixels of each node.
     */
    public Game(int _xSize, int _ySize, int _nodeCount, int _connectionPasses, int seed, int _nodeRadius){

        nodes = new ArrayList<Node>();
        connections = new ArrayList<Node[]>();
        xSize = _xSize;
        ySize = _ySize;
        randomGen = new Random(seed);
        nodeRadius = _nodeRadius;
        nodeCount = _nodeCount;
        connectionPasses = _connectionPasses;
        populateNodes();
        connectNodes(connectionPasses);
        timer = new TimerContainer();

    }

    // Add nodes to the node list until nodeCount is reached.
    private void populateNodes(){
        int iterations = 0;
        boolean passed;
        // If there is no room to place any more nodes, the placer will try 3 * nodeCount
        // times to place nodes and then will stop.
        while (nodes.size()<nodeCount && iterations < 3*nodeCount){
            // Generate a random x and y position between 0+nodeRadius and size-nodeRadius
            int tempX = randomGen.nextInt(xSize-(2*nodeRadius))+nodeRadius;
            int tempY = randomGen.nextInt(ySize-(2*nodeRadius))+nodeRadius;

            // Test to ensure the node does not intersect with another node.
            // If the generated co-ordinates do intersect, begin the loop again.
            if (nodes.size()!=0) {
                passed = true;
                for (int i = 0; i < nodes.size(); i++) {
                    if ((!((tempX >= nodes.get(i).x() - (nodeRadius * 2)
                            && tempX <= nodes.get(i).x() + (nodeRadius * 2))
                            && (tempY >= nodes.get(i).y() - (nodeRadius * 2))
                            && tempY <= nodes.get(i).y() + (nodeRadius * 2)))) {
                    }
                    else{
                        passed = false;
                    }
                }
                if (passed){
                    nodes.add(new Node(tempX, tempY, nodeRadius));
                }
            }
            else{
                nodes.add(new Node(tempX, tempY, nodeRadius));
            }
            iterations++;
        }
    }

    // Generate connections between random nodes
    // connectionPasses - Maximum possible connections on a node.
    private void connectNodesOld(int maxConnections){
        int connectionCount;
        int confirmedConnections;
        int choice;
        for (int i = 0; i < nodes.size(); i++){
            connectionCount = randomGen.nextInt(connectionPasses)+1;
            confirmedConnections = 0;
            while (confirmedConnections<connectionCount){
                choice = randomGen.nextInt(nodes.size());
                if (!connections.contains(new Node[]{nodes.get(i), nodes.get(choice)}) && !connections.contains(new Node[]{nodes.get(choice), nodes.get(i)})) {
                    connections.add(new Node[]{nodes.get(i), nodes.get(choice)});
                    confirmedConnections++;
                }
            }
        }
    }

    // Updated version which SHOULD consistently generate solvable problems.
    private void connectNodes(int maxConnections){
        // Find the midpoint between every node in the group.
        int xTotal = 0;
        int yTotal = 0;
        for (int i = 0; i < nodes.size(); i++){
            xTotal += nodes.get(i).x();
            yTotal += nodes.get(i).y();
        }
        int xAverage = xTotal/nodes.size();
        int yAverage = yTotal/nodes.size();

        // Sort and connect the nodes in a polygon based on vector angle from the midpoint.
        ArrayList<Node> sortedNodes = new ArrayList<>();
        // Initialize each node's midAngle property, which will be used to sort.
        for (int i = 0; i < nodes.size(); i++){
            nodes.get(i).midAngle = Math.atan2(nodes.get(i).x() - xAverage, yAverage - nodes.get(i).y());
        }

        // Sort nodes by midAngle in ascending order.
        while (sortedNodes.size() < nodes.size()){
            double smallestAngle = 999;
            int smallestAngleIndex = -1;
            for (int i = 0; i < nodes.size(); i++){
                if (!sortedNodes.contains(nodes.get(i))){
                    if (nodes.get(i).midAngle<smallestAngle){
                        smallestAngle = nodes.get(i).midAngle;
                        smallestAngleIndex = i;
                    }
                }
            }
            sortedNodes.add(nodes.get(smallestAngleIndex));
        }

        // Connect each node in sortedNodes to its neighbor.
        for(int i = 0; i < sortedNodes.size(); i++){
            if (i == sortedNodes.size()-1){
                connections.add(new Node[]{sortedNodes.get(i), sortedNodes.get(0)});
                sortedNodes.get(i).connectionCount++;
                sortedNodes.get(0).connectionCount++;
            }
            else{
                connections.add(new Node[]{sortedNodes.get(i), sortedNodes.get(i+1)});
                sortedNodes.get(i).connectionCount++;
                sortedNodes.get(i+1).connectionCount++;
            }
        }
        boolean crossed;
        // For each node attempt to connect up to #maxConnections-2 times to nearby nodes without intersections.
        for (int i = 0; i < nodes.size(); i++){
            ArrayList<Node> possibleConnections = new ArrayList<>();
            if (maxConnections > 2){
                // Create a list of nodes the node can connect to without intersection.
                // Test each node j with node i:
                for (int j = 0; j < nodes.size(); j++){
                    // 1. A node cannot connect to itself. Skip any case where i==j
                    if(i!=j){
                        // 2. A node cannot create a connection that already exists. Skip any such connection.
                        if (!connections.contains(new Node[]{nodes.get(i), nodes.get(j)}) && !connections.contains(new Node[]{nodes.get(j), nodes.get(i)})){
                            boolean usable = true;
                            // 3. Test every other existing connection for intersection.
                            // Intersections with connections originating from a common node are exempted.
                            for (int k=0; k < connections.size(); k++){
                                if (!samePoint(
                                        nodes.get(i).x(),
                                        nodes.get(i).y(),
                                        nodes.get(j).x(),
                                        nodes.get(j).y(),
                                        connections.get(k)[0].x(),
                                        connections.get(k)[0].y(),
                                        connections.get(k)[1].x(),
                                        connections.get(k)[1].y())
                                        && intersectTest(
                                        nodes.get(i).x(),
                                        nodes.get(i).y(),
                                        nodes.get(j).x(),
                                        nodes.get(j).y(),
                                        connections.get(k)[0].x(),
                                        connections.get(k)[0].y(),
                                        connections.get(k)[1].x(),
                                        connections.get(k)[1].y())){
                                    usable = false;
                                }
                            }
                            if (usable){
                                possibleConnections.add(nodes.get(j));
                            }
                        }
                    }
                }
                for (int k = 0; k < possibleConnections.size(); k++){
                    if (nodes.get(i).connectionCount < maxConnections && possibleConnections.get(k).connectionCount < maxConnections) {
                        if (!testDuplicates(new Node[]{nodes.get(i), possibleConnections.get(k)})) {
                            connections.add(new Node[]{nodes.get(i), possibleConnections.get(k)});
                            nodes.get(i).connectionCount++;
                            possibleConnections.get(k).connectionCount++;
                        }
                    }
                }
            }
        }
        // Finally, having generated a solved puzzle, unsolve it by randomizing the position of each node.
        // Ensure that the average length between each node is not greater than 1/2 screen height.
        // Also ensure that no connection is longer than 3/4 screen height.
        boolean usable = false;
        while (!usable) {
            for (int i = 0; i < nodes.size(); i++) {
                boolean passed = false;
                while (!passed) {
                    passed = true;
                    nodes.get(i).xPos = randomGen.nextInt(xSize - (4 * nodeRadius)) + Math.round(2 * nodeRadius);
                    nodes.get(i).yPos = randomGen.nextInt((ySize-128) - (4 * nodeRadius)) + Math.round(2 * nodeRadius) +128;
                    for (int j = 0; j < nodes.size(); j++) {
                        int adjNodeRadius;
                        if (nodes.get(i).getAdjustedNodeSize()>nodes.get(j).getAdjustedNodeSize())
                            adjNodeRadius = nodes.get(i).getAdjustedNodeSize();
                        else
                            adjNodeRadius = nodes.get(j).getAdjustedNodeSize();
                        if ((!((nodes.get(i).xPos >= nodes.get(j).x() - (adjNodeRadius * 2f)
                                && nodes.get(i).xPos <= nodes.get(j).x() + (adjNodeRadius * 2f))
                                && (nodes.get(i).yPos >= nodes.get(j).y() - (adjNodeRadius * 2f))
                                && nodes.get(i).yPos <= nodes.get(j).y() + (adjNodeRadius * 2f)))) {
                        } else {
                            if (i != j) {
                                passed = false;
                            }
                        }
                    }
                }

            }
            usable = true;
            // get the average length of all connections
            long average = 0;
            for (int i = 0; i < connections.size(); i++){
                double dist = Math.sqrt(Math.pow(connections.get(i)[0].x() - connections.get(i)[1].x(), 2)
                        + Math.pow(connections.get(i)[0].y() - connections.get(i)[1].y(), 2) );
                average += dist;
                if (dist>3*(ySize/4)){
                    usable = false;
                }
            }
            average = average/connections.size();
            if (Math.round(average) > Math.round(ySize / 2)){
                usable = false;
            }
        }
    }

    private boolean testDuplicates(Node[] connection){
        for (int i = 0; i < connections.size(); i++) {
            if (connection[0].equals(connections.get(i)[0]) || connection[0].equals(connections.get(i)[1])) {
                if (connection[1].equals(connections.get(i)[0]) || connection[1].equals(connections.get(i)[1])) {
                    return true;
                }
            }
        }
        return false;
    }
    // Tests whether any two connections share a node.
    private boolean samePoint(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4){
        if (((x1 == x3 || x1 == x4)&&(y1 == y3 || y1 == y4))||((x2 == x3 || x2 == x4)&&(y2 == y3 || y2 == y4))){
            return true;
        }
        return false;
    }

    // Faster Intersection test provided by CommanderKeith (http://www.java-gaming.org/index.php?topic=22590.0)
    private boolean intersectTest(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4){
        // Return false if either of the lines have zero length
        if (x1 == x2 && y1 == y2 ||
                x3 == x4 && y3 == y4){
            return false;
        }
        // Fastest method, based on Franklin Antonio's "Faster Line Segment Intersection" topic "in Graphics Gems III" book (http://www.graphicsgems.org/)
        int ax = x2-x1;
        int ay = y2-y1;
        int bx = x3-x4;
        int by = y3-y4;
        int cx = x1-x3;
        int cy = y1-y3;

        int alphaNumerator = by*cx - bx*cy;
        int commonDenominator = ay*bx - ax*by;
        if (commonDenominator > 0){
            if (alphaNumerator < 0 || alphaNumerator > commonDenominator){
                return false;
            }
        }else if (commonDenominator < 0){
            if (alphaNumerator > 0 || alphaNumerator < commonDenominator){
                return false;
            }
        }
        double betaNumerator = ax*cy - ay*cx;
        if (commonDenominator > 0){
            if (betaNumerator < 0 || betaNumerator > commonDenominator){
                return false;
            }
        }else if (commonDenominator < 0){
            if (betaNumerator > 0 || betaNumerator < commonDenominator){
                return false;
            }
        }
        if (commonDenominator == 0){
            // This code wasn't in Franklin Antonio's method. It was added by Keith Woodward.
            // The lines are parallel.
            // Check if they're collinear.
            int y3LessY1 = y3-y1;
            int colinearityTestForP3 = x1*(y2-y3) + x2*(y3LessY1) + x3*(y1-y2);   // see http://mathworld.wolfram.com/Collinear.html
            // If p3 is collinear with p1 and p2 then p4 will also be collinear, since p1-p2 is parallel with p3-p4
            if (colinearityTestForP3 == 0){
                // The lines are collinear. Now check if they overlap.
                if (x1 >= x3 && x1 <= x4 || x1 <= x3 && x1 >= x4 ||
                        x2 >= x3 && x2 <= x4 || x2 <= x3 && x2 >= x4 ||
                        x3 >= x1 && x3 <= x2 || x3 <= x1 && x3 >= x2){
                    if (y1 >= y3 && y1 <= y4 || y1 <= y3 && y1 >= y4 ||
                            y2 >= y3 && y2 <= y4 || y2 <= y3 && y2 >= y4 ||
                            y3 >= y1 && y3 <= y2 || y3 <= y1 && y3 >= y2){
                        return true;
                    }
                }
            }
            return false;
        }
        return true;
    }
}
