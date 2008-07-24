import java.awt.*;
import java.io.Serializable;

public class Player implements Serializable
{
	public final String name;
	public final Color color;
	
	public Player(String n, Color c)
	{
		name = n;
		color = c;
	}
	
	public boolean equals(Object other)
	{
		if( other == null || !(other instanceof Player) )
			return false;
			
		Player otherPlayer = (Player)other;
		return otherPlayer.name.equals(name) && otherPlayer.color.equals(color);
	}
	
	public int hashCode()
	{
		return
			name.hashCode() ^ color.hashCode();
	}
    
    public String toString()
    {
        return "Player \"" + name + "\" (" + color.toString() + ")";
    }
		
}