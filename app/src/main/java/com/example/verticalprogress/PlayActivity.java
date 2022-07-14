package com.example.verticalprogress;

import android.animation.ObjectAnimator;
import android.content.Intent;
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


    //슬라이딩패널
    int imgNum;

    //검색 - 다음, 이전
    int[] start = new int[100]; //탐색 시작 위치 저장
    int s=1; //탐색위치 저장할 start[]의 인덱스
    int store[] = new int[100]; //이전 탐색 위치 저장
    int i;

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


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        //음악 뷰
        TextView timeText = (TextView) findViewById(R.id.timeText);
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
        //날짜 표시
        TextView toolbar_date = (TextView) findViewById(R.id.toolbar_date);
        toolbar_date.setText("Oct. 13. 2021");
        //----------------


        //시크바
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setVisibility(ProgressBar.VISIBLE);
        seekBar.setMax(mp.getDuration());

//        //검색
//        EditText editText = (EditText) findViewById(R.id.editText);
//        Button next = (Button) findViewById(R.id.next);
//        Button previous = (Button) findViewById(R.id.previous);


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


//        start[0] = 0; //처음엔 인덱스 0부터 검사
//        //검색
//        editText.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View view, int keycode, KeyEvent keyEvent) {
//                //Enter Key 누르면
//                if ((keyEvent.getAction() == keyEvent.ACTION_DOWN) && (keycode == KeyEvent.KEYCODE_ENTER)) {
//                    //키패드 숨기기
//                    InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//                    manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//
//
//                    String inputText = editText.getText().toString();
//                    String fullText = textView.getText().toString();
//                    String highlighted = "<font color='red'>" + inputText + "</font>";
//
//                    //인덱스
//                    final int[] index1 = {fullText.indexOf(inputText)};
//                    final int[] index2 = {textView.getLayout().getLineForOffset(index1[0])};
//
//
//                    if (inputText.replace(" ", "").equals("")) { //input이 없으면 HTML적용 안 함.
//                        Log.i("test", "빈 텍스트");
//                        editText.setText(null);
//
//                        Toast.makeText(getApplicationContext(), "검색어가 없습니다.", Toast.LENGTH_SHORT).show();
//
//                        SpannableString spannableString = null; //timestamp 색 변경
//                        for (int i = 3; i < line_count; i += 2) {
//                            spannableString = new SpannableString(rLine[i]);
//                            String click_time = rLine[i]; //String형 timeStamp필요
//
//                            //클릭 시 할 동작
//                            ClickableSpan clickableSpan = new ClickableSpan() {
//                                @Override
//                                public void onClick(@NonNull View view) {
//                                    String[] split = click_time.split(":");  //02:10에서 :제거
//                                    String M = split[0]; //02
//                                    String S = split[1]; //10
//
//                                    int mesc = 0;
//                                    int m = Integer.parseInt(M); //int로 변경
//                                    int s = Integer.parseInt(S); //int로 변경
//                                    m *= 60; //분을 초로 변경
//                                    mesc = m + s; //분 초 더해서
//                                    mesc *= 1000; //mesc형태로 변경
//
//
//                                    mp.seekTo(mesc);
//                                    seekBar.setProgress(mp.getCurrentPosition());
//                                    timeText.setText(timeFormat.format(mp.getCurrentPosition()));
//
//
//                                }
//                            };
//
//                            spannableString.setSpan(clickableSpan, 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                            textView.append(spannableString); //clickableSpan이 적용된 timeStamp
//                            textView.append("\n" + rLine[i + 1] + "\n\n");
//                            textView.setMovementMethod(LinkMovementMethod.getInstance());
//
//                        }
//
//                    } else if (fullText.contains(inputText)) { //찾는 단어 있으면
//                        editText.setText(null);
//
//                        //처음 검색단어로 이동
//                        scrollView.scrollTo(0, textView.getLayout().getLineTop(index2[0]));
//
//                        store[0] = index1[0]; //1번째 키워드의 인덱스 저장
//
//                        //다음 검색단어로 이동
//                        next.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                start[s++] = index1[0] + inputText.length(); //새 탐색 시작 위치 저장([1]=첫번째 단어 제외하고 부터 )
//
//                                store[i++] = index1[0];
//
//                                index1[0] = fullText.indexOf(inputText, start[s - 1]);
//                                index2[0] = textView.getLayout().getLineForOffset(index1[0]);
//                                scrollView.scrollTo(0, textView.getLayout().getLineTop(index2[0]));
//
//                                if (index1[0] < 0) { //마지막 단어 이후
//                                    index1[0] = fullText.indexOf(inputText, start[s - 2]);
//                                    index2[0] = textView.getLayout().getLineForOffset(index1[0]);
//                                    scrollView.scrollTo(0, textView.getLayout().getLineTop(index2[0]));
//                                    Toast.makeText(getApplicationContext(), "마지막 단어 입니다.", Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        });
//
//                        previous.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                try {
//                                    start[s++] = store[--i] + inputText.length(); //새 탐색 시작 위치 저장(1부터 : [0]=전체, [1]=첫번째 단어 제외하고 부터 )
//
//                                    index1[0] = fullText.indexOf(inputText, store[i]);
//                                    index2[0] = textView.getLayout().getLineForOffset(index1[0]);
//
//                                    scrollView.scrollTo(0, textView.getLayout().getLineTop(index2[0]));
//
//
//                                } catch (IndexOutOfBoundsException e) {
//                                    //인덱스 벗어나면 = 더 이상 이전 단어가 없으면
//                                    Toast.makeText(getApplicationContext(), "첫번째 단어입니다.", Toast.LENGTH_SHORT).show();
//                                    i = 1; //배열 다시 세팅
//                                }
//                            }
//                        });
//
//
//                        //highlight
//                        for (int i = 0; i < line_count; i++) {
//                            rLine[i] = rLine[i].replace(inputText, highlighted);
//
//                            textView.setText(""); //리셋
//                        }
//
//
//                        //하이라트된 text 뷰에 붙이기
//                        for (int n = 3; n < line_count; n += 2) {//HTML 따로 적용해야지 red로 바뀜
//                            //=======================================================
//                            SpannableString SearchSpannable = new SpannableString(rLine[n]);
//                            String click_time = rLine[n];
//                            ClickableSpan clickableSpan = new ClickableSpan() {
//                                @Override
//                                public void onClick(@NonNull View view) {
//                                    String[] split = click_time.split(":");  //02:10에서 :제거
//                                    String M = split[0]; //02
//                                    String S = split[1]; //10
//
//                                    int mesc = 0;
//                                    int m = Integer.parseInt(M); //int로 변경
//                                    int s = Integer.parseInt(S); //int로 변경
//                                    m *= 60; //분을 초로 변경
//                                    mesc = m + s; //분 초 더해서
//                                    mesc *= 1000; //mesc형태로 변경
//
//
//                                    mp.seekTo(mesc);
//                                    seekBar.setProgress(mp.getCurrentPosition());
//                                    timeText.setText(timeFormat.format(mp.getCurrentPosition()));
//
//
//                                }
//                            };
//                            SearchSpannable.setSpan(clickableSpan, 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                            textView.append(SearchSpannable);
////                        textView.append(Html.fromHtml(rLine[n])); //타임스탬프
//                            textView.append("\n");
//                            textView.append(Html.fromHtml(rLine[n + 1])); //내용
//                            textView.append("\n\n");
//                        }
//
//                        //다시 돌려놓기 : 그래야 다음 검색 시 전에 검색한 highlight 제거 됨
//                        for (int i = 0; i < line_count; i++) {
//                            rLine[i] = rLine[i].replace(highlighted, inputText);
//                        }
//
//
//                    } else { //찾는 단어 본문에 없
//                        editText.setText(null);
//
//                        Toast.makeText(getApplicationContext(), inputText + "없음", Toast.LENGTH_SHORT).show();
//
//                        for (int i = 0; i < line_count; i++) {
//                            rLine[i] = rLine[i].replace(highlighted, inputText); //하이라이트 지우기
//                            textView.setText(""); //리셋
//                        }
//
//                        //새 text 뷰에 붙이기
//                        for (int n = 3; n < line_count; n += 2) {//HTML 따로 적용해야지 red로 바뀜
//                            //=======================================================
//                            SpannableString SearchSpannable = new SpannableString(rLine[n]);
//                            String click_time = rLine[n];
//                            ClickableSpan clickableSpan = new ClickableSpan() {
//                                @Override
//                                public void onClick(@NonNull View view) {
//                                    String[] split = click_time.split(":");  //02:10에서 :제거
//                                    String M = split[0]; //02
//                                    String S = split[1]; //10
//
//                                    int mesc = 0;
//                                    int m = Integer.parseInt(M); //int로 변경
//                                    int s = Integer.parseInt(S); //int로 변경
//                                    m *= 60; //분을 초로 변경
//                                    mesc = m + s; //분 초 더해서
//                                    mesc *= 1000; //mesc형태로 변경
//
//
//                                    mp.seekTo(mesc);
//                                    seekBar.setProgress(mp.getCurrentPosition());
//                                    timeText.setText(timeFormat.format(mp.getCurrentPosition()));
//
//                                }
//                            };
//                            SearchSpannable.setSpan(clickableSpan, 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                            textView.append(SearchSpannable);
////                        textView.append(Html.fromHtml(rLine[n])); //타임스탬프
//                            textView.append("\n");
//                            textView.append(Html.fromHtml(rLine[n + 1])); //내용
//                            textView.append("\n\n");
//
//                        }
//                    }
//                }
//
//                return false;
//            }
//        });


        //재생
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

