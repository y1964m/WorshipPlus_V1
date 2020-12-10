package com.hanarae.administrator.worshipplus;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

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

        //인터넷 연결확인 작업
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isMobile=true;
        boolean isWiFi=true;
        boolean isWiMax=true;

        if(manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)!=null) {
            isMobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        }
        if(manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!=null) {
            isWiFi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        }
        if(manager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX)!=null) {
            isWiMax = manager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX).isConnectedOrConnecting();
        }


        if (!isMobile && !isWiFi) {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

            adb.setTitle(getString(R.string.app_name));
            adb.setMessage("인터넷 연결을 확인해주세요");
            adb.show();
        }

        else {
            Handler hd = new Handler();
            hd.postDelayed(new splashhandler(), 1500); // 1초 후에 hd handler 실행  3000ms = 3초
        }

    }

    private class splashhandler implements Runnable{
        public void run(){
            startActivity(new Intent(getApplication(), MainActivity.class)); //로딩이 끝난 후 이동
            SplashActivity.this.finish(); // 로딩페이지 Activity stack에서 제거
        }
    }

    @Override
    public void onBackPressed() {
        //초반 플래시 화면에서 넘어갈때 뒤로가기 버튼 못누르게 함
    }

}
