/**
 * Created by joseLucas on 24/04/17.
 */
public interface GameUpdatesListener {


    public void matchStatusChangedTo(MatchStatus matchStatus);
    public void gameStatusChangedTo(GameStatus gameStatus);
    public void updateForLocalUserTurn(Boolean turn);
    public void updateBoard();

    public void receivedNewChatMessage(String playerName,String message,MessageType type);

}
