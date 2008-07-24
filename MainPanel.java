import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.*;


public class MainPanel extends JPanel implements ActionListener
{
    MapControl mapControl;
    MiniMap miniMap;
    JButton zoomInButton, zoomOutButton, undoMoveButton, endTurnButton;
    JScrollPane mapScroller;
    JPanel overviewPanel, miniMapPanel;
    JLabel currentPlayerLabel;
    
    int mapControlSize = 30;
    
    WorldInterface world;
    Player player;
    
    final WorldListener worldListener;

    class MainWorldListener extends UnicastRemoteObject implements WorldListener
    {
        public MainWorldListener() throws RemoteException
        {
            world.addWorldListener(this);
        }
    
        public void tileChanged(int row, int col, int state, Player owner, int stones)
        {
            if(mapControl != null)
                mapControl.tileChanged(row, col, state, owner, stones);
                
            if(miniMap != null)
                miniMap.tileChanged(row, col, state, owner, stones);
        }
        
        public void currentPlayerChanged(Player newPlayer)
        {
            setEnabled(player.equals(newPlayer));
            if(newPlayer == null)
            {
                try {
                    Object[] resultSet = world.getGameResults().entrySet().toArray();
                    Arrays.sort(resultSet, new Comparator() {
                        public int compare(Object o1, Object o2) {
                            return ((Integer)(((Map.Entry)o2).getValue())).compareTo(
                                (Integer)(((Map.Entry)o1).getValue()) );
                        }
                    } );
                    
                    String results = "";
                    for(int n = 0; n < resultSet.length; ++n)
                    {
                         Map.Entry entry = (Map.Entry)resultSet[n];
                         results += ((Player)entry.getKey()).name + ": " +
                            ((Integer)entry.getValue()).intValue() + " fields\n";
                    }
                    
                    final String message = "Conquered territory per player:\n" + results;
                    ( new Thread() {
                        public void run() {
                            JOptionPane.showMessageDialog( MainPanel.this, message,
                                "Game has ended", JOptionPane.PLAIN_MESSAGE);
                        }
                    } ).start();
                }
                catch(RemoteException re) {
                    JOptionPane.showMessageDialog( MainPanel.this, "Failed to retrieve game results!\n\n" + re,
                        "Game has ended", JOptionPane.ERROR_MESSAGE );
                    re.printStackTrace();
                }
            }
            else
            {
                currentPlayerLabel.setBackground(newPlayer.color);
                currentPlayerLabel.setText(newPlayer.name);
            }
        }
    }    
        
    public MainPanel(WorldInterface world, Player player) throws RemoteException
    {
        this.world  = world;
        this.player = player;
        worldListener = new MainWorldListener();
        
        // Create layout
        setLayout(new BorderLayout());
        
        // Add map control with scrollbar
        mapScroller = new JScrollPane(mapControl = new MapControl(world, mapControlSize));
        mapScroller.setPreferredSize(new Dimension(0, 0));
        add(mapScroller, BorderLayout.CENTER);

        // Add left menu
        {
            Container panel = new JPanel(new BorderLayout());
            
            // Add control panel
            {
                Container controlPanel = new JPanel(new GridLayout(3,2));
                
                zoomInButton = new JButton("Zoom In");
                zoomInButton.setActionCommand("ZoomIn");
                zoomInButton.addActionListener(this);
                controlPanel.add(zoomInButton);
                zoomOutButton = new JButton("Zoom Out");
                zoomOutButton.setActionCommand("ZoomOut");
                zoomOutButton.addActionListener(this);
                controlPanel.add(zoomOutButton);
                
                undoMoveButton = new JButton("Undo Move");
                undoMoveButton.setActionCommand("UndoMove");
                undoMoveButton.addActionListener(this);
                controlPanel.add(undoMoveButton);                
                endTurnButton = new JButton("End Turn");
                endTurnButton.setActionCommand("EndTurn");
                endTurnButton.addActionListener(this);
                controlPanel.add(endTurnButton);
            
                controlPanel.add(new JLabel("Current player: ", JLabel.RIGHT));
                controlPanel.add(currentPlayerLabel = new JLabel("", JLabel.CENTER));
                currentPlayerLabel.setOpaque(true);
                currentPlayerLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createRaisedBevelBorder(), BorderFactory.createEmptyBorder(4, 4, 4, 4) ));
                panel.add(controlPanel, BorderLayout.NORTH);
            }
            
            // Add appropriately sized minimap
            {
                Dimension dim;
                int size = 32;
                do {
                    miniMap = new MiniMap(world, size--, mapScroller);
                    dim = miniMap.getPreferredSize();
                } while(size > 1 && (dim.width > 160 || dim.height > 240));
                Container miniMapPanel = new JPanel(new FlowLayout());
                miniMapPanel.add(miniMap);
                panel.add(miniMapPanel, BorderLayout.SOUTH);
            }
            
            add(panel, BorderLayout.WEST);
        }
        
        worldListener.currentPlayerChanged(world.getCurrentPlayer());
        setVisible(true);
    }
    
    public void setEnabled(boolean state)
    {
        super.setEnabled(state);
        mapControl.setEnabled(state);
        undoMoveButton.setEnabled(state);
        endTurnButton.setEnabled(state);
    }
    
    public void actionPerformed(ActionEvent ae)
    {
        String command = ae.getActionCommand();
        if(command.equals("ZoomIn"))
        {
			Point view = mapScroller.getViewport().getViewPosition();
			mapControlSize += 5;
            mapControl.setTileSize(mapControlSize);
            mapScroller.setViewportView(mapControl);
            mapScroller.getViewport().setViewPosition(view);
            if(mapControlSize >= 50)
                zoomInButton.setEnabled(false);
			zoomOutButton.setEnabled(true);
        }
        else
        if(command.equals("ZoomOut"))
        {
			Point view = mapScroller.getViewport().getViewPosition();
			mapControlSize -= 5;
            mapControl.setTileSize(mapControlSize);
            mapScroller.setViewportView(mapControl);
            mapScroller.getViewport().setViewPosition(view);
            if(mapControlSize <= 10)
                zoomOutButton.setEnabled(false);
            zoomInButton.setEnabled(true);
        }
        else
        if(command.equals("UndoMove"))
        {
            try {
                world.undoMove();
                mapControl.setSelection(false);
            }
            catch(RemoteException re) {
                JOptionPane.showMessageDialog( this, "Unable to send command to game server!\n\n" + re,
                    "Undo Move", JOptionPane.ERROR_MESSAGE );
                re.printStackTrace();
            }
        }
        else
        if(command.equals("EndTurn"))
        {
            try {
                world.endTurn();
                mapControl.setSelection(false);
                setEnabled(false);
            }
            catch(RemoteException re) {
                JOptionPane.showMessageDialog( this, "Unable to send command to game server!\n\n" + re,
                    "End Turn", JOptionPane.ERROR_MESSAGE );
                re.printStackTrace();
            }
        }
    }
}
