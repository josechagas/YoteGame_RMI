import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;

public class ConnectionDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField ipTF;
    private JTextField portTF;
    private JLabel messageLabel;

    public ConnectionDialog() {
        setContentPane(contentPane);
        setModal(true);

        setUpIpTF();
        setUpPortTF();
        setUpConfirmButton();
        setUpCancelButton();

        setTitle("Sala");

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    public static void presentRelativeTo(Component c,String message){

        ConnectionDialog dialog = new ConnectionDialog();
        dialog.messageLabel.setText(message);
        if(c != null){
            System.out.println(c.getGraphicsConfiguration());
            dialog.setLocationRelativeTo(c);
        }
        dialog.pack();
        dialog.setVisible(true);
        //System.exit(0);
    }

    //region ConfirmButton methods

    private void setUpConfirmButton(){
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
    }

    private void onOK() {
        // add your code here

        String ip = ipTF.getText();
        String port = portTF.getText();
        if(ip != null && !ip.equalsIgnoreCase("") && port != null && !port.equalsIgnoreCase("")){
            Integer portNumber = new Integer(port);
            //ComunicationManager.getInstance().connectWith(new Tuple<String,Integer>(ip,portNumber));
        }
        else{
            /*Tuple<String,Integer> data = ComunicationManager.getInstance().waitConnection();
            if(data != null){
                System.out.println("My IP Address is "+ data.one+" and port "+data.two);
            }*/
        }
        dispose();
    }

    private void updateOKButtonTitle(){
        if(ipTF.getText().toCharArray().length > 0){
            if(portTF.getText().toCharArray().length > 0){
                buttonOK.setText("Conectar");
                return;
            }
        }
        buttonOK.setText("Criar");

    }
    //endregion

    //region CancelButton methods


    private void setUpCancelButton(){
        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
    //endregion



    /*public static void main(String[] args) {
        show();
    }*/

    //region TextField methods

    private void setUpIpTF(){
        ipTF.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                updateOKButtonTitle();
                if(e.getKeyChar() == '\n'){//try to connect
                    System.out.println("Typed IP "+ ipTF.getText());
                    onOK();
                }
            }
        });

        ipTF.getDocument().addDocumentListener(listenToChangesOnText());
    }


    private void setUpPortTF(){
        portTF.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                updateOKButtonTitle();
                if(e.getKeyChar() == '\n'){//try to connect
                    System.out.println("Typed port "+ portTF.getText());
                    onOK();
                }
            }
        });


        portTF.getDocument().addDocumentListener(listenToChangesOnText());

    }

    private DocumentListener listenToChangesOnText(){
        return new DocumentListener(){
            // implement the methods
            @Override
            public void insertUpdate(DocumentEvent e) {updateOKButtonTitle();}
            @Override
            public void removeUpdate(DocumentEvent e) {updateOKButtonTitle();}
            @Override
            public void changedUpdate(DocumentEvent e) {updateOKButtonTitle();}
        };
    }
    //endregion
}
