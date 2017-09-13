import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;


public class GameLoopTask extends TimerTask {
    //Local versions of the required parameters
    private Activity mainActivity;
    private RelativeLayout mainLayout;
    private Context mainContext;

    //Win indication text view
    private TextView WINLOSS;

    //Enumerate all game directions
    public enum gameDirection {
        UP, RIGHT, DOWN, LEFT, NO_MOVEMENT
    }

    //Linkedlist containing all game blocks
    private LinkedList<GameBlock> blockList = new LinkedList();

    //Grid based gameblock storage
    //Stores each gameblock in [x][y] game board slot in the corresponding [x][y] array slot
    //Is the slot is empty, the value of the array at that index is null
    public GameBlock[][] boardSlot=new GameBlock[4][4];

    //Array list containing blocks to be merged data storage
    //List B contains gameblocks that will collide into gameblocks in list A
    private ArrayList<GameBlock> blocksToMergeA = new ArrayList();
    private ArrayList<GameBlock> blocksToMergeB = new ArrayList();

    //Boolean variables
    private boolean newblock = false; //True when there is a new target direction after which a new block must be created
    private boolean movingBlocks = false; //False if after the collision detection algorithm, the target and current locations of all blocks are the same => no movement

    public GameLoopTask(Activity myActivity, RelativeLayout myLayout, Context myContext, TextView WIN_LOSS){
        //Pass activity, layout and context by reference
        mainActivity = myActivity;
        mainLayout = myLayout;
        mainContext = myContext;

        //Create first 2 blocks
        createBlock();
        createBlock();

        //Pass the textview by reference. This textview is used to show whether the player has lost/won the game
        WINLOSS = WIN_LOSS;
    }

    private void createBlock(){
        //Create a new block
        GameBlock newBlock = new GameBlock(mainContext, this, WINLOSS);
        //Add the new block to the linkedlist
        blockList.add(newBlock);
        //Add the new block to the main layout
        mainLayout.addView(blockList.getLast());
        //Save the reference of this block in the right array slot, where the first index is x, the second index is y
        boardSlot[(newBlock.currentPosition[0]+60)/270][(newBlock.currentPosition[1]+60)/270] = newBlock;
        //Remove the initial reference to the new block
        newBlock = null;
    }

