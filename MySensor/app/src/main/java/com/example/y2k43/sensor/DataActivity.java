package com.example.y2k43.sensor;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.DecimalFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.login.LoginException;

@RequiresApi(api = Build.VERSION_CODES.N)
public class DataActivity extends AppCompatActivity implements SensorEventListener{

    TextView textView;
    TextView textView2;
    TextView textX;
    TextView textY;
    TextView textZ;
    TextView textTimer;
    TextView txtGyro1, txtGyro2, txtGyro3, txtPitch, txtRoll, txtYaw, txtDt;
    TextView textNonG_x, textNonG_y, textNonG_z;

    TextView step, speedTxt, txtTHRESHOLD, txtTimerPeriod, txtTimerPeriodSec;
    EditText editText, editText2;


    SensorManager manager;
   // List<Sensor> sensors;

    Sensor sensor_ACCELEROMETER;
    Sensor sensor_GYROSCOPE;

    double[] m_gravity_data = new double[3];
    double[] m_accel_data = new double[3];

    int sensorIndex = 0;
    String sensorName = "";

    //세이브 타입 여부 확인
    int saveCheckSum = 0;

    //파일에 저장될 내용들에 대한 String 변수
    String txt1, txt2, txt3, info, text;
    //double res = 0;

     //시간 초
//    double timer_sec = 0;
//    double timer_sec_real = 0;
    BigDecimal timer_sec = new BigDecimal(0);
    BigDecimal timer_sec_real = new BigDecimal(0);

    //스레드 실행 간격 조절
    int timerPeriod = 1000;

    //클래스 이름, 파일 이름 정의
    private static final String TAG = MainActivity.class.getName();
    private static final String FILENAME = "test.txt";

    //타이머테스크 정의, 스레드 정의, 타이머 정의
    private TimerTask second;
    private final Handler handler = new Handler();
    private Timer timer;

    //센서 리스너에서 사용되는 변수들 (가속도 센서)
    double valueA, valueB, valueC;
    double lastValueA, lastValueB, lastValueC;

    //자이로 센서
    double valueD, valueE, valueF;
    //double lastValueD, lastValueE, lastValueF;

    //**** 테스트용
    int count = 0;
    private long lastTime;
    private static int SHAKE_THRESHOLD = 800; //흔들림 임계값 정의
    private double speed;
    String msg;

    DecimalFormat decimalFormat;

    /////////////////////////////////////////////////////////////
    //Roll and Pitch
    private double pitch;
    private double roll;
    private double yaw;

    //timestamp and dt
    private double timestamp;
    private double dt;

    // for radian -> dgree
    private double RAD2DGR = 180 / Math.PI;
    private static final float NS2S = 1.0f/1000000000.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        textView = (TextView) findViewById(R.id.textView);
        textView2 = (TextView) findViewById(R.id.textView2);
        textX = (TextView) findViewById(R.id.textX);
        textY = (TextView) findViewById(R.id.textY);
        textZ = (TextView) findViewById(R.id.textZ);
        textTimer = (TextView) findViewById(R.id.textTimer);
        txtGyro1 = (TextView) findViewById(R.id.txtGyro1);
        txtGyro2 = (TextView) findViewById(R.id.txtGyro2);
        txtGyro3 = (TextView) findViewById(R.id.txtGyro3);

        textNonG_x = (TextView) findViewById(R.id.textNonG_x);
        textNonG_y = (TextView) findViewById(R.id.textNonG_y);
        textNonG_z = (TextView) findViewById(R.id.textNonG_z);



        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countStart();
               // Toast.makeText(getApplicationContext(), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() , Toast.LENGTH_SHORT).show();
            }
        });

        Button buttonSave = (Button) findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProcess();
               // Toast.makeText(getApplicationContext(), getExternalFilesDir(null).toString(), Toast.LENGTH_LONG).show();
            }
        });

        step =  (TextView) findViewById(R.id.step);
        speedTxt = (TextView)  findViewById(R.id.speedTxt);
        txtTHRESHOLD = (TextView)  findViewById(R.id.txtTHRESHOLD);
        txtTimerPeriod =  (TextView) findViewById(R.id.txtTimerPeriod);
        txtTimerPeriodSec = (TextView)  findViewById(R.id.txtTimerPeriodSec);

        editText = (EditText) findViewById((R.id.editText));
        editText2 = (EditText) findViewById((R.id.editText2));

        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
       // sensors = manager.getSensorList(Sensor.TYPE_ALL);
        sensor_ACCELEROMETER = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensor_GYROSCOPE = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


        Intent passedIntent = getIntent();
        prosassCommand(passedIntent);

        decimalFormat = new DecimalFormat();
        decimalFormat.applyLocalizedPattern("0.##");

