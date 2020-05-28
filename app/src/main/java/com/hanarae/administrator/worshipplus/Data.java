package com.hanarae.administrator.worshipplus;

import android.net.Uri;

import java.util.ArrayList;

public class Data  {

    private String title;
    private String content;
    private String bible_date, bible, sermon, leader;


    String single_explanation, single_music, single_sheet;
    Uri single_sheet_temp;

    ArrayList sheet_url_Arraylist = new ArrayList<>();
    String sheet_url;

    private ArrayList date;
    private ArrayList explanation = new ArrayList();
    private ArrayList music = new ArrayList();
    private ArrayList sheet = new ArrayList();


    ArrayList tempDate;
    ArrayList tempExplanation;
    ArrayList tempMusic;
    ArrayList tempSheet;


    ArrayList Check= new ArrayList();
    ArrayList Second_Check= new ArrayList();
    ArrayList TitleArrayList = new ArrayList();
    ArrayList ChordArrayList = new ArrayList();

    ArrayList<ArrayList> DateArrayList = new ArrayList<ArrayList>();
    ArrayList<ArrayList> ExplanationArrayList = new ArrayList<ArrayList>();
    ArrayList<ArrayList> MusicArrayList = new ArrayList<ArrayList>();
    ArrayList<ArrayList> SheetArrayList = new ArrayList<ArrayList>();


    public boolean isEmpty(){
        if(TitleArrayList.size()==0) return true;
        else return false;
    }

    public void setSheet_url_Arraylist(String sheet_url){
        sheet_url_Arraylist.add(sheet_url);
    }
    public String getSheet_url(int position){
        return sheet_url_Arraylist.get(position).toString();
    }
    public void setSingle_Sheet_url(String sheet_url){
        this.sheet_url=sheet_url;
    }
    public String getSingle_Sheet_url(){
        return sheet_url;
    }


    public void removeSheet_url_ArrayList(){
        sheet_url_Arraylist.clear();
    }

    public void removeContiInfo(){
        bible_date = "";
        bible = "";
        sermon = "";
        leader = "";
    }

    public void setBibleDate(String bible_date){
        this.bible_date=bible_date;
    }
    public String getBibleDate(){
        return bible_date;
    }

    public void setBible(String bible){
        this.bible=bible;
    }
    public String getBible(){
        return bible;
    }

    public void setSermon(String sermon){
        this.sermon=sermon;
    }
    public String getSermon(){
        return sermon;
    }

    public void setLeader(String leader){
        this.leader=leader;
    }
    public String getLeader(){
        return leader;
    }



    public void setSingle_explanation(String explanation){
        single_explanation = explanation;
    }
    public String getSingle_explanation(){
        return single_explanation;
    }

    public void setSingle_music(String music){
        single_music = music;
    }
    public String getSingle_music(){
        return single_music;
    }


    public void setSingle_sheet(String sheet){
        single_sheet = sheet;
    }
    public String getSingle_sheet(){
        return single_sheet;
    }

    public void setSingle_sheet_temp(Uri sheet){
        single_sheet_temp = sheet;
    }
    public Uri getSingle_sheet_temp(){
        return single_sheet_temp;
    }



    public void setCheck(int check, int position, int second_position) {
        Check.set(position, check);
        ArrayList temp = new ArrayList<>();
        //체크한게 기본정보일때 태그가 복사되면 이상하기에 그냥 빈칸으로 복사하기
        if(DateArrayList.get(position).get(second_position).equals("기본정보")){
            temp.add("");
        }
        else {
            temp.add(ExplanationArrayList.get(position).get(second_position));
        }

        temp.add(MusicArrayList.get(position).get(second_position));
        temp.add(SheetArrayList.get(position).get(second_position));
        Second_Check.set(position, temp);

    }
    public void removeCheck() {Check.clear();Second_Check.clear();}

