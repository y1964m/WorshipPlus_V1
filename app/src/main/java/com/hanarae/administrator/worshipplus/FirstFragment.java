package com.hanarae.administrator.worshipplus;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;


public class FirstFragment extends Fragment {
    // Store instance variables
    InputMethodManager imm;
    TextInputLayout textInputLayout;
    CalendarView calendarView;
    CountDownLatch latch;
    SearchDB searchDB_first;
    static EditText tvLabe2;


    // newInstance constructor for creating fragment with arguments
    public static FirstFragment newInstance(int page, String title) {
        FirstFragment fragment = new FirstFragment();
        Bundle args = MainActivity.args;
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragment.setArguments(args);
        return fragment;
    }

    public abstract class OnSingleClickListener implements View.OnClickListener {
        // 중복 클릭 방지 시간 설정
        private static final long MIN_CLICK_INTERVAL=1000;
        private long mLastClickTime;
        public abstract void onSingleClick(View v);

        @Override
        public final void onClick(View v) {

            long currentClickTime=SystemClock.uptimeMillis();
            long elapsedTime=currentClickTime-mLastClickTime;
            mLastClickTime=currentClickTime;

            // 중복 클릭인 경우
            if(elapsedTime<=MIN_CLICK_INTERVAL){
                return;
            }
            // 중복 클릭아 아니라면 추상함수 호출
            onSingleClick(v);
        }
    }

    public static void loadConti(Context context, CountDownLatch latch, String loadDate) {

        SearchDB searchDB_first;

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

        if (!isMobile && !isWiFi) {
            Toast.makeText(context, "인터넷 연결을 확인해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        MainActivity.args.putString("someDate", loadDate);

        if(MainActivity.args.get("someDate").toString().isEmpty()){
            Toast.makeText(context, "날자를 선택해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        latch = new CountDownLatch(1);

        //초기화작업
        if(ThirdFragment.adapter != null) ThirdFragment.adapter.listData.clear();

        MainActivity.tempConti.removeTitleArrayList();
        MainActivity.tempConti.removeChordArrayList();
        MainActivity.tempConti.removeDateArrayList();
        MainActivity.tempConti.removeExplanationArrayList();
        MainActivity.tempConti.removeMusicArrayList();
        MainActivity.tempConti.removeSheetArrayList();
        MainActivity.tempConti.removeCheck();

        /*AsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR) 쓰레드 병령실행 때 */

        searchDB_first = new SearchDB(3, context, latch);
        searchDB_first.execute();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(MainActivity.tempConti.getTitleArrayListSize() > 0 && MainActivity.tempConti.getBible() != null){

            Toast.makeText(context, "콘티를 불러왔습니다", Toast.LENGTH_SHORT).show();
            SecondFragment.editText_bible.setText(MainActivity.tempConti.getBible().toString());
            SecondFragment.editText_title1.setText(MainActivity.tempConti.getSermon().toString());
            SecondFragment.editText_title2.setText(MainActivity.tempConti.getLeader().toString());

        }else{
            MainActivity.tempConti.removeTitleArrayList();
            MainActivity.tempConti.removeChordArrayList();
            MainActivity.tempConti.removeDateArrayList();
            MainActivity.tempConti.removeExplanationArrayList();
            MainActivity.tempConti.removeMusicArrayList();
            MainActivity.tempConti.removeSheetArrayList();
            MainActivity.tempConti.removeCheck();
            Toast.makeText(context,"응답없음",Toast.LENGTH_SHORT).show();
        }

        MainActivity.vpPager.setCurrentItem(2);
        searchDB_first.cancel(true);
        return;
    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        tvLabe2.setText(""+getArguments().getString("someDate"));
        if(tvLabe2.getText().toString().equals("null")) tvLabe2.setText("");
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_input_first, container, false);
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        final LinearLayout first = view.findViewById(R.id.linear_layout_first);
        first.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imm!=null)
                    imm.hideSoftInputFromWindow(v.getWindowToken(),0);
            }
        });

