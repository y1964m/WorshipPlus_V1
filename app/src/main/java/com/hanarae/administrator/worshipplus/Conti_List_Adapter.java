package com.hanarae.administrator.worshipplus;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class Conti_List_Adapter extends RecyclerView.Adapter<Conti_List_Adapter.ItemViewHolder> {

    ArrayList<Data> listData = new ArrayList<>();

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

    void addItem(Data data) {
        // 외부에서 item을 추가시킬 함수입니다.
        listData.add(data);
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
            conti_list_team.setText(data.getListTeam());
            conti_list_content.setText(data.getListContent());

            for (int i =0; i < data.getDateSize(); i++){







             /*   TempList tempList = new TempList(itemView.getContext(), width, height, imm,1, getAdapterPosition(), i);
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
                container.addView(tempList);*/
            }

        }
    }

}


