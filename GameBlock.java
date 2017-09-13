import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.TextView;
import java.util.Random;

public class GameBlock extends GameBlockTemplate{
    //Acceleration Constant
    private final float acceleration = 0.10f;
    //Declare a variable that holds the velocity value
    public float velocity;

    //Scaling constant of the image
    private final float IMAGE_SCALE = 0.6f;

    //Current X and Y position of the block on the layout
    public int[] currentPosition = new int[2];
    //Target X and Y position of the block on the game board
    public int[] targetPosition = new int[2];

    //The target direction of all gameblock objects
    private GameLoopTask.gameDirection targetDirection = GameLoopTask.gameDirection.NO_MOVEMENT;

    //The blocks number value on the board
    public int BLOCK_VALUE;

    //Local copy of the GameLoopTask
    private GameLoopTask TASK;

    //Textview used to indicate a win or a loss visually
    private TextView WINLOSS;

    //Constructor
    public GameBlock(Context mainContext, GameLoopTask task, TextView WinLoss){

        //ImageView constructor
        super(mainContext);

        //Scale X and Y to the Image Scale constant
        this.setScaleX(IMAGE_SCALE);
        this.setScaleY(IMAGE_SCALE);

        //Pass main task by reference
        TASK = task;

        //Set the block value to 2 or 4
        setBlockType(2 * (new Random().nextInt(2) + 1));

        //Set the position of the block at a random spot on the board
        //Run do while loop until it finds a slot that has not been taken
        do{
            for(int i=0; i<2; i++){
                //Set the position of the block at a random spot on the board
                //270pixels is the slot isolation value; the distance between each slot is 270 pixels.
                this.currentPosition[i]=-60+270*(new Random().nextInt(4));
                //Originally, the target and current position is the same
                this.targetPosition[i]=this.currentPosition[i];
            }
        }while(TASK.boardSlot[(this.currentPosition[0]+60)/270][(this.currentPosition[1]+60)/270]!=null);
        //(pixels+60 offset pixels)/270 pixels is the formula used to find the # of slot

        //Place the object using X and Y coordinates
        this.setX(currentPosition[0]);
        this.setY(currentPosition[1]);

        //The velocity of this block; originally it is not moving
        this.velocity = 0;

        //The win or loss textview
        WINLOSS = WinLoss;
    }

    //This function sets the blocks internal value at creation and every time it gets merged
    //as well as sets the right block image based on the block value
    public void setBlockType(int i) {
        switch (i) {
            case 0:
                this.setImageResource(android.R.color.transparent);
                this.BLOCK_VALUGameBlock.javaE = 0;
                TASK.checkForDelete(); //Delete the remaining references of this block
                break;

            case 2:
                this.setImageResource(R.drawable.i2);
                this.BLOCK_VALUE = 2;
                break;

            case 4:
                this.setImageResource(R.drawable.i4);
                this.BLOCK_VALUE = 4;
                break;

            case 8:
                this.setImageResource(R.drawable.i8);
                this.BLOCK_VALUE = 8;
                break;

            case 16:
                this.setImageResource(R.drawable.i16);
                this.BLOCK_VALUE = 16;
                break;

            case 32:
                this.setImageResource(R.drawable.i32);
                this.BLOCK_VALUE = 32;
                break;

            case 64:
                this.setImageResource(R.drawable.i64);
                this.BLOCK_VALUE = 64;
                break;

            case 128:
                this.setImageResource(R.drawable.i128);
                this.BLOCK_VALUE = 128;
                break;

            case 256:
                this.setImageResource(R.drawable.i256);
                this.BLOCK_VALUE = 256;
                Log.d("_","WIN 256 ");
                WINLOSS.setText("WIN");
                WINLOSS.setTextColor(Color.RED);
                WINLOSS.bringToFront();
                break;

            case 512:
                this.setImageResource(R.drawable.i512);
                this.BLOCK_VALUE = 512;
                break;

            case 1024:
                this.setImageResource(R.drawable.i1024);
                this.BLOCK_VALUE = 1024;
                break;

            case 2048:
                this.setImageResource(R.drawable.i2048);
                this.BLOCK_VALUE = 2048;
                break;

            default:
                break;
        }
    }

    //Set a new target direction
    public void setBlockDirection(GameLoopTask.gameDirection newDirection){
        targetDirection = newDirection;
    }

    //This function moves game blocks and returns whether that gameblock's velocity is 0 or not
    public boolean move(){
        switch(targetDirection){
            default:
                break;
            case UP:
                //If moving the object by the velocity value of pixels causes the object to go out of bound, set the velocity to 0 and set the current position to the target position.
                if((this.currentPosition[1]-this.velocity) <= this.targetPosition[1]){
                    this.currentPosition[1] = this.targetPosition[1];
                    this.velocity = 0;
                }
                else{
                    //Remove the pixel velocity to the Y coordinate (move UP the object by velocity pixels)
                    this.currentPosition[1] -= this.velocity;
                    //Increase the pixel velocity by the acceleration value
                    this.velocity += acceleration;
                }
                break;
            case RIGHT:
                //If moving the object by the velocity value of pixels causes the object to go out of bound, set the velocity to 0 and set the current position to the target position.
                if((this.currentPosition[0]+this.velocity) >= this.targetPosition[0]){
                    this.currentPosition[0] = this.targetPosition[0];
                    this.velocity = 0;
                }
                else{
                    //Add the pixel velocity to the X coordinate (move the object to the Right by velocity pixels)
                    this.currentPosition[0] += this.velocity;
                    //Increase the pixel velocity by the acceleration value
                    this.velocity += acceleration;
                }
                break;
            case DOWN:
                //If moving the object by the velocity value of pixels causes the object to go out of bound, set the velocity to 0 and set the current position to the target position.
                if((this.currentPosition[1]+this.velocity) >= this.targetPosition[1]){
                    this.currentPosition[1] = this.targetPosition[1];
                    this.velocity = 0;
                }
                else{
                    //Add the pixel velocity to the  Y coordinate (move Down the object by velocity pixels)
                    this.currentPosition[1] += this.velocity;
                    //Increase the pixel velocity by the acceleration value
                    this.velocity += acceleration;
                }
                break;
            case LEFT:
                //If moving the object by the velocity value of pixels causes the object to go out of bound, set the velocity to 0 and set the current position to the target position.
                if((this.currentPosition[0]-this.velocity) <= this.targetPosition[0]){
                    this.currentPosition[0] = this.targetPosition[0];
                    this.velocity = 0;
                }
                else{
                    //Remove the pixel velocity to the X coordinate (move the object to the Left by velocity pixels)
                    this.currentPosition[0] -= this.velocity;
                    //Increase the pixel velocity by the acceleration value
                    this.velocity += acceleration;
                }
                break;
        }

        //Place the object using X and Y coordinates
        this.setX(this.currentPosition[0]);
        this.setY(this.currentPosition[1]);

        //If the velocity is 0, there is no movement
        if(this.velocity == 0.0f){
            targetDirection = GameLoopTask.gameDirection.NO_MOVEMENT;
            return true; //Return true when the velocity is 0
        }
        return false; //Return false when the velocity is not 0
    }


}
