import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;
import java.rmi.RemoteException;

class MiniMapTileVisualizer
{
	private Tile tile;
	private Hexagon location;
    private MiniMap miniMap;
	private Polygon segmentIndicator;
	private boolean valid;
	
	MiniMapTileVisualizer(Tile tile, Hexagon location, MiniMap miniMap)
	{
		this.tile     = tile;
		this.location = location;
        this.miniMap  = miniMap;
		if(tile.getSegmentLocation() != -1)
			segmentIndicator = location.getSegment(tile.getSegmentLocation());
		valid = false;
	}

    public void tileChanged(int state, Player owner, int stones)
    {
        tile = tile.update(state, owner, stones);
        invalidate();
    }
    
	public void invalidate()
	{
		valid = false;
        miniMap.repaint();
	}
	
	public void paint(Graphics2D g)
	{
		if (tile.getState() == Tile.DEAD)
		{
			g.setColor(Color.black);
			g.fillPolygon(location);
			g.drawPolygon(location);
			valid = true;
			return;
		}
		
		g.setColor(tile.getOwner() != null ? tile.getOwner().color : Color.GRAY);
		
		g.fillPolygon(location);
		g.setColor(Color.gray);
		g.drawPolygon(location);
		
		if (segmentIndicator != null)
		{
			g.setColor(Color.black);
			g.drawPolyline(segmentIndicator.xpoints, segmentIndicator.ypoints, segmentIndicator.npoints);
		}
		
		valid = true;
	}
	
	public boolean isValid()
	{
		return valid;
	}
}

public class MiniMap extends JLabel
{
	private int hexSize;
				
	private VolatileImage imageBuffer;
	private Graphics2D graphicsBuffer;
	private Dimension bufferDimension;
	
	private Rectangle currentView = new Rectangle();
	
	private WorldInterface world;
	
	private JScrollPane mapScroller;
	
	private JViewport mapViewport;
	
	private MiniMapTileVisualizer[][] tiles;
    private int rows, cols;
	
	public MiniMap(WorldInterface world, int hexSize, JScrollPane mapScroller) throws RemoteException
	{
		this.world = world;
		this.hexSize = hexSize;
		this.mapScroller = mapScroller;
		mapViewport = mapScroller.getViewport();
        rows = world.getRows();
        cols = world.getCols();
		
		int tempX = (int)Math.round((cols + 0.5) * Hexagon.getHLength(hexSize)) + 1;
		int tempY = (int)Math.round((Hexagon.getVShared(hexSize) + hexSize) * rows +
						Hexagon.getVShared(hexSize)) + 1;
		
		bufferDimension = new Dimension(tempX, tempY);
		setPreferredSize(bufferDimension);
		
		int x, y;
        Tile[][] worldTiles = world.getTiles();
		tiles = new MiniMapTileVisualizer[rows][cols];
		for (int row = 0; row < rows; row++)
			for (int col = 0; col < cols; col++)
			{
				x = (int)(((row % 2) > 0 ? col + 0.5 : col) * Hexagon.getHLength(hexSize));
				y = row * (Hexagon.getVShared(hexSize) + hexSize);
				if(worldTiles[row][col] != null)
					tiles[row][col] = new MiniMapTileVisualizer( worldTiles[row][col],
                        new Hexagon(new Point(x,y), hexSize), this );
			}
		
		enableEvents(AWTEvent.MOUSE_EVENT_MASK);
	}
	
	protected void processMouseEvent(MouseEvent e)
	{
		if (e.getID() == MouseEvent.MOUSE_PRESSED)
		{
			Rectangle visible = ((JComponent)mapViewport.getView()).getVisibleRect();
			Dimension total = mapViewport.getView().getPreferredSize();
			Dimension size = getPreferredSize();
			
			Point location = new Point((int)((double)e.getX() / size.width * total.width),
									   (int)((double)e.getY() / size.height * total.height));
			
			location.x -= visible.width / 2;
			location.x = Math.min(location.x, total.width-visible.width);
			location.x = Math.max(location.x, 0);
			
			location.y -= visible.height / 2;
			location.y = Math.min(location.y, total.height-visible.height);
			location.y = Math.max(location.y, 0);
			
			mapViewport.setViewPosition(location);
            
            repaint();
		}
	}
	
	public void paint(Graphics g)
	{
		drawOffScreen();
		
		g.drawImage(imageBuffer, currentView.x, currentView.y, this);
		
		g.setColor(Color.red);
		
		Rectangle visible = ((JComponent)mapViewport.getView()).getVisibleRect();
		Dimension total = mapViewport.getView().getPreferredSize();
		
		Dimension size = getPreferredSize();
		
		g.drawRect((int)((double)visible.x / total.width * size.width),
				   (int)((double)visible.y / total.height * size.height),
				   (int)((double)visible.width / total.width * size.width),
				   (int)((double)visible.height / total.height * size.height));
	}
	
	public void drawOffScreen()
	{
		if (imageBuffer == null)
		{
			imageBuffer = createVolatileImage(bufferDimension.width, bufferDimension.height);
            graphicsBuffer=imageBuffer.createGraphics();
            
            graphicsBuffer.setBackground(Color.black);
            graphicsBuffer.clearRect(0, 0, bufferDimension.width, bufferDimension.height);
            
            for (int col = 0; col < cols; col++)
				for (int row = 0; row < rows; row++)
					if (tiles[row][col] != null)
						tiles[row][col].paint(graphicsBuffer);
        }
        else    
        {
        	for (int col = 0; col < cols; col++)
				for (int row = 0; row < rows; row++)
					if (tiles[row][col] != null && !tiles[row][col].isValid())
						tiles[row][col].paint(graphicsBuffer);
		}
	}

    public void tileChanged(int row, int col, int state, Player owner, int stones)
    {
        tiles[row][col].tileChanged(state, owner, stones);
    }
    
    public Dimension getPreferredSize()
    {
        return bufferDimension;
    }
    
    public Dimension getMinimumSize()
    {
        return bufferDimension;
    }

    public Dimension getMaximumSize()
    {
        return bufferDimension;
    }

}