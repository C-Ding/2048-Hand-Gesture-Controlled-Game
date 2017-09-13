import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

public class AccelerometerSensorEventListener  implements SensorEventListener {
    //Constant C value for the low pass filter
    final float C = 14f;

    //Create a float to store filtered x and y axises readings
    float[] filteredSE = new float[2];

    //Create a float to store the slope value between two consecutive points
    float[] delta = new float[2];

    //Create a new GestureRecognition object
    GestureRecognition FiniteStateMachine;

    public AccelerometerSensorEventListener(TextView input1, GameLoopTask input2) {
        //Textviews for displaying sensor data and gesture signature
        FiniteStateMachine = new GestureRecognition(input1, input2);
    }

    public void onAccuracyChanged(Sensor s, int i) {
    }

    public void onSensorChanged(SensorEvent se) {
        //Check if the new sensor readings are from the accelerometer sensor
        if (se.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //Iterate through x and y values
            for (int i = 0; i < 2; i++) {
                //Low Pass Filter:
                //filteredSE[i]=filteredSE[i]+(se.values[i]-filteredSE[i])/C ,where filteredSE[i] on the L.H.S. contains the new reading value
                //while filteredSE[i] on the R.H.S contains the previous reading value
                //Thus, the difference between the previous reading and the new reading is (se.values[i]-filteredSE[i])/C, which is also the slope
                delta[i] = (se.values[i] - filteredSE[i]) / C;
                filteredSE[i] += delta[i];

            }

            //Invoke the combinatorial logic in order to capture the hand gesture
            FiniteStateMachine.combinatorialLogic(delta, filteredSE);
        }
    }
}
