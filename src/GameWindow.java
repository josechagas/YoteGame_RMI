import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.rmi.RemoteException;

/**https://www.lifewire.com/display-hidden-files-in-os-x-153332
 * Created by joseLucas on 15/04/17.
 */
public class GameWindow implements ComunicationListener,GameUpdatesListener {

    private JPanel contentPanel;
    private JPanel yotePanel;

    private JTextField messageTF;
    private JTextPane chatTextPane;
    private JButton createPartyButton;
    private JLabel connectionLabel;
    private JTextField playerNameTF;
    private JLabel playerTurnLabel;
    private JButton giveUpButton;


    public  GameWindow(){
        setUpMessageTF();
        setUpCreatePartyButton();
        setUpPlayerNameTF();
        setUpGiveUpButton();
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }


    private void createUIComponents() {
        yotePanel = new YoteBoardPanel();
        // TODO: place custom component creation code here
    }

    public void showAskUserNameDialog(){

        String answer = JOptionPane.showInputDialog(this.contentPanel,"Seu nickname",null,JOptionPane.QUESTION_MESSAGE);
        if(answer != null){
            if(answer.length() < 2){
                this.showAlertWith("ALERTA","O nick dever ter no minimo 2 caracter");
                showAskUserNameDialog();
            }
            else if(!Main.cManager.validateNickname(answer)){
                this.showAlertWith("ALERTA","O nick "+answer+" já esta em uso");
                showAskUserNameDialog();
            }
            else{
                Main.gManager.playerName = answer;
                this.playerNameTF.setText(answer);
            }
        }
    }

    //region MessageTF methods

    private void setUpMessageTF(){
        messageTF.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);

