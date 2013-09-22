package com.gulshansingh.urlrecognizer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ListHistory extends ListActivity {
	public static final String PREFS_NAME = "PrefsFile";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		Set<String> history = settings.getStringSet("url_history",
				new LinkedHashSet<String>());
		// Toast.makeText(this.getApplicationContext(), history.size(),
		// Toast.LENGTH_LONG).show();
		List<String> historyList = new ArrayList<String>(history);
		ListAdapter adapter = new ArrayAdapter<String>(this,
				R.layout.history_row, R.id.history_url, historyList);
		// Bind to our new adapter.
		setListAdapter(adapter);

		ListView lv = getListView();

		// listening to single list item on click
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				// selected item
				String url = ((TextView) view).getText().toString();
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);

			}
		});
	}

}