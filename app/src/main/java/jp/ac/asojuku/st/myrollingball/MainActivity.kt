package jp.ac.asojuku.st.myrollingball

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SensorEventListener,SurfaceHolder.Callback{

    //プロパティ
    private var surfaceWidth:Int=0; //サーフェスの幅
    private var sufaceHeight:Int=0; //サーフェスの高さ

    private val radius=50.0f; //ボールの半径
    private val coef=1000.0f; //ボールの移動量を計算するための係数(計数)

    private var ballx:Float=0f; //ボールの現在のX座標
    private var bally:Float=0f; //ボールの現在のY座標
    private var vx:Float=0f; //ボールのX座標の加速度
    private var vy:Float=0f; //ボールのY座標の加速度
    private var time:Long=0L; //前回の取得時間

    private var flag=1;

    //誕生時のライフサイクルイベント
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val holder=surfaceView.holder; //サーフェスホルダーを取得
        holder.addCallback(this); //サーフェスホルダーのコールバックに自クラスを追加
        //画面の縦横してわプリから指定指定してロック(縦方向に)
        requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        button.setOnClickListener{onbtn()}
    }

    //画面表示・再表示のライフサイクルイベント
    override fun onResume() {

        //親クラスのonResume()処理
        super.onResume()

        //自クラスのonResume()処理
        //センサーマネージャをOSから取得
        //val sensorManager=this.getSystemService(Context.SENSOR_SERVICE) as SensorManager;
        //加速度センサー(Accelerometer)を指定してセンサーマネージャからセンサーを取得
        //val accSensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //リスナー登録して加速度センサーの監視を開始
        /*sensorManager.registerListener(
                this, //イベントリスナー機能を持つインスタンス（自クラスのインスタンス）
                accSensor, //監視するセンサー（加速度センサー）
                SensorManager.SENSOR_DELAY_GAME //センサーの更新頻度
        )*/
    }

    //reset
    fun onbtn(){
        imageView.setImageResource(R.drawable.soccer_cheer_horn_ouen);
        textView.setText("Fight");
        //ボールの初期位置を保存しておく
        ballx=(surfaceWidth/2).toFloat();
        bally=sufaceHeight.toFloat();
        flag=1;
    }

    //画面が非表示の時のライフサイクルべんと
    override fun onPause() {
        super.onPause()
        val sensorManager=this.getSystemService(Context.SENSOR_SERVICE) as SensorManager;
        sensorManager.unregisterListener(this);
    }

    //制度が変わった時のイベントコールバック
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    //センサーの値が変わった時のイベントコールバック
    override fun onSensorChanged(event: SensorEvent?) {

        //センサーの値が変わったらログに出力する

        //イベントが何もなかったらそのままリターン
        if(event==null){ return; }

        //加速度センサーか判定
        /*if (event.sensor.type == Sensor.TYPE_ACCELEROMETER){
            //ログ出力用文字列を組み立て
            val str:String="x = ${event.values[0].toString()}"+" y = ${event.values[1].toString()}"+" z = ${event.values[2].toString()}";
            //デバックログに出力
            //Lo.d("加速度センサー",str);
            //テキストビューに表示
            //txvMain.text=str;
        }*/

        //ボールの描画の計算処理

        if (time==0L){
            time=System.currentTimeMillis(); //最初のタイミングでは現在時刻を保存
        }
        //イベントのセンサー種別の情報がアクセラメーター(加速度センサー)の時だけ以下の処理を実行
        if (event.sensor.type==Sensor.TYPE_ACCELEROMETER){

            //センサーのx(左右),y(縦)値を取得
            val x=event.values[0]*-1;
            val y=event.values[1];

            //経過時間を計算(今の時間-前の時間=経過時間)
            var t=(System.currentTimeMillis()-time).toFloat();
            //今の時間を「前の時間として保存」
            time=System.currentTimeMillis();
            t/=1000.0f;

            //移動距離を計算(ボールをどれくらい動かすか)
            val dx=(vx*t)+(x*t*t)/2.0f; //xの移動距離(メートル)
            val dy=(vy*t)+(y*t*t)/2.0f; //yの移動距離(メートル)
            //メートルをピクセルのcmに補正してボールのX座標に足しこむ＝新しいボールのX座標
            ballx+=(dx*coef);
            //メートルをピクセルのcmに補正してボールのY座標に足しこむ＝新しいボールのY座標
            bally+=(dy*coef);
            //今の各方向の加速度を更新
            vx+=(x*t);
            vy+=(y*t);

            //画面の端に来たら跳ね返る処理

            //左右について
            if (((ballx-radius)<0&&vx<0)) {
                //左にぶつかった時
                vx = -vx / 1.5f;
                ballx = radius;
            }else if((ballx+radius)>surfaceWidth&&vx>0){
                //右にぶつかった時
                vx=-vx/1.5f;
                ballx=(surfaceWidth-radius);
            }else if(bally-radius<500.0f&&bally+radius>400.0f&&ballx-radius==700.0f){
                vx=-1*vx;
                ballx = radius+700.0f;
            }else if(bally-radius<900.0f&&bally+radius>800.0f&&ballx+radius==500.0f){
                vx=-1*vx;
                ballx=radius-500.0f;
            }
            //上下について
            if (((bally-radius<0)&&vy<0)/*||(bally+radius==900.0f&&ballx+radius>500.0f)*/){
                /*if(bally+radius==900.0f&&ballx+radius>500.0f){
                    vy=vy*(-1)
                }*/
                //&&ballx+radius>500.0f
                //うえにぶつかった時
                vy=-vy/1.5f;
                bally=radius;
            }else if((bally+radius)>sufaceHeight&&vy>0) {
                vy = -vy / 1.5f;
                bally = sufaceHeight - radius;
            }else if(bally-radius<=500.0f&&bally+radius>=400.0f&&ballx-radius<=700.0f){
                vy=-1*vy;
                if (bally-radius==500.0f){
                    bally=radius+500.0f;
                }else if(bally+radius==400.0f){
                    bally=400.0f-radius;
                }
            }else if(bally-radius<=900.0f&&bally+radius>=800.0f&&ballx+radius>=500.0f){
                vy=-1*vy;
                if (bally-radius==900.0f){
                    bally=radius+900.0f;
                }else if(bally+radius==800.0f){
                    bally=800.0f-radius;
                }
            }

            //障害物、またはクリア判定
            if(flag==1){
                //クリア
                if(bally-radius<80.0f){
                    //vy=-vy/1.5f;
                    //bally=radius;
                    clear();
                }
                //失敗上
                if (bally-radius<=500.0f&&bally+radius>=400.0f&&ballx-radius<=700.0f){
                    //vy=0.0f;
                    fail();
                }else if (bally-radius<=900.0f&&bally+radius>=800.0f&&ballx+radius>=500.0f){
                    fail();
                }
            }




            //キャンバスに描画
            this.drawCanvas();

        }

    }

    //サーフェスが更新されたイベント
    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

        //サーフェスの幅と高さをプロパティに保存しておく
        surfaceWidth=width;
        sufaceHeight=height;
        //ボールの初期位置を保存しておく
        ballx=(width/2).toFloat();
        bally=height.toFloat();

    }

    //サーフェスが破棄された時のイベント
    override fun surfaceDestroyed(holder: SurfaceHolder?) {

        //加速度センサーの登録を解除する流れ
        val sensorManager=this.getSystemService(Context.SENSOR_SERVICE) as SensorManager;
        //センサーマネージャを通じてOSからリスナー(自分自身)を登録解除
        sensorManager.unregisterListener(this);

    }

    //サーフェスが作成された時のイベント
    override fun surfaceCreated(holder: SurfaceHolder?) {

        //加速度センサーのリスナーを登録する流れ
        //センサーマネージャを取得
        val sensorManager=this.getSystemService(Context.SENSOR_SERVICE) as SensorManager;
        //センサーマネージャから加速度センサーを取得
        val accSensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //加速度センサーのリスナーをOSに登録
        sensorManager.registerListener(
                this, //リスナー(自クラス)
                accSensor, //加速度センサー
                SensorManager.SENSOR_DELAY_GAME //センシングの頻度
        )

    }

    //サーフェスのキャンバスに描画するメソッド
    private fun drawCanvas(){
        //キャンバスをロックして取得
        val canvas=surfaceView.holder.lockCanvas();
        //キャンバスの背景色を設定
        canvas.drawColor(Color.BLACK);
        //キャンバスに円を描いてボールにする
        canvas.drawCircle(
                ballx,
                bally,
                radius,
                Paint().apply {
                    color=Color.MAGENTA; //ペイントブラシのインスタンス
                }
        );
        canvas.drawRect(
                0.0f, 0.0f, 1500.0f, 80.0f,
                Paint().apply { color=Color.BLUE }
        );
        canvas.drawRect(
                0.0f, 400.0f, 700.0f, 500.0f,
                Paint().apply { color=Color.WHITE }
        );
        canvas.drawRect(
                500.0f, 800.0f, 1200.0f, 900.0f,
                Paint().apply { color=Color.WHITE }
        );
        //キャンバスをアンロック(ロック解除)
        surfaceView.holder.unlockCanvasAndPost(canvas);

    }

    //clear
    private fun clear(){
        imageView.setImageResource(R.drawable.present_happy_boy);
        textView.setText("Clear!");
        flag=0;
    }

    //fail
    private fun fail(){
        imageView.setImageResource(R.drawable.gakkari_tameiki_man);
        textView.setText("Fail...");
        flag=0;
    }

}
