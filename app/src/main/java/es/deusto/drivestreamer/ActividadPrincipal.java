package es.deusto.drivestreamer;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.* ;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.api.Scope;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;


public class ActividadPrincipal extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123123;
    private static final String TAG = "Conectarse a drive";
    protected static ServicioReproductor reproductor;
    boolean servicioConectado = false;
    protected static ArrayList<Audio> listaCanciones =new ArrayList<Audio>();
    public static final String Broadcast_PLAY_NEW_AUDIO = "es.deusto.drivestreamer.PlayNewAudio";
    protected static ListView listView;
    protected static ArrayList<String> nombreCanciones = new ArrayList<>();
    protected static ArrayList<String> nombreCancionesReproductor = new ArrayList<>();
    protected static ArrayAdapter adaptador;
    protected static  Button botonGDrive;
    protected GoogleDriveCanciones gDrive;
    protected TextView textViewCancion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadAudio();
        gDrive = new GoogleDriveCanciones(getCacheDir().getPath());
        gDrive.loadAudio();
        cargarVistaCanciones();


    }


    public void cargarVistaCanciones(){
        listView = (ListView) findViewById(R.id.listaCanciones);
        textViewCancion = (TextView) findViewById(R.id.textViewCancion);

        for(int i = 0;i<listaCanciones.size();i++){
            nombreCanciones.add(listaCanciones.get(i).getTitle());

        }
        nombreCancionesReproductor=new ArrayList<>(nombreCanciones);

        adaptador = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, nombreCancionesReproductor);
        listView.setAdapter(adaptador);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                playAudio(position);
            }
        });

        ImageButton botonRewind = (ImageButton) findViewById(R.id.botonRewind);
        botonRewind.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
               reproductor.skipToPrevious();
            }
        });

        ImageButton botonPausa = (ImageButton) findViewById(R.id.botonPausa);
        botonPausa.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if(reproductor.reproduciendo()){
                    reproductor.pauseMedia();
                } else{
                    reproductor.resumeMedia();
                }
            }
        });

        ImageButton botonForward = (ImageButton) findViewById(R.id.botonForward);
        botonForward.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                reproductor.skipToNext();
            }
        });

        Button botonDescargarGDrive = (Button) findViewById(R.id.botonDescargarGDrive);
        botonDescargarGDrive.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), GoogleDriveDescarga.class);
                startActivity(intent);
            }
        });

        botonGDrive = (Button) findViewById(R.id.botonGDrive);
        botonGDrive.setText("GDrive");
        botonGDrive.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                adaptador.clear();
                if(botonGDrive.getText() == "GDrive"){
                    gDrive.loadAudio();
                    if(servicioConectado) {
                        reproductor.setAudioList(GoogleDriveCanciones.listaCanciones);
                    }
                    nombreCancionesReproductor = new ArrayList<>(GoogleDriveCanciones.nombreCanciones);
                    adaptador.addAll(nombreCancionesReproductor);
                    botonGDrive.setText("Local");
                }else {
                    if (servicioConectado) {
                        reproductor.setAudioList(ActividadPrincipal.listaCanciones);
                    }
                    nombreCancionesReproductor = new ArrayList<>(ActividadPrincipal.nombreCanciones);
                    adaptador.addAll(nombreCancionesReproductor);
                    botonGDrive.setText("GDrive");
                }

            }
        });
    }


    // Conectando el cliente al servicio AudioPlayer
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ServicioReproductor.LocalBinder binder = (ServicioReproductor.LocalBinder) service;
            reproductor = binder.getService();
            servicioConectado  = true;

            //Toast.makeText(ActividadPrincipal.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            servicioConectado  = false;
        }
    };

    private void playAudio(int audioIndex) {
        textViewCancion.setText(nombreCancionesReproductor.get(audioIndex));
        // Comprueba si el servicio esta activo y lo inicia si no esta
        if (!servicioConectado ) {
            //Store Serializable audioList to SharedPreferences
            UtilidadAlmacenamiento storage = new UtilidadAlmacenamiento(getApplicationContext());
            if(botonGDrive.getText() == "GDrive") {
                storage.storeAudio(listaCanciones);
            } else{
                storage.storeAudio(GoogleDriveCanciones.listaCanciones);
            }
            storage.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(this, ServicioReproductor.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Store the new audioIndex to SharedPreferences
            UtilidadAlmacenamiento storage = new UtilidadAlmacenamiento(getApplicationContext());
            storage.storeAudioIndex(audioIndex);

            // El servicio esta activo
            // Manda al servicio el broadcast -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
    }

    // Estos tres metodos guardan y restauran el estado del servicio cuando el usuario cierra/abre la aplicacion
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", servicioConectado);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        servicioConectado = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (servicioConectado) {
            unbindService(serviceConnection);
            // El servicio esta activo
            reproductor.stopSelf();
        }
    }


    // Carga las canciones almacenadas en el movil al ArrayList de canciones
    private void loadAudio() {
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";


        // Pide permiso para acceder a los archivos
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to read the contacts
            }

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

            // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
            // app-defined int constant that should be quite unique

            return;
        }

        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            listaCanciones = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                // Guardar en listaCanciones
                listaCanciones.add(new Audio(data, title, album, artist));
            }
        }
        cursor.close();
    }



}
