package sabikoi.app;

import java.io.IOException;
import java.util.Random;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
	String[] filepathlist;
	int cursor;
	ReporterHandler reporter;
	int[] playcounts;
	int[] skipcounts;
	
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
		
		filepathlist = intent.getStringArrayExtra("filepathlist");
		cursor = intent.getIntExtra("cursor", 0);
		int pos = intent.getIntExtra("seekposition", 0);
		playcounts = intent.getIntArrayExtra("playcounts");
		skipcounts = intent.getIntArrayExtra("skipcounts");
		mp = new MediaPlayer();
		
		try {
			mp.setDataSource(filepathlist[cursor]);
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
		PendingIntent pintent=PendingIntent.getActivity(this, 0, new Intent(this,sabikoi.app.PlayerActivity.class), 0);
		notification.setLatestEventInfo(this, "ベホイミプレイヤー", filepathlist[cursor], pintent);
		notification.flags = Notification.FLAG_FOREGROUND_SERVICE;
		
		startForeground(1,notification);
	}
	
	void ChangeSong(int flag)
	{
		Random rng = new Random();
		int next;

		while(true)
		{
			next = rng.nextInt(filepathlist.length);
			if(skipcounts[next] != 0)
			{
				int ran = rng.nextInt(1+playcounts[next]);
				if(ran <= playcounts[next]-skipcounts[next])
					break;
			}else
				break;
		}
		
		cursor=next;		
		if(cursor >= filepathlist.length)
		{
			mp.stop();
			mp.setOnCompletionListener(null);
			mp.release();
			Message msg2 = Message.obtain(null,StaticFinals.MsgPause);
			try {
				msgToActivity.send(msg2);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			msgToActivity = null;
			reporter = null;
			stopSelf();
		}else
		{
			try{
				mp.reset();

				mp.setDataSource(filepathlist[cursor]);
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
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		ChangeSong(0);
	}
}
