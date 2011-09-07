package com.quasarlab.magellan;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Debug;
import android.util.DebugUtils;
import android.util.Log;

public class AsyncThumbnailLoadOperation extends AsyncTask<List<MagellanFile>, MagellanFile, Void> 
{
	private Context m_context;

	public AsyncThumbnailLoadOperation(Context c)
	{
		m_context = c;
	}
	
	protected Void doInBackground(List<MagellanFile>... params) 
	{
		List<MagellanFile> lst = params[0];
		BitmapFactory.Options options = new BitmapFactory.Options();
		MagellanFile f;
		Bitmap b;
		
		for(int i = 0; i < lst.size(); i++)
		{
			f = lst.get(i);
			
			if (!f.isDirectory())
			{
				options.inJustDecodeBounds = true;
				options.outWidth = 0;
				options.outHeight = 0;
				options.inSampleSize = 1;
				
				try
				{
					BitmapFactory.decodeFile(f.getAbsolutePath(), options);
				}
				catch(Exception e)
				{
					continue;
				}
								
				if(options.outWidth > 0 && options.outHeight > 0)
				{
					
					int factor_w = (options.outWidth) / 64;
					int factor_h = (options.outHeight) / 64;
					
					int factor = Math.min(factor_w, factor_h);
					factor = (factor > 1) ? factor : 1;
	                
	                options.inSampleSize = factor;
	                options.inJustDecodeBounds = false;
	                
	                b = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
	                
	                
	                if(b != null)
	                {
	                	f.setIcon(new BitmapDrawable(m_context.getResources(), b));
	                	publishProgress(f);
	                }
				}
				
			}
		}		
		return null;
	}

	protected void onProgressUpdate(MagellanFile... params)
	{
		MagellanFile f = params[0];
		MainActivity ac = (MainActivity) m_context;
		ac.reloadFile(f);
	}
	
	
}
