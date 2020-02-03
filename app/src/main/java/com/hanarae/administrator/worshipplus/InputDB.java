package com.hanarae.administrator.worshipplus;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

public class InputDB extends AsyncTask<Void,Integer,Void>{

    Context context;
    CountDownLatch latch;
    PowerManager.WakeLock mWakeLock;
    String db_data;
    int case_num;


    InputDB (Context context, CountDownLatch latch, String db_data, int case_num){
        this.context = context;
        this.latch = latch;
        this.db_data=db_data;
        this.case_num = case_num;
    }

    InputDB (CountDownLatch latch, String db_data, int case_num){
        this.latch = latch;
        this.db_data=db_data;
        this.case_num = case_num;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        //cpu 잠들지 않도록 하는 처리
        if(context!=null){
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            mWakeLock.acquire();
        }



    }

    @Override
    protected Void doInBackground(Void... voids) {

        /* 인풋 파라메터값 생성 */
        String param = "";
        switch (case_num){
            case 0: // 일반 콘티 추가
                 param = "date=" + MainActivity.args.getString("someDate") +
                        "&bible=" + MainActivity.args.getString("someBible") +
                        "&sermon=" + MainActivity.args.getString("someTitle1") +
                        "&leader=" + MainActivity.args.getString("someTitle2") +
                        db_data +
                         "&user_account="+ MainActivity.logged_in_db_id;
                 param = param.replace("null", "");
                 Log.e("SENT DATA", param);
                 break;
            case 1: // 곡 따로 추가할때
                param = db_data+
                        "&user_account="+ MainActivity.logged_in_db_id;
                param = param.replace("null", "");
                break;
            case 2://곡 추가 후 기본정보에 상세내용 넣어서 업데이트
                param = db_data +"&remark=updated"+
                        "&user_account="+ MainActivity.logged_in_db_id;
                param = param.replace("null", "");
                break;
            case 3://악보 및 사진 삭제 시
                param = db_data+
                        "&user_account="+ MainActivity.logged_in_db_id;
                param = param.replace("null", "");
                Log.e("SENT DATA", param);
                break;
        }

        try {
            /* 서버연결 */
            /*URL url = new URL(
                    "http://y1964m.dothome.co.kr/worshipplus/inputDB.php");*/
            URL url = new URL(
                    "http://ssyp.synology.me:8812/worshipplus/inputDB.php");
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
            if(data.contains("곡중복"))PraiseSearch.double_check =1;
            else PraiseSearch.double_check=0;

            if(data.contains("1064")) ThirdFragment.isUploaded = false;
            else ThirdFragment.isUploaded = true;

            latch.countDown();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if(context==null) return;
    }

}

