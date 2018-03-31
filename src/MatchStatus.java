/**
 * Created by joseLucas on 25/04/17.
 */
public enum MatchStatus {

    //waitingToStart this status appear when a user wants to start a new match and keep waiting other player to confirm to start the match

    none(0),inProgress(1),finished(2),waitingToStart(3);

    private final int value;

    MatchStatus(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }
}
