package com.quasarlab.magellan;

import java.util.LinkedList;
import java.util.Vector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.quasarlab.magellan.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.content.DialogInterface;

public class MainActivity extends Activity 
{

	private ListView m_listView;
	String m_currentPath;
	
	private boolean contains(File file, String name)
	{
		String[] childs = file.list();
		for(int i = 0; i < childs.length; i++)
		{
			if(childs[i].compareTo(name) == 0)
				return true;
		}
		
		/* not found */
		return false;
	}


	private Vector<String> m_list(File f, boolean folders)
	{
		String[] all = f.list();
		Vector<String> ret = new Vector<String>();

		for(int i = 0; i < all.length; i++)
		{
			String name = all[i];
			File child = new File(f.getAbsolutePath() + "/" + all[i]);
			if((child.isDirectory() && folders) || (!child.isDirectory() && !folders))
				ret.add(name);
		}

		return ret;
	}

	private void m_sort(Vector<String> unsorted)
	{
		for(int i = 0; i < unsorted.size(); i++)
		{
			String key = unsorted.get(i);
			int j = i-1;
			while(j >= 0 && (unsorted.get(j).toLowerCase().compareTo(key.toLowerCase()) > 0))
			{
				unsorted.set(j+1, unsorted.get(j));
				j--;
			}
			unsorted.set(j+1, key);
		}
	}

	public void setCurrentPath(String path) 
	{
		m_currentPath = path;
		refreshView();
	}

	public void refreshView()
	{
		File file = new File(m_currentPath);

		setTitle( (m_currentPath.compareTo("/") == 0) ? "/" : file.getName() );

		/* sort files and folders separately */
		Vector<String> files, folders;
		files = m_list(file, false);
		folders = m_list(file, true);

		/* sort both alphabetically */
		m_sort(files);
		m_sort(folders);

		/* put them in our item model, folders first */
		HashMap<String, String> map;
		ArrayList<HashMap<String, String> > itemList = new ArrayList<HashMap<String,String>>();

		for(int i = 0; i < folders.size(); i++)
		{
			String name = folders.get(i);

			map = new HashMap<String,String>();
			map.put("title", name);
			map.put("descr", "Directory");
			map.put("img", String.valueOf(R.drawable.folder));
			itemList.add(map);
		}

		/* then simple files */
		for(int i = 0; i < files.size(); i++)
		{
			String name = files.get(i);

			map = new HashMap<String,String>();
			map.put("title", name);
			map.put("descr", "File");
			map.put("img", String.valueOf(R.drawable.file));
			itemList.add(map);
		}		

		SimpleAdapter adapter = new SimpleAdapter(this.getBaseContext(), itemList, R.layout.item, new String[] {"img", "title", "descr"}, new int[] {R.id.img, R.id.title, R.id.descr});
		m_listView.setAdapter(adapter);
	}

