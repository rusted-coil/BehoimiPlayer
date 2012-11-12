package sabikoi.app;

import android.graphics.Bitmap;

public class FileListArrayItem {
	Bitmap icon;
	public String text;
	public boolean isDirectory;
	
	public FileListArrayItem(Bitmap icon, String text, boolean isDirectory)
	{
		this.icon = icon;
		this.text = text;
		this.isDirectory = isDirectory;
	}

}
