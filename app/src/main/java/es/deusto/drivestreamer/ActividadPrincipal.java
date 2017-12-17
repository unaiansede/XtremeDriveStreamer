package es.deusto.drivestreamer;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;


public class ActividadPrincipal extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123123;
    private ServicioReproductor reproductor;
    boolean servicioConectado = false;
    ArrayList<Audio> listaCanciones;
    public static final String Broadcast_PLAY_NEW_AUDIO = "es.deusto.drivestreamer.PlayNewAudio";





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ImageButton buttonOne = (ImageButton) findViewById(R.id.Play);
        buttonOne.setOnClickListener(new ImageButton.OnClickListener() {
            public void onClick(View v) {
                if(true) {

                }

            }
        });
        //playAudio("https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg");

        loadAudio();
        playAudio(5);
        //play the first audio in the ArrayList
        //playAudio(listaCanciones.get(0).getData());

    }


    // Conectando el cliente al servicio AudioPlayer
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ServicioReproductor.LocalBinder binder = (ServicioReproductor.LocalBinder) service;
            reproductor = binder.getService();
            servicioConectado  = true;

            Toast.makeText(ActividadPrincipal.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            servicioConectado  = false;
        }
    };


    private void playAudio(int audioIndex) {
        // Comprueba si el servicio esta activo y lo inicia si no esta
        if (!servicioConectado ) {
            //Store Serializable audioList to SharedPreferences
            UtilidadAlmacenamiento storage = new UtilidadAlmacenamiento(getApplicationContext());
            storage.storeAudio(listaCanciones);
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
