package sabikoi.app;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements OnItemClickListener{
	ScrollView mainView;
	ListView commandList;

  @Override
  public void onCreate(Bundle bundle) 
  {
      super.onCreate(bundle);
      
      List<CommandListArrayItem> items = new ArrayList<CommandListArrayItem>();
      
      Bitmap icon=null;
      items.add(new CommandListArrayItem(icon, "ファイルブラウザ"));
      items.add(new CommandListArrayItem(icon, "プレイシート"));
      items.add(new CommandListArrayItem(icon, "つづきから"));
      
      ListAdapter adapter = new CommandListArrayAdapter(this,R.layout.commandlist,items);
      commandList = new ListView(this);
      commandList.setAdapter(adapter);
      commandList.setFocusable(true);
      commandList.setScrollingCacheEnabled(false);
      commandList.setOnItemClickListener(this);
      commandList.setVerticalFadingEdgeEnabled(false);      
      
      mainView = new ScrollView(this);
//      mainView.addView(commandList);
 //     setContentView(mainView);
    setContentView(commandList);
  }

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if(arg0 == commandList)
		{
			if(arg2 == 0)
			{
				Intent intent = new Intent(this,sabikoi.app.FileBrowser.class);
				intent.putExtra("currentpath", Environment.getExternalStorageDirectory().getAbsolutePath() + "/musics");
				startActivity(intent);
			}
			else if(arg2 == 1)
			{
				Intent intent = new Intent(this,sabikoi.app.PlaysheetActivity.class);
				startActivity(intent);
			}
		}
	}
}
