package com.example.verticalprogress;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private Menu menu;
    String recTime;

    private ArrayList<VoiceList> arrayList;
    private MainAdapter mainAdapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;

    //상태를 표시하는 '상수' 지정
    //- 각각의 숫자는 독립적인 개별 '상태' 의미
    public static final int INIT = 0;//처음
    public static final int RUN = 1;//실행중
    public static final int PAUSE = 2;//정지

    //상태값을 저장하는 변수
    //- INIT은 초기값임, 그걸 status 안에 넣는다.(0을 넣은거다)
    public static int status = INIT;

    //타이머 시간 값을 저장할 변수
    private long baseTime,pauseTime;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //리사이클러뷰에 LinearLayoutManager객체 지정
        recyclerView = (RecyclerView)findViewById(R.id.rv);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        arrayList = new ArrayList<>();
        arrayList.add(new VoiceList("recording1",R.drawable.lush_wordcloud));
        arrayList.add(new VoiceList("recording2",R.drawable.lush_wordcloud));
        arrayList.add(new VoiceList("recording3",R.drawable.lush_wordcloud));

        mainAdapter = new MainAdapter(arrayList);


        //item별 클릭 처리
        mainAdapter.setOnItemClickListener(new MainAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                if (pos == 0) {
                    final VoiceList item = arrayList.get(pos);

                    Intent intent = new Intent(getApplicationContext(),PlayActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("제목",item.getTitle()); //title값 보냄
                    getApplicationContext().startActivity(intent);

                    ((MyLog)MyLog.mContext).inputLog(((Pid)Pid.context_pid).info_study+"|"+((Pid)Pid.context_pid).info_pid+"|"
                            +((Pid)Pid.context_pid).info_task +"|"+((Pid)Pid.context_pid).info_condition+"|"+"click_title1");
                } else if (pos == 1) {
                    final VoiceList item = arrayList.get(pos);

                    Intent intent = new Intent(getApplicationContext(),PlayActivity2.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("제목",item.getTitle()); //title값 보냄
                    getApplicationContext().startActivity(intent);

                    ((MyLog)MyLog.mContext).inputLog(((Pid)Pid.context_pid).info_study+"|"+((Pid)Pid.context_pid).info_pid+"|"
                            +((Pid)Pid.context_pid).info_task +"|"+((Pid)Pid.context_pid).info_condition+"|"+"click_title2");
                }  else if (pos == 2) {
                    final VoiceList item = arrayList.get(pos);

                    Intent intent = new Intent(getApplicationContext(),PlayActivity3.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("제목",item.getTitle()); //title값 보냄
                    getApplicationContext().startActivity(intent);

                    ((MyLog)MyLog.mContext).inputLog(((Pid)Pid.context_pid).info_study+"|"+((Pid)Pid.context_pid).info_pid+"|"
                            +((Pid)Pid.context_pid).info_task + "|"+((Pid)Pid.context_pid).info_condition+"|"+"click_title3");
                }
            }
        });

        recyclerView.setAdapter(mainAdapter);

    }


    //상단바 메뉴
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;

        return true;
    }

    //'설정' 누를 시
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_settings:
                Intent settingIntent = new Intent(getApplicationContext(),Pid.class);
                startActivity(settingIntent);
                break;
            case R.id.menu_timer:
                staButton();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    private void staButton(){
        MenuItem menu_timer = menu.findItem(R.id.menu_timer);

        switch (status){
            case INIT:
                //어플리케이션이 실행되고 나서 실제로 경과된 시간...
                baseTime = SystemClock.elapsedRealtime();

                //핸들러 실행
                handler.sendEmptyMessage(0);
                menu_timer.setTitle("멈춤");

                //상태 변환
                status = RUN;
                break;
            case RUN:
                //핸들러 정지
                handler.removeMessages(0);

                //정지 시간 체크
                pauseTime = SystemClock.elapsedRealtime();
                menu_timer.setTitle("시작");
                //상태변환
                status = PAUSE;

                //기록
                Log.i("test","타이머 == "+getTime());
                ((MyLog)MyLog.mContext).inputLog(getTime());

                baseTime = 0;
                pauseTime = 0;
                status = INIT;
                break;
            case PAUSE:
                long reStart = SystemClock.elapsedRealtime();
                baseTime += (reStart - pauseTime);

                handler.sendEmptyMessage(0);

                menu_timer.setTitle("멈춤");

                status = RUN;

        }

    }

    private String getTime(){
        //경과된 시간 체크

        long nowTime = SystemClock.elapsedRealtime();
        //시스템이 부팅된 이후의 시간?
        long overTime = nowTime - baseTime;

        long m = overTime/1000/60;
        long s = (overTime/1000)%60;
        long ms = overTime % 1000;

        recTime = String.format("%02d:%02d:%02d",m,s,ms);

        return recTime;
    }

    Handler handler = new Handler(){

        @Override
        public void handleMessage(@NonNull Message msg) {
            handler.sendEmptyMessage(0);
        }
    };

}
