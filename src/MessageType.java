import java.awt.*;

/**
 * Created by joseLucas on 22/04/17.
 */
public enum MessageType {

    localPlayer(0),otherPlayer(1),system(2),error(3);

    private final int value;

    public int getValue(){
        return value;
    }


    MessageType(int value){
        this.value = value;
    }


    public Color messageColor(){
        switch (value){
            case 0:
                return Color.BLACK;
            case 1:
                return Color.BLACK;
            case 2:
                return Color.gray;
            case 3:
                return Color.red;
            default:
                return Color.BLACK;
        }
    }

    public Color titleColor(){
        switch (value){
            case 0:
                return Color.blue;
            case 1:
                return Color.ORANGE;
            case 2:
                return Color.gray;
            case 3:
                return Color.red;
            default:
                return Color.BLACK;
        }
    }
}
