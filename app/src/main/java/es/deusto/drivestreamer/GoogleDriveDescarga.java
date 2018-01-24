package es.deusto.drivestreamer;

/**
 * Created by UNAI on 2018/01/21.
 */
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;
import android.content.Context;

import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Activity to illustrate how to retrieve and read file contents.
 */
public class GoogleDriveDescarga extends GoogleDrive {
    private static final String TAG = "RetrieveContents";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDriveClientReady() {
        seleccionarAudio()
                .addOnSuccessListener(this,
                        new OnSuccessListener<DriveId>() {
                            @Override
                            public void onSuccess(DriveId driveId) {
                                retrieveContents(driveId.asDriveFile());
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Ningun archivo seleccionado", e);
                        showMessage("No se ha seleccinado ningún archivo");
                        finish();
                    }
                });
    }

    private void retrieveContents(DriveFile file) {
        // [START open_file]
        Task<DriveContents> openFileTask =
                getDriveResourceClient().openFile(file, DriveFile.MODE_READ_ONLY);

        // Nombre unico del audio descargado de drive
        final String nomAudio = file.getDriveId().toInvariantString()+ ".mp3";

        FilenameFilter filtro = new FilenameFilter() {
            @Override public boolean accept(File dir, String name) {
                if(name == nomAudio){
                    return true;
                } else{
                    return false;
                }}};

        final File audiotemp = new File(getCacheDir().getPath(), nomAudio);
        Log.d(TAG,audiotemp.getAbsolutePath());


        if(getCacheDir().listFiles(filtro).length == 0) {
            Log.d(TAG,"Hay que descargar el archivo");
            openFileTask
                    .continueWithTask(new Continuation<DriveContents, Task<Void>>() {
                        @Override
                        public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                            DriveContents contents = task.getResult();
                            InputStream input = contents.getInputStream();

                            try {

                                OutputStream output = new FileOutputStream(audiotemp);
                                try {
                                    byte[] buffer = new byte[4 * 1024]; // or other buffer size
                                    int read;

                                    while ((read = input.read(buffer)) != -1) {
                                        output.write(buffer, 0, read);
                                    }

                                    output.flush();
                                } finally {
                                    output.close();
                                }
                            } finally {
                                input.close();
                            }

                            Task<Void> discardTask = getDriveResourceClient().discardContents(contents);
                            return discardTask;
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    MediaPlayer mp = new MediaPlayer();

                    try {
                        audiotemp.setReadable(true, false);
                        mp.setDataSource(audiotemp.getAbsolutePath());
                        mp.prepare();
                        mp.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Unable to read contents", e);
                    showMessage(getString(R.string.read_failed));
                    finish();
                }
            });
        }else{

            Log.d(TAG,"No se ha tenido que descargar el archivo porque ya estaba en cache");
            MediaPlayer mp = new MediaPlayer();

            try {
                audiotemp.setReadable(true, false);
                mp.setDataSource(audiotemp.getAbsolutePath());
                mp.prepare();
                mp.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            finish();
        }


    }
}
