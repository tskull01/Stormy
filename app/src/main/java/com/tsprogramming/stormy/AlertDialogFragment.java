package com.tsprogramming.stormy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by Tskulley on 2/22/2018.
 */

public class AlertDialogFragment extends DialogFragment {


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
       Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Sorry!").setMessage("There was an error try again.")
                .setPositiveButton("OK",null);
    AlertDialog dialog = builder.create();
    return dialog;
    }
}
