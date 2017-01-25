package es.schooleando.xkcdcomichandlers;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class ComicActivity extends AppCompatActivity {
    ComicManager manager;
    ImageView comicView;
    ProgressBar progressBar;
    Button butAccion;
    Button butSalir;


    Boolean accion=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        comicView = (ImageView)findViewById(R.id.imageView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        butAccion =(Button) findViewById(R.id.butActivar);
        butSalir =(Button) findViewById(R.id.butSalir);

        // Inicializamos el Comic
        manager = new ComicManager(comicView, progressBar, this);

        // Descargamos el primer comic
        //manager.start();
    }


    // Aquí faltará añadir Listeners para:
    // un botón de activar/desactivar Timer (manager.start(), manager.stop())
    // un botón para salir de la App
    public void activarDesactivarTimer(View view) {
        if (accion){
            manager.activarDescarga();
            accion = false;
            butAccion.setText("Desconectar");

        } else {
            manager.parardescarga();
            accion = true;
            butAccion.setText("Activar");
            progressBar.setVisibility(View.GONE);
        }
    }

    public void salirApp(View view) {
        manager.pararHandlerThread();
        finish();
    }

    @Override
    protected void onDestroy() {
        manager.pararHandlerThread();
        super.onDestroy();
    }
}
