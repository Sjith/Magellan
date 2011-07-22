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
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.content.DialogInterface;

public class MainActivity extends Activity 
{

	private LinearLayout m_breadcrumb;
	private ListView m_listView;
	String m_currentPath;
	String m_copied;
	
	private String m_convert(long size)
	{
		String ret;
		
		if(size/1000000000 > 0) // superior to 1 GB
			ret = String.valueOf((double) Math.round((((double)size) / ((double)1000000000))*10) / 10 ) + " " + getResources().getString(R.string.unit_gigabyte);  
		else if(size/1000000 > 0) // superior to 1 MB
			ret = String.valueOf((double) Math.round((((double)size) / ((double)1000000))*10) / 10 ) + " " + getResources().getString(R.string.unit_megabyte);  
		else if(size/1000 > 0) // superior to 1KB
			ret = String.valueOf((double) Math.round((((double)size) / ((double)1000))*10) / 10 ) + " " + getResources().getString(R.string.unit_kilobyte);  
		else
			ret = String.valueOf(size) + " " + getResources().getString(R.string.unit_byte);
		
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

	private void dialog(String title, String message) {
		AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
		adb.setTitle(title);
		adb.setMessage(message);
		adb.setPositiveButton(getResources().getString(R.string.error_dialog_ok), null);
		adb.show();
	}
	private void dialog(String title, int message) {
		dialog(title, getResources().getString(message));
	}
	private void dialog(int title, String message) {
		dialog(getResources().getString(title), message);
	}
	private void dialog(int title, int message) {
		dialog(getResources().getString(title), getResources().getString(message));
	}
	private void dialog(Pair<Integer, String> title, int message) {
		dialog(String.format(getResources().getString(title.getFirst()), title.getSecond()), getResources().getString(message));
	}
	private void dialog(int title , Pair<Integer, String> message) {
		dialog(getResources().getString(title), String.format(getResources().getString(message.getFirst()), message.getSecond()));
	}
	private void dialog(Pair<Integer, String>title , Pair<Integer, String> message) {
		dialog(String.format(getResources().getString(title.getFirst()), title.getSecond()), String.format(getResources().getString(message.getFirst()), message.getSecond()));
	}
	private void dialog(Pair<Integer, String> title, String message) {
		dialog(String.format(getResources().getString(title.getFirst()), title.getSecond()), message);
	}
	private void dialog(String title , Pair<Integer, String> message) {
		dialog(title, String.format(getResources().getString(message.getFirst()), message.getSecond()));
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
		populateBreadcrumb();

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
			
			String descr = String.format(getResources().getString(R.string.mainactivity_folder_description, folder.count(), folder.count()));
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
			
			String descr = String.format(getResources().getString(R.string.mainactivity_file_description), m_convert(file.length()));
			map.put("descr", descr);
			
			map.put("img", String.valueOf(R.drawable.file));
			itemList.add(map);
		}		

		SimpleAdapter adapter = new SimpleAdapter(this.getBaseContext(), itemList, R.layout.item, new String[] {"img", "title", "descr"}, new int[] {R.id.img, R.id.title, R.id.descr});
		m_listView.setAdapter(adapter);
	}

