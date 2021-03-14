package gMusicPlayer;

import backgroundAnimation.ButtonBackgroundAnimation;
import backgroundAnimation.FrameBackgroundAnimation;

import javax.imageio.ImageIO;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;

public class MusicPlayerFrame extends JFrame implements ActionListener, ListSelectionListener {
    private JSlider jSliderTime;
    private JSlider jSliderVolume;
    private JButton start;
    private JButton pause;
    private JButton restart;
    private JButton tenSecsMinus;
    private JButton tenSecsPlus;
    private JButton next;
    private JButton showQueueContent;
    private JButton open;
    private JButton addToQueue;
    private JLabel songName;
    private JLabel volumeStatus;
    private JLabel endTimeLabel;
    private JLabel actualTimeLabel;
    private Song actualSong;
    private ArrayList<Song> songQueue;
    private JFileChooser jFileChooser;
    private JFileChooser playlistFileChooser;
    private boolean first;
    private float volume;
    private JPanel playListPanel;
    private JPanel controlPanel;
    private JPanel volumePanel;
    private JPanel playListAndVolumeNamePanel;
    private DefaultListModel<String> jListModelPL;
    private JList<String> jListPL;
    private ArrayList<Song> playList;
    private int lastSelectedSongInPlaylist;

    public MusicPlayerFrame() {
        initializePlaylist();

        initializeButtons();

        initializeLabelsAndVariables();

        initializeControlPanel();

        initializeVolumePanel();

        initializePlaylistAndVolumePanel();

        initializeFrame();

        setAnimation();
    }

