package com.example.websocketapplication;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class LoginDialogFragment extends DialogFragment {

    DialogCallback callback;
    String username;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.login_dialog, container, false);
        final EditText usernameField = rootView.findViewById(R.id.username);
        Button dismiss = rootView.findViewById(R.id.login);

        dismiss.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                username = usernameField.getText().toString();
                dismiss();
            }
        });

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = (DialogCallback) getActivity();
        assert callback != null;
        callback.returnData(username);
    }
}
