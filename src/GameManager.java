import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


/**
 * Created by joseLucas on 23/04/17.
 */
public class GameManager extends UnicastRemoteObject implements GameActions {
    //private ComunicationManager comunicationM = ComunicationManager.getInstance();
    public GameActions remoteGA;

    private int[][] boardMatrix = new int [6][5];

    private GameStatus gameStatus = GameStatus.none;
    private MatchStatus matchStatus = MatchStatus.none;



    public GameUpdatesListener gameListener;
    public ComunicationListener cListener;

    //region Local player variables
    public String playerName = null;
    public Player player;
    //public Player remotePlayer;
    private Boolean myTurn = false;

    //endregion

    //sempre o jogador local suas pecas tem o valor um o remoto tem valor 2 e casas vazias valor 0

    public GameManager() throws RemoteException {
        super();
        boardMatrix = new int[6][5];
        //sendDM = ComunicationManager.getInstance();
    }

    public Player getRemotePlayer(){
        if(remoteGA != null){
            try {
                return remoteGA.getPlayer();
            } catch (RemoteException e) {
                System.out.println("Tentou pegar a instancia do jogador remoto e nao foi possivel, e melhor encerrar a conexao");
                e.printStackTrace();
            }
        }
        return null;
    }

    private void startMatch(){
        boardMatrix = new int[6][5];
        player = new Player(Main.cManager.isHost);
        updateMyTurnStatusTo(Main.cManager.isHost);
    }

    private void endMatch(){
        boardMatrix = new int[6][5];
        player = null;
        myTurn = false;
    }

    //region Getters and Setters

