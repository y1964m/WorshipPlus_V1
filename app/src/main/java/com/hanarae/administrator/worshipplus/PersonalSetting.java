package com.hanarae.administrator.worshipplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PersonalSetting extends LinearLayout {

    TextView team_id;
    CheckBox write,search,notify;
    SharedPreferences sharedPreferences;
    static SharedPreferences.Editor editor;

    public PersonalSetting(Context context, final String content) {
        super(context);

        sharedPreferences = getContext().getSharedPreferences("login",0);
        editor = sharedPreferences.edit();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.setting_temp_layout,this);

        team_id = findViewById(R.id.team_id);
        team_id.setText(content);

        write = findViewById(R.id.checkbox_write);
        if(sharedPreferences.getString("write","team").equals(content)) write.setChecked(true);
        write.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(!sharedPreferences.getString("write","team").equals("team") && !sharedPreferences.getString("write","team").equals(content)){
                        //중복장지 코드, 선택한게 이것도 아니고 비어있지도 않으면, 다른것을 선택하려는 것 그럴때에는 선택 못하게 막고 헤지하고 하라함
                        write.setChecked(false);
                        Toast.makeText(getContext(),"작성권한은 하나만 선택할 수 있습니다\n기존체크를 해제한 후, 다시 체크해주세요",Toast.LENGTH_LONG).show();
                    }
                    else {
                        editor.putString("write",content);
                        MainActivity.team_info = content;
                    }
                }
                else editor.putString("write",null);
                editor.apply();
            }
        });
        search = findViewById(R.id.checkbox_search);
        if(sharedPreferences.getInt(content+"_search",0) != 0) search.setChecked(true);
        search.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) editor.putInt(content+"_search",1);
                else editor.putInt(content+"_search",0);
                editor.apply();
            }
        });
        notify = findViewById(R.id.checkbox_notify);
        if(sharedPreferences.getInt(content+"_notify",0) != 0) notify.setChecked(true);
        notify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) editor.putInt(content+"_notify",1);
                else editor.putInt(content+"_notify",0);
                editor.apply();
            }
        });

    }
}
