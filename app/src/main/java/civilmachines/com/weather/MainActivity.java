package civilmachines.com.weather;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;

import data.CityPreference;
import data.JSONWeatherParser;
import data.WeatherHTTPClient;
import model.Weather;
import util.Utils;

public class MainActivity extends AppCompatActivity {

    private TextView cityName;
    private TextView temp;
    private ImageView iconView;
    private TextView description;
    private TextView humidity;
    private TextView pressure;
    private TextView wind;
    private TextView sunrise;
    private TextView sunset;
    private TextView updated;


    Weather weather = new Weather();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cityName = (TextView)findViewById(R.id.city_text);
        temp = (TextView)findViewById(R.id.tempText);
        iconView = (ImageView)findViewById(R.id.thumbnailIcon);
        description = (TextView)findViewById(R.id.cloudText);
        humidity = (TextView)findViewById(R.id.humidText);
        pressure = (TextView)findViewById(R.id.pressureText);
        wind = (TextView)findViewById(R.id.windText);
        sunrise = (TextView)findViewById(R.id.riseText);
        sunset = (TextView)findViewById(R.id.setText);
        updated = (TextView)findViewById(R.id.updateText);

        CityPreference cityPreference = new CityPreference(MainActivity.this);

        renderWeatherData("Surat,IN");
    }


    public void renderWeatherData(String city){
        WeatherTask weatherTask = new WeatherTask();
        weatherTask.execute(new String[]{city + "&appid="+"786c4f66acce475cd60eefb333f36bdd"+"&units=metric"});
    }

    private class WeatherTask extends AsyncTask<String, Void , Weather>{

        @Override
        protected Weather doInBackground(String... strings) {

            String data = ((new WeatherHTTPClient().getWeatherData(strings[0])));
            weather = JSONWeatherParser.getWeather(data);
            weather.iconData = weather.currentCondition.getIcon();



            Log.v("Data: " , weather.place.getCity());

            new DownloadImageAsyncTask().execute(weather.iconData);
            return weather;
        }

        @Override
        protected void onPostExecute(Weather weather) {

            super.onPostExecute(weather);


            DateFormat df = DateFormat.getTimeInstance();
            String sunriseDate = df.format( new Date(weather.place.getSunrise()));
            String sunsetDate = df.format(new Date(weather.place.getSunset()));
            String updateDate = df.format(new Date(weather.place.getLastUpdate()));

            DecimalFormat decimalFormat = new DecimalFormat("#.#");
            String tempFormat = decimalFormat.format(weather.currentCondition.getTemperature());

            cityName.setText(weather.place.getCity() + "," + weather.place.getCountry());
            temp.setText("" + tempFormat + "C");
            humidity.setText("Humidity: "  + weather.currentCondition.getHumidity() + "%" );
            pressure.setText("Pressure: " + weather.currentCondition.getPressure() + "hPa");
            wind.setText("Wind: " + weather.wind.getSpeed() + "mps");
            sunrise.setText("Sunrise: " + sunriseDate);
            sunset.setText("Sunset: " + sunsetDate);
            updated.setText("Last Updated: " + updateDate);
            description.setText("Condition: " + weather.currentCondition.getCondition() + " (" +
            weather.currentCondition.getDescription() + ")");

        }
    }

    private void showInputDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Change city");

        final EditText cityInput = new EditText(MainActivity.this);
        cityInput.setInputType(InputType.TYPE_CLASS_TEXT);
        cityInput.setHint("Delhi,IN");
        builder.setView(cityInput);
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CityPreference cityPreference = new CityPreference(MainActivity.this);

                cityPreference.setCity(cityInput.getText().toString());

                String newCity = cityPreference.getCity();

                renderWeatherData(newCity);
            }
        });

        builder.show();
    }

    private class DownloadImageAsyncTask extends AsyncTask<String,Void,Bitmap>{
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            iconView.setImageBitmap(bitmap);
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            return downloadImage(strings[0]);
        }

        private Bitmap downloadImage(String code){
            final DefaultHttpClient client = new DefaultHttpClient();

            final HttpGet getRequest =new HttpGet(Utils.ICON_URL + code + ".png");

            try {
                HttpResponse response = client.execute(getRequest);

                final int statusCode =response.getStatusLine().getStatusCode();

                if(statusCode != HttpStatus.SC_OK){
                    Log.e("Download Image" , "Error: " + statusCode);
                    return null;
                }
                final HttpEntity entity = response.getEntity();
                if(entity != null){
                    InputStream inputStream = null;

                    inputStream = entity.getContent();

                    //decode contents from stream
                    final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    return bitmap;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.change_city) {
            showInputDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
