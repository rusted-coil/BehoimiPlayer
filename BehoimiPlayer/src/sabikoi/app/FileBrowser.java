package sabikoi.app;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;

public class FileBrowser extends Activity implements OnItemClickListener{
	ListView fileList;
	ArrayList<File> files = new ArrayList<File>();
	String[] filepathlist;
	SQLiteDatabase db;//データベースオブジェクト
	ArrayList<String>  playsheetname = new ArrayList<String>();
	int selectingindex;
	
	//定数
	static final int CMENU_TOP = 10;
	static final int CMENU_SHEETS = 20;
	static final int CMENU_ADDSHEET = 11;

  @Override
  public void onCreate(Bundle bundle) 
  {
		super.onCreate(bundle);
		Intent intent = getIntent();
		String filepath = intent.getStringExtra("currentpath");
		
		List<FileListArrayItem> items = new ArrayList<FileListArrayItem>();
		
		File dir = new File(filepath);
		File[] filessub = dir.listFiles();
		files.clear();
		for(File file : filessub)
		{
			if(file.isHidden() == false)
				files.add(file);
		}
		
		try{
			Collections.sort(files, new Comparator<File>(){
				@Override
				public int compare(File f1, File f2)
				{
					if(f1.isDirectory() == f2.isDirectory())
						return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
					else if(f1.isDirectory())
						return -1;
					return 1;
				}
			});
		}catch (Exception e)
		{
			Log.d("error",e.toString());
		}
		
		Bitmap icon=null;
		filepathlist = new String[files.size()];
		for(int i=0;i<files.size();i++)
		{
			filepathlist[i] = files.get(i).toString();
			items.add(new FileListArrayItem(icon, files.get(i).getName(),files.get(i).isDirectory()));
		}
		
		ListAdapter adapter = new FileListArrayAdapter(this,R.layout.filelist,items);
		fileList = new ListView(this);
		fileList.setAdapter(adapter);
		fileList.setFocusable(true);
		fileList.setScrollingCacheEnabled(false);
		fileList.setOnItemClickListener(this);
		fileList.setVerticalFadingEdgeEnabled(false);      
		fileList.setFastScrollEnabled(true);
      
		setContentView(fileList);

		//データベースオブジェクトの取得
		DBHelper dbHelper = new DBHelper(this);
		db=dbHelper.getWritableDatabase();

		//プレイシートデータを取得
		Cursor c = db.query("sheets",
				new String[]{"name"},
				null,null,null,null,null);
		playsheetname.clear();
		if(c.moveToFirst())
		{
			do
			{
				String test = c.getString(0);
				playsheetname.add(test);
			}while(c.moveToNext());
		}
		c.close();
		
		registerForContextMenu(fileList);
  }

