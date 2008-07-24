import java.awt.Color;
import java.util.*;
import java.io.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

public class World extends UnicastRemoteObject implements WorldInterface, ServerInterface
{
    public final Scenario scenario;

    protected Collection listeners = new ArrayList();

    private List players = new ArrayList();
    private int currentPlayer;
	private HashMap gameResults;

	private final int rows, cols;
	private Tile[][] tiles;
    private Stack
        oldTiles = new Stack(),
        oldMoves = new Stack();

	public World(Scenario scenario) throws RemoteException
	{
	    this.scenario = scenario;
	    this.tiles    = scenario.getMap();
	    this.rows     = this.tiles.length;
	    this.cols     = this.tiles[0].length;
	}
    
    public WorldInterface joinGame(Player myPlayer) throws RemoteException
    {
        Tile[][] oldTiles;
        
        synchronized(this) {
            if(players.size() >= scenario.getMaxPlayers())
                return null;
                
            Iterator i = players.iterator();
            while(i.hasNext())
            {
                Player player = ((Player)i.next());
                if(myPlayer.name.equals(player.name) || myPlayer.color.equals(player.color))
                    return null;
            }
                
            players.add(myPlayer);
    
            oldTiles  = Tile.cloneMap(tiles);
            Tile[] playerTiles = scenario.getPlayerAssignment(myPlayer, players.size()-1);
            Tile.mergeMap(tiles, playerTiles);
        }
        
        fireTileChangedEvents(oldTiles);
        return this;
    }
    
    synchronized public Player getCurrentPlayer()
    {
        return (players.isEmpty() || isGameOver()) ? null : (Player)players.get(currentPlayer);
    }
    
    synchronized public boolean isGameOver()
    {
        return gameResults != null;
    }

    synchronized public Map getGameResults()
    {
        return (Map)gameResults.clone();
    }
	
	synchronized public Collection generateMoves(int row, int col)
	{
        if(isGameOver())
            return null;
            
        Tile sourceTile = getTile(row, col);

        if(sourceTile.getOwner() != (Player)players.get(currentPlayer))
            return null; // tile is no owned by current player
        
        if(!sourceTile.isAlive())
            return null; // tile is not alive

        {
            Iterator i = oldMoves.iterator();
            while(i.hasNext())
            {
                Move move = (Move)i.next();
                if(move.destRow == row && move.destCol == col)
                    return null; // tile has already been moved to.                
                    
                if(getTile(move.sourceRow, move.sourceCol).segment == sourceTile.segment)
                    return null; // segment has already been moved from.
            }
        }
        
        // Valid source tile; generate available moves.
        {
            Collection moves = new ArrayList();
            for (int i = 0; i < 6; i++)
                generateDirectedMoves(row, col, row, col, 0, sourceTile.getStones(), i, moves);
            return moves;
        }
	}

	private void generateDirectedMoves ( int sourceRow, int sourceCol,
        int row, int col, int stepsTaken, int stones, int direction,
        Collection moves )
	{
		int nextRow, nextCol;
		
		if(row % 2 == 0)
		{
			switch (direction)
			{
				case Hexagon.EAST:
					nextRow = row;
					nextCol = col+1;
					break;
				case Hexagon.SOUTH_EAST:
					nextRow = row+1;
					nextCol = col;
					break;
				case Hexagon.SOUTH_WEST:
					nextRow = row+1;
					nextCol = col-1;
					break;
				case Hexagon.WEST:
					nextRow = row;
					nextCol = col-1;
					break;
				case Hexagon.NORTH_WEST:
					nextRow = row-1;
					nextCol = col-1;
					break;
				case Hexagon.NORTH_EAST:
					nextRow = row-1;
					nextCol = col;
					break;
				default:
					throw new IllegalArgumentException();
			}
		}
		else
		{			
			switch (direction)
			{
				case Hexagon.EAST:
					nextRow = row;
					nextCol = col+1;
					break;
				case Hexagon.SOUTH_EAST:
					nextRow = row+1;
					nextCol = col+1;
					break;
				case Hexagon.SOUTH_WEST:
					nextRow = row+1;
					nextCol = col;
					break;
				case Hexagon.WEST:
					nextRow = row;
					nextCol = col-1;
					break;
				case Hexagon.NORTH_WEST:
					nextRow = row-1;
					nextCol = col;
					break;
				case Hexagon.NORTH_EAST:
					nextRow = row-1;
					nextCol = col+1;
					break;
				default:
					throw new IllegalArgumentException();
			}
		}
		
		try
		{
			if (tiles[nextRow][nextCol].getState() < Tile.GROWING)
			{
				stepsTaken++;
                
                moves.add(new Move(direction, stepsTaken, sourceRow, sourceCol, nextRow, nextCol));
				
                if (stepsTaken < stones)
					generateDirectedMoves( sourceRow, sourceCol,
                        nextRow, nextCol, stepsTaken, stones, direction, moves );
			}
		}
		catch (Exception e)	{}
	}
	
