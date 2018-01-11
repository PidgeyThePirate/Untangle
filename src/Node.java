package antimonypidgey.untangle;

/**
 * A container class representing a node and its connections.
 *
 * Variables:
 * int xPos/yPos - x and y co-ordinate on the game board the point belongs to.
 */
public class Node {
    public int xPos;
    public int yPos;
    public double midAngle;
    public int connectionCount = 0;
    public int baseNodeSize;

    public Node(int _xPos, int _yPos, int _nodeSize){
        xPos = _xPos;
        yPos = _yPos;
        baseNodeSize = _nodeSize;
    }

    public int x(){
        return xPos;
    }

    public int y(){
        return yPos;
    }

    public int getAdjustedNodeSize(){
        return baseNodeSize+(Math.round(baseNodeSize*0.1f)*connectionCount);
    }
}
