import java.io.Serializable;

public class Move implements Serializable, Cloneable
{
	public final int
		direction,	// 0 <= direction < 6
		stones,		// 0 <= stones < 5 
		sourceRow,
		sourceCol,
		destRow,
		destCol;
			   
	public Move( int direction, int stones, int sourceRow,
				 int sourceCol, int destRow, int destCol )
	{
		this.direction = direction;
		this.stones = stones;
		this.sourceRow = sourceRow;
		this.sourceCol = sourceCol;
		this.destRow = destRow;
		this.destCol = destCol;
	}
	
	public Move( Move otherMove )
	{
		this( otherMove.direction, otherMove.stones,
			  otherMove.sourceRow, otherMove.sourceCol,
			  otherMove.destRow,   otherMove.destCol );
	}
	
	public boolean equals(Object other)
	{
		if( other == null || !(other instanceof Move) )
			return false;
			
		Move otherMove = (Move)other;
		return
			otherMove.direction == direction &&	 otherMove.stones    == stones    &&
			otherMove.sourceRow == sourceRow &&  otherMove.sourceCol == sourceCol &&
			otherMove.destRow   == destRow   &&  otherMove.destCol   == destCol ;
	}
	
	public int hashCode()
	{
		return
			direction <<  0 | stones    <<  3 |
			sourceRow <<  6 | sourceCol << 12 |
			destRow   << 18 | destCol   << 24 ;
	}
	
	public Object clone()
	{
		return new Move(this);
	}
}