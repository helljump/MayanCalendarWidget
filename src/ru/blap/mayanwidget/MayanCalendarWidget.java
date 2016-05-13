package ru.blap.mayanwidget;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

public class MayanCalendarWidget extends AppWidgetProvider {

	private static final String TAG = MayanCalendarWidget.class.getSimpleName();
	private static final int SECOND = 1000;

	public String MAYANCALENDAR_WIDGET_UPDATE;// =
												// "ru.blap.mayancalendar.action.WIDGET_UPDATE";
	public int UPDATE_INTERVAL;// = 1000 * 60;
	public int LAYOUT;// = R.layout.mayancalendar_layout;
	public int AFTER_TEXT;// = R.string.after;
	public int BEFORE_TEXT;// = R.string.before;

	public MayanCalendarWidget() {
		super();
		MAYANCALENDAR_WIDGET_UPDATE = "ru.blap.mayancalendar.action.WIDGET_UPDATE";
		UPDATE_INTERVAL = 1000 * 60;
		LAYOUT = R.layout.mayancalendar_layout;
		AFTER_TEXT = R.string.after;
		BEFORE_TEXT = R.string.before;
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];
			try {
				updateAppWidget(context, appWidgetManager, appWidgetId);
			} catch (Exception e) {
				Log.e(TAG, "" + e);
			}
		}
	}

	public void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
		final Calendar pindyr_date = GregorianCalendar.getInstance();
		pindyr_date.set(2012, 11, 21, 0, 0, 0);
		Calendar cal = GregorianCalendar.getInstance();

		long diff_seconds = (pindyr_date.getTimeInMillis() - cal.getTimeInMillis()) / SECOND;

		RemoteViews updateViews = new RemoteViews(context.getPackageName(), LAYOUT);

		if (diff_seconds < 0) {
			diff_seconds = Math.abs(diff_seconds);
			updateViews.setTextViewText(R.id.title_view, context.getText(AFTER_TEXT));
		} else {
			updateViews.setTextViewText(R.id.title_view, context.getText(BEFORE_TEXT));
		}

		long diff_days = (long) Math.floor(diff_seconds / 86400);
		diff_seconds -= diff_days * 86400;
		long diff_hours = (long) Math.floor(diff_seconds / 3600);
		diff_seconds -= diff_hours * 3600;
		long diff_minutes = (long) Math.floor(diff_seconds / 60);
		diff_seconds -= diff_minutes * 60;

		Bitmap minutes = convertToImg(String.format("%02d", diff_minutes), context);
		updateViews.setImageViewBitmap(R.id.minutes_view, minutes);
		Bitmap hours = convertToImg(String.format("%02d", diff_hours), context);
		updateViews.setImageViewBitmap(R.id.hours_view, hours);
		Bitmap days = convertToImg(String.valueOf(diff_days), context);
		updateViews.setImageViewBitmap(R.id.days_view, days);

		if (LAYOUT == R.layout.mayancalendarseconds_layout) {
			Bitmap seconds = convertToImg(String.format("%02d", diff_seconds), context);
			updateViews.setImageViewBitmap(R.id.seconds_view, seconds);
		}

		drawArrows(context, updateViews);
		setClockIntent(context, updateViews);

		appWidgetManager.updateAppWidget(appWidgetId, updateViews);

	}

	protected static void drawArrows(Context context, RemoteViews updateViews) {
		final Bitmap clockArrow = BitmapFactory.decodeResource(context.getResources(), R.drawable.arrow);
		final Bitmap clockBigArrow = BitmapFactory.decodeResource(context.getResources(), R.drawable.big_arrow);

		final Bitmap holder = Bitmap.createBitmap(clockArrow.getWidth(), clockArrow.getHeight(),
				Bitmap.Config.ARGB_4444);
		final Canvas canvas = new Canvas(holder);
		final Matrix matrix = new Matrix();
		final Paint paint = new Paint();
		final Calendar cal = GregorianCalendar.getInstance();

		float min_degrees = cal.get(Calendar.MINUTE) * 6;
		matrix.preRotate(min_degrees, clockArrow.getWidth() / 2, clockArrow.getHeight() / 2);
		canvas.drawBitmap(clockArrow, matrix, paint);

		matrix.reset();
		float hour_degrees = cal.get(Calendar.HOUR) * 30 + min_degrees / 12;
		matrix.preRotate(hour_degrees, clockBigArrow.getWidth() / 2, clockBigArrow.getHeight() / 2);
		canvas.drawBitmap(clockBigArrow, matrix, paint);

		updateViews.setImageViewBitmap(R.id.image_holder, holder);

		clockArrow.recycle();
		clockBigArrow.recycle();
	}

	public PendingIntent createClockTickIntent(Context context) {
		Intent intent = new Intent(MAYANCALENDAR_WIDGET_UPDATE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingIntent;
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		Log.d(TAG, "Starting timer");
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.MILLISECOND, 1);
		alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), UPDATE_INTERVAL,
				createClockTickIntent(context));
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		Log.d(TAG, "Turning off timer");
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(createClockTickIntent(context));
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		if (MAYANCALENDAR_WIDGET_UPDATE.equals(intent.getAction())) {
			ComponentName thisAppWidget = new ComponentName(context.getPackageName(), getClass().getName());
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
			for (int appWidgetID : ids) {
				updateAppWidget(context, appWidgetManager, appWidgetID);
			}
		}
	}

	public static Bitmap convertToImg(String text, Context context) {
		Typeface font = Typeface.createFromAsset(context.getAssets(), "crystal.ttf");
		float font_size = context.getResources().getDimension(R.dimen.number_size);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setSubpixelText(true);
		paint.setTypeface(font);
		paint.setColor(Color.BLACK);
		paint.setTextSize(font_size);
		int width = Math.round(paint.measureText(text));
		Bitmap btmText = Bitmap.createBitmap(width, (int) font_size, Bitmap.Config.ARGB_4444);
		Canvas cnvText = new Canvas(btmText);
		cnvText.drawText(text, 0, font_size, paint);
		font = null;
		return btmText;
	}

	public void setCalendarIntent(Context context, RemoteViews views) {
		try {
			Intent launchIntent = new Intent();
			String dataString = "content://com.android.calendar/time";
			launchIntent.setAction(Intent.ACTION_VIEW);
			launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
					| Intent.FLAG_ACTIVITY_CLEAR_TOP);
			Uri data = Uri.parse(dataString);
			launchIntent.setData(data);
			PendingIntent pin = PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			views.setOnClickPendingIntent(LAYOUT, pin);
		} catch (Exception e) {
			Log.i(TAG, "no calendar " + e);
		}
	}

	public void setClockIntent(Context context, RemoteViews views) {
		PackageManager packageManager = context.getPackageManager();
		Intent alarmClockIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);

		// Verify clock implementation
		String clockImpls[][] = {
				{ "HTC Alarm Clock", "com.htc.android.worldclock", "com.htc.android.worldclock.WorldClockTabControl" },
				{ "Standar Alarm Clock", "com.android.deskclock", "com.android.deskclock.AlarmClock" },
				{ "Froyo Nexus Alarm Clock", "com.google.android.deskclock", "com.android.deskclock.DeskClock" },
				{ "Moto Blur Alarm Clock", "com.motorola.blur.alarmclock", "com.motorola.blur.alarmclock.AlarmClock" } };

		boolean foundClockImpl = false;

		for (int i = 0; i < clockImpls.length; i++) {
			String vendor = clockImpls[i][0];
			String packageName = clockImpls[i][1];
			String className = clockImpls[i][2];
			try {
				ComponentName cn = new ComponentName(packageName, className);
				ActivityInfo aInfo = packageManager.getActivityInfo(cn, PackageManager.GET_META_DATA);
				alarmClockIntent.setComponent(cn);
				Log.d(TAG, "Found " + vendor + " --> " + packageName + "/" + className);
				foundClockImpl = true;
			} catch (NameNotFoundException e) {
				Log.i(TAG, vendor + " does not exists");
			}
		}

		if (foundClockImpl) {
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, alarmClockIntent, 0);
			views.setOnClickPendingIntent(LAYOUT, pendingIntent);
		}
	}

}
