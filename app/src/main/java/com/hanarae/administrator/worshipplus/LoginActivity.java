package com.hanarae.administrator.worshipplus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);     //다이얼로그에서 사용할 레이아웃입니다.

        final EditText id, pw, db_id;
        Button login, join;

        db_id= findViewById(R.id.editText_db_id);
        db_id.setText(MainActivity.logged_in_db_id);

        id = findViewById(R.id.editText_id);
        id.setText(MainActivity.logged_in_id);

        pw = findViewById(R.id.editText_pw);
        login = findViewById(R.id.button_login);
        join = findViewById(R.id.button_join);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                LoginDB loginDB = new LoginDB(0, db_id.getText().toString(),id.getText().toString(),pw.getText().toString(),tokenID);
                                loginDB.execute();
                            }
                        });
            }
        });

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(db_id.getText().toString().equals("") || id.getText().toString().equals("") || pw.getText().toString().equals("")
                        || db_id.getText()==null  || id.getText()==null || pw.getText()==null){
                    Toast.makeText(getApplicationContext(),"로그인 정보를 입력해주세요", Toast.LENGTH_SHORT).show();
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
                                            LoginDB loginDB = new LoginDB(1, db_id.getText().toString(),id.getText().toString(),pw.getText().toString(),tokenID);
                                            loginDB.execute();
                                        }
                                    });
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        if(MainActivity.logged_in_db_id ==null || MainActivity.logged_in_db_id.equals("")){
            Toast.makeText(getApplicationContext(),"로그아웃 상태입니다", Toast.LENGTH_SHORT).show();
        }
        else super.onBackPressed();

    }

    private class LoginDB extends AsyncTask {

        int case_num;
        String id, pw, db_id, tokenID;
        int error_code;
        String result;


        LoginDB(int case_num, String db_id, String id, String pw, String tokenID){

            this.case_num = case_num;
            this.db_id = db_id;
            this.id = id;
            this.pw = pw;
            this.tokenID = tokenID;        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object[] objects) {


            String option;

            if(case_num ==0) option = "&login=1";
            else option = "&join=1";

            /* 인풋 파라메터값 생성 */
            String param = "db_id=" + db_id +"&id=" + id + "&pw=" + pw + "&tokenID=" + tokenID + option;

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

                if(data.contains("0")) error_code = 0; // 비밀번호 불일치
                else if(data.contains("1")){
                    error_code = 1;// 비밀번호 일치
                    result=id;
                }

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
                    Toast.makeText(getApplicationContext(),"로그인 정보가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(getApplicationContext(),"환영합니다! " + result, Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    MainActivity.editor.putString("db_id",db_id);
                    MainActivity.editor.putString("id",id);
                    MainActivity.editor.putString("pw",pw);
                    MainActivity.editor.apply();
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
            }

            cancel(true);

        }


    }


}