//                    figureImage.setImageResource(R.drawable.advice);
                    keyword.setText("머리 감고 가도록 적극적으로 권유한다.");
                }else if(Time>=150 && Time<259) {
                    figureImage.setImageDrawable(BitmapImage(R.drawable.customer));

//                    figureImage.setImageResource(R.drawable.customer);
                    keyword.setText("손님과 공감하고 나누면서 친해진다.");
                }else if(Time>=260 && Time<339) {
                    figureImage.setImageDrawable(BitmapImage(R.drawable.enfpkorean));
//                    figureImage.setImageResource(R.drawable.enfpkorean);
                    keyword.setText("enfp(엔프피) 스타일이다.");
                }else if(Time>=343 && Time<438) {
                    figureImage.setImageDrawable(BitmapImage(R.drawable.smoothie));
//                    figureImage.setImageResource(R.drawable.smoothie);
                    keyword.setText("스무디왕에서 아르바이트를 했는데 전국 매장 컴피티션에서 1등을 했다.");
                }else if(Time>=445 && Time<503) {
                    figureImage.setImageDrawable(BitmapImage(R.drawable.firstprize));
//                    figureImage.setImageResource(R.drawable.firstprize);
                    keyword.setText(" 전세계 매장 매출 1등을 여러번 했다.");
                }else if(Time>=522 && Time<533) {
                    figureImage.setImageDrawable(BitmapImage(R.drawable.difficulty));
//                    figureImage.setImageResource(R.drawable.difficulty);
                    keyword.setText("매니저로서 매장을 관리하며 어려움은 있다.");
                }else if(Time>=534 && Time<601) {
                    figureImage.setImageDrawable(BitmapImage(R.drawable.enjoy));
//                    figureImage.setImageResource(R.drawable.enjoy);
                    keyword.setText("오늘 하루도 즐겁게 일하자는 나만의 슬로건을 가지고 있다.");
                }else if(Time>=602 && Time<632) {
                    figureImage.setImageDrawable(BitmapImage(R.drawable.goodboss));
//                    figureImage.setImageResource(R.drawable.goodboss);
                    keyword.setText("서비스직으로 감정적으로 힘들 때가 있었지만 다독여주는 좋은 직장 상사가 계셨다.");
                }else if(Time>=704 && Time<749) {
                    figureImage.setImageDrawable(BitmapImage(R.drawable.children));
//                    figureImage.setImageResource(R.drawable.children);
                    keyword.setText("어린 자녀를 둔 고객이 걱정없이 편하게 쇼핑할수 있도록 아이 놀아주며 케어 한다.");
                }else if(Time>=750 && Time<857) {
                    figureImage.setImageDrawable(BitmapImage(R.drawable.remain_single));
//                    figureImage.setImageResource(R.drawable.remain_single);
                    keyword.setText("비혼 선언하는 임직원에게 축의금과 유급휴가를 주는 비혼식 제도가 있다.");
                }else if(Time>=859 && Time<915) {
                    figureImage.setImageDrawable(BitmapImage(R.drawable.dog));
//                    figureImage.setImageResource(R.drawable.dog);
                    keyword.setText("반려동물을 키우면 반려동물 수당을 준다.");
                }else if(Time>=922 && Time<1005) {
                    figureImage.setImageDrawable(BitmapImage(R.drawable.occupational_disease));
//                    figureImage.setImageResource(R.drawable.occupational_disease);
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



//        //슬라이딩패널----------------
//        GridView gridList = (GridView)findViewById(R.id.gridList);
//        ImgKeyListAdapter imgAdapter = new ImgKeyListAdapter();
//        gridList.setAdapter(imgAdapter);
//
//        //슬라이딩 패널에 이미지 추가
//        imgAdapter.addItem(R.drawable.guest,"guest tonight");
//        imgAdapter.addItem(R.drawable.songwriter,"songwriter youngest");
//        imgAdapter.addItem(R.drawable.artist,"artist win grammy");
//        imgAdapter.addItem(R.drawable.happier,"happier world tour");
//        imgAdapter.addItem(R.drawable.welcome,"welcome billie eilish");
//
//
//
//
//        //제목, 이미지position 받아오기
//        Intent intent = getIntent();
//        if(intent.hasExtra("제목")){
//            String titleIntent = intent.getStringExtra("제목");
//            getSupportActionBar().setTitle(titleIntent);
//
//        }else if(intent.hasExtra("이미지번호")) {
//            //클릭한 이미지 번호 받아오기
//            imgNum = intent.getIntExtra("이미지번호", 0);
//
//        }

        //각 이미지 클릭 시
//        switch (imgNum){
//            case 0:
//                mp.seekTo(0);
//                timeText.setText(timeFormat.format(mp.getCurrentPosition()));
//                break;
//            case 1:  //00:05
//                mp.seekTo(5000);
//                timeText.setText(timeFormat.format(mp.getCurrentPosition()));
//                break;
//            case 2: //01:44
//                mp.seekTo(104000);
//                timeText.setText(timeFormat.format(mp.getCurrentPosition()));
//                break;
//            case 3:
//                figureImage.setImageResource(R.drawable.figure_image4);
//                break;
//            case 4:
//                figureImage.setImageResource(R.drawable.figure_image5);
//                break;
//            case 5:
//                figureImage.setImageResource(R.drawable.figure_image6);
//                break;
//            case 6:
//                figureImage.setImageResource(R.drawable.figure_image1);
//                break;
//
//        }




    }



    //다른 곳 클릭시, search 후 키보드 내리기
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







}

