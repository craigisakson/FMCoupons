package com.runninghusky.coupons;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class CategoryActivity extends Activity implements Runnable{
	ListView list;
	private ProgressDialog pd;
	private List<Category> cS = new ArrayList<Category>();

	@Override
	public void onDestroy() {
		// adapter.imageLoader.stopThread();
		// list.setAdapter(null);
		super.onDestroy();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		list = new ListView(this);
		setupStart();
	}

	private void setupStart() {
		getCategories();
	}

	public void getCategories() {
		pd = ProgressDialog.show(this, "", "Fetching categories...", true, false);
		Thread thread = new Thread(this);
		thread.start();
	}

	public void run() {
		callWebService();
		handler.sendEmptyMessage(0);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			pd.dismiss();
			try {
				results();
				setContentView(list);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public void callWebService() {
		String res = "";
		try {
			String cat = "http://www.fargomobilecoupons.com/admin/xml/category-list.xml";
			URL url = new URL(cat);

			URLConnection connection;
			connection = url.openConnection();

			HttpURLConnection httpConnection = (HttpURLConnection) connection;

			int responseCode = httpConnection.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_OK) {
				InputStream in = httpConnection.getInputStream();
				Writer writer = new StringWriter();

				char[] buffer = new char[1024];
				try {
					Reader reader = new BufferedReader(new InputStreamReader(
							in, "UTF-8"));
					int n;
					while ((n = reader.read(buffer)) != -1) {
						writer.write(buffer, 0, n);
					}
				} finally {
					in.close();
				}
				res = writer.toString();
			}
			cS = makeCs(res);
			Collections.sort(cS);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}

	} // end callWebService()

	private void results() {
		if (cS.size() > 0) {
//			list = (ListView) findViewById(R.id.ListViewCategory);
			ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();

//			Log.d("cS.size()", String.valueOf(cS.size()));
			for (Category c : cS) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("retailer", c.getTitle());
				map.put("heading", "");
				map.put("img", c.getName());
				mylist.add(map);
			}

//			Log.d("mylist.size: ", String.valueOf(mylist.size()));
			
			SimpleAdapter mSchedule = new SimpleAdapter(this, mylist,
					R.layout.row,
					new String[] { "retailer", "heading", "img" }, new int[] {
							R.id.Retailer, R.id.Heading, R.id.Img });
			list.setAdapter(mSchedule);

			list.setOnItemClickListener(new OnItemClickListener() {
				// @Override
				public void onItemClick(AdapterView<?> a, View v, int position,
						long id) {
					Object o = list.getItemAtPosition(position);
					HashMap fullObject = (HashMap) o;
					String cat = fullObject.get("img").toString();

					Intent myIntent = new Intent(CategoryActivity.this, CategoriedCouponActivity.class);
					Bundle b = new Bundle();
					b.putString("cat", cat);
					myIntent.putExtras(b);
					CategoryActivity.this.startActivity(myIntent);
				}
			});
		} else {
			Toast.makeText(this, "No results found...", Toast.LENGTH_LONG)
					.show();
		}
	}

	private List<Category> makeCs(String res) {
		List<Category> categories = new ArrayList<Category>();
//		Log.d("res", res);
		String[] cps = res.split("<class>");
		for (int i = 1; i < cps.length; i++) {
			Category c = new Category();
//			Log.d("cps", cps[i]);
			c.setTitle(getData(cps[i], "<display>", "</display>"));
			c.setName(getData(cps[i], "<item>", "</item>"));
			categories.add(c);
		}

		return categories;
	}

	private String getData(String s, String beginTag, String endTag) {
//		Log.d("inputs: ", s + " " + beginTag + " " + endTag);
		String[] s1 = s.split(beginTag);
		String[] s2 = s1[1].split(endTag);
//		Log.d("s2: ", s2[0] + " " + s2[1]);
		return s2[0];
	}
}