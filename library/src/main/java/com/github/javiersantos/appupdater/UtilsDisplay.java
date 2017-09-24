package com.github.javiersantos.appupdater;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;
import android.view.View;

import com.github.javiersantos.appupdater.enums.UpdateFrom;

import java.net.URL;

class UtilsDisplay {
  static String channel_updates = "com.beehive.pronounce.updates";

  static AlertDialog showUpdateAvailableDialog(final Context context, String title, String content,
                                               String btnNegative, String btnPositive,
                                               String btnNeutral,
                                               final DialogInterface.OnClickListener updateClickListener,
                                               final DialogInterface.OnClickListener dismissClickListener,
                                               final DialogInterface.OnClickListener disableClickListener) {
    return new AlertDialog.Builder(new ContextThemeWrapper(context,
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH ? android.R.style.Theme_DeviceDefault_Light_Dialog : android.R.style.Theme_Dialog))
      .setTitle(title)
      .setMessage(content)
      .setPositiveButton(btnPositive, updateClickListener)
      .setNegativeButton(btnNegative, dismissClickListener)
      .setNeutralButton(btnNeutral, disableClickListener).create();
  }

  static AlertDialog showUpdateNotAvailableDialog(final Context context, String title,
                                                  String content) {
    return new AlertDialog.Builder(context)
      .setTitle(title)
      .setMessage(content)
      .setPositiveButton(context.getResources().getString(android.R.string.ok),
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
          }
        })
      .create();
  }

  static Snackbar showUpdateAvailableSnackbar(final Context context, final AppUpdater appUpdater,
                                              String content, Boolean indefinite,
                                              final UpdateFrom updateFrom, final URL apk) {
    Activity activity = (Activity) context;
    int snackbarTime = indefinite ? Snackbar.LENGTH_INDEFINITE : Snackbar.LENGTH_LONG;

        /*if (indefinite) {
            snackbarTime = Snackbar.LENGTH_INDEFINITE;
        } else {
            snackbarTime = Snackbar.LENGTH_LONG;
        }*/

    Snackbar snackbar = Snackbar
      .make(activity.findViewById(android.R.id.content), content, snackbarTime);
    snackbar.setAction(context.getResources().getString(R.string.appupdater_btn_update),
      new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          UtilsLibrary.goToUpdate(context, updateFrom, apk);

          if (appUpdater.getUpdateReactionListener() != null) {
            appUpdater.getUpdateReactionListener().updatePressed();
          }
        }
      });
    return snackbar;
  }

  static void showUpdateAvailableNotification(Context context, String title, String content,
                                              UpdateFrom updateFrom, URL apk,
                                              int smallIconResourceId) {

    createNotificationChannel(context);

    PendingIntent contentIntent = PendingIntent.getActivity(context, 0, context.getPackageManager()
        .getLaunchIntentForPackage(UtilsLibrary.getAppPackageName(context)),
      PendingIntent.FLAG_CANCEL_CURRENT);

    PendingIntent pendingIntentUpdate = PendingIntent
      .getActivity(context, 0, UtilsLibrary.intentToUpdate(context, updateFrom, apk),
        PendingIntent.FLAG_CANCEL_CURRENT);

    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel_updates)
      .setContentIntent(pendingIntentUpdate)
      .setContentTitle(title)
      .setContentText(content)
      .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
      .setSmallIcon(smallIconResourceId)
      .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
      .setOnlyAlertOnce(true)
      .setAutoCancel(true)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .addAction(R.drawable.ic_system_update_white_24dp,
        context.getResources().getString(R.string.appupdater_btn_update), pendingIntentUpdate);

    NotificationManager notificationManager = (NotificationManager) context
      .getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.notify(0, builder.build());
  }

  private static void createNotificationChannel(Context context) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      NotificationManager mNotificationManager = (NotificationManager) context
        .getSystemService(Context.NOTIFICATION_SERVICE);

      // The id of the channel.
      // The user-visible name of the channel.
      CharSequence name = "Updates";
      // The user-visible description of the channel.
      String description = "Update notifications";
      int importance = NotificationManager.IMPORTANCE_HIGH;

      NotificationChannel mChannel = new NotificationChannel(channel_updates, name, importance);
      mChannel.setDescription(description);
      mChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
      mChannel.setShowBadge(true);

      if (mNotificationManager != null) {
        mNotificationManager.createNotificationChannel(mChannel);
      }
    }
  }
}
