package com.quasarlab.magellan;

import java.util.Vector;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;

import com.quasarlab.magellan.R;
import com.quasarlab.magellan.CopyOperation;
import com.quasarlab.magellan.Folder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
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
	String m_copied;
	
	private String m_convert(long size)
	{
		String ret;
		
		if(size/1000000000 > 0) // superior to 1 GB
			ret = String.valueOf((double) Math.round((((double)size) / ((double)1000000000))*10) / 10 ) + " GB";  
		else if(size/1000000 > 0) // superior to 1 MB
			ret = String.valueOf((double) Math.round((((double)size) / ((double)1000000))*10) / 10 ) + " MB";  
		else if(size/1000 > 0) // superior to 1KB
			ret = String.valueOf((double) Math.round((((double)size) / ((double)1000))*10) / 10 ) + " KB";  
		else
			ret = String.valueOf(size) + " Bytes";
		
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
		Folder currentFolder = new Folder(m_currentPath);

		setTitle( (m_currentPath.compareTo("/") == 0) ? "/" : currentFolder.getName() );

		/* sort files and folders separately */
		Vector<String> filesList, foldersList;
		filesList = currentFolder.list(false); 
		foldersList = currentFolder.list(true);

		/* sort both alphabetically */
		m_sort(filesList);
		m_sort(foldersList);

		/* put them in our item model, folders first */
		HashMap<String, String> map;
		ArrayList<HashMap<String, String> > itemList = new ArrayList<HashMap<String,String>>();

		for(int i = 0; i < foldersList.size(); i++)
		{
			String name = foldersList.get(i);
			Folder folder = new Folder(m_currentPath + "/" + name);

			map = new HashMap<String,String>();
			map.put("title", name);
			
			String descr = "Directory, " + folder.count() + " elements";
			map.put("descr", descr);
			
			map.put("img", String.valueOf(R.drawable.folder));
			itemList.add(map);
		}

		/* then simple files */
		for(int i = 0; i < filesList.size(); i++)
		{
			String name = filesList.get(i);
			File file = new File(m_currentPath + "/" + name);

			map = new HashMap<String,String>();
			map.put("title", name);
			
			String descr = "File, " + m_convert(file.length());
			map.put("descr", descr);
			
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
        			Folder currentFolder = new Folder(m_currentPath);
        			EditText et = (EditText)alertDialogView.findViewById(R.id.EditText1);
        			String fileName = et.getText().toString();
        			if(currentFolder.contains(fileName))
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
        			Folder currentFolder = new Folder(m_currentPath);
        			EditText et = (EditText)alertDialogView.findViewById(R.id.EditText1);
        			String fileName = et.getText().toString();
        			if(currentFolder.contains(fileName))
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

	private class AsyncDetailsDisplayer extends AsyncTask<String, Void, Void> {
		protected int count = 1;
		ProgressDialog dialog;
		File file;

		protected void onPreExecute() {
			dialog = ProgressDialog.show(MainActivity.this, "", "Getting details...", true);
		}
		protected Void doInBackground(String... args) {
			assert (args.length == 1);
			String path = args[0];
			file = new File(path);
			if(file.isDirectory())
			{
				Folder folder = new Folder(path);
				count = folder.recursiveCount();
			}
			return null;
		}
		protected void onPostExecute(Void foo) {
			try {
				dialog.dismiss();
			}
			catch (IllegalArgumentException e) {
				// Window has leaked
			}
			AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
			adb.setTitle(file.getName());
			adb.setMessage("File path : " + file.getAbsolutePath() + "\n" +
							"File size : " + file.length() + "\n" +
							"Items count : " + count + "\n");

			adb.setPositiveButton("Ok", null);
			adb.show();
		}
	}
	public void details(String path)
	{
		new AsyncDetailsDisplayer().execute(path);
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
		case R.id.action_paste:
			if(m_copied.compareTo("") == 0)
			{
				AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
				adb.setTitle("Exception");
				adb.setMessage("An exception occured.");
				adb.setPositiveButton("Ok", null);
				adb.show(); 
				return false;
			}
			File fileToCopy = new File(m_copied);
			File toCopy[] = { fileToCopy };
			new CopyOperation(MainActivity.this, toCopy).execute(m_currentPath);
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
			m_copied = clickedFile.getAbsolutePath();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
}
