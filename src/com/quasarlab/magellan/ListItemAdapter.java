package com.quasarlab.magellan;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ListItemAdapter extends BaseAdapter
{
	private Context m_context;
	private List<MagellanFile> m_items = new ArrayList<MagellanFile>();
	
	public ListItemAdapter(Context context)
	{
		m_context = context;
	}
	
	public void addItem(MagellanFile f)
	{
		m_items.add(f);
	}
	
	public void setItemList(List<MagellanFile> list)
	{
		m_items = list;
	}
	
	public int getCount() 
	{
		return m_items.size();
	}

	public Object getItem(int arg0) 
	{
		return m_items.get(arg0);
	}

	public long getItemId(int arg0) 
	{
		return arg0;
	}

	public List<MagellanFile> items()
	{
		return m_items;
	}
	
	public View getView(int arg0, View arg1, ViewGroup arg2) 
	{
		ListItem it;
		if(arg1 == null)
			it = new ListItem(m_context, m_items.get(arg0));
		else
			it = (ListItem) arg1;
		
		MagellanFile f = m_items.get(arg0);
		it.setTitle(f.getName());
		
		it.setDescr( f.isDirectory() ? 
			String.format(m_context.getResources().getString(R.string.mainactivity_folder_description), new Folder(f.getAbsolutePath()).count()) :
			String.format(m_context.getResources().getString(R.string.mainactivity_file_description), MainActivity.convert(f.length(), m_context)));
		
		if(f.icon() != null)
			it.setIcon(f.icon());
		else
		{
			int drawable_id = f.isDirectory() ? R.drawable.folder : R.drawable.file;
			it.setIcon(m_context.getResources().getDrawable(drawable_id));
		}
		
		return it;
	}

}
