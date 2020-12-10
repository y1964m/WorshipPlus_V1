package com.hanarae.administrator.worshipplus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class LoginActivity extends AppCompatActivity {

    static ArrayList team = new ArrayList();
    SharedPreferences sharedPreferences;
    static SharedPreferences.Editor editor;
    CountDownLatch latch;
    Button login, join;
    Button back;
    TextView textView_email;
    TextInputLayout textInputLayout_pw;


    public static String sha256(String str) {
        String SHA = "";
        try{
            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            sh.update(str.getBytes());
            byte byteData[] = sh.digest();
            StringBuffer sb = new StringBuffer();
            for(int i = 0 ; i < byteData.length ; i++) sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
            SHA = sb.toString();
        }catch(NoSuchAlgorithmException e) { e.printStackTrace(); SHA = null; }
        return SHA;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        team.clear();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);     //다이얼로그에서 사용할 레이아웃입니다.

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

        final EditText id, pw, editText_db_id;
        final LinearLayout background, login1, login_setting, login_setting_team;
        final InputMethodManager imm;

        sharedPreferences = getApplicationContext().getSharedPreferences("login",0);
        editor = sharedPreferences.edit();

        editText_db_id= findViewById(R.id.editText_db_id);
        editText_db_id.setText(MainActivity.logged_in_db_id);

        id = findViewById(R.id.editText_id);
        pw = findViewById(R.id.editText_pw);
        textInputLayout_pw = findViewById(R.id.text_input_layout_pw);
        textView_email = findViewById(R.id.text_email);
        textView_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent email = new Intent(Intent.ACTION_SEND);
                email.setType("plain/text");
                String[] address = {"y196411m@gmail.com"};
                email.putExtra(Intent.EXTRA_EMAIL, address);
                email.putExtra(Intent.EXTRA_SUBJECT, "그룹코드 신규신청");
                email.putExtra(Intent.EXTRA_TEXT, "다음의 양식을 작성해주세요\n\n교회명:\n찬양팀명:\n\n감사합니다");
                startActivity(email);
            }
        });


        login = findViewById(R.id.button_login);
        join = findViewById(R.id.button_join);
        back = findViewById(R.id.join_button_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MainActivity.logged_in_db_id ==null || MainActivity.logged_in_db_id.equals("")){
                    Toast.makeText(getApplicationContext(),"로그아웃 상태입니다", Toast.LENGTH_SHORT).show();
                }else if(sharedPreferences.getString("write","team").equals("team"))
                    Toast.makeText(getApplicationContext(),"팀을 선택해주세요", Toast.LENGTH_SHORT).show();
                else onBackPressed();
            }
        });

        if(MainActivity.logged_in_db_id !=null ) {
            textInputLayout_pw.setVisibility(View.GONE);
            textView_email.setVisibility(View.INVISIBLE);
        }

        login1 = findViewById(R.id.login_1);
        login_setting = findViewById(R.id.login_setting);
        login_setting_team = findViewById(R.id.login_setting_team);

        if(MainActivity.logged_in_id != null){

            id.setText(MainActivity.logged_in_id);

            login.setText("Logout");
            join.setText("Team");
        }

        imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        background = findViewById(R.id.background_login);
        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imm!=null)
                    imm.hideSoftInputFromWindow(v.getWindowToken(),0);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imm!=null)
                    imm.hideSoftInputFromWindow(v.getWindowToken(),0);

                if(login.getText().equals("Login")){

                    FirebaseInstanceId.getInstance().getInstanceId()
                            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                    if (!task.isSuccessful()) {
                                        Log.e("FirebaseMsgService", "getInstanceId failed", task.getException());
                                        return;
                                    }

                                    // Get new Instance ID token
                                    String tokenID = task.getResult().getToken();
                                    Log.e("FirebaseMsgService", "Token: " + tokenID);

                                    LoginDB loginDB = new LoginDB(0, editText_db_id.getText().toString(),id.getText().toString(),sha256(pw.getText().toString()),tokenID);
                                    loginDB.execute();
                                }
                            });
                }
                else if(login.getText().equals("Logout")){
                    MainActivity.editor.putString("db_id",null);
                    MainActivity.editor.putString("id",null);
                    MainActivity.editor.putString("pw",null);
                    MainActivity.editor.putString("write",null);
                    MainActivity.editor.apply();
                    //MainActivity.editor.clear();

                    MainActivity.logged_in_id = null;
                    MainActivity.logged_in_db_id=null;

                    id.setText("");
                    pw.setText("");
                    login.setText("Login");
                    join.setText("NEW");

                    textInputLayout_pw.setVisibility(View.VISIBLE);
                    textView_email.setVisibility(View.VISIBLE);

                    Toast.makeText(getApplicationContext(),"로그아웃 되었습니다", Toast.LENGTH_SHORT).show();
                }

            }
        });

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(imm!=null)
                    imm.hideSoftInputFromWindow(v.getWindowToken(),0);

                if(join.getText().equals("NEW")){
                    if(editText_db_id.getText().toString().equals("") || id.getText().toString().equals("") || pw.getText().toString().equals("")
                            || editText_db_id.getText()==null  || id.getText()==null || pw.getText()==null){
                        Toast.makeText(getApplicationContext(),"빈칸을 채워주세요", Toast.LENGTH_SHORT).show();
                    }

                    else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setTitle("새로운 ID를 생성합니다");
                        builder.setPositiveButton("취소", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {

                            }
                        });
                        builder.setNegativeButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseInstanceId.getInstance().getInstanceId()
                                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                                if (!task.isSuccessful()) {
                                                    Log.e("FirebaseMsgService", "getInstanceId failed", task.getException());
                                                    return;
                                                }

                                                // Get new Instance ID token
                                                String tokenID = task.getResult().getToken();
                                                Log.e("FirebaseMsgService", "Token: " + tokenID);
                                                LoginDB loginDB = new LoginDB(1, editText_db_id.getText().toString(),id.getText().toString(),sha256(pw.getText().toString()),tokenID);
                                                loginDB.execute();
                                            }
                                        });
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                }
                else if(join.getText().equals("Team")){

                    latch = new CountDownLatch(1);
                    String temp_team;
                    if(MainActivity.logged_in_db_id==null) temp_team = editText_db_id.getText().toString();
                    else temp_team = MainActivity.logged_in_db_id;
                    LoginDB loginDB = new LoginDB(2, temp_team);
                    loginDB.execute();
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    for(int i = 0; i<team.size() ; i++){
                        PersonalSetting personalSetting = new PersonalSetting(getApplicationContext(), team.get(i).toString());
                        login_setting_team.addView(personalSetting);
                    }

                    login1.setVisibility(View.GONE);
                    login_setting.setVisibility(View.VISIBLE);

                    textView_email.setVisibility(View.INVISIBLE);
                    join.setText("확인");
                    login.setVisibility(View.GONE);
                    back.setVisibility(View.GONE);

                }
                else if(join.getText().equals("확인")){

                    StringBuffer temp= new StringBuffer();
                    StringBuffer temp_search= new StringBuffer();
                    Map<String, ?> allEntries = sharedPreferences.getAll();
                    for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                        if(entry.getKey().contains("_notify") && entry.getValue().toString().equals("1")){
                            temp.append(entry.getKey().substring(0,entry.getKey().indexOf("_"))+",");
                        }
                        if(entry.getKey().contains("_search") && entry.getValue().toString().equals("1")){
                            temp_search.append(entry.getKey().substring(0,entry.getKey().indexOf("_"))+",");
                        }
                    }
                    if(temp.substring(0).equals("")) temp.append("없음");
                    if(temp_search.substring(0).equals("")) temp_search.append("없음");

                    MainActivity.checked_search = temp_search.substring(0);
                    editor.putString("search",MainActivity.checked_search);
                    editor.apply();

                    LoginDB loginDB = new LoginDB(3, MainActivity.logged_in_db_id, temp.substring(0),MainActivity.logged_in_id);
                    loginDB.execute();

                    login_setting_team.removeAllViews();

                    join.setText("Team");
                    login1.setVisibility(View.VISIBLE);

                    if(MainActivity.logged_in_db_id ==null ) textView_email.setVisibility(View.VISIBLE);

                    login.setVisibility(View.VISIBLE);
                    login_setting.setVisibility(View.GONE);

                    back.setVisibility(View.VISIBLE);

                    team.clear();

                    Toast.makeText(getApplicationContext(),"업데이트 완료", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onBackPressed() {

        if(MainActivity.logged_in_db_id ==null || MainActivity.logged_in_db_id.equals("")){
            Toast.makeText(getApplicationContext(),"로그아웃 상태입니다", Toast.LENGTH_SHORT).show();
        }else if(sharedPreferences.getString("write","team").equals("team"))
            Toast.makeText(getApplicationContext(),"팀을 선택해주세요", Toast.LENGTH_SHORT).show();
        else super.onBackPressed();
    }

    private class LoginDB extends AsyncTask {

        int case_num;
        String id, pw, db_id, tokenID, content;
        int error_code;
        String result;


        LoginDB(int case_num, String db_id, String id, String pw, String tokenID){

            this.case_num = case_num;
            this.db_id = db_id;
            this.id = id;
            this.pw = pw;
            this.tokenID = tokenID; }

        LoginDB(int case_num, String db_id){
            this.case_num = case_num;
            this.db_id = db_id; }

        LoginDB(int case_num, String db_id, String content, String id){
            this.case_num = case_num;
            this.db_id = db_id;
            this.id = id;
            this.content = content;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object[] objects) {


            String option;

            if(case_num ==0) option = "&login=1";
            else if(case_num ==1) option = "&join=1";
            else if (case_num ==2) option = "&team=1";
            else if (case_num ==3) option = "&update_notify=" + content;
            else option = "";

            /* 인풋 파라메터값 생성 */
            String param;
            if(case_num ==2) param = "db_id=" + db_id + option;
            else if(case_num ==3) param = "db_id=" + db_id + "&id=" + id + option;
            else param = "db_id=" + db_id +"&id=" + id + "&pw=" + pw + "&tokenID=" + tokenID + option;
            Log.e("sent DATA", param);

            try {
                /* 서버연결 */
                URL url = new URL(
                        "http://ssyp.synology.me:8812/worshipplus/login.php");
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

                if(data.contains("team_name")){
                    //서버에서 정보 가져오기
                    try {
                        JSONArray jsonArray = new JSONArray(data);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            team.add(jsonObject.getString("team_name"));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        error_code = 7;
                    }

                    latch.countDown();
                }

                if(data.equals("not matched")) error_code = 0; // 비밀번호 불일치
                else if(data.equals("matched")){
                    error_code = 1;// 비밀번호 일치
                    result=id;
                }
                else if(data.equals("1146")) error_code = 6; // 그룹 없음

                if(data.equals("invalid")) error_code = 2; // 아이디 중복
                if(data.equals("added")) error_code = 3; // 생성 성공

                //서버에서 개인정보 가져오기

            } catch (MalformedURLException e) {
                e.printStackTrace();
                error_code = 4;
            } catch (IOException e) {
                e.printStackTrace();
                error_code = 5;
            }

            return null;
        }


        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            switch (error_code){
                case 0:
                    if(case_num != 2 && case_num != 3)
                    Toast.makeText(getApplicationContext(),"로그인 정보가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    setResult(Activity.RESULT_OK);
                    MainActivity.editor.putString("db_id",db_id);
                    MainActivity.editor.putString("id",id);
                    MainActivity.editor.putString("pw",pw);
                    MainActivity.editor.apply();
                    MainActivity.logged_in_db_id = db_id;
                    MainActivity.logged_in_id = id;

                    if(sharedPreferences.getString("write","team").equals("team")){
                        //if(!db_id.equals(MainActivity.logged_in_db_id)) MainActivity.editor.clear();
                        Toast.makeText(getApplicationContext(),"팀을 선택해주세요", Toast.LENGTH_SHORT).show();
                        join.setText("Team");
                        return;
                    }

                    finish();
                    break;
                case 2:
                    Toast.makeText(getApplicationContext(),"이미 존재하는 아이디입니다", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(getApplicationContext(),"계정이 성공적으로 생성되었습니다!", Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    Toast.makeText(getApplicationContext(),"URL ERROR", Toast.LENGTH_SHORT).show();
                    break;
                case 5:
                    Toast.makeText(getApplicationContext(),"I/O ERROR", Toast.LENGTH_SHORT).show();
                    break;
                case 6:
                    Toast.makeText(getApplicationContext(),"그룹 아이디가 올바르지 않습니다", Toast.LENGTH_SHORT).show();
                    break;
                case 7:
                    Toast.makeText(getApplicationContext(),"TEAM_json ERROR", Toast.LENGTH_SHORT).show();
                    break;
            }

            cancel(true);

        }


    }


}
