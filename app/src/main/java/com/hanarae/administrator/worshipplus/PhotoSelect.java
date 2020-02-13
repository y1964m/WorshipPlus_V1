package com.hanarae.administrator.worshipplus;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

public class PhotoSelect extends AppCompatActivity {

    static ImageView imageView;
    static WebView webView;
    WebSettings webSettings;
    static Context context;

    static Photo_Recycler_Adapter adapter;
    RecyclerView recyclerView;

    Button load,camera,save, cancel;
    int REQ_CODE_SELECT_IMAGE = 1001;
    private String mImgPath = null;
    private String mImgTitle = null;
    private String mImgOrient = null;
    private String mImgType = null;
    String serverAddress = "http://ssyp.synology.me:8812/worshipplus/";
    int position, position_main_search;
    String song_name, url;
    Uri uri;
    CountDownLatch latch;
    SearchDB searchDB_photo;
    static Data data_photo;
    static boolean isFile;
    static String temp_string;

    public static void verifyStoragePermissions(Activity activity) {


        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
    }



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        song_name = intent.getStringExtra("song_name");
        if(song_name==null || song_name.equals("")) song_name = "empty";
        position = intent.getIntExtra("position",0);
        position_main_search= intent.getIntExtra("position_main_search",0);
        url = intent.getStringExtra("url");
        isFile = false;
        temp_string=null;

        verifyStoragePermissions(this);

        setContentView(R.layout.photo_select);
        recyclerView = findViewById(R.id.recycler_view_photo);

        imageView = findViewById(R.id.imageview_sheet);
        webView = findViewById(R.id.webview_sheet);
        webSettings = webView.getSettings();
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setBuiltInZoomControls(true);

        load = findViewById(R.id.button_photo_load);
        //camera = findViewById(R.id.button_photo_camera);
        save = findViewById(R.id.button_photo_save);
        cancel = findViewById(R.id.button_photo_cancel);

        if(position==888){ // 메인화면 이번주 콘티에서 버튼 가리기
            load.setVisibility(View.GONE);
            save.setVisibility(View.GONE);
        }

        if(position!=888 && position!=999) temp_string = ThirdFragment.adapter.listData.get(position).getSingle_sheet();

        context = this;
        data_photo = new Data();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new Photo_Recycler_Adapter();
        recyclerView.setAdapter(adapter);

        if(position!=888){
            latch = new CountDownLatch(1);
            searchDB_photo = new SearchDB(4, getApplicationContext(), latch, song_name);
            new AsyncTaskCancelTimerTask(searchDB_photo, 10000, 1000, true).start();
            searchDB_photo.execute();
            adapter.listData.clear();//초기화 하고, 안그러면 밑으로 똑같은 뷰가 계속 붙음
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            searchDB_photo.cancel(true);


            for (int i = 0; i < data_photo.getTitleArrayListSize(); i++) {
                // 각 List의 값들을 data 객체에 set 해줍니다.
                Data data = new Data();
                data.setSingle_Sheet_url(data_photo.getSheet_url(i));
                data.setTitle(data_photo.getTitleArrayListItem(i));


                // 각 값이 들어간 data를 adapter에 추가합니다.
                if(data.getTitle()!=null) adapter.addItem(data);
            }
            adapter.notifyDataSetChanged();
        }


