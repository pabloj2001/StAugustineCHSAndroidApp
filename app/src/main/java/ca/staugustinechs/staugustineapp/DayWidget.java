package ca.staugustinechs.staugustineapp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;


/**
 * Implementation of App Widget functionality.
 */
public class DayWidget extends AppWidgetProvider {
    public static String day = "Click to update";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        final RemoteViews widgetViews = new RemoteViews(context.getPackageName(), R.layout.day_widget);

        //UPDATE DATE
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("EST"));
        //cal.setTimeInMillis(System.currentTimeMillis() + (1000 * 60 * 60 * 24));
        DateFormat date = new SimpleDateFormat("EEEE, MMMM d, yyyy");
        final String today = date.format(cal.getTime());

        updateDay(today);

        widgetViews.setTextViewText(R.id.date_widg, today);
        widgetViews.setTextViewText(R.id.day_widg, day);

        Intent update = new Intent(context, DayWidget.class);
        update.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] idArray = new int[]{appWidgetId};
        update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray);
        PendingIntent pendingUpdate = PendingIntent.getBroadcast(context, appWidgetId, update,
                PendingIntent.FLAG_UPDATE_CURRENT);

        widgetViews.setOnClickPendingIntent(R.id.day_widg, pendingUpdate);
        widgetViews.setOnClickPendingIntent(R.id.date_widg, pendingUpdate);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, widgetViews);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
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

    private static void updateDay(final String today) {
        FirebaseFirestore.getInstance().collection("info")
                .document("dayNumber").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();

                            boolean haveFun = doc.getBoolean("haveFun");
                            String dayNum = doc.getString("dayNumber");
                            if (haveFun) {
                                day = dayNum;
                            } else {
                                if (!today.contains("Saturday") && !today.contains("Sunday")) {
                                    if (dayNum.trim().equals("1") || dayNum.trim().equals("2")) {
                                        day = "Day " + dayNum;
                                    } else {
                                        day = "Day " + dayNum;
                                    }
                                } else {
                                    if (dayNum.trim().equals("1") || dayNum.trim().equals("2")) {
                                        int finalDay = 1;
                                        if (dayNum.trim().equals("1")) {
                                            finalDay = 2;
                                        }
                                        day = "On Monday, it will be a Day " + finalDay;
                                    }
                                }
                            }
                        }
                    }
                });
    }
}

