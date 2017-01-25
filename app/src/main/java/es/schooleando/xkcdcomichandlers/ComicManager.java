package es.schooleando.xkcdcomichandlers;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * Created by ruben on 5/01/17.
 */

public class ComicManager {
    private HandlerThread downloadHandlerThread;
    private DownloadHandler downloadHandler; // Funcionará asociado al Worker Thread (HandlerThread)
    private ImageHandler imageHandler;            // Funcionará asociado al UI Thread

    private Context contexto;
    private Looper looper;
    private ImageView imageViewComic;
    private ProgressBar progressBarComic;

    public ComicManager(ImageView imageView, ProgressBar progressBar, Context context) {
        this.imageViewComic = imageView;
        this.progressBarComic = progressBar;
        this.contexto = context;
        downloadHandlerThread = new HandlerThread("myHandlerThread");
        downloadHandlerThread.start();
        looper = downloadHandlerThread.getLooper();
        downloadHandler = new DownloadHandler(looper);
        imageHandler = new ImageHandler(Looper.getMainLooper());

        imageHandler.setResponseHandler(downloadHandler);
        imageHandler.setResponseImageView(imageViewComic);
        imageHandler.setResponseContext(contexto);
        imageHandler.setResponseProgressBar(progressBarComic);
        downloadHandler.setResponseHandler(imageHandler);

        downloadComic();

    }

    public void activarDescarga() {
        /*if (!downloadHandlerThread.isAlive()){
            downloadHandlerThread.start();
            looper = downloadHandlerThread.getLooper();
            downloadHandler = new DownloadHandler(looper);

        }*/
        // Configuramos el tiempo en imageHandler
        imageHandler.initTimer(10);

        downloadComic();

    }

    public void parardescarga() {
        // TODO: Enviamos un Toast de que se está parando la descarga automática

        Toast.makeText(contexto, "Se ha parado la descarga automatica", Toast.LENGTH_LONG).show();
        // Desactivamos el timer deel imageHandler para que evite enviar mensajes retardados
        imageHandler.disableTimer();
        // TODO: Paramos el HandlerThread, limpiando su cola de mensajes y esperando a que acabe su trabajo activo si lo tiene
        //downloadHandlerThread.quitSafely();
        imageHandler.removeMessages(Constantes.LOAD_IMAGE);
    }

    public void pararHandlerThread(){

        downloadHandlerThread.quitSafely();
    }

    // enviamos un mensaje para descargar un Comic (cuando pulsemos sobre el imageView)
    public void downloadComic() {
        // TODO: crear mensaje
        Message msg = new Message();
        msg.what = Constantes.DOWNLOAD;
        downloadHandler.sendMessage(msg);

    }
}
