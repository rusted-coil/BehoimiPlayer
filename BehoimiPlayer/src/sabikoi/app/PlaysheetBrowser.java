package sabikoi.app;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

public class PlaysheetBrowser extends Activity implements OnItemClickListener{
	SQLiteDatabase db;//データベースオブジェクト
	ListView songList;
	String sheetname;
	ArrayList<String> songpaths = new ArrayList<String>();
	ArrayList<Integer> playcounts = new ArrayList<Integer>(); 
	ArrayList<Integer> skipcounts = new ArrayList<Integer>(); 
	
  @Override
  public void onCreate(Bundle bundle) 
  {
		super.onCreate(bundle);
		
		Intent intent = getIntent();
		sheetname = intent.getStringExtra("sheetname");

		//データベースオブジェクトの取得
		DBHelper dbHelper = new DBHelper(this);
		db=dbHelper.getWritableDatabase();
				
		Reconstruct();
  }

  void Reconstruct()
  {
		List<FileListArrayItem> items = new ArrayList<FileListArrayItem>();
		
		Bitmap icon=null;
		
		//プレイシートデータを取得
		Cursor c = null;
		try{
			c = db.query("sheet",
					new String[]{"path","name","playcount","skipcount"},
					"sheetname = ?",
					new String[]{sheetname},
					null,null,null);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		items.clear();
		songpaths.clear();
		playcounts.clear();
		skipcounts.clear();
		if(c.moveToFirst())
		{
			do
			{
				String test = c.getString(1);
				songpaths.add(c.getString(0));
				playcounts.add(c.getInt(2));
				skipcounts.add(c.getInt(3));
				items.add(new FileListArrayItem(icon,test,false));
			}while(c.moveToNext());
		}
		c.close();
		
		ListAdapter adapter = new FileListArrayAdapter(this,R.layout.filelist,items);
		songList = new ListView(this);
		songList.setAdapter(adapter);
		songList.setFocusable(true);
		songList.setScrollingCacheEnabled(false);
		songList.setOnItemClickListener(this);
		songList.setVerticalFadingEdgeEnabled(false);      
		setContentView(songList);
  }

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
	{
		Intent intent = new Intent(this,sabikoi.app.PlayerActivity.class);
		intent.putExtra("mode", StaticFinals.ModeStartPlaysheet);
		String []filepathlist = (String[])songpaths.toArray(new String[0]); 
		intent.putExtra("filepathlist", filepathlist);
		int []playcountlist = new int[playcounts.size()];
		for(int i=0;i<playcounts.size();i++)
			playcountlist[i] = playcounts.get(i);
		intent.putExtra("playcounts", playcountlist);
		int []skipcountlist = new int[skipcounts.size()];
		for(int i=0;i<skipcounts.size();i++)
			skipcountlist[i] = skipcounts.get(i);
		intent.putExtra("skipcounts", skipcountlist);
		intent.putExtra("playingsheet", sheetname);
		intent.putExtra("cursor", arg2);
    startActivity(intent);		
	}
}
