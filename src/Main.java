import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by joseLucas on 15/04/17.
 */
public class Main {

    public static GameManager gManager;// = new GameManager();

    public static ConnectionManager cManager;

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        cManager = new ConnectionManager();



        try{
            gManager = new GameManager();
            startGame();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }


    public static void startGame(){
        //ComunicationManager.getInstance();

        GameWindow gWindow = new GameWindow();

        JFrame appFrame = new JFrame();
        appFrame.setContentPane(gWindow.getContentPanel());
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        appFrame.setSize(700,660);
        appFrame.setResizable(false);
        //appFrame.pack();//resize it to fit all its content
        appFrame.setVisible(true);

        appFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.out.println("Faz alguma coisa para sair da partida");
                if(Main.gManager.getMatchStatus() != MatchStatus.none && Main.gManager.getGameStatus() == GameStatus.onParty){
                    Main.gManager.quitMatch();
                }
            }
        });

        //ComunicationManager.getInstance().newsListener = gWindow;
        gManager.gameListener = gWindow;
        gManager.cListener = gWindow;


        gWindow.showAskUserNameDialog();
        gWindow.connectAsServerProcess();

        //cManager = new ConnectionManager();

        //Boolean connected = cManager.tryToConnect();



        //ConnectionDialog dialog = new ConnectionDialog();
        //JOptionPane.showMessageDialog(appFrame, "Java Technolgy Dive Log", "Dive", JOptionPane.INFORMATION_MESSAGE, null);
        //int result = JOptionPane.showConfirmDialog(appFrame,dialog.getContentPane(), "ScreenPreview", JOptionPane.NO_OPTION,JOptionPane.PLAIN_MESSAGE);

    }
}
