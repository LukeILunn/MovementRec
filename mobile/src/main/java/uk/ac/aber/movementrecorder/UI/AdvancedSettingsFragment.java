package uk.ac.aber.movementrecorder.UI;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import uk.ac.aber.movementrecorder.R;

import static uk.ac.aber.movementrecorder.UI.MainActivity.PREFS_NAME;

/**
 * Created by einar on 23/07/2018.
 */

public class AdvancedSettingsFragment extends Fragment {

    private Button btAdRemoveData;

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btAdRemoveData:
                    clearSharedPreferences();
                    break;
                default:
                    break;
            }
        }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.advanced_settings_fragment, container, false);

        btAdRemoveData = view.findViewById(R.id.btAdRemoveData);
        btAdRemoveData.setOnClickListener(clickListener);



        return view;
    }

    private void clearSharedPreferences() {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.commit();

        Toast.makeText(getActivity(), "All data cleared from the device", Toast.LENGTH_LONG).show();
    }
}
