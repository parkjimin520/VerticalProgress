package com.example.verticalprogress;

public class VoiceList {

    private String title;
    boolean isExpanded = false;

    private int wordcloud;


    public VoiceList(String name, int wordcloud) {
        this.title = name;
        this.wordcloud = wordcloud;
    }

    //getter,setter
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public int getWordcloud() {
        return wordcloud;
    }

    public void setWordcloud(int wordcloud) {
        this.wordcloud = wordcloud;
    }
}

