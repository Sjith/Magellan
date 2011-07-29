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
	File file;
	
	public AsyncDetailsDisplayer(Context context)
	{
		m_context = context;
	}

	protected void onPreExecute() {
		dialog = ProgressDialog.show(m_context, "", m_context.getResources().getString(R.string.mainactivity_dialog_progress_getting_details), true);
	}
	protected Void doInBackground(String... args) {
		assert (args.length == 1);
		String path = args[0];
		file = new File(path);
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
		
		MainActivity act = (MainActivity) m_context;
		
		AlertDialog.Builder adb = new AlertDialog.Builder(m_context);
		adb.setTitle(file.getName());
		adb.setMessage(String.format(m_context.getResources().getString(R.string.mainactivity_dialog_details_info), file.getAbsolutePath(), act.convert(size), count));
		adb.setPositiveButton(m_context.getResources().getString(R.string.error_dialog_ok), null);
		adb.show();
	}
}