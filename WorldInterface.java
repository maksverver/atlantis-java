import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;

interface WorldInterface extends Remote
{
	int getRows() throws RemoteException;
    int getCols() throws RemoteException;
	int countLivingNeighbours(int row, int col) throws RemoteException;
    Collection neighbours(int row, int col) throws RemoteException;
    Player getCurrentPlayer() throws RemoteException;
    boolean isGameOver() throws RemoteException;
    Map getGameResults() throws RemoteException;
	
    Collection generateMoves(int row, int col) throws RemoteException;
	
	void executeMove(Move move) throws RemoteException;
    boolean undoMove() throws RemoteException;
	void endTurn() throws RemoteException;
	
    Tile[][] getTiles() throws RemoteException;
	Tile getTile(int row, int col) throws RemoteException;
	
    boolean addWorldListener(WorldListener wl) throws RemoteException;
    boolean removeWorldListener(WorldListener wl) throws RemoteException;
}
