package backgroundAnimation;

import javax.swing.*;
import java.awt.*;

public class FrameBackgroundAnimation {
    private int g, b, r;
    private boolean checkMax;
    private final Timer timer;

    public FrameBackgroundAnimation(JFrame frame){
        g = 100;
        b = 250;
        r = 255;
        checkMax = true;

        timer = new Timer(50, e -> {
            if(r==255) {
                checkMax=true;
            }
            else if(r==50) {
                checkMax=false;
            }
            if(checkMax) {
                r--;
                if(g<255) {
                    g++;
                }

            }
            else {
                r++;
                if(g>25) {
                    g--;
                }
                if(b<255) {
                    b++;
                }
            }
            frame.getContentPane().setBackground(new Color(r,g,b));
        });
    }

    public void start() {
        timer.start();
        timer.setDelay(100);
    }
}
