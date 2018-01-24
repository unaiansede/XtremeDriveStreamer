package es.deusto.drivestreamer;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import java.util.ArrayList;

/**
 * Created by UNAI on 2018/01/24.
 */


public class GoogleDriveCanciones extends Activity {

    final static String TAG ="GoogleDriveCanciones";

    protected static ArrayList<Audio> listaCanciones;
    protected static ListView listaView;

    @Override
    public void onCreate(Bundle b ){
        super.onCreate(b);
        //setContentView(R.layout.activity_main);
        this.loadAudio();
        //listaView = (ListView) findViewById(R.id.listaCancionesGDrive);

        final ArrayList<String> nombreCanciones = new ArrayList<>();

        for(int i = 0;i<listaCanciones.size();i++){
            nombreCanciones.add(listaCanciones.get(i).getTitle());

        }

        final ArrayAdapter adaptador = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, nombreCanciones);
        if(listaView == null){
            Log.d(TAG,"vista nula");
        } else if(adaptador == null){
            Log.d(TAG,"adaptador nulo");
        }
        listaView.setAdapter(adaptador);

        listaView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //playAudio(position);
                Log.d(TAG,position + "");
            }
        });

   }

    protected void loadAudio() {

        listaCanciones = new ArrayList<Audio>();

        String path = getCacheDir().getPath();
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: " + files.length);
        for (int i = 0; i < files.length; i++) {
            Log.d("Files", "FileName:" + files[i].getName());

                    String data = files[i].getAbsolutePath();
                    String title = files[i].getName();
                    String album = null;
                    String artist = null;

                    // Guardar en listaCanciones
                    listaCanciones.add(new Audio(data, title, album, artist));
                }
    }


}
