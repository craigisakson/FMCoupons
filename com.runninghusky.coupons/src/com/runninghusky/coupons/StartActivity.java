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
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebSettings.TextSize;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class StartActivity extends Activity implements Runnable {
	private ViewPager awesomePager;
	private static int NUM_AWESOME_VIEWS = 2;
	private Context cxt;
	private AwesomePagerAdapter awesomeAdapter;
	ListView listCoupon;
	ListView listCategory;
	private ProgressDialog pd;
	private List<Coupon> coupons = new ArrayList<Coupon>();
	private List<Category> categories = new ArrayList<Category>();

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		cxt = this;
		pd = ProgressDialog.show(this, "", "Getting coupon info...", true,
				false);
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
				resultsCoupon();
				resultsCategory();
				awesomeAdapter = new AwesomePagerAdapter();
				awesomePager = (ViewPager) findViewById(R.id.awesomepager);
				awesomePager.setAdapter(awesomeAdapter);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	private void resultsCoupon() {
		listCoupon = new ListView(cxt);
		if (coupons.size() > 0) {
			// list = (ListView) findViewById(R.id.ListViewCategory);
			ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();

			// Log.d("cS.size()", String.valueOf(cS.size()));
			for (Coupon c : coupons) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("retailer", c.getRetailer());
				map.put("heading", c.getHeading());
				map.put("img", c.getImageUri());
				mylist.add(map);
			}

			Log.d("mylist.size: ", String.valueOf(mylist.size()));

			SimpleAdapter mSchedule = new SimpleAdapter(cxt, mylist,
					R.layout.row,
					new String[] { "retailer", "heading", "img" }, new int[] {
							R.id.Retailer, R.id.Heading, R.id.Img });
			listCoupon.setAdapter(mSchedule);

			listCoupon.setOnItemClickListener(new OnItemClickListener() {
				// @Override
				public void onItemClick(AdapterView<?> a, View v, int position,
						long id) {
					Object o = listCoupon.getItemAtPosition(position);
					HashMap fullObject = (HashMap) o;
					String img = fullObject.get("img").toString();

					Intent myIntent = new Intent(StartActivity.this,
							OpenCoupon.class);
					Bundle b = new Bundle();
					b.putString("url", img);
					myIntent.putExtras(b);
					StartActivity.this.startActivity(myIntent);
				}
			});
		} else {
			// Toast.makeText(this, "No results found...", Toast.LENGTH_LONG)
			// .show();
		}
	}

	private void resultsCategory() {
		listCategory = new ListView(cxt);
		if (categories.size() > 0) {
			// list = (ListView) findViewById(R.id.ListViewCategory);
			ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();

			// Log.d("cS.size()", String.valueOf(cS.size()));
			for (Category c : categories) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("retailer", c.getTitle());
				map.put("heading", "");
				map.put("img", c.getName());
				mylist.add(map);
			}

			// Log.d("mylist.size: ", String.valueOf(mylist.size()));

			SimpleAdapter mSchedule = new SimpleAdapter(this, mylist,
					R.layout.row,
					new String[] { "retailer", "heading", "img" }, new int[] {
							R.id.Retailer, R.id.Heading, R.id.Img });
			listCategory.setAdapter(mSchedule);

			listCategory.setOnItemClickListener(new OnItemClickListener() {
				// @Override
				public void onItemClick(AdapterView<?> a, View v, int position,
						long id) {
					Object o = listCategory.getItemAtPosition(position);
					HashMap fullObject = (HashMap) o;
					String cat = fullObject.get("img").toString();

					Intent myIntent = new Intent(StartActivity.this,
							CategoriedCouponActivity.class);
					Bundle b = new Bundle();
					b.putString("cat", cat);
					myIntent.putExtras(b);
					StartActivity.this.startActivity(myIntent);
				}
			});
		} else {
			// Toast.makeText(this, "No results found...", Toast.LENGTH_LONG)
			// .show();
		}
	}

	private List<Coupon> makeCs(String res) {
		List<Coupon> coupons = new ArrayList<Coupon>();
		String[] cps = res.split("<coupon>");
		for (int i = 1; i < cps.length; i++) {
			Coupon c = new Coupon();
			c.setRetailer(getData(cps[i], "<retailer>", "</retailer>"));
			if (!c.getRetailer().equalsIgnoreCase("your business here!")) {
				c.setId(getData(cps[i], "<id>", "</id>"));
				c.setHeading(getData(cps[i], "<heading>", "</heading>"));
				c.setImageUri(getData(cps[i], "<body>", "</body>").replaceAll(
						" ", "%20"));
				// Log.d("body: ", c.getImageUri());
				c.setCat(getData(cps[i], "<class>", "</class>"));
				coupons.add(c);
			}
		}

		return coupons;
	}

	private List<Category> makeCategories(String res) {
		List<Category> categories = new ArrayList<Category>();
		// Log.d("res", res);
		String[] cps = res.split("<class>");
		for (int i = 1; i < cps.length; i++) {
			Category c = new Category();
			// Log.d("cps", cps[i]);
			c.setTitle(getData(cps[i], "<display>", "</display>"));
			c.setName(getData(cps[i], "<item>", "</item>"));
			categories.add(c);
		}

		return categories;
	}

	private String getData(String s, String beginTag, String endTag) {
		// Log.d("inputs: ", s + " " + beginTag + " " + endTag);
		String[] s1 = s.split(beginTag);
		String[] s2 = s1[1].split(endTag);
		// Log.d("s2: ", s2[0] + " " + s2[1]);
		return s2[0];
	}

	public void callWebService() {
		String res = "";
		try {
			String cat = "http://www.fargomobilecoupons.com/admin/xml/category.xml";
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
			coupons = makeCs(res);
			Collections.sort(coupons);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}

		res = "";
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
			categories = makeCategories(res);
			Collections.sort(categories);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}

	} // end callWebService()

	private class AwesomePagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return NUM_AWESOME_VIEWS;
		}

		/**
		 * Create the page for the given position. The adapter is responsible
		 * for adding the view to the container given here, although it only
		 * must ensure this is done by the time it returns from
		 * {@link #finishUpdate()}.
		 * 
		 * @param container
		 *            The containing View in which the page will be shown.
		 * @param position
		 *            The page position to be instantiated.
		 * @return Returns an Object representing the new page. This does not
		 *         need to be a View, but can be some other container of the
		 *         page.
		 */
		@Override
		public Object instantiateItem(View collection, int position) {
			ListView lv = new ListView(cxt);
			TextView tv1 = new TextView(cxt);
			TextView tv2 = new TextView(cxt);
			ImageView iv = new ImageView(cxt);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.CENTER_HORIZONTAL);
			tv1.setLayoutParams(params);
			
			
			
			RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			tv1.setTypeface(Typeface.DEFAULT_BOLD);
			tv2.setTypeface(Typeface.DEFAULT_BOLD);
			tv1.setTextSize(13);
			tv2.setTextSize(13);
			if (position == 0) {
				tv1.setText("Categories");
				params2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				iv.setImageResource(R.drawable.ic_more_arrow_right);
				iv.setLayoutParams(params2);
				iv.setId(1);
				params3.addRule(RelativeLayout.LEFT_OF, iv.getId());
				tv2.setLayoutParams(params3);
				tv2.setText("All Coupons");
				lv = listCategory;
			}else{			
				tv1.setText("All Coupons");
				params2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				iv.setImageResource(R.drawable.ic_more_arrow_left);
				iv.setLayoutParams(params2);
				iv.setId(2);
				params3.addRule(RelativeLayout.RIGHT_OF, iv.getId());
				tv2.setLayoutParams(params3);
				tv2.setText("Categories");
				lv = listCoupon;
			}
			RelativeLayout rl = new RelativeLayout(cxt);
			rl.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.bkgd_tile_black);
			BitmapDrawable bitmapDrawable = new BitmapDrawable(bmp);
			bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
			rl.setBackgroundDrawable(bitmapDrawable);
			rl.addView(iv);
			rl.addView(tv1);
			rl.addView(tv2);
			
			LinearLayout ll = new LinearLayout(cxt);
			ll.setOrientation(LinearLayout.VERTICAL);

			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.FILL_PARENT);
			ll.setLayoutParams(lp);
			
			ll.addView(rl);
			ll.addView(lv);

			((ViewPager) collection).addView(ll, 0);

			return ll;
		}

		/**
		 * Remove a page for the given position. The adapter is responsible for
		 * removing the view from its container, although it only must ensure
		 * this is done by the time it returns from {@link #finishUpdate()}.
		 * 
		 * @param container
		 *            The containing View from which the page will be removed.
		 * @param position
		 *            The page position to be removed.
		 * @param object
		 *            The same object that was returned by
		 *            {@link #instantiateItem(View, int)}.
		 */
		@Override
		public void destroyItem(View collection, int position, Object view) {
			((ViewPager) collection).removeView((TextView) view);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == ((View) object);
		}

		/**
		 * Called when the a change in the shown pages has been completed. At
		 * this point you must ensure that all of the pages have actually been
		 * added or removed from the container as appropriate.
		 * 
		 * @param container
		 *            The containing View which is displaying this adapter's
		 *            page views.
		 */
		@Override
		public void finishUpdate(View arg0) {
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}

	}

}