        tvLabe2 = view.findViewById(R.id.textView2);
        textInputLayout = view.findViewById(R.id.text_input_layout);
        textInputLayout.setError(null);
        textInputLayout.setEndIconDrawable(R.drawable.round_search_black_18dp);
        textInputLayout.setEndIconOnClickListener(new OnSingleClickListener() {

            @Override
            public void onSingleClick(View v) {//중복 클릭 방지 시간 설정 ( 해당 시간 이후에 다시 클릭 가능

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

                if (!isMobile && !isWiFi) {
                    Toast.makeText(getContext(), "인터넷 연결을 확인해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                    getArguments().putString("someDate", tvLabe2.getText().toString());

                    if(getArguments().get("someDate").toString().isEmpty()){
                        Toast.makeText(getContext(), "날자를 선택해주세요", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    latch = new CountDownLatch(1);

                    //초기화작업
                    if(ThirdFragment.adapter != null) ThirdFragment.adapter.listData.clear();

                    MainActivity.tempConti.removeTitleArrayList();
                    MainActivity.tempConti.removeChordArrayList();
                    MainActivity.tempConti.removeDateArrayList();
                    MainActivity.tempConti.removeExplanationArrayList();
                    MainActivity.tempConti.removeMusicArrayList();
                    MainActivity.tempConti.removeSheetArrayList();
                    MainActivity.tempConti.removeCheck();

                    /*AsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR) 쓰레드 병령실행 때 */

                    searchDB_first = new SearchDB(3, getContext(), latch);
                    searchDB_first.execute();

                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (MainActivity.tempConti.getTitleArrayListSize() == 0){
                        //초기화
                        MainActivity.tempConti.removeTitleArrayList();
                        MainActivity.tempConti.removeChordArrayList();
                        MainActivity.tempConti.removeDateArrayList();
                        MainActivity.tempConti.removeExplanationArrayList();
                        MainActivity.tempConti.removeMusicArrayList();
                        MainActivity.tempConti.removeSheetArrayList();
                        MainActivity.tempConti.removeCheck();

                        Toast.makeText(getContext(), "작성된 콘티가 없습니다", Toast.LENGTH_SHORT).show();

                    }else if(MainActivity.tempConti.getTitleArrayListSize() > 0 && MainActivity.tempConti.getBible() != null){

                        Toast.makeText(getContext(), "콘티를 불러왔습니다", Toast.LENGTH_SHORT).show();
                        SecondFragment.editText_bible.setText(MainActivity.tempConti.getBible().toString());
                        SecondFragment.editText_title1.setText(MainActivity.tempConti.getSermon().toString());
                        SecondFragment.editText_title2.setText(MainActivity.tempConti.getLeader().toString());
                        MainActivity.vpPager.setCurrentItem(2);

                    }else{
                        MainActivity.tempConti.removeTitleArrayList();
                        MainActivity.tempConti.removeChordArrayList();
                        MainActivity.tempConti.removeDateArrayList();
                        MainActivity.tempConti.removeExplanationArrayList();
                        MainActivity.tempConti.removeMusicArrayList();
                        MainActivity.tempConti.removeSheetArrayList();
                        MainActivity.tempConti.removeCheck();
                        Toast.makeText(getContext(),"응답없음",Toast.LENGTH_SHORT).show();
                    }

                    searchDB_first.cancel(true);
            }
        });

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if((s.length()>4 && !s.toString().contains(".")) || (s.length()>10 && !s.toString().contains("/")) || (s.length()>7 && (s.charAt(7))!='.')) {
                    textInputLayout.setError("ex)2000.01.01/팀명");
                }
                else  {
                    textInputLayout.setError(null);
                    getArguments().putString("someDate", tvLabe2.getText().toString());
                }
            }
        };
        tvLabe2.addTextChangedListener(watcher);
        calendarView = view.findViewById(R.id.calender);
        calendarView.setFocusableInTouchMode(true);

        if(getArguments().getString("someDate")!=null){
            calendarView.setDate(getArguments().getLong("someDateAsLong"));
        }

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                //캘린더에 선택한 날짜로 변경하기
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss,SSS");
                Date date0 = null;
                String space1, space2;
                try {
                    date0 = simpleDateFormat.parse((year + "." + (month + 1) + "." + dayOfMonth)+" 00:00:0,000");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                final long temp_date = date0.getTime();

                if(month + 1 < 10) space1 = ".0";
                else space1 =".";

                if(dayOfMonth < 10) space2 = ".0";
                else space2 =".";

                final String date = (year + space1 + (month + 1) + space2 + dayOfMonth) + "/" + MainActivity.team_info;


                if(!MainActivity.tempConti.isEmpty()){//로드 한번이라도 했다면
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("작성하던 콘티정보가 삭제됩니다\n새로운 콘티를 작성하시겠습니까?"+getArguments().getLong("someDateAsLong"));
                    builder.setNeutralButton("취소", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int id){
                            if((getArguments().getLong("someDateAsLong"))!=0) calendarView.setDate(getArguments().getLong("someDateAsLong"));

                        }
                    });

                    builder.setNegativeButton("날짜만 변경", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int id){
                            tvLabe2.setText(date);
                            getArguments().putString("someDate", tvLabe2.getText().toString());
                            getArguments().putLong("someDateAsLong", temp_date);
                            Toast.makeText(getContext(), "날자가 변경되었습니다", Toast.LENGTH_SHORT).show();
                            MainActivity.temp_author="";
                        }
                    });

