package uk.ac.aber.movementrecorder.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

import uk.ac.aber.movementrecorder.R;

/**
 * Created by einar on 16/07/2018.
 */

public class ListPickerAdapter extends BaseAdapter {

    private String[] sensors;
    private Context context;
    private static boolean[] checkboxstate;

    public ListPickerAdapter(Context context, String[] sensors, ArrayList<String> chosenSensors) {
        this.context = context;
        this.sensors = sensors;
        checkboxstate = new boolean[sensors.length];
        for(int i = 0; i<sensors.length;i++) {
            if (chosenSensors.contains(sensors[i]))
                checkboxstate[i] = true;
            else
                checkboxstate[i] = false;
        }
    }

    @Override
    public int getCount() {
        return sensors.length;
    }

    @Override
    public Object getItem(int position) {
        return sensors[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public boolean[] getCheckboxstate() {
        return checkboxstate;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = View.inflate(context, R.layout.sensor_list_item, null);

        final CheckBox cbSelected = view.findViewById(R.id.cbSelected);
        cbSelected.setChecked(checkboxstate[position]);
        cbSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((CheckBox)v).isChecked()){
                    checkboxstate[position]=true;
                    //v.setSelected(true);
                }else{
                    checkboxstate[position]=false;
                    //v.setSelected(false);
                }
            }
        });


        TextView tvSensorName = view.findViewById(R.id.tvSensorName);
        tvSensorName.setText(sensors[position]);
        tvSensorName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cbSelected.isChecked()){
                    checkboxstate[position]=false;
                    cbSelected.setChecked(false);
                    //getCheckbox(position).setChecked(false);
                }else{
                    checkboxstate[position]=true;
                    cbSelected.setChecked(true);
                    //getCheckbox(position).setChecked(true);
                }
            }
        });

        return view;
    }


}
