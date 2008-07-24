import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.rmi.RemoteException;

class MapControlTileVisualizer
{
	private Tile tile;
	private Hexagon location;
	private Polygon segmentIndicator;
	private boolean valid, focus, selected, target;
	private MapControl mapControl;

	MapControlTileVisualizer(Tile tile, MapControl mapControl)
	{
		this.tile       = tile;
		this.location   = location;
		this.mapControl = mapControl;
	}

    public void updateTile(int state, Player owner, int stones)
    {
        tile = tile.update(state, owner, stones);
        invalidate();
    }
    
	public void invalidate()
	{
		valid = false;
		mapControl.repaint();
	}
	
	public boolean isValid()
	{
		return valid;
	}
	
	public void setFocus(boolean f)
	{
		focus = f;
		invalidate();
	}
	
	public void setSelected(boolean s)
	{
		selected = s;
		invalidate();
	}
	
	public void setTarget(boolean t)
	{
		target = t;
		invalidate();
	}
	
	public boolean isTarget()
	{
		return target;
	}
    
    public void setLocation(Hexagon location)
    {
        this.location = location;
        if(tile.getSegmentLocation() != -1)
			segmentIndicator = location.getShrunkSegment(tile.getSegmentLocation());
        invalidate();
    }
	
	public void paint(Graphics2D g)
	{
		final int    state  = tile.getState();
		final int    stones = tile.getStones();
		final Player owner  = tile.getOwner();
		
		if (state == Tile.DEAD)
		{
			g.setColor(Color.darkGray);
			g.fillPolygon(location);
    		g.setColor(Color.black);
    		g.drawPolygon(location);
			valid = true;
			return;
		}
		
		if (state == Tile.GROWING)
			g.setColor(owner.color);
        else
		if (focus)
			g.setColor(new Color(220, 220, 255));
		else
		if (selected)
			g.setColor(new Color(150, 150, 255));
		else
		if (target)
			g.setColor(new Color(180, 180, 255));
		else
			g.setColor(new Color(200, 200, 235));
		
		g.fillPolygon(location);
		g.setColor(Color.black);
		g.drawPolygon(location);
		
		if(segmentIndicator != null)
		{
			g.drawPolyline(segmentIndicator.xpoints, segmentIndicator.ypoints, segmentIndicator.npoints);
		}
		
		if((state == Tile.OCCUPIED) || (state == Tile.GROWING))
		{
			Point p = new Point(location.getOrigin());
			Rectangle bound = location.getBounds();
			
			if (state == Tile.OCCUPIED)
			{
				g.setColor(tile.getOwner().color);
				int radius = bound.height/2;
				
				p.x += (location.getHLength() - radius)/2;
				p.y += radius/2;
				
				g.fillOval(p.x, p.y, radius, radius);
				g.setColor(Color.black);
				g.drawOval(p.x, p.y, radius-1, radius-1);
			}
			
			String text = String.valueOf(stones);
			Font f = g.getFont();
			
			switch(location.getSideLength())
			{
				case 50:
					f = f.deriveFont(36.0f);
					break;
				case 45:
					f = f.deriveFont(30.0f);
					break;
				case 40:
					f = f.deriveFont(26.0f);
					break;
				case 35:
					f = f.deriveFont(20.0f);
					break;
				case 30:
					f = f.deriveFont(18.0f);
					break;
				case 25:
					f = f.deriveFont(16.0f);
					break;
				default:
					f = f.deriveFont(12.0f);
					break;
			}
				
			f = f.deriveFont(Font.BOLD);
			
			g.setFont(f);
			FontMetrics fm = g.getFontMetrics();
			
			p.x = bound.x + bound.width/2 - fm.stringWidth(text)/2;
			p.y = bound.y + bound.height/2 + fm.getAscent()/3;
			
			g.drawString(text, p.x, p.y);
		}
		
		valid = true;
	}
}

public class MapControl extends JLabel
{
	private static int tileSize,
					   focusRow = -1,
					   focusCol = -1,
					   selectedRow = -1,
					   selectedCol = 1;
	
	private BufferedImage mouseMap;
	
	private VolatileImage imageBuffer;
	
	private Graphics2D graphicsBuffer;
	
	private Dimension dimensionBuffer,
					  dimensionMouseMap;
	
	private Rectangle currentView;
	
	private WorldInterface world;
	
	private MapControlTileVisualizer[][] tiles;
    
    private Collection moves;
    
    private int rows, cols;
    
