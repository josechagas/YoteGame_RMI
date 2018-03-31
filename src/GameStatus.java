/**
 * Created by joseLucas on 23/04/17.
 */
public enum GameStatus {

    none(0), onParty(1), lookingForPlayer(2);

    private final int value;

    GameStatus(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }
}
