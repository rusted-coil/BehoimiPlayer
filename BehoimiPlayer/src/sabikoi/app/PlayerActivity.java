package sabikoi.app;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PlayerActivity extends Activity implements OnClickListener
{
	RelativeLayout layout;
	Messenger msgToService;
	Messenger msgFromService;
	int nowposition;
	String[] filepathlist;
	int cursor;
	String playingsheet;
	boolean nowplaying = false;
	SeekBar seekBar;
	TextView songtitle,statistics;
	ImageView playpause,skipbutton,toheadbutton;
	int[] playcounts;
	int[] skipcounts;
	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT; 
	private final int FP = ViewGroup.LayoutParams.FILL_PARENT;
	private final int ID_TV1 = 1;
	private final int ID_TV2 = 7;
	private final int ID_SONGTITLE = 2;
	private final int ID_PLAYPAUSE = 3;
	private final int ID_SKIP = 4;
	private final int ID_TOHEAD = 5;
	private final int ID_SEEKBAR = 6;
	private final int ID_STATISTICS = 8;
	SQLiteDatabase db;//データベースオブジェクト
	
	class ActivityHandler extends Handler 
	{
    //Handler#handleMessage()をoverrideしServiceからのMessageを受け取った際の処理を実装
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what)
			{
				case StaticFinals.MsgReport://定時連絡
					nowposition = msg.arg1;
					if(seekBar != null)
					{
						seekBar.setMax(msg.arg2);
						seekBar.setProgress(msg.arg1);
					}
					break;
				case StaticFinals.MsgPause:
					nowplaying = false;
					break;
				case StaticFinals.MsgCursorupdate:
					ChangeSong(msg.arg1,msg.arg2);
					break;
			}
		}
	}
	
	void ChangeSong(int next, int flag)
	{
		//カウントを更新
		if(flag == 0)
		{
			ContentValues values = new ContentValues();
			values.put("playcount", playcounts[cursor]+1);
			db.update("sheet", values,
					"sheetname=? AND path=?", new String[]{playingsheet,filepathlist[cursor]});
			playcounts[cursor]++;
		}
		else
		{
			ContentValues values = new ContentValues();
			values.put("playcount", playcounts[cursor]+1);
			values.put("skipcount", skipcounts[cursor]+1);
			db.update("sheet", values,
					"sheetname=? AND path=?", new String[]{playingsheet,filepathlist[cursor]});
			playcounts[cursor]++;
			skipcounts[cursor]++;
		}
		
		cursor = next;
		songtitle.setText(filepathlist[cursor]);
		statistics.setText("再生回数:" + playcounts[cursor] + " / スキップ回数:"+skipcounts[cursor]);
	}
	
	ServiceConnection conn = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName className, IBinder service) 
		{
			msgFromService = new Messenger(new ActivityHandler());
			msgToService = new Messenger(service);
			try{
				Message msg = Message.obtain(null, StaticFinals.MsgReplySet);
				msg.replyTo = msgFromService;
				msgToService.send(msg);
			}catch (Exception e){				
			}			
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			//リスナーのアンセットを指示
			try{
				Message msg = Message.obtain(null, StaticFinals.MsgReplyUnset);
				msgToService.send(msg);
			}catch(Exception e)
			{
			}
			msgFromService = null;
			msgToService = null;
		}
	};
	
	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		
		//データベースオブジェクトの取得
		DBHelper dbHelper = new DBHelper(this);
		db=dbHelper.getWritableDatabase();
		
		//起動情報の受取
		Intent intent = getIntent();
		int mode = intent.getIntExtra("mode",StaticFinals.ModeNormal);
		filepathlist = intent.getStringArrayExtra("filepathlist");
		cursor = intent.getIntExtra("cursor", 0);
		if(mode == StaticFinals.ModeStartFolder || mode == StaticFinals.ModeStartPlaysheet)
		{
			if(mode == StaticFinals.ModeStartPlaysheet)
			{
				playingsheet = intent.getStringExtra("playingsheet");
				playcounts = intent.getIntArrayExtra("playcounts");
				skipcounts = intent.getIntArrayExtra("skipcounts");
			}
			//再生の開始
			nowposition = 0;
			PlayMedia();
		}
		
		layout = new RelativeLayout(this);
