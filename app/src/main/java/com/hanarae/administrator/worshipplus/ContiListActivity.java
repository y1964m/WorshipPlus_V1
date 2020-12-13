package com.hanarae.administrator.worshipplus;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ContiListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    CountDownLatch latch;
    SearchDB searchDB_list;
    static Data tempList;
    static int more;
    Conti_List_Adapter adapter;
    Activity activity;
    ProgressDialog progressDialog;


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

        if(MainActivity.currentMode == AppCompatDelegate.MODE_NIGHT_NO) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        progressDialog = new ProgressDialog(ContiListActivity.this);
        progressDialog.setMessage("정보를 불러오는 중...");
        progressDialog.setCancelable(true);

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
                    searchDB_list = new SearchDB(5, getApplicationContext(), latch, progressDialog);
                    new AsyncTaskCancelTimerTask(searchDB_list, 10000, 1000, true).start();
                    searchDB_list.execute();
                    adapter.listData.clear();//초기화 하고, 안그러면 밑으로 똑같은 뷰가 계속 붙음
    /*                try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    searchDB_list.cancel(true);

                    for (int i = tempList.getTitleArrayListSize()-1; i >= 0; i--) {
                        // 각 List의 값들을 data 객체에 set 해줍니다.
                        Data data = new Data();
                        data.setListDate(tempList.getTitleArrayListItem(i)); // 이때 날짜와 팀 한꺼번에 넣음
                        data.setListContent(tempList.getDateArrayListItem(i),tempList.getExplanationArrayListItem(i));

                        // 각 값이 들어간 data를 adapter에 추가합니다.
                        if(data.getListDate()!=null) adapter.addItem(data, activity);
                    }*/

         /*           recyclerView.post(new Runnable() {
                        public void run() {
                            // There is no need to use notifyDataSetChanged()
                            adapter.notifyDataSetChanged();
                            tempList.removeTitleArrayList();
                            tempList.removeDateArrayList();
                            tempList.removeExplanationArrayList();
                        }
                    });*/

                }
            }
        };

        adapter = new Conti_List_Adapter();
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(onScrollListener);

        latch = new CountDownLatch(1);

        searchDB_list = new SearchDB(5, getApplicationContext(), latch, progressDialog);
        new AsyncTaskCancelTimerTask(searchDB_list, 10000, 1000, true).start();
        searchDB_list.execute();
        adapter.listData.clear();//초기화 하고, 안그러면 밑으로 똑같은 뷰가 계속 붙음
