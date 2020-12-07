package com.hanarae.administrator.worshipplus;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;


public class Search_Recycler_Adapter extends RecyclerView.Adapter<Search_Recycler_Adapter.ItemViewHolder> {

    // adapter에 들어갈 list 입니다.
    public static ArrayList<Data> listData = new ArrayList<>();
    public int case_num;
    InputMethodManager imm;
    int width, height;

    Search_Recycler_Adapter(int case_num, InputMethodManager imm, int width, int height){
        this.case_num = case_num;
        this.imm = imm;
        this.width=width;
        this.height=height;
        listData.clear();
        return;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // LayoutInflater를 이용하여 전 단계에서 만들었던 item.xml을 inflate 시킵니다.
        // return 인자는 ViewHolder 입니다.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recylcer_output_songlist, parent, false);
        if(case_num == 1) view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_conti_main, parent, false);
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
        private ToggleButton toggleButton;
        private LinearLayout container;
        private TextView song_date;
        private TextView numbering;

        ItemViewHolder(View itemView) {
            super(itemView);

            //변수정의
            if(case_num == 1){//메인화면 최근 콘티 로드 시
                container = itemView.findViewById(R.id.linear_layout_container_main);
                song_title = itemView.findViewById(R.id.song_title_main);
                song_chord = itemView.findViewById(R.id.song_chord_main);
                numbering = itemView.findViewById(R.id.song_check_main);
                //toggleButton = itemView.findViewById(R.id.toggleButton_song_detail_main);
            }
            else {
                container = itemView.findViewById(R.id.linear_layout_container);
                song_title = itemView.findViewById(R.id.song_title);
                song_chord = itemView.findViewById(R.id.song_chord);
                toggleButton = itemView.findViewById(R.id.toggleButton_song_detail);
            }

            //linearLayout = itemView.findViewById(R.id.linearLayout_song_hidden);

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
            toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(imm!=null)
                        imm.hideSoftInputFromWindow(buttonView.getWindowToken(),0);
                    if (isChecked) {
                        container.setVisibility(View.VISIBLE);

                    }
                    else container.setVisibility(View.GONE);
                }
            });

        }

        void onBind(final Data data) {

            container.removeAllViews();

            song_title.setText(data.getTitle());
            song_chord.setText(data.getContent());
            song_chord.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) { // 길게 누르면 수정가능하게
                    if(MainActivity.logged_in_db_id.equals("ssyp")){

                        CustomDialog cd = new CustomDialog(1, itemView.getContext(), getAdapterPosition(), imm, data.getContent());
                        WindowManager.LayoutParams wm = cd.getWindow().getAttributes();  //다이얼로그의 높이 너비 설정하기위해
                        wm.copyFrom(cd.getWindow().getAttributes());  //여기서 설정한값을 그대로 다이얼로그에 넣겠다는의미
                        wm.width = width ;  //화면 너비의 절반
                        wm.height = height / 2;  //화면 높이의 절반
                        cd.show();

                    }

                    return false;
                }
            });
            toggleButton.setChecked(false);

                for (int i =0; i < data.getDateSize(); i++){
                    TempList tempList = new TempList(itemView.getContext(), width, height, imm,1, getAdapterPosition(), i);
                    if(data.getDate(i).equals("기본정보")){
                        tempList.setDate(data.getDate(i),data.getTitle());
                        tempList.setExplanation(data.getExplanation(i),6);
                        tempList.setMusic(data.getMusic(i),7);
                        tempList.setSheet(data.getTitle(),999,data.getSheet(i));// 악보검색할때 곡제목 전달
                    }
                    else {
                        tempList.setDate(data.getDate(i));
                        tempList.setExplanation(data.getExplanation(i));
                        tempList.setMusic(data.getMusic(i),data.getDate(i));
                        tempList.setSheet(data.getTitle(),888, data.getSheet(i));//메인콘티와 비슷하게 레이아웃
                    }
                    tempList.setDate(data.getDate(i));
                    container.addView(tempList);
                }

            song_date = itemView.findViewById(R.id.textView_conti_date);
            song_date.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(imm!=null)
                        imm.hideSoftInputFromWindow(v.getWindowToken(),0);
                }
            });

        }
    }
}
