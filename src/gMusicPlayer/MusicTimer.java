package gMusicPlayer;

import javax.swing.*;

public class MusicTimer extends Thread {
    private final JLabel currentTimeLabel;
    private final Song actualSong;
    private final JSlider slider;

    public MusicTimer(JLabel currentTimeLabel, Song actualSong, JSlider slider) {
        this.currentTimeLabel = currentTimeLabel;
        this.actualSong = actualSong;
        this.slider = slider;
        slider.setValue(0);
        slider.setEnabled(true);
        slider.setMaximum((int)actualSong.getMusicLengthInSecs());
    }

    public void run() {
        String status = "";
        while (!status.equals("stop")) {
            status = actualSong.getStatus();
            try {
                Thread.sleep(200);
                if (!status.equals("pause")) {
                    if (actualSong.getStatus().equals("play")) {
                        currentTimeLabel.setText(actualSong.getFormattedCurrentLength());
                        long currentSecond = actualSong.getMusicCurrentLengthInSecs();
                        slider.setValue((int) currentSecond);
                    }
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                if (status.equals("reset")) {
                    slider.setValue(0);
                    currentTimeLabel.setText("00:00:00");
                    break;
                }
            }
        }
    }
}
