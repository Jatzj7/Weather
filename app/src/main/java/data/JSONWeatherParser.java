package data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import model.Place;
import model.Weather;
import util.Utils;

public class JSONWeatherParser {
    public static Weather getWeather(String data){
        Weather weather = new Weather();
        //create JSON object for data
        try {
            JSONObject jsonObject = new JSONObject(data);

            Place place = new Place();

            JSONObject coorobj = Utils.getObject("coord",jsonObject);
            place.setLat(Utils.getFloat("lat",coorobj));
            place.setLon(Utils.getFloat("lon",coorobj));

            //Get the Sys Obj
            JSONObject sysObj = Utils.getObject("sys",jsonObject);
            place.setCountry(Utils.getString("country",sysObj));
            place.setLastUpdate(Utils.getInt("dt",jsonObject));
            place.setSunrise(Utils.getInt("sunrise",sysObj));
            place.setSunset(Utils.getInt("sunset",sysObj));
            place.setCity(Utils.getString("name",jsonObject));

            weather.place = place;

            //Get Weather INfo
            JSONArray jsonArray = jsonObject.getJSONArray("weather");
            JSONObject jsonweather = jsonArray.getJSONObject(0);
            weather.currentCondition.setWeatherId(Utils.getInt("id",jsonweather));
            weather.currentCondition.setDescription(Utils.getString("description" , jsonweather));
            weather.currentCondition.setCondition(Utils.getString("main",jsonweather));
            weather.currentCondition.setIcon(Utils.getString("icon" , jsonweather));

            JSONObject mainObj = Utils.getObject("main",jsonObject);
            weather.currentCondition.setHumidity(Utils.getInt("humidity" , mainObj));
            weather.currentCondition.setTemperature(Utils.getDouble("temp",mainObj));
            weather.currentCondition.setPressure(Utils.getInt("pressure",mainObj));
            weather.currentCondition.setMinTemp(Utils.getFloat("temp_min",mainObj));
            weather.currentCondition.setMaxTemp(Utils.getFloat("temp_max",mainObj));

            JSONObject windObj = Utils.getObject("wind", jsonObject);
            weather.wind.setSpeed(Utils.getFloat("speed",windObj));
            weather.wind.setDeg(Utils.getFloat("deg" , windObj));

            JSONObject cloudObj = Utils.getObject("clouds",jsonObject);
            weather.clouds.setPrecipitation(Utils.getInt("all" , cloudObj));

            return weather;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
