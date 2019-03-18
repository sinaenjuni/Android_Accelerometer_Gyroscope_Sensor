package com.example.y2k43.sensor;

import android.app.ListActivity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.List;

public class MainActivity extends ListActivity {
    SensorManager manager;
    List<Sensor> sensors;

    SensorListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensors = manager.getSensorList(Sensor.TYPE_ACCELEROMETER);

        //sensors = manager.getSensorList(Sensor.TYPE_GYROSCOPE);

        adapter = new SensorListAdapter(this, R.layout.sensor_item, sensors);
        setListAdapter(adapter);

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Sensor sensor = sensors.get(position);
        String sensorName = sensor.getName();

        Intent intent = new Intent(this, DataActivity.class);
        intent.putExtra("SensorIndex", position);
        intent.putExtra("SensorName", sensorName);
        startActivity(intent);
    }
}
