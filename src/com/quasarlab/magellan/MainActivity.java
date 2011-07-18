package com.quasarlab.magellan;

import java.util.Vector;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;

import com.quasarlab.magellan.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.View;
import android.view.ContextMenu;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity 
{

	private ListView m_listView;
	String m_currentPath;

	private Vector<String> m_list(File f, boolean folders)
	import android.view.ContextMenu;
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
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);
	}
}
