import java.awt.*;

/**
 * Created by joseLucas on 23/04/17.
 */
public class Block {

    private int value;//its 1 or 2 depends of state
    private Point initialPos;//its first position, not on board pos
    private Point currentPos; //its actual position, not on board pos
    private int side;
    //
    public Block(int value, Point initialPos,int side){
        this.value = value;
        this.initialPos = initialPos;
        this.currentPos = initialPos;
        this.side = side;
    }


    public Point getInitialPos() {
        return initialPos;
    }

    public Point getCurrentPos() {
        return currentPos;
    }

    public void drawWith(Graphics g,Point currentPos,Boolean isSelected){
        //g.drawOval(currentPos.x,currentPos.y,side,side);

        g.setColor(value == 1  ? Color.white : Color.black);

        if(isSelected){
            Color selectedColor = new Color(g.getColor().getRed(),g.getColor().getGreen(),g.getColor().getBlue(),125);
            g.setColor(selectedColor);
        }

        if(value == 1){//circle
            g.fillOval(currentPos.x,currentPos.y,side,side);
        }
        else{//rect
            g.fillRoundRect(currentPos.x,currentPos.y,side,side,side/4,side/4);
        }
    }
}
