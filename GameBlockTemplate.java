import android.content.Context;
import android.widget.ImageView;

//Abstract class for game blocks; used for future game expansions
public abstract class GameBlockTemplate extends ImageView{
    public GameBlockTemplate(Context input1){
        super(input1); //Invoke ImageView constructor
    }
    public abstract boolean move();
    public abstract void setBlockDirection(GameLoopTask.gameDirection newDirection);
}
