package walquiria.tmarco1;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Controller extends AppCompatActivity {

    private LinearLayout baseProgressBar;
    private ClimaBairo climas = new ClimaBairo();
    public Context context = Controller.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view);
        baseProgressBar = (LinearLayout)findViewById(R.id.baseProgressBar);
        climas.execute();
    }

    private class ClimaBairo extends AsyncTask<String, Integer, String> {

        public ArrayList dadosWS= new ArrayList();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            baseProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            String url = "https://metroclimaestacoes.procempa.com.br/metroclima/seam/resource/rest/externalRest/ultimaLeitura";
            try {
                HttpURLConnection conexao = conectar(url);
                int resposta = conexao.getResponseCode();

                if (resposta ==  HttpURLConnection.HTTP_OK) {
                    InputStream is = conexao.getInputStream();

                    JSONArray json = new JSONArray(bytesParaString(is));
                    for(int i=0;i<json.length();i++){
                        try {
                            JSONObject jsonObject=json.getJSONObject(i);
                            dadosWS.add(json.getJSONObject(i).getString("bairro")+" (MIN:"+json.getJSONObject(i).getInt("temperaturaMinimaPrevisao")+" - MAX:"+json.getJSONObject(i).getInt("temperaturaMaximaPrevisao")+")");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("TAG_ASYNC_TASK", e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            baseProgressBar.setVisibility(View.INVISIBLE);
            ListView lista = (ListView) findViewById(R.id.lista);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, dadosWS);
            lista.setAdapter(adapter);
        }

        private String bytesParaString(InputStream is) throws IOException {
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream bufferzao = new ByteArrayOutputStream();
            int bytesLidos;
            while ((bytesLidos = is.read(buffer)) != -1) {
                bufferzao.write(buffer, 0, bytesLidos);
            }
            return new String(bufferzao.toByteArray(), "UTF-8");
        }


        private HttpURLConnection conectar(String urlArquivo) throws IOException {
            final int SEGUNDOS = 1000;
            URL url = new URL(urlArquivo);
            HttpURLConnection conexao = (HttpURLConnection)url.openConnection();
            conexao.setReadTimeout(10 * SEGUNDOS);
            conexao.setConnectTimeout(15 * SEGUNDOS);
            conexao.setRequestMethod("GET");
            conexao.setDoInput(true);
            conexao.setDoOutput(false);
            conexao.connect();
            return conexao;
        }
    }
}