//        decimalFormat.format(timer_sec_real);
//        timer_sec_real = timerPeriod / 1000;
        timer_sec_real = BigDecimal.valueOf((double) timerPeriod/1000);

        Log.i("timer_sec_real : ", timer_sec_real+"");

        txtTHRESHOLD.setText("임계치 : " + Integer.toString(SHAKE_THRESHOLD));
        txtTimerPeriod.setText(Integer.toString(timerPeriod));
        txtTimerPeriodSec.setText(timer_sec_real.toString());


        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onApplyButtonClicked();
            }
        });

        Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onApplyButtonClicked2();
            }
        });


        txtPitch = (TextView) findViewById(R.id.txtPitch);
        txtRoll = (TextView) findViewById(R.id.txtRoll);
        txtYaw = (TextView) findViewById(R.id.txtYaw);
        txtDt = (TextView) findViewById(R.id.txtDt);

    }

    public void prosassCommand(Intent intent){
        if(intent != null) {
            sensorIndex = intent.getIntExtra("SensorIndex", 0);
            sensorName = intent.getStringExtra("SensorName");
            textView.setText(sensorName);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        manager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

       // manager.registerListener(this, sensors.get(sensorIndex), SensorManager.SENSOR_DELAY_UI);

        manager.registerListener(this, sensor_ACCELEROMETER, SensorManager.SENSOR_DELAY_GAME);
        manager.registerListener(this, sensor_GYROSCOPE, SensorManager.SENSOR_DELAY_GAME);
    }

    public void testStart() { //타이머 시작(thread)

//        text = "Ax" + "\t" + "nonG_Ax" + "\t" + "Ay" + "\t" + "nonG_Ay" + "\t" + "Az" + "\t" +
//                "nonG_Az" + "\t" + "t" + "\t" + "speed" + "\t" + "msg" + "\t" + "Gx" + "\t" +
//                "Gy" + "\t" + "Gz" + "\t" + "Pitch" + "\t" + "Roll" + "\t" + "Yaw" ;

        String text1 = "Ax" + "\t" + "nonG_Ax" + "\t" + "Ay" + "\t" + "nonG_Ay" + "\t" + "Az" + "\t" +
                "nonG_Az" + "\t" + "t" + "\t" + "speed" + "\t" + "msg" + "\t" + "Gx" + "\t" +
                "Gy" + "\t" + "Gz" + "\t" + "Pitch" + "\t" + "Roll" + "\t" + "Yaw" ;

        appendLog(text1);

        second = new TimerTask() {
            @Override
            public void run() {
                Log.i("타이머", "timer start");

                Update();

//                text = text + "\n" + info;
//                writeToFile(text);
//                appendLog(info);
                appendLog(info);

                // 좀 손봐야 할 필요가 있는 부분
                // 얘를 내버려두변 계속 메모리 잡아먹지 않을까 싶은데..

                timer_sec = timer_sec.add(timer_sec_real);

//                Log.i("timer_sec : ", timer_sec+"");
                Log.i("timer_sec : ", timer_sec + ", " + timer_sec_real);

                if(saveCheckSum == 0) {
                    timer.cancel();
                }
            }
        };
        timer = new Timer();
        timer.schedule(second, 0, timerPeriod);
    }

    protected void Update() { //testStart가 호출될때마다 해당 메소드를 실행시켜 timer_sec(초)를 갱신한 다음 TextView에 뿌려줌
        Runnable updater = new Runnable() {
            public void run() {
                textTimer.setText(timer_sec.toString());
            }
        };
        if(saveCheckSum == 2) {
            handler.post(updater);
        } else if(saveCheckSum == 0) {
            handler.removeCallbacks(updater);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        final float alpha = (float)0.8;

        if (sensorEvent.sensor == sensor_ACCELEROMETER) {
            m_gravity_data[0] = alpha * m_gravity_data[0] + (1 - alpha) * sensorEvent.values[0];
            m_gravity_data[1] = alpha * m_gravity_data[1] + (1 - alpha) * sensorEvent.values[1];
            m_gravity_data[2] = alpha * m_gravity_data[2] + (1 - alpha) * sensorEvent.values[2];

            //중력가속도를 제외한 가속도 값
            m_accel_data[0] = sensorEvent.values[0] - m_gravity_data[0];
            m_accel_data[1] = sensorEvent.values[1] - m_gravity_data[1];
            m_accel_data[2] = sensorEvent.values[2] - m_gravity_data[2];

            valueA = sensorEvent.values[0];
            valueB = sensorEvent.values[1];
            valueC = sensorEvent.values[2];

        }else if(sensorEvent.sensor == sensor_GYROSCOPE){

            valueD = sensorEvent.values[0];
            valueE = sensorEvent.values[1];
            valueF = sensorEvent.values[2];


        }

        dt = (sensorEvent.timestamp - timestamp) * NS2S;
        timestamp = sensorEvent.timestamp;

        if (dt - timestamp*NS2S != 0) {

                /* 각속도 성분을 적분 -> 회전각(pitch, roll)으로 변환.
                 * 여기까지의 pitch, roll의 단위는 '라디안'이다.
                 * SO 아래 로그 출력부분에서 멤버변수 'RAD2DGR'를 곱해주어 degree로 변환해줌.  */

            pitch = pitch + valueE * dt;
            roll = roll + valueD * dt;
            yaw = yaw + valueF * dt;

        }


        String timestamp = "Sensor Timestamp -> " + sensorEvent.timestamp;
        textView2.setText(timestamp);

        txtGyro1.setText("X : " + decimalFormat.format(valueD));
        txtGyro2.setText("Y : " + decimalFormat.format(valueE));
        txtGyro3.setText("Z : " + decimalFormat.format(valueF));

        txtPitch.setText("Pitch : " + decimalFormat.format(pitch));
        txtRoll.setText("Roll : " + decimalFormat.format(roll));
        txtYaw.setText("Yaw : " + decimalFormat.format(yaw));
        txtDt.setText("Dt : " + dt);

        String strgX, strgY, strgZ, strpitch, strroll, stryaw;

        strgX = decimalFormat.format(valueD);
        strgY = decimalFormat.format(valueE);
        strgZ = decimalFormat.format(valueF);
        strpitch = decimalFormat.format(pitch);
        strroll = decimalFormat.format(roll);
        stryaw = decimalFormat.format(yaw);

        //String sensorValue = "X축 : " + String.format("%.4f", sensorEvent.values[0]);
        //String sensorValue2 = "Y축 : " + String.format("%.4f", sensorEvent.values[1]);
        //String sensorValue3 = "Z축 : " + String.format("%.4f", sensorEvent.values[2]);

        textX.setText("X : " + decimalFormat.format(m_gravity_data[0]) );
        txt1 = decimalFormat.format(m_gravity_data[0]);
        textY.setText("Y : " + decimalFormat.format(m_gravity_data[1]));
        txt2 = decimalFormat.format(m_gravity_data[1]);
        textZ.setText("Z : " + decimalFormat.format(m_gravity_data[2]));
        txt3 = decimalFormat.format(m_gravity_data[2]);


        textNonG_x.setText("nonG_x : " + decimalFormat.format(m_accel_data[0]));
        textNonG_y.setText("nonG_y : " + decimalFormat.format(m_accel_data[1]));
        textNonG_z.setText("nonG_z : " + decimalFormat.format(m_accel_data[2]));


        String strNonG_x, strNonG_y, strNonG_z;
        strNonG_x = decimalFormat.format(m_accel_data[0]);
        strNonG_y = decimalFormat.format(m_accel_data[1]);
        strNonG_z = decimalFormat.format(m_accel_data[2]);

        long currentTime = System.currentTimeMillis(); // 0.1초 간격으로 업데이트
        long gabOfTime = (currentTime - lastTime);

        if(gabOfTime > 100) { // 걸음 수의 시간 간격
            lastTime = currentTime;
            speed = Math.abs( m_accel_data[0] +  m_accel_data[1] +  m_accel_data[2] - lastValueA - lastValueB - lastValueC) / gabOfTime * 10000;

            if(speed > SHAKE_THRESHOLD && saveCheckSum == 1) {
                count++;
                msg = count / 2 + "";
                step.setText(msg);
            } else if(speed > SHAKE_THRESHOLD && saveCheckSum == 2) {
                count++;
                msg = count / 2 + "";
                step.setText(msg);
            }
            lastValueA =  m_accel_data[0];
            lastValueB =  m_accel_data[1];
            lastValueC =  m_accel_data[2];
        }
        double exSpeed = speed;
        speedTxt.setText("가속도 : " + decimalFormat.format(exSpeed));


        if(timer_sec.compareTo(BigDecimal.valueOf(0)) != 0) {

            info = txt1 + "\t" + strNonG_x + "\t" + txt2 + "\t" + strNonG_y + "\t" + txt3 + "\t" + strNonG_x + "\t" +
                    timer_sec + "\t" + decimalFormat.format(speed) + "\t" + msg + "\t" + strgX + "\t" + strgY + "\t" +
                    strgZ + "\t" + strpitch + "\t" + strroll + "\t" + stryaw;
        }


        if(saveCheckSum == 1) {

            info = txt1 + "\t" + txt2 + "\t" + txt3 + "\t" + decimalFormat.format(speed) + "\t" + msg;
            text = text + "\n" + info;
            appendLog(text);

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void countStart(){
        if (saveCheckSum == 0) {
            if(!checkToFile()) {
                count = 0;
                saveCheckSum = 1;
                Toast.makeText(getApplicationContext(), "카운트 시작" , Toast.LENGTH_LONG).show();
            } else {
                deleteToFile();
                Toast.makeText(getApplicationContext(), "이전 파일이 존재합니다. 이전 파일을 삭제 후 자동으로 파일을 저장합니다.", Toast.LENGTH_LONG).show();
                countStart();
            }
        } else {
            saveCheckSum = 0;
            Toast.makeText(getApplicationContext(), "저장 종료", Toast.LENGTH_LONG).show();
        }
        }

    public void saveProcess() {

        if (saveCheckSum == 0) { //최초 실행
            if(!checkToFile()) { // 파일 유무 체크, 이때는 파일이 없을 경우임
                timer_sec = BigDecimal.valueOf(0); // 타이머 초기화
//                timer_sec_real = BigDecimal.valueOf(0);
                count = 0; // 걸음수 초기화
                saveCheckSum = 2; // 실행 상태 변경
                testStart(); // 스레드 시작

            } else { // 파일이 있을 경우
                deleteToFile(); // 파일을 삭제한다
                Toast.makeText(getApplicationContext(), "이전 파일이 존재합니다. 이전 파일을 삭제 후 자동으로 파일을 저장합니다.", Toast.LENGTH_LONG).show();
                saveProcess();

            }
            Toast.makeText(getApplicationContext(), "저장 시작 (" + getExternalFilesDir(null).toString() + ")", Toast.LENGTH_LONG).show();
        } else {
            saveCheckSum = 0;
            timer_sec = BigDecimal.valueOf(0);
            Update();
            Toast.makeText(getApplicationContext(), "저장 종료", Toast.LENGTH_LONG).show();
        }
    }


//    public void writeToFile(String data) { //파일을 만들고, 저장한다. 저장 위치는 data
//        try {
//            File file = new File(getExternalFilesDir(null), FILENAME);
//            Log.e("1", "File Check: " + file.exists());
//            FileOutputStream os = new FileOutputStream(file);
//            byte[] content = data.getBytes();
//
//            if(!file.exists()){
//                //디렉토리 확인후 존재하지 않으면 생성
//                file.mkdir();
//            }
//
//            os.write(content);
//            os.flush();
//            os.close();
//        } catch (IOException e) {
//            Log.e(TAG, "파일을 쓰는데 실패했습니다." + e.toString());
//        }
//    }


    public void appendLog(String text)
    {
        File logFile = new File(getExternalFilesDir(null), FILENAME);
        Log.e("1", "File Check: " + logFile.exists());

        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {

                e.printStackTrace();
            }
        }
        try
        {
            //퍼포먼스를 위해 BufferedWriter를 썼고 FileWriter의 true는 파일을 이어쓸 수 있게 하기 위해
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // 적절한 예외처리를 해주면됩니다.
            e.printStackTrace();
            Log.e(TAG, "파일을 쓰는데 실패했습니다." + e.toString());
        }
    }


    public void deleteToFile() { //파일을 만들고, 저장한다. 저장 위치는 data
        File file = new File(getExternalFilesDir(null), FILENAME);
        file.delete();
        Log.e("2", "File Delete Result : " + file.exists());
    }

    public boolean checkToFile() {
        File file = new File(getExternalFilesDir(null), FILENAME);
        Log.e("3", "File Exist? : " + file.exists());

        if(file.exists()) {
            return true;
        } else {
            return false;
        }
    }


    public void onApplyButtonClicked () { //임계값 조절 버튼
        if(editText.getText().toString().equals("")) {
            Toast.makeText(this, "값이 입력되지 않았습니다. 입력 후 재시도 바랍니다.", Toast.LENGTH_LONG).show();
        } else {
            SHAKE_THRESHOLD = Integer.parseInt(editText.getText().toString());
            txtTHRESHOLD.setText("임계치 : " + Integer.toString(SHAKE_THRESHOLD));
            Toast.makeText(this, "임계값 조정 : " + editText.getText(), Toast.LENGTH_LONG).show();
            editText.setText("");
        }
    }

    public void onApplyButtonClicked2 () {
        if(editText2.getText().toString().equals("")) {
            Toast.makeText(this, "값이 입력되지 않았습니다. 입력 후 재시도 해주세요.", Toast.LENGTH_LONG).show();

        } else {

            timerPeriod = Integer.parseInt(editText2.getText().toString());
            txtTimerPeriod.setText(Integer.toString(timerPeriod));
//            timer_sec_real = (double)timerPeriod / 1000;

            timer_sec_real = BigDecimal.valueOf((double) timerPeriod/1000);
            txtTimerPeriodSec.setText(timer_sec_real.toString());

            Toast.makeText(this, "타이머 간격 조정 = " + timerPeriod + ", " + timer_sec_real, Toast.LENGTH_LONG).show();
            editText2.setText("");
        }
    }
}
