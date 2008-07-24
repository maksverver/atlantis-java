import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import javax.swing.*;

class InitialDialog extends JDialog implements ActionListener
{
    /*****************************
     * Player name   [name]      *
     * Player color  [color]     *
     *****************************
     * RMI Registry: [hostname]  *
     * Game name:    [gamename]  *
     *****************************
     *  Scenario:    [scenario]  *
     *****************************
     *  [Join game] [Host game]  *
     *****************************/

    JTextField rmiHost, playerName, gameName;
    ButtonGroup playerColor;
    JComboBox scenario;
    public static final Scenario[] scenarios = new Scenario[] {
        new TriangleScenario( 2), new TriangleScenario( 3),  new TriangleScenario( 4), new TriangleScenario( 5), 
        new TriangleScenario( 6), new TriangleScenario( 7),  new TriangleScenario( 8), new TriangleScenario( 9), 
        new TriangleScenario(10), new TriangleScenario(11),  new TriangleScenario(12), new TriangleScenario(13),
        new TriangleScenario(14), new TriangleScenario(15),  new TriangleScenario(20), new TriangleScenario(25),
    };
    
    public static final Color[] colors = new Color[] {
        Color.getHSBColor(0.000f, 0.7f, 1.0f), Color.getHSBColor(0.075f, 0.7f, 1.0f),
        Color.getHSBColor(0.175f, 0.7f, 1.0f), Color.getHSBColor(0.325f, 0.7f, 1.0f),
        Color.getHSBColor(0.675f, 0.7f, 1.0f), Color.getHSBColor(0.775f, 0.7f, 1.0f) };    
     
    public InitialDialog()
    {
        setTitle("Atlantis - New Game");

        // Exit on window close.       
    	addWindowListener(
    	    new WindowAdapter() {
        		public void	windowClosing(WindowEvent e) {
        			System.exit(0);
        		}
    	    }
        );
    
        Container mainPanel   = new Box(BoxLayout.Y_AXIS);
        Container buttonPanel = new JPanel(new FlowLayout());
        
        // Add player config panel.
        {
            JPanel panel = new JPanel(new GridLayout(2,2));
            panel.add(new JLabel("Player name:"));
            panel.add(playerName = new JTextField(10));
            panel.add(new JLabel("Player color:"));
            playerColor = new ButtonGroup();
            JPanel colorPanel = new JPanel(new GridLayout(1, colors.length));
            for(int n = 0; n < colors.length; ++n)
            {
                AbstractButton button = new JRadioButton();
                button.setBackground(colors[n]);
                colorPanel.add(button);
                playerColor.add(button);
            }
            panel.add(colorPanel);
            mainPanel.add(panel);
        }

        mainPanel.add(Box.createVerticalStrut(8));
        
        // Add RMI registry config panel.
        {
            JPanel panel = new JPanel(new GridLayout(2,2));
            panel.add(new JLabel("RMI registry host:"));
            panel.add(rmiHost = new JTextField(10));
            rmiHost.setText(Atlantis.localHostName);
            panel.add(new JLabel("Game name:"));
            panel.add(gameName = new JTextField(10));
            gameName.setText("Atlantis");
            mainPanel.add(panel);
        }
        
        mainPanel.add(Box.createVerticalStrut(8));
        
        // Add scenarion config panel.
        {
            JPanel panel = new JPanel(new GridLayout(1,2));
            panel.add(new JLabel("Scenario:"));
            panel.add(scenario = new JComboBox(scenarios));
            mainPanel.add(panel);
        }

        // Add buttons
        {
            JButton button;
            buttonPanel.add(button = new JButton("Join game"));
            button.setActionCommand("Join");
            button.addActionListener(this);
            buttonPanel.add(button = new JButton("Host game"));
            button.setActionCommand("Host");
            button.addActionListener(this);
            buttonPanel.add(button = new JButton("Exit"));
            button.setActionCommand("Exit");
            button.addActionListener(this);
        }

        Container mainPane = new JPanel(new FlowLayout());
        mainPane.add(mainPanel);        
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(mainPane, BorderLayout.NORTH);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent ae)
    {
        String command = ae.getActionCommand();
        if(command.equals("Exit"))
        {
            System.exit(0);
        }
        else
        if(command.equals("Join") || command.equals("Host"))
        {
            String pn = playerName.getText();
            if(pn.length() == 0)
            {
                playerName.requestFocus();
                JOptionPane.showMessageDialog(this, "Please choose a player name.",
                    command + " Game", JOptionPane.INFORMATION_MESSAGE );
                return;
            }
            Color pc = null;
            Enumeration en = playerColor.getElements();
            while(en.hasMoreElements())
            {
                AbstractButton ab = (AbstractButton)en.nextElement();
                if(ab.isSelected())
                    pc = ab.getBackground();                
            }
            if(pc == null)
            {
                JOptionPane.showMessageDialog(this, "Please choose a player color.",
                    command + " Game", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            String rmiName = "//"+rmiHost.getText()+"/"+gameName.getText();
            ServerInterface server;
            if(command.equals("Host"))
            {
                try {
                    server = new World((Scenario)(scenario.getSelectedItem()));
                    Naming.rebind(rmiName, server);
                }
                catch(Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog( this, "Failed to bind game to RMI name \"" + rmiName + "\"!\n"+
                        "Please make sure that RMI registry is running and accessible.\n\n" + e,
                        command + " Game", JOptionPane.ERROR_MESSAGE );
                    return;
                }
            }
            else
            {
                try {
                    server = (ServerInterface)Naming.lookup(rmiName);
                }
                catch(Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog( this, "Failed to bind game to RMI name \"" + rmiName + "\"!\n"+
                        "Please make sure that RMI registry is running and accessible\n"+
                        "and that a compatible game is bound under the given name.\n\n" + e,
                        command + " Game", JOptionPane.ERROR_MESSAGE  );
                    return;
                }
            }

            try {
                Player player = new Player(pn, pc);
                WorldInterface world = server.joinGame(player);
                
                if(world == null)
                {
                    JOptionPane.showMessageDialog( this, "The game host has denied your application!\n" +
                        "The game may be full or may have already started, or you may have requesed\n" +
                        "a player name or player color that is already in use.\n",
                        command + " Game", JOptionPane.ERROR_MESSAGE  );
                    return;
                }

                MainFrame mainFrame = new MainFrame(world, player);
		        mainFrame.setBounds(100,100,600,600);

                hide();
                mainFrame.show();
            }
            catch(RemoteException re) {
                JOptionPane.showMessageDialog( this,
                    "Failed to contact the game bound on RMI name \"" + rmiName + "\"!\n"+
                    "The game may be non-existent.\n\n" + re,
                    command + " Game", JOptionPane.ERROR_MESSAGE  );
                re.printStackTrace();
                return;
            }
        }
        
    }
}


public class Atlantis
{
    public final static String localHostName;
    
    static
    {
        String hostName = "localhost"; 
        try
        {
            hostName = InetAddress.getLocalHost().getCanonicalHostName();
        }
        catch(java.net.UnknownHostException uhe)
        {
            System.err.println("Unable to look up local host name!");
            uhe.printStackTrace();
        }
        localHostName = hostName;

    }

    public static void main(String args[]) throws Exception
    {
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new RMISecurityManager());

        InitialDialog initialDialog = new InitialDialog();
        initialDialog.setBounds(100, 100, 400, 250);
        initialDialog.show();
    }
   
}