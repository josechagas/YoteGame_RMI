/**
 * Created by joseLucas on 21/04/17.
 */
public interface ComunicationListener {

    public void showAlertWith(String title,String message);
    public void showChatAlertMessage(String title,String message);
    public void updateInfoLabel(String text);
    public Boolean showConfirmAlertWith(String title,String message);


}