    private void initializePlaylist(){
        ImageIcon iconPlaylist = new ImageIcon(getResizedImage("resources\\playlist.png"));
        ImageIcon iconAddMusic = new ImageIcon(getResizedImage("resources\\addmusic.png"));
        ImageIcon iconRemoveMusic = new ImageIcon(getResizedImage("resources\\remove.png"));
        ImageIcon iconSavePlaylist = new ImageIcon(getResizedImage("resources\\save.png"));
        lastSelectedSongInPlaylist = -1;

        playList = new ArrayList<>();

        JButton savePlayList = new JButton("Save Playlist");
        savePlayList.setIcon(iconSavePlaylist);
        savePlayList.setBorderPainted(false);

        JButton loadPlayList = new JButton("Load Playlist");
        loadPlayList.setIcon(iconPlaylist);
        loadPlayList.setBorderPainted(false);

        JButton addMusicPlayList = new JButton("Add Music To Playlist");
        addMusicPlayList.setIcon(iconAddMusic);
        addMusicPlayList.setBorderPainted(false);

        JButton removeMusicPlayList = new JButton("Remove Music From Playlist");
        removeMusicPlayList.setIcon(iconRemoveMusic);
        removeMusicPlayList.setBorderPainted(false);

        new ButtonBackgroundAnimation(savePlayList).start();
        new ButtonBackgroundAnimation(loadPlayList).start();
        new ButtonBackgroundAnimation(addMusicPlayList).start();
        new ButtonBackgroundAnimation(removeMusicPlayList).start();

        JPanel playListButtonsPanel = new JPanel();
        playListButtonsPanel.setBackground(new Color(0,0,0,0));
        playListButtonsPanel.setOpaque(false);
        playListButtonsPanel.add(addMusicPlayList);
        playListButtonsPanel.add(removeMusicPlayList);
        playListButtonsPanel.add(savePlayList);
        playListButtonsPanel.add(loadPlayList);

        savePlayList.addActionListener(this);
        loadPlayList.addActionListener(this);
        addMusicPlayList.addActionListener(this);
        removeMusicPlayList.addActionListener(this);

        jListModelPL = new DefaultListModel<>();
        jListPL = new JList<>(jListModelPL);
        jListPL.scrollRectToVisible(new Rectangle());
        jListPL.setBackground(new Color(0,0,0,0));
        jListPL.setOpaque(false);
        jListPL.addListSelectionListener(this);
        jListPL.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JList theList = (JList) e.getSource();
                if (e.getModifiers() == MouseEvent.BUTTON3_MASK && e.getClickCount() == 2) {
                    int index = theList.locationToIndex(e.getPoint());
                    fromPlaylistToQueue(index);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(jListPL, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setOpaque(false);
        scrollPane.setBackground(new Color(0,0,0,0));
        scrollPane.setViewportView(jListPL);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setBackground(new Color(0,0,0,0));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        jListPL.setLayoutOrientation(JList.VERTICAL);

        playListPanel = new JPanel();
        playListPanel.setBackground(new Color(0,0,0,0));
        playListPanel.setOpaque(false);
        playListPanel.setLayout(new BorderLayout());
        playListPanel.add(scrollPane, BorderLayout.CENTER);
        playListPanel.add(playListButtonsPanel,BorderLayout.NORTH);

        savePlayList.setFocusable(false);
        loadPlayList.setFocusable(false);
        addMusicPlayList.setFocusable(false);
        removeMusicPlayList.setFocusable(false);
    }

    void fromPlaylistToQueue(int index){
        if(songQueue.isEmpty()){
            next.setEnabled(true);
            showQueueContent.setEnabled(true);
        }
        songQueue.add(playList.get(index));
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if(e.getValueIsAdjusting()) {
            int songIndex = jListPL.getSelectedIndex();
            if(lastSelectedSongInPlaylist != -1){
                actualSong.restart();
                actualSong.pause();
                if(lastSelectedSongInPlaylist < playList.size()) {
                    playList.get(lastSelectedSongInPlaylist).restart();
                    playList.get(lastSelectedSongInPlaylist).pause();
                }
            }
            else if (actualSong != null) {
                actualSong.restart();
                actualSong.pause();
            }
            actualSong = playList.get(songIndex);
            startPlayingMusic();
            lastSelectedSongInPlaylist = songIndex;
        }
    }

    private void initializeButtons(){
        ImageIcon iconPlay = new ImageIcon(getResizedImage("resources\\start.png"));
        ImageIcon iconPause = new ImageIcon(getResizedImage("resources\\pause.png"));
        ImageIcon iconRestart = new ImageIcon(getResizedImage("resources\\restart.png"));
        ImageIcon iconOpen = new ImageIcon(getResizedImage("resources\\open.png"));
        ImageIcon iconTenMinus = new ImageIcon(getResizedImage("resources\\tenminus.png"));
        ImageIcon iconTenPlus = new ImageIcon(getResizedImage("resources\\tenplus.png"));
        ImageIcon iconNext = new ImageIcon(getResizedImage("resources\\next.png"));
        ImageIcon iconAddToQueue = new ImageIcon(getResizedImage("resources\\queue.png"));
        ImageIcon iconShowContent = new ImageIcon(getResizedImage("resources\\showcontent.png"));

        start = new JButton("START");
        start.setIcon(iconPlay);
        start.setBorderPainted(false);

        pause = new JButton("PAUSE");
        pause.setIcon(iconPause);
        pause.setBorderPainted(false);

        restart = new JButton("RESTART");
        restart.setIcon(iconRestart);
        restart.setBorderPainted(false);

        open = new JButton("OPEN");
        open.setIcon(iconOpen);
        open.setMnemonic('O');
        open.setBorderPainted(false);

        tenSecsPlus = new JButton("+10s");
        tenSecsPlus.setIcon(iconTenPlus);
        tenSecsPlus.setBorderPainted(false);

        tenSecsMinus = new JButton("-10s");
        tenSecsMinus.setIcon(iconTenMinus);
        tenSecsMinus.setBorderPainted(false);

        next = new JButton("NEXT");
        next.setIcon(iconNext);
        next.setBorderPainted(false);

        addToQueue = new JButton("Add to Queue");
        addToQueue.setIcon(iconAddToQueue);
        addToQueue.setBorderPainted(false);

        showQueueContent = new JButton("Show Queue Content");
        showQueueContent.setIcon(iconShowContent);
        showQueueContent.setBorderPainted(false);

        setButtonProperties();
    }

    private void setButtonProperties(){
        start.setEnabled(false);
        pause.setEnabled(false);
        restart.setEnabled(false);
        tenSecsPlus.setEnabled(false);
        tenSecsMinus.setEnabled(false);
        next.setEnabled(false);
        showQueueContent.setEnabled(false);

        start.addActionListener(this);
        pause.addActionListener(this);
        restart.addActionListener(this);
        open.addActionListener(this);
        tenSecsPlus.addActionListener(this);
        tenSecsMinus.addActionListener(this);
        next.addActionListener(this);
        addToQueue.addActionListener(this);
        showQueueContent.addActionListener(this);

        open.setFocusable(false);
        start.setFocusable(false);
        pause.setFocusable(false);
        restart.setFocusable(false);
        tenSecsMinus.setFocusable(false);
        tenSecsPlus.setFocusable(false);
        next.setFocusable(false);
        addToQueue.setFocusable(false);
        showQueueContent.setFocusable(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "START" -> actualSong.resumePlay();
            case "PAUSE" -> actualSong.pause();
            case "RESTART" -> actualSong.restart();
            case "+10s" -> actualSong.jumpSecs(10);
            case "-10s" -> actualSong.jumpSecs(-10);
            case "OPEN" -> openFile();
            case "NEXT" -> {
                if(!songQueue.isEmpty()) playNextFromQueue();
                else if(!playList.isEmpty()) playNextFromPlaylist();
            }
            case "Add to Queue" -> toQueue();
            case "Show Queue Content" -> queueContent();
            case "Add Music To Playlist" -> {
                addMusicToPlayList();
                if(!playList.isEmpty()) {
                    next.setEnabled(true);
                }
            }
            case "Remove Music From Playlist" -> {
                removeMusicFromPlaylist();
                if(playList.isEmpty() && songQueue.isEmpty()) next.setEnabled(false);
            }
            case "Save Playlist" -> savePlaylist();
            case "Load Playlist" -> {
                try {
                    loadPlaylist();
                    revalidate();
                } catch (LineUnavailableException | UnsupportedAudioFileException ioException) {
                    System.out.println("Error: Loading Playlist!");
                }

                if(!playList.isEmpty()){
                    next.setEnabled(true);
                }
            }
        }
    }

    private void initializeLabelsAndVariables(){
        songQueue = new ArrayList<>();

        actualSong = null;
        first = true;
        jFileChooser = new JFileChooser(FileSystemView.getFileSystemView().getDefaultDirectory());
        playlistFileChooser = new JFileChooser(FileSystemView.getFileSystemView().getDefaultDirectory());

        songName = new JLabel("Playing: ");

        ImageIcon iconVolume = new ImageIcon(getResizedImage("resources\\volume.png"));
        volumeStatus = new JLabel("Volume: 75");
        volumeStatus.setIcon(iconVolume);

        actualTimeLabel = new JLabel("00:00:00");

        endTimeLabel = new JLabel("00:00:00");
    }

    private void initializeControlPanel(){
        JPanel playerPanel = new JPanel();
        playerPanel.setBackground(new Color(0,0,0,0));
        playerPanel.setOpaque(false);
        playerPanel.add(open);
        playerPanel.add(tenSecsMinus);
        playerPanel.add(start);
        playerPanel.add(pause);
        playerPanel.add(restart);
        playerPanel.add(tenSecsPlus);
        playerPanel.add(next);

        jSliderTime = new JSlider();
        jSliderTime.setBackground(new Color(0,0,0,0));
        jSliderTime.setOpaque(false);
        jSliderTime.setPreferredSize(new Dimension(500, 30));
        jSliderTime.setEnabled(false);

        jSliderTime.addChangeListener(e -> {
            JSlider source = (JSlider)e.getSource();
            int fps = source.getValue();

            if (source.getValueIsAdjusting()) {
                actualSong.pause();
                actualSong.jumpToSecs(fps);
                actualTimeLabel.setText(actualSong.getFormattedCurrentLength());
            }
            else actualSong.play();

            if(fps >= actualSong.getMusicLengthInSecs() && !songQueue.isEmpty()){
                playNextFromQueue();
            }
            else if (fps >= actualSong.getMusicLengthInSecs() && !playList.isEmpty()){
                playNextFromPlaylist();
            }
        });

        JPanel sliderPanel = new JPanel();
        sliderPanel.setBackground(new Color(0,0,0,0));
        sliderPanel.setOpaque(false);
        sliderPanel.add(addToQueue);
        sliderPanel.add(actualTimeLabel);
        sliderPanel.add(jSliderTime);
        sliderPanel.add(endTimeLabel);
        sliderPanel.add(showQueueContent);

        controlPanel = new JPanel();
        controlPanel.setBackground(new Color(0,0,0,0));
        controlPanel.setOpaque(false);
        controlPanel.setLayout(new BorderLayout());
        controlPanel.add(sliderPanel,BorderLayout.CENTER);
        controlPanel.add(playerPanel, BorderLayout.NORTH);
    }

    private void initializeVolumePanel(){
        jSliderVolume = new JSlider();
        jSliderVolume.setBackground(new Color(0,0,0,0));
        jSliderVolume.setOpaque(false);
        jSliderVolume.setPreferredSize(new Dimension(200, 30));
        jSliderVolume.setEnabled(false);
        jSliderVolume.setMinimum(0);
        jSliderVolume.setMaximum(100);
        jSliderVolume.addChangeListener(e -> {
            JSlider source = (JSlider)e.getSource();
            int volumePercent = source.getValue();
            float volumeValue = (float)volumePercent/100;
            setVolume(volumeValue);
            volumeStatus.setText("Volume: " + volumeToInteger(volume));
        });

        volumePanel = new JPanel();
        volumePanel.setBackground(new Color(0,0,0,0));
        volumePanel.setOpaque(false);
        volumePanel.add(volumeStatus);
        volumePanel.add(jSliderVolume);
    }

    private void initializePlaylistAndVolumePanel(){
        playListAndVolumeNamePanel = new JPanel();
        playListAndVolumeNamePanel.setBackground(new Color(0,0,0,0));
        playListAndVolumeNamePanel.setOpaque(false);
        playListAndVolumeNamePanel.setLayout(new BorderLayout());
        playListAndVolumeNamePanel.add(playListPanel,BorderLayout.CENTER);
        playListAndVolumeNamePanel.add(volumePanel,BorderLayout.SOUTH);
    }

    private void initializeFrame(){
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(0,0,0,0));
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(controlPanel,BorderLayout.NORTH);
        mainPanel.add(playListAndVolumeNamePanel,BorderLayout.CENTER);

        add(songName, BorderLayout.NORTH);
        add(mainPanel,BorderLayout.CENTER);

        setSize(955,355);
        setVisible(true);
        setTitle("GMusicPlayer");
        setLogo(this);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
    }

    private void setAnimation(){
        new FrameBackgroundAnimation(this).start();

        new ButtonBackgroundAnimation(start).start();
        new ButtonBackgroundAnimation(pause).start();
        new ButtonBackgroundAnimation(restart).start();
        new ButtonBackgroundAnimation(open).start();
        new ButtonBackgroundAnimation(tenSecsPlus).start();
        new ButtonBackgroundAnimation(tenSecsMinus).start();
        new ButtonBackgroundAnimation(next).start();
        new ButtonBackgroundAnimation(addToQueue).start();
        new ButtonBackgroundAnimation(showQueueContent).start();
    }

    private void playNextFromQueue(){
        if(actualSong != null) {
            actualSong.restart();
            actualSong.pause();
        }

        actualSong = songQueue.get(0);
        songQueue.remove(0);

        if(songQueue.isEmpty()){
            showQueueContent.setEnabled(false);
        }

        startPlayingMusic();
    }

    private void startPlayingMusic(){
        if(first) {
            start.setEnabled(true);
            pause.setEnabled(true);
            restart.setEnabled(true);
            tenSecsPlus.setEnabled(true);
            tenSecsMinus.setEnabled(true);
            jSliderVolume.setEnabled(true);
            volume = 0.75f;
            jSliderVolume.setValue(75);
            volumeStatus.setText("Volume: " + volumeToInteger(volume));
            first = false;
        }
        setVolume(volume);
        endTimeLabel.setText(actualSong.getFormattedLength());
        volumeStatus.setText("Volume: " + volumeToInteger(volume));
        actualTimeLabel.setText("00:00:00");
        MusicTimer timer = new MusicTimer(actualTimeLabel, actualSong, jSliderTime);
        timer.start();
        actualSong.play();
        songName.setText("Playing: " + actualSong.getSongName());
    }

    private void toQueue(){
        FileNameExtensionFilter restrict = new FileNameExtensionFilter("Only .wav, .aiff, .aifc, .snd or .au files", "wav","aiff","aifc","snd","au");
        jFileChooser.addChoosableFileFilter(restrict);
        jFileChooser.setAcceptAllFileFilterUsed(false);

        int r = jFileChooser.showOpenDialog(null);

        if(r == JFileChooser.APPROVE_OPTION){
            File file = jFileChooser.getSelectedFile();
            Path filePath = Path.of(String.valueOf(file));

            try {
                songQueue.add(new Song(filePath.toString()));
            } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ioException) {
                ioException.printStackTrace();
            }
            next.setEnabled(true);
            showQueueContent.setEnabled(true);
        }
        else{
            System.out.println("OPENING CANCELED");
        }
    }

