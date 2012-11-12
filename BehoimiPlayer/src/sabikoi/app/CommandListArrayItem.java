package sabikoi.app;

import android.graphics.Bitmap;

public class CommandListArrayItem {
	Bitmap icon;
	public String text;
	
	public CommandListArrayItem(Bitmap icon, String text)
	{
		this.icon = icon;
		this.text = text;
	}
}
