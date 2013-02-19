package sabikoi.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;

public class PlaysheetActivity extends Activity implements OnItemClickListener{
	SQLiteDatabase db;//データベースオブジェク
	ListView sheetList;
	ArrayList<String>  playsheetname = new ArrayList<String>();
	String selectingname;
	
	//定数
	static final int CMENU_DELETE = 10;
	static final int CMENU_RPLAY = 20;
	
  @Override
  public void onCreate(Bundle bundle) 
  {
		super.onCreate(bundle);

		//データベースオブジェクトの取得
		DBHelper dbHelper = new DBHelper(this);
		db=dbHelper.getWritableDatabase();
				
		Reconstruct();
		
		registerForContextMenu(sheetList);
  }
  void Reconstruct()
  {
		List<CommandListArrayItem> items = new ArrayList<CommandListArrayItem>();
		
		Bitmap icon=null;
		
		//プレイシートデータを取得
		Cursor c = db.query("sheets",
				new String[]{"name"},
				null,null,null,null,null);
		
		items.clear();
		playsheetname.clear();
		if(c.moveToFirst())
		{
			do
			{
				String test = c.getString(0);
				playsheetname.add(test);
				items.add(new CommandListArrayItem(icon,test));
			}while(c.moveToNext());
		}
		c.close();
		
		ListAdapter adapter = new CommandListArrayAdapter(this,R.layout.commandlist,items);
		sheetList = new ListView(this);
		sheetList.setAdapter(adapter);
		sheetList.setFocusable(true);
		sheetList.setScrollingCacheEnabled(false);
		sheetList.setOnItemClickListener(this);
		sheetList.setVerticalFadingEdgeEnabled(false);      
		setContentView(sheetList);
  }
  
