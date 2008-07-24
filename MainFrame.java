import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.rmi.RemoteException;

public class MainFrame extends JFrame
{
	JScrollPane mainPanelScroller;
	MainPanel mainPanel;
	
	public MainFrame(WorldInterface world, Player player) throws RemoteException
	{
		super("Atlantis");

        // Exit on window close.       
    	addWindowListener(
    	    new WindowAdapter() {
        		public void	windowClosing(WindowEvent e) {
        			System.exit(0);
        		}
    	    }
        );
		
		mainPanel = new MainPanel(world, player);
		mainPanelScroller = new JScrollPane(mainPanel);
		getContentPane().add(mainPanelScroller);
	}
    
}
