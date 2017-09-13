import android.widget.TextView;

import static GestureRecognition.curvePattern.Type_A;
import static GestureRecognition.curvePattern.Type_B;
import static GestureRecognition.curvePattern.Type_X;
import static GestureRecognition.state.DETERMINED;
import static GestureRecognition.state.FALL_A;
import static GestureRecognition.state.FALL_B;
import static GestureRecognition.state.RISE_A;
import static GestureRecognition.state.RISE_B;
import static GestureRecognition.state.WAIT;

public class GestureRecognition {
    //Objects needed for the constructor
    private TextView outputGesture;

    //Create a float to store filtered x and y axises readings
    private float[] filteredSE = new float[2];

    //Create a float to store the slope value between two consecutive points
    private float[] delta = new float[2];

    //Create a counter to keep track of how many times the FSM was invoked
    private int fsmCounter = 0;

    //Enumerate states
    enum state {
        WAIT, RISE_A, RISE_B, FALL_A, FALL_B, DETERMINED
    }
    state currentState[] = new state[]{WAIT, WAIT};

    //Enumerate pattern types
    enum curvePattern {
        Type_A, Type_B, Type_X
    }
    curvePattern currentPattern[] = new curvePattern[]{Type_X, Type_X};

    private GameLoopTask mainTask;


    //Threshold values for the FSM
    //The first index determines whether it is a threshold value for x or y axis
    //The second index determines whether threshold values for Type A or Type B pattern should be used
    //The third index determines the threshold values for different state transition types
    final float[][][] threshold = new float[][][]{
            {{0.04f, 0.5f, -0.05f}, {-0.04f, -0.1f, 0.45f}},
            {{0.04f, 0.5f, -0.05f}, {-0.04f, -0.1f, 0.45f}}
    };

    public GestureRecognition(TextView outputTextview, GameLoopTask mainGameLoopTask){
        outputGesture = outputTextview;
        mainTask = mainGameLoopTask;
    }

    public void combinatorialLogic(float[] input1, float[] input2){
        for(int i=0; i<2; i++){
            delta[i]=input1[i];
            filteredSE[i]=input2[i];
        }

        //If both x and y FSM reach the DETERMINED state, then display the recent gesture signature and reset the FSM
        //Otherwise, invoke the x and y FSMs
        if (currentState[0] == DETERMINED && currentState[1] == DETERMINED) {
            if (currentPattern[0] == Type_B && currentPattern[1] == Type_X) {
                outputGesture.setText("Left");
                mainTask.setDirection(GameLoopTask.gameDirection.LEFT);
            } else if (currentPattern[0] == Type_A && currentPattern[1] == Type_X) {
                outputGesture.setText("Right");
                mainTask.setDirection(GameLoopTask.gameDirection.RIGHT);
            } else if (currentPattern[0] == Type_X && currentPattern[1] == Type_A) {
                outputGesture.setText("Up");
                mainTask.setDirection(GameLoopTask.gameDirection.UP);
            } else if (currentPattern[0] == Type_X && currentPattern[1] == Type_B) {
                outputGesture.setText("Down");
                mainTask.setDirection(GameLoopTask.gameDirection.DOWN);
            } else {
                outputGesture.setText("Unknown");
                mainTask.setDirection(GameLoopTask.gameDirection.NO_MOVEMENT);
            }

            resetFSM();
        } else {
            //Invoke the FSM method for x and y values
            FSM(0);
            FSM(1);

            //Increment the FSM counter since FSM method was just invoked for a set of x and y values
            //Do not increment when the FSMs are still in wait mode
            if (currentState[0] != WAIT || currentState[1] != WAIT) {
                fsmCounter++;
            }

            //If the FSM has been invoked more than 30 times, reset the counter to 0 and reset the FSM
            if (fsmCounter > 30) {
                resetFSM();
            }
        }
    }

    private void FSM(int i) {
        //Please, refer to the UML FSM Diagram for explanation
        switch (currentState[i]) {
            case WAIT:
                if (delta[i] > threshold[i][0][0]) {
                    currentState[i] = RISE_A;
                } else if (delta[i] < threshold[i][1][0]) {
                    currentState[i] = FALL_B;
                }
                break;
            case RISE_A:
                if (delta[i] <= 0 && filteredSE[i] > threshold[i][0][1]) {
                    currentState[i] = FALL_A;
                } else if (delta[i] <= 0 && filteredSE[i] < threshold[i][0][1]) {
                    currentPattern[i] = Type_X;
                    currentState[i] = DETERMINED;
                }
                break;
            case FALL_A:
                if (delta[i] >= 0 && filteredSE[i] < threshold[i][0][2]) {
                    currentPattern[i] = Type_A;
                    currentState[i] = DETERMINED;
                } else if (delta[i] >= 0 && filteredSE[i] > threshold[i][0][2]) {
                    currentPattern[i] = Type_X;
                    currentState[i] = DETERMINED;
                }
                break;
            case FALL_B:
                if (delta[i] >= 0 && filteredSE[i] < threshold[i][1][1]) {
                    currentState[i] = RISE_B;
                } else if (delta[i] >= 0 && filteredSE[i] > threshold[i][1][1]) {
                    currentPattern[i] = Type_X;
                    currentState[i] = DETERMINED;
                }
                break;
            case RISE_B:
                if (delta[i] <= 0 && filteredSE[i] > threshold[i][1][2]) {
                    currentPattern[i] = Type_B;
                    currentState[i] = DETERMINED;
                } else if (delta[i] <= 0 && filteredSE[i] < threshold[i][1][2]) {
                    currentPattern[i] = Type_X;
                    currentState[i] = DETERMINED;
                }
                break;
            case DETERMINED:
                //Do nothing and wait until both x and y current states are DETERMINED
                break;
        }
    }

    //Reset all values
    private void resetFSM() {
        fsmCounter = 0;
        currentState[0] = WAIT;
        currentState[1] = WAIT;
        currentPattern[0] = Type_X;
        currentPattern[1] = Type_X;
    }

}
