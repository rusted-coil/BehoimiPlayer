package sabikoi.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//データベースヘルパー
public class DBHelper extends SQLiteOpenHelper
{
	public DBHelper(Context context) {
		super(context, "playsheetlist.db", null, 1);
	}

	//データベースの作成
	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		db.execSQL("create table if not exists sheets(name text primary key)");
		db.execSQL("create table if not exists sheet(sheetname text, path text, name text, playcount integer, skipcount integer)");
	}

	//データベースの更新
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		db.execSQL("drop table if exists sheets");
		db.execSQL("drop table if exists sheet");
		onCreate(db);
	}  	
}
