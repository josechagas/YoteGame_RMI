import java.util.function.Function;

/**
 * Created by joseLucas on 21/05/17.
 */
public class InBackground implements Runnable {

    //number of repetition,
    // returns should repeat
    private Function<Integer,Boolean> function;

    private InBackground(Function<Integer,Boolean> function){
        this.function = function;
    }

    public static void execute(Function<Integer,Boolean> function){
        InBackground background = new InBackground(function);
        Thread thread = new Thread(background);
        thread.start();
    }

    @Override
    public void run() {
        int i = 0;
        try {
            while(this.function.apply(i)){
                i++;
            }
        }
        catch (Exception e){}
    }
}