  public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo info) {
  	super.onCreateContextMenu(menu, view, info);
  	AdapterContextMenuInfo adapterInfo = (AdapterContextMenuInfo) info;
  	ListView listView = (ListView) view;
  	CommandListArrayItem item = (CommandListArrayItem) listView.getItemAtPosition(adapterInfo.position);
  	selectingname = item.text;
  	menu.add(0,CMENU_DELETE,0,"削除");
  	menu.add(0,CMENU_RPLAY,0,"ランダム再生");
  }
  public boolean onContextItemSelected(MenuItem item)
  {
//    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();    
    switch (item.getItemId()) 
    {
	    case CMENU_DELETE:
	    	DeleteSheet(selectingname);
	    	Reconstruct();
	    	return true;
	    case CMENU_RPLAY:
	  		Intent intent = new Intent(this,sabikoi.app.PlaysheetBrowser.class);
	  		intent.putExtra("sheetname", selectingname);
	  		intent.putExtra("mode", 1);
	  		startActivity(intent);
	    	return true;
	    default:
	    	return super.onContextItemSelected(item);
    }
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
  	super.onCreateOptionsMenu(menu);
  	
  	//アイテムの追加
  	MenuItem item0 = menu.add(0,0,0,"新規作成");
  	item0.setIcon(android.R.drawable.ic_menu_add);
  	MenuItem item1 = menu.add(1,1,0,"エクスポート");
  	item1.setIcon(android.R.drawable.ic_menu_save);
  	MenuItem item2 = menu.add(1,2,0,"インポート");
  	item2.setIcon(android.R.drawable.ic_menu_revert);
  	
		return true;
  	
  }
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
  	switch(item.getItemId())
  	{
  		case 0:
  			showDialog(this,"プレイシート新規作成","プレイシート名");
  			return true;
  		//エクスポート
  		case 1:
  			ExportDatabase();
  			return true;
  		//インポート
  		case 2:
  			ImportDatabase();
  			return true;
  	}
  	return true;
  }
  
  private void ExportDatabase()
  {
//	db.execSQL("create table if not exists sheets(name text primary key)");
//	db.execSQL("create table if not exists sheet(sheetname text, path text, name text, playcount integer, skipcount integer)");
  	File directory = Environment.getExternalStorageDirectory();
  	String filepath = directory.getAbsolutePath() + "/export.txt";
  	File file = new File(filepath);
  	try{
	  	FileOutputStream fs = new FileOutputStream(file);
	  	OutputStreamWriter osw = new OutputStreamWriter(fs,"Shift-JIS");
	  	PrintWriter pw = new PrintWriter(osw);
	  	
	  	pw.print("<playsheets>\n");
	  	
	  	//プレイシートデータを取得
			Cursor c = db.query("sheets",
					new String[]{"name"},
					null,null,null,null,null);
			if(c.moveToFirst())
			{
				do
				{
					String test = c.getString(0);
			  	pw.print(test);
			  	pw.print("\n");
				}while(c.moveToNext());
			}
			c.close();

	  	pw.print("<data>\n");
	  	
	  	//データを取得
			Cursor cs = db.query("sheet",
					new String[]{"sheetname","path","name","playcount","skipcount"},
					null,null,null,null,null);
			if(cs.moveToFirst())
			{
				do
				{
					String test = cs.getString(0);
			  	pw.print(test);
			  	pw.print("\n");

					test = cs.getString(1);
			  	pw.print(test);
			  	pw.print("\n");

					test = cs.getString(2);
			  	pw.print(test);
			  	pw.print("\n");

					int kari = cs.getInt(3);
			  	pw.print(kari);
			  	pw.print("\n");

					kari = cs.getInt(4);
			  	pw.print(kari);
			  	pw.print("\n");

				}while(cs.moveToNext());
			}
			cs.close();

	  	pw.close();
	  	osw.close();
	  	fs.close();
  	}catch(Exception e)
  	{
  		e.printStackTrace();
  	}
  	
  }
  
  private void ImportDatabase()
  {
//	db.execSQL("create table if not exists sheets(name text primary key)");
//	db.execSQL("create table if not exists sheet(sheetname text, path text, name text, playcount integer, skipcount integer)");
  	File directory = Environment.getExternalStorageDirectory();
  	String filepath = directory.getAbsolutePath() + "/export.txt";
  	File file = new File(filepath);
  	try{
	  	FileInputStream fs = new FileInputStream(file);
	  	InputStreamReader osw = new InputStreamReader(fs,"Shift-JIS");
	  	BufferedReader br = new BufferedReader(osw);

	  	String line = br.readLine();
	  	if(line.compareTo("<playsheets>") != 0)
	  	{
	  		br.close();
	  		throw new Exception("hoge");
	  	}

	  	//シートリスト
	  	while(true)
	  	{
	  		line = br.readLine();
	  		if(line.compareTo("<data>") == 0)
	  			break;
	  		
	  		CreateSheet(line);
	  	}
	  	
	  	//データ
	  	while((line = br.readLine()) != null)
	  	{
	    	ContentValues values = new ContentValues();
	    	values.put("sheetname", line);
	    	line = br.readLine();
	    	values.put("path", line);
	    	line = br.readLine();
	    	values.put("name", line);
	    	line = br.readLine();
	    	values.put("playcount", Integer.valueOf(line));
	    	line = br.readLine();
	    	values.put("skipcount", Integer.valueOf(line));
	    	db.insert("sheet","",values);	  		
	  	}
	  	
	  	br.close();
	  	osw.close();
	  	fs.close();
  	}catch(Exception e)
  	{
  		e.printStackTrace();
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
				CreateSheet(et.getText().toString());
  			Reconstruct();
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
  void DeleteSheet(String name)
  {
  	db.delete("sheets", "name=?", new String[]{name});
		db.delete("sheet", "sheetname = " + name,null);
  }

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
	{
		Intent intent = new Intent(this,sabikoi.app.PlaysheetBrowser.class);
		intent.putExtra("sheetname", playsheetname.get(arg2));
		startActivity(intent);
	}
}