	public void executeMove(Move move)
	{
	    synchronized(this) {
            oldTiles.push(Tile.cloneMap(tiles));
            oldMoves.push(move);
            executeStep(move.direction, move.stones, move.sourceRow, move.sourceCol, move.destRow, move.destCol);
        }

        fireTileChangedEvents((Tile[][])oldTiles.peek());
    }

    private void executeStep( int direction, int stones,
        int row, int col, int destRow, int destCol )
    {    
        if( (row == destRow && col == destCol) || (stones == 0) || 
            (tiles[row][col].getOwner() != (Player)players.get(currentPlayer)) )
        {
            // move is done.
            return;
        }

		int nextRow, nextCol;
		if(row % 2 == 0)
		{
			switch (direction)
			{
				case Hexagon.EAST:
					nextRow = row;
					nextCol = col+1;
					break;
				case Hexagon.SOUTH_EAST:
					nextRow = row+1;
					nextCol = col;
					break;
				case Hexagon.SOUTH_WEST:
					nextRow = row+1;
					nextCol = col-1;
					break;
				case Hexagon.WEST:
					nextRow = row;
					nextCol = col-1;
					break;
				case Hexagon.NORTH_WEST:
					nextRow = row-1;
					nextCol = col-1;
					break;
				case Hexagon.NORTH_EAST:
					nextRow = row-1;
					nextCol = col;
					break;
				default:
					throw new IllegalArgumentException();
			}
		}
		else
		{
			switch (direction)
			{
				case Hexagon.EAST:
					nextRow = row;
					nextCol = col+1;
					break;
				case Hexagon.SOUTH_EAST:
					nextRow = row+1;
					nextCol = col+1;
					break;
				case Hexagon.SOUTH_WEST:
					nextRow = row+1;
					nextCol = col;
					break;
				case Hexagon.WEST:
					nextRow = row;
					nextCol = col-1;
					break;
				case Hexagon.NORTH_WEST:
					nextRow = row-1;
					nextCol = col;
					break;
				case Hexagon.NORTH_EAST:
					nextRow = row-1;
					nextCol = col+1;
					break;
				default:
					throw new IllegalArgumentException();
			}
		}
		
		tiles[row][col].removeStones(stones);
		tiles[nextRow][nextCol].addStones(stones, (Player)(Player)players.get(currentPlayer));
		
        int nextStones = ( tiles[nextRow][nextCol].getStones() < stones ?
            tiles[nextRow][nextCol].getStones() : stones);
        
        executeStep(direction, nextStones, nextRow, nextCol, destRow, destCol);
	}

