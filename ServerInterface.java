import java.rmi.Remote;
import java.rmi.RemoteException;

interface ServerInterface extends Remote
{
    WorldInterface joinGame(Player myPlayer) throws RemoteException;
}
