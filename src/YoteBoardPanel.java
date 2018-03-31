import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created by joseLucas on 16/04/17.
 */
public class YoteBoardPanel extends JPanel {

    private Point boardTopLeft;
    private int squareSide;
    private Graphics boardGraphics;

    public YoteBoardPanel(){
        /*this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                Point initalPos = convertToBoardCoords(e.getPoint());

                if(Main.gManager.isAValidBlockToUser(initalPos,true)){

                }

            }
        });
*/
        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(isEnabled()){
                    System.out.println("clicked");
                    Point pos = convertToBoardCoords(e.getPoint());
                    if(Main.gManager.didClickedOnPos(pos)){
                        redrawUI();
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        //Color c = this.getBackground();
        //int a = enabled ? 255 : 120;
        //this.setBackground(new Color(c.getRed(),c.getGreen(),c.getBlue(),a));
    }

    private Point convertToBoardCoords(Point point){
        int col = (point.x - boardTopLeft.x)/squareSide;
        int line = (point.y - boardTopLeft.y)/squareSide;
        col = col < 0 ? 0 : (col > 5 ? 5 : col);
        line = line < 0 ? 0 : (line > 6 ? 6 : line);
        return new Point(col,line);
    }

    private Point convertFromBoardCoords(Point pos,int blockSide){
        int sideOffset = squareSide - blockSide;
        int posX = pos.x*squareSide + boardTopLeft.x + sideOffset/2;
        int posY = pos.y*squareSide + boardTopLeft.y + sideOffset/2;
        return new Point(posX,posY);
    }


    //region Draw methods

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        //System.out.println("Passed");
        boardGraphics = g.create();
        //System.out.println(boardGraphics);
        paintBoard();
        paintPlayerInfo();
        drawBlocksOnBoard();
    }

    public void redrawUI(){
        paintBoard();
        drawBlocksOnBoard();
        paintPlayerInfo();
        //this.paint(boardGraphics);
        this.repaint();
    }

    private void paintString(Point p,String title){
        int size = title.toCharArray().length;
        boardGraphics.drawChars(title.toCharArray(),0,size,p.x,p.y);
    }

    private void drawAvailableBlocks(Point point){
        Integer availableBlocks = new Integer(12);
        if(Main.gManager.getMatchStatus() == MatchStatus.inProgress){
            availableBlocks = new Integer(Main.gManager.player.getBlocksToAdd());
        }
        paintString(point,"Disponiveis: "+availableBlocks.toString());
    }

    private void capturedBlocks(Point point){
        Integer captured = new Integer(0);
        if(Main.gManager.getMatchStatus() == MatchStatus.inProgress){
            captured = new Integer(Main.gManager.player.getCapturedBlocks());
        }
        paintString(point,"Conquistadas: "+captured.toString());
    }

    private void lostBlocks(Point point){
        Integer lost = new Integer(0);
        if(Main.gManager.getMatchStatus() == MatchStatus.inProgress){
            Player remotePlayer = Main.gManager.getRemotePlayer();
            if(remotePlayer != null){
                lost = new Integer(remotePlayer.getCapturedBlocks());
            }
        }
        paintString(point,"Perdidas: "+lost.toString());
    }

    private void paintPlayerInfo(){
        Point point = new Point(boardTopLeft.x - 30,boardTopLeft.y+squareSide*6 + 35);
        paintString(point,"PEÇAS");
        drawAvailableBlocks(new Point(boardTopLeft.x + 20,boardTopLeft.y+squareSide*6 + 15));
        capturedBlocks(new Point(boardTopLeft.x + 20,boardTopLeft.y+squareSide*6 + 35));
        lostBlocks(new Point(boardTopLeft.x + 20,boardTopLeft.y+squareSide*6 + 55));
    }

    private void paintBoard(){
        int verticalSquares = 6;
        int horizontalSquares = 5;

        int baseValue = (this.getWidth() <= this.getHeight() ? this.getWidth() : this.getHeight()) - 20;


        squareSide = baseValue/6;
        Point offSet = new Point((this.getWidth() - squareSide*horizontalSquares)/2, (this.getHeight() - squareSide*verticalSquares)/2);
        Point firstPos = new Point();

        boardTopLeft = offSet;

        for(int y = 0; y < verticalSquares; y++){
            for(int x = 0; x < horizontalSquares; x++){
                int newX = firstPos.x + offSet.x + squareSide*x;
                int newY = firstPos.y + offSet.y + squareSide*y;
                boardGraphics.drawRect(newX,newY,squareSide,squareSide);
            }
        }
    }

    private void drawBlocksOnBoard(){
        for(int line=0;line <6;line ++){
            for(int col=0;col<5;col++){
                int value = Main.gManager.getBlockAtPoint(new Point(col,line));
                if(value > 0){
                    Point boardCoords = new Point(col,line);
                    Point pos = convertFromBoardCoords(boardCoords,squareSide - 10);
                    Block block = new Block(value,pos,squareSide - 10);

                    Boolean isSelected = false;
                    if(Main.gManager.getMatchStatus() == MatchStatus.inProgress){
                        Player remotePlayer =  Main.gManager.getRemotePlayer();
                        Player localPlayer = Main.gManager.player;
                        if(localPlayer != null && localPlayer.selectedBlockPoint != null && value == localPlayer.blocksValue){
                            isSelected = localPlayer.selectedBlockPoint.equals(boardCoords);
                            //System.out.println(Main.gManager.player.selectedBlockPoint.toString()+" "+boardCoords.toString()+"  "+isSelected.toString());
                        }
                        else if(remotePlayer != null){
                            if(remotePlayer.selectedBlockPoint != null){
                                isSelected = remotePlayer.selectedBlockPoint.equals(boardCoords);
                            }
                        }
                    }
                    block.drawWith(boardGraphics,pos,isSelected);
                }

            }
        }
    }
}
