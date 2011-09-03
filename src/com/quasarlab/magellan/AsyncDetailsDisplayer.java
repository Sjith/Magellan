package com.quasarlab.magellan;

import java.io.File;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class AsyncDetailsDisplayer extends AsyncTask<String, Void, Void> {
	protected int count = 1;
	protected long size = 0;
	ProgressDialog dialog;
	Context m_context;
	MagellanFile file;
	String type;
	
	public AsyncDetailsDisplayer(Context context)
	{
		m_context = context;
	}

	protected void onPreExecute() 
	{
		dialog = ProgressDialog.show(m_context, "", m_context.getResources().getString(R.string.mainactivity_dialog_progress_getting_details), true);
	}
	
	protected Void doInBackground(String... args) 
	{
		assert (args.length == 1);
		String path = args[0];
		file = new MagellanFile(path);
		type = file.mimeType();
		if(file.isDirectory())
		{
			Folder folder = new Folder(path);
			count = folder.recursiveCount();
			size = folder.recursiveSize();
		}
		else
			size = file.length();
		return null;
	}
	
	protected void onPostExecute(Void foo) {
		try {
			dialog.dismiss();
		}
		catch (IllegalArgumentException e) {
			// Window has leaked
		}
				
		AlertDialog.Builder adb = new AlertDialog.Builder(m_context);
		adb.setTitle(file.getName());
		adb.setMessage(String.format(m_context.getResources().getString(R.string.mainactivity_dialog_details_info), file.getAbsolutePath(), MainActivity.convert(size, m_context), count, type));
		adb.setPositiveButton(m_context.getResources().getString(R.string.error_dialog_ok), null);
		adb.show();
	}
}