//		layout.setOrientation(LinearLayout.VERTICAL);
		
		TextView tv1 = new TextView(this);
		tv1.setText("楽曲情報");
		tv1.getPaint().setUnderlineText(true);
		tv1.setId(ID_TV1);
		RelativeLayout.LayoutParams rlp1 = new RelativeLayout.LayoutParams(FP,WC);
		rlp1.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		layout.addView(tv1,rlp1);
		
		//タイトル
		songtitle = new TextView(this);
		songtitle.setText(filepathlist[cursor].substring(filepathlist[cursor].lastIndexOf("/")+1));
		songtitle.setId(ID_SONGTITLE);
		RelativeLayout.LayoutParams rlp2 = new RelativeLayout.LayoutParams(WC,WC);
		rlp2.addRule(RelativeLayout.BELOW,ID_TV1);
		layout.addView(songtitle,rlp2);

		TextView tv2 = new TextView(this);
		tv2.setText("統計情報");
		tv2.getPaint().setUnderlineText(true);
		tv2.setId(ID_TV2);
		RelativeLayout.LayoutParams rlp7 = new RelativeLayout.LayoutParams(FP,WC);
		rlp7.addRule(RelativeLayout.BELOW,ID_SONGTITLE);
		layout.addView(tv2,rlp7);
		
		//統計情報
		statistics = new TextView(this);
		statistics.setText("再生回数:" + playcounts[cursor] + " / スキップ回数:"+skipcounts[cursor]);
		statistics.setId(ID_STATISTICS);
		RelativeLayout.LayoutParams rlp8 = new RelativeLayout.LayoutParams(WC,WC);
		rlp8.addRule(RelativeLayout.BELOW,ID_TV2);
		layout.addView(statistics,rlp8);
		
		playpause = new ImageView(this);
		playpause.setImageResource(R.drawable.pause);
		playpause.setOnClickListener(this);
		playpause.setId(ID_PLAYPAUSE);
		RelativeLayout.LayoutParams rlp3 = new RelativeLayout.LayoutParams(WC,WC);
		rlp3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		rlp3.addRule(RelativeLayout.CENTER_HORIZONTAL);
		layout.addView(playpause,rlp3);
		
		skipbutton = new ImageView(this);
		skipbutton.setImageResource(R.drawable.skip);
		skipbutton.setOnClickListener(this);
		skipbutton.setId(ID_SKIP);
		RelativeLayout.LayoutParams rlp5 = new RelativeLayout.LayoutParams(WC,WC);
		rlp5.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		rlp5.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		layout.addView(skipbutton,rlp5);
		
		toheadbutton = new ImageView(this);
		toheadbutton.setImageResource(R.drawable.tohead);
		toheadbutton.setOnClickListener(this);
		toheadbutton.setId(ID_TOHEAD);
		RelativeLayout.LayoutParams rlp6 = new RelativeLayout.LayoutParams(WC,WC);
		rlp6.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		rlp6.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		layout.addView(toheadbutton,rlp6);
		
		//シークバー
		seekBar = new SeekBar(this);
		seekBar.setMax(100);
		seekBar.setProgress(0);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				nowposition = seekBar.getProgress();
				PlayMedia();
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				if(nowplaying)
					PauseMedia();
			}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			}
		});
		seekBar.setId(ID_SEEKBAR);
		RelativeLayout.LayoutParams rlp4 = new RelativeLayout.LayoutParams(FP,WC);
		rlp4.addRule(RelativeLayout.ABOVE,ID_PLAYPAUSE);
		layout.addView(seekBar,rlp4);
		
		setContentView(layout);		
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		//通信コネクション
		bindService(new Intent(this,PlayerService.class),conn,0);
		nowplaying = isServiceRunning("sabikoi.app.PlayerService");		
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		//リスナーのアンセットを指示
		try{
			Message msg = Message.obtain(null, StaticFinals.MsgReplyUnset);
			msgToService.send(msg);
		}catch(Exception e)
		{
		}
		msgFromService = null;
		msgToService = null;
	}
	
	void PauseMedia()
	{
		if(msgToService != null)
		{
			Message msg = Message.obtain(null, StaticFinals.MsgPause);
			try{
				msgToService.send(msg);
			}catch(Exception e)
			{}
		}
		nowplaying = false;
	}
	void PlayMedia()
	{
		//再生の開始
    Intent intent2 = new Intent(this,sabikoi.app.PlayerService.class);
    intent2.putExtra("filepathlist", filepathlist);
    intent2.putExtra("cursor", cursor);
		intent2.putExtra("seekposition", nowposition);
		intent2.putExtra("playcounts", playcounts);
		intent2.putExtra("skipcounts", skipcounts);
		startService(intent2);
		bindService(new Intent(this,PlayerService.class),conn,0);
		nowplaying = true;
	}
	
	@Override
	public void onClick(View v) 
	{
		if(v == playpause)
		{
			if(nowplaying)
			{
				PauseMedia();
				playpause.setImageResource(R.drawable.play);
			}else
			{
				PlayMedia();
				playpause.setImageResource(R.drawable.pause);
			}
		}
		else if(v == skipbutton)
		{
			if(msgToService != null)
			{
				Message msg = Message.obtain(null, StaticFinals.MsgNextsong);
				try{
					msgToService.send(msg);
				}catch(Exception e)
				{}
			}
		}
	}
	boolean isServiceRunning(String className) {
    ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
    List<ActivityManager.RunningServiceInfo> serviceInfos = am.getRunningServices(Integer.MAX_VALUE);
    int serviceNum = serviceInfos.size();
    for (int i = 0; i < serviceNum; i++) {
        if (serviceInfos.get(i).service.getClassName().equals(className)) {
            return true;
        }
    }
    return false;
}
}
