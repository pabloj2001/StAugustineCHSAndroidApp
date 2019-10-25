package ca.staugustinechs.staugustineapp;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import ca.staugustinechs.staugustineapp.Fragments.HomeFragment;

/**
 * Implementation of App Widget functionality.
 */
public class DayWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        final RemoteViews widgetViews = new RemoteViews(context.getPackageName(), R.layout.day_widget);

        //UPDATE DATE
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("EST"));
        //cal.setTimeInMillis(System.currentTimeMillis() + (1000 * 60 * 60 * 24));
        DateFormat date = new SimpleDateFormat("EEEE, MMMM d, yyyy");
        final String today = date.format(cal.getTime());

        widgetViews.setTextViewText(R.id.date_widg, today);
        widgetViews.setTextViewText(R.id.day_widg, HomeFragment.dayNumber);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, widgetViews);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