    public void run() {
        //Run on UI Thread
        mainActivity.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                            //Loop through all blocks, move them if necessary and check whether their velocity is 0
                            //If the velocity is 0, then set allTrue to false since allTrue tells whether all blocks are stationary or not
                            //Since move() returns a boolean, we can establish when block.move() returns false( in other words, not completely moving).
                            //If one of the blocks in the link list does move, Alltrue becomes false and no block is created.
                            boolean allTrue = true;
                            for (GameBlock block : blockList) {
                                block.move(); //Move the block
                                if(block.move() != true) {
                                    allTrue = false;
                                }
                            }

                            //Call the merge method in order to merge same blocks
                            merge();

                            //only create a new block if all the blocks have stopped moving

                            //3 conditions need to be met for when creating blocks:
                            //Newblock is made true when a new target direction is given
                            //Alltrue is made true only when all of the blocks stop moving.
                            //After running the collision detection algorithm, at least one block has its target position different from the current position.
                            if (allTrue && newblock && movingBlocks) {
                                //Create a new block
                                createBlock();

                                //Reset the boolean flags
                                newblock = false;
                                movingBlocks = false;
//                            for(int x=0; x<4; x++){
//                                for(int y=0; y<4; y++){
//                                    if(boardSlot[x][y]!=null){
//                                        Log.d("after ", String.format("(%d,%d),(%d,%d),%d", 1+x, 1+y, 1+(boardSlot[x][y].targetPosition[0]+60)/270 , 1+(boardSlot[x][y].targetPosition[1]+60)/270, boardSlot[x][y].BLOCK_VALUE));
//                                    }
//                                }
//                            }
                            }

                            //Check if the player has lost or not
                            //Check if none of the blocks are moving and no blocks needs to be merged (since when merging not all blocks are set to a fixed location)
                            if(allTrue && blocksToMergeA.isEmpty()){
                                //Check if there are 16 game blocks in the linked list
                                if(blockList.size()>=16){
                                    //Invoke loser(), which returns true if no blocks on the gameboard can merge
                                    if(loser()){
                                        //Make all blocks 50% transparent
                                        for(GameBlock block: blockList){
                                            block.setAlpha(0.5f);
                                        }
                                        //Set the background image
                                        mainLayout.setBackgroundResource(R.drawable.loser);
                                        //Set the textview
                                        WINLOSS.setText("GAME OVER");
                                    }
                                }
                            }

                    }
                }
        );
    }

    //Gives the game-direction as determined by the FSM to the the GameLoopTask so it can set the current block direction
    public void setDirection(gameDirection newDirection){
        //Only accepts a new direction if none of the blocks are moving, if at least one of the blocks move, noMove becomes false and the direction is not fed in
        if(newDirection!=gameDirection.NO_MOVEMENT) {
            boolean noMove = false;
            for (GameBlock block: blockList) {
                if (block.velocity == 0.0f) {
                    noMove = true;
                } else {
                    noMove = false;
                    break;
                }
            }
            //If none of the blocks move, set a new direction for all blocks
            if (noMove) {
                for (GameBlock block : blockList) {
                    block.setBlockDirection(newDirection); //Pass the direction to the block object
                }
                //Invoke collision detection algorithm in order to set target locations of all blocks
                collisionDetection(newDirection);
                //Set newblock to true since there is a new direction and a new block should be created after all blocks stop
                newblock=true;
            }
        }
    }

    //This function sets the target positions for all blocks
    private void collisionDetection(gameDirection newDirection){
        //Used for debugging
//        Log.d("Invoked", "Invoked!");
//        for(int x=0; x<4; x++){
//            for(int y=0; y<4; y++){
//                if(boardSlot[x][y]!=null){
//                    Log.d("before ", String.format("(%d,%d),(%d,%d),%d", 1+x, 1+y, 1+(boardSlot[x][y].targetPosition[0]+60)/270 , 1+(boardSlot[x][y].targetPosition[1]+60)/270, boardSlot[x][y].BLOCK_VALUE));
//                }
//            }
//        }

        //Declare a variable to store the initial target slot
        int boundary;

        //Switch-case algorithm for all directions
        switch(newDirection){
            case LEFT:
                for(int i=0; i<4; i++){
                    boundary = 0; //Target left slot; x value
                    for(int j=1; j<4; j++){
                        //If at [j][i] there is no game block, skip this iteration
                        if(boardSlot[j][i] == null){
                            continue;
                        }
                        //If the slot at the boundary is empty, set the target position of the [j][i] slot to that boundary slot and remove the reference at the old position
                        else if(boardSlot[boundary][i]==null){
                            boardSlot[j][i].targetPosition[0]=boundary*270-60;
                            boardSlot[boundary][i]=boardSlot[j][i];
                            boardSlot[j][i]=null;
                        }
                        //If the slot at the boundary has the same value, set the target position of the current block to the block at the boundary slot
                        //Add both blocks to the list of blocks that needs to be merged
                        //Remove the reference of the block B that will collide into block A
                        //Move the boundary slot to the next slot since after merging, no other slot can move to that slot
                        else if(boardSlot[j][i].BLOCK_VALUE==boardSlot[boundary][i].BLOCK_VALUE){
                            boardSlot[j][i].targetPosition[0]=boundary*270-60;
                            blocksToMergeA.add(boardSlot[boundary][i]);
                            blocksToMergeB.add(boardSlot[j][i]);
                            boardSlot[j][i]=null;
                            boundary++;
                        }
                        else{
                            //If the values of the boundary slot is not the same, move the boundary to the next slot
                            boundary++;
                            //Move to slot to the new boundary slot if it is not already there
                            if(boardSlot[j][i].targetPosition[0]!=boundary*270-60){
                                boardSlot[j][i].targetPosition[0]=boundary*270-60;
                                boardSlot[boundary][i]=boardSlot[j][i];
                                boardSlot[j][i]=null;
                            }
                        }
                    }
                }
                break;
            case UP:
                for(int i=0; i<4; i++){
                    boundary = 0; //Target top slot; y value
                    for(int j=1; j<4; j++){
                        //If at [j][i] there is no game block, skip this iteration
                        if(boardSlot[i][j] == null){
                            continue;
                        }
                        //If the slot at the boundary is empty, set the target position of the [i][j] slot to that boundary slot and remove the reference at the old position
                        else if(boardSlot[i][boundary]==null){
                            boardSlot[i][j].targetPosition[1]=boundary*270-60;
                            boardSlot[i][boundary]=boardSlot[i][j];
                            boardSlot[i][j]=null;
                        }
                        //If the slot at the boundary has the same value, set the target position of the current block to the block at the boundary slot
                        //Add both blocks to the list of blocks that needs to be merged
                        //Remove the reference of the block B that will collide into block A
                        //Move the boundary slot to the next slot since after merging, no other slot can move to that slot
                        else if(boardSlot[i][j].BLOCK_VALUE==boardSlot[i][boundary].BLOCK_VALUE){
                            boardSlot[i][j].targetPosition[1]=boundary*270-60;
                            blocksToMergeA.add(boardSlot[i][boundary]);
                            blocksToMergeB.add(boardSlot[i][j]);
                            boardSlot[i][j]=null;
                            boundary++;
                        }
                        //If the values of the boundary slot is not the same, move the boundary to the next slot
                        else{
                            boundary++;
                            //Move to slot to the new boundary slot if it is not already there
                            if(boardSlot[i][j].targetPosition[1]!=boundary*270-60){
                                boardSlot[i][j].targetPosition[1]=boundary*270-60;
                                boardSlot[i][boundary]=boardSlot[i][j];
                                boardSlot[i][j]=null;
                            }
                        }
                    }
                }
                break;
            case RIGHT:
                for(int i=0; i<4; i++){
                    boundary = 3; //Target right slot; x value
                    for(int j=2; j>=0; j--){
                        //If at [j][i] there is no game block, skip this iteration
                        if(boardSlot[j][i] == null){
                            continue;
                        }
                        //If the slot at the boundary is empty, set the target position of the [j][i] slot to that boundary slot and remove the reference at the old position
                        else if(boardSlot[boundary][i]==null){
                            boardSlot[j][i].targetPosition[0]=boundary*270-60;
                            boardSlot[boundary][i]=boardSlot[j][i];
                            boardSlot[j][i]=null;
                        }
                        //If the slot at the boundary has the same value, set the target position of the current block to the block at the boundary slot
                        //Add both blocks to the list of blocks that needs to be merged
                        //Remove the reference of the block B that will collide into block A
                        //Move the boundary slot to the previous slot since after merging, no other slot can move to that slot
                        else if(boardSlot[j][i].BLOCK_VALUE==boardSlot[boundary][i].BLOCK_VALUE){
                            boardSlot[j][i].targetPosition[0]=boundary*270-60;
                            blocksToMergeA.add(boardSlot[boundary][i]);
                            blocksToMergeB.add(boardSlot[j][i]);
                            boardSlot[j][i]=null;
                            boundary--;
                        }
                        else{
                            //If the values of the boundary slot is not the same, move the boundary to the previous slot
                            boundary--;
                            //Move to slot to the new boundary slot if it is not already there
                            if(boardSlot[j][i].targetPosition[0]!=boundary*270-60){
                                boardSlot[j][i].targetPosition[0]=boundary*270-60;
                                boardSlot[boundary][i]=boardSlot[j][i];
                                boardSlot[j][i]=null;
                            }
                        }
                    }
                }
                break;
            case DOWN:
                for(int i=0; i<4; i++){
                    boundary = 3; //Target bottom slot; y value
                    for(int j=2; j>=0; j--){
                        //If at [j][i] there is no game block, skip this iteration
                        if(boardSlot[i][j] == null){
                            continue;
                        }
                        //If the slot at the boundary is empty, set the target position of the [i][j] slot to that boundary slot and remove the reference at the old position
                        else if(boardSlot[i][boundary]==null){
                            boardSlot[i][j].targetPosition[1]=boundary*270-60;
                            boardSlot[i][boundary]=boardSlot[i][j];
                            boardSlot[i][j]=null;
                        }
                        //If the slot at the boundary has the same value, set the target position of the current block to the block at the boundary slot
                        //Add both blocks to the list of blocks that needs to be merged
                        //Remove the reference of the block B that will collide into block A
                        //Move the boundary slot to the previous slot since after merging, no other slot can move to that slot
                        else if(boardSlot[i][j].BLOCK_VALUE==boardSlot[i][boundary].BLOCK_VALUE){
                            boardSlot[i][j].targetPosition[1]=boundary*270-60;
                            blocksToMergeA.add(boardSlot[i][boundary]);
                            blocksToMergeB.add(boardSlot[i][j]);
                            boardSlot[i][j]=null;
                            boundary--;
                        }
                        else{
                            //If the values of the boundary slot is not the same, move the boundary to the previous slot
                            boundary--;
                            //Move to slot to the new boundary slot if it is not already there
                            if(boardSlot[i][j].targetPosition[1]!=boundary*270-60){
                                boardSlot[i][j].targetPosition[1]=boundary*270-60;
                                boardSlot[i][boundary]=boardSlot[i][j];
                                boardSlot[i][j]=null;
                            }
                        }
                    }
                }
                break;
            default:
                break;
        }

        //Check if after running the above algorithm, there is a change in the target positions compared to the current positions
        movingBlocks = !noChange();
    }

    //This function returns TRUE if after the collision detection algorithm has set the target position, there is no change since the target=current positions
    private boolean noChange(){
        for(GameBlock block: blockList){
            if(block.targetPosition[0]!=block.currentPosition[0] || block.targetPosition[1]!=block.currentPosition[1]){
                return false;
            }
        }
        return true;
    }

    //Merge same value blocks
    private void merge(){
        //Checks to see is any blocks are in the blockToMerge list
        boolean empty = blocksToMergeA.isEmpty();
        if(!empty){
            //If there is blocks to merge,check how many
            int numberOfMerges = blocksToMergeA.size();

            //Loop through all blocks that need to merge
            for(int i=0; i<numberOfMerges; i++){
                //Checks to see if the blocks are done moving and at their target location before merging
                float Velocity = blocksToMergeB.get(i).velocity;
                if(Velocity == 0.0f){
                    //Doubles one of the blocks values and sets the other ones value to zero
                    int Value = blocksToMergeB.get(i).BLOCK_VALUE;
                    int newValue = (2*Value);

                    //Calls the setBlockType function in GameBlock,
                    //this uses a switch case to change the drawable of one while self removing the other
                    blocksToMergeB.get(i).setBlockType(0);
                    blocksToMergeA.get(i).setBlockType(newValue);

                    //Remove both blocks from the blocksToMerge list because they have been merged
                    //Remove the reference to the colliding block, by setting it to null
                    blocksToMergeB.set(i, null);
                    blocksToMergeA.remove(i);
                    blocksToMergeB.remove(i);

                    //decrease the counter since a block was removed
                    i--;
                    //decrease the remaining number of blocks to be merged
                    numberOfMerges--;
                }

            }
        }

    }

    //The block deletion function. Removes the reference of one of the merged blocks in the linked list
    //Used for Java Garbage Collection
    public void checkForDelete(){
        //Loop through all blocks in the linked list in order to find the block which value is 0
        for (int i = 0; i<blockList.size(); i++)
        {
            int DeleteValue = blockList.get(i).BLOCK_VALUE;
            if(DeleteValue == 0){
                //Remove the block by setting to null
                blockList.set(i, null);
                //Disconnect the block from the linkedlist
                blockList.remove(i);
                //Since an element was removed, decrement the counter
                i--;
            }
        }
    }

    //This function checks if any of the blocks on the board can merge or not
    //Returns TRUE if no blocks can merge and the game is over
    private boolean loser(){
        //Loop through all slots on the gameboard
        for(int i=0; i<4; i++){
            for(int j=1; j<=2; j++){
                //If any of the slots is null, the board is not completely filled
                if(boardSlot[i][j]==null || boardSlot[i][j-1]==null || boardSlot[i][j+1]==null){
                    return false;
                }
                if(boardSlot[j][i]==null || boardSlot[j-1][i]==null || boardSlot[j+1][i]==null){
                    return false;
                }
                //Check the neighbouring slots and see if they can merge
                if(boardSlot[i][j].BLOCK_VALUE==boardSlot[i][j-1].BLOCK_VALUE || boardSlot[i][j].BLOCK_VALUE==boardSlot[i][j+1].BLOCK_VALUE){
                    return false;
                }
                if(boardSlot[j][i].BLOCK_VALUE==boardSlot[j-1][i].BLOCK_VALUE || boardSlot[j][i].BLOCK_VALUE==boardSlot[j+1][i].BLOCK_VALUE){
                    return false;
                }
            }
        }
        return true;
    }
}
