package smaillns.smail.toulouse.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import smaillns.smail.toulouse.App;
import smaillns.smail.toulouse.R;
import com.google.android.gms.maps.GoogleMap;

public class Preferences
{
	private SharedPreferences mSharedPreferences;
	private Context mContext;

	private SharedPreferences mlanguage;

	
	public Preferences(Context context)
	{
		if(context==null) context = App.Companion.getContext();
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		mlanguage = PreferenceManager.getDefaultSharedPreferences(context);
		mContext = context;
	}
	
	
	public void clearPreferences()
	{
		Editor editor = mSharedPreferences.edit();
		editor.clear();
		editor.commit();
	}


	// getters


	public int getMapType()
	{
		String key = mContext.getString(R.string.prefs_key_map_type);
		int value = mSharedPreferences.getInt(key, GoogleMap.MAP_TYPE_NORMAL);
		return value;
	}

	public String getLanguage()
	{
		String key = mContext.getString(R.string.prefs_key_language);
		String value = mlanguage.getString(key, "fr");
		return value;
	}


	// setters


	public void setMapType(int mapType)
	{
		String key = mContext.getString(R.string.prefs_key_map_type);
		Editor editor = mSharedPreferences.edit();
		editor.putInt(key, mapType);
		editor.commit();
	}

	public void setLanguage(String language)
	{
		String key = mContext.getString(R.string.prefs_key_language);
		Editor editor = mlanguage.edit();
		editor.putString(key, language);
		editor.commit();
	}
}
