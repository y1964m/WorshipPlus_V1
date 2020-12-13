package com.hanarae.administrator.worshipplus;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
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
import java.util.List;
import java.util.concurrent.CountDownLatch;

import me.relex.circleindicator.CircleIndicator;

public class MainActivity extends AppCompatActivity {

   FragmentPagerAdapter adapterViewPager;
   LinearLayout linearLayout_latest_conti, linearLayout_viewpager;
   SwipeRefreshLayout swipeRefreshLayout;
   static Latest_Conti_Adapter adapter_main;
   SearchDB searchDB_main;
   CountDownLatch latch;
   InputMethodManager imm;
   int width, height;
   TextView date,bible, sermon, leader, button_copy_all;
   static ViewPager vpPager;
   Button button_id, button_help, button_list;
   static  ArrayList autoText;
   RecyclerView recyclerView;
   BottomNavigationView bottomNavigationView;
   ProgressDialog progressDialog;

   static Bundle args = new Bundle();
   static Data tempData;
   static Data tempConti, tempLatestConti, tempPhoto;
   private long backKeyPressedTime = 0;
   Toast toast;
   SharedPreferences sharedPreferences;
   static SharedPreferences.Editor editor;

   static String logged_in_db_id;
   static String logged_in_id;
   static String team_info;
   static String checked_search;
   static String temp_author;

   static String admin_id= "david"; // 기본정보 수정 가능 권한가진 id.

    boolean isFirstClicked = false;
    boolean isFromList = false;
    static boolean isFirstClickedCalendar = true;
    static int currentMode;

    static ConnectivityManager manager;

    @Override
    public void onBackPressed() {

        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지났으면 Toast Show
        // 2000 milliseconds = 2 seconds
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지나지 않았으면 종료
        // 현재 표시된 Toast 취소
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finishAffinity();
            toast.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finishAffinity();
        System.runFinalizersOnExit(true);
        System.exit(0);
    }


