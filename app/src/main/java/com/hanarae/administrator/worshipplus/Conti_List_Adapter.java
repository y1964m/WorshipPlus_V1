package com.hanarae.administrator.worshipplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class Conti_List_Adapter extends RecyclerView.Adapter<Conti_List_Adapter.ItemViewHolder> {

    ArrayList<Data> listData = new ArrayList<>();
    Activity activity;

    @NonNull
    @Override
    public Conti_List_Adapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // LayoutInflater를 이용하여 전 단계에서 만들었던 item.xml을 inflate 시킵니다.
        // return 인자는 ViewHolder 입니다.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_conti_list, parent, false);
        return new Conti_List_Adapter.ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Conti_List_Adapter.ItemViewHolder holder, int position) {
        // Item을 하나, 하나 보여주는(bind 되는) 함수입니다.
        holder.onBind(listData.get(position));
    }

    @Override
    public int getItemCount() {
        // RecyclerView의 총 개수 입니다.
        return listData.size();
    }

    void addItem(Data data, Activity activity) {
        // 외부에서 item을 추가시킬 함수입니다.
        listData.add(data);
        this.activity = activity;
    }

    // RecyclerView의 핵심인 ViewHolder 입니다.
    // 여기서 subView를 setting 해줍니다.
    class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView conti_list_date;
        private TextView conti_list_team;
        private TextView conti_list_content;

        ItemViewHolder(View itemView) {
            super(itemView);

            //변수정의
            conti_list_date = itemView.findViewById(R.id.conti_list_date);
            conti_list_team = itemView.findViewById(R.id.conti_list_team);
            conti_list_content = itemView.findViewById(R.id.conti_list_content);

        }

        void onBind(final Data data) {

            conti_list_date.setText(data.getListDate());
            conti_list_date.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra("dateToLoad",data.getListDate()+"/"+data.getListTeam());
                    activity.setResult(Activity.RESULT_OK,intent);
                    activity.finish();
                    return false;
                }
            });
            conti_list_team.setText(data.getListTeam());
            conti_list_content.setText(data.getListContent());

        }
    }

}


