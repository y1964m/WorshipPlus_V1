package com.hanarae.administrator.worshipplus;

import android.app.AlertDialog;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;


public class Photo_Recycler_Adapter extends RecyclerView.Adapter<Photo_Recycler_Adapter.ItemViewHolder> {

    // adapter에 들어갈 list 입니다.
    public static ArrayList<Data> listData = new ArrayList<>();
    public int case_num;
    InputMethodManager imm;
    int width, height;
    CountDownLatch latch;


    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // LayoutInflater를 이용하여 전 단계에서 만들었던 item.xml을 inflate 시킵니다.
        // return 인자는 ViewHolder 입니다.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_photo, parent, false);

        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        // Item을 하나, 하나 보여주는(bind 되는) 함수입니다.
        holder.onBind(listData.get(position), position);
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

        private TextView photo_name;
        private Button button;


        ItemViewHolder(View itemView) {
            super(itemView);

            //변수정의
            photo_name = itemView.findViewById(R.id.textview_photo_name);
            button = itemView.findViewById(R.id.button_photo_delete);

        }

        void onBind(final Data data, final int position) {

            if(data.getTitle()==null){//사진 날자정보 서버에 없을때
                photo_name.setText(data.getTitle());
                photo_name.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        /*PhotoSelect.imageView.setImageURI(data.getSingle_sheet_temp());
                        PhotoSelect.isFile = false;
                        PhotoSelect.imageView.setVisibility(View.VISIBLE);
                        PhotoSelect.webView.setVisibility(View.GONE);*/
                    }
                });

            }

            else{//서버에 사진 있을때
                photo_name.setText(data.getTitle());
                photo_name.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        PhotoSelect.webView.loadUrl(data.getSingle_Sheet_url());
                        PhotoSelect.isFile = false;
                        PhotoSelect.imageView.setVisibility(View.GONE);
                        PhotoSelect.webView.setVisibility(View.VISIBLE);
                    }
                });

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(PhotoSelect.context);
                        builder.setTitle("삭제하시겠습니까?");
                        builder.setPositiveButton("취소", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int id){

                            }
                        });

                        builder.setNegativeButton("삭제", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {
                                String db_data="";

                                db_data += "&url="+listData.get(getAdapterPosition()).getSingle_Sheet_url();

                                latch = new CountDownLatch(1);
                                InputDB inputDB = new InputDB(latch, db_data,3);
                                inputDB.execute();

                                try {
                                    latch.await();

                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                inputDB.cancel(true);

                                listData.remove(position);
                                notifyDataSetChanged();

                                PhotoSelect.imageView.setVisibility(View.VISIBLE);
                                PhotoSelect.webView.setVisibility(View.GONE);
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                    }
                });
            }

        }
    }
}
