package nl.tudelft.exchange.student.locationtracker.movement;


import android.app.Activity;
import android.util.Log;
import android.widget.ImageView;

import nl.tudelft.exchange.student.locationtracker.R;

/**
 * Created by stepien on 10.06.16.
 */
public class ArrowManager {

    private static Activity activity;

    private static int[][] arrow = new int[][]{
            {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {3, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 2, 0, 1, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 2, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 2, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 2, 0, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 2, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 4, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 4, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 1, 4, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 4, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 4, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 1, 3},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0}
    };

    private static void setUpArrow() {
        ImageView img= (ImageView) activity.findViewById(R.id.imageView);
        img.setImageResource(R.drawable.arrow_up);
    }

    private static void setDownArrow() {
        ImageView img= (ImageView) activity.findViewById(R.id.imageView);
        img.setImageResource(R.drawable.arrow_down);
    }

    private static void setLeftArrow() {
        ImageView img= (ImageView) activity.findViewById(R.id.imageView);
        img.setImageResource(R.drawable.arrow_left);
    }

    private static void setRightArrow() {
        ImageView img= (ImageView) activity.findViewById(R.id.imageView);
        img.setImageResource(R.drawable.arrow_right);
    }

    private static void setNoneArrow() {
        ImageView img= (ImageView) activity.findViewById(R.id.imageView);
        img.setImageResource(0);
    }

    public static void setArrow(Activity _activity, Object previousCellID, Object currentCellID) {
        activity = _activity;

        int prevCellIndex = (Integer.parseInt(previousCellID.toString())) - 1;
        int currCellIndex = (Integer.parseInt(currentCellID.toString())) - 1;

        int decision = arrow[prevCellIndex][currCellIndex];
        if (decision == 1)
            setUpArrow();
        else if (decision == 2)
            setRightArrow();
        else if (decision == 3)
            setDownArrow();
        else if (decision == 4)
            setLeftArrow();
        else
            setNoneArrow();
    }
}
