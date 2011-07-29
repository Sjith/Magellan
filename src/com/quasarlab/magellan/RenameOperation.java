package com.quasarlab.magellan;

import java.io.File;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;

public class RenameOperation extends AsyncTask<Void, Void, Pair<Boolean,String>>
{
	
	Context m_context;
	File m_toRename;
	String m_newName;

	public RenameOperation(Context context, String toRename, String newName)
	{
		m_context = context;
		m_toRename = new File(toRename);
		m_newName = newName;
	}
	
	protected void onPreExecute()
	{
		
	}
	@Override
	protected Pair<Boolean,String> doInBackground(Void... params) 
	{
		Folder parentFolder = new Folder(m_toRename.getParent());
		
		if(m_newName.compareTo("") == 0)
	        return(new Pair<Boolean,String>(false, m_context.getResources().getString(R.string.error_name_null)));			
		
		if(parentFolder.contains(m_newName))   
	        return(new Pair<Boolean,String>(false, m_context.getResources().getString(R.string.error_duplicated_name)));

		if(!parentFolder.canWrite())
	        return(new Pair<Boolean,String>(false, m_context.getResources().getString(R.string.error_permissions_cannot_write_parent_dir)));
		
		if(!m_toRename.canWrite())
	        return(new Pair<Boolean,String>(false, m_context.getResources().getString(R.string.error_permissions_cannot_rename_file)));
		
		String newPath = parentFolder.getAbsolutePath() + "/" + m_newName;
		m_toRename.renameTo(new File(newPath));
		
		return new Pair<Boolean,String>(true, "Success");
	}

	@Override
	protected void onPostExecute(Pair<Boolean,String> ret)
	{
		AlertDialog.Builder adb = new AlertDialog.Builder(m_context);
		adb.setTitle(m_toRename.getName());
		adb.setMessage(ret.getSecond());
		adb.setPositiveButton(m_context.getResources().getString(R.string.error_dialog_ok), null);
		adb.show();
		
		MainActivity activity = (MainActivity) m_context;
		activity.refreshView();
	}

}
