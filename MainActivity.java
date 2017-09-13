import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.Timer;


public class MainActivity extends AppCompatActivity {

    //Create an accelerometer sensor event listener
    AccelerometerSensorEventListener ACCELEROMETER;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Relative Layout
        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.activity_main);
        mainLayout.getLayoutParams().width = 1080; //gameboard size
        mainLayout.getLayoutParams().height = 1260; //gameboard size
        //setting background to be gameboard.png
        mainLayout.setBackgroundResource(R.drawable.gameboard);

        //Create 2 TextViews
        TextView gestureSignature = (TextView) findViewById (R.id.gestureSignature);
        TextView WinLoss = (TextView) findViewById (R.id.WinLossText);

        //Create a game loop task
        GameLoopTask mainTask = new GameLoopTask(this, mainLayout, getApplicationContext(), WinLoss);

        //Set a 16 ms timer
        Timer gameLoop = new Timer();
        gameLoop.schedule(mainTask, 16, 16);


        //Create a sensor manager 
        SensorManager sensorMan = (SensorManager) getSystemService(SENSOR_SERVICE);
        //Create the accelerometer censor
        Sensor accelerometerSensorPhy = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //Initialize the event listener
        ACCELEROMETER = new AccelerometerSensorEventListener(gestureSignature, mainTask);
        //Register the listeners
        sensorMan.registerListener(ACCELEROMETER,accelerometerSensorPhy, SensorManager.SENSOR_DELAY_GAME);

    }
}
