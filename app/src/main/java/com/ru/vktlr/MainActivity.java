package com.ru.vktlr;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private String TAG_HTTP = "HTTP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String str = this.readTxt();
        JSONArray jsonArray = parseJson(str);
        JSONObject today = getTodayData(jsonArray);
        setTimes(today);
        String stringUrl = "http://52.27.138.37:8080/marks?id_staff=2&" +
                "apikey=6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b&" +
                "token=wasFRYmtUu1WvOKJ8x6iSkucKy4ZQTxWE9xE9YhD9ocPvfQbDgUO9QXLihjbGFcvP1edb4TicdHWzh2SibRv4VIWEF5aNRbsYP8F" +
                "&class=12&id_discipline=6&date=2015-09-29";
    }

    private String readTxt() {

        InputStream inputStream = getResources().openRawResource(R.raw.times_array);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int i;
        try {
            i = inputStream.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return byteArrayOutputStream.toString();
    }

    private JSONArray parseJson(String json) {
        JSONArray jsonArray = new JSONArray();

        try {
            jsonArray = new JSONArray(json);
        } catch (JSONException e) {
        }
        return jsonArray;
    }

    private JSONObject getTodayData(JSONArray jsonArray) {
        JSONObject jsonObject = new JSONObject();
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH) + 1;
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                jsonObject = jsonArray.getJSONObject(i);
                int todayDay = jsonObject.getInt("day");
                int todayMonth = jsonObject.getInt("month");
                if (todayDay == day && todayMonth == month) {
                    return jsonObject;
                }
            } catch (JSONException e) {
            }
        }
        return jsonObject;
    }

    private void setTimes(JSONObject jsonObject) {
        try {
            int start1 = jsonObject.getInt("start1"),
                    end1 = jsonObject.getInt("end1"),
                    start2 = jsonObject.getInt("start2"),
                    start3 = jsonObject.getInt("start3"),
                    start4 = jsonObject.getInt("start4"),
                    start5 = jsonObject.getInt("start5"),
                    tillEnd = getTillEndTime(start1, end1, start2, start3, start4, start5);
            TextView tStart1 = (TextView) findViewById(R.id.start1);
            TextView tEnd1 = (TextView) findViewById(R.id.end1);
            TextView tStart2 = (TextView) findViewById(R.id.start2);
            TextView tStart3 = (TextView) findViewById(R.id.start3);
            TextView tStart4 = (TextView) findViewById(R.id.start4);
            TextView tStart5 = (TextView) findViewById(R.id.start5);
            TextView tTillEnd = (TextView) findViewById(R.id.tillEnd);
            TextView tTillEndTitle = (TextView) findViewById(R.id.tillEndTitle);

            tStart1.setText(getStringTime(start1));
            tEnd1.setText(getStringTime(end1));
            tStart2.setText(getStringTime(start2));
            tStart3.setText(getStringTime(start3));
            tStart4.setText(getStringTime(start4));
            tStart5.setText(getStringTime(start5));
            if (tillEnd < 0) {
                tillEnd *= -1;
                tTillEnd.setText(getStringTime(tillEnd));
                tTillEndTitle.setText(getResources().getString(R.string.tillStart));
            } else {
                tTillEnd.setText(getStringTime(tillEnd));
                tTillEndTitle.setText(getResources().getString(R.string.tillEnd));
            }
        } catch (JSONException e) {
        }

    }

    /**
     * @param time - value of time in minutes, integer
     * @return - string, time format
     */
    private String getStringTime(int time) {
        int hours = time / 60,
                minutes = time % 60;
        String answer = hours < 10 ? "0" + hours : "" + hours;
        answer += minutes < 10 ? ":0" + minutes : ":" + minutes;
        return answer;
    }

    private int getTillEndTime(int start1, int end1, int start2, int start3, int start4, int start5) {
        Calendar cal = Calendar.getInstance();
        System.out.println(cal.toString() + " now cal is");
        int mins = cal.get(Calendar.MINUTE);
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        int nowMins = mins + hours * 60;
        System.out.println(nowMins + " nowMins");
        if (start5 > 24 * 60 && nowMins < start5 % 24 * 60 && nowMins < start1) {
            //Если 5 начинается после полуночи
            System.out.println("before 5, after 00:00");
            return start5 % 24 * 60 - nowMins;
        }
        if (nowMins < start1) {
            System.out.println("before 1");

            return start1 - nowMins;
        }
        if (nowMins < end1) {
            System.out.println("before end 1");
            return end1 - nowMins;
        }
        if (nowMins < start2) {
            //Хочу этот случай обработать отдельно, чтобы показать "До начала:"
            System.out.println("before 2, minus");
            return nowMins - start2;
        }
        if (nowMins < start3) {
            System.out.println("before 3");
            return start3 - nowMins;
        }
        if (nowMins < start4) {
            System.out.println("before 4");
            return start4 - nowMins;
        }
        if (nowMins < start5) {
            System.out.println("before 5");
            return start5 - nowMins;
        }
        System.out.println("before 5 till tomorrow");
        return start1 + 24 * 60 - nowMins;
    }


}