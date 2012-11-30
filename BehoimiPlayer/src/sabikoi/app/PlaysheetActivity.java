package sabikoi.app;

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
  	}
  	return true;
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
  }

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
	{
		Intent intent = new Intent(this,sabikoi.app.PlaysheetBrowser.class);
		intent.putExtra("sheetname", playsheetname.get(arg2));
		startActivity(intent);
	}
}
