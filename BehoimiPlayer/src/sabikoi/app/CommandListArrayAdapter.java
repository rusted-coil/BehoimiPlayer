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
	
	//1�v�f���̃r���[
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		//�r���[�̐���
		View view;
		if(convertView!=null)
			view=convertView;
		else
			view=inflater.inflate(layoutID, null);
		
		//�A�C�e���̎擾
		CommandListArrayItem item = items.get(position);
		
		//�A�C�R��
		ImageView imageView=(ImageView)view.findViewWithTag("icon");
		imageView.setImageDrawable(new BitmapDrawable(item.icon));
		
		//�e�L�X�g
		TextView textView;
		textView = (TextView)view.findViewWithTag("text");
		textView.setText(item.text);
		
		return view;
	}
}
