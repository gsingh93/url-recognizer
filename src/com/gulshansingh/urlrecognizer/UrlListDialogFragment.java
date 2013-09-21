package com.gulshansingh.urlrecognizer;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class UrlListDialogFragment extends DialogFragment {

	private static List<String> urls;

	public static void setUrls(List<String> urls) {
		UrlListDialogFragment.urls = urls;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (urls == null) {
			throw new NullPointerException();
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("URLs");

		ListView urlList = new ListView(getActivity());
		ArrayAdapter<String> urlAdapter = new ArrayAdapter<String>(
				getActivity(), R.layout.url_list_row, R.id.url_edit_text, urls);
		urlList.setAdapter(urlAdapter);
		builder.setView(urlList);
		return builder.create();
	}
}
