package com.hanarae.administrator.worshipplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class TempList extends LinearLayout {

    private TextView song_date;
    private Button button_sheet;
    private Button button_explanation;
    private Button button_music;
    CustomDialog cd;
    InputMethodManager imm;
    int width, height, position, second_position;
    private CheckBox checkBox;
    String content_string;
    Context context;


    public TempList(final Context context, int width, int height, InputMethodManager imm, int case_num, final int position, final int second_position) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.temp_layout,this);
        this.imm = imm;
        this.width=width;
        this.height=height;
        this.position=position;
        this.second_position=second_position;
        this.context = context;

        checkBox = findViewById(R.id.song_check);

        if(case_num==0) checkBox.setVisibility(INVISIBLE);// 메인콘티에서 체크박스 지움
        if(case_num==1) {//검색기능에서 버튼 체크박스 추가 기능
            checkBox.setChecked(false);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        if((int)MainActivity.tempData.Check.get(position)==1){
                            checkBox.setChecked(false);
                            Toast.makeText(context,"각 곡당 1개의 콘티만 체크할 수 있습니다\n기존체크를 해제한 후, 다시 체크해주세요",Toast.LENGTH_LONG).show();
                            return;
                        }else MainActivity.tempData.setCheck(1, position, second_position);
                    } else {
                        MainActivity.tempData.setCheck(0, position, second_position);
                    }
                }
            });
        }
        return;

    }


    public void setDate(String date){

        song_date = findViewById(R.id.textView_conti_date);
        song_date.setText(date);

    }

    public void setDate(String date, final String song_name){//기본정보용으로 길게 누르면 수정가능


        song_date = findViewById(R.id.textView_conti_date);
        song_date.setText(date);
        song_date.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if(MainActivity.logged_in_id.equals(MainActivity.admin_id)) {

                    cd = new CustomDialog(4, getContext(), position, imm, song_name);
                    WindowManager.LayoutParams wm = cd.getWindow().getAttributes();  //다이얼로그의 높이 너비 설정하기위해
                    wm.copyFrom(cd.getWindow().getAttributes());  //여기서 설정한값을 그대로 다이얼로그에 넣겠다는의미
                    wm.width = width;  //화면 너비의 절반
                    wm.height = height / 2;  //화면 높이의 절반
                    cd.show();
                }

                return false;
            }
        });

    }

    public void setExplanation(String ex){

        final String explanation = ex;
        button_explanation = findViewById(R.id.button_explanation);
        if(ex.equals("")) button_explanation.setTextColor(Color.LTGRAY);
        button_explanation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {


                cd = new CustomDialog(3, getContext(), position, imm, explanation);
                //cd = new CustomDialog(3, getContext(), 0, imm, explanation);
                WindowManager.LayoutParams wm = cd.getWindow().getAttributes();  //다이얼로그의 높이 너비 설정하기위해
                wm.copyFrom(cd.getWindow().getAttributes());  //여기서 설정한값을 그대로 다이얼로그에 넣겠다는의미
                wm.width = width ;  //화면 너비의 절반
                wm.height = height / 2;  //화면 높이의 절반
                cd.show();

            }
        });
    }

    public void setExplanation(String ex, final int special){//기본정보용 edit text로 대체

        final String explanation = ex;
        button_explanation = findViewById(R.id.button_explanation);
        //기본정보 탭의 '내용' 글자를 '태그'로 바꿈
        button_explanation.setText("태그");
        button_explanation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                cd = new CustomDialog(special, getContext(), position, imm, explanation);
                //cd = new CustomDialog(special, getContext(), 0, imm, explanation);
                WindowManager.LayoutParams wm = cd.getWindow().getAttributes();  //다이얼로그의 높이 너비 설정하기위해
                wm.copyFrom(cd.getWindow().getAttributes());  //여기서 설정한값을 그대로 다이얼로그에 넣겠다는의미
                wm.width = width ;  //화면 너비의 절반
                wm.height = height / 2;  //화면 높이의 절반
                cd.show();

            }
        });
    }

    public void setSheet(String ex){ // 악보링크 보여줌

        final String sheet = ex;
        button_sheet = findViewById(R.id.button_sheet);
        button_sheet.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                cd = new CustomDialog(5, getContext(), position, imm, sheet);
                WindowManager.LayoutParams wm = cd.getWindow().getAttributes();  //다이얼로그의 높이 너비 설정하기위해
                wm.copyFrom(cd.getWindow().getAttributes());  //여기서 설정한값을 그대로 다이얼로그에 넣겠다는의미
                wm.width = width ;  //화면 너비의 절반
                wm.height = height / 2;  //화면 높이의 절반
                cd.show();
                //Toast.makeText(getContext(),sheet,Toast.LENGTH_SHORT).show();

            }
        });

    }

    public void setSheet(String ex, final int special){

        final String explanation = ex;

        button_sheet = findViewById(R.id.button_sheet);
        button_sheet.setText("악보");
        button_sheet.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoSelect = new Intent(getContext(),PhotoSelect.class);
                photoSelect.putExtra("position", special);//검색창에 쓸 예외처리
                photoSelect.putExtra("song_name", explanation);//
                photoSelect.putExtra("position_main_search", position);//
                getContext().startActivity(photoSelect);

            }
        });

    }

    public void setSheet(String ex, final int special, final String url){

        final String explanation = ex;

        button_sheet = findViewById(R.id.button_sheet);
        button_sheet.setText("악보");
        if(url.equals("")) button_sheet.setTextColor(getResources().getColor(R.color.DarkModeGraytoBlack));
        button_sheet.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isMobile=true;
                boolean isWiFi=true;
                boolean isWiMax=true;

                //인터넷 연결확인 작업
                if(MainActivity.manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)!=null) {
                    isMobile = MainActivity.manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
                }
                if(MainActivity.manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!=null) {
                    isWiFi = MainActivity.manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
                }
                if(MainActivity.manager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX)!=null) {
                    isWiMax = MainActivity.manager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX).isConnectedOrConnecting();
                }

                if (!isMobile && !isWiFi) {
                    Toast.makeText(context, "인터넷 연결을 확인해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent photoSelect = new Intent(getContext(),PhotoSelect.class);
                photoSelect.putExtra("position", special);//검색창에 쓸 예외처리
                photoSelect.putExtra("song_name", explanation);//
                photoSelect.putExtra("position_main_search", position);//
                photoSelect.putExtra("url",url );
                getContext().startActivity(photoSelect);

            }
        });

        button_sheet.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(imm!=null)
                    imm.hideSoftInputFromWindow(view.getWindowToken(),0);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + explanation.replace(" ","") + "&tbm=isch"));
                    context.startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK));

                return false;
            }
        });

    }

    @SuppressLint("ResourceAsColor")
    public void setMusic(String ex, final String date, final String title){

        final String music = ex;

        button_music = findViewById(R.id.button_music);
        if(ex.equals("")) button_music.setTextColor(getResources().getColor(R.color.DarkModeGraytoBlack));
        button_music.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                cd = new CustomDialog(5, getContext(), position, imm, music);
                WindowManager.LayoutParams wm = cd.getWindow().getAttributes();  //다이얼로그의 높이 너비 설정하기위해
                wm.copyFrom(cd.getWindow().getAttributes());  //여기서 설정한값을 그대로 다이얼로그에 넣겠다는의미
                wm.width = width ;  //화면 너비의 절반
                wm.height = height / 2;  //화면 높이의 절반
                cd.show();

            }
        });
            button_music.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    content_string = "https://www.youtube.com/results?search_query=" + title.replace(" ","");

                    if(MainActivity.logged_in_db_id.equals("ssyp")){
                        content_string = content_string.concat("\n\n실황링크\nhttp://ssyp.synology.me:8812/worshipplus/record/" +
                                (date.substring(0,10)).replace(".","")
                                + ".mp3");


                        cd = new CustomDialog(5, getContext(), position, imm, "Youtube\n" + content_string);
                        WindowManager.LayoutParams wm = cd.getWindow().getAttributes();  //다이얼로그의 높이 너비 설정하기위해
                        wm.copyFrom(cd.getWindow().getAttributes());  //여기서 설정한값을 그대로 다이얼로그에 넣겠다는의미
                        wm.width = width ;  //화면 너비의 절반
                        wm.height = height / 2;  //화면 높이의 절반
                        cd.show();
                    }

                    else {
                        if(imm!=null)
                            imm.hideSoftInputFromWindow(v.getWindowToken(),0);
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(content_string));
                            context.startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK));
                    }

                    return false;
                }
            });


    }

    @SuppressLint("ResourceAsColor")
    public void setMusic(String ex, final int special, final String title){ //검색에서 기본정보일떄

        final String music = ex;

        button_music = findViewById(R.id.button_music);
        if(ex.equals("")) button_music.setTextColor(getResources().getColor(R.color.DarkModeGraytoBlack));
        button_music.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                cd = new CustomDialog(special, getContext(), position, imm, music);
                WindowManager.LayoutParams wm = cd.getWindow().getAttributes();  //다이얼로그의 높이 너비 설정하기위해
                wm.copyFrom(cd.getWindow().getAttributes());  //여기서 설정한값을 그대로 다이얼로그에 넣겠다는의미
                wm.width = width ;  //화면 너비의 절반
                wm.height = height / 2;  //화면 높이의 절반
                cd.show();

            }
        });

        button_music.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                content_string = "https://www.youtube.com/results?search_query=" + title.replace(" ","");

                    if(imm!=null)
                        imm.hideSoftInputFromWindow(v.getWindowToken(),0);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(content_string));
                    context.startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK));

                return false;
            }
        });

    }


}
