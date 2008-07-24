import java.rmi.Remote;
import java.rmi.RemoteException;

interface WorldListener extends Remote
{
    void tileChanged(int row, int col, int state, Player owner, int stones) throws RemoteException;
    void currentPlayerChanged(Player newPlayer) throws RemoteException; // newPlayer==null means game is over!
}