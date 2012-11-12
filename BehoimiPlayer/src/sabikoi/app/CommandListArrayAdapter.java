package sabikoi.app;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CommandListArrayAdapter extends ArrayAdapter<CommandListArrayItem> 
{
	int layoutID;
	List<CommandListArrayItem> items;
	LayoutInflater inflater;

	public CommandListArrayAdapter(Context context, int layoutID, List<CommandListArrayItem> items)
	{
		super(context,layoutID,items);
		this.layoutID = layoutID;
		this.items = items;
		this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	//1要素分のビュー
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		//ビューの生成
		View view;
		if(convertView!=null)
			view=convertView;
		else
			view=inflater.inflate(layoutID, null);
		
		//アイテムの取得
		CommandListArrayItem item = items.get(position);
		
		//アイコン
		ImageView imageView=(ImageView)view.findViewWithTag("icon");
		imageView.setImageDrawable(new BitmapDrawable(item.icon));
		
		//テキスト
		TextView textView;
		textView = (TextView)view.findViewWithTag("text");
		textView.setText(item.text);
		
		return view;
	}
}
