package sabikoi.app;

import java.util.List;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileListArrayAdapter extends ArrayAdapter<FileListArrayItem>{
	int layoutID;
	List<FileListArrayItem> items;
	LayoutInflater inflater;

	public FileListArrayAdapter(Context context, int layoutID, List<FileListArrayItem> items)
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
		FileListArrayItem item = items.get(position);
		
		//�A�C�R��
		ImageView imageView=(ImageView)view.findViewWithTag("icon");
		if(item.isDirectory)
			imageView.setImageResource(R.drawable.folder);
		else
			imageView.setImageResource(R.drawable.music);
		
		//�e�L�X�g
		TextView textView;
		textView = (TextView)view.findViewWithTag("text");
		if(item.isDirectory)
			textView.setTextColor(Color.YELLOW);
		else
			textView.setTextColor(Color.WHITE);
		textView.setText(item.text);
		
		return view;
	}

}
