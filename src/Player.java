import java.awt.*;
import java.io.Serializable;

/**
 * Created by joseLucas on 24/04/17.
 */
public class Player implements Serializable {


    private int blocksToAdd = 12; //the number of blocks that he can add to board
    private int capturedBlocks = 0;
    public int blocksValue = 1;

    public Point selectedBlockPoint = null;

    public int extraAvailableCaptB = 0;

    Player(Boolean isHost){
        blocksValue = isHost ? 1 : 2;
    }

    public int getBlocksToAdd() {
        return blocksToAdd;
    }

    public int getCapturedBlocks() {
        return capturedBlocks;
    }

    public void addNewBlockOnBoard(){
        blocksToAdd--;
    }

    public void captureABlock(){
        capturedBlocks++;
    }


}


