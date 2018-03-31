import java.awt.*;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by joseLucas on 20/05/17.
 */
public interface GameActions extends Remote {

    /**
     * This method must be used to help users to determine that some player is on match or not, to avoid a remote player
     * try to connect with a player that is already on match.
     * */
    public MatchStatus mStatus()throws RemoteException;
    public Player getPlayer() throws  RemoteException;
    public String getPlayerName() throws RemoteException;
    public Boolean connectWith(String name) throws RemoteException;
    public void showChatMessage(String playerName,String message) throws RemoteException;



    public void addNewBlock(int value , Point point) throws RemoteException;
    public void moveBlockFrom(Point initialPos,Point finalPos) throws RemoteException;
    public void rmBlockAt(Point point) throws RemoteException;

    //public void updateGameStatusTo(GameStatus status) throws RemoteException;
    //public void updateMatchStatusTo(MatchStatus status) throws RemoteException;



    //These method must be called only remotelly
    public void informPlayerToUpdateBoard() throws RemoteException;
    public void informPlayerIsQuiting() throws RemoteException;
    public void informPlayerWantToRestart() throws RemoteException;
    public void informPlayerGaveUp() throws RemoteException;

    /**This method sends a message to remote player that another player wants to start a match
     * */
    public Boolean informPlayerWantStartMatch(String name) throws RemoteException;

    public void informPlayerVictory() throws RemoteException;
    public void informPlayerDefeat() throws RemoteException;
    public void informDraw() throws RemoteException;


}
