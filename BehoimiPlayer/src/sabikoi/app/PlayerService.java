package sabikoi.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class PlayerService extends Service  implements OnCompletionListener{
	Messenger msgToActivity;
	Messenger msgFromActivity;
	MediaPlayer mp = null;
	ArrayList<String> filepathlist = new ArrayList<String>();
	ArrayList<Integer> playcounts;
	ArrayList<Integer> skipcounts;
	int cursor;
	ReporterHandler reporter;
	int playoption;
	String playingsheet;
	SQLiteDatabase db;//データベースオブジェクト
	static int NOTIFICATION_ID = 999;
	
	@Override
	public IBinder onBind(Intent intent) {
		return msgFromActivity.getBinder();
	}
	
	class IncommingHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
				case StaticFinals.MsgPause:
					Message msg2 = Message.obtain(null,StaticFinals.MsgReport,mp.getCurrentPosition(),mp.getDuration());
					try {
						msgToActivity.send(msg2);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					mp.stop();
					msgToActivity = null;
					reporter = null;
					stopSelf();
					break;
				case StaticFinals.MsgSeekto:
					if(mp != null)
						mp.seekTo(msg.arg1);
					break;
				case StaticFinals.MsgReplySet:
					msgToActivity = msg.replyTo;
					Message msg3 = Message.obtain(null,StaticFinals.MsgCursorupdate,cursor,0);
					try {
						msgToActivity.send(msg3);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					break;
				case StaticFinals.MsgReplyUnset:
					msgToActivity = null;
					break;
				case StaticFinals.MsgNextsong:
					ChangeSong(1);
					break;
			}
		}
	}
	
	class ReporterHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			if(msgToActivity != null && mp != null && mp.isPlaying())
			{
				Message msg2 = Message.obtain(null,StaticFinals.MsgReport,mp.getCurrentPosition(),mp.getDuration());
				try {
					msgToActivity.send(msg2);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			if(reporter != null)
				reporter.sleep(100);
		}
		
		//スリープメソッド
		public void sleep(long delayMills)
		{
			removeMessages(0);
			sendMessageDelayed(obtainMessage(0),delayMills);
		}
	}
	
	@Override
	public void onStart(Intent intent, int startID)
	{
		msgFromActivity = new Messenger(new IncommingHandler());
		
		//データベースオブジェクトの取得
		DBHelper dbHelper = new DBHelper(this);
		db=dbHelper.getWritableDatabase();
		
		filepathlist = intent.getStringArrayListExtra("filepathlist");
		cursor = intent.getIntExtra("cursor", 0);
		int pos = intent.getIntExtra("seekposition", 0);
		playcounts = intent.getIntegerArrayListExtra("playcounts");
		skipcounts = intent.getIntegerArrayListExtra("skipcounts");
		playingsheet = intent.getStringExtra("playingsheet");
		playoption = intent.getIntExtra("playoption",0);
		mp = new MediaPlayer();
		
		try {
			mp.setDataSource(filepathlist.get(cursor));
			mp.prepare();
			mp.setOnCompletionListener(this);
			mp.seekTo(pos);
			mp.start();
						
			reporter = new ReporterHandler();
			reporter.sleep(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//ノーティフィケーションオブジェクトの生成
		Notification notification=new Notification();
		notification.icon = R.drawable.ic_launcher;
		Intent subintent = new Intent(this,sabikoi.app.PlayerActivity.class);
		subintent.putExtra("mode",StaticFinals.ModeNormal);
		subintent.putStringArrayListExtra("filepathlist",filepathlist);
		subintent.putExtra("cursor",cursor);
		subintent.putExtra("playingsheet",playingsheet);
		subintent.putIntegerArrayListExtra("playcounts",playcounts);
		subintent.putIntegerArrayListExtra("skipcounts",skipcounts);
		PendingIntent pintent=PendingIntent.getActivity(this, 0, subintent, 0);
		notification.setLatestEventInfo(this, "ベホイミプレイヤー", filepathlist.get(cursor).substring(filepathlist.get(cursor).lastIndexOf("/")+1), pintent);
		notification.flags = Notification.FLAG_FOREGROUND_SERVICE;
		
		startForeground(NOTIFICATION_ID,notification);
	}
	
	void updateNotification()
	{
		//ノーティフィケーションマネージャーの取得
		NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		//ノーティフィケーションオブジェクトの生成
		Notification notification=new Notification();
		notification.icon = R.drawable.ic_launcher;
		Intent subintent = new Intent(this,sabikoi.app.PlayerActivity.class);
		subintent.putExtra("mode",StaticFinals.ModeNormal);
		subintent.putStringArrayListExtra("filepathlist",filepathlist);
		subintent.putExtra("cursor",cursor);
		subintent.putExtra("playingsheet",playingsheet);
		subintent.putIntegerArrayListExtra("playcounts",playcounts);
		subintent.putIntegerArrayListExtra("skipcounts",skipcounts);
		subintent.putExtra("playoption",playoption);
		PendingIntent pintent=PendingIntent.getActivity(this, 0, subintent, 0);
		notification.setLatestEventInfo(this, "ベホイミプレイヤー", filepathlist.get(cursor).substring(filepathlist.get(cursor).lastIndexOf("/")+1), pintent);
		notification.flags = Notification.FLAG_FOREGROUND_SERVICE;
		
		nm.notify(NOTIFICATION_ID, notification);
	}
	
	void ChangeSong(int flag)
	{
		Random rng = new Random();
		int next;

		//カウントを更新
		if(playoption == 1)
		{
			//シートプレイ
			if(playingsheet != null)
			{
				if(flag == 0)
				{
					ContentValues values = new ContentValues();
					values.put("playcount", playcounts.get(cursor)+1);
					db.update("sheet", values,
							"sheetname=? AND path=?", new String[]{playingsheet,filepathlist.get(cursor)});
					playcounts.set(cursor, playcounts.get(cursor)+1);
				}
				else
				{
					ContentValues values = new ContentValues();
					values.put("playcount", playcounts.get(cursor)+1);
					values.put("skipcount", skipcounts.get(cursor)+1);
					db.update("sheet", values,
							"sheetname=? AND path=?", new String[]{playingsheet,filepathlist.get(cursor)});
					playcounts.set(cursor, playcounts.get(cursor)+1);
					skipcounts.set(cursor, skipcounts.get(cursor)+1);
				}
				
				while(true)
				{
					next = rng.nextInt(filepathlist.size());
					if(skipcounts.get(next) != 0)
					{
						int ran = rng.nextInt(1+playcounts.get(next));
						if(ran <= playcounts.get(next)-skipcounts.get(next))
							break;
					}else
						break;
				}
			}
			//フォルダプレイ
			else
			{
				next = rng.nextInt(filepathlist.size());
			}
			
			cursor=next;		
		}
		else
			cursor++;
		
		if(cursor >= filepathlist.size())
			cursor = 0;
		updateNotification();
		try{
			mp.reset();

			mp.setDataSource(filepathlist.get(cursor));
			mp.prepare();
			mp.setOnCompletionListener(this);
			mp.start();										
		}catch(Exception e){
			e.printStackTrace();
		}						
		if(msgToActivity != null)
		{
			Message msg = Message.obtain(null,StaticFinals.MsgCursorupdate,cursor,flag);
			try {
				msgToActivity.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		ChangeSong(0);
	}
}
