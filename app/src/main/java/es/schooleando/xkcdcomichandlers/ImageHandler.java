package es.schooleando.xkcdcomichandlers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;

import static es.schooleando.xkcdcomichandlers.R.id.imageView;

/**
 * Created by ruben on 24/01/17.
 */

public class ImageHandler extends Handler {
    Handler responseHandler;
    private boolean timerActive;             // Controlamos si el timer está activo o no
    private int seconds;                     // Segundos del timer
    private Context contexto;
    private ImageView imageView;
    private ProgressBar progressBarComic;

    public ImageHandler(Looper looper) {
        super(looper);
    }

    public void initTimer(int seconds) {
        this.timerActive = true;
        this.seconds = seconds;
    }

    public void disableTimer() {
        this.timerActive=false;
        this.seconds=0;
    }

    public void setResponseHandler(DownloadHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    public void setResponseContext (Context context){
        this.contexto = context;
    }

    public void setResponseImageView (ImageView imageView){
        this.imageView = imageView;
    }

    public void setResponseProgressBar (ProgressBar progressBar){
        this.progressBarComic = progressBar;
    }

    public void handleMessage(Message msg) {
        switch(msg.what) {
            case (Constantes.LOAD_IMAGE):
                // TODO: Obtenemos la URI del archivo temporal y cargamos el imageView
                Bundle data =  msg.getData();
                String pathImagen = data.getString("pathImagen");

                File archivo = new File(pathImagen);
                if (archivo.exists()){
                    progressBarComic.setVisibility(View.GONE);
                    Bitmap bmImg = BitmapFactory.decodeFile(pathImagen);
                    imageView.setImageBitmap(bmImg);
                }

                // si está activo el timer posteriormente enviaremos un mensaje retardado de DOWNLOAD_COMIC al HandlerThread, solo si está activo el Timer.
                if (timerActive) {
                    // TODO: terminar de construir el mensaje DOWNLOAD_COMIC
                    // downloadHandler.sendMessageDelayed(..., seconds);
                    Message msgDownload = new Message();
                    msgDownload.what = Constantes.DOWNLOAD;
                    responseHandler.sendMessageDelayed(msgDownload, seconds);

                }
                break;
            case (Constantes.PROGRESS):
                //progressBarComic.setVisibility(View.VISIBLE);
                Bundle datoPorcentaje = msg.getData();
                int porcentaje = Integer.parseInt(datoPorcentaje.getString("porcentaje")) ;
                // actualizaremos el progressBar

                progressBarComic.setVisibility(View.VISIBLE);
                progressBarComic.setProgress(porcentaje);

                break;
            case (Constantes.ERROR):
                Bundle datoError = msg.getData();
                String error = datoError.getString("pathImagen");
                Toast.makeText(contexto, error, Toast.LENGTH_LONG).show();
                // mostraremos un Toast del error. Cancelamos el Timer para evitar errores posteriores
                break;
        }
    }
}


