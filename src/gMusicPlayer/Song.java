package gMusicPlayer;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class Song {
    private final String filePath;
    private String status;
    private long currentTime;
    private final Clip musicClip;

    public Song(String filePath) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        this.filePath = filePath;
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());
        currentTime = 0;
        musicClip = AudioSystem.getClip();
        musicClip.open(audioInputStream);
        status = "pause";
    }

    public void play(){
        setStatus("play");
        musicClip.start();
    }

    public void pause(){
        if(!status.equals("pause")){
            setStatus("pause");
            currentTime = musicClip.getMicrosecondPosition();
            musicClip.stop();
        }
    }

    public void resumePlay(){
        if(!status.equals("play")){
            setStatus("play");
            musicClip.setMicrosecondPosition(currentTime);
            play();
        }
    }

    public void restart(){
        setStatus("restart");
        currentTime = 0;
        musicClip.setMicrosecondPosition(0);
        play();
    }

    public void jumpSecs(long jumpTime){
        currentTime = musicClip.getMicrosecondPosition();
        if((currentTime + (jumpTime * 1000000)) > 0  && (currentTime + (jumpTime * 1000000) < musicClip.getMicrosecondLength())){
            currentTime += jumpTime * 1000000;
        }
        else if((currentTime + (jumpTime * 1000000)) > musicClip.getMicrosecondLength()){
            currentTime = musicClip.getMicrosecondLength()-1;
        }
        else {
            currentTime = 0;
        }
        musicClip.setMicrosecondPosition(currentTime);
        play();
    }

    public void jumpToSecs(long jumpTime){
        if((jumpTime * 1000000) < musicClip.getMicrosecondLength()){
            currentTime = jumpTime * 1000000;
        }
        else if((jumpTime * 1000000) > musicClip.getMicrosecondLength()){
            currentTime = musicClip.getMicrosecondLength()-1;
        }
        else{
            currentTime = 0;
        }
        musicClip.setMicrosecondPosition(currentTime);
    }

    public long getMusicLengthInSecs(){
        return musicClip.getMicrosecondLength() / 1_000_000;
    }

    public String getFormattedLength(){
        long hour = 0;
        long minute;
        long lengthInSeconds = getMusicLengthInSecs();
        String formatedLength = "";

        if (lengthInSeconds >= 3600) {
            hour = lengthInSeconds / 3600;
            formatedLength = String.format("%02d:", hour);
        }
        else {
            formatedLength += "00:";
        }

        minute = lengthInSeconds - hour * 3600;
        if (minute >= 60) {
            minute = minute / 60;
            formatedLength += String.format("%02d:", minute);

        }
        else {
            minute = 0;
            formatedLength += "00:";
        }
        long second = lengthInSeconds - hour * 3600 - minute * 60;
        formatedLength += String.format("%02d", second);

        return formatedLength;
    }

    public long getMusicCurrentLengthInSecs(){
        return musicClip.getMicrosecondPosition() / 1_000_000;
    }

    public String getFormattedCurrentLength(){
        long hour = 0;
        long minute;
        long lengthInSeconds = getMusicCurrentLengthInSecs();
        String formatedLength = "";

        if (lengthInSeconds >= 3600) {
            hour = lengthInSeconds / 3600;
            formatedLength = String.format("%02d:", hour);
        }
        else {
            formatedLength += "00:";
        }

        minute = lengthInSeconds - hour * 3600;
        if (minute >= 60) {
            minute = minute / 60;
            formatedLength += String.format("%02d:", minute);

        }
        else {
            minute = 0;
            formatedLength += "00:";
        }
        long second = lengthInSeconds - hour * 3600 - minute * 60;
        formatedLength += String.format("%02d", second);
        return formatedLength;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getStatus() {
        return status;
    }

    public Clip getClip() {
        return musicClip;
    }

    public String getSongName(){
        String[] songName = filePath.split("\\\\");
        return songName[songName.length - 1];
    }

    @Override
    public String toString() {
        return filePath;
    }
}
