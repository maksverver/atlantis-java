import java.io.Serializable;
public class TriangleScenario implements Scenario, Serializable
{
    protected final int sideLength;
    protected final Tile[][] map;
    
    public TriangleScenario(int sideLength)
    {
        if(sideLength < 2 || sideLength > 100)
            throw new IllegalArgumentException("sideLength must be between 2 and 100 (inclusive)");
            
        this.sideLength = sideLength;
        
        // Build map
        int rows  = 3 * sideLength;
        int cols  = 1 + (5 * sideLength)/2;
        map = new Tile[rows][cols];
        for(int line = 0; line < sideLength; ++line)
        {
            int row = 1 + 3*line;
            int col = 1 + (line + 1) / 2;
            for(int segment = 0; segment <= line; ++segment, col +=2, row -= 2)
                Tile.mergeMap(map, Tile.getSegment(row, col));
        }
    }
    
    public String toString()
    {
        return "3 players: triangle size "+sideLength;
    }
    
    public int getMaxPlayers()
    {
        return 3;
    }
    
    public Tile[][] getMap()
    {
        return Tile.cloneMap(map);
    }
    
    public Tile[] getPlayerAssignment(Player player, int position)
    {
        Tile[] segmentTiles;
        switch(position)
        {
        case 0:
            segmentTiles = Tile.getSegment(1, 1);
            break;
        case 1:
            segmentTiles = Tile.getSegment(
                1 + 3*(sideLength - 1),
                1 + sideLength / 2 );
            break;
        case 2:
            segmentTiles = Tile.getSegment(
                1 + 3*(sideLength - 1) - 2*(sideLength - 1),
                1 + sideLength / 2 + 2*(sideLength - 1) );
            break;
        default:
            throw new IllegalArgumentException("position must be between 0 and 3 (exclusive).");
        }
        for(int n = 0; n < segmentTiles.length; ++n)
            segmentTiles[n].addStones(1, player);
        return segmentTiles; 
    }

	public int getSideLength()
	{
	    return sideLength;
	}
}
