import java.awt.*;


public class Hexagon extends Polygon
{
	private int sideLength,
				hHalfLength,
				vShared,
				hLength,
				vLength;
	
	private Point origin;
	
	public final static int EAST       = 0,
							SOUTH_EAST = 1,
							SOUTH_WEST = 2,
							WEST       = 3,
							NORTH_WEST = 4,
							NORTH_EAST = 5;
	
	public Hexagon(Point ulOrigin, int sideLength)
	{
		this.sideLength = sideLength;
		origin = ulOrigin;
		
		vShared = (int)Math.round(Math.sin(Math.PI/6)*sideLength);
		hHalfLength = (int)Math.round(Math.cos(Math.PI/6)*sideLength);
		hLength = 2 * hHalfLength;
		vLength = sideLength + 2 * vShared;
		
		Point temp = new Point(origin);
		temp.x += hHalfLength;
		addPoint(temp.x, temp.y);
		
		temp.x += hHalfLength;
		temp.y += vShared;
		addPoint(temp.x, temp.y);
		
		temp.y += sideLength;
		addPoint(temp.x, temp.y);
		
		temp.y += vShared;
		temp.x -= hHalfLength;
		addPoint(temp.x, temp.y);
		
		temp.y -= vShared;
		temp.x -= hHalfLength;
		addPoint(temp.x, temp.y);
		
		temp.y -= sideLength;
		addPoint(temp.x, temp.y);
		
		temp.y -= vShared;
		temp.x += hHalfLength;
		addPoint(temp.x, temp.y);
	}
	
	public Polygon getSegment(int segment)
	{
		int num = 4;
		int[] newX = new int[num];
		int[] newY = new int[num];
		
		switch (segment)
		{
			case EAST:
				newX[0] = xpoints[0];
				newY[0] = ypoints[0];
				
				newX[1] = xpoints[1];
				newY[1] = ypoints[1];
				
				newX[2] = xpoints[2];
				newY[2] = ypoints[2];
				
				newX[3] = xpoints[3];
				newY[3] = ypoints[3];
				
				return new Polygon(newX, newY, num);
				
			case SOUTH_EAST:
				newX[0] = xpoints[1];
				newY[0] = ypoints[1];
				
				newX[1] = xpoints[2];
				newY[1] = ypoints[2];
				
				newX[2] = xpoints[3];
				newY[2] = ypoints[3];
				
				newX[3] = xpoints[4];
				newY[3] = ypoints[4];
				
				return new Polygon(newX, newY, num);
				
			case SOUTH_WEST:
				newX[0] = xpoints[2];
				newY[0] = ypoints[2];
				
				newX[1] = xpoints[3];
				newY[1] = ypoints[3];
				
				newX[2] = xpoints[4];
				newY[2] = ypoints[4];
				
				newX[3] = xpoints[5];
				newY[3] = ypoints[5];
				
				return new Polygon(newX, newY, num);
				
			case WEST:
				newX[0] = xpoints[3];
				newY[0] = ypoints[3];
				
				newX[1] = xpoints[4];
				newY[1] = ypoints[4];
				
				newX[2] = xpoints[5];
				newY[2] = ypoints[5];
				
				newX[3] = xpoints[6];
				newY[3] = ypoints[6];
				
				return new Polygon(newX, newY, num);
				
			case NORTH_WEST:
				newX[0] = xpoints[4];
				newY[0] = ypoints[4];
				
				newX[1] = xpoints[5];
				newY[1] = ypoints[5];
				
				newX[2] = xpoints[6];
				newY[2] = ypoints[6];
				
				newX[3] = xpoints[1];
				newY[3] = ypoints[1];
				
				return new Polygon(newX, newY, num);
				
			case NORTH_EAST:
				newX[0] = xpoints[5];
				newY[0] = ypoints[5];
				
				newX[1] = xpoints[6];
				newY[1] = ypoints[6];
				
				newX[2] = xpoints[1];
				newY[2] = ypoints[1];
				
				newX[3] = xpoints[2];
				newY[3] = ypoints[2];
				
				return new Polygon(newX, newY, num);
			default:
				return null;
		}
	}
			
