package com.hanarae.administrator.worshipplus;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static com.hanarae.administrator.worshipplus.PraiseSearch.adapter;


public class CustomDialog extends Dialog {

    EditText explanation;
    Button save, cancel;
    InputMethodManager imm;
    TextView explanation_text;
    CountDownLatch latch;


    public CustomDialog(final int type, final Context context, final int position, final InputMethodManager imm, String content_string) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);   //다이얼로그의 타이틀바를 없애주는 옵션입니다.
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));  //다이얼로그의 배경을 투명으로 만듭니다.
        setContentView(R.layout.custom_dialog);     //다이얼로그에서 사용할 레이아웃입니다.

        explanation = findViewById(R.id.editText_dialog_explanation);
        explanation.setMovementMethod(LinkMovementMethod.getInstance());
        explanation_text = findViewById(R.id.textView_dialog_explanation);
        explanation_text.setMovementMethod(LinkMovementMethod.getInstance());
        save = findViewById(R.id.button_dialog_save);
        cancel = findViewById(R.id.button_dialog_cancel);
        this.imm=imm;

        switch (type){
            // 0~2 : 인풋
            case 0:
                explanation.setVisibility(View.VISIBLE);
                explanation_text.setVisibility(View.GONE);
                explanation.setText(content_string);
                //explanation.setText(ThirdFragment.adapter.listData.get(position).getExplanation(0));
                //explanation.setText(ThirdFragment.adapter.listData.get(position).getSingle_explanation());
                explanation.setHint("세부콘티");
                break;
            case 1: // 기본정보 길게 눌러 코드 수정 업뎃할때
                explanation.setVisibility(View.VISIBLE);
                explanation_text.setVisibility(View.GONE);
                explanation.setHint("코드변경");
                explanation.setText(content_string);
                save.setText("저장");
                break;
            case 2:
                explanation.setVisibility(View.VISIBLE);
                explanation_text.setVisibility(View.GONE);
                explanation.setText(content_string);
                //explanation.setText(ThirdFragment.adapter.listData.get(position).getSingle_music());
                explanation.setHint("링크주소");
                break;

            case 3: // 검색결과 콘티 보여주는 케이스
                explanation.setVisibility(View.GONE);
                explanation_text.setVisibility(View.VISIBLE);
                explanation_text.setTextIsSelectable(true);
                explanation_text.setText(content_string);
                //save.setText("복사완료");
                save.setVisibility(View.GONE);
                cancel.setText("확인");
                break;
            case 4: // 기본정보 곡 이름 수정할때
                explanation.setVisibility(View.VISIBLE);
                explanation_text.setVisibility(View.GONE);
                explanation.setHint("곡변경");
                explanation.setText(content_string);
                save.setText("저장");
                break;
            case 5: // 검색결과 링크 보여주는 케이스
                explanation.setVisibility(View.GONE);
                explanation_text.setVisibility(View.VISIBLE);
                explanation_text.setText(content_string);
                save.setVisibility(View.GONE);
                cancel.setText("확인");
                break;

            //6~7 검색결과 중 기본정보용 (수정가능)
            case 6:
                explanation.setVisibility(View.VISIBLE);
                explanation_text.setVisibility(View.GONE);
                explanation.setHint("메모");
                explanation.setText(content_string);
                save.setText("저장");
                break;
            case 7:
                explanation.setVisibility(View.VISIBLE);
                explanation_text.setVisibility(View.GONE);
                explanation.setHint("ex) 마커스: \n https://www.youtube.com/1234");
                explanation.setText(content_string);
                save.setText("저장");
                break;
            case 8: //카톡용 복사
                explanation.setVisibility(View.VISIBLE);
                explanation_text.setVisibility(View.GONE);
                explanation.setHint("다음의 콘티양식을 꼭 지켜주세요\n0 찬양제목 - A>B (엔터)\n 세부콘티 (엔터)\n1 찬양제목 - G (엔터)\n 세부콘티 (엔터)");
                explanation.setText(content_string);
                save.setText("저장");
                break;

          /*      //콘티 수정할때 불러와서 로드하는 코드
                case 8:
                    explanation.setVisibility(View.VISIBLE);
                    explanation_text.setVisibility(View.GONE);
                    explanation.setText(ThirdFragment.adapter.listData.get(position).getExplanation(0));
                    explanation.setHint("세부콘티");
                    break;
                case 9:
                    explanation.setVisibility(View.VISIBLE);
                    explanation_text.setVisibility(View.GONE);
                    explanation.setText(ThirdFragment.adapter.listData.get(position).getSheet(0));
                    explanation.setHint("악보경로");
                    break;
                case 10:
                    explanation.setVisibility(View.VISIBLE);
                    explanation_text.setVisibility(View.GONE);
                    explanation.setText(ThirdFragment.adapter.listData.get(position).getMusic(0));
                    explanation.setHint("링크주소");
                    break;*/
        }



        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imm.hideSoftInputFromWindow(v.getWindowToken(),0);

                ArrayList tempArray;

                switch (type){
                    case 0:
                        /*tempArray= new ArrayList();
                        tempArray.add(explanation.getText().toString());*/
                        ThirdFragment.adapter.listData.get(position).setSingle_explanation(explanation.getText().toString());
                        dismiss();
                        break;
                    case 1: // 기본정보 코드 수정
                        String db_data4="";

                        db_data4 += "&id_date=0000"
                                + "&title=" + adapter.listData.get(position).getTitle()
                                + "&chord=" + adapter.listData.get(position).getContent()
                                + "&changed_chord=" + explanation.getText().toString().replace("\"","\\\"");

                        latch = new CountDownLatch(1);
                        InputDB inputDB4 = new InputDB(getContext(),latch, db_data4,2);
                        inputDB4.execute();

                        try {
                            latch.await();

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        inputDB4.cancel(true);
                        Toast.makeText(getContext(),"변경되었습니다.",Toast.LENGTH_SHORT).show();
                        imm.hideSoftInputFromWindow(v.getWindowToken(),0);
                        dismiss();

                        /*tempArray= new ArrayList();
                        tempArray.add(explanation.getText().toString());
                        ThirdFragment.adapter.listData.get(position).setSheet(tempArray);
                        dismiss();*/
                        break;
                    case 2:
                        /*tempArray= new ArrayList();
                        tempArray.add(explanation.getText().toString());*/
                        ThirdFragment.adapter.listData.get(position).setSingle_music(explanation.getText().toString());
                        dismiss();
                        break;



                    case 3:
                        //ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
                        //clipboard.setText(explanation_text.getText().toString());
                        dismiss();
                        //Toast.makeText(getContext(),"복사완료",Toast.LENGTH_SHORT).show();
                        break;
                    case 4:// 기본정보 곡 이름 수정할때
                        String db_data3="";

                        db_data3 += "&id_date=0000"
                                + "&title=" + adapter.listData.get(position).getTitle()
                                + "&chord=" + adapter.listData.get(position).getContent()
                                + "&changed_title=" + explanation.getText().toString().replace("\"","\\\"");

                        latch = new CountDownLatch(1);
                        InputDB inputDB3 = new InputDB(getContext(),latch, db_data3,2);
                        inputDB3.execute();

                        try {
                            latch.await();

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        inputDB3.cancel(true);
                        Toast.makeText(getContext(),"변경되었습니다.",Toast.LENGTH_SHORT).show();
                        imm.hideSoftInputFromWindow(v.getWindowToken(),0);
                        dismiss();
                        break;
                    case 5:
                        break;



                    case 6: // 세부내용 db에 update (기본정보)
                        String db_data="";

                        db_data += "&id_date=0000"
                                + "&title=" + adapter.listData.get(position).getTitle()
                                + "&chord=" + adapter.listData.get(position).getContent()
                                + "&tag=" + explanation.getText().toString().replace("\"","\\\"");

                        latch = new CountDownLatch(1);
                        InputDB inputDB = new InputDB(getContext(),latch, db_data,2);
                        inputDB.execute();

                        try {
                            latch.await();

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        inputDB.cancel(true);
                        Toast.makeText(getContext(),"변경되었습니다.",Toast.LENGTH_SHORT).show();
                        imm.hideSoftInputFromWindow(v.getWindowToken(),0);
                        dismiss();
                        break;
                    case 7: // 신곡의 링크정보 update
                        String db_data2="";

                        db_data2 += "&id_date=0000"
                                + "&title=" + adapter.listData.get(position).getTitle()
                                + "&chord=" + adapter.listData.get(position).getContent()
                                + "&music=" + explanation.getText().toString().replace("\"","\\\"");



                        latch = new CountDownLatch(1);
                        InputDB inputDB2 = new InputDB(getContext(),latch, db_data2,2);
                        inputDB2.execute();

                        try {
                            latch.await();

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        inputDB2.cancel(true);
                        Toast.makeText(getContext(),"변경되었습니다.",Toast.LENGTH_SHORT).show();
                        imm.hideSoftInputFromWindow(v.getWindowToken(),0);
                        dismiss();
                        break;
                    case 8: //카톡용 복사

                        MainActivity.tempConti.removeTitleArrayList();
                        MainActivity.tempConti.removeChordArrayList();
                        MainActivity.tempConti.removeDateArrayList();
                        MainActivity.tempConti.removeExplanationArrayList();
                        MainActivity.tempConti.removeMusicArrayList();
                        MainActivity.tempConti.removeSheetArrayList();

                        String toParse = explanation.getText().toString();
                        //toParse = toParse.replace("\"","\\\"").replace("\'","\\\'");
                        String[] arr = toParse.split("\\n");

                        boolean fail = false;
                        for(int i = 0; i < arr.length; i++){
                            if (arr[i].length()<1) i++;
                            if (arr[i].length()<1) i++;
                            if (arr[i].length()<1) i++;

                            if ((arr[i].charAt(0)=='0'||arr[i].charAt(0)=='1'||arr[i].charAt(0)=='2'||arr[i].charAt(0)=='3'||arr[i].charAt(0)=='4'||arr[i].charAt(0)=='5'
                                    ||arr[i].charAt(0)=='6'||arr[i].charAt(0)=='7'||arr[i].charAt(0)=='8'||arr[i].charAt(0)=='9')
                                    &&(arr[i].charAt(1) == '.' || arr[i].charAt(1)==' ')){
                               if(!arr[i].contains("-")){
                                   MainActivity.tempConti.removeTitleArrayList();
                                   MainActivity.tempConti.removeChordArrayList();
                                   MainActivity.tempConti.removeDateArrayList();
                                   MainActivity.tempConti.removeExplanationArrayList();
                                   MainActivity.tempConti.removeMusicArrayList();
                                   MainActivity.tempConti.removeSheetArrayList();
                                   fail = true;
                                   Toast.makeText(getContext(),arr[i].charAt(0)+"곡 코드 앞에 - 를 입력해주세요",Toast.LENGTH_SHORT).show();
                                   return;
                               }
                               ArrayList temp_arr = new ArrayList();
                               temp_arr.add("");
                               MainActivity.tempConti.setDateArrayList(temp_arr);
                               MainActivity.tempConti.setMusicArrayList(temp_arr);
                               MainActivity.tempConti.setSheetArrayList(temp_arr);

                               MainActivity.tempConti.addTitleArrayListItem(arr[i].substring(arr[i].indexOf(" ")+1, arr[i].lastIndexOf("-")));
                               MainActivity.tempConti.setChordArrayList(arr[i].substring(arr[i].lastIndexOf("-")+1));

                               temp_arr.clear();
                               //temp_arr.add(arr[i+1]);
                               //MainActivity.tempConti.setExplanationArrayList(temp_arr);
                               if(arr[i+1].equals("")||arr[i+1]==null) MainActivity.tempConti.addExplanation("없음");
                               else MainActivity.tempConti.addExplanation(arr[i+1]);

                           }
                    }

                    if(!fail){
                        Toast.makeText(getContext(),"반영완료",Toast.LENGTH_SHORT).show();
                        ThirdFragment.adapter.listData.clear();

                        for (int i = 0; i < MainActivity.tempConti.getTitleArrayListSize(); i++) {
                            // 각 List의 값들을 data 객체에 set 해줍니다.
                            Data data = new Data();
                            data.setTitle(MainActivity.tempConti.getTitleArrayListItem(i));
                            data.setContent(MainActivity.tempConti.getChordArrayListItem(i));

                            data.setDate(MainActivity.tempConti.getDateArrayListItem(i));
                            data.setSingle_explanation(MainActivity.tempConti.getExplanation(i));
                            data.setMusic(MainActivity.tempConti.getMusicArrayListItem(i));
                            data.setSheet(MainActivity.tempConti.getSheetArrayListItem(i));

                            // 각 값이 들어간 data를 adapter에 추가합니다.
                            if(data.getTitle()!=null) ThirdFragment.adapter.addItem(data);
                        }

                        ThirdFragment.adapter.notifyDataSetChanged();

                        MainActivity.tempConti.removeTitleArrayList();
                        MainActivity.tempConti.removeChordArrayList();
                        MainActivity.tempConti.removeDateArrayList();
                        MainActivity.tempConti.removeExplanationArrayList();
                        MainActivity.tempConti.removeMusicArrayList();
                        MainActivity.tempConti.removeSheetArrayList();

                        dismiss();
                    }

                    break;
                }

                if(PraiseSearch.searchDB!=null){ // 검색시 바로 리프레쉬 되게
                    //초기화작업
                    MainActivity.tempData.removeTitleArrayList();
                    MainActivity.tempData.removeChordArrayList();
                    MainActivity.tempData.removeDateArrayList();
                    MainActivity.tempData.removeExplanationArrayList();
                    MainActivity.tempData.removeMusicArrayList();
                    MainActivity.tempData.removeSheetArrayList();
                    MainActivity.tempData.removeCheck();

                    latch = new CountDownLatch(1);
                    PraiseSearch.searchDB = new SearchDB(2, getContext(), latch);
                    PraiseSearch.searchDB.execute();
                    adapter.listData.clear();//초기화 하고, 안그러면 밑으로 똑같은 뷰가 계속 붙음

                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    PraiseSearch.searchDB.cancel(true);
                    PraiseSearch.getData();
                }

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(v.getWindowToken(),0);
                dismiss();
            }
        });
    }

}