	@Override
	public void onBackPressed()
	{
		if(m_currentPath.compareTo("/") != 0)
		{
			File currentFile = new File(m_currentPath);
			setCurrentPath(currentFile.getParent());
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		m_listView = new ListView(this);
		setContentView(m_listView);
		registerForContextMenu(m_listView);

		setCurrentPath("/sdcard");			

		m_listView.setOnItemClickListener(new OnItemClickListener() 
		{
			@SuppressWarnings("unchecked")
			public void onItemClick(AdapterView<?> a, View v, int position, long id) 
			{ 
				HashMap<String,String> map = (HashMap<String,String>) m_listView.getItemAtPosition(position);
				File clickedFile = new File(m_currentPath + "/" + map.get("title"));
				if(clickedFile.isDirectory())
				{
					// do we have permissions to explore this directory ?
					if(clickedFile.canExecute() && clickedFile.canRead())
					{
						setCurrentPath(clickedFile.getAbsolutePath());
					}
					else
					{
						AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
						adb.setTitle(clickedFile.getName());
						adb.setMessage("You don't have permissions to explore this directory. It may be a system file.");
						adb.setPositiveButton("Ok", null);
						adb.show();
					}
				}
				else
				{
					// open the file 

				}
			}
		});							
	}
	
	@SuppressWarnings("unchecked")
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		/* display the menu */
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
	
		/* get the position of the item that was clicked */
		int position = ((AdapterContextMenuInfo)menuInfo).position;
		
		/* did the user clicked on a directory or a file ? get the file clicked */
		HashMap<String,String> map = (HashMap<String,String>) m_listView.getItemAtPosition(position);
		File clickedFile = new File(m_currentPath + "/" + map.get("title"));
		menu.setHeaderTitle(clickedFile.getName());
		
		/* use the appropriate menu : simple files have a share action */
		if(clickedFile.isDirectory())
			inflater.inflate(R.menu.context, menu);
		else
			inflater.inflate(R.menu.filecontext, menu);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}
	
	
	public void newFile(boolean directory)
	{
		File file = new File(m_currentPath);
		
		String type = directory ? "folder" : "file";
		
		if(!file.canWrite())
		{
			AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
			adb.setTitle("New " +type);
			adb.setMessage("You don't have permissions to create a " + type + " in this directory. It may be a system directory.");
			adb.setPositiveButton("Ok", null);
			adb.show();
			return;
		}
		
		/* prompt the name of the new file */
        LayoutInflater factory = LayoutInflater.from(this);
        final View alertDialogView = factory.inflate(R.layout.dialogbox, null);
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setView(alertDialogView);
        adb.setTitle("Choose a name for the new " + type + " :");
        
        if(directory)
        {
        	adb.setPositiveButton("OK", new DialogInterface.OnClickListener() 
        	{
        		public void onClick(DialogInterface dialog, int which) 
        		{
        			File file = new File(m_currentPath);
        			EditText et = (EditText)alertDialogView.findViewById(R.id.EditText1);
        			String fileName = et.getText().toString();
        			if(contains(file, fileName))
        			{
        				AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
        				adb.setTitle("New folder");
        				adb.setMessage("A file with that name already exists here.");
        				adb.setPositiveButton("Ok", null);
        				adb.show();         
        				return;
        			}
        			else if(fileName.contains("/"))
        			{
        				AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
        				adb.setTitle("New folder");
        				adb.setMessage("You can't user the character / on a file name.");
        				adb.setPositiveButton("Ok", null);
        				adb.show();         
        				return;
        			}
        			else
        			{
        				File newFile = new File(m_currentPath + "/" + fileName);
        				if(!newFile.mkdir()) 
        				{
        					AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
        					adb.setTitle("New folder");
        					adb.setMessage("An exception occured.");
        					adb.setPositiveButton("Ok", null);
        					adb.show();         
        					return;
        				}
        				refreshView();
        			} 
        		} // on click
        	}); // on click listener
        	
        }
        else
        {
        	adb.setPositiveButton("OK", new DialogInterface.OnClickListener() 
        	{
        		public void onClick(DialogInterface dialog, int which) 
        		{
        			File file = new File(m_currentPath);
        			EditText et = (EditText)alertDialogView.findViewById(R.id.EditText1);
        			String fileName = et.getText().toString();
        			if(contains(file, fileName))
        			{
        				AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
        				adb.setTitle("New file");
        				adb.setMessage("A file with that name already exists here.");
        				adb.setPositiveButton("Ok", null);
        				adb.show();         
        				return;
        			}
        			else if(fileName.contains("/"))
        			{
        				AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
        				adb.setTitle("New file");
        				adb.setMessage("You can't user the character / on a file name.");
        				adb.setPositiveButton("Ok", null);
        				adb.show();         
        				return;
        			}
        			else
        			{
        				File newFile = new File(m_currentPath + "/" + fileName);
        				try 
        				{
        					newFile.createNewFile();
        				} 
        				catch(IOException e) 
        				{
        					AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
        					adb.setTitle("New file");
        					adb.setMessage("An exception occured. Error : " + e.getMessage());
        					adb.setPositiveButton("Ok", null);
        					adb.show();         
        					return;
        				}
        				refreshView();
        			} 
        		} // on click
        	}); // on click listener
        } // file
        
        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	return;
            }
        });
        adb.show();
	}
	
	public boolean recursiveRmdir(String absPath)
	{
		File file = new File(absPath);
		if(!file.isDirectory())
			return false;
		
		if(!file.canExecute() || !file.canRead())
		{
			AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
			adb.setTitle(file.getName());
			adb.setMessage("You don't have permissions to delete the contents of this folder. It may be a system folder.");
			adb.setPositiveButton("Ok", null);
			adb.show();
			return false;		
		}
		else
		{
			// delete contents
			String[] childs = file.list();
			for(int i = 0; i < childs.length; i++)
			{
				File child = new File(absPath + "/" + childs[i]);
				if(!child.canWrite())
				{
					AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
					adb.setTitle(child.getName());
					adb.setMessage("You don't have permissions to delete this file. It may be a system file.");
					adb.setPositiveButton("Ok", null);
					adb.show();
					return false;		
				}
				if(child.isDirectory())
				{
					if(!recursiveRmdir(child.getAbsolutePath()))
						return false;
			    }
				else
				{
					if(!child.delete())
					{
						AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
						adb.setTitle(child.getName());
						adb.setMessage("An error occured and this file could not be deleted");
						adb.setPositiveButton("Ok", null);
						adb.show();
						return false;							
					}
				}		
			}
			
			// at this time the directory should be empty
			return file.delete();
		}	
	}
	
	public void delete(String name)
	{
		File file = new File(m_currentPath + "/" + name);
		String type = file.isDirectory() ? "folder" : "file";
		
		if(!file.canWrite())
		{
			AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
			adb.setTitle(file.getName());
			adb.setMessage("You don't have permissions to delete this a " + type + ". It may be a system " + type + ".");
			adb.setPositiveButton("Ok", null);
			adb.show();
			return;			
		}
		if(file.isDirectory())
		{
			if(!recursiveRmdir(file.getAbsolutePath()))
			{
				AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
				adb.setTitle("Exception");
				adb.setMessage("An error occured, and this folder could not be deleted.");
				adb.setPositiveButton("Ok", null);
				adb.show();         
				return;					
			}
		}
		else
		{
			if(!file.delete())
			{	
				AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
				adb.setTitle("Exception");
				adb.setMessage("An exception occured, and this " + type + " could not be deleted.");
				adb.setPositiveButton("Ok", null);
				adb.show();         
				return;			
			}
		}
	    
	    refreshView();
	}
	
	public boolean copyFile(String sourcePath, String destPath)
	{
		File source = new File(sourcePath);
		File dest = new File(destPath);
		
		String name = source.getName();
		int i = 1;
		while(contains(dest,name))
		{
			name = name + "." + String.valueOf(i);
			i++;
		}
		
		if(!source.exists())
		{
			AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
			adb.setTitle(source.getName());
			adb.setMessage("The source file does not exists. Aborting.");
			adb.setPositiveButton("Ok", null);
			adb.show();         
			return false;						
		}			
		
		if(!source.canRead())
		{
			AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
			adb.setTitle(source.getName());
			adb.setMessage("The source file could not be read. Aborting.");
			adb.setPositiveButton("Ok", null);
			adb.show();         
			return false;						
		}
		
		if(!dest.canWrite())
		{
			AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
			adb.setTitle(dest.getName());
			adb.setMessage("Impossible to create files in this repository. Check it exists and you have enough permissions on it.");
			adb.setPositiveButton("Ok", null);
			adb.show();         
			return false;						
		}
		
		
		File newFile = new File(destPath + "/" + name);
		try
		{
			newFile.createNewFile();
		}
		catch(IOException e)
		{
			AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
			adb.setTitle(newFile.getName());
			adb.setMessage("An error occured while creating the new file : " + e.getMessage() + ". Aborting.");
			adb.setPositiveButton("Ok", null);
			adb.show();         
			return false;	
		}
		
		InputStream in;
		OutputStream out;
		try
		{
			in = new FileInputStream(source);
			out = new FileOutputStream(newFile);
		}
		catch(FileNotFoundException e)
		{
			AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
			adb.setTitle(newFile.getName());
			adb.setMessage("An error occured while creating the new file : " + e.getMessage() + ". Aborting.");
			adb.setPositiveButton("Ok", null);
			adb.show();   
			newFile.delete();
			return false;			
		}

	    // Transfer bytes from in to out
	    byte[] buf = new byte[1024];
	    int len;
	    
	    try
	    {
	    	while ((len = in.read(buf)) > 0) 
	    		out.write(buf, 0, len);
		    in.close();
		    out.close();
	    }
	    catch(IOException e)
	    {
			AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
			adb.setTitle(newFile.getName());
			adb.setMessage("An error occured while creating the new file : " + e.getMessage() + ". Aborting.");
			adb.setPositiveButton("Ok", null);
			adb.show();     
			newFile.delete();
			return false;	
	    }
		
	    return true;
	}
	
	@SuppressWarnings("unchecked")
	public void copyDirectory(String sourcePath, String destPath)
	{
		Queue< Pair<String,String> > queue = new LinkedList< Pair<String,String> >();
		queue.add( new Pair(sourcePath, destPath) );
		
		while(!queue.isEmpty())
		{
			Pair<String,String> p = queue.poll();
			
			File source = new File(p.getFirst());
			File dest = new File(p.getSecond());
			
			if(!source.exists() || !source.isDirectory())
				continue;
			
			if(!source.canRead() || !source.canExecute())
			{
				AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
				adb.setTitle(source.getName());
				adb.setMessage("The source directory could not be read. Aborting.");
				adb.setPositiveButton("Ok", null);
				adb.show();         
				return;						
			}
			
			if(!dest.exists())
			{
				if(!dest.mkdirs())
				{
					AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
					adb.setTitle(source.getName());
					adb.setMessage("The directory " + dest.getName() + " could not be created. Check permissions. Aborting.");
					adb.setPositiveButton("Ok", null);
					adb.show();         
					return;
				}
			}
				
			String[] files = source.list();
			for(int i = 0; i < files.length; i++)
			{
				String srcName = p.getFirst() + "/" + files[i];
				String destName = p.getSecond() + "/" + files[i];
				
				File file = new File(srcName);
				if(file.isDirectory())
					queue.add(new Pair<String,String>(srcName,destName));
				else
				{
					if(!copyFile(srcName, p.getSecond()))
						return;
				}
			}				
		}
	}
	
	public void copy(String name)
	{
		ProgressDialog dialog = new ProgressDialog(MainActivity.this);
		dialog.setTitle("Copy");
		dialog.setMessage("Copying files. Please wait.");
		dialog.setIndeterminate(true);
		dialog.show();
				
		File file = new File(name);
		if(file.isDirectory())
		{
			File dest = new File(m_currentPath);
			String fname = file.getName();
			
			int i = 1;
			while(contains(dest, fname))
			{
				fname = fname + "." + String.valueOf(i);
				i++;
			}
			
			copyDirectory(name, m_currentPath + "/" + fname);
		}
		else
			copyFile(name, m_currentPath);
		
		dialog.dismiss();
		
		refreshView();
	}
	
	/*
	public void paste()
	{
		if(m_copied.compareTo("") == 0)
			return;
		
		File file = new File(m_currentPath);
		File newFile = new File(m_copied);
		
		if(contains(file, newFile.getName()))
		{
			/* prompt the name of the new file 
			AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
	        adb.setTitle("Error");
	        adb.setMessage("A file with that name already exists.");
			adb.setPositiveButton("Ok", null);
			adb.show();         
	        return;	        
		}
		
		try 
		{
			newFile.createNewFile();
		} 
		catch(IOException e) 
		{
			AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
			adb.setTitle("New file");
			adb.setMessage("An exception occured. Error : " + e.getMessage());
			adb.setPositiveButton("Ok", null);
			adb.show();         
			return;
		}
		
		
	}
	*/

	
	public void details(String path)
	{
		File file = new File(path);
		
		AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
		adb.setTitle(file.getName());
		adb.setMessage("File path : " + file.getAbsolutePath() + "\n" +
					   "File size : " + file.length() + "\n");
		adb.setPositiveButton("Ok", null);
		adb.show();
	}
	
	public boolean onOptionsItemSelected(MenuItem item) 
	{
	    // Handle item selection
	    switch (item.getItemId()) 
	    {
	    case R.id.action_file:
	    	newFile(false);
	    	return true;
	    case R.id.action_folder:
	    	newFile(true);
	    	return true;
	    case R.id.action_quit:
	        finish();
	        return true;
	    case R.id.action_prop:
	    	details(m_currentPath);
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean onContextItemSelected(MenuItem item) 
	{
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		HashMap<String,String> map = (HashMap<String,String>) m_listView.getItemAtPosition(info.position);
		File clickedFile = new File(m_currentPath + "/" + map.get("title"));
		
		switch (item.getItemId()) 
		{
		case R.id.action_delete:
			delete(clickedFile.getName());
			return true;
		case R.id.action_prop:
			details(clickedFile.getAbsolutePath());
			return true;
		case R.id.action_copy:
			copy(clickedFile.getAbsolutePath());
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
}
