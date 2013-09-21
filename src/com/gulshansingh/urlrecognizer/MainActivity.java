package com.gulshansingh.urlrecognizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

public class MainActivity extends Activity {

	private static final String APP_NAME = "UrlRecognizer";
	private static final int INTENT_ID_CAPTURE_IMAGE = 0;

	private File mCaptureFile;

	private TextView mResultStringTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mResultStringTextView = (TextView) findViewById(R.id.result_string);
		try {
			installTrainingData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void installTrainingData() throws IOException {
		File file = new File(getFilesDir() + "/tessdata/eng.traineddata");
		file.getParentFile().mkdirs();
		FileOutputStream out = new FileOutputStream(file);

		InputStream in = getAssets().open("eng.traineddata");
		copyFile(in, out);

		out.close();
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}

	private File captureImage() throws IOException {
		// TODO: Move this to internal directory after image capture
		File file = new File(Environment.getExternalStorageDirectory(),
				"capture.jpg");
		Uri outputFileUri = Uri.fromFile(file);

		Intent intent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

		startActivityForResult(intent, INTENT_ID_CAPTURE_IMAGE);

		Toast.makeText(this, "Launching Camera", Toast.LENGTH_SHORT).show();

		System.out.println("file: " + file);

		return file;
	}

	private void onPhotoTaken() {
		if (mCaptureFile == null) {
			throw new NullPointerException("Decoded bitmap is null");
		}

		/*
		 * TODO: This fails for images with low compression ratios. We need to
		 * choose the sample size dynamically.
		 */
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;

		System.out.println("onPhotoTaken: " + mCaptureFile);
		Bitmap b = BitmapFactory.decodeFile(mCaptureFile.getAbsolutePath(),
				options);

		if (b == null) {
			throw new NullPointerException("Decoded bitmap is null");
		}

		try {
			prepareBitmap(b);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String parsedText = parseText(b);
		mResultStringTextView.setText("Result: " + parsedText);

		List<String> urls = findUrls(parsedText);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (!urls.isEmpty()) {
			builder.setTitle("URLs");

			ListView urlList = new ListView(this);
			ArrayAdapter<String> urlAdapter = new ArrayAdapter<String>(this,
					R.layout.url_list_row, R.id.url_edit_text, urls);
			urlList.setAdapter(urlAdapter);
			builder.setView(urlList);
			builder.create().show();
		} else {
			builder.setTitle("No URL found");
			builder.setMessage("No URL found. Valid URLs start with http or https. If the URL does begin with this, try taking a picture again.");
			builder.create().show();
		}
	}

	/*
	 * The bitmap must be properly rotated and the format must be ARGB_8888 in
	 * order to work with the Tesseract API
	 */
	private void prepareBitmap(Bitmap b) throws IOException {
		/*
		 * TODO: There is a bug in Android that affects this code for some
		 * devices:
		 * http://stackoverflow.com/questions/8450539/images-taken-with-
		 * action-image-capture-always-returns-1-for-exifinterface-tag-or We
		 * need to implement the workaround.
		 */
		ExifInterface exif = new ExifInterface(mCaptureFile.getAbsolutePath());
		int exifOrientation = exif
				.getAttributeInt(ExifInterface.TAG_ORIENTATION,
						ExifInterface.ORIENTATION_NORMAL);

		int rotate = 0;

		switch (exifOrientation) {
		case ExifInterface.ORIENTATION_ROTATE_90:
			rotate = 90;
			break;
		case ExifInterface.ORIENTATION_ROTATE_180:
			rotate = 180;
			break;
		case ExifInterface.ORIENTATION_ROTATE_270:
			rotate = 270;
			break;
		}

		if (rotate != 0) {
			Log.d(APP_NAME, "Rotating " + rotate + " degrees");

			int w = b.getWidth();
			int h = b.getHeight();

			Matrix mtx = new Matrix();
			mtx.preRotate(rotate);

			b = Bitmap.createBitmap(b, 0, 0, w, h, mtx, false);
		}
		b = b.copy(Bitmap.Config.ARGB_8888, true);
	}

	private String parseText(Bitmap b) {
		TessBaseAPI baseApi = new TessBaseAPI();
		baseApi.init(getFilesDir().getAbsolutePath(), "eng");
		baseApi.setImage(b);
		String text = baseApi.getUTF8Text();
		baseApi.end();

		return text;
	}

	private List<String> findUrls(String text) {
		String regex = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
		Pattern urlPattern = Pattern.compile(regex);
		Matcher m = urlPattern.matcher(text);
		List<String> urls = new ArrayList<String>();
		while (m.find()) {
			urls.add(m.group());
		}

		return urls;
	}

	public void captureImageClicked(View v) {
		try {
			mCaptureFile = captureImage();
			System.out.println("mCaptureFile: ");
			System.out.println(mCaptureFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		/*
		 * TODO: Get bitmap directly from intent data if possible. This doesn't
		 * work on Nexus and Galaxy devices, but may work on other devices.
		 */
		if (requestCode == INTENT_ID_CAPTURE_IMAGE) {
			switch (resultCode) {
			case Activity.RESULT_OK:
				onPhotoTaken();
				break;
			}
		}
	}
}
