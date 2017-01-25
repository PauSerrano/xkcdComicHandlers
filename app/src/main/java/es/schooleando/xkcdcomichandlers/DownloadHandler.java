package es.schooleando.xkcdcomichandlers;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.JsonReader;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Random;

import static android.content.ContentValues.TAG;

public class DownloadHandler extends Handler {

    private Handler responseHandler;

    private String urlImageString;
    private String pathImagen;

    public DownloadHandler(Looper looper) {
        super(looper);
    }

    public void setResponseHandler(Handler responseHandler) {
        this.responseHandler = responseHandler;
    }


    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case (Constantes.DOWNLOAD):
                downloadImage();
                break;
        }


        // No es necesario procesar Runnables luego no llamamos a super.handleMessage(msg)
    }


    private void downloadImage() {
        // nos descargará una imagen y una vez descargada enviaremos un mensaje LOAD_IMAGE al UI Thread indicando
        // la URI del archivo descargado.


        try {

            Random random = new Random();
            int numComic = random.nextInt(1000);
            String urlJson = "https://xkcd.com/"+numComic+"/info.0.json";
            urlImageString = obtenerUrlStringImage(urlJson);
            pathImagen = obtenerPathImagen(urlImageString);

        } catch (IOException e) {
            e.printStackTrace();
            Message msg = new Message();
            Bundle dataFin = new Bundle();
            String errorString = e.getMessage();
            dataFin.putString("pathImagen", errorString);
            msg.what = Constantes.ERROR;
            msg.setData(dataFin);
            responseHandler.sendMessage(msg);
        }

        // Reutilizad código de prácticas anteriores aquí

        // También enviaremos mensajes PROGRESS al UI Thread mediante responseHandler.sendMessage() indicando el porcentaje de progreso, si hay.
        // Enviaremos mensajes ERROR, en caso de que haya un error en la conexión, descarga, etc...


    }

    private String obtenerUrlStringImage (String urlJson) throws IOException {

        String urlStringImage;

        //  1. Para descargar el resultado JSON para leer la URL.
        URL url = new URL(urlJson);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        //Realizamos la peticion de la descarga del Json
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();
        InputStream inJson = new BufferedInputStream(urlConnection.getInputStream());
        //Guardamos el json en un objeto JsonReader para facilitar la lectura en el caso qeu fueran varios
        JsonReader jsonReader = new JsonReader(new InputStreamReader(inJson, "UTF-8"));
        //metodo creado para obtener la url de la imagen que contiene el json obtenido del inputStream
        urlStringImage = leerJson(jsonReader);
        jsonReader.close();

        return urlStringImage;
    }

    public String leerJson (JsonReader reader)throws IOException {

        String url = null;

        reader.beginObject();
        while (reader.hasNext()) {

            String name = reader.nextName();
            switch (name) {
                case "img":
                    url = reader.nextString();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return url;
    }

    private String obtenerPathImagen (String urlStringImage) throws IOException {
        String pathImagen;

        //  2. Una vez tenemos la URL descargar la imagen en la carpeta temporal.
        URL urlImagen = new URL(urlStringImage);
        HttpURLConnection urlConnectionDescarga = (HttpURLConnection) urlImagen.openConnection();

        //Realizamos la peticion de la descarga de la imagen
        urlConnectionDescarga.setRequestMethod("GET");
        urlConnectionDescarga.connect();
        String tipo = urlConnectionDescarga.getContentType();
        int tamañoRecurso = urlConnectionDescarga.getContentLength();

        if (tipo.startsWith("image/")) {
            InputStream is = urlConnectionDescarga.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int n = 0;
            int total = 0;
            //Mientras el resultado de lectura del buffer sea distinto a -1
            Log.d(TAG, "Empieza la descarga de la imagen");
            while ((n = is.read(buffer)) != -1) {
                //Escribimos los bytes
                bos.write(buffer, 0, n);

                //Estado de la descarga en progreso
                total += n;
                if (tamañoRecurso != -1) {

                    Integer porc = (total * 100) / tamañoRecurso;
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    data.putString("porcentaje", String.valueOf(porc));
                    msg.what = Constantes.PROGRESS;
                    msg.setData(data);
                    responseHandler.sendMessage(msg);
                } else {
                    Integer porc = total;
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    data.putString("porcentaje", String.valueOf(porc));
                    msg.what = Constantes.PROGRESS;
                    msg.setData(data);
                    responseHandler.sendMessage(msg);
                }

            }//while

            //cerramos los Streams
            bos.close();
            is.close();
            //++Obtenemos el path de la imagen para pasarlo con el Bundle (y no el bitmap)
            String[] data = urlStringImage.split("/");
            String[] f = data[data.length - 1].split("\\.");

            if (f.length < 2) {
                f = new String[]{"unknown", "jpg"};
            }
            //directorio de la caché
            File directory = Environment.getExternalStorageDirectory();

            //creo el fichero temporal
            File temporalFile = File.createTempFile(f[0], "." + f[1], directory);

            //lo elimino al salir
            temporalFile.deleteOnExit();

            //creo el stream sobre el fichero temporal
            FileOutputStream fos = new FileOutputStream(temporalFile);
            fos.write(bos.toByteArray());

            pathImagen = temporalFile.getPath();

            //byte[] arrayImagen = bos.toByteArray();
            //imagenBmpDescargada = BitmapFactory.decodeByteArray(arrayImagen, 0, arrayImagen.length);

            Log.d(TAG, "FINALIZA la descarga de la imagen");
            Message msg = new Message();
            Bundle dataFin = new Bundle();
            dataFin.putString("pathImagen", pathImagen);
            msg.what = Constantes.LOAD_IMAGE;
            msg.setData(dataFin);
            responseHandler.sendMessage(msg);

        }else {
            Log.d(TAG, "Servicio NO Correcto! la url no corresponde a una imagen");
            Message msg = new Message();
            Bundle dataFin = new Bundle();
            String errorUrl = "La url no corresponde a una imagen";
            dataFin.putString("pathImagen", errorUrl);
            msg.what = Constantes.ERROR;
            msg.setData(dataFin);
            responseHandler.sendMessage(msg);
            pathImagen=null;
        }


        return pathImagen;


    }



}