	public MapControl(WorldInterface world, int tileSize) throws RemoteException
	{
		this.world    = world;

        rows = world.getRows();
        cols = world.getCols();

        // initialize tile visualizers
        Tile[][] worldTiles = world.getTiles();
		tiles = new MapControlTileVisualizer[rows][cols];
		for (int row = 0; row < rows; row++)
			for (int col = 0; col < cols; col++)
				if (worldTiles[row][col] != null)
					tiles[row][col] = new MapControlTileVisualizer(worldTiles[row][col], this);

        setTileSize(tileSize);
        
		enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
		enableEvents(AWTEvent.MOUSE_EVENT_MASK);
	}
    
    public void setTileSize(int tileSize)
    {
        this.tileSize = tileSize;
    
        // resize dimensions
        {
            int w = (int)Math.round((cols + 0.5) * Hexagon.getHLength(tileSize)) + 1;
            int h = (int)Math.round((Hexagon.getVShared(tileSize) + tileSize) * rows +
                    Hexagon.getVShared(tileSize)) + 1;
            dimensionBuffer = new Dimension(w, h);
            setPreferredSize(dimensionBuffer);
            currentView = new Rectangle();
        }
            
        // resize tile visualizers
        for (int row = 0; row < rows; row++)
            for (int col = 0; col < cols; col++)
                if(tiles[row][col] != null)
                {
                    int x = (int)(((row % 2) > 0 ? col + 0.5 : col) * Hexagon.getHLength(tileSize));
                    int y = row * (Hexagon.getVShared(tileSize) + tileSize);                    
                    tiles[row][col].setLocation(new Hexagon(new Point(x,y), tileSize));
                }

        // resize mouse map
		dimensionMouseMap = new Dimension(Hexagon.getHLength(tileSize),
			 (Hexagon.getVShared(tileSize) + tileSize) * 2);
			 
		mouseMap = new BufferedImage(dimensionMouseMap.width,
			dimensionMouseMap.height, BufferedImage.TYPE_INT_RGB);
		
		Hexagon hex = new Hexagon(new Point(0,0), tileSize);
		Graphics gLookup = mouseMap.getGraphics();
		
		Color fc = new Color(0, 1, 1); //middle
		gLookup.setColor(fc);
		gLookup.fillPolygon(hex);
		
		fc = new Color(0, 0, 0); //topleft
		hex.translate(-hex.getHHalfLength(), -(hex.getVShared() + tileSize));
		gLookup.setColor(fc);
		gLookup.fillPolygon(hex);
		
		fc = new Color(0, 0, 1); //topright
		hex.translate(dimensionMouseMap.width, 0);
		gLookup.setColor(fc);
		gLookup.fillPolygon(hex);
		
		fc = new Color(0, 2, 1); //bottomright
		hex.translate(0, dimensionMouseMap.height);
		gLookup.setColor(fc);
		gLookup.fillPolygon(hex);
		
		fc = new Color(0, 2, 0); //bottomleft
		hex.translate(-dimensionMouseMap.width, 0);
		gLookup.setColor(fc);
		gLookup.fillPolygon(hex);
    }
	
	protected void processMouseMotionEvent(MouseEvent e)
	{
	    if(!isEnabled())
	        return;
	        
		int regionRow, regionCol;
		
		regionRow = (int)(e.getY() / dimensionMouseMap.height) * 2;
		regionCol = (int)(e.getX() / dimensionMouseMap.width);
		
		int mouseMapY = e.getY() % dimensionMouseMap.height;
		int mouseMapX = e.getX() % dimensionMouseMap.width;
		
		Color c = new Color(mouseMap.getRGB(mouseMapX, mouseMapY));
		
		regionRow += c.getGreen() - 1;
		regionCol += c.getBlue() - 1;

		if(! (regionRow >= 0 && regionRow < rows &&
			  regionCol >= 0 && regionCol < cols &&
			  tiles[regionRow][regionCol] != null) )
		{
			regionRow = regionCol = -1;
		}
		
		if ((focusRow != regionRow) || (focusCol != regionCol))
		{
			Tile tile;
			
			if(focusRow != -1 && focusCol != -1)
				tiles[focusRow][focusCol].setFocus(false);
							
			focusRow = regionRow;
			focusCol = regionCol;
			
			if(focusRow != -1 && focusCol != -1)
				tiles[focusRow][focusCol].setFocus(true);
		}
	}
    
    protected void setSelectionTargets(boolean state)
    {
        Iterator i = moves.iterator();
        while(i.hasNext())
        {
            Move move = (Move)i.next();
            tiles[move.destRow][move.destCol].setTarget(state);
        }    
    }
	
