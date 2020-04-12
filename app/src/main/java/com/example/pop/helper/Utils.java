package com.example.pop.helper;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.pdf.PdfDocument;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.pop.R;
import com.example.pop.activity.FolderActivity;
import com.example.pop.activity.FragmentHolder;
import com.example.pop.activity.ReceiptActivity;
import com.example.pop.asynctasks.AddFolderAsyncTask;
import com.example.pop.asynctasks.DeleteFolderAsyncTask;
import com.example.pop.model.Folder;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Utils extends AppCompatActivity {

    public static void printToast(Context context, String message){
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        View toastView = toast.getView();
        toastView.getBackground().setColorFilter(Color.DKGRAY, PorterDuff.Mode.SRC_IN);
        TextView v = toast.getView().findViewById(android.R.id.message);
        v.setTextColor(Color.WHITE);
        toast.show();
    }

    private static char[] hexCharsArray = "0123456789ABCDEF".toCharArray();

    public static String toHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexCharsArray[v >>> 4];
            hexChars[j * 2 + 1] = hexCharsArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String s){
        int len = s.length();
        byte[] data = new byte[len / 2];
        for(int i = 0; i < len; i+=2){
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static void addDrawerFolder(NavigationView navigationView, Context context, int itemId, String itemName){
        Session session = new Session(context);

        MenuItem myMoveGroupItem = navigationView.getMenu().getItem(1);
        SubMenu subMenu = myMoveGroupItem.getSubMenu();

        if(!session.getCurrentFolder().equalsIgnoreCase(String.valueOf(itemId)))
            subMenu.add(Menu.NONE, itemId, Menu.NONE, itemName).setIcon(R.drawable.ic_folder_black_24dp).setOnMenuItemClickListener(FragmentHolder.drawerFolderClickListener);
        else
            subMenu.add(Menu.NONE, itemId, Menu.NONE, itemName).setIcon(R.drawable.baseline_folder_open_24).setOnMenuItemClickListener(FragmentHolder.drawerFolderClickListener);
    }

    public static void showNotification(Context context, String chanelId, String title, String text, int receiptId){
        String CHANNEL_ID = chanelId;
        String CHANNEL_NAME = "Notification";
        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ context.getPackageName() + "/" + R.raw.success);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            channel.setLightColor(Color.BLUE);
            channel.enableLights(true);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.setSound(soundUri, audioAttributes);

            notificationManager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(context, ReceiptActivity.class);
        NotificationCompat.Builder notificationBuilder;

        if(receiptId != -1) {
            notificationIntent.putExtra("receiptID", receiptId);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setVibrate(new long[]{0, 100})
                    .setPriority(Notification.PRIORITY_MAX)
                    .setLights(Color.BLUE, 3000, 3000)
                    .setAutoCancel(true)
                    .setContentIntent(intent)
                    .setSmallIcon(R.drawable.logo_dark)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.logo_dark))
                    .setContentTitle(title)
                    .setContentText(text);
        }
        else{
            notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setVibrate(new long[]{0, 100})
                    .setPriority(Notification.PRIORITY_MAX)
                    .setLights(Color.BLUE, 3000, 3000)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.logo_dark)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.logo_dark))
                    .setContentTitle(title)
                    .setContentText(text);
        }

        notificationManager.notify(CHANNEL_ID, 1, notificationBuilder.build());
    }

    public static void createPdf(Context context, Bitmap bitmap, File myDir, String fileName){
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#ffffff"));
        canvas.drawPaint(paint);

        paint.setColor(Color.BLUE);
        canvas.drawBitmap(bitmap, 0, 0 , null);
        document.finishPage(page);

        // write the document content
        File filePath = new File(myDir, fileName);
        try {
            document.writeTo(new FileOutputStream(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        document.close();
    }

    public static void newFolderPopUp(final NavigationView navigationView, final Activity acc) {
        LayoutInflater inflater = acc.getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.new_folder_pop_up, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(acc);

        builder.setTitle("Create New Folder");
        builder.setView(dialoglayout);

        final EditText userInput = dialoglayout.findViewById(R.id.newFolderInput);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newFolderName = userInput.getText().toString();
                boolean exists = false;
                for(Folder f : FragmentHolder.folderList){
                    if (f.getName().equalsIgnoreCase(newFolderName)) {
                        exists = true;
                    }
                }
                if(!exists){
                    AddFolderAsyncTask addFolderAsyncTask = new AddFolderAsyncTask(navigationView, acc, newFolderName);
                    addFolderAsyncTask.execute();
                }
                else {
                    Toast.makeText(acc,"Folder already exists",Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static void deleteFolderPopUp(final Activity acc, final int folderId) {
        LayoutInflater inflater = (LayoutInflater) acc.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialoglayout = inflater.inflate(R.layout.delete_folder_pop_up, null);

        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(dialoglayout.getRootView().getContext());

        builder.setTitle("Delete Folder");
        builder.setView(dialoglayout);

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DeleteFolderAsyncTask deleteFolderAsyncTask = new DeleteFolderAsyncTask(folderId);
                deleteFolderAsyncTask.execute();
                Toast.makeText(acc,"Folder Deleted",Toast.LENGTH_LONG).show();

                acc.finish();

                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(acc,"Cancel",Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static boolean requestStoragePermission(final Activity acc) {
        boolean answer = false;

        if(ActivityCompat.shouldShowRequestPermissionRationale(acc, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            answer = true;
            new AlertDialog.Builder(acc)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed to export your receipts.")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(acc, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(acc, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        return answer;
    }
}