	public void populateBreadcrumb() {
		int numberOfChilds = m_breadcrumb.getChildCount();
		for (int i=0; i < numberOfChilds; i++) {
			m_breadcrumb.removeViewAt(0); // At each iteration, the first element will be removed
		}
		Button button;

		class OnBreadcrumbClick implements OnClickListener {
			MainActivity m_context;
			String m_path;

			public OnBreadcrumbClick(MainActivity context, String path) {
				m_context = context;
				m_path = path;
			}

			@Override
			public void onClick(View button) {
				m_context.setCurrentPath(m_path);
			}
		}

		String displayedPath = "/"; // Path that has already been turned into button
		button = new Button(this);
		button.setOnClickListener(new OnBreadcrumbClick(this, "/"));
		button.setText("/");
		m_breadcrumb.addView(button);
		for (String folder : m_currentPath.split("/")) {
			if (folder.length() == 0) {
				continue;
			}
			displayedPath += folder + "/";
			button = new Button(this);
			button.setOnClickListener(new OnBreadcrumbClick(this, displayedPath));
			button.setText(folder);
			m_breadcrumb.addView(button);
		}
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
		setContentView(R.layout.main);
		m_listView = (ListView) findViewById(R.id.main_elements_list);
		registerForContextMenu(m_listView);
		m_breadcrumb = (LinearLayout) findViewById(R.id.main_breadcrumb);

		setCurrentPath("/sdcard");
		populateBreadcrumb();

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
						dialog(clickedFile.getName(), R.string.error_no_permissions_explore);
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
		
		String type = directory ? getResources().getString(R.string.folder) : getResources().getString(R.string.file);
		
		if(!file.canWrite())
		{
			dialog(R.string.error_dialog_title, new Pair<Integer, String>(R.string.error_no_permissions_create, type));
			return;
		}
		
		/* prompt the name of the new file */
        LayoutInflater factory = LayoutInflater.from(this);
        final View alertDialogView = factory.inflate(R.layout.dialogbox, null);
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setView(alertDialogView);
        adb.setTitle(String.format(getResources().getString(R.string.mainactivity_dialog_prompt_name_title), type));
        
        if(directory)
        {
        	adb.setPositiveButton(getResources().getString(R.string.error_dialog_ok), new DialogInterface.OnClickListener() 
        	{
        		public void onClick(DialogInterface dialog, int which) 
        		{
        			Folder currentFolder = new Folder(m_currentPath);
        			EditText et = (EditText)alertDialogView.findViewById(R.id.EditText1);
        			String fileName = et.getText().toString();
        			if(currentFolder.contains(fileName))
        			{
        				dialog(R.string.error_dialog_title, R.string.error_duplicated_name);
        				return;
        			}
        			else if(fileName.contains("/"))
        			{
        				dialog(R.string.error_dialog_title, new Pair<Integer, String>(R.string.error_invalid_character_in_filename, "/"));
        				return;
        			}
        			else
        			{
        				File newFile = new File(m_currentPath + "/" + fileName);
        				if(!newFile.mkdir()) 
        				{
        					dialog(R.string.mainactivity_dialog_create_folder_title, R.string.error_unknown);
        					return;
        				}
        				refreshView();
        			} 
        		} // on click
        	}); // on click listener
        	
        }
        else
        {
        	adb.setPositiveButton(getResources().getString(R.string.error_dialog_ok), new DialogInterface.OnClickListener() 
        	{
        		public void onClick(DialogInterface dialog, int which) 
        		{
        			Folder currentFolder = new Folder(m_currentPath);
        			EditText et = (EditText)alertDialogView.findViewById(R.id.EditText1);
        			String fileName = et.getText().toString();
        			if(currentFolder.contains(fileName))
        			{
        				dialog(R.string.error_dialog_title, R.string.error_duplicated_name);
        				return;
        			}
        			else if(fileName.contains("/"))
        			{
        				dialog(R.string.error_dialog_title, new Pair<Integer, String>(R.string.error_invalid_character_in_filename, "/"));
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
        					dialog(R.string.error_dialog_title, new Pair<Integer, String>(R.string.error_unknown_with_message, e.getMessage()));
        					return;
        				}
        				refreshView();
        			} 
        		} // on click
        	}); // on click listener
        } // file
        
        adb.setNegativeButton(getResources().getString(R.string.error_dialog_cancel), new DialogInterface.OnClickListener() {
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
			dialog(file.getName(), R.string.error_no_permissions_delete_folder);
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
					dialog(child.getName(), R.string.error_no_permissions_delete_folder);
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
						dialog(child.getName(), R.string.error_unknown);
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
		String type = file.isDirectory() ? getResources().getString(R.string.folder) : getResources().getString(R.string.file);;
		
		if(!file.canWrite())
		{
			dialog(file.getName(), new Pair<Integer, String>(R.string.error_no_permissions_delete, type));
			return;			
		}
		if(file.isDirectory())
		{
			if(!recursiveRmdir(file.getAbsolutePath()))
			{
				dialog(R.string.error_dialog_title, R.string.error_unknown);
				return;					
			}
		}
		else
		{
			if(!file.delete())
			{	
				dialog(R.string.error_dialog_title, R.string.error_unknown);
				return;			
			}
		}
	    
	    refreshView();
	}


	public void details(String path)
	{
		new AsyncDetailsDisplayer(MainActivity.this).execute(path);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) 
	{
	    // Handle item selection
	    switch (item.getItemId()) 
	    {
	    case R.id.mainactivity_menu_create_file:
	    	newFile(false);
	    	return true;
	    case R.id.mainactivity_menu_create_folder:
	    	newFile(true);
	    	return true;
	    case R.id.mainactivity_menu_quit:
	        finish();
	        return true;
	    case R.id.mainactivity_menu_details:
	    	details(m_currentPath);
	    	return true;
		case R.id.mainactivity_menu_paste:
			if(m_copied.compareTo("") == 0)
			{
				dialog(R.string.error_dialog_title, R.string.error_unknown);
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
		case R.id.mainactivity_filecontext_delete:
		case R.id.mainactivity_context_delete:
			delete(clickedFile.getName());
			return true;
		case R.id.mainactivity_filecontext_details:
		case R.id.mainactivity_context_details:
			details(clickedFile.getAbsolutePath());
			return true;
		case R.id.mainactivity_filecontext_copy:
		case R.id.mainactivity_context_copy:
			m_copied = clickedFile.getAbsolutePath();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
}
