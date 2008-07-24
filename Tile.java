import java.awt.*;
import java.util.*;
import java.io.Serializable;
import java.rmi.RemoteException;

public class Tile implements Cloneable, Serializable
{
	public static Tile[][] cloneMap(Tile[][] map)
	{
	    Tile[][] result;
	    int rows, cols;
	    result = new Tile[rows = map.length][];
	    for(int row = 0; row < rows; ++row)
	    {
	        result[row] = new Tile[cols = map[row].length];
    		for(int col = 0; col < cols; ++col)
    		    result[row][col] = ( map[row][col] == null ? null :
    		        (Tile)map[row][col].clone() );
	    }
		return result;
	}

	public static void mergeMap(Tile[][] map, Tile[] tiles)
	{
        for(int n = 0; n < tiles.length; ++n)
            map[tiles[n].row][tiles[n].col] = tiles[n];
	}

    public static Tile[] getSegment(int centerRow, int centerCol)
    {
        Tile[] tiles = new Tile[7];
        int segmentLocation = 32768*centerRow + centerCol;
        if(centerRow % 2 == 0)
        {
            tiles[0] = new Tile(centerRow,     centerCol,     segmentLocation, -1                );
            tiles[1] = new Tile(centerRow,     centerCol + 1, segmentLocation, Hexagon.EAST      );
			tiles[2] = new Tile(centerRow + 1, centerCol,     segmentLocation, Hexagon.SOUTH_EAST);
			tiles[3] = new Tile(centerRow + 1, centerCol - 1, segmentLocation, Hexagon.SOUTH_WEST);
			tiles[4] = new Tile(centerRow,     centerCol - 1, segmentLocation, Hexagon.WEST      );
			tiles[5] = new Tile(centerRow - 1, centerCol - 1, segmentLocation, Hexagon.NORTH_WEST);
			tiles[6] = new Tile(centerRow - 1, centerCol,     segmentLocation, Hexagon.NORTH_EAST);
		}
		else
		{
            tiles[0] = new Tile(centerRow,     centerCol,     segmentLocation, -1 );
            tiles[1] = new Tile(centerRow,     centerCol + 1, segmentLocation, Hexagon.EAST      );
            tiles[2] = new Tile(centerRow + 1, centerCol + 1, segmentLocation, Hexagon.SOUTH_EAST);
        	tiles[3] = new Tile(centerRow + 1, centerCol,     segmentLocation, Hexagon.SOUTH_WEST);
        	tiles[4] = new Tile(centerRow,     centerCol - 1, segmentLocation, Hexagon.WEST      );
        	tiles[5] = new Tile(centerRow - 1, centerCol,     segmentLocation, Hexagon.NORTH_WEST);
        	tiles[6] = new Tile(centerRow - 1, centerCol + 1, segmentLocation, Hexagon.NORTH_EAST);
        }
        return tiles;
    }

	
	// Tile states
	public static final int EMPTY    = 0,
							OCCUPIED = 1,
							GROWING  = 2,
							DEAD     = 3;
	
	/*
		Invariants;
			state==EMPTY    --> stones==0, player==null
			state==OCCUPIED --> stones> 0, player!=null
			state==GROWING  --> stones>=0, player!=null
			state==DEAD     --> stones==0, player==null
			
		Tiles in either EMPTY or OCCUPIED state are considered 'living'.
		Tiles in either GROWING or DEAD state are considered 'dead'.

	*/

	public final int row, col, segment, segmentLocation;
	
	private int stones, state;
	private Player owner;

	public Tile(int row, int col, int segment, int segmentLocation)
	{
		this.col             = col;
		this.row             = row;
		this.segment         = segment;
		this.segmentLocation = segmentLocation;
	}
	
	public Tile(int row, int col, int segment, int segmentLocation,
		int stones, int state, Player owner)
	{
		this(row, col, segment, segmentLocation);
		this.state  = state;
        this.stones = stones;
		this.owner  = owner;
	}

	public Tile update(int state, Player owner, int stones)
    {
        return new Tile(row, col, segment, segmentLocation, stones, state, owner);
    }
    
    	
	public Object clone()
	{
        return new Tile(row, col, segment, segmentLocation, stones, state, owner);	
	}

	public void addStones(int num, Player player)
	{
		if(num < 0)
			throw new IllegalArgumentException();
		
		if(num == 0)
			return;
			
		if (state == EMPTY)
		{
			// A new pile of stones has been created.
			stones = num;
			owner  = player;
			state  = OCCUPIED;
		}
		else
		if (state == OCCUPIED)
		{
			if (player.equals(owner))
			{
				// Add stones to an existing pile of the same player.
				stones += num;
			}
			else 
			{
				if(num > stones)
				{
					// Replace the existing pile with the remaining stones of the new player
					stones = num - stones;
					owner  = player;
				}
				else
				if(num < stones)
				{
					// Remove part of the stones from the existing pile.
					stones -= num;
				}
				else
				{
					// Remove all stones on the field.
					stones = 0;
					state  = EMPTY;
					owner  = null;
				}
			}
		}
	}
	
	public Player getOwner()
	{
		return owner;
	}	
	
	public int getState()
	{
		return state;
	}
	
	public int getStones()
	{
		return stones;
	}
	
	public int getSegment()
	{
		return segment;
	}
	
	public int getSegmentLocation()
	{
		return segmentLocation;
	}

	public boolean isAlive()
	{
		return state < GROWING;
	}

	public boolean isGrowing()
	{
		return state == GROWING;
	}

	public boolean updateState(World world)
	{
	    if(!( (state == OCCUPIED || state == GROWING) && owner.equals(world.getCurrentPlayer())
	            && stones >= world.countLivingNeighbours(row, col) ))
	    {
	        // No explosion.
            return false;
        }
        
        // Pile will explode now!
        if(state == OCCUPIED)
            state = GROWING;
        else
        if(state == GROWING)
            state = DEAD;

		stones = 0;
		
		Collection tiles = world.neighbours(row, col);
		Iterator i = tiles.iterator();
		while(i.hasNext())
		{
			Tile tile = (Tile)i.next();
			if(tile.getState() < GROWING)
				tile.addStones(1, owner);
			tile.updateState(world);
		}		
        
        if(state == DEAD)
            owner = null;

        return true;
	}
	
	public void grow()
	{
		++stones;
	}
	
	public void removeStones(int num)
	{
		if((num < 0) || (num > stones))
			throw new IllegalArgumentException();
				
		stones -= num;
		if (stones == 0)
		{
			owner  = null;
			state  = EMPTY;
		}
	}
    
    public boolean equals(Object other)
    {
		if( other == null || !(other instanceof Tile) )
			return false;
    
        Tile otherTile = (Tile)other;
        return ( otherTile.row == row && otherTile.col == col && otherTile.segment == segment &&
                 otherTile.segmentLocation == segmentLocation && 
                 otherTile.state == state && otherTile.stones == stones &&
                 (otherTile.owner == null ? owner == null : otherTile.owner.equals(owner)) );
    }
    
    public void assign(Tile that)
    {
        this.stones = that.stones;
        this.state  = that.state;
        this.owner  = that.owner;
    }

}