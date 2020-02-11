package com.hanarae.administrator.worshipplus;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class Latest_Conti_Adapter extends RecyclerView.Adapter<Latest_Conti_Adapter.ItemViewHolder> {

    // adapter에 들어갈 list 입니다.
    public static ArrayList<Data> listData = new ArrayList<>();
    public int case_num;
    InputMethodManager imm;
    int width, height;
    Context context;

    Latest_Conti_Adapter(int case_num, InputMethodManager imm, int width, int height, Context context){
        this.case_num = case_num;
        this.imm = imm;
        this.width=width;
        this.height=height;
        this.context=context;
        listData.clear();
        return;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // LayoutInflater를 이용하여 전 단계에서 만들었던 item.xml을 inflate 시킵니다.
        // return 인자는 ViewHolder 입니다.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_conti_main, parent, false);
        //메인화면 콘티

        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        // Item을 하나, 하나 보여주는(bind 되는) 함수입니다.
        holder.onBind(listData.get(position));
    }

    @Override
    public int getItemCount() {
        // RecyclerView의 총 개수 입니다.
        return listData.size();
    }

    void addItem(Data data) {
        // 외부에서 item을 추가시킬 함수입니다.
        listData.add(data);
    }

    // RecyclerView의 핵심인 ViewHolder 입니다.
    // 여기서 subView를 setting 해줍니다.
    class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView song_title;
        private TextView song_chord;
        //private ToggleButton toggleButton;
        private LinearLayout container;
        private TextView song_date, numbering;

        ItemViewHolder(View itemView) {
            super(itemView);

            //변수정의
            if(case_num == 1){
                container = itemView.findViewById(R.id.linear_layout_container_main);
                song_title = itemView.findViewById(R.id.song_title_main);
                song_chord = itemView.findViewById(R.id.song_chord_main);
                numbering = itemView.findViewById(R.id.song_check_main);
                //toggleButton = itemView.findViewById(R.id.toggleButton_song_detail_main);
            }

            song_title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(imm!=null)
                        imm.hideSoftInputFromWindow(v.getWindowToken(),0);
                }
            });
            song_chord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(imm!=null)
                        imm.hideSoftInputFromWindow(v.getWindowToken(),0);
                }
            });
        /*    toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(imm!=null)
                        imm.hideSoftInputFromWindow(buttonView.getWindowToken(),0);
                    if (isChecked) {
                        container.setVisibility(View.VISIBLE);

                    }
                    else container.setVisibility(View.GONE);
                }
            });*/

        }

        void onBind(final Data data) {

            container.removeAllViews();

            /*if(data.getTitle().contains("(")) song_title.setText(data.getTitle().substring(0,data.getTitle().indexOf("(")));
            else song_title.setText(data.getTitle());*/
            song_title.setText(data.getTitle());
            song_chord.setText(data.getContent());
            //toggleButton.setChecked(false);
            numbering.setText(getAdapterPosition()+".  ");

            for (int i =0; i < data.getDateSize(); i++){
                TempList tempList = new TempList(itemView.getContext(), width, height, imm,0, getAdapterPosition(),0);

                tempList.setExplanation(data.getExplanation(i));
              /*  if(data.getTitle().contains("(")||data.getTitle().contains(")")){
                    String title_sub=data.getTitle().substring(data.getTitle().indexOf("("),data.getTitle().indexOf(")")+1);
                    tempList.setDate(title_sub);
                }else tempList.setDate("");
                    //tempList.setDate(data.getDate(i));*/

                tempList.setMusic(data.getMusic(i));
                tempList.setSheet(data.getTitle(),888, data.getSingle_Sheet_url());
                container.addView(tempList);
            }

            song_date = itemView.findViewById(R.id.textView_conti_date);
            song_date.setText("온라인 검색");
            song_date.setClickable(true);
            song_date.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(imm!=null)
                        imm.hideSoftInputFromWindow(v.getWindowToken(),0);
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q="+data.getTitle()));
                        context.startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK));
                }
            });

        }
    }
}

