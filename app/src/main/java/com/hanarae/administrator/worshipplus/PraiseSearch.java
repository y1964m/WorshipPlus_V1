package com.hanarae.administrator.worshipplus;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class PraiseSearch extends AppCompatActivity {

    static AutoCompleteTextView editText_search_content;
    static EditText editText_search_chord;
    static EditText editText_search_tag;
    Button button_song_search;
    static Button button_save;
    static Search_Recycler_Adapter adapter;

    RecyclerView recyclerView;

    static SearchDB searchDB;
    CountDownLatch latch_DB, latch_all;
    InputMethodManager imm;
    boolean all_good;
    int width, height;
    static int double_check=0;
    static boolean song_search = false;
    ProgressDialog progressDialog;
    boolean isMobile=true;
    boolean isWiFi=true;
    boolean isWiMax=true;

    @Override
    protected void onResume() {
        super.onResume();
        if(song_search) songSearch();
        song_search=false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_praise_search);

        if(MainActivity.currentMode == AppCompatDelegate.MODE_NIGHT_NO) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        all_good = false;

        editText_search_content = findViewById(R.id.editText_search_content);

        ArrayAdapter auto = new ArrayAdapter(this, R.layout.autocomplete_item, MainActivity.autoText);
        editText_search_content.setAdapter(auto);
        editText_search_content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                songSearch();
            }
        });

        //키보드 엔터 누르면 작동되는 액셩 정의
        editText_search_content.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        songSearch();
                        recyclerView.requestFocus();
                        break;
                    default:
                        // 기본 엔터키 동작
                        return false;
                }
                return true;
            }
        });
        editText_search_chord = findViewById(R.id.editText_search_chord);
        editText_search_chord.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        songSearch();
                        break;
                    default:
                        // 기본 엔터키 동작
                        return false;
                }
                return true;
            }
        });
        editText_search_tag = findViewById(R.id.editText_search_tag);
        editText_search_tag.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        songSearch();
                        break;
                    default:
                        // 기본 엔터키 동작
                        return false;
                }
                return true;
            }
        });
        button_song_search = findViewById(R.id.button_song_search);


        button_save = findViewById(R.id.button_save);
        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                for (int i = 0; i < MainActivity.tempData.getTitleArrayListSize(); i++){
                    if((int)MainActivity.tempData.Check.get(i)==1){
                        ArrayList tempArray = new ArrayList();
                        tempArray.add("");

                        if(MainActivity.tempData.DateArrayList.get(i).get(0).equals("//기본정보")){
                            MainActivity.tempConti.addExplanation(""); // 세부콘티
                            MainActivity.tempConti.addMusic(""); // 링크주소
                            MainActivity.tempConti.addSheet(""); // 악보주소
                        }
                        else {
                            MainActivity.tempConti.addExplanation(MainActivity.tempData.getCheckedContent(i,1)); // 세부콘티
                            MainActivity.tempConti.addMusic(MainActivity.tempData.getCheckedContent(i,2)); // 링크주소
                            MainActivity.tempConti.addSheet(MainActivity.tempData.getCheckedContent(i,3)); // 악보주소
                        }

                        MainActivity.tempConti.addTitleArrayListItem(MainActivity.tempData.getTitleArrayListItem(i));
                        MainActivity.tempConti.setChordArrayList(MainActivity.tempData.getChordArrayListItem(i));
                        MainActivity.tempConti.setDateArrayList(tempArray);

                    }

                }
                Toast.makeText(getApplicationContext(),"선택한 콘티가 리스트에 추가되었습니다" ,Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayout background = findViewById(R.id.background);
        //키보드 화면선택시 자동으로 내리는 친구
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(v.getWindowToken(),0);
            }
        });

        recyclerView = findViewById(R.id.recycler_view_search_result);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        width = dm.widthPixels; //디바이스 화면 너비
        height = dm.heightPixels; //디바이스 화면 높이

        adapter = new Search_Recycler_Adapter(2, imm, width, height);
        recyclerView.setAdapter(adapter);

        button_song_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                songSearch();
            }
        });

        //길게 눌러서 곡 추가
        button_song_search.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(imm!=null)
                    imm.hideSoftInputFromWindow(v.getWindowToken(),0);

                if(editText_search_content.getText().toString().isEmpty() || editText_search_chord.getText().toString().isEmpty() || editText_search_tag.getText().toString().isEmpty()) all_good = false;
                else all_good = true;

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("곡을 추가하시겠어요?");
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {

                    }
                });

                builder.setPositiveButton("추가", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id){
                        if(all_good){
                            String db_data="";

                            db_data += "&id_date=0000"
                                    + "&title=" + editText_search_content.getText()
                                    + "&chord=" + editText_search_chord.getText()
                                    + "&tag=" + editText_search_tag.getText();

                            latch_DB = new CountDownLatch(1);
                            InputDB inputDB = new InputDB(getApplicationContext(), latch_DB, db_data,1);
                            inputDB.execute();

                            try {
                                latch_DB.await();

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            inputDB.cancel(true);

                            latch_DB = new CountDownLatch(1);

                            //초기화작업
                            MainActivity.args.putString("someContent", editText_search_content.getText().toString());
                            MainActivity.args.putString("someChord", editText_search_chord.getText().toString());
                            MainActivity.args.putString("someTag","");//editText_search_tag.getText().toString());

                            MainActivity.tempData.removeTitleArrayList();
                            MainActivity.tempData.removeChordArrayList();
                            MainActivity.tempData.removeDateArrayList();
                            MainActivity.tempData.removeExplanationArrayList();
                            MainActivity.tempData.removeMusicArrayList();
                            MainActivity.tempData.removeSheetArrayList();
                            MainActivity.tempData.removeCheck();

                            /*AsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR) 쓰레드 병령실행 때 */

                            searchDB = new SearchDB(2, getApplicationContext(), latch_DB);
                            searchDB.execute();

                            adapter.listData.clear();//초기화 하고, 안그러면 밑으로 똑같은 뷰가 계속 붙음

                            try {
                                latch_DB.await();

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            getData();
                            searchDB.cancel(true);

                            if(double_check==1) Toast.makeText(getApplicationContext(),"이미 등록된 곡입니다.",Toast.LENGTH_SHORT).show();
                            else {
                                Toast.makeText(getApplicationContext(),"추가되었습니다.",Toast.LENGTH_SHORT).show();
                                songSearch();
                            }
                        }
                        else Toast.makeText(getApplicationContext(),"빈칸을 채워주세요",Toast.LENGTH_SHORT).show();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                return false;
            }
        });

    }

    public void songSearch(){

        //인터넷 연결확인 작업
        if(MainActivity.manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)!=null) {
            isMobile = MainActivity.manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        }
        if(MainActivity.manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!=null) {
            isWiFi = MainActivity.manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        }
        if(MainActivity.manager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX)!=null) {
            isWiMax = MainActivity.manager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX).isConnectedOrConnecting();
        }

        if (!isMobile && !isWiFi) {
            Toast.makeText(getApplicationContext(), "인터넷 연결을 확인해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        imm.hideSoftInputFromWindow(editText_search_content.getWindowToken(), 0);
        button_save.setVisibility(View.GONE);

        //검색어 입력했는지 확인절차
        if(editText_search_content.getText().toString().isEmpty()
                && editText_search_chord.getText().toString().isEmpty()
                && editText_search_tag.getText().toString().isEmpty()) all_good = false;
        else all_good = true;

        if (all_good) {

            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("정보를 불러오는 중...");
            progressDialog.setCancelable(true);

            String temp_string = editText_search_content.getText().toString();
            MainActivity.args.putString("someContent", temp_string.replace(" ",""));
            MainActivity.args.putString("someChord", editText_search_chord.getText().toString());
            MainActivity.args.putString("someTag", editText_search_tag.getText().toString());

            latch_DB = new CountDownLatch(1);

            //초기화작업
            MainActivity.tempData.removeTitleArrayList();
            MainActivity.tempData.removeChordArrayList();
            MainActivity.tempData.removeDateArrayList();
            MainActivity.tempData.removeExplanationArrayList();
            MainActivity.tempData.removeMusicArrayList();
            MainActivity.tempData.removeSheetArrayList();
            MainActivity.tempData.removeCheck();

            /*AsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR) 쓰레드 병령실행 때 */

            searchDB = new SearchDB(2, this, latch_DB, progressDialog);
            new AsyncTaskCancelTimerTask(searchDB, 5000, 1000, true).start();
            searchDB.execute();

            adapter.listData.clear();//초기화 하고, 안그러면 밑으로 똑같은 뷰가 계속 붙음

       /*     try {
                latch_DB.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            getData();

            if (MainActivity.tempData.getTitleArrayListSize() == 0){
                Toast.makeText(getApplicationContext(), "일치하는 정보가 없습니다", Toast.LENGTH_SHORT).show();
                button_save.setVisibility(View.GONE);
            }*/

        }
        else {

            button_save.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(),"검색단어를 입력해주세요",Toast.LENGTH_SHORT).show();
            adapter.listData.clear();

            MainActivity.tempData.removeTitleArrayList();
            MainActivity.tempData.removeChordArrayList();
            MainActivity.tempData.removeDateArrayList();
            MainActivity.tempData.removeExplanationArrayList();
            MainActivity.tempData.removeMusicArrayList();
            MainActivity.tempData.removeSheetArrayList();
            MainActivity.tempData.removeCheck();

            adapter.notifyDataSetChanged();

        }

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
                if(asyncTask.getStatus() == AsyncTask.Status.FINISHED){
                    asyncTask.cancel(interrupt);
                    return;
                }
                else if(asyncTask.getStatus() == AsyncTask.Status.PENDING ||
                        asyncTask.getStatus() == AsyncTask.Status.RUNNING ) {
                    asyncTask.cancel(interrupt);
                    if(progressDialog.isShowing()) progressDialog.dismiss();
                    Toast.makeText(PraiseSearch.this,"네트워크 에러",Toast.LENGTH_SHORT).show();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.listData.clear();
        finishAndRemoveTask();
    }

    public static void getData(){

        for (int i = 0; i < MainActivity.tempData.getTitleArrayListSize(); i++) {
            // 각 List의 값들을 data 객체에 set 해줍니다.
            Data data = new Data();
            MainActivity.tempData.Check.add(0);
            MainActivity.tempData.Second_Check.add(0);
            data.setTitle(MainActivity.tempData.getTitleArrayListItem(i));
            data.setContent(MainActivity.tempData.getChordArrayListItem(i));
            //data.setDate(MainActivity.tempData.getDateArrayListItem(i));
            data.setDate(MainActivity.tempData.getDateArrayListItem(i));
            data.setExplanation(MainActivity.tempData.getExplanationArrayListItem(i));
            data.setMusic(MainActivity.tempData.getMusicArrayListItem(i));
            data.setSheet(MainActivity.tempData.getSheetArrayListItem(i));

            // 각 값이 들어간 data를 adapter에 추가합니다.
            if(data.getTitle()!=null) adapter.addItem(data);
        }

        PraiseSearch.adapter.notifyDataSetChanged();
        button_save.setVisibility(View.VISIBLE);

    }

    public static class SearchDB extends AsyncTask<Void, Integer, Void> {

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


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //super.onPostExecute(aVoid);

            getData();

            if (MainActivity.tempData.getTitleArrayListSize() == 0){
                Toast.makeText(context, "일치하는 정보가 없습니다", Toast.LENGTH_SHORT).show();
                button_save.setVisibility(View.GONE);
            }

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