	protected void processMouseEvent(MouseEvent e)
	{
	    if(!isEnabled())
	        return;

		if (e.getID() == MouseEvent.MOUSE_EXITED)
		{
			if(focusRow != -1 && focusCol != -1)
				tiles[focusRow][focusCol].setFocus(false);
			focusRow = focusCol = -1;
		}
		else if (e.getID() == MouseEvent.MOUSE_PRESSED &&
				 (e.getButton() == MouseEvent.BUTTON1) )
		{
			if( focusRow != -1 && focusCol != -1 && tiles[focusRow][focusCol].isTarget() )
            {
                Iterator i = moves.iterator();
                while(i.hasNext())
                {
                    Move move = (Move)i.next();
                    if( move.sourceRow  == selectedRow  &&  move.sourceCol == selectedCol &&
                        move.destRow    == focusRow     &&  move.destCol == focusCol )
                    {
                        setSelection(false);
                        try {
                            world.executeMove(move);
                        }
                        catch(java.rmi.RemoteException re) {
                            re.printStackTrace();
                        }
                        return;
                    }
                }
            }
            
            setSelection(false);
                    
            if( focusRow != -1 && focusCol != -1)
            {
                try {
                    moves = world.generateMoves(focusRow, focusCol);
                }
                catch(java.rmi.RemoteException re) {
                    re.printStackTrace();
                    moves = null;
                }
                
                if(moves != null )
				{
					selectedRow = focusRow;
					selectedCol = focusCol;
                    setSelection(true);
				}
			}
		}		
	}
	
	public void setSelection(boolean state)
	{
        if(state)
        {
            tiles[selectedRow][selectedCol].setSelected(true);
            setSelectionTargets(true);
        }
        else
        if(selectedRow != -1 && selectedCol != -1)
        {
            tiles[selectedRow][selectedCol].setSelected(false);
            setSelectionTargets(false);
    		selectedRow = selectedCol = -1;
        }
	}
	
	public void paint(Graphics g)
	{
		drawOffScreen();
		g.drawImage(imageBuffer, currentView.x, currentView.y, this);
	}
	
	public void drawOffScreen()
	{
		Rectangle view = getVisibleRect();
		if (!currentView.equals(view))
		{
			if ((currentView.width != view.width) ||
				(currentView.height != view.height))
            {
            	imageBuffer = createVolatileImage(view.width, view.height);
            	graphicsBuffer=imageBuffer.createGraphics();
            	
            	graphicsBuffer.translate(-view.x, -view.y);
            }
            else
            	graphicsBuffer.translate(currentView.x - view.x, currentView.y - view.y);
            
            currentView = view;
            
            graphicsBuffer.setBackground(Color.black);
            graphicsBuffer.clearRect(currentView.x, currentView.y, currentView.width, currentView.height);
            
            int firstRow = (int)(currentView.y / dimensionMouseMap.height) * 2 - 1;
            firstRow = Math.max(firstRow, 0);
			int firstCol = (int)(currentView.x / dimensionMouseMap.width) - 1;
			firstCol = Math.max(firstCol, 0);
			
			int lastRow = (int)((currentView.y + currentView.height) / dimensionMouseMap.height) * 2 + 1;
			lastRow = Math.min(lastRow, rows-1);
			int lastCol = (int)((currentView.x + currentView.width) / dimensionMouseMap.width);
			lastCol = Math.min(lastCol, cols-1);
			
			for (int col = firstCol; col <= lastCol; col++)
				for (int row = firstRow; row <= lastRow; row++)
					if(tiles[row][col] != null)
						tiles[row][col].paint(graphicsBuffer);
		}
		else
        {
        	int firstRow = (int)(currentView.y / dimensionMouseMap.height) * 2 - 1;
            firstRow = Math.max(firstRow, 0);
			int firstCol = (int)(currentView.x / dimensionMouseMap.width) - 1;
			firstCol = Math.max(firstCol, 0);
			
			int lastRow = (int)((currentView.y + currentView.height) / dimensionMouseMap.height) * 2 + 1;
			lastRow = Math.min(lastRow, rows-1);
			int lastCol = (int)((currentView.x + currentView.width) / dimensionMouseMap.width);
			lastCol = Math.min(lastCol, cols-1);
			
			for (int col = firstCol; col <= lastCol; col++)
				for (int row = firstRow; row <= lastRow; row++)
					if ( tiles[row][col] != null && !tiles[row][col].isValid() )
						 tiles[row][col].paint(graphicsBuffer);
		}
	}
    
    public void tileChanged(int row, int col, int state, Player owner, int stones)
    {
        tiles[row][col].updateTile(state, owner, stones);
    }
}
