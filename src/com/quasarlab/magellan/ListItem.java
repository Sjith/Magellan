package com.quasarlab.magellan;

import com.quasarlab.magellan.Folder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ListItem extends LinearLayout
{
	private TextView m_title;
	private TextView m_descr;
	private ImageView m_icon;
	private Context m_context;
	
	public ListItem(Context context, MagellanFile f)
	{
		super(context);
		m_context = context;
		
		LayoutInflater layout = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layout.inflate(R.layout.item, this, true);
		
		m_title = (TextView) findViewById(R.id.title);
		m_descr = (TextView) findViewById(R.id.descr);
		m_icon = (ImageView) findViewById(R.id.img);
		
		setFile(f);
	}
	
	public void setFile(MagellanFile f)
	{
		m_title.setText(f.getName());
		
		// icon and descr
		Drawable d;
		String descr;
		
		if(f.isDirectory())
		{
			d = m_context.getResources().getDrawable(R.drawable.folder);
			descr = String.format(m_context.getResources().getString(R.string.mainactivity_folder_description), MainActivity.convert(new Folder(f.getAbsolutePath()).recursiveSize(), m_context));
		}
		else
		{
			d = m_context.getResources().getDrawable(R.drawable.file);
			descr = String.format(m_context.getResources().getString(R.string.mainactivity_file_description), MainActivity.convert(f.length(), m_context));
		}
		
		m_descr.setText(descr);
		m_icon.setImageDrawable(d);
	}
	
	public void setTitle(String title)
	{
		m_title.setText(title);
	}
	
	public void setDescr(String descr)
	{
		m_descr.setText(descr);
	}
	
	public void setIcon(Drawable d)
	{
		m_icon.setImageDrawable(d);
	}
}