	public Polygon getShrunkSegment(int segment)
	{
		int num = 4;
		int[] newX = new int[num];
		int[] newY = new int[num];
		
		switch (segment)
		{
			case EAST:
				newX[0] = xpoints[0];
				newY[0] = ypoints[0]+1;
				
				newX[1] = xpoints[1]-1;
				newY[1] = ypoints[1];
				
				newX[2] = xpoints[2]-1;
				newY[2] = ypoints[2];
				
				newX[3] = xpoints[3];
				newY[3] = ypoints[3]-1;
				
				return new Polygon(newX, newY, num);
				
			case SOUTH_EAST:
				newX[0] = xpoints[1]-1;
				newY[0] = ypoints[1];
				
				newX[1] = xpoints[2]-1;
				newY[1] = ypoints[2];
				
				newX[2] = xpoints[3];
				newY[2] = ypoints[3]-1;
				
				newX[3] = xpoints[4]+1;
				newY[3] = ypoints[4];
				
				return new Polygon(newX, newY, num);
				
			case SOUTH_WEST:
				newX[0] = xpoints[2]-1;
				newY[0] = ypoints[2];
				
				newX[1] = xpoints[3];
				newY[1] = ypoints[3]-1;
				
				newX[2] = xpoints[4]+1;
				newY[2] = ypoints[4];
				
				newX[3] = xpoints[5]+1;
				newY[3] = ypoints[5];
				
				return new Polygon(newX, newY, num);
				
			case WEST:
				newX[0] = xpoints[3];
				newY[0] = ypoints[3]-1;
				
				newX[1] = xpoints[4]+1;
				newY[1] = ypoints[4];
				
				newX[2] = xpoints[5]+1;
				newY[2] = ypoints[5];
				
				newX[3] = xpoints[6];
				newY[3] = ypoints[6]+1;
				
				return new Polygon(newX, newY, num);
				
			case NORTH_WEST:
				newX[0] = xpoints[4]+1;
				newY[0] = ypoints[4];
				
				newX[1] = xpoints[5]+1;
				newY[1] = ypoints[5];
				
				newX[2] = xpoints[6];
				newY[2] = ypoints[6]+1;
				
				newX[3] = xpoints[1]-1;
				newY[3] = ypoints[1];
				
				return new Polygon(newX, newY, num);
				
			case NORTH_EAST:
				newX[0] = xpoints[5]+1;
				newY[0] = ypoints[5];
				
				newX[1] = xpoints[6];
				newY[1] = ypoints[6]+1;
				
				newX[2] = xpoints[1]-1;
				newY[2] = ypoints[1];
				
				newX[3] = xpoints[2]-1;
				newY[3] = ypoints[2];
				
				return new Polygon(newX, newY, num);
			default:
				return null;
		}
	}		
	
	public void setOrigin(Point location)
	{
		int x = location.x - origin.x;
		int y = location.y - origin.y;
		
		translate(x,y);
	}
	
	public Point getOrigin()
	{
		return origin;
	}
	
	public int getSideLength()
	{
		return sideLength;
	}
	
	public int getHHalfLength()
	{
		return hHalfLength;
	}
	
	public int getVShared()
	{
		return vShared;
	}
	
	public int getHLength()
	{
		return hLength;
	}
	
	public int getVLength()
	{
		return vLength;
	}
	
	public static int getHHalfLength(int sideLength)
	{
		return (int)Math.round(Math.cos(Math.PI/6)*sideLength);
	}
	
	public static int getVShared(int sideLength)
	{
		return (int)Math.round(Math.sin(Math.PI/6)*sideLength);
	}
	
	public static int getHLength(int sideLength)
	{
		int hHalfLength = (int)Math.round(Math.cos(Math.PI/6)*sideLength);
		return 2 * hHalfLength;
	}
	
	public static int getVLength(int sideLength)
	{
		int vShared = (int)Math.round(Math.sin(Math.PI/6)*sideLength);
		return sideLength + 2 * vShared;
	}
}