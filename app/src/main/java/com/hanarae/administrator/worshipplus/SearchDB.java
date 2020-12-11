package com.hanarae.administrator.worshipplus;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

public class SearchDB extends AsyncTask<Void, Integer, Void> {

    int case_number;
    Context context;
    CountDownLatch latch_DB;
    PowerManager.WakeLock mWakeLock;
    int error_code = 0;
    String photo_param;
    ProgressDialog pd;
    String server_address = "http://ssyp.synology.me:8812/";

    SearchDB(int case_number, Context context, CountDownLatch latch_DB, ProgressDialog pd){
        this.case_number = case_number;
        this.context = context;
        this.latch_DB = latch_DB;
        this.pd = pd;
    }

    SearchDB(int case_number, Context context, CountDownLatch latch_DB){
        this.case_number = case_number;
        this.context = context;
        this.latch_DB = latch_DB;
    }


    SearchDB(int case_number, Context context, CountDownLatch latch_DB, String param){
        this.case_number = case_number;
        this.context = context;
        this.latch_DB = latch_DB;
        this.photo_param = param;
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

        if(case_number==0) {//맨처음 메인콘티 불러오기 + 자동검색단어 저장

            /* 인풋 파라메터값 생성 */
            String param = "user_account="+ MainActivity.logged_in_db_id + "&team=" + MainActivity.team_info;

            try {
                /* 서버연결 */
             /*   URL url = new URL(
                        "http://y1964m.dothome.co.kr/worshipplus/search_latest_conti.php");*/
                URL url = new URL(
                        server_address+"worshipplus/initial_start.php");
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

                //서버에서 개인정보 가져오기
                //서버에서 정보 가져오기
                try {
                    JSONArray jsonArray = new JSONArray(data);

                    ArrayList tempDate = new ArrayList();
                    ArrayList tempExplanation = new ArrayList();
                    ArrayList tempMusic = new ArrayList();
                    // ArrayList tempSheet = new ArrayList();

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);


                        if(jsonObject.has("auto_title"))
                        MainActivity.autoText.add(jsonObject.getString("auto_title"));

                        else {
                            if(i == jsonArray.length()-1){

                                MainActivity.tempLatestConti.setBibleDate(jsonObject.getString("date"));
                                MainActivity.tempLatestConti.setBible(jsonObject.getString("bible"));
                                MainActivity.tempLatestConti.setSermon(jsonObject.getString("sermon"));
                                MainActivity.tempLatestConti.setLeader(jsonObject.getString("leader"));
                                break;


                            }else {
                                JSONObject next_jsonObject = jsonArray.getJSONObject(i + 1);

                                if(next_jsonObject.has("bible")){
                                    MainActivity.tempLatestConti.setBibleDate(next_jsonObject.getString("date"));
                                    MainActivity.tempLatestConti.setBible(next_jsonObject.getString("bible"));
                                    MainActivity.tempLatestConti.setSermon(next_jsonObject.getString("sermon"));
                                    MainActivity.tempLatestConti.setLeader(next_jsonObject.getString("leader"));

                                    MainActivity.tempLatestConti.addTitleArrayListItem(jsonObject.getString("title"));
                                    MainActivity.tempLatestConti.setChordArrayList(jsonObject.getString("chord"));

                                    tempDate.add(jsonObject.getString("id_date"));
                                    tempExplanation.add(jsonObject.getString("explanation"));
                                    tempMusic.add(jsonObject.getString("music"));
                                    //tempSheet.add(jsonObject.getString("sheet"));

                                    MainActivity.tempLatestConti.setDateArrayList(tempDate);
                                    MainActivity.tempLatestConti.setExplanationArrayList(tempExplanation);
                                    MainActivity.tempLatestConti.setMusicArrayList(tempMusic);
                                    MainActivity.tempLatestConti.addSheet(jsonObject.getString("sheet"));

                                    tempDate.clear();
                                    tempExplanation.clear();
                                    tempMusic.clear();
                                    // tempSheet.clear();
                                    break;
                                }

                                if (!(
                                        (jsonObject.getString("title").equalsIgnoreCase(next_jsonObject.getString("title")))&&
                                                (jsonObject.getString("chord").equalsIgnoreCase((next_jsonObject.getString("chord"))))
                                )) {
                                    MainActivity.tempLatestConti.addTitleArrayListItem(jsonObject.getString("title"));
                                    MainActivity.tempLatestConti.setChordArrayList(jsonObject.getString("chord"));

                                    tempDate.add(jsonObject.getString("id_date"));
                                    tempExplanation.add(jsonObject.getString("explanation"));
                                    tempMusic.add(jsonObject.getString("music"));
                                    //tempSheet.add(jsonObject.getString("sheet"));

                                    MainActivity.tempLatestConti.setDateArrayList(tempDate);
                                    MainActivity.tempLatestConti.setExplanationArrayList(tempExplanation);
                                    MainActivity.tempLatestConti.setMusicArrayList(tempMusic);
                                    MainActivity.tempLatestConti.addSheet(jsonObject.getString("sheet"));

                                    tempDate.clear();
                                    tempExplanation.clear();
                                    tempMusic.clear();
                                    //tempSheet.clear();

                                } else {

                                    tempDate.add(jsonObject.getString("id_date"));
                                    tempExplanation.add(jsonObject.getString("explanation"));
                                    tempMusic.add(jsonObject.getString("music"));
                                    //tempSheet.add(jsonObject.getString("sheet"));
                                    MainActivity.tempLatestConti.addSheet(jsonObject.getString("sheet"));

                                }
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

        if(case_number==1) {//메인콘티 불러오기

            /* 인풋 파라메터값 생성 */
            String param = "user_account="+ MainActivity.logged_in_db_id + "&team=" + MainActivity.team_info;

            try {
                /* 서버연결 */
             /*   URL url = new URL(
                        "http://y1964m.dothome.co.kr/worshipplus/search_latest_conti.php");*/
                URL url = new URL(
                        server_address+"worshipplus/search_latest_conti.php");
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

                //서버에서 개인정보 가져오기
                //서버에서 정보 가져오기
                try {
                    JSONArray jsonArray = new JSONArray(data);

                    ArrayList tempDate = new ArrayList();
                    ArrayList tempExplanation = new ArrayList();
                    ArrayList tempMusic = new ArrayList();
                   // ArrayList tempSheet = new ArrayList();

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        if(i == jsonArray.length()-1){

                            MainActivity.tempLatestConti.setBibleDate(jsonObject.getString("date"));
                            MainActivity.tempLatestConti.setBible(jsonObject.getString("bible"));
                            MainActivity.tempLatestConti.setSermon(jsonObject.getString("sermon"));
                            MainActivity.tempLatestConti.setLeader(jsonObject.getString("leader"));
                            break;


                        }else {
                            JSONObject next_jsonObject = jsonArray.getJSONObject(i + 1);

                            if(next_jsonObject.has("bible")){
                                MainActivity.tempLatestConti.setBibleDate(next_jsonObject.getString("date"));
                                MainActivity.tempLatestConti.setBible(next_jsonObject.getString("bible"));
                                MainActivity.tempLatestConti.setSermon(next_jsonObject.getString("sermon"));
                                MainActivity.tempLatestConti.setLeader(next_jsonObject.getString("leader"));

                                MainActivity.tempLatestConti.addTitleArrayListItem(jsonObject.getString("title"));
                                MainActivity.tempLatestConti.setChordArrayList(jsonObject.getString("chord"));

                                tempDate.add(jsonObject.getString("id_date"));
                                tempExplanation.add(jsonObject.getString("explanation"));
                                tempMusic.add(jsonObject.getString("music"));
                                //tempSheet.add(jsonObject.getString("sheet"));

                                MainActivity.tempLatestConti.setDateArrayList(tempDate);
                                MainActivity.tempLatestConti.setExplanationArrayList(tempExplanation);
                                MainActivity.tempLatestConti.setMusicArrayList(tempMusic);
                                MainActivity.tempLatestConti.addSheet(jsonObject.getString("sheet"));

                                tempDate.clear();
                                tempExplanation.clear();
                                tempMusic.clear();
                               // tempSheet.clear();
                                break;
                            }

                            if (!(
                                        (jsonObject.getString("title").equalsIgnoreCase(next_jsonObject.getString("title")))
                                                && (jsonObject.getString("chord").equalsIgnoreCase((next_jsonObject.getString("chord"))))
                                )) {
                                    MainActivity.tempLatestConti.addTitleArrayListItem(jsonObject.getString("title"));
                                    MainActivity.tempLatestConti.setChordArrayList(jsonObject.getString("chord"));

                                    tempDate.add(jsonObject.getString("id_date"));
                                    tempExplanation.add(jsonObject.getString("explanation"));
                                    tempMusic.add(jsonObject.getString("music"));
                                    //tempSheet.add(jsonObject.getString("sheet"));

                                    MainActivity.tempLatestConti.setDateArrayList(tempDate);
                                    MainActivity.tempLatestConti.setExplanationArrayList(tempExplanation);
                                    MainActivity.tempLatestConti.setMusicArrayList(tempMusic);
                                    MainActivity.tempLatestConti.addSheet(jsonObject.getString("sheet"));

                                    tempDate.clear();
                                    tempExplanation.clear();
                                    tempMusic.clear();
                                    //tempSheet.clear();

                                } else {

                                    tempDate.add(jsonObject.getString("id_date"));
                                    tempExplanation.add(jsonObject.getString("explanation"));
                                    tempMusic.add(jsonObject.getString("music"));
                                    //tempSheet.add(jsonObject.getString("sheet"));
                                    MainActivity.tempLatestConti.addSheet(jsonObject.getString("sheet"));

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

        if(case_number==2){ // notification 누르면 실행되는 검색

            /* 인풋 파라메터값 생성 */
            String param = "title=" + MainActivity.args.getString("someContent") +
                    "&chord=" + MainActivity.args.getString("someChord") +
                    "&tag=" + MainActivity.args.getString("someTag").replace(" ","") +
                    "&user_account="+ MainActivity.logged_in_db_id +
                    "&team="+ MainActivity.checked_search;

            Log.e("SENT DATA", param);
            try {
                /* 서버연결 */
               /* URL url = new URL(
                        "http://y1964m.dothome.co.kr/worshipplus/search.php");*/
                URL url = new URL(
                        server_address+"worshipplus/search.php");
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
                    ArrayList tempMusic = new ArrayList();
                    ArrayList tempSheet = new ArrayList();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        if(i == jsonArray.length()-1){
                            MainActivity.tempData.addTitleArrayListItem(jsonObject.getString("title"));
                            MainActivity.tempData.setChordArrayList(jsonObject.getString("chord"));

                            tempDate.add(jsonObject.getString("id_date"));
                            tempExplanation.add(jsonObject.getString("explanation"));
                            tempMusic.add(jsonObject.getString("music"));
                            tempSheet.add(jsonObject.getString("sheet"));

                            MainActivity.tempData.setDateArrayList(tempDate);
                            MainActivity.tempData.setExplanationArrayList(tempExplanation);
                            MainActivity.tempData.setMusicArrayList(tempMusic);
                            MainActivity.tempData.setSheetArrayList(tempSheet);

                            tempDate.clear();
                            tempExplanation.clear();
                            tempMusic.clear();
                            tempSheet.clear();

                            break;

                        }else {
                            JSONObject next_jsonObject = jsonArray.getJSONObject(i + 1);

                            if (!(
                                    (jsonObject.getString("title"))
                                            .equalsIgnoreCase
                                                    (next_jsonObject.getString("title"))
                                            &&(jsonObject.getString("chord")
                                            .equalsIgnoreCase(
                                                    (next_jsonObject.getString("chord"))))
                           /* (jsonObject.getString("title").substring(0,jsonObject.getString("title").indexOf("("))
                                    .equalsIgnoreCase
                                            (next_jsonObject.getString("title").substring(0,next_jsonObject.getString("title").indexOf("("))))
                                    &&(jsonObject.getString("chord").equalsIgnoreCase((next_jsonObject.getString("chord"))))*/
                            )) {
                                MainActivity.tempData.addTitleArrayListItem(jsonObject.getString("title"));
                                MainActivity.tempData.setChordArrayList(jsonObject.getString("chord"));

                                tempDate.add(jsonObject.getString("id_date"));
                                tempExplanation.add(jsonObject.getString("explanation"));
                                tempMusic.add(jsonObject.getString("music"));
                                tempSheet.add(jsonObject.getString("sheet"));

                                MainActivity.tempData.setDateArrayList(tempDate);
                                MainActivity.tempData.setExplanationArrayList(tempExplanation);
                                MainActivity.tempData.setMusicArrayList(tempMusic);
                                MainActivity.tempData.setSheetArrayList(tempSheet);

                                tempDate.clear();
                                tempExplanation.clear();
                                tempMusic.clear();
                                tempSheet.clear();

                            } else {

                                tempDate.add(jsonObject.getString("id_date"));
                                tempExplanation.add(jsonObject.getString("explanation"));
                                tempMusic.add(jsonObject.getString("music"));
                                tempSheet.add(jsonObject.getString("sheet"));

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


        if(case_number==3){ // 콘티 수정 할때 불러오는 코드

            MainActivity.temp_author="";

            /* 인풋 파라메터값 생성 */
            String param = "date=" + MainActivity.args.getString("someDate") +
                    "&user_account="+ MainActivity.logged_in_db_id +
                    "&author=" + MainActivity.logged_in_id;

            if(MainActivity.logged_in_id.equals(MainActivity.admin_id)){
                param = param + "&team="+ MainActivity.checked_search;
            }else param = param + "&team="+ MainActivity.team_info;

            try {
                /* 서버연결 */
               /* URL url = new URL(
                        "http://y1964m.dothome.co.kr/worshipplus/load_conti.php");*/
                URL url = new URL(
                        server_address+"worshipplus/load_conti.php");
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

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        if(i == jsonArray.length()-1){

                            MainActivity.tempConti.setBible(jsonObject.getString("bible"));
                            MainActivity.tempConti.setSermon(jsonObject.getString("sermon"));
                            MainActivity.tempConti.setLeader(jsonObject.getString("leader"));
                            break;

                        }else {
                            JSONObject next_jsonObject = jsonArray.getJSONObject(i + 1);

                            if(next_jsonObject.has("bible")){
                                MainActivity.tempConti.setBible(next_jsonObject.getString("bible"));
                                MainActivity.tempConti.setSermon(next_jsonObject.getString("sermon"));
                                MainActivity.tempConti.setLeader(next_jsonObject.getString("leader"));

                                MainActivity.tempConti.addTitleArrayListItem(jsonObject.getString("title"));
                                MainActivity.tempConti.setChordArrayList(jsonObject.getString("chord"));

                                tempDate.add(jsonObject.getString("id_date"));


                                MainActivity.tempConti.setDateArrayList(tempDate);
                                MainActivity.tempConti.addExplanation(jsonObject.getString("explanation"));
                                MainActivity.tempConti.addMusic(jsonObject.getString("music"));
                                MainActivity.tempConti.addSheet(jsonObject.getString("sheet"));

                                MainActivity.temp_author=jsonObject.getString("author"); //콘티 작성자 확인


                                tempDate.clear();
                                break;
                            }

                            if (!(
                                    (jsonObject.getString("title").equalsIgnoreCase(next_jsonObject.getString("title")))&&
                                            (jsonObject.getString("chord").equalsIgnoreCase((next_jsonObject.getString("chord"))))
                            )) {
                                MainActivity.tempConti.addTitleArrayListItem(jsonObject.getString("title"));
                                MainActivity.tempConti.setChordArrayList(jsonObject.getString("chord"));

                                tempDate.add(jsonObject.getString("id_date"));

                                MainActivity.tempConti.setDateArrayList(tempDate);
                                MainActivity.tempConti.addExplanation(jsonObject.getString("explanation"));
                                MainActivity.tempConti.addMusic(jsonObject.getString("music"));
                                MainActivity.tempConti.addSheet(jsonObject.getString("sheet"));

                                MainActivity.temp_author=jsonObject.getString("author"); //콘티 작성자 확인

                                tempDate.clear();


                            } else {

                                tempDate.add(jsonObject.getString("id_date"));
                                MainActivity.tempConti.addExplanation(jsonObject.getString("explanation"));
                                MainActivity.tempConti.addMusic(jsonObject.getString("music"));
                                MainActivity.tempConti.addSheet(jsonObject.getString("sheet"));
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

        //사진 불러오기 용
        if(case_number == 4){
            /* 인풋 파라메터값 생성 */

                String param = "song="+ photo_param.replace(" ","")
                        + "&user_account="+ MainActivity.logged_in_db_id
                        + "&team="+ MainActivity.checked_search;

            try {
                /* 서버연결 */
             /*   URL url = new URL(
                        "http://y1964m.dothome.co.kr/worshipplus/search_latest_conti.php");*/
                URL url = new URL(
                        server_address+"worshipplus/search_photo.php");
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

                //서버에서 개인정보 가져오기
                //서버에서 정보 가져오기
                try {
                    JSONArray jsonArray = new JSONArray(data);

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        /*MainActivity.tempPhoto.setSheet_url_Arraylist(jsonObject.getString("url"));
                        MainActivity.tempPhoto.addTitleArrayListItem(jsonObject.getString("song_date"));*/
                        PhotoSelect.data_photo.setSheet_url_Arraylist(jsonObject.getString("url"));
                        PhotoSelect.data_photo.addTitleArrayListItem(jsonObject.getString("song_date"));

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

        if(pd!=null) pd.dismiss();

        if(error_code != 0) {
            if(error_code==3) Toast.makeText(context,"인터넷이 불안정합니다",Toast.LENGTH_SHORT).show();
            //else Toast.makeText(context,"error: " + error_code,Toast.LENGTH_SHORT).show();
        }
        if(context==null) return;

    }

}