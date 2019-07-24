package uk.ac.aber.movementrecorder.UI;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;


import uk.ac.aber.movementrecorder.R;

import static uk.ac.aber.movementrecorder.UI.MainActivity.PREFS_NAME;

/**
 * Created by einar on 08/07/2018.
 */

public class PersonalDataFragment extends Fragment {

    // Obj
    private TextView nYob;
    private EditText nHeight;
    private EditText nWeight;
    private EditText etParticipantID;
    private ImageButton ibPersonalDataDone;

    private NumberPicker birthYear;
    private TextView tvYearPicked;

    private String yearOfBirth = "";

    private PersonalDataFragment.IPersonalDataFragment iPersonalDataFragment;


    public interface IPersonalDataFragment {
        void dataAdded(String id, String age, String height, String weight);
    }

    /**
     * Click listener for the buttons in the fragment
     */
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ibParticipantInfoDone:
                    if (isInfoEntered()) {
                        iPersonalDataFragment.dataAdded(getID(), getYOB(), getHeight(), getWeight());
                        storeValues();
                        Toast.makeText(getActivity(), "Data Saved", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.nYob:
                    yobDialog();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * Stores the data in shared preferences
     */
    private void storeValues() {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(getString(R.string.participantid_key), getID());
        editor.putString(getString(R.string.yob_key), getYOB());
        editor.putString(getString(R.string.height_key), getHeight());
        editor.putString(getString(R.string.weight_key), getWeight());
        editor.commit();
    }

    public void setData() {
        String pYob, pHeight, pWeight, pParticipantID;
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        pParticipantID = prefs.getString(getString(R.string.participantid_key), null);
        pYob = prefs.getString(getString(R.string.yob_key), null);
        pHeight = prefs.getString(getString(R.string.height_key), null);
        pWeight = prefs.getString(getString(R.string.weight_key), null);

        if (pParticipantID != null && pYob != null && pHeight != null && pWeight != null) {
            etParticipantID.setText(pParticipantID);
            nYob.setText(pYob);
            yearOfBirth = pYob;
            nHeight.setText(pHeight);
            nWeight.setText(pWeight);
        }
    }

    private NumberPicker.OnValueChangeListener valueChangedListener = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            StringBuilder sb = new StringBuilder(tvYearPicked.getText().toString());
            if (picker == birthYear) {
                sb.replace(0, 4, Integer.toString(newVal));
            }
            tvYearPicked.setText(sb.toString());
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.personal_data_fragment, container, false);

        nYob = view.findViewById(R.id.nYob);
        nYob.setOnClickListener(clickListener);
        nHeight = view.findViewById(R.id.nHeight);
        nWeight = view.findViewById(R.id.nWeight);
        etParticipantID = view.findViewById(R.id.etParticipantID);
        ibPersonalDataDone = view.findViewById(R.id.ibParticipantInfoDone);
        ibPersonalDataDone.setOnClickListener(clickListener);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        iPersonalDataFragment = (IPersonalDataFragment) context;
    }

    @Override
    public void onResume() {
        setData();
        super.onResume();
    }

    private boolean isInfoEntered() {
        if (getYOB().length() != 4) {
            Toast.makeText(getContext(), "Please write the year of birth as YYYY.", Toast.LENGTH_LONG).show();
            return false;
        }
        else {
            if (Integer.parseInt(getYOB()) < 1900) {
                Toast.makeText(getContext(), Integer.parseInt(getYOB()) + " is not a valid year", Toast.LENGTH_LONG).show();
                return false;
            }
            else if (Integer.parseInt(getYOB()) > 2030) {
                Toast.makeText(getContext(), Integer.parseInt(getYOB()) + " is not a valid year", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        if (getID().isEmpty() || getYOB().isEmpty() || getHeight().isEmpty() || getWeight().isEmpty()) {
            Toast.makeText(getContext(), "Please enter all the data.", Toast.LENGTH_LONG).show();
            return false;
        }
        else
            return true;

    }

    private void yobDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getActivity().getLayoutInflater().inflate(R.layout.year_picker_fragment, null);
        tvYearPicked = view.findViewById(R.id.tvYearPicked);
        birthYear = view.findViewById(R.id.birthYear);
        birthYear.setMinValue(1900);
        birthYear.setMaxValue(2018);
        birthYear.setValue(1990);
        birthYear.setOnValueChangedListener(valueChangedListener);
        ImageButton ibDone = view.findViewById(R.id.ibDone);
        builder.setView(view);
        final AlertDialog yearDialog = builder.create();
        ibDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                yearOfBirth = Integer.toString(birthYear.getValue());
                nYob.setText(yearOfBirth);
                yearDialog.dismiss();
            }
        });
        yearDialog.show();
        //return null;
    }

    private String getID() {
        return etParticipantID.getText().toString();
    }

    private String getHeight() {
        return nHeight.getText().toString();
    }

    private String getWeight() {
        return nWeight.getText().toString();
    }

    private String getYOB() {
        return yearOfBirth;
    }

}
