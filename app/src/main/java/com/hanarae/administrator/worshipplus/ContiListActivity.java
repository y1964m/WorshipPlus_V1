package com.hanarae.administrator.worshipplus;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
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
    static int more;
    Conti_List_Adapter adapter;
    Activity activity;


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

        int nightModeFlags =
                getApplicationContext().getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;

        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                break;
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                break;
        }

        activity = this;

        tempList = new Data();
        more = 1;

        recyclerView = findViewById(R.id.recycler_view_conti_list);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = LinearLayoutManager.class.cast(recyclerView.getLayoutManager());
                int totalItemCount = layoutManager.getItemCount();
                int lastVisible = layoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisible >= totalItemCount - 1) {//마지막 포지션일 때 리프레시

                    more++;
                    latch = new CountDownLatch(1);
                    searchDB_list = new SearchDB(5, getApplicationContext(), latch);
                    new AsyncTaskCancelTimerTask(searchDB_list, 10000, 1000, true).start();
                    searchDB_list.execute();
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    searchDB_list.cancel(true);
                    adapter.listData.clear();//초기화 하고, 안그러면 밑으로 똑같은 뷰가 계속 붙음

                    for (int i = tempList.getTitleArrayListSize()-1; i >= 0; i--) {
                        // 각 List의 값들을 data 객체에 set 해줍니다.
                        Data data = new Data();
                        data.setListDate(tempList.getTitleArrayListItem(i)); // 이때 날짜와 팀 한꺼번에 넣음
                        data.setListContent(tempList.getDateArrayListItem(i),tempList.getExplanationArrayListItem(i));

                        // 각 값이 들어간 data를 adapter에 추가합니다.
                        if(data.getListDate()!=null) adapter.addItem(data, activity);
                    }

                    recyclerView.post(new Runnable() {
                        public void run() {
                            // There is no need to use notifyDataSetChanged()
                            adapter.notifyDataSetChanged();
                            tempList.removeTitleArrayList();
                            tempList.removeDateArrayList();
                            tempList.removeExplanationArrayList();
                        }
                    });

                }
            }
        };

        adapter = new Conti_List_Adapter();
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(onScrollListener);

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

        //searchDB_list.cancel(true);


        for (int i = tempList.getTitleArrayListSize()-1; i >= 0; i--) {
            // 각 List의 값들을 data 객체에 set 해줍니다.
            Data data = new Data();
            data.setListDate(tempList.getTitleArrayListItem(i)); // 이때 날짜와 팀 한꺼번에 넣음
            data.setListContent(tempList.getDateArrayListItem(i),tempList.getExplanationArrayListItem(i));

            // 각 값이 들어간 data를 adapter에 추가합니다.
            if(data.getListDate()!=null) adapter.addItem(data,this);
        }

        adapter.notifyDataSetChanged();
        if(adapter.getItemCount()==0) Toast.makeText(ContiListActivity.this,"최근 1년 동안 작성된 콘티가 없습니다",Toast.LENGTH_SHORT).show();

        tempList.removeTitleArrayList();
        tempList.removeDateArrayList();
        tempList.removeExplanationArrayList();

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