	public void endTurn()
	{
	    Tile[][] backupTiles;
	    
        // remove undo information
        synchronized(this) {
            oldMoves.clear();
            oldTiles.clear();
    
            backupTiles = Tile.cloneMap(tiles);

            // explode
            for (int row = 0; row < rows; row++)
    			for (int col = 0; col < cols; col++)
    			{
    			    Player owner = tiles[row][col] == null ? null : tiles[row][col].getOwner();
    				if(owner != null && owner.equals((Player)players.get(currentPlayer)))
    					tiles[row][col].updateState(this);
    			}
                    
            // grow
    		for (int row = 0; row < rows; row++)
    			for (int col = 0; col < cols; col++)
    			{
    			    Player owner = tiles[row][col] == null ? null : tiles[row][col].getOwner();
    				if( owner != null && owner.equals((Player)players.get(currentPlayer)) &&
    					tiles[row][col].getState() == Tile.GROWING )
    				{
    					tiles[row][col].grow();
    				}
    			}
    			
            // check for end of game
            if(!calculateEndGame())
            {
                // give the turn to the next player
                boolean player_found = false;
                while(!player_found)
                {
                    currentPlayer = (currentPlayer + 1) % players.size();
                    Player player = (Player)players.get(currentPlayer);
                find_player:
            		for (int row = 0; row < rows; row++)
            			for (int col = 0; col < cols; col++)
            			    if(tiles[row][col] != null && player.equals(tiles[row][col].getOwner()))
            			    {
            			        player_found = true;
            			        break find_player;
                            }
                }
            }        
        }		
        
        fireTileChangedEvents(backupTiles);
        firePlayerChangedEvent();
 	}
    
    public boolean undoMove()
    {
        Tile[][] backupTiles;
        
        synchronized(this) {
            if(oldMoves.isEmpty())
                return false;
                
            oldMoves.pop();
    
            backupTiles = Tile.cloneMap(tiles);
            tiles = (Tile[][])oldTiles.pop();
        }
        
        fireTileChangedEvents(backupTiles);
        
        return true;        
    }

	public synchronized Collection neighbours(int row, int col)
	{
		Collection results = new ArrayList(6);
		Tile tile;
		if(row % 2 == 0)
		{
			if(col+1 < cols && (tile = tiles[row  ][col+1]) != null)
				results.add(tile);
			if(row+1 < rows && (tile = tiles[row+1][col  ]) != null)
				results.add(tile);
			if(row+1 < rows && col-1 >= 0 && (tile = tiles[row+1][col-1]) != null)
				results.add(tile);
			if(col-1 >= 0 && (tile = tiles[row  ][col-1]) != null)
				results.add(tile);
			if(row-1 >= 0 && col-1 >= 0 && (tile = tiles[row-1][col-1]) != null)
				results.add(tile);
			if(row-1 >= 0 && (tile = tiles[row-1][col  ]) != null)
				results.add(tile);
		}
		else
		{
			if(col+1 < cols && (tile = tiles[row  ][col+1]) != null)
				results.add(tile);
			if(row+1 < rows && col+1 < cols && (tile = tiles[row+1][col+1]) != null)
				results.add(tile);
			if(row+1 < rows && (tile = tiles[row+1][col  ]) != null)
				results.add(tile);
			if(col-1 >= 0 && (tile = tiles[row  ][col-1]) != null)
				results.add(tile);
			if(row-1 >= 0 && (tile = tiles[row-1][col  ]) != null)
				results.add(tile);
			if(row-1 >= 0 && col+1 < cols && (tile = tiles[row-1][col+1]) != null)
				results.add(tile);
		}		
		
		return results;
	}

	public synchronized int countLivingNeighbours(int row, int col)
	{
		int result = 0;
		Collection tiles = neighbours(row, col);
		Iterator i = tiles.iterator();
		while(i.hasNext())
			if(((Tile)i.next()).getState() < Tile.GROWING)
				++result;
		return result;
	}
	
	public synchronized Tile[][] getTiles()
	{
		return Tile.cloneMap(tiles);
	}

	public synchronized Tile getTile(int row, int col)
	{
		return (row >= 0 && col >= 0 && row < rows && col < cols) ? tiles[row][col] : null;
	}
	
	public int getRows()
	{
		return rows;
	}
	
	public int getCols()
	{
		return cols;
	}
	
	private synchronized boolean calculateEndGame()
	{
		for (int row = 0; row < rows; row++)
			for (int col = 0; col < cols; col++)
				if(tiles[row][col] != null && tiles[row][col].isGrowing())
					return false;
		
		gameResults = new HashMap();
		
		Iterator i = calculateGroups().iterator();
		while(i.hasNext())
			if (!calculateGroupScore((Collection)i.next()))
            {
                gameResults = null;
				return false;
            }
				
		return true;
	}
	
