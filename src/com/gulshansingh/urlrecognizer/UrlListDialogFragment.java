package com.gulshansingh.urlrecognizer;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class UrlListDialogFragment extends DialogFragment {
	public static final String PREFS_NAME = "PrefsFile";

	private static List<String> urls;

	public static UrlListDialogFragment newInstance(List<String> urls) {
		UrlListDialogFragment.urls = urls;
		return new UrlListDialogFragment();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (urls == null) {
			throw new NullPointerException("Urls is null");
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		if (urls.isEmpty()) {
			builder.setTitle("No URLs Found");
			builder.setMessage("No URL found. Valid URLs start with http or https. If the URL does begin with this, try taking a picture again.");
		} else {
			builder.setTitle("URLs");

			ListView urlList = new ListView(getActivity());
			ArrayAdapter<String> urlAdapter = new ArrayAdapter<String>(
					getActivity(), R.layout.url_list_row, R.id.url_edit_text,
					urls);
			urlList.setAdapter(urlAdapter);
			builder.setView(urlList);
		}
		return builder.create();
	}
}