    final void dialogShow() {
        final List<String> ListItems = new ArrayList<>();
        ListItems.add("기본정보");
        ListItems.add("찬양제목");
        ListItems.add("세부내용");
        ListItems.add("악보/링크");
        final CharSequence[] items =  ListItems.toArray(new String[ListItems.size()]);
        final List SelectedItems  = new ArrayList();


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("복사할 항목을 선택해주세요");
        builder.setMultiChoiceItems(items, null,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which,
                                        boolean isChecked) {
                        if (isChecked) {
                            //사용자가 체크한 경우 리스트에 추가
                            SelectedItems.add(ListItems.get(which));
                        } else if (SelectedItems.contains(ListItems.get(which))) {
                            //이미 리스트에 들어있던 아이템이면 제거
                            SelectedItems.remove(Integer.valueOf(which));
                        }
                    }
                });
        builder.setPositiveButton("복사",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String temp_string = "";
                            if(SelectedItems.contains("기본정보"))
                                temp_string+="@ "+ tempLatestConti.getBibleDate()+ "\n말씀본문: "+ tempLatestConti.getBible()+ "\n말씀제목: "+ tempLatestConti.getSermon()+ "\n찬양인도: "+ tempLatestConti.getLeader();

                            if(SelectedItems.contains("세부내용") && SelectedItems.contains("악보/링크")){
                                for (int i = 0; i < MainActivity.tempLatestConti.getTitleArrayListSize(); i++) {
                                    // 각 List의 값들을 data 객체에 set 해줍니다.
                                    temp_string += "\n\n" + i + ". " + adapter_main.listData.get(i).getTitle() + " - " + adapter_main.listData.get(i).getContent() + "\n"
                                            + adapter_main.listData.get(i).getExplanation(0) + "\n링크: " + adapter_main.listData.get(i).getMusic(0) + "\n악보: " + adapter_main.listData.get(i).getSheet(0);
                                }
                            }else if(SelectedItems.contains("세부내용")){
                                for (int i = 0; i < MainActivity.tempLatestConti.getTitleArrayListSize(); i++) {
                                    // 각 List의 값들을 data 객체에 set 해줍니다.
                                    temp_string += "\n\n" + i + ". " + adapter_main.listData.get(i).getTitle() + " - " + adapter_main.listData.get(i).getContent() + "\n"
                                            + adapter_main.listData.get(i).getExplanation(0) ;
                                }
                            } else if(SelectedItems.contains("찬양제목")){
                                for (int i = 0; i < MainActivity.tempLatestConti.getTitleArrayListSize(); i++) {
                                    // 각 List의 값들을 data 객체에 set 해줍니다.
                                    temp_string += "\n" + i + ". " + adapter_main.listData.get(i).getTitle() + " - " + adapter_main.listData.get(i).getContent();
                                }
                           }

                        ClipboardManager clipboard = (ClipboardManager) getApplicationContext().getSystemService(CLIPBOARD_SERVICE);
                        clipboard.setText(temp_string);
                        Toast.makeText(getApplicationContext(),"클립보드에 복사완료",Toast.LENGTH_SHORT).show();
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //인터넷 연결확인 작업
        manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

    /*    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog().build());*/

        //기본 SharedPreferences 환경과 관련된 객체를 얻어옵니다.
        sharedPreferences = getSharedPreferences("login",0);
        // SharedPreferences 수정을 위한 Editor 객체를 얻어옵니다.
        editor = sharedPreferences.edit();

        //theme check
        currentMode =  sharedPreferences.getInt("mode",1);
        if(currentMode == AppCompatDelegate.MODE_NIGHT_NO) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        if(sharedPreferences.getString("id","")==null || sharedPreferences.getString("id","").equals("")){
            //Toast.makeText(getApplicationContext(),sharedPreferences.getString("id",""),Toast.LENGTH_SHORT).show();
            Intent intent_login = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent_login,1000);
        }
        else {
            logged_in_db_id = sharedPreferences.getString("db_id","");
            logged_in_id = sharedPreferences.getString("id","");
            team_info = sharedPreferences.getString("write","");
            checked_search = sharedPreferences.getString("search","");
        }


        setContentView(R.layout.activity_main);

        tempData = new Data(); // db로부터 받을 데이터 그릇
        tempConti = new Data(); // db로 넣을 데이터 그릇
        tempLatestConti = new Data(); //latest conti container
        tempPhoto = new Data(); //악보 그릇
        autoText = new ArrayList(); // 검색어 자동완성 그릇

        temp_author="";

        bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                boolean isMobile=true;
                boolean isWiFi=true;
                boolean isWiMax=true;

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
                    swipeRefreshLayout.setRefreshing(false);
                    return;
                }

                //swipeRefreshLayout.setRefreshing(true);
                getLatestConti(false);
            }
        });

        vpPager = findViewById(R.id.vpPager);

        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);
        vpPager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imm!=null)
                    imm.hideSoftInputFromWindow(v.getWindowToken(),0);
            }
        });

        vpPager.setCurrentItem(2);

        date = findViewById(R.id.textview_conti_info_date);
        bible = findViewById(R.id.textview_conti_info_bible_main);
        sermon = findViewById(R.id.textview_conti_info_sermon_main);
        leader = findViewById(R.id.textview_conti_info_leader_main);
        button_copy_all = findViewById(R.id.button_copy_all); // 이번주 콘티

        button_list = findViewById(R.id.button_list);
        button_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(FirstFragment.tvLabe2==null) {
                    vpPager.setCurrentItem(0);
                    vpPager.setCurrentItem(2);
                }
                Intent intent_list = new Intent(MainActivity.this, ContiListActivity.class);
                startActivityForResult(intent_list,2000);
            }
        });

        button_id = findViewById(R.id.button_id_change);
        button_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_login = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(intent_login,1000);
            }
        });
        button_help = findViewById(R.id.button_help);
        button_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_help = new Intent(MainActivity.this, Help.class);
                startActivity(intent_help);
            }
        });


        // 이번주 콘티
        button_copy_all.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                dialogShow();
                return false;
            }
        });



        linearLayout_viewpager = findViewById(R.id.linear_layout_viewpager);

        linearLayout_latest_conti=findViewById(R.id.linear_layout_latest_conti);
        recyclerView = findViewById(R.id.recycler_view_main);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        width = dm.widthPixels; //디바이스 화면 너비
        height = dm.heightPixels; //디바이스 화면 높이

        //최근 콘티 부분
        if(autoText.size()>0) autoText.clear();
        getLatestConti(true);

        CircleIndicator indicator = findViewById(R.id.indicator);
        indicator.setViewPager(vpPager);

    }


    public void getLatestConti(boolean isInitial){
        //최근 콘티 부분
        adapter_main.listData.clear();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("정보를 불러오는 중...");
        progressDialog.setCancelable(true);

        MainActivity.tempLatestConti.removeContiInfo();

        MainActivity.tempLatestConti.removeTitleArrayList();
        MainActivity.tempLatestConti.removeChordArrayList();
        MainActivity.tempLatestConti.removeDateArrayList();
        MainActivity.tempLatestConti.removeExplanationArrayList();
        MainActivity.tempLatestConti.removeMusicArrayList();
        MainActivity.tempLatestConti.removeSheetArrayList();
        MainActivity.tempLatestConti.removeCheck();

        latch = new CountDownLatch(1);

        adapter_main = new Latest_Conti_Adapter(1, imm, width, height, getApplicationContext());
        recyclerView.setAdapter(adapter_main);
        if(isInitial) searchDB_main = new SearchDB(0, this, latch, progressDialog);
        //else searchDB_main = new SearchDB(1, this, latch);
        else searchDB_main = new SearchDB(1, this, latch, progressDialog);
        new AsyncTaskCancelTimerTask(searchDB_main, 8000, 1000, true).start();
        searchDB_main.execute();
        adapter_main.listData.clear();//초기화 하고, 안그러면 밑으로 똑같은 뷰가 계속 붙음
       /* try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //searchDB_main.cancel(true);

        date.setText("@ "+tempLatestConti.getBibleDate());
        bible.setText("말씀본문: "+ tempLatestConti.getBible());
        sermon.setText("말씀제목: "+ tempLatestConti.getSermon());
        leader.setText("찬양인도: "+ tempLatestConti.getLeader());

        for (int i = 0; i < MainActivity.tempLatestConti.getTitleArrayListSize(); i++) {
            // 각 List의 값들을 data 객체에 set 해줍니다.
            Data data = new Data();
            MainActivity.tempData.Check.add(0);
            data.setTitle(MainActivity.tempLatestConti.getTitleArrayListItem(i));
            data.setContent(MainActivity.tempLatestConti.getChordArrayListItem(i));
            data.setDate(MainActivity.tempLatestConti.getDateArrayListItem(i));
            data.setExplanation(MainActivity.tempLatestConti.getExplanationArrayListItem(i));
            data.setMusic(MainActivity.tempLatestConti.getMusicArrayListItem(i));
            data.setSingle_Sheet_url(MainActivity.tempLatestConti.getSheet(i));

            // 각 값이 들어간 data를 adapter에 추가합니다.
            if(data.getTitle()!=null) adapter_main.addItem(data);
        }
        adapter_main.notifyDataSetChanged();*/

        swipeRefreshLayout.setRefreshing(false);
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
                if(asyncTask.getStatus() == AsyncTask.Status.FINISHED) {
                    asyncTask.cancel(interrupt);
                    return;
                }
                else if(asyncTask.getStatus() == AsyncTask.Status.PENDING ||
                        asyncTask.getStatus() == AsyncTask.Status.RUNNING ) {

                    asyncTask.cancel(interrupt);
                    if(progressDialog.isShowing()) progressDialog.dismiss();
                    Toast.makeText(MainActivity.this,"네트워크 에러",Toast.LENGTH_SHORT).show();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //getLatestConti(false);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //getLatestConti();
                    swipeRefreshLayout.setVisibility(View.VISIBLE);
                    linearLayout_viewpager.setVisibility(View.GONE);
                    return true;
                case R.id.navigation_dashboard:
                    if(!isFirstClicked&&!isFromList) {
                        vpPager.setCurrentItem(0);
                        isFirstClicked=true;
                    }
                    //if(FirstFragment.tvLabe2==null)vpPager.setCurrentItem(0);
                    swipeRefreshLayout.setVisibility(View.GONE);
                    linearLayout_viewpager.setVisibility(View.VISIBLE);
                    // Intent intent_add = new Intent(MainActivity.this, PraiseSearch.class);
                    // startActivity(intent_add);
                    return true;
                case R.id.navigation_notifications:
                    Intent intent_search = new Intent(MainActivity.this, PraiseSearch.class);
                    startActivity(intent_search);
                    return false;
            }
            return false;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1000){//회원정보 버튼 눌렀을 시
            if(resultCode== Activity.RESULT_OK){
                logged_in_db_id = sharedPreferences.getString("db_id","");
                logged_in_id = sharedPreferences.getString("id","");
                getLatestConti(true);
            }
            if(resultCode== Activity.RESULT_CANCELED){
                logged_in_db_id = sharedPreferences.getString("db_id","");
                logged_in_id = sharedPreferences.getString("id","");
            }
        }
        if(requestCode==2000){//list 버튼 눌렀을 시
            if(resultCode== Activity.RESULT_OK){
                FirstFragment.loadConti(getApplicationContext(), latch, data.getStringExtra("dateToLoad"));
                isFromList=true;
                bottomNavigationView.setSelectedItemId(R.id.navigation_dashboard);
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
            }return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            //super.onPostExecute(aVoid);

            //searchDB_main.cancel(true);

            date.setText("@ "+tempLatestConti.getBibleDate());
            bible.setText("말씀본문: "+ tempLatestConti.getBible());
            sermon.setText("말씀제목: "+ tempLatestConti.getSermon());
            leader.setText("찬양인도: "+ tempLatestConti.getLeader());

            for (int i = 0; i < MainActivity.tempLatestConti.getTitleArrayListSize(); i++) {
                // 각 List의 값들을 data 객체에 set 해줍니다.
                Data data = new Data();
                MainActivity.tempData.Check.add(0);
                data.setTitle(MainActivity.tempLatestConti.getTitleArrayListItem(i));
                data.setContent(MainActivity.tempLatestConti.getChordArrayListItem(i));
                data.setDate(MainActivity.tempLatestConti.getDateArrayListItem(i));
                data.setExplanation(MainActivity.tempLatestConti.getExplanationArrayListItem(i));
                data.setMusic(MainActivity.tempLatestConti.getMusicArrayListItem(i));
                data.setSingle_Sheet_url(MainActivity.tempLatestConti.getSheet(i));

                // 각 값이 들어간 data를 adapter에 추가합니다.
                if(data.getTitle()!=null) adapter_main.addItem(data);
            }
            adapter_main.notifyDataSetChanged();

            if(case_number == 1) Toast.makeText(getApplicationContext(),"업데이트 완료",Toast.LENGTH_SHORT).show();
            else if(sharedPreferences.getString("id","")!=null || !sharedPreferences.getString("id","").equals("")){
                Toast.makeText(getApplicationContext(),"환영합니다! "+ sharedPreferences.getString("id","Guest") +"님",Toast.LENGTH_SHORT).show();
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




    public class MyPagerAdapter extends FragmentPagerAdapter {
        private int NUM_ITEMS = 3;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }


        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return FirstFragment.newInstance(0, "예배일자");
                case 1:
                    //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    return SecondFragment.newInstance(1, "설교정보");
                case 2:
                    return ThirdFragment.newInstance(2, "콘티작성");
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }


    }

}
