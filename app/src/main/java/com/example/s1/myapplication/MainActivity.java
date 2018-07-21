package com.example.s1.myapplication;

import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextView frase, txtCategoria,urlTextView;
    private Button btnGenerar, btnHablar;
    private Spinner spinnerCategoria;
    private String uRLCategorias="https://api.chucknorris.io/jokes/categories";
    //String url ="https://api.chucknorris.io/jokes/random";
    private ArrayAdapter<String> adaptador;
    private List<String> categoriasArray;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frase = (TextView)findViewById(R.id.frase);
        txtCategoria = (TextView)findViewById(R.id.txtCategoria);
        urlTextView =(TextView)findViewById(R.id.urlTxtView);
        btnGenerar = (Button) findViewById(R.id.btnGenerar);
        btnHablar =(Button) findViewById(R.id.btnHablar);
        spinnerCategoria = (Spinner) findViewById(R.id.categoria);


        tts = new TextToSpeech(this, this);

        cargarCategorias();
        generarFrase();

        //cada vez que se pulse el boton genera una nueva frase
        btnGenerar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generarFrase();
            }
        });

        //al seleccionar una nueva categoria cambia la URL que genera frases
        spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {

                txtCategoria.setText("Categoria actual: " + parent.getItemAtPosition(position));
                urlTextView.setText("https://api.chucknorris.io/jokes/random?category=" + parent.getItemAtPosition(position));
                //Log.e("URL", urlTextView.getText().toString());
                //actualizar el adaptador del spinner
                adaptador.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnHablar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                hablar();
            }

        });

    }

    protected void generarFrase(){

        //libreria volley de android para no tener que trabajar con tareas asincronas
        RequestQueue requestQueue=Volley.newRequestQueue(this);

        //se hace el request a la web
        JsonObjectRequest objectRequest=new JsonObjectRequest(
                Request.Method.GET,
                //la url cambia según la categoría
                urlTextView.getText().toString(),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //Log.e("rest response",response.toString());
                            //se coge solo la parte value del JSON
                            frase.setText(response.get("value").toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("rest response",error.toString());
                    }
                }

        );
        requestQueue.add(objectRequest);

    }

    protected void cargarCategorias(){

        categoriasArray =  new ArrayList<String>();

        adaptador = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, categoriasArray);

        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerCategoria.setAdapter(adaptador);

        RequestQueue requestQueue=Volley.newRequestQueue(this);

        //se inicia la peticion del JSON a la api
        JsonArrayRequest objectRequest= new JsonArrayRequest(
                Request.Method.GET,
                uRLCategorias,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                            //se divide el JSONArray y se añade cada palabra al arraylist del spinner
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    categoriasArray.add(response.get(i).toString());
                                    adaptador.notifyDataSetChanged();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("rest response",error.toString());
                    }
                }

        );
        requestQueue.add(objectRequest);

    }

    @Override
    public void onInit(int estado) {
        if (estado == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.UK);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                btnHablar.setEnabled(true);
                hablar();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }

    //funcion que pasa el textview de la frase a voz
    private void hablar() {

        String text = frase.getText().toString();

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
}
