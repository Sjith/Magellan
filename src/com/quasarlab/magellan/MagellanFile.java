package com.quasarlab.magellan;

import java.io.File;

import android.webkit.MimeTypeMap;

public class MagellanFile extends File
{
	public MagellanFile(String path) 
	{
		super(path);
	}
	
	public String suffix()
	{
		String name = getName();
				
		int dot_pos = name.lastIndexOf(".");
		if(dot_pos >= 0)
			return name.substring(dot_pos);
		else
			return "";
	}
	
//	public String completeSuffix()
//	{
//		String path = getPath();
//		
//		if(!path.contains("."))
//			return "";
//		
//		String[] parts = path.split(".");
//		String ret = new String();
//		for(int i = 1; i < parts.length; i++)
//		{
//			ret += ("." + parts[i]); 
//		}
//		
//		return ret.substring(1); // remove the first .
//	}
	
	public String mimeType()
	{
		if(isDirectory())
			return "inode/directory";
		
		String suffix = suffix().substring(1); // remove the first .
		String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
		if(type != null)
			return type;
		
		// try with lowercase
		type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix.toLowerCase());
		if(type != null)
			return type;
		
		return "*/*";
	}
	
}