    public Boolean isMyTurn() {
        return myTurn;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public MatchStatus getMatchStatus() {
        return matchStatus;
    }


    //endregion

    public void updateMyTurnStatusTo(Boolean myTurn){
        if(this.myTurn != myTurn){
            this.myTurn = myTurn;
            player.extraAvailableCaptB = 0;//limpa a quantidade de blocos que se pode capturar para assim
            gameListener.updateForLocalUserTurn(this.myTurn);
        }
    }

    //region Moves and verifications methods

    public int getBlockAtPoint(Point pos){
        if(pos.x >= 0 && pos.x <= 4 && pos.y >= 0 && pos.y <= 5){
            return boardMatrix[pos.y][pos.x];
        }
        return 0;
    }

    private void setBlockAt(Point pos,int value){
        boardMatrix[pos.y][pos.x] = value;
    }

    private int numberOfBlocksOnBoard(int value){
        int quant = 0;
        for(int lin = 0; lin < boardMatrix.length ; lin ++){
            for(int col = 0; col < boardMatrix[lin].length ; col ++){
                int posValue = boardMatrix[lin][col];
                quant += posValue == value ? 1 : 0;

            }
        }
        return quant;
    }
    /**Returns true if the tapped blocks belongs to corresponding user
     * */
    public Boolean isAValidBlockToUser(Point pos){
        int value = getBlockAtPoint(pos);
        return value == this.player.blocksValue;
    }

    private Boolean isAFreeDestinyPos(Point pos){
        return getBlockAtPoint(pos) == 0;
    }

    public Boolean canMoveBlockFrom(Point initialPoint,Point finalPoint){
        if(initialPoint.y == finalPoint.y && initialPoint.y >= 0 && initialPoint.y <= 5){//horizontal move
            int dif = finalPoint.x - initialPoint.x;
            if(Math.abs(dif) == 1){
                return true;
            }
            else if (Math.abs(dif) == 2){//for moves that might pass over a block
                int newX = initialPoint.x + dif/2;
                if(newX >= 0 && newX <= 4){
                    Point midlePos = new Point(newX,initialPoint.y);
                    int value = getBlockAtPoint(midlePos);
                    return value != player.blocksValue && value != 0;
                }
            }
        }
        else if(initialPoint.x == finalPoint.x && initialPoint.x >= 0 && initialPoint.x <= 4){//vertical move
            int dif = finalPoint.y - initialPoint.y;
            if(Math.abs(dif) == 1){
                return true;
            }
            else if (Math.abs(dif) == 2){//for moves that might pass over a block
                int newY = initialPoint.y + dif/2;
                if(newY >= 0 && newY <= 5) {
                    Point midlePos = new Point(initialPoint.x, newY);
                    int value = getBlockAtPoint(midlePos);
                    return value != player.blocksValue && value != 0;
                }
            }
        }
        return false;
    }

    public Point blockToCaptWithMove(Point initialPoint,Point finalPoint){
        if(initialPoint.y == finalPoint.y){//horizontal move
            int dif = finalPoint.x - initialPoint.x;
            if (Math.abs(dif) == 2) {//for moves that might pass over a block
                Point middlePos = new Point(initialPoint.x + dif / 2, initialPoint.y);
                return middlePos;
            }
        }
        else if(initialPoint.x == finalPoint.x){//vertical move
            int dif = finalPoint.y - initialPoint.y;
            if (Math.abs(dif) == 2){//for moves that might pass over a block
                Point middlePos = new Point(initialPoint.x,initialPoint.y + dif/2);
                return middlePos;
            }
        }
        return null;
    }

    /**
     * Search for some move that can eat some enemy block
     * returns true if there is some possibility and false instead
     * */
    private Boolean checkCaptureMovesFor(int value){
        for(int lin = 0; lin < boardMatrix.length ; lin ++){
            for(int col = 0; col < boardMatrix[lin].length ; col ++){
                int posValue = boardMatrix[lin][col];
                if(posValue == value){
                    Point initialPoint = new Point(col,lin);
                    Point topMove = new Point(col - 2, lin);
                    Point bottomMove = new Point(col + 2, lin);
                    Point leftMove = new Point(col, lin - 2);
                    Point rightMove = new Point(col, lin + 2);

                    Boolean canCapt = canMoveBlockFrom(initialPoint,topMove);
                    canCapt = canCapt || canMoveBlockFrom(initialPoint,bottomMove);
                    canCapt = canCapt || canMoveBlockFrom(initialPoint,leftMove);
                    canCapt = canCapt || canMoveBlockFrom(initialPoint,rightMove);

                    if(canCapt){
                        return true;
                    }
                }

            }
        }
        return false;
    }

    /**Returns true if player or remotePlayer has some possibility to capture a block, false instead
     * */
    private Boolean isThereSomeCapture(){
        return checkCaptureMovesFor(player.blocksValue) || checkCaptureMovesFor(getRemotePlayer().blocksValue);
    }

    private Boolean isEmpate(){
        Player remotePlayer = getRemotePlayer();
        if(remotePlayer != null){
            if(player.getCapturedBlocks() >= 9 && remotePlayer.getCapturedBlocks() >= 9){
                return isThereSomeCapture();
            }
        }
        return false;
    }
    /**
     * This method checks if this is the end of game
     * if is returns the winner
    * */
    private Player checkForWinner(){
        Player remotePlayer = getRemotePlayer();
        if(player.getCapturedBlocks() == 12){
            return player;
        }
        else if(remotePlayer != null){
            if(remotePlayer.getCapturedBlocks() == 12) {
                return remotePlayer;
            }
        }
        return null;
    }

    //endregion


    /**
     * This method check and inform the players if its end of game, because some player is a winner or because its draw
     * */
    private Boolean checkEndOfGame(){
        Boolean success = false;
        if(isEmpate()){
            updateMatchStatusTo(MatchStatus.finished);
            try {
                remoteGA.informDraw();
                success = true;
            } catch (RemoteException e) {}
            cListener.showAlertWith("EMPATE","A partida terminou empatada, por falta de jogadas");

        }
        else{
            Player winnner = checkForWinner();
            if(winnner != null){
                if(winnner.equals(player)){
                    updateMatchStatusTo(MatchStatus.finished);
                    try {
                        remoteGA.informPlayerDefeat();
                        success = true;
                    } catch (RemoteException e) {}
                    cListener.showAlertWith("VITORIA","PARABENS "+playerName);
                }
                else{
                    updateMatchStatusTo(MatchStatus.finished);
                    try {
                        remoteGA.informPlayerVictory();
                        success = true;
                    } catch (RemoteException e) {}
                    cListener.showAlertWith("DERROTA","Uma pena mas você PERDEU "+playerName);
                }
            }
        }
        return success;
    }

    //-----------------------------


    /**
     * return true if need updates
     * */
    public Boolean didClickedOnPos(Point pos){
        Boolean success = false;
        if(isAFreeDestinyPos(pos)){
            if(player.selectedBlockPoint == null){// add a new block on board
                try{
                    remoteGA.addNewBlock(player.blocksValue,pos);
                    addNewBlock(player.blocksValue,pos);
                    success = true;
                }
                catch (Exception e){
                    System.out.println("Erro ao tentar add o bloco didClickedOnPos");
                }
            }
            else{//move the selected block to 'pos'
                if(canMoveBlockFrom(player.selectedBlockPoint,pos)){

                    try {
                        remoteGA.moveBlockFrom(player.selectedBlockPoint,pos);
                        moveBlockFrom(player.selectedBlockPoint,pos);
                        success = true;
                    } catch (RemoteException e) {
                        System.out.println("Erro ao tentar mover o bloco didClickedOnPos");
                    }
                }
                System.out.println("Move selected player block to this position");
            }
        }
        else if(!isAValidBlockToUser(pos)) {//tapped on other player block
            if(player.extraAvailableCaptB > 0){
                try {
                    remoteGA.rmBlockAt(pos);
                    rmBlockAt(pos);
                    success = true;
                } catch (RemoteException e) {
                    System.out.println("Erro ao tentar remover o bloco didClickedOnPos");
                }
            }
            //check if can get this block based on last move
        }
        else if(player.extraAvailableCaptB == 0){//tapped on his own block and to enable it he must not have block to get
            if(player.selectedBlockPoint != null && player.selectedBlockPoint.equals(pos)){
                player.selectedBlockPoint = null;
            }
            else{
                player.selectedBlockPoint = pos;
            }
            this.gameListener.updateBoard();

            try {
                this.remoteGA.informPlayerToUpdateBoard();
                success = true;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            //prepare to move the block
        }
        //success = checkEndOfGame();
        return success;
    }





    public void restartMatch(){
        try{
            if(Main.gManager.remoteGA.mStatus() == MatchStatus.waitingToStart){//Player is only waiting my answer
                this.remoteGA.informPlayerWantToRestart();
                this.updateMatchStatusTo(MatchStatus.inProgress);
            }
            else{
                this.remoteGA.informPlayerWantToRestart();
                this.updateMatchStatusTo(MatchStatus.waitingToStart);
            }
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println("Erro em retartMatch GameManager");
            cListener.showAlertWith("ALERTA","Nao foi possivel se comunicar com o outro jogador");
        }
    }

    public void quitMatch(){
        try{
            if(remoteGA != null){
                this.remoteGA.informPlayerIsQuiting();
            }
            this.updateGameStatusTo(GameStatus.none);
            Main.cManager.restartCM();
            this.remoteGA = null;
        }
        catch(Exception e){
            System.out.println("Erro em quitMatch GameManager");
        }
    }

    public void giveUpMatch(){
        try{
            this.remoteGA.informPlayerGaveUp();
            this.updateMatchStatusTo(MatchStatus.finished);
        }
        catch(Exception e){
            System.out.println("Erro em giveUpMatch GameManager");
        }
    }

//------------------------------ GAMEACTIONS METHODS
    public Player getPlayer() throws  RemoteException{
        return player;
    }


    public String getPlayerName() throws RemoteException{
        return this.playerName;
    }

    public MatchStatus mStatus()throws RemoteException{
        return this.matchStatus;
    }



    /**
     * Do not call this method locally this is only for remote invocations
     * */
    @Override
    public Boolean connectWith(String name) {
        Main.cManager.isHost = true;
        this.myTurn = true;// o anfitriao comeca.
        GameActions rGA = Main.cManager.connectToPlayer(name);
        if(rGA != null){
            this.remoteGA = rGA;
            try {
                this.updateGameStatusTo(GameStatus.onParty);
            } catch (Exception e) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void showChatMessage(String playerName, String message) throws RemoteException{
        MessageType type = playerName.equalsIgnoreCase(this.playerName) ? MessageType.localPlayer : MessageType.otherPlayer;
        gameListener.receivedNewChatMessage(playerName,message,type);
    }


    @Override
    public void addNewBlock(int value, Point point) throws RemoteException{
        Player player = value == this.player.blocksValue ? this.player : getRemotePlayer();

        if(player.getBlocksToAdd() > 0 && isAFreeDestinyPos(point)){
            setBlockAt(point,value);
            player.addNewBlockOnBoard();
            Boolean isMyTurn = value == this.player.blocksValue ? true : false;
            updateMyTurnStatusTo(!isMyTurn);
            gameListener.updateBoard();
        }
    }
    public void rmBlockAt(Point point) throws RemoteException{
        Boolean isMyBlock = this.getBlockAtPoint(point) == this.player.blocksValue ? true : false;

        if(isMyBlock){// I lost a block
            updateMyTurnStatusTo(getRemotePlayer().extraAvailableCaptB <= 1);//if 1 it will be removed if 0 it was removed
            gameListener.updateBoard();
        }
        else{// I got this block
            player.extraAvailableCaptB --;
            this.player.captureABlock();
            updateMyTurnStatusTo(player.extraAvailableCaptB > 0);
        }

        setBlockAt(point,0);
        gameListener.updateBoard();
    }

    @Override
    public void moveBlockFrom(Point initialPos,Point finalPos) throws RemoteException{
        int value = getBlockAtPoint(initialPos);
        setBlockAt(initialPos,0);
        setBlockAt(finalPos,value);


        Boolean isMyTurn = value == this.player.blocksValue ? true : false;
        Boolean keepMyTurn = !isMyTurn;
        if(isMyTurn){
            Point blockToCapt = blockToCaptWithMove(initialPos,finalPos);
            if(blockToCapt != null){//do not finish my turn and enable to make another capt move or capture extra block and finish my turn
                //call remove block method
                player.extraAvailableCaptB += this.numberOfBlocksOnBoard(getRemotePlayer().blocksValue) > 1 ? 2 : 1;
                remoteGA.rmBlockAt(blockToCapt);
                rmBlockAt(blockToCapt);
                keepMyTurn = player.extraAvailableCaptB > 0;
            }
        }
        //If there is not another block to get after capture move his turn has to end
        updateMyTurnStatusTo(keepMyTurn);
        //updateMyTurnStatusTo(!isMyTurn);
        gameListener.updateBoard();
        player.selectedBlockPoint = null;

    }

    //These methods must be called remotelly not locally

    public void informPlayerToUpdateBoard() throws RemoteException{
        this.gameListener.updateBoard();
    }


    public void informPlayerIsQuiting() throws RemoteException{
        String playerName = this.remoteGA.getPlayerName();
        InBackground.execute(integer -> {
            this.cListener.showAlertWith("ALERTA",playerName+" deixou a partida");
            this.updateGameStatusTo(GameStatus.none);
            Main.cManager.restartCM();
            this.remoteGA = null;
            return false;
        });
    }
    public void informPlayerWantToRestart() throws RemoteException{
        if(this.matchStatus == MatchStatus.waitingToStart){
            this.updateMatchStatusTo(MatchStatus.inProgress);
        }
        else{
            String playerName = this.remoteGA.getPlayerName();
            InBackground.execute(integer -> {
                this.cListener.showAlertWith("INFO",playerName+" está pronto para recomeçar");
                return false;
            });
        }
    }
    public void informPlayerGaveUp() throws RemoteException{
        String playerName = this.remoteGA.getPlayerName();
        InBackground.execute(integer -> {
            this.cListener.showAlertWith("INFO",playerName+" desistiu.\nEntão parabéns VENCEDOR");
            return false;
        });
        this.updateMatchStatusTo(MatchStatus.finished);
    }

    public Boolean informPlayerWantStartMatch(String name) throws RemoteException{
        return cListener.showConfirmAlertWith("Info",name+" gostaria de começar uma partida com você");
    }

    public void informPlayerVictory() throws RemoteException{
        updateMatchStatusTo(MatchStatus.finished);
        InBackground.execute(integer -> {
            cListener.showAlertWith("VITORIA","PARABENS "+playerName);
            return false;
        });
    }

    public void informPlayerDefeat() throws RemoteException{
        updateMatchStatusTo(MatchStatus.finished);
        InBackground.execute(integer -> {
            cListener.showAlertWith("DERROTA","Uma pena mas você PERDEU "+playerName);
            return false;
        });
    }

    public void informDraw() throws RemoteException{
        updateMatchStatusTo(MatchStatus.finished);
        InBackground.execute(integer -> {
            cListener.showAlertWith("EMPATE","A partida terminou empatada, por falta de jogadas");
            return false;
        });
    }

    //Methods that probably not necessary to be on GameActions Interface

    public void updateGameStatusTo(GameStatus status){
        if (this.gameStatus != status){

            MatchStatus s = MatchStatus.none;
            if(status == GameStatus.onParty){
                s = MatchStatus.inProgress;
            }
            else{
                s = MatchStatus.none;
            }

            this.updateMatchStatusTo(s);
            this.gameStatus = status;
            gameListener.gameStatusChangedTo(gameStatus);
        }
    }

    public void updateMatchStatusTo(MatchStatus status){
        if (this.matchStatus != status){

            if(status == MatchStatus.inProgress || status == MatchStatus.waitingToStart){
                startMatch();
            }
            else{
                endMatch();
            }

            this.matchStatus = status;
            gameListener.matchStatusChangedTo(this.matchStatus);
        }
    }
}
