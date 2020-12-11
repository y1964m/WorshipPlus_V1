package com.hanarae.administrator.worshipplus;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;


public class ThirdFragment extends Fragment {
    // Store instance variables
    static Input_Recycler_Adapter adapter;
    CountDownLatch latch;
    InputMethodManager imm;
    LinearLayout background;
    static LinearLayout conti_info;
    TextView conti_bible, conti_leader;
    Switch switch1, switch2;
    static int state_code = 0;
    int width, height;
    String publicOn, pushOn;
    CustomDialog cd;
    static boolean isUploaded;


    // newInstance constructor for creating fragment with arguments
    public static ThirdFragment newInstance(int page, String title) {
        ThirdFragment fragment = new ThirdFragment();
        Bundle args = MainActivity.args;
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragment.setArguments(args);
        return fragment;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();

        ////adapter.listData.clear();

        conti_bible.setText("@"+MainActivity.args.getString("someDate")+"\n" + MainActivity.args.getString("someBible"));
        conti_leader.setText("\""+ MainActivity.args.getString("someTitle1") + "\"" + " \n찬양인도: " +MainActivity.args.getString("someTitle2"));

        for (int i = 0; i < MainActivity.tempConti.getTitleArrayListSize(); i++) {
            // 각 List의 값들을 data 객체에 set 해줍니다.
            Data data = new Data();
            data.setTitle(MainActivity.tempConti.getTitleArrayListItem(i));
            data.setContent(MainActivity.tempConti.getChordArrayListItem(i));

            data.setDate(MainActivity.tempConti.getDateArrayListItem(i));
            data.setSingle_explanation(MainActivity.tempConti.getExplanation(i));
            //data.setExplanation(MainActivity.tempConti.getExplanationArrayListItem(i));
            //data.setMusic(MainActivity.tempConti.getMusicArrayListItem(i));
            data.setSingle_music(MainActivity.tempConti.getMusic(i));
            //data.setSingle_Sheet_url(MainActivity.tempConti.getSingle_Sheet_url());
            data.setSingle_sheet(MainActivity.tempConti.getSheet(i));

            // 각 값이 들어간 data를 adapter에 추가합니다.
            if(data.getTitle()!=null) adapter.addItem(data);
        }

        MainActivity.tempConti.removeTitleArrayList();
        MainActivity.tempConti.removeChordArrayList();
        MainActivity.tempConti.removeSheetArrayList();
        MainActivity.tempConti.removeMusicArrayList();
        MainActivity.tempConti.removeExplanationArrayList();
        MainActivity.tempConti.removeDateArrayList();

        adapter.notifyDataSetChanged();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.listData.clear();
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        state_code=0;
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        width = dm.widthPixels; //디바이스 화면 너비
        height = dm.heightPixels; //디바이스 화면 높이

        View view = inflater.inflate(R.layout.fragment_input_third, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);

        background = view.findViewById(R.id.background_third_fragment);
        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imm!=null)
                    imm.hideSoftInputFromWindow(v.getWindowToken(),0);
            }
        });

        conti_info = view.findViewById(R.id.linear_layout_conti_info);
        conti_bible = view.findViewById(R.id.textview_conti_info_bible);
        conti_leader = view.findViewById(R.id.textview_conti_info_leader);
        switch1 = view.findViewById(R.id.switch1);
        publicOn = "&noPub=1";
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) publicOn = "&pubOn=on";
                else publicOn = "&noPub=1";
            }
        });
        switch2 = view.findViewById(R.id.switch2);
        pushOn = "&noPush=1";
        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) pushOn = "&push=on";
                else pushOn = "&noPush=1";
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);


        adapter = new Input_Recycler_Adapter(1,imm,width,height,getContext());
        recyclerView.setAdapter(adapter);

        recyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imm!=null)
                    imm.hideSoftInputFromWindow(v.getWindowToken(),0);
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(adapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);


        final Button button_add = view.findViewById(R.id.button_add);
        final Button button_done = view.findViewById(R.id.button_done);

        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state_code==0) addData();
                if (state_code==1) {
                    button_add.setText("+");
                    button_done.setText(">");
                    conti_info.setVisibility(View.GONE);
                    adapter.num_on = false;
                    state_code = 0;
                    adapter.notifyDataSetChanged();
                }
            }
        });
        button_add.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                cd = new CustomDialog(8, getContext(), 0,imm, "");
                WindowManager.LayoutParams wm = cd.getWindow().getAttributes();  //다이얼로그의 높이 너비 설정하기위해
                wm.copyFrom(cd.getWindow().getAttributes());  //여기서 설정한값을 그대로 다이얼로그에 넣겠다는의미
                wm.width = width ;  //화면 너비의 절반
                wm.height = height / 2;  //화면 높이의 절반
                cd.show();
                return false;
            }
        });

        if(state_code == 1){

            conti_bible.setText("@"+MainActivity.args.getString("someDate")+"\n" + MainActivity.args.getString("someBible"));
            conti_leader.setText("\""+ MainActivity.args.getString("someTitle1") + "\""+ " \n찬양인도: " +MainActivity.args.getString("someTitle2"));
            adapter.notifyDataSetChanged();
            conti_info.setVisibility(View.VISIBLE);

            adapter.num_on = true;
            adapter.notifyDataSetChanged();

            button_add.setText("<");
            button_done.setText(">");

        }

        button_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imm!=null)
                    imm.hideSoftInputFromWindow(v.getWindowToken(),0);

                switch (state_code){
                    case 1: // 확인 후 최종 저장 단계
                        String db_data = "";
                        String temp_ex = "없음";
                        String temp_link_ssyp="";
                        for(int i =0; i < adapter.getItemCount(); i++){

                            if(adapter.listData.get(i).getSingle_explanation()!=null)
                                temp_ex = adapter.listData.get(i).getSingle_explanation()
                                        .replace("\"","\\\'").replace("\'","\\\'").replace("\\\"","\\\'");
                            else temp_ex = "없음";

                            //성실교회 실황링크 자동삽입
                            /*if(MainActivity.logged_in_db_id.equals("ssyp")) temp_link_ssyp =
                                    "\n\n실황링크\nhttp://ssyp.synology.me:8812/worshipplus/record/" +
                                            (MainActivity.args.getString("someDate").substring(0,10)).replace(".","")
                                            + ".mp3";*/

                            db_data += "&id_date[]=" + MainActivity.args.getString("someDate") + "/"+i
                                    + "&title[]=" + adapter.listData.get(i).getTitle().trim()
                                    + "&chord[]=" + adapter.listData.get(i).getContent().replace(" ","")
                                    + "&ex[]=" + temp_ex
                                + "&music[]=" + adapter.listData.get(i).getSingle_music() //+ temp_link_ssyp
                                + "&sheet[]=" + adapter.listData.get(i).getSingle_sheet()
                                    + "&author=" + MainActivity.logged_in_id
                                + publicOn + pushOn;
                        }

                        latch = new CountDownLatch(1);
                        InputDB inputDB = new InputDB(getContext(),latch,db_data,0);
                        new AsyncTaskCancelTimerTask(inputDB, 10000, 1000, true).start();
                        inputDB.execute();

                        try {
                            latch.await();

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        //초기화 시작
                        if(isUploaded){
                            adapter.listData.clear();
                            adapter.notifyDataSetChanged();

                            MainActivity.args.clear();
                            MainActivity.args.remove("someDate");

                            SecondFragment.editText_bible.setText("");
                            SecondFragment.editText_title1.setText("");
                            SecondFragment.editText_title2.setText("");

                            state_code = 0;
                            conti_info.setVisibility(View.GONE);
                            button_add.setText("+");
                            button_done.setText(">");

                            adapter.num_on = false; //번호 가리기

                            Toast.makeText(getContext(),"저장되었습니다",Toast.LENGTH_SHORT).show();
                            MainActivity.vpPager.setCurrentItem(0);
                        }else Toast.makeText(getContext(),"콘티내용 중 특수문자 에러입니다",Toast.LENGTH_SHORT).show();

                        break;

                    case 0: // 확인 단계
                        if(!MainActivity.logged_in_id.equals(MainActivity.admin_id)){//no admin no control
                            if(!(MainActivity.temp_author.equals(""))&&!(MainActivity.temp_author.equals(MainActivity.logged_in_id))) { // only first writer can edit
                                Toast toast = Toast.makeText(getContext(),MainActivity.temp_author + " 작성한 콘티입니다\n수정권한이 없습니다" ,Toast.LENGTH_SHORT);
                                toast.show();
                                return;
                        }

                        }

                        if(adapter.getItemCount()==0){
                            Toast.makeText(getContext(),"콘티정보를 작성해주세요",Toast.LENGTH_SHORT).show();
                            return;
                        }


                        for(int i =0; i < adapter.getItemCount(); i++){
                            if(adapter.listData.get(i).getTitle().isEmpty() || adapter.listData.get(i).getContent().isEmpty()){
                                Toast.makeText(getContext(),"빈칸없이 콘티정보를 작성해주세요",Toast.LENGTH_SHORT).show();
                                MainActivity.vpPager.setCurrentItem(2);
                                return;
                            }
                        }

                        if(SecondFragment.editText_bible.getText().toString().equals("") || MainActivity.args.getString("someBible")==null || MainActivity.args.getString("someTitle1")==null || MainActivity.args.getString("someTitle2")==null){
                            Toast.makeText(getContext(),"기본정보를 작성해주세요",Toast.LENGTH_SHORT).show();
                            MainActivity.vpPager.setCurrentItem(1);
                        }

                        if(MainActivity.args.getString("someDate").equals("") ){
                            Toast.makeText(getContext(),"예배일자를 작성해주세요",Toast.LENGTH_SHORT).show();
                            MainActivity.vpPager.setCurrentItem(0);
                        }

                        else {
                            conti_bible.setText(MainActivity.args.getString("someDate")+"\n" + MainActivity.args.getString("someBible"));
                            conti_leader.setText("\""+ MainActivity.args.getString("someTitle1") + "\""+ " \n찬양인도: " +MainActivity.args.getString("someTitle2"));
                            adapter.notifyDataSetChanged();
                            conti_info.setVisibility(View.VISIBLE);

                            adapter.num_on = true;
                            adapter.notifyDataSetChanged();

                            button_add.setText("<");
                            button_done.setText(">");

                            state_code = 1;
                        }

                }


            }
        });

        return view;

    }


    private void addData(){

        ArrayList tempArray = new ArrayList();
        tempArray.add("");
        Data data = new Data();
        data.setTitle("");
        data.setContent("");//코드값
        data.setDate(tempArray);
        data.setExplanation(tempArray);
        data.setMusic(tempArray);
        data.setSingle_sheet("");

        adapter.addItem(data);
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
                    Toast.makeText(getContext(),"네트워크 에러",Toast.LENGTH_SHORT).show();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }


}