                    builder.setPositiveButton("새로운 콘티", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            ThirdFragment.adapter.listData.clear();

                            MainActivity.tempConti.removeTitleArrayList();
                            MainActivity.tempConti.removeChordArrayList();
                            MainActivity.tempConti.removeDateArrayList();
                            MainActivity.tempConti.removeExplanationArrayList();
                            MainActivity.tempConti.removeMusicArrayList();
                            MainActivity.tempConti.removeSheetArrayList();
                            MainActivity.tempConti.removeCheck();


                            SecondFragment.editText_bible.setText("");
                            SecondFragment.editText_title1.setText("");
                            SecondFragment.editText_title2.setText("");

                            tvLabe2.setText(date);
                            getArguments().putString("someDate", tvLabe2.getText().toString());
                            getArguments().putLong("someDateAsLong", temp_date);

                        /*    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss,SSS");
                            Date date0 = null;
                            try {
                                date0 = simpleDateFormat.parse(getArguments().getString("someDate").substring(0,9)+" 00:00:0,000");
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            long temp_date = date0.getTime();
                            getArguments().putLong("someDateAsLong", temp_date);*/

                            MainActivity.temp_author="";
                            Toast.makeText(getContext(), "새로운 콘티 "+getArguments().getString("someDate").substring(0,10), Toast.LENGTH_SHORT).show();
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return;
                }

                //초기화작업
                if(ThirdFragment.adapter != null) {
                    if(ThirdFragment.adapter.getItemCount()!=0){
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("작성하던 콘티정보가 삭제됩니다\n새로운 콘티를 작성하시겠습니까?");
                        builder.setNeutralButton("취소", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int id){
                                if((getArguments().getLong("someDateAsLong"))!=0)calendarView.setDate(getArguments().getLong("someDateAsLong"));
                            }
                        });

                        builder.setNegativeButton("날짜만 변경", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int id){
                                tvLabe2.setText(date);
                                getArguments().putString("someDate", tvLabe2.getText().toString());
                                getArguments().putLong("someDateAsLong", temp_date);
                                Toast.makeText(getContext(), "날자가 변경되었습니다", Toast.LENGTH_SHORT).show();
                                MainActivity.temp_author="";
                            }
                        });

                        builder.setPositiveButton("새로운 콘티", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {
                                ThirdFragment.adapter.listData.clear();

                                MainActivity.tempConti.removeTitleArrayList();
                                MainActivity.tempConti.removeChordArrayList();
                                MainActivity.tempConti.removeDateArrayList();
                                MainActivity.tempConti.removeExplanationArrayList();
                                MainActivity.tempConti.removeMusicArrayList();
                                MainActivity.tempConti.removeSheetArrayList();
                                MainActivity.tempConti.removeCheck();


                                SecondFragment.editText_bible.setText("");
                                SecondFragment.editText_title1.setText("");
                                SecondFragment.editText_title2.setText("");

                                tvLabe2.setText(date);
                                getArguments().putString("someDate", tvLabe2.getText().toString());
                                getArguments().putLong("someDateAsLong", temp_date);

                                Toast.makeText(getContext(), "새로운 콘티를 작성합니다", Toast.LENGTH_SHORT).show();
                                MainActivity.temp_author="";
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                        return;
                    }
                    else {
                        tvLabe2.setText(date);
                        getArguments().putString("someDate", tvLabe2.getText().toString());
                        getArguments().putLong("someDateAsLong", temp_date);
                    }
                }
                else {
                    tvLabe2.setText(date);
                    getArguments().putString("someDate", tvLabe2.getText().toString());

                }

                getArguments().putLong("someDateAsLong", temp_date);

                if(imm!=null)
                    imm.hideSoftInputFromWindow(first.getWindowToken(),0);
            }
        });

        return view;
    }



}

