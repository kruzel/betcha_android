package com.betcha.model.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpStatus;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.betcha.model.User;

public class ProfilePictureHandler extends AsyncTask<Void, Void, Void> {
	private User user;
	private Context context;
	private ArrayAdapter<User> adapter;
	
	private static LruCache<String, Bitmap> mMemoryCache;
	
	public ProfilePictureHandler(User user, Context ctx, ArrayAdapter<User> adapter) {
		this.user = user;
		this.context = ctx;
		this.adapter = adapter;
		
		if(mMemoryCache==null) {
		
			// Get memory class of this device, exceeding this amount will throw an
		    // OutOfMemory exception.
		    final int memClass = ((ActivityManager) context.getSystemService(
		            Context.ACTIVITY_SERVICE)).getMemoryClass();
	
		    // Use 1/8th of the available memory for this memory cache but no more hen 1MB.
		    final int cacheSize = 1024 * 1024 * memClass / 8;
	
		    mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
		        @SuppressLint("NewApi")
				@Override
		        protected int sizeOf(String key, Bitmap bitmap) {
		            // The cache size will be measured in bytes rather than number of items.
		            return bitmap.getByteCount();
		        }
		    };
		}
	    
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		
		Bitmap profile_pic_bitmap;
		ContentResolver cr = context.getContentResolver();
		
		if(user.getContact_id()!=null) {
		    Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, user.getContact_id());
		    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
		    if (input != null) 
		    {
		    	profile_pic_bitmap = BitmapFactory.decodeStream(input);
		    	if(profile_pic_bitmap!=null) {
			    	addBitmapToMemoryCache(profile_pic_bitmap);
			    	return null;
		    	}
		    }
		    
		}

		if(user.getContact_photo_id()!=null) {
		    byte[] photoBytes = null;
	
		    Uri photoUri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, user.getContact_photo_id());
	
		    Cursor c = cr.query(photoUri, new String[] {ContactsContract.CommonDataKinds.Photo.PHOTO}, null, null, null);
	
		    try 
		    {
		        if (c.moveToFirst()) 
		            photoBytes = c.getBlob(0);
	
		    } catch (Exception e) {
		        // TODO: handle exception
		        e.printStackTrace();
	
		    } finally {
	
		        c.close();
		    }           
	
		    if (photoBytes != null) {
		    	profile_pic_bitmap = BitmapFactory.decodeByteArray(photoBytes,0,photoBytes.length);
		    	if(profile_pic_bitmap!=null) {
			    	addBitmapToMemoryCache(profile_pic_bitmap);
			    	return null;
		    	}
		    }
		    
		}
		
		if(user.getProvider().equals("facebook")) {
			String url = "http://graph.facebook.com/" + user.getUid() + "/picture?type=square";
			profile_pic_bitmap = downloadBitmap(url);
			if(profile_pic_bitmap!=null) {
		    	addBitmapToMemoryCache(profile_pic_bitmap);
		    	return null;
	    	}
    	}
		
		return null;
	}
		
	@Override
	protected void onPostExecute(Void result) {
		if(adapter!=null)
			adapter.notifyDataSetChanged();
		super.onPostExecute(result);
	}

	private void addBitmapToMemoryCache(Bitmap bitmap) {
		Bitmap bm = getBitmapFromMemCache();
	    if (bm == null) {
	    	if(user.getProvider().equals("facebook")) {
	    		bm = mMemoryCache.put(user.getUid(), bitmap);
			} else {
				bm = mMemoryCache.put(user.getEmail(), bitmap);
			}
	        
	    }
	    
	    return;
	}
		
	public Bitmap getBitmapFromMemCache() {
		if(user.getProvider().equals("facebook")) {
			return mMemoryCache.get(user.getUid());
		} else {
			return mMemoryCache.get(user.getEmail());
		}
	}
	
	public Bitmap downloadBitmap(String url) {
		
		URL https_url = null;
		try {
			https_url = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
		
		HttpURLConnection con = null;
		try {
			con = (HttpURLConnection)https_url.openConnection();
			
			if(con==null)
				return null;
							
			if (con.getResponseCode() != HttpStatus.SC_SEE_OTHER || con.getResponseCode() != HttpStatus.SC_MOVED_TEMPORARILY) { 
				String newUrl = con.getHeaderField("Location");
			
	            if (newUrl != null && newUrl.length() != 0) {
	                // call again the same downloading method with new URL
	                return downloadBitmap(newUrl);
	            }
	        }
	        
	        if (con.getResponseCode() != HttpStatus.SC_OK) { 
	            Log.w("ImageDownloader", "Error " + con.getResponseCode() + " while retrieving bitmap from " + url); 
	            return null;
	        }
			
			InputStream inputStream = null;
			try {
				inputStream = con.getInputStream();
				final Bitmap bitmap = BitmapFactory.decodeStream(new BufferedInputStream(inputStream));
				return bitmap;
			} catch (Exception e) {
				if (inputStream != null) {
                    inputStream.close();  
                }
				e.printStackTrace();
			} finally {
                if (inputStream != null) {
                    inputStream.close();  
                }
            }
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}	
		
//		if(url.startsWith("https")) { 
//			URL https_url = null;
//			try {
//				https_url = new URL(url);
//			} catch (MalformedURLException e) {
//				e.printStackTrace();
//				return null;
//			}
//			
//			HttpsURLConnection con = null;
//			try {
//				con = (HttpsURLConnection)https_url.openConnection();
//				
//				if(con==null)
//					return null;
//								
//				if (con.getResponseCode() != HttpStatus.SC_SEE_OTHER || con.getResponseCode() != HttpStatus.SC_MOVED_TEMPORARILY) { 
//					String newUrl = con.getHeaderField("Location");
//				
//		            if (newUrl != null && newUrl.length() != 0) {
//		                // call again the same downloading method with new URL
//		                return downloadBitmap(newUrl);
//		            }
//		        }
//		        
//		        if (con.getResponseCode() != HttpStatus.SC_OK) { 
//		            Log.w("ImageDownloader", "Error " + con.getResponseCode() + " while retrieving bitmap from " + url); 
//		            return null;
//		        }
//				
//				InputStream inputStream = null;
//				try {
//					inputStream = con.getInputStream();
//					final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//					return bitmap;
//				} catch (Exception e) {
//					if (inputStream != null) {
//	                    inputStream.close();  
//	                }
//					e.printStackTrace();
//				} finally {
//	                if (inputStream != null) {
//	                    inputStream.close();  
//	                }
//	            }
//			} catch (IOException e) {
//				e.printStackTrace();
//				return null;
//			}	
//		} 
		
		return null;
	}
	
}