                if(e.getKeyChar() == '\n'){//send the message
                    //chatTextPane.setText(chatTextPane.getText()+"\n\n"+messageTF.getText());
                    //call messages socket to send the message
                    Boolean success = true;
                    try {
                        if(Main.gManager.remoteGA != null){
                            Main.gManager.remoteGA.showChatMessage(Main.gManager.playerName,messageTF.getText());
                        }
                    } catch (Exception e1) {
                        success = false;
                        e1.printStackTrace();
                    }

                    if(success){
                        showMessage(Main.gManager.playerName,messageTF.getText(),MessageType.localPlayer);
                        messageTF.setText(null);
                    }
                    else{
                        showMessage("ERRO","Não foi possivel enviar sua mensagem !",MessageType.error);
                    }
                }
            }
        });
    }

    private Boolean showMessage(String playerName, String message,MessageType type) {
        StyledDocument doc = chatTextPane.getStyledDocument();

        Style titleStyle = chatTextPane.addStyle("title", null);
        Style messageStyle = chatTextPane.addStyle("message", null);

        //color is accordingly to player
        Color titleColor = type.titleColor();
        Color messageColor = type.messageColor();

        try {
            //title
            int lenght = doc.getLength();
            StyleConstants.setForeground(titleStyle, titleColor);
            String text;
            if(playerName != null){//Its an error message
                text = (lenght > 0 ? "\n\n" : "") + playerName + ": ";
            }
            else{
                text = (lenght > 0 ? "\n\n" : "");
            }

            if(type == MessageType.system){
                text = text.toUpperCase();
                message = message.toUpperCase();
            }

            doc.insertString(lenght, text, titleStyle);

            //message
            StyleConstants.setForeground(messageStyle, messageColor);
            doc.insertString(doc.getLength(), message, messageStyle);
            return true;
        } catch (BadLocationException e) {
            return false;
        }
    }

    //endregion

    private void setUpPlayerNameTF(){
        if(playerNameTF.getText().isEmpty()){
            Main.gManager.playerName = playerNameTF.getText();
        }

        playerNameTF.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                if(e.getKeyChar() == '\n'){
                    //ComunicationManager.getInstance().setPlayerName(playerNameTF.getText());
                    createPartyButton.requestFocus();
                }
            }
        });
    }

    //region NewMatchButton methods
    private void setUpCreatePartyButton(){
        createPartyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(Main.gManager.getGameStatus() == GameStatus.onParty) {
                    //its on a match
                    String[] options = new String[] {"Sair", "Cancelar"};

                    String message = "Deseja Continuar ?";

                    int response = JOptionPane.showConfirmDialog(contentPanel,message,"Abandonar Partida",JOptionPane.YES_NO_OPTION,JOptionPane.PLAIN_MESSAGE);

                    if(response == 0){
                        if(Main.gManager.getMatchStatus() != MatchStatus.none && Main.gManager.getGameStatus() == GameStatus.onParty){
                            Main.gManager.quitMatch();
                        }
                        //ComunicationManager.getInstance().closeConnection(true);
                    }
                }
                else if(Main.gManager.getGameStatus() == GameStatus.lookingForPlayer){
                    System.out.println("Faz alguma coisa para cancelar a busca por partida");
                    //cancelSearchForMatch();
                    //ComunicationManager.getInstance().closeConnection(true);
                }
                else{//tenta se conectar
                    if(Main.cManager.isConnected){
                        InBackground.execute(integer -> {
                            connectToPlayerProcess();
                            return false;
                        });
                    }
                    else{
                        connectAsServerProcess();
                    }

                }
            }
        });
    }

    private void connectToPlayerProcess(){
        Main.gManager.updateGameStatusTo(GameStatus.lookingForPlayer);

        GameActions rGA = Main.cManager.searchForMatch();
        String message = "Conexao estabelecida";
        String title = "Sucesso";
        Boolean success = false;
        if(rGA != null){
            Main.gManager.updateMatchStatusTo(MatchStatus.waitingToStart);
            try {
                success = rGA.informPlayerWantStartMatch(Main.gManager.playerName);
                if (success) {
                    try {
                        if (rGA.connectWith(Main.gManager.playerName)) {
                            Main.gManager.remoteGA = rGA;
                            Main.gManager.updateGameStatusTo(GameStatus.onParty);
                            message = "O " + rGA.getPlayerName() + " entrou na partida";
                            success = true;
                        } else {
                            Main.gManager.updateGameStatusTo(GameStatus.none);
                            title = "Erro";
                            message = "Nao foi possivel conectar-se com " + rGA.getPlayerName();
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    Main.gManager.updateGameStatusTo(GameStatus.none);
                    title = "INFO";
                    message = "Nao foi possivel encontrar uma partida";
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                Main.gManager.updateGameStatusTo(GameStatus.none);
                title = "Erro";
                message = "Ocorreu um erro inesperado";
            }
        }
        else{
            Main.gManager.updateGameStatusTo(GameStatus.none);
            title = "INFO";
            message = "Nao foi possivel encontrar uma partida";

        }
        Main.gManager.updateGameStatusTo(success ? GameStatus.onParty : GameStatus.none);
        JOptionPane.showMessageDialog(contentPanel,message,title,JOptionPane.PLAIN_MESSAGE);

    }

    public void connectAsServerProcess(){
        if(Main.gManager.playerName == null || Main.gManager.playerName.length() <= 1){
            showAskUserNameDialog();
        }
        connectionLabel.setText("Conectando ...");
        Boolean success = Main.cManager.connectAsServer(Main.gManager.playerName);
        String message = success ? "Conexao estabelecida" : "Nao foi possivel conectar-se ao servidor";
        String title = success ? "Sucesso" : "Erro";
        this.gameStatusChangedTo(GameStatus.none);
        JOptionPane.showMessageDialog(contentPanel,message,title,JOptionPane.PLAIN_MESSAGE);
    }

    //endregion

    private void updateGiveUpButtonFor(MatchStatus status){
        giveUpButton.setForeground(status == MatchStatus.inProgress ? Color.red : Color.black);
        giveUpButton.setText(status != MatchStatus.finished ? "DESISTIR" : "Jogar Novamente");

        Boolean shouldPresent = status != MatchStatus.none && status != MatchStatus.waitingToStart ? true : false;
        giveUpButton.setVisible(shouldPresent);
        giveUpButton.setEnabled(shouldPresent);
    }

    private void setUpGiveUpButton(){
        giveUpButton.setVisible(false);
        giveUpButton.setEnabled(false);
        giveUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(Main.gManager.getMatchStatus() == MatchStatus.inProgress) {//give up
                    String message = "Deseja Continuar ?";

                    int response = JOptionPane.showConfirmDialog(contentPanel,message,"Desistir",JOptionPane.YES_NO_OPTION,JOptionPane.PLAIN_MESSAGE);
                    if(response == 0){
                        Main.gManager.giveUpMatch();
                    }
                }
                else{//play again
                    Main.gManager.restartMatch();
                }
            }
        });
    }

    //region ComunicationListener methods
    @Override
    public void showChatAlertMessage(String title, String message) {
        this.showMessage(title,message,MessageType.error);
    }

    @Override
    public void showAlertWith(String title, String message) {
        JOptionPane.showMessageDialog(this.contentPanel,message,title,JOptionPane.PLAIN_MESSAGE);
    }

    @Override
    public void updateInfoLabel(String text) {
        this.connectionLabel.setText(text);
    }

    @Override
    public Boolean showConfirmAlertWith(String title, String message) {
        int choice = JOptionPane.showConfirmDialog(this.contentPanel,message,title,JOptionPane.YES_NO_OPTION,JOptionPane.PLAIN_MESSAGE);
        return choice == 0;
    }

    //endregion

    //region GameUpdatesListener methods
    @Override
    public void receivedNewChatMessage(String playerName, String message,MessageType type) {
        System.out.println("Chat Message received on GameWindow by ReceivedDataListener");
        showMessage(playerName,message,type);
    }

    @Override
    public void updateBoard() {
        YoteBoardPanel panel = (YoteBoardPanel)yotePanel;
        panel.redrawUI();
    }

    @Override
    public void matchStatusChangedTo(MatchStatus matchStatus) {
        updateGiveUpButtonFor(matchStatus);
        updateBoard();

        if(matchStatus == MatchStatus.inProgress){
            if(playerNameTF.getText().isEmpty()){
                playerNameTF.setText(Main.gManager.playerName);
            }
            playerNameTF.setEnabled(false);
            yotePanel.setEnabled(Main.gManager.isMyTurn());

            updateForLocalUserTurn(Main.gManager.isMyTurn());//to show turn label
        }
        else if(matchStatus == MatchStatus.waitingToStart){
            if(playerNameTF.getText().isEmpty()){
                playerNameTF.setText(Main.gManager.playerName);
            }
            playerNameTF.setEnabled(false);
            yotePanel.setEnabled(false);
            playerTurnLabel.setText(Main.gManager.getGameStatus() == GameStatus.onParty ? "Esperando por confirmação" : null);
        }
        else{
            playerNameTF.setEnabled(true);
            this.playerTurnLabel.setText("");
            yotePanel.setEnabled(false);
        }
    }

    @Override
    public void gameStatusChangedTo(GameStatus gameStatus) {
        /*if(!(playerNameTF.getText().toLowerCase().equalsIgnoreCase(ComunicationManager.getInstance().getPlayerName().toLowerCase()))){
            playerNameTF.setText(ComunicationManager.getInstance().getPlayerName());
        }*/

        if(gameStatus == GameStatus.onParty || gameStatus == GameStatus.lookingForPlayer){

            this.chatTextPane.setText("");
            this.connectionLabel.setText( gameStatus == GameStatus.lookingForPlayer ? "Buscando partida ..." : null);

            createPartyButton.setText(gameStatus == GameStatus.lookingForPlayer ? "Cancelar":"Sair");

            createPartyButton.setEnabled(gameStatus == GameStatus.lookingForPlayer ? false : true);
            createPartyButton.setVisible(gameStatus == GameStatus.lookingForPlayer ? false : true);


            //createPartyButton.setText(gameStatus == GameStatus.lookingForPlayer && !Main.cManager.isHost ? "Cancelar":"Sair");
            //createPartyButton.setEnabled(gameStatus == GameStatus.lookingForPlayer && !Main.cManager.isHost? false : true);

        }
        else{
            this.connectionLabel.setText(null);
            this.playerTurnLabel.setText("");
            this.chatTextPane.setText("");
            playerNameTF.setEnabled(true);
            yotePanel.setEnabled(false);
            updateBoard();

            String text = Main.cManager.isConnected ? "Buscar partida" : "Conectar";
            createPartyButton.setText(text);
            createPartyButton.setEnabled(true);
            createPartyButton.setVisible(true);
        }
    }

    @Override
    public void updateForLocalUserTurn(Boolean turn) {
        yotePanel.setEnabled(turn);
        String message = turn ? "Sua vez" : "Aguardando outro jogador";
        playerTurnLabel.setText(message);
    }
    //endregion

    //region Connection UI methods

    /**
     * This method call the dialog that asks if you want create a new party or connect to one
     * */
    public void requestConnection(String message){
        ConnectionDialog.presentRelativeTo(contentPanel,message);
    }

    //endregion

    /*
    public void showOptionPaneWith(String title, String message) {
        JOptionPane.showMessageDialog(this.contentPanel,message,title,JOptionPane.OK_OPTION);
        //int result = JOptionPane.showConfirmDialog(this.contentPanel,null, "ScreenPreview", JOptionPane.OK_OPTION,JOptionPane.PLAIN_MESSAGE);
    }
    */
}