	private Collection calculateGroups()
	{
		Collection groups = new ArrayList();
		
		Tile[][] clone = Tile.cloneMap(tiles);
		
		for (int row = 0; row < rows; row++)
			for (int col = 0; col < cols; col++)
				if(clone[row][col] != null && clone[row][col].getState() < Tile.GROWING)
				{
					Collection group = new ArrayList();
					floodFillGroup(group, row, col, clone);
					groups.add(group);
				}

		return groups;
	}
	
	private void floodFillGroup(Collection group, int row, int col, Tile[][] world)
	{
		if(world[row][col] == null || !world[row][col].isAlive())
			return;
		
		group.add(world[row][col]);
		world[row][col] = null;
		
		if(row % 2 == 0)
		{
			if(col+1 < cols) floodFillGroup(group, row, col+1, world);	//EAST
			if(row+1 < rows) floodFillGroup(group, row+1, col, world);	//SOUTH_EAST
			if(row+1 < rows && col-1 >= 0) floodFillGroup(group, row+1, col-1, world);	//SOUTH_WEST
			if(col-1 >= 0) floodFillGroup(group, row, col-1, world);	//WEST
			if(row-1 >= 0 && col-1 >= 0) floodFillGroup(group, row-1, col-1, world);	//NORTH_WEST
			if(row-1 >= 0) floodFillGroup(group, row-1, col, world);	//NORTH_EAST
		}				
		else
		{
			if(col+1 < cols) floodFillGroup(group, row, col+1, world);	//EAST
			if(row+1 < rows && col+1 < cols) floodFillGroup(group, row+1, col+1, world);	//SOUTH_EAST
			if(row+1 < rows) floodFillGroup(group, row+1, col, world);	//SOUTH_WEST
			if(col-1 >= 0) floodFillGroup(group, row, col-1, world);	//WEST
			if(row-1 >= 0) floodFillGroup(group, row-1, col, world);	//NORTH_WEST
			if(row-1 >= 0 && col+1 < cols) floodFillGroup(group, row-1, col+1, world);	//NORTH_EAST
		}
	}
		
    private boolean calculateGroupScore(Collection group)
	{
		Player p = null;
		Tile tile;
		
		Iterator i = group.iterator();
		while(i.hasNext())		
		{
			tile = (Tile)i.next();
			
			if (tile.getState() == Tile.OCCUPIED)
			{
				if (p == null)
					p = tile.getOwner();
				else
				if(!p.equals(tile.getOwner()))
					return false;
			}
		}
		
		if (p != null)
		{
			Integer count;
			if (gameResults.containsKey(p))
			{
				count = (Integer)gameResults.remove(p);
				count = new Integer(count.intValue() + group.size());
			}
			else
				count = new Integer(group.size());
			
			gameResults.put(p, count);
		}

		return true;			
	}
    
    public boolean addWorldListener(WorldListener wl)
    {
        synchronized(listeners)
        {
            return listeners.add(wl);
        }
    }
    
    public boolean removeWorldListener(WorldListener wl)
    {
        synchronized(listeners)
        {
            return listeners.remove(wl);        
        }
    }
    
    protected void fireTileChangedEvents(Tile[][] referenceTiles)
    {
        for(int row = 0; row < rows; row++)
			for (int col = 0; col < cols; col++)
                if(tiles[row][col] != null && !tiles[row][col].equals(referenceTiles[row][col]))
                    fireTileChangedEvent(tiles[row][col]);
    }
    
    protected void fireTileChangedEvent(Tile tile)
    {
        Collection purged = new ArrayList();
        synchronized(listeners)
        {
            Iterator i = listeners.iterator();
            while(i.hasNext())
            {
                WorldListener wl = (WorldListener)i.next();
                try
                {
                    wl.tileChanged( tile.row, tile.col,
                        tile.getState(), tile.getOwner(), tile.getStones() );
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    purged.add(wl);
                }
            }        
            listeners.removeAll(purged);
        }        
    }
    
    protected void firePlayerChangedEvent()
    {
        Collection purged = new ArrayList();
        synchronized(listeners)
        {
            Iterator i = listeners.iterator();
            while(i.hasNext())
            {
                WorldListener wl = (WorldListener)i.next();
                try
                {
                    wl.currentPlayerChanged( getCurrentPlayer() );
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    purged.add(wl);
                }
            }
            listeners.removeAll(purged);
        }
    }
}