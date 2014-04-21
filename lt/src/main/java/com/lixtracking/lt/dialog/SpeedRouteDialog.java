package com.lixtracking.lt.dialog;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;

import com.lixtracking.lt.R;


@SuppressLint("ValidFragment")
public class SpeedRouteDialog extends DialogFragment implements OnClickListener{
	
	public Button negativeButton = null;
	public Button positiveButton = null;
	private String title = "Speed route";
    private NumberPicker numberPicker = null;
    int default_value = 1;

	public static interface Listener {
        void onPositiveButtonSpeedRoute(int value);
    }

    public SpeedRouteDialog(int value) {
        default_value = value;
    }

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState){
		getDialog().setTitle(title);
		View view = inflater.inflate(R.layout.dialog_set_speed_route, null);
		negativeButton = (Button)view.findViewById(R.id.negativeButton);
		positiveButton = (Button)view.findViewById(R.id.positiveButton);
        numberPicker = (NumberPicker)view.findViewById(R.id.numberPicker);
		negativeButton.setOnClickListener(this);
		positiveButton.setOnClickListener(this);

        numberPicker.setMaxValue(10);
        numberPicker.setMinValue(1);
        numberPicker.setValue(default_value);
		return view;
	}
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch(view.getId()){
		case R.id.positiveButton:
			if(getActivity() instanceof Listener){
				((Listener)getActivity()).onPositiveButtonSpeedRoute(numberPicker.getValue());
			}
            dismiss();
			break;
		case R.id.negativeButton:
			dismiss();
			break;
		default: break;
		}
	}
	@Override 
	public void onDismiss(DialogInterface dialog){
		super.onDismiss(dialog);
	}

	public void onCancel(DialogInterface dialog){
		super.onCancel(dialog);
	}
}
