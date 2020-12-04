package com.hanarae.administrator.worshipplus;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.CountDownLatch;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ContiListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    CountDownLatch latch;
    SearchDB searchDB_list;
    static Data tempList;
    Conti_List_Adapter adapter;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.listData.clear();
        finishAndRemoveTask();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conti_list);

        tempList = new Data();

        recyclerView = findViewById(R.id.recycler_view_conti_list);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new Conti_List_Adapter();
        recyclerView.setAdapter(adapter);
        latch = new CountDownLatch(1);

        searchDB_list = new SearchDB(5, getApplicationContext(), latch);
        new AsyncTaskCancelTimerTask(searchDB_list, 10000, 1000, true).start();
        searchDB_list.execute();
        adapter.listData.clear();//초기화 하고, 안그러면 밑으로 똑같은 뷰가 계속 붙음
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        searchDB_list.cancel(true);


        for (int i = tempList.getTitleArrayListSize()-1; i > 0; i--) {
            // 각 List의 값들을 data 객체에 set 해줍니다.
            Data data = new Data();
            data.setListDate(tempList.getTitleArrayListItem(i)); // 이때 날짜와 팀 한꺼번에 넣음
            data.setListContent(tempList.getDateArrayListItem(i),tempList.getExplanationArrayListItem(i));

            // 각 값이 들어간 data를 adapter에 추가합니다.
            if(data.getListDate()!=null) adapter.addItem(data);
        }

        adapter.notifyDataSetChanged();


    }

    //네트워크 작업하는 테스크에 타임아웃 걸기
    class AsyncTaskCancelTimerTask extends CountDownTimer {
        private AsyncTask asyncTask;
        private boolean interrupt;

        private AsyncTaskCancelTimerTask(AsyncTask asyncTask, long startTime, long interval) {
            super(startTime, interval);
            this.asyncTask = asyncTask;
        }

        private AsyncTaskCancelTimerTask(AsyncTask asyncTask, long startTime, long interval, boolean interrupt) {
            super(startTime, interval);
            this.asyncTask = asyncTask;
            this.interrupt = interrupt;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if(asyncTask == null) {
                this.cancel();
                return;
            }

            if(asyncTask.isCancelled())
                this.cancel();

            if(asyncTask.getStatus() == AsyncTask.Status.FINISHED)
                this.cancel();
        }

        @Override
        public void onFinish() {
            if(asyncTask == null || asyncTask.isCancelled() )
                return;

            try {
                if(asyncTask.getStatus() == AsyncTask.Status.FINISHED)
                    return;

                if(asyncTask.getStatus() == AsyncTask.Status.PENDING ||
                        asyncTask.getStatus() == AsyncTask.Status.RUNNING ) {

                    asyncTask.cancel(interrupt);
                    Toast.makeText(ContiListActivity.this,"네트워크 에러",Toast.LENGTH_SHORT).show();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }


}