        if(position == 888 ){// 메인화면 콘티
            if(url==null || url.equals("")){
                PhotoSelect.webView.setVisibility(View.GONE);
                PhotoSelect.imageView.getLayoutParams().height = ((LinearLayout) PhotoSelect.imageView.getParent()).getLayoutParams().height;
                PhotoSelect.imageView.setVisibility(View.VISIBLE);
            }else{

                if(url.contains(",")){//저장된 악보가 2개일때
                    PhotoSelect.webView.loadUrl(url.substring(0,url.indexOf(",")));
                }//하나의 악보일때
                else PhotoSelect.webView.loadUrl(url);

                recyclerView.setVisibility(View.GONE);
                PhotoSelect.imageView.setVisibility(View.GONE);
                PhotoSelect.webView.getLayoutParams().height = ((LinearLayout) PhotoSelect.webView.getParent()).getLayoutParams().height;
                PhotoSelect.webView.setVisibility(View.VISIBLE);
            }
        }
        else if(position == 999){//  검색 클릭하고 기본정보 곡에 업뎃할때
            if(PraiseSearch.adapter.listData.get(position_main_search).getSheet(0)!=null){
                if(adapter.getItemCount()!=0){
                    PhotoSelect.webView.loadUrl(adapter.listData.get(0).getSingle_Sheet_url());
                    PhotoSelect.imageView.setVisibility(View.GONE);
                    PhotoSelect.webView.setVisibility(View.VISIBLE);
                }
                save.setText("확인");
                cancel.setVisibility(View.GONE);
            }
        }
        else {//일반 콘티 인풋때
            /*if(ThirdFragment.adapter.listData.get(position).getSingle_sheet_temp()!=null){//사진이 미리 들어있는지 확인
                Uri uri_temp=ThirdFragment.adapter.listData.get(position).getSingle_sheet_temp();
                imageView.setImageURI(uri_temp);

            }else*/ if(ThirdFragment.adapter.listData.get(position).getSingle_sheet()!=null){// 웹주소가 들어있는지 확인

                if(ThirdFragment.adapter.listData.get(position).getSingle_sheet().contains(",")){//저장된 악보가 2개일때
                    PhotoSelect.webView.loadUrl(ThirdFragment.adapter.listData.get(position).getSingle_sheet().substring(0,ThirdFragment.adapter.listData.get(position).getSingle_sheet().indexOf(",")));
                }//하나의 악보일때
                else PhotoSelect.webView.loadUrl(ThirdFragment.adapter.listData.get(position).getSingle_sheet());

                PhotoSelect.imageView.setVisibility(View.GONE);
                PhotoSelect.webView.setVisibility(View.VISIBLE);
            }else {

                if(url.contains(",")){//저장된 악보가 2개일때
                    PhotoSelect.webView.loadUrl(url.substring(0,url.indexOf(",")));
                }//하나의 악보일때
                else PhotoSelect.webView.loadUrl(url);

                PhotoSelect.imageView.setVisibility(View.GONE);
                PhotoSelect.webView.setVisibility(View.VISIBLE);

            }
        }

        if(data_photo.getTitleArrayListSize()==0 && position != 888){// 메인콘티나 정보 없을때는 환면 회색 표시
            webView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
        }

        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoSelect = new Intent(Intent.ACTION_PICK);
                photoSelect.setType(MediaStore.Images.Media.CONTENT_TYPE);
                photoSelect.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(photoSelect,REQ_CODE_SELECT_IMAGE);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*if(isFile){
                    ThirdFragment.adapter.listData.get(position).setSingle_sheet_temp(uri);
                    ThirdFragment.adapter.listData.get(position).setSingle_sheet(adapter.listData.get(adapter.getItemCount()-1).getSingle_Sheet_url());
                    ThirdFragment.adapter.listData.get(position).setSingle_Sheet_url(null);
                    Toast.makeText(getApplicationContext(),"업데이트 성공", Toast.LENGTH_SHORT).show();
                    finish();
                }


                else */if(position==999) {
                    //PraiseSearch.song_search=true;
                    finish(); // 기본정보 엣뎃 시 실행 위의 http에서 업로드 했기에 여기서는 아무 작업도 안함
                }

