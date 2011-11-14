package com.runninghusky.coupons;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

public class OpenCoupon extends Activity implements Runnable {
	private ProgressDialog pd;
	private String url;
	private Drawable image;
	private Context context;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Bundle b = getIntent().getExtras();
		url = b.getString("url");
		setContentView(R.layout.coupon);
		context = this;
		getTheImage();
	}

	private void getTheImage() {
		pd = ProgressDialog.show(this, "", "Loading Coupon...", true, false);
		Thread thread = new Thread(this);
		thread.start();

	}

	public void run() {
		image = ImageOperations(context, url, "image.gif");
		handler.sendEmptyMessage(0);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			pd.dismiss();
			try {
				ImageView imgView = new ImageView(context);
				imgView = (ImageView) findViewById(R.id.ImageViewCoupon);
				imgView.setImageDrawable(image);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	private Drawable ImageOperations(Context ctx, String url,
			String saveFilename) {
		try {
			InputStream is = (InputStream) this.fetch(url);
			Drawable d = Drawable.createFromStream(is, "src");
			return d;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Object fetch(String address) throws MalformedURLException,
			IOException {
		URL url = new URL(address);
		Object content = url.getContent();
		return content;
	}
}
