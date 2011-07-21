package com.quasarlab.magellan;

import java.io.File;
import java.util.Vector;

public class Folder extends File
{

	public Folder(String path) 
	{
		super(path);
	}
	
	public Vector<String> list(boolean folders)
	{
		Vector<String> ret = new Vector<String>();
		
		if(this.isDirectory())
		{
			String[] all = this.list();
			for(int i = 0; i < all.length; i++)
			{
				String name = all[i];
				File child = new File(this.getAbsolutePath() + "/" + all[i]);
				if((child.isDirectory() && folders) || (!child.isDirectory() && !folders))
					ret.add(name);
			}
		}
		
		return ret;
	}
	
	public boolean contains(String name)
	{
		String[] childs = this.list();
		for(int i = 0; i < childs.length; i++)
		{
			if(childs[i].compareTo(name) == 0)
				return true;
		}
		
		/* not found */
		return false; 
	}
	
	public int count()
	{
		if(this.canRead() && this.canExecute())
			return list().length;
		else
			return 0;
	}
	
	public int recursiveCount()
	{
		int count = 0;
		
		if(!this.canRead() || !this.canExecute())
			return count;

		Vector<String> subFolders = this.list(true);
		for(int i = 0; i < subFolders.size(); i++)
		{
			Folder subFolder = new Folder(this.getAbsolutePath() + "/" + subFolders.get(i));
			count += (subFolder.recursiveCount());
		}
		
		count += this.count();
		
		return count;
	}

}
