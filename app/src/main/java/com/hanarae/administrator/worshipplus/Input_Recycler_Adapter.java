package com.hanarae.administrator.worshipplus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class Input_Recycler_Adapter extends RecyclerView.Adapter<Input_Recycler_Adapter.ItemViewHolder> implements ItemTouchHelperListener{

    // adapter에 들어갈 list 입니다.
    public static ArrayList<Data> listData = new ArrayList<>();
    public int case_num;
    InputMethodManager imm;
    CustomDialog cd;
    Context context;
    int width, height;
    boolean num_on=false;


    Input_Recycler_Adapter(int case_num, InputMethodManager imm, int width, int height, Context context){
        this.case_num = case_num;
        this.imm = imm;
        this.context = context;
        this.width=width;
        this.height=height;
        return;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_input_conti, parent, false);


        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {

        holder.onBind(listData.get(position));

    }


    @Override
    public int getItemCount() {
        return listData.size();
    }


    void addItem(Data data) {
        // 외부에서 item을 추가시킬 함수입니다.
        listData.add(data);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if(fromPosition < 0 || fromPosition >= listData.size() || toPosition < 0 || toPosition >= listData.size()){
            return false;
        }

        Data fromItem = listData.get(fromPosition);
        listData.remove(fromPosition);
        listData.add(toPosition, fromItem);
        notifyItemMoved(fromPosition, toPosition);

        return true;
    }

    @Override
    public void onItemRemove(int position) {

        listData.remove(position);
        notifyItemRemoved(position);

    }


    // RecyclerView의 핵심인 ViewHolder 입니다.
    // 여기서 subView를 setting 해줍니다.
    class ItemViewHolder extends RecyclerView.ViewHolder {

        private AutoCompleteTextView song_title;
        private EditText song_chord;
        private ToggleButton toggleButton;
        private LinearLayout container;
        private TextView numbering;
        private Button button_sheet;
        private Button button_explanation;
        private Button button_music;


        ItemViewHolder (View itemView) {
            super(itemView);

            ArrayAdapter auto = new ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line,MainActivity.autoText);
            container = itemView.findViewById(R.id.linear_layout_container_input);

            song_title = itemView.findViewById(R.id.song_title_input);
            song_title.setAdapter(auto);

            song_chord = itemView.findViewById(R.id.song_chord_input);

            numbering = itemView.findViewById(R.id.song_check_input);

            toggleButton = itemView.findViewById(R.id.toggleButton_song_detail_input);
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

            //container.removeAllViews();
            View.OnClickListener keyboardHider = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(imm!=null)
                        imm.hideSoftInputFromWindow(v.getWindowToken(),0);
                }
            };

            if(data.getContent().isEmpty() && num_on)song_chord.setHintTextColor(Color.RED);
            else song_chord.setHintTextColor(Color.GRAY);

            song_title.setText(data.getTitle());
            song_title.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    listData.get(getAdapterPosition()).setTitle(song_title.getText().toString());
                }
            });

            song_chord.setText(data.getContent());
            song_chord.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    listData.get(getAdapterPosition()).setContent(song_chord.getText().toString());
                }
            });

            numbering.setOnClickListener(keyboardHider);
            if(num_on) numbering.setText(""+getAdapterPosition());
            else numbering.setText("#");

            toggleButton.setChecked(false);

            button_sheet = itemView.findViewById(R.id.button_sheet_input);
            button_music = itemView.findViewById(R.id.button_music_input);

            button_explanation = itemView.findViewById(R.id.button_explanation_input);
            button_explanation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cd = new CustomDialog(0, context, getAdapterPosition(),imm, data.getSingle_explanation());
                    WindowManager.LayoutParams wm = cd.getWindow().getAttributes();  //다이얼로그의 높이 너비 설정하기위해
                    wm.copyFrom(cd.getWindow().getAttributes());  //여기서 설정한값을 그대로 다이얼로그에 넣겠다는의미
                    wm.width = width ;  //화면 너비의 절반
                    wm.height = height / 2;  //화면 높이의 절반
                    cd.show();
                }
            });


            button_sheet = itemView.findViewById(R.id.button_sheet_input);
            button_sheet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent photoSelect = new Intent(context,PhotoSelect.class);
                    photoSelect.putExtra("song_name",data.getTitle().toString());
                    photoSelect.putExtra("position", getAdapterPosition());
                    photoSelect.putExtra("url", data.getSingle_sheet());
                    context.startActivity(photoSelect);

                    /*cd = new CustomDialog(1,context, getAdapterPosition(),imm);
                    WindowManager.LayoutParams wm = cd.getWindow().getAttributes();  //다이얼로그의 높이 너비 설정하기위해
                    wm.copyFrom(cd.getWindow().getAttributes());  //여기서 설정한값을 그대로 다이얼로그에 넣겠다는의미
                    wm.width = width ;  //화면 너비의 절반
                    wm.height = height / 2;  //화면 높이의 절반
                    cd.show();*/
                }
            });


            button_music = itemView.findViewById(R.id.button_music_input);
            button_music.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cd = new CustomDialog(2,context, getAdapterPosition(),imm,data.getSingle_music());
                    WindowManager.LayoutParams wm = cd.getWindow().getAttributes();  //다이얼로그의 높이 너비 설정하기위해
                    wm.copyFrom(cd.getWindow().getAttributes());  //여기서 설정한값을 그대로 다이얼로그에 넣겠다는의미
                    wm.width = width ;  //화면 너비의 절반
                    wm.height = height / 2;  //화면 높이의 절반
                    cd.show();
                }
            });




        }
    }


}
