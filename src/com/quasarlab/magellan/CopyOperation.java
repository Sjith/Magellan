package com.quasarlab.magellan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;


public class CopyOperation extends AsyncTask<String, Pair<Integer,String>, Boolean>
{
	private ProgressDialog m_progressDialog;
	Context m_context;
	File[] m_toCopy;
	Vector<File> m_copied;
	
	protected void onPreExecute()
	{
		m_progressDialog =  new ProgressDialog(m_context);
		m_progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		m_progressDialog.setTitle("Copying elements...");
		m_progressDialog.setCancelable(false);
		
		int count = m_toCopy.length;
		for(int i = 0; i < m_toCopy.length; i++)
		{
			File file = m_toCopy[i];
			
			if(file.isDirectory())
			{
				Folder folder = new Folder(file.getAbsolutePath());
				count += folder.recursiveCount();
			}
		}

		m_progressDialog.setMax(count);
		m_progressDialog.show();	
	}
	
	public CopyOperation(Context context, File[] toCopy)
	{
		m_context = context;
		m_toCopy = toCopy;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Boolean doInBackground(String... params) 
	{
		
		String destinationFolder = params[0];
		Folder destFolder = new Folder(destinationFolder);
		m_copied = new Vector<File>();
		
		int count = 0;
		for(int i = 0; i < m_toCopy.length; i++)
		{
			File file = m_toCopy[i];
			if(file.isDirectory())
			{
				String fname = file.getName();
				
				int j = 1;
				while(destFolder.contains(fname))
				{
					fname = file.getName() + "." + String.valueOf(j);
					j++;
				}
				
				Queue< Pair<String,String> > queue = new LinkedList< Pair<String,String> >();
				queue.add( new Pair<String, String>(file.getAbsolutePath(), destinationFolder + "/" + fname) );
				
				while(!queue.isEmpty())
				{
					Pair<String,String> p = queue.poll();
					
					File source = new File(p.getFirst());
					File dest = new File(p.getSecond());
					
					if(!source.exists() || !source.isDirectory())
						continue;
					
					if(!source.canRead() || !source.canExecute())
					{
						publishProgress(new Pair<Integer, String>(-1, "The source directory could not be read. Aborting."));
						return false;
					}
					
					if(!dest.exists())
					{
						if(!dest.mkdirs())
						{
							publishProgress(new Pair<Integer, String>(-1, "The directory " + dest.getName() + " could not be created. Check permissions. Aborting."));
							return false;
						}
						else
						{
							count++;
							publishProgress(new Pair<Integer,String>(count, dest.getName()));
						}
					}
						
					String[] files = source.list();
					for(int l = 0; l < files.length; l++)
					{
						String srcName = p.getFirst() + "/" + files[l];
						String destName = p.getSecond() + "/" + files[l];
						
						File f = new File(srcName);
						if(f.isDirectory())
							queue.add(new Pair<String,String>(srcName,destName));
						else
						{
							String name = f.getName();
							int k = 1;
							while(destFolder.contains(name))
							{
								name = f.getName() + "." + String.valueOf(k);
								k++;
							}
							
							if(!f.exists())
							{
								publishProgress(new Pair<Integer, String>(-1, "The source file does not exists. Aborting."));       
								return false;						
							}			
							
							if(!f.canRead())
							{
								publishProgress(new Pair<Integer, String>(-1, "The source file could not be read. Aborting."));
								return false;						
							}
							
							if(!dest.canWrite())
							{
								publishProgress(new Pair<Integer, String>(-1, "Impossible to create files in this repository. Check it exists and you have enough permissions on it."));       
								return false;						
							}
							
							File newFile = new File(dest.getAbsolutePath() + "/" + name);
							try
							{
								newFile.createNewFile();
							}
							catch(IOException e)
							{
								publishProgress(new Pair<Integer, String>(-1, "An error occured while creating the new file : " + e.getMessage() + ". Aborting."));
								return false;	
							}
							
							InputStream in;
							OutputStream out;
							try
							{
								in = new FileInputStream(f);
								out = new FileOutputStream(newFile);
							}
							catch(FileNotFoundException e)
							{
								publishProgress(new Pair<Integer, String>(-1, "An error occured while creating the new file : " + e.getMessage() + ". Aborting."));
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
								publishProgress(new Pair<Integer, String>(-1, "An error occured while creating the new file : " + e.getMessage() + ". Aborting."));
								newFile.delete();
								return false;		
						    }
						    
							count++;
							publishProgress(new Pair<Integer,String>(count, f.getName()));
						}
					}				
				}
			}
			else
			{				 
				String name = file.getName();
				int j = 1;
				while(destFolder.contains(name))
				{
					name = name + "." + String.valueOf(j);
					j++;
				}
				
				if(!file.exists())
				{
					publishProgress(new Pair<Integer, String>(-1, "The source file does not exists. Aborting."));       
					return false;						
				}			
				
				if(!file.canRead())
				{
					publishProgress(new Pair<Integer, String>(-1, "The source file could not be read. Aborting."));
					return false;						
				}
				
				if(!destFolder.canWrite())
				{
					publishProgress(new Pair<Integer, String>(-1, "Impossible to create files in this repository. Check it exists and you have enough permissions on it."));       
					return false;						
				}
				
				File newFile = new File(destinationFolder + "/" + name);
				try
				{
					newFile.createNewFile();
				}
				catch(IOException e)
				{
					publishProgress(new Pair<Integer, String>(-1, "An error occured while creating the new file : " + e.getMessage() + ". Aborting."));
					return false;	
				}
				
				InputStream in;
				OutputStream out;
				try
				{
					in = new FileInputStream(file);
					out = new FileOutputStream(newFile);
				}
				catch(FileNotFoundException e)
				{
					publishProgress(new Pair<Integer, String>(-1, "An error occured while creating the new file : " + e.getMessage() + ". Aborting."));
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
					publishProgress(new Pair<Integer, String>(-1, "An error occured while creating the new file : " + e.getMessage() + ". Aborting."));
					newFile.delete();
					return false;		
			    }
		
				count++;
				publishProgress(new Pair<Integer,String>(count, file.getName()));	
			
			}
		
			m_copied.add(file);
			
		}
	
		
		return true;
	}
	
	protected void onProgressUpdate(Pair<Integer,String>... p)
	{		
		if(p[0].getFirst() < 0)
		{
			// the operation encountered an error. Stop it
			for(int i = 0; i < m_copied.size(); i++)
			{
				File copied = m_copied.get(i);
				if(copied.exists());
					//delete(copied)
			}
			
			// display the error
			AlertDialog.Builder adb = new AlertDialog.Builder(m_context);
			adb.setTitle("Error");
			adb.setMessage(p[0].getSecond());
			adb.setPositiveButton("Ok", null);
			adb.show();         
			return;
		}
		
		m_progressDialog.setProgress(p[0].getFirst());
		m_progressDialog.setMessage(p[0].getSecond());
		
	}
	
	@Override
	protected void onPostExecute(Boolean ret)
	{
		m_progressDialog.dismiss();	
		if(ret)
		{
			AlertDialog.Builder adb = new AlertDialog.Builder(m_context);
			adb.setTitle("OK");
			adb.setMessage("Copy ended with success.");
			adb.setPositiveButton("Ok", null);
			adb.show();         
			return;
			
		}
	}

}