                else{// 웹주소 받아서 그릇에만 저장
                    ThirdFragment.adapter.listData.get(position).setSingle_Sheet_url(temp_string);
                    ThirdFragment.adapter.listData.get(position).setSingle_sheet(temp_string);
                    //ThirdFragment.adapter.listData.get(position).setSingle_Sheet_url(webView.getUrl());
                    //ThirdFragment.adapter.listData.get(position).setSingle_sheet(webView.getUrl());
                    //ThirdFragment.adapter.listData.get(position).setSingle_sheet_temp(null);
                    Toast.makeText(getApplicationContext(),"업데이트 완료", Toast.LENGTH_LONG).show();
                    finish();

                    if(adapter.getItemCount()==0){
                        ThirdFragment.adapter.listData.get(position).setSingle_Sheet_url("");
                        ThirdFragment.adapter.listData.get(position).setSingle_sheet("");
                    }

                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAndRemoveTask();
            }
        });


    }

    @Override

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // 선택된 사진을 받아 서버에 업로드한다
        if (requestCode == REQ_CODE_SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                uri = data.getData();
                getImageNameToUri(uri);
                try {
                    /*imageView.setImageURI(uri);
                    webView.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);*/
                }
                catch(Exception e) {
                    e.printStackTrace();
                }

                 new HttpRequestAsyncTask().execute(mImgPath, mImgTitle, mImgOrient, mImgType); // 파일받아서 서버에 업로드
            }
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
                if(asyncTask.getStatus() == AsyncTask.Status.FINISHED)
                    return;

                if(asyncTask.getStatus() == AsyncTask.Status.PENDING ||
                        asyncTask.getStatus() == AsyncTask.Status.RUNNING ) {

                    asyncTask.cancel(interrupt);
                    Toast.makeText(PhotoSelect.this,"네트워크 에러",Toast.LENGTH_SHORT).show();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

        private void getImageNameToUri(Uri data) {
            String[] proj = {
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.TITLE,
                    MediaStore.Images.Media.ORIENTATION,
                    MediaStore.Images.Media.MIME_TYPE
            };

            Cursor cursor = this.getContentResolver().query(data, proj, null, null, null);
            cursor.moveToFirst();

            int column_data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            int column_title = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
            int column_orientation = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION);
            int column_type = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE);

            mImgPath = cursor.getString(column_data);
            mImgTitle = cursor.getString(column_title);
            mImgOrient = cursor.getString(column_orientation);
            mImgType = cursor.getString(column_type);

        }

    void imageUpload(final String filename, String song_name) {
        String urlString = serverAddress + "sheet_upload.php";

        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "******";

        try {

            //Log.d("Test", "file name is " + filename);
            FileInputStream mFileInputStream = new FileInputStream(filename);
            URL connectUrl = new URL(urlString);

            // open connection
            HttpURLConnection con = (HttpURLConnection) connectUrl.openConnection();
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.setRequestMethod("POST");
            con.setRequestProperty("ENCTYPE", "multipart/form-data");
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary+";charset=UTF-8");


            // write data
            DataOutputStream dos = new DataOutputStream(con.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"song_name\"\r\n\r\n");
            dos.writeUTF(song_name+lineEnd);
            dos.writeBytes( twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"user_account\"\r\n\r\n");
            dos.writeBytes(MainActivity.logged_in_db_id +lineEnd);
            dos.writeBytes( twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + URLEncoder.encode(filename,"utf-8") + "\"" + lineEnd);
            dos.writeBytes("Content-Type: application/octet-stream\r\n\r\n");

            int bytesAvailable = mFileInputStream.available();
            int maxBufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            byte[] buffer = new byte[bufferSize];
            int bytesRead = mFileInputStream.read(buffer, 0, bufferSize);

            // read image
            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = mFileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = mFileInputStream.read(buffer, 0, bufferSize);
            }

            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // close streams
            int serverResponseCode = con.getResponseCode();
            String serverResponseMessage = con.getResponseMessage();
            Log.i("Uploaded", "HTTP Response is : "

                    + serverResponseMessage + ": " + serverResponseCode);

            Log.e("Test", "File is written:"+filename);
            //Log.e("Test", filename+"");
            mFileInputStream.close();
            dos.flush(); // finish upload...

            // get response
            int ch;
            InputStream is = con.getInputStream();
            StringBuffer b = new StringBuffer();
            while ((ch = is.read()) != -1) {
                b.append((char) ch);
            }
            String s = b.toString();
            Log.e("result", ":" + s);
            dos.close();


        } catch (Exception e) {
            Log.d("Test", "exception " + e.getMessage());
            // TODO: handle exception
        }
    }

    public class HttpRequestAsyncTask extends AsyncTask<String, Integer, Integer> {

        private String mImagePath = null;
        private String mImageTitle = null;
        private String mImageOrientation = null;
        private String mImageType = null;
        private ProgressDialog mWaitDlg = null;

        // 작업을 시작하기 전에 필요한 UI를 화면에 보여준다
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mWaitDlg = new ProgressDialog(PhotoSelect.this);
            mWaitDlg.setMessage("이미지 정보 전송 중...");
            mWaitDlg.show();
        }

        // 서버 요청 작업을 진행한다

        @Override
        protected Integer doInBackground(String... arg) {
            mImagePath = arg[0];
            mImageTitle = arg[1];
            mImageOrientation = arg[2];
            mImageType = arg[3].substring(6);
            if (mImageType.equals("jpeg")) mImageType = "jpg";
            HashMap<String, String> params = new HashMap<>();

            params.put("IMG_FILE_PATH", mImagePath);
            params.put("IMG_TITLE", mImageTitle);
            params.put("IMG_ORIENTATION", mImageOrientation);

            imageUpload(mImgPath,song_name);

            int result = 0;//uploadImageInfo(params);

            return result;

        }

        // 서버 작업 진행 중에 UI를 갱신할 필요가 있는 경우 호출된다. doInBackground에서 publishProgress()를 호출하면 invoked된다
        @Override
        protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        }

        // 서버 작업 완료 후 화면에 필요한 UI를 보여준다
        @Override
        protected void onPostExecute(Integer aResult) {
        super.onPostExecute(aResult);
            if (mWaitDlg != null) {
                mWaitDlg.dismiss();
                mWaitDlg = null;
            }

            if (aResult == null) {  return; }

            if (aResult.intValue() == 0) {
                if(position==999){ // 검색 클릭하고 기본정보 곡에 업뎃할때


                    String db_data="";

                    db_data += "&filename=" + mImageTitle.replace(" ","") +"."+mImageType
                            + "&title=" + song_name + "&sheet=" + serverAddress+"sheet_images/"+mImageTitle+"."+mImageType;

                    latch = new CountDownLatch(1);
                    InputDB inputDB = new InputDB(getApplicationContext(),latch, db_data,2);
                    new AsyncTaskCancelTimerTask(inputDB, 10000, 1000, true).start();
                    inputDB.execute();

                    try {
                        latch.await();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    inputDB.cancel(true);

                    Data temp_data_photo = new Data();
                    temp_data_photo.setTitle("기본정보");
                    temp_data_photo.setSingle_Sheet_url(serverAddress+"sheet_images/"+mImageTitle+"."+mImageType);
                    //temp_data_photo.setSingle_sheet_temp(uri);
                    adapter.addItem(temp_data_photo);
                    adapter.notifyDataSetChanged();

                    PhotoSelect.webView.loadUrl(serverAddress+"sheet_images/"+mImageTitle+"."+mImageType);
                    PhotoSelect.imageView.setVisibility(View.GONE);
                    PhotoSelect.webView.setVisibility(View.VISIBLE);

                    Toast.makeText(getApplicationContext(),"기본정보 업데이트 완료", Toast.LENGTH_SHORT).show();
                    //finish();
                }
                else {
                    Data temp_data_photo = new Data();
                    temp_data_photo.setTitle(song_name+"-temp");
                    temp_data_photo.setSingle_Sheet_url(serverAddress+"sheet_images/"+mImageTitle+"."+mImageType);
                    //temp_data_photo.setSingle_sheet_temp(uri);
                    adapter.addItem(temp_data_photo);
                    adapter.notifyDataSetChanged();

                    PhotoSelect.webView.loadUrl(serverAddress+"sheet_images/"+mImageTitle+"."+mImageType);
                    PhotoSelect.imageView.setVisibility(View.GONE);
                    PhotoSelect.webView.setVisibility(View.VISIBLE);

                    //Toast.makeText(getApplicationContext(),"업데이트 성공", Toast.LENGTH_SHORT).show();
                    //finish();
                }
            }

       else {
               Toast.makeText(getApplicationContext(),"업데이트 실패", Toast.LENGTH_SHORT).show();
               finish();
            }

        }

    }

}