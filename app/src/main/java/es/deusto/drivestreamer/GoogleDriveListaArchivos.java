package es.deusto.drivestreamer;

/**
 * Created by UNAI on 2018/01/22.
 */
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ListView;

import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.widget.DataBufferAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

/**
 * An activity to illustrate how to query files.
 */
public class GoogleDriveListaArchivos extends GoogleDrive {
    private static final String TAG = "QueryFiles";

    private DataBufferAdapter<Metadata> mResultsAdapter;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_contenido_drive);
        ListView mListView = (ListView) findViewById(R.id.listContenido);
        mResultsAdapter = new ResultsAdapter(this);
        mListView.setAdapter(mResultsAdapter);
    }

    @Override
    protected void onDriveClientReady() {
        listFiles();
    }

    /**
     * Clears the result buffer to avoid memory leaks as soon
     * as the activity is no longer visible by the user.
     */
    @Override
    protected void onStop() {
        super.onStop();
        mResultsAdapter.clear();
    }

    /**
     * Retrieves results for the next page. For the first run,
     * it retrieves results for the first page.
     */
    private void listFiles() {
        Query query = new Query.Builder()
                //.addFilter(Filters.eq(SearchableField.MIME_TYPE, "text/plain"))
                .build();
        // [START query_files]
        Task<MetadataBuffer> queryTask = getDriveResourceClient().query(query);
        // [END query_files]
        // [START query_results]
        queryTask
                .addOnSuccessListener(this,
                        new OnSuccessListener<MetadataBuffer>() {
                            @Override
                            public void onSuccess(MetadataBuffer metadataBuffer) {
                                // Handle results...
                                // [START_EXCLUDE]
                                System.out.println("añadido");
                                mResultsAdapter.append(metadataBuffer);
                                // [END_EXCLUDE]
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure...
                        // [START_EXCLUDE]
                        Log.e(TAG, "Error retrieving files", e);
                        showMessage(getString(R.string.query_failed));
                        finish();
                        // [END_EXCLUDE]
                    }
                });
        // [END query_results]

    }
}