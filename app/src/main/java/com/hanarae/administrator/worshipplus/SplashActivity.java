package com.hanarae.administrator.worshipplus;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.ActivityResult;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;


public class SplashActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    AppUpdateManager appUpdateManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        sharedPreferences = getSharedPreferences("login",0);
        if(sharedPreferences.getInt("mode",1) == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else if(sharedPreferences.getInt("mode",1) == AppCompatDelegate.MODE_NIGHT_NO){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        //핸드폰 디폴트 다크모드 적용여부
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
            // Creates instance of the manager.
            appUpdateManager = AppUpdateManagerFactory.create(this);

            // Returns an intent object that you use to check for an update.
            Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

            // Checks that the platform will allow the specified type of update.
            appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        // For a flexible update, use AppUpdateType.FLEXIBLE
                        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    // Request the update.
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                                // Pass the intent that is returned by 'getAppUpdateInfo()'.
                                appUpdateInfo,
                                // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                                AppUpdateType.IMMEDIATE,
                                // The current activity making the update request.
                                this,
                                // Include a request code to later monitor this update request.
                                1);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }

                } else  {
                    Handler hd = new Handler();
                    hd.postDelayed(new splashhandler(), 1500); // 1초 후에 hd handler 실행  3000ms = 3초
                }

            });

        }
    }

    private class splashhandler implements Runnable{
        public void run(){
            startActivity(new Intent(getApplication(), MainActivity.class)); //로딩이 끝난 후 이동
            SplashActivity.this.finish(); // 로딩페이지 Activity stack에서 제거
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode != RESULT_OK) {
                Log.e("Update Result", "Result code: " + resultCode);
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }
            if (resultCode != RESULT_CANCELED) {
                Log.e("Update Result", "Result code: " + resultCode);
                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                adb.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Handler hd = new Handler();
                        hd.postDelayed(new splashhandler(), 1500); // 1초 후에 hd handler 실행  3000ms = 3초
                    }
                });

                adb.setTitle(getString(R.string.app_name));
                adb.setMessage("추후 업데이트를 다시 실행합니다");
                adb.show();

            }
            if (resultCode != ActivityResult.RESULT_IN_APP_UPDATE_FAILED) {
                Log.e("Update Result", "Result code: " + resultCode);
                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                adb.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Handler hd = new Handler();
                        hd.postDelayed(new splashhandler(), 1500); // 1초 후에 hd handler 실행  3000ms = 3초
                    }
                });

                adb.setTitle(getString(R.string.app_name));
                adb.setMessage("추후 업데이트를 다시 실행합니다");
                adb.show();

            }
        }
    }

    // Checks that the update is not stalled during 'onResume()'.
    // However, you should execute this check at all entry points into the app.
    @Override
    protected void onResume() {
        super.onResume();
        if(appUpdateManager==null) return;
        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(
                        appUpdateInfo -> {
                            if (appUpdateInfo.updateAvailability()
                                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                // If an in-app update is already running, resume the update.
                                try {
                                    appUpdateManager.startUpdateFlowForResult(
                                            appUpdateInfo,
                                            AppUpdateType.IMMEDIATE,
                                            this,
                                            1);
                                } catch (IntentSender.SendIntentException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
    }


    @Override
    public void onBackPressed() {
        //초반 플래시 화면에서 넘어갈때 뒤로가기 버튼 못누르게 함
    }

}