    private void queueContent(){
        JFrame tempFrame = new JFrame("Queue Content");
        new FrameBackgroundAnimation(tempFrame).start();
        tempFrame.setLayout(new FlowLayout());

        JTextArea area = new JTextArea();
        area.setFont(new Font(Font.SERIF, Font.BOLD,  17));
        area.setBackground(new Color(0,0,0,0));

        for(int elem = 0; elem < songQueue.size(); elem++){
            area.append(elem+1 + ". " + songQueue.get(elem).getSongName());
            area.append("\n");
        }
        area.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(area,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setOpaque(false);
        scrollPane.setBackground(new Color(0,0,0,0));
        scrollPane.setViewportView(area);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setBackground(new Color(0,0,0,0));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel tempPanel = new JPanel();
        tempPanel.add(scrollPane);
        tempFrame.add(scrollPane);
        tempFrame.setSize(650,250);
        scrollPane.setPreferredSize(new Dimension(600,200));
        tempFrame.setVisible(true);
        setLogo(tempFrame);
        tempFrame.setResizable(false);
    }

    private void openFile(){
        FileNameExtensionFilter restrict = new FileNameExtensionFilter("Only .wav, .aiff, .aifc, .snd or .au files", "wav","aiff","aifc","snd","au");
        jFileChooser.addChoosableFileFilter(restrict);
        jFileChooser.setAcceptAllFileFilterUsed(false);

        int r = jFileChooser.showOpenDialog(null);

        if(r == JFileChooser.APPROVE_OPTION){
            File file = jFileChooser.getSelectedFile();

            Path filePath = Path.of(String.valueOf(file));

            try {
                if(actualSong != null) {
                    actualSong.restart();
                    actualSong.pause();
                }
                actualSong = new Song(filePath.toString());
            } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ioException) {
                ioException.printStackTrace();
            }
            startPlayingMusic();
        }
        else{
            System.out.println("OPENING CANCELED");
        }
    }