	@Override
  public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo info) {
  	super.onCreateContextMenu(menu, view, info);
  	AdapterContextMenuInfo adapterInfo = (AdapterContextMenuInfo) info;
  	selectingindex = adapterInfo.position;
  	SubMenu submenu = menu.addSubMenu(0,CMENU_TOP,0,"プレイシートに追加");
  	submenu.add(0,CMENU_ADDSHEET,0,"新しいプレイシート");
  	for(int i=0;i<playsheetname.size();i++)
  	{
  		submenu.add(0,CMENU_SHEETS+i,0,playsheetname.get(i));
  	}
  }
	public static String getSuffix(String fileName) 
	{
    if (fileName == null)
        return null;
    int point = fileName.lastIndexOf(".");
    if (point != -1) {
        return fileName.substring(point + 1);
    }
    return fileName;
	}
	public boolean onContextItemSelected(MenuItem item)
  {
//    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();    
  	if(item.getItemId() >= CMENU_SHEETS && item.getItemId() < CMENU_SHEETS+playsheetname.size())
  	{
  		int i = item.getItemId() - CMENU_SHEETS;
  		//ディレクトリをrecursiveに追加
  		if(files.get(selectingindex).isDirectory())
  		{
  			File dir = new File(files.get(selectingindex).toString());
  			File[] filessub = dir.listFiles();
  			ArrayList<File> files2 = new ArrayList<File>();
  			files2.clear();
  			for(File file : filessub)
  			{  				
  				if(file.isHidden() == false && file.isDirectory() == false && getSuffix(file.getName()).equals("mp3"))
  					files2.add(file);
  			}
  			
  			try{
  				Collections.sort(files2, new Comparator<File>(){
  					@Override
  					public int compare(File f1, File f2)
  					{
  						if(f1.isDirectory() == f2.isDirectory())
  							return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
  						else if(f1.isDirectory())
  							return -1;
  						return 1;
  					}
  				});
  			}catch (Exception e)
  			{
  				Log.d("error",e.toString());
  			}
  			
  			for(File file : files2)
  			{
  				ContentValues values = new ContentValues();
  				values.put("sheetname", playsheetname.get(i));
  				values.put("path", file.toString());
  				values.put("name", file.getName());
  				values.put("playcount", 0);
  				values.put("skipcount", 0);
  				db.insert("sheet", "", values);  				
  			}
  		}else
  			AddSongToSheet(i,selectingindex);
  		return true;
  	}
    switch (item.getItemId()) 
    {
    	case CMENU_ADDSHEET:
  			showDialog(this,"プレイシート新規作成","プレイシート名");
    		return true;
	    case CMENU_TOP:
	    	return true;
	    default:
	    	return super.onContextItemSelected(item);
    }
  }
  
  //新規作成ダイアログ
  private void showDialog(final Activity context, String title, String text)
  {
  	AlertDialog.Builder ad = new AlertDialog.Builder(context);
  	ad.setTitle(title);
  	ad.setMessage(text);
  	final EditText et = new EditText(context);
  	ad.setView(et);
  	ad.setPositiveButton("OK",new DialogInterface.OnClickListener(){		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String newsheetname = et.getText().toString();
				playsheetname.add(newsheetname);
				CreateSheet(newsheetname);
				
	  		//ディレクトリをrecursiveに追加
	  		if(files.get(selectingindex).isDirectory())
	  		{
	  			File dir = new File(files.get(selectingindex).toString());
	  			File[] filessub = dir.listFiles();
	  			ArrayList<File> files2 = new ArrayList<File>();
	  			files2.clear();
	  			for(File file : filessub)
	  			{  				
	  				if(file.isHidden() == false && file.isDirectory() == false && getSuffix(file.getName()).equals("mp3"))
	  					files2.add(file);
	  			}
	  			
	  			try{
	  				Collections.sort(files2, new Comparator<File>(){
	  					@Override
	  					public int compare(File f1, File f2)
	  					{
	  						if(f1.isDirectory() == f2.isDirectory())
	  							return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
	  						else if(f1.isDirectory())
	  							return -1;
	  						return 1;
	  					}
	  				});
	  			}catch (Exception e)
	  			{
	  				Log.d("error",e.toString());
	  			}
	  			
	  			for(File file : files2)
	  			{
	  				ContentValues values = new ContentValues();
	  				values.put("sheetname", newsheetname);
	  				values.put("path", file.toString());
	  				values.put("name", file.getName());
	  				values.put("playcount", 0);
	  				values.put("skipcount", 0);
	  				db.insert("sheet", "", values);  				
	  			}
	  		}else
	  			AddSongToSheet(playsheetname.size()-1,selectingindex);
	  		
				context.setResult(Activity.RESULT_OK);
			}
		});
  	ad.create();
  	ad.show();
  }
  void CreateSheet(String name)
  {
  	ContentValues values = new ContentValues();
  	values.put("name", name);
  	db.insert("sheets","",values);
  }
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
	{	
		if(files.get(arg2).isDirectory())
		{
			Intent intent = new Intent(this,sabikoi.app.FileBrowser.class);
			intent.putExtra("currentpath", files.get(arg2).toString());
			startActivity(intent);
		}else
		{
			Intent intent = new Intent(this,sabikoi.app.PlayerActivity.class);
			intent.putExtra("mode", StaticFinals.ModeStartFolder);
			intent.putExtra("filepathlist", filepathlist);
			intent.putExtra("cursor", arg2);
/*			int []playcountlist = new int[filepathlist.length];
			for(int i=0;i<playcounts.size();i++)
				playcountlist[i] = playcounts.get(i);
			intent.putExtra("playcounts", playcountlist);
			int []skipcountlist = new int[skipcounts.size()];
			for(int i=0;i<skipcounts.size();i++)
				skipcountlist[i] = skipcounts.get(i);
			intent.putExtra("skipcounts", skipcountlist);*/
	    startActivity(intent);		
		}
	}
	
	void AddSongToSheet(int sheetnum, int songnum)
	{
		ContentValues values = new ContentValues();
		values.put("sheetname", playsheetname.get(sheetnum));
		values.put("path", filepathlist[songnum]);
		values.put("name", files.get(songnum).getName());
		values.put("playcount", 0);
		values.put("skipcount", 0);
		db.insert("sheet", "", values);
	}
	
}