/*        try {
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

        adapter.notifyDataSetChanged();*/
     /*   if(adapter.getItemCount()==0) Toast.makeText(ContiListActivity.this,"최근 1년 동안 작성된 콘티가 없습니다",Toast.LENGTH_SHORT).show();

        tempList.removeTitleArrayList();
        tempList.removeDateArrayList();
        tempList.removeExplanationArrayList();*/

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
                    if(progressDialog.isShowing()) progressDialog.dismiss();
                    Toast.makeText(ContiListActivity.this,"네트워크 에러",Toast.LENGTH_SHORT).show();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }



    public class SearchDB extends AsyncTask<Void, Integer, Void> {

        int case_number;
        Context context;
        CountDownLatch latch_DB;
        PowerManager.WakeLock mWakeLock;
        int error_code = 0;
        String photo_param;
        ProgressDialog pd;
        String server_address = "http://ssyp.synology.me:8812/";


        SearchDB(int case_number, Context context, CountDownLatch latch_DB){
            this.case_number = case_number;
            this.context = context;
            this.latch_DB = latch_DB;
        }

        SearchDB(int case_number, Context context, CountDownLatch latch_DB, ProgressDialog pd){
            this.case_number = case_number;
            this.context = context;
            this.latch_DB = latch_DB;
            this.pd = pd;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(pd!=null) {
                pd.show();
            }

            //cpu 잠들지 않도록 하는 처리
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            mWakeLock.acquire();

        }

        @Override
        protected Void doInBackground(Void... unused) {

            if(case_number==5){ // 콘티 리스트로 불러오기

            /*long now = System.currentTimeMillis();
            Date mDate = new Date(now);
            SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy.MM");
            String getTime = simpleDate.format(mDate);*/

                /* 인풋 파라메터값 생성 */
                String param = "more=" + ContiListActivity.more + // 리스트 로드 배수
                        "&user_account="+ MainActivity.logged_in_db_id +
                        //"&team="+ MainActivity.team_info + // 소속팀 콘티만 부르고 싶을때
                        "&team="+ MainActivity.checked_search +
                        "&author=" + MainActivity.logged_in_id;

                Log.e("SEND DATA", param);

                try {
                    /* 서버연결 */
               /* URL url = new URL(
                        "http://y1964m.dothome.co.kr/worshipplus/load_conti.php");*/
                    URL url = new URL(
                            server_address+"worshipplus/load_list.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.connect();

                    /* 안드로이드 -> 서버 파라메터값 전달 */
                    OutputStream outs = conn.getOutputStream();
                    outs.write(param.getBytes("UTF-8"));
                    outs.flush();
                    outs.close();

                    /* 서버 -> 안드로이드 파라메터값 전달 */
                    InputStream is = null;
                    BufferedReader in = null;
                    String data = "";

                    is = conn.getInputStream();
                    in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                    String line = null;
                    StringBuffer buff = new StringBuffer();


                    while ((line = in.readLine()) != null) {
                        buff.append(line + "\n");
                    }
                    data = buff.toString().trim();

                    /* 서버에서 응답 */
                    Log.e("RECV DATA", data);

                    //서버에서 정보 가져오기
                    try {
                        JSONArray jsonArray = new JSONArray(data);

                        ArrayList tempDate = new ArrayList();
                        ArrayList tempExplanation = new ArrayList();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            if(i == jsonArray.length()-1){
                                ContiListActivity.tempList.addTitleArrayListItem(jsonObject.getString("id_date"));
                                //ContiListActivity.tempList.setChordArrayList(jsonObject.getString("chord"));

                                tempDate.add(jsonObject.getString("title")); //콘티 내 곡들
                                tempExplanation.add(jsonObject.getString("chord")); // 콘티 내 코드이다

                                ContiListActivity.tempList.setDateArrayList(tempDate);
                                ContiListActivity.tempList.setExplanationArrayList(tempExplanation);

                                tempDate.clear();
                                tempExplanation.clear();

                                break;

                            }else {
                                JSONObject next_jsonObject = jsonArray.getJSONObject(i + 1);

                                if (!(
                                        (jsonObject.getString("id_date").substring(0,jsonObject.getString("id_date").lastIndexOf("/")))
                                                .equalsIgnoreCase
                                                        (next_jsonObject.getString("id_date").substring(0,jsonObject.getString("id_date").lastIndexOf("/")))

                                )) {
                                    ContiListActivity.tempList.addTitleArrayListItem(jsonObject.getString("id_date")); // 날짜다

                                    tempDate.add(jsonObject.getString("title")); //콘티 내 곡들
                                    tempExplanation.add(jsonObject.getString("chord")); // 콘티 내 코드이다

                                    ContiListActivity.tempList.setDateArrayList(tempDate);
                                    ContiListActivity.tempList.setExplanationArrayList(tempExplanation);

                                    tempDate.clear();
                                    tempExplanation.clear();



                                } else {

                                    tempDate.add(jsonObject.getString("title")); //콘티 내 곡들
                                    tempExplanation.add(jsonObject.getString("chord")); // 콘티 내 코드이다

                                }
                            }

                        }

                        mWakeLock.release();
                        latch_DB.countDown();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        latch_DB.countDown();
                        error_code = 1;
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    latch_DB.countDown();
                    error_code = 2;
                } catch (IOException e) {
                    e.printStackTrace();
                    latch_DB.countDown();
                    error_code = 3;
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //super.onPostExecute(aVoid);

            for (int i = tempList.getTitleArrayListSize()-1; i >= 0; i--) {
                // 각 List의 값들을 data 객체에 set 해줍니다.
                Data data = new Data();
                data.setListDate(tempList.getTitleArrayListItem(i)); // 이때 날짜와 팀 한꺼번에 넣음
                data.setListContent(tempList.getDateArrayListItem(i),tempList.getExplanationArrayListItem(i));

                // 각 값이 들어간 data를 adapter에 추가합니다.
                if(data.getListDate()!=null) adapter.addItem(data,ContiListActivity.this);
            }

        /*    adapter.notifyDataSetChanged();
            tempList.removeTitleArrayList();
            tempList.removeDateArrayList();
            tempList.removeExplanationArrayList();*/

            if(adapter.getItemCount()==0) Toast.makeText(ContiListActivity.this,"최근 1년 동안 작성된 콘티가 없습니다",Toast.LENGTH_SHORT).show();

            recyclerView.post(new Runnable() {
                public void run() {
                    // There is no need to use notifyDataSetChanged()
                    adapter.notifyDataSetChanged();
                    tempList.removeTitleArrayList();
                    tempList.removeDateArrayList();
                    tempList.removeExplanationArrayList();
                }
            });

            if(mWakeLock.isHeld())mWakeLock.release();
            if(pd!=null) pd.dismiss();

            if(error_code != 0) {
                if(error_code==3) Toast.makeText(context,"인터넷이 불안정합니다",Toast.LENGTH_SHORT).show();
                //else Toast.makeText(context,"error: " + error_code,Toast.LENGTH_SHORT).show();
            }
            if(context==null) return;

        }

    }


}
