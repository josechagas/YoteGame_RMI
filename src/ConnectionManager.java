import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * Created by joseLucas on 20/05/17.
 */
public class ConnectionManager {

    private String serverName = "localhost:1099";
    //private String port = "1021";

    public Boolean isConnected = false;

    public Boolean isHost = false;

    ConnectionManager(String namingServer){
        serverName = namingServer;
    }

    ConnectionManager(){
        try {
            LocateRegistry.createRegistry(1099);
        } catch (Exception e) {
            //e.printStackTrace();
            /*try {
            } catch (Exception e2) {
                e2.printStackTrace();
            }*/
        }
    }

    /**
     * This method returns this class to its default properties values
     * */
    public void restartCM(){
        isHost = false;
    }

    public Boolean validateNickname(String name){
        String [] players = connectedPlayers();
        for(String player : players){
            String playerName = player.replace("//"+serverName+"/","");
            return !playerName.equalsIgnoreCase(name);
        }
        return true;
    }

    public Boolean isThereConnectedPlayers(){
        return connectedPlayers().length > 0;
    }

    public String[] connectedPlayers(){
        try{
            return tryToGetConnectedPlayers();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return new String[0];
    }

    public GameActions connectToPlayer(String name){
        try {
            return this.tryToConnectToPlayer(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Boolean connectAsServer(String name){
        try {
            this.tryToConnectAsServer(name);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        isConnected = true;
        return true;
    }

    public GameActions searchForMatch(){
        String [] players = connectedPlayers();
        for (String player: players) {
            String playerName = player.replace("//"+serverName+"/","");
            if(!playerName.equalsIgnoreCase(Main.gManager.playerName)){
                GameActions rGA = this.connectToPlayer(playerName);
                try {
                    /**
                     * If user is not on a match or waiting it to start
                     * */
                    MatchStatus s = rGA.mStatus();
                    if(rGA.mStatus() != MatchStatus.inProgress && rGA.mStatus() != MatchStatus.waitingToStart){
                        return rGA;
                    }
                }
                catch (Exception e){

                }
            }
        }
        return null;
    }

    private String[] tryToGetConnectedPlayers() throws MalformedURLException, RemoteException {
        String[] players = Naming.list("//"+serverName+"/");
        return players;
    }

    private void tryToConnectAsServer(String name) throws RemoteException, MalformedURLException {
        Naming.rebind("//"+serverName+"/"+name,Main.gManager);
    }

    private GameActions tryToConnectToPlayer(String name) throws RemoteException, MalformedURLException {
        try {
            return (GameActions) Naming.lookup("//"+serverName+"/"+name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

/*
    public Boolean tryToConnect(){
        try {
            if(connectedPlayers().length > 0){// ja tem player la esperando o outro jogador
                isHost = false;
                Main.gManager.remoteGame = (GameAction) Naming.lookup("//127.0.0.1:1020/PlayerOne");
                Naming.rebind("//"+serverName+"/PlayerTwo",Main.gManager);
                Boolean connected = Main.gManager.remoteGame.connect();
                return connected;
            }
            else{//ainda nao tem players esperando
                isHost = true;
                Naming.rebind("//"+serverName+"/PlayerOne",Main.gManager);
                Main.gManager.updateGameStatusTo(GameStatus.lookingForPlayer,true);
                return true;
            }
        }
        catch (Exception e){
            System.out.println("Erro no metodo tryToConnect");
            e.printStackTrace();
        }
        return false;
    }

    public Boolean closeConnection(){
        String name = Main.gManager.player.blocksValue == 1 ? "PlayerOne" : "PlayerTwo";
        try {
            Naming.unbind("//"+serverName+"/"+name);
            return true;
        } catch (Exception e){
            System.out.println("Erro no metodo closeConnection");
            e.printStackTrace();
        }

        return false;
    }
*/
}
