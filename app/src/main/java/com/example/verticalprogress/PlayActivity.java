package com.example.verticalprogress;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;

//유퀴즈 러쉬
public class PlayActivity extends AppCompatActivity {

    int Time; //이미지 매핑 범위

    MediaPlayer mp;
    SeekBar seekBar;
    boolean isPlaying = false; //재생중인지 확인할 변수

    SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");

    //txt파일 추출
    private String line = null;
    private String rLine[] = new String[1000];
    int line_count = 0;
    String timeStamp[] = new String[1000]; //타임스탬프 배열


    //자동 스크롤
    ObjectAnimator AutoScroll;
    boolean touchScroll; //ScrollView를 직접 터치했는지의 여부

    //타이머
    String recTime;
    Button timer;
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

    TextView timeText;//프로그레스바 타임

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);


        //음악 뷰
        timeText = (TextView) findViewById(R.id.timeText);
        ImageButton play_button = (ImageButton) findViewById(R.id.play_button);
        ImageButton stop_button = (ImageButton) findViewById(R.id.stop_button);
        ImageButton pause_button = (ImageButton) findViewById(R.id.pause_button);

        //전사
        TextView textView = (TextView) findViewById(R.id.textView);
        ImageView figureImage = (ImageView) findViewById(R.id.figureImage);
        TextView keyword = (TextView) findViewById(R.id.keyword);
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);

        //음원
        mp = MediaPlayer.create(PlayActivity.this, R.raw.youquiz_lush_mp);

        //상단바----------
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //back버튼
        //제목은 img번호와 intent구별 위해 밑에서 받음

        //음원시간 표시
        TextView toolbar_time = (TextView) findViewById(R.id.toolbar_time);
        toolbar_time.setText(timeFormat.format(mp.getDuration()));

        //시크바
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setVisibility(ProgressBar.VISIBLE);
        seekBar.setMax(mp.getDuration());

        //타이머
        timer = (Button)findViewById(R.id.timer);


        //txt추출 : TimeStamp, 내용 나눠서 저장
        try {
            BufferedReader bfRead = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.youquiz_lush)));

            // 한줄씩 NULL이 아닐때까지 읽어 rLine 배열에 넣는다
            while ((line = bfRead.readLine()) != null) {
                rLine[line_count] = line;
                line_count++;
            }

            bfRead.close();
        } catch (Exception e) {
            e.printStackTrace();

        }


        //TextView의 TimeStamp클릭 시 이동
        SpannableString spannableString = null;
        for (int i = 3; i < line_count; i += 2) {
            spannableString = new SpannableString(rLine[i]);
            String click_time = rLine[i]; //String형 timeStamp필요

            //클릭 시 할 동작
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    String[] split = click_time.split(":");  //02:10에서 :제거
                    String M = split[0]; //02
                    String S = split[1]; //10

                    int mesc = 0;
                    int m = Integer.parseInt(M); //int로 변경
                    int s = Integer.parseInt(S); //int로 변경
                    m *= 60; //분을 초로 변경
                    mesc = m + s; //분 초 더해서
                    mesc *= 1000; //mesc형태로 변경


                    mp.seekTo(mesc);
                    seekBar.setProgress(mp.getCurrentPosition());
                    timeText.setText(timeFormat.format(mp.getCurrentPosition()));


                }
            };

            spannableString.setSpan(clickableSpan, 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.append(spannableString); //clickableSpan이 적용된 timeStamp
            textView.append("\n" + rLine[i + 1] + "\n\n");
            textView.setMovementMethod(LinkMovementMethod.getInstance());

        }


        play_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MyLog)MyLog.mContext).inputLog(((Pid)Pid.context_pid).info_study+"|"+((Pid)Pid.context_pid).info_pid+"|"
                        +((Pid)Pid.context_pid).info_task + "|"+((Pid)Pid.context_pid).info_condition+"|"+"click_playButton");

                mp.start();

                //노래 진행 시간
                new Thread() {
                    SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");

                    public void run() {
                        seekBar.setMax(mp.getDuration());

                        while (mp.isPlaying()) {
                            //위젯변경위한 Ui쓰레드
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    seekBar.setProgress(mp.getCurrentPosition());
                                    timeText.setText(timeFormat.format(mp.getCurrentPosition()));
                                }
                            });
                            SystemClock.sleep(200); //0.2초마다 진행상태 변경
                        }
                    }
                }.start();

                touchScroll = false;

            }
        });

        //일시정지
        pause_button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                mp.pause();
                seekBar.setProgress(mp.getCurrentPosition());

                ((MyLog)MyLog.mContext).inputLog(((Pid)Pid.context_pid).info_study+"|"+((Pid)Pid.context_pid).info_pid+"|"
                        +((Pid)Pid.context_pid).info_task + "|"+((Pid)Pid.context_pid).info_condition+"|"+"click_pauseButton");


            }
        });



        //멈춤
        stop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MyLog)MyLog.mContext).inputLog(((Pid)Pid.context_pid).info_study+"|"+((Pid)Pid.context_pid).info_pid+"|"
                        +((Pid)Pid.context_pid).info_task +"|"+((Pid)Pid.context_pid).info_condition+"|"+"click_stopButton");

                try {
                    mp.stop();
                    seekBar.setProgress(0);
                    timeText.setText("00:00");
                    mp.prepare();
                } catch (IOException e) {
                }
            }
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mp.seekTo(progress);
                    touchScroll = false; //다시 프로그레스바 만지면 병렬처리 되도록
                }

                //일시정지 후 seekbar이동 시에도 timeStamp에 변화 있도록
                seekBar.setProgress(mp.getCurrentPosition());
                timeText.setText(timeFormat.format(mp.getCurrentPosition()));


                //자동 스크롤 + 이미지 mapping
                String time = timeText.getText().toString();
                String fullText = textView.getText().toString();
                if (fullText.contains(time)) {  //본문에 timeStamp가 있다면
                    //자동스크롤
                    int index1 = fullText.indexOf(time);
                    int index2 = textView.getLayout().getLineForOffset(index1);

                    AutoScroll = ObjectAnimator.ofInt(scrollView, "scrollY", textView.getLayout().getLineTop(index2)).setDuration(700);
                    if (!touchScroll) { //텍스트뷰 안 만졌으면
                        AutoScroll.start(); //자동 스크롤
                    }

                }

                String[] t = timeText.getText().toString().split(":"); //"00:00"
                String t_M = t[0];//"00"
                String t_S = t[1];//"00"
                String timeee = t_M + t_S;//"0000"
                Time = Integer.parseInt(timeee);//0000

                //이미지 매핑
                if(Time>=0 && Time<110) {
                    figureImage.setImageDrawable(BitmapImage(R.drawable.lushshop));
                    keyword.setText("러쉬 매장에서의 경험 간증글이 화제가 되었다.");
                }else if(Time>=110 && Time<150) {
                    figureImage.setImageDrawable(BitmapImage(R.drawable.advice));
                    keyword.setText("머리 감고 가도록 적극적으로 권유한다.");
                }else if(Time>=150 && Time<259) {
                    figureImage.setImageDrawable(BitmapImage(R.drawable.customer));
                    keyword.setText("손님과 공감하고 나누면서 친해진다.");
                }else if(Time>=260 && Time<339) {
                    figureImage.setImageDrawable(BitmapImage(R.drawable.enfpkorean));
                    keyword.setText("enfp(엔프피) 스타일이다.");
                }else if(Time>=343 && Time<438) {
                    figureImage.setImageDrawable(BitmapImage(R.drawable.smoothie));
                    keyword.setText("스무디왕에서 아르바이트를 했는데 전국 매장 컴피티션에서 1등을 했다.");
                }else if(Time>=445 && Time<503) {
                    figureImage.setImageDrawable(BitmapImage(R.drawable.firstprize));
                    keyword.setText(" 전세계 매장 매출 1등을 여러번 했다.");
                }else if(Time>=522 && Time<533) {
                    figureImage.setImageDrawable(BitmapImage(R.drawable.difficulty));
                    keyword.setText("매니저로서 매장을 관리하며 어려움은 있다.");
                }else if(Time>=534 && Time<601) {
                    figureImage.setImageDrawable(BitmapImage(R.drawable.enjoy));
                    keyword.setText("오늘 하루도 즐겁게 일하자는 나만의 슬로건을 가지고 있다.");
                }else if(Time>=602 && Time<632) {
                    figureImage.setImageDrawable(BitmapImage(R.drawable.goodboss));
                    keyword.setText("서비스직으로 감정적으로 힘들 때가 있었지만 다독여주는 좋은 직장 상사가 계셨다.");
                }else if(Time>=704 && Time<749) {
                    figureImage.setImageDrawable(BitmapImage(R.drawable.children));
                    keyword.setText("어린 자녀를 둔 고객이 걱정없이 편하게 쇼핑할수 있도록 아이 놀아주며 케어 한다.");
                }else if(Time>=750 && Time<857) {
                    figureImage.setImageDrawable(BitmapImage(R.drawable.remain_single));
                    keyword.setText("비혼 선언하는 임직원에게 축의금과 유급휴가를 주는 비혼식 제도가 있다.");
                }else if(Time>=859 && Time<915) {
                    figureImage.setImageDrawable(BitmapImage(R.drawable.dog));
                    keyword.setText("반려동물을 키우면 반려동물 수당을 준다.");
                }else if(Time>=922 && Time<1005) {
                    figureImage.setImageDrawable(BitmapImage(R.drawable.occupational_disease));
                    keyword.setText("직업병이 있다.");
                }



            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //progressbar 움직이는 것 Log에 기록
                ((MyLog)MyLog.mContext).inputLog(((Pid)Pid.context_pid).info_study+"|"+((Pid)Pid.context_pid).info_pid+"|"
                        +((Pid)Pid.context_pid).info_task +"|"+((Pid)Pid.context_pid).info_condition+"|"+
                        "onProgressChangedStop"+"|"+timeFormat.format(mp.getCurrentPosition()));

            }
        });



        //터치에 의해 스크롤이 변할 때
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touchScroll = true; //터치가 가해짐
                        ((MyLog)MyLog.mContext).inputLog(((Pid)Pid.context_pid).info_study+"|"+((Pid)Pid.context_pid).info_pid+"|"
                                +((Pid)Pid.context_pid).info_task +"|"+((Pid)Pid.context_pid).info_condition+"|"+
                                "touch_ScrollView");

                        break;

                    case MotionEvent.ACTION_MOVE:
                        break;

                    case MotionEvent.ACTION_UP:
                        break;
                }
                return false;
            }
        });

        //제목, 이미지position 받아오기
        Intent intent = getIntent();
        if(intent.hasExtra("제목")){
            String titleIntent = intent.getStringExtra("제목");
            getSupportActionBar().setTitle(titleIntent);

        }

        timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimer();

                //Play되도록
                ((MyLog)MyLog.mContext).inputLog(((Pid)Pid.context_pid).info_study+"|"+((Pid)Pid.context_pid).info_pid+"|"
                        +((Pid)Pid.context_pid).info_task + "|"+((Pid)Pid.context_pid).info_condition+"|"+"click_playButton");

                mp.start();

                //노래 진행 시간
                new Thread() {
                    SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");

                    public void run() {
                        seekBar.setMax(mp.getDuration());

                        while (mp.isPlaying()) {
                            //위젯변경위한 Ui쓰레드
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    seekBar.setProgress(mp.getCurrentPosition());
                                    timeText.setText(timeFormat.format(mp.getCurrentPosition()));
                                }
                            });
                            SystemClock.sleep(200); //0.2초마다 진행상태 변경
                        }
                    }
                }.start();

                touchScroll = false;
            }
        });

    } //onCreate


    //키보드 키로 Log타이머 + 재생 하기
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                Toast.makeText(this, "볼륨 누름", Toast.LENGTH_SHORT).show();
            } else if (keyCode == KeyEvent.KEYCODE_SPACE) {
                Toast.makeText(this, "space 누름", Toast.LENGTH_SHORT).show();
            }else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                Toast.makeText(this, "KEYCODE_DPAD_DOWN 누름", Toast.LENGTH_SHORT).show();
            }else if (keyCode == KeyEvent.KEYCODE_0) {
                Toast.makeText(this, "KEYCODE_0 누름", Toast.LENGTH_SHORT).show();
                startTimer();
                ((MyLog)MyLog.mContext).inputLog(((Pid)Pid.context_pid).info_study+"|"+((Pid)Pid.context_pid).info_pid+"|"
                        +((Pid)Pid.context_pid).info_task + "|"+((Pid)Pid.context_pid).info_condition+"|"+"click_playButton");

                mp.start();

                //노래 진행 시간
                new Thread() {
                    SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");

                    public void run() {
                        seekBar.setMax(mp.getDuration());

                        while (mp.isPlaying()) {
                            //위젯변경위한 Ui쓰레드
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    seekBar.setProgress(mp.getCurrentPosition());
                                    timeText.setText(timeFormat.format(mp.getCurrentPosition()));
                                }
                            });
                            SystemClock.sleep(200); //0.2초마다 진행상태 변경
                        }
                    }
                }.start();

                touchScroll = false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View focusView = getCurrentFocus();
        if (focusView != null) {
            Rect rect = new Rect();
            focusView.getGlobalVisibleRect(rect);
            int x = (int) ev.getX(), y = (int) ev.getY();
            if (!rect.contains(x, y)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
                focusView.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    //디바이스 자체 back버튼
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //노래 계속 재생돼서 추가
        if(mp.isPlaying()){
            mp.stop();
        }
    }

    //상단바 back버튼
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //노래 계속 재생돼서 추가
        if(mp.isPlaying()){
            mp.stop();
        }
        return super.onOptionsItemSelected(item);
    }


    public BitmapDrawable BitmapImage(int image)
    {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),image);
        Bitmap resize = Bitmap.createScaledBitmap(bitmap,200,150,true);


        BitmapDrawable drawable = new BitmapDrawable(getResources(),resize);

        return drawable;
    }



    //타이머
    private void startTimer(){

        switch (status){
            case INIT:
                //어플리케이션이 실행되고 나서 실제로 경과된 시간...
                baseTime = SystemClock.elapsedRealtime();

                //핸들러 실행
                handler.sendEmptyMessage(0);
                timer.setText("멈춤");

                //상태 변환
                status = RUN;
                ((MyLog)MyLog.mContext).inputLog(((Pid)Pid.context_pid).info_study+"|"+((Pid)Pid.context_pid).info_pid+"|"
                        +((Pid)Pid.context_pid).info_task +"|"+((Pid)Pid.context_pid).info_condition+"|"+"click_timerButton_Start");

                break;
            case RUN:
                //핸들러 정지
                handler.removeMessages(0);

                //정지 시간 체크
                pauseTime = SystemClock.elapsedRealtime();
                timer.setText("시작");
                //상태변환
                status = PAUSE;

                //기록
                Log.i("test","타이머 == "+getTime());
                ((MyLog)MyLog.mContext).inputLog(((Pid)Pid.context_pid).info_study+"|"+((Pid)Pid.context_pid).info_pid+"|"
                        +((Pid)Pid.context_pid).info_task +"|"+((Pid)Pid.context_pid).info_condition+"|"+"click_timerButton_Stop"+"|"+getTime());


                baseTime = 0;
                pauseTime = 0;
                status = INIT;
                break;
            case PAUSE:
                long reStart = SystemClock.elapsedRealtime();
                baseTime += (reStart - pauseTime);

                handler.sendEmptyMessage(0);

                timer.setText("멈춤");

                status = RUN;

        }

    }

    //경과된 시간 체크
    private String getTime(){
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