    private void setVolume(float newVolume){
        FloatControl gainControl = (FloatControl) actualSong.getClip().getControl(FloatControl.Type.MASTER_GAIN);
        float range = gainControl.getMaximum() - gainControl.getMinimum();
        float gain = (newVolume * range) + gainControl.getMinimum();
        volume = newVolume;
        gainControl.setValue(gain);
    }

    private void addMusicToPlayList(){
        jFileChooser.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter restrict = new FileNameExtensionFilter("Only .wav, .aiff, .aifc, .snd or .au files", "wav","aiff","aifc","snd","au");
        jFileChooser.addChoosableFileFilter(restrict);

        int r = jFileChooser.showOpenDialog(null);

        if(r == JFileChooser.APPROVE_OPTION){
            File file = jFileChooser.getSelectedFile();

            Path filePath = Path.of(String.valueOf(file));

            try {
                playList.add(new Song(filePath.toString()));
                jListModelPL.addElement(playList.size() + ". " + playList.get(playList.size()-1).getSongName());
            } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ioException) {
                ioException.printStackTrace();
            }
        }
        else{
            System.out.println("OPENING CANCELED");
        }
    }

    private void  removeMusicFromPlaylist(){
        int selectedIndex = jListPL.getSelectedIndex();
        if (selectedIndex != -1) {
            if (!playList.isEmpty()) {
                playList.remove(selectedIndex);
                jListModelPL.remove(selectedIndex);
                if (actualSong != null) {
                    actualSong.restart();
                    actualSong.pause();
                }
                for (int i = selectedIndex; i < playList.size(); i++) {
                    jListModelPL.remove(i);
                    String songName = playList.get(i).getSongName();
                    jListModelPL.add(i, i + 1 + ". " + songName);
                }
            }
        }
    }

    private void playNextFromPlaylist(){
        if(lastSelectedSongInPlaylist != -1){
            actualSong.restart();
            actualSong.pause();
            playList.get(lastSelectedSongInPlaylist).restart();
            playList.get(lastSelectedSongInPlaylist).pause();
        }
        else if(actualSong != null) {
            actualSong.restart();
            actualSong.pause();
        }

        if((lastSelectedSongInPlaylist + 1) < playList.size()){
            actualSong = playList.get(lastSelectedSongInPlaylist + 1);
            jListPL.setSelectedIndex(lastSelectedSongInPlaylist + 1);
            lastSelectedSongInPlaylist += 1;
        }
        else{
            actualSong = playList.get(0);
            jListPL.setSelectedIndex(0);
            lastSelectedSongInPlaylist = 0;
        }
        jListPL.ensureIndexIsVisible(lastSelectedSongInPlaylist);

        startPlayingMusic();
    }

    private void savePlaylist(){
        FileNameExtensionFilter restrict = new FileNameExtensionFilter("Only .GM","GM");
        playlistFileChooser.setFileFilter(restrict);
        int r = playlistFileChooser.showSaveDialog(null);

        if (r == JFileChooser.APPROVE_OPTION) {
            File file = playlistFileChooser.getSelectedFile();
            playlistFileChooser.setCurrentDirectory(file);
            try{
                FileWriter outFile = new FileWriter(file);
                PrintWriter out = new PrintWriter(outFile,true);
                for(Song e:playList){
                    out.println(e.getFilePath());
                }
                out.close();
                System.out.println("FILE SAVED: " + playlistFileChooser.getSelectedFile());
            }
            catch(IOException exc){
                System.out.println("Error: Saving the playlist!");
            }
        }
        else{
            System.out.println("SAVING CANCELED");
        }
    }

    private void loadPlaylist() throws LineUnavailableException, UnsupportedAudioFileException {
        FileNameExtensionFilter restrict = new FileNameExtensionFilter("Only .GM","GM");
        playlistFileChooser.setFileFilter(restrict);
        playlistFileChooser.setAcceptAllFileFilterUsed(false);
        int r = playlistFileChooser.showOpenDialog(null);

        if(r == JFileChooser.APPROVE_OPTION){
            System.out.println("FILE OPENED: " + playlistFileChooser.getSelectedFile());
            File file = playlistFileChooser.getSelectedFile();
            playlistFileChooser.setCurrentDirectory(file);
            jListModelPL.removeAllElements();
            playList.clear();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(String.valueOf(file)));
                String line = reader.readLine();
                while(line != null){
                    playList.add(new Song(line));
                    jListModelPL.addElement(playList.size() + ". " + playList.get(playList.size()-1).getSongName());
                    line = reader.readLine();
                }
                reader.close();
            } catch (IOException ioException) {
                System.out.println("Error: Reading from file!");
            }
        }
        else{
            System.out.println("LOADING CANCELED");
        }
    }

    private Image getResizedImage(String pathName){
        Image imgStart = null;
        try {
            imgStart = ImageIO.read(new File(pathName));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        assert imgStart != null;
        return imgStart.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
    }

    private void setLogo(JFrame frame){
        Image imgLogo = null;
        try {
            imgLogo = ImageIO.read(new File("resources\\GM.png"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        frame.setIconImage(imgLogo);
    }

    private int volumeToInteger(float vol){
        return Math.round(vol*100);
    }
}