    public String getCheckedContent(int position, int type) {
        String result = "";

        if((int)Check.get(position)==1){
            switch (type){
                case 1: result = ((ArrayList)Second_Check.get(position)).get(0).toString();
                    break;
                case 2: result = ((ArrayList)Second_Check.get(position)).get(1).toString();
                    break;
                case 3: result = ((ArrayList)Second_Check.get(position)).get(2).toString();
                    break;
            }
        }
        return result;
    }



    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void addTitleArrayListItem(String content){
        TitleArrayList.add(content);
    }
    public void addTitleArrayListItem(int position, String content){
        TitleArrayList.add(position,content);
    }
    public void removeTitleArrayList(){
        TitleArrayList.clear();
    }
    public void removeTitleArrayList(int position){
        TitleArrayList.remove(position);
    }


    public void setChordArrayList(String content){
        ChordArrayList.add(content);
    }
    public void setChordArrayList(int position, String content){
        ChordArrayList.add(position, content);
    }
    public void removeChordArrayList(){
        ChordArrayList.clear();
    }
    public void removeChordArrayList(int position){
        ChordArrayList.remove(position);
    }


    public void setDateArrayList(ArrayList content){
        tempDate= new ArrayList();
        for (int i = 0; i < content.size();i++){
            String a = content.get(i).toString();
            tempDate.add(a);
        }

        DateArrayList.add(tempDate);
    }
    public void removeDateArrayList(){
        DateArrayList.clear();
    }
    public String getDate(int i) {
        return date.get(i).toString();
    }
    public int getDateSize() {
        return date.size();
    }
    public void setDate(ArrayList date) {
        this.date = date;
    }


    public void setExplanationArrayList(ArrayList content){
        tempExplanation= new ArrayList();
        for (int i = 0; i < content.size();i++){
            String a = content.get(i).toString();
            tempExplanation.add(a);
        }
        ExplanationArrayList.add(tempExplanation);
    }
    public void removeExplanationArrayList(){
        ExplanationArrayList.clear();
        explanation.clear();
    }  public String getExplanation(int i) {
        return explanation.get(i).toString();
    }
    public int getExplanationSize() {
        return explanation.size();
    }
    public void setExplanation(ArrayList ex) {
        this.explanation = ex;
    }
    public void addExplanation(String ex) {
        this.explanation.add(ex);
    }


    public void setMusicArrayList(ArrayList content){
        tempMusic= new ArrayList();
        for (int i = 0; i < content.size();i++){
            String a = content.get(i).toString();
            tempMusic.add(a);
        }
        MusicArrayList.add(tempMusic);
    }
    public void removeMusicArrayList(){
        MusicArrayList.clear();
        music.clear();
    }
    public String getMusic(int i) {
        return music.get(i).toString();
    }
    public void setMusic(ArrayList ex) {
        this.music = ex;
    }
    public void addMusic(String ex) {
        this.music.add(ex);
    }



    public void setSheetArrayList(ArrayList content){
        tempSheet= new ArrayList();
        for (int i = 0; i < content.size();i++){
            String a = content.get(i).toString();
            tempSheet.add(a);
        }
        SheetArrayList.add(tempSheet);
    }
    public void removeSheetArrayList(){
        SheetArrayList.clear();
        sheet.clear();
        sheet_url_Arraylist.clear();
    }
    public String getSheet(int i) {
        return sheet.get(i).toString();
    }
    public void setSheet(ArrayList ex) {
        this.sheet = ex;
    }
    public void addSheet(String ex) {
        this.sheet.add(ex);
    }




    public String getTitleArrayListItem(int i){ return TitleArrayList.get(i).toString();}
    public int getTitleArrayListSize(){ return TitleArrayList.size();}

    public String getChordArrayListItem(int i){ return ChordArrayList.get(i).toString();}
    public int getChordArrayListSize(){ return ChordArrayList.size();}

    public ArrayList<ArrayList> getDateArrayListItem(int i){ return DateArrayList.get(i);}
    public int getDateArrayListSize(){ return DateArrayList.size();}

    public ArrayList<ArrayList> getExplanationArrayListItem(int i){ return ExplanationArrayList.get(i);}
    public ArrayList<ArrayList> getMusicArrayListItem(int i){ return MusicArrayList.get(i);}
    public ArrayList<ArrayList> getSheetArrayListItem(int i){ return SheetArrayList.get(i);}

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}