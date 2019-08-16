package ch.fhnw.ip6_feedbackapp;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SettingsFragment extends Fragment {

    public static final String SHARED_PREFERENCES_NAME = "privacy_settings";
    public static final String SHARED_PREFERENCES_DEVICE_DATA = "shareDeviceData";
    public static final String SHARED_PREFERENCES_BLUETOOTH = "shareBluetoothState";
    public static final String SHARED_PREFERENCES_WIFI = "shareWifiState";
    public static final String SHARED_PREFERENCES_MOBILE_DATA = "shareMobileNetworkState";
    public static final String SHARED_PREFERENCES_GPS_LOCATION = "shareGPSLocation";

    private static final int PERMISSION_CODE_BLUETOOTH = 2;
    private static final int PERMISSION_CODE_GPS_LOCATION = 3;
    private static final int PERMISSION_CODE_MOBILE_NETWORK = 4;
    private static final int PERMISSION_CODE_WIFI = 5;

    public MainActivity mainActivity;
    private FirebaseUser user;
    private View view;
    private TextView textViewUserName;
    SwitchCompat switchDeviceData;
    SwitchCompat switchGPSPosition;
    SwitchCompat switchWifiState;
    SwitchCompat switchMobileNetworkState;
    SwitchCompat switchBluetoothState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mainActivity = (MainActivity) getActivity();
        user = mainActivity.mUser;

        view = inflater.inflate(R.layout.fragment_settings, container, false);
        switchBluetoothState = view.findViewById(R.id.switchBluetoothState);
        switchMobileNetworkState = view.findViewById(R.id.switchMobileNetworkState);
        switchWifiState = view.findViewById(R.id.switchWifiState);
        switchGPSPosition = view.findViewById(R.id.switchGPSPosition);
        switchDeviceData = view.findViewById(R.id.switchDeviceData);
        initializeButtons();

        return view;
    }

    @Override
    public void onStart() {
        updateUI();
        super.onStart();
    }

    @Override
    public void onResume() {
        loadSettings();
        super.onResume();
    }

    @Override
    public void onPause() {
        saveSettings();
        super.onPause();
    }

    private void updateUI() {
        textViewUserName = view.findViewById(R.id.textViewUsername);
        textViewUserName.setText(user.getDisplayName());

        TextView textViewEmail = view.findViewById(R.id.textViewEmailAdress);
        textViewEmail.setText(user.getEmail());

        ImageView profilePic = view.findViewById(R.id.imageViewUserProfilePic);
        // TODO: SET PROFILE PIC
    }

    private void initializeButtons(){
        // Edit username button
        ImageButton editUsername = view.findViewById(R.id.imageButtonEditUsername);
        editUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(view.getContext());
                View mView = getLayoutInflater().inflate(R.layout.username_change_dialogue, null);
                final EditText fieldUserName = mView.findViewById(R.id.editTextUserName);
                fieldUserName.setText(textViewUserName.getText());

                mBuilder.setView(mView);
                final AlertDialog userNameChangeDialog = mBuilder.create();

                Button buttonCancel = mView.findViewById(R.id.btnCancelUserNameUpdate);
                buttonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        userNameChangeDialog.cancel();
                    }
                });
                Button buttonApply = mView.findViewById(R.id.btnApplyUserNameUpdate);
                buttonApply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        userNameChangeDialog.cancel();
                        String newUserName = fieldUserName.getText().toString();
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if(newUserName != user.getDisplayName()){
                            UserProfileChangeRequest changeUsername = new UserProfileChangeRequest.Builder().setDisplayName(newUserName).build();
                            user.updateProfile(changeUsername).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        updateUI();
                                    }
                                    else{
                                        Toast t = Toast.makeText(getContext(), "Benutzername konnte nicht aktualisiert werden", Toast.LENGTH_LONG);
                                        t.show();
                                    }
                                }
                            });
                        }else{
                            Toast t = Toast.makeText(getContext(), "Benutzername ist identisch mit Vorherigem", Toast.LENGTH_LONG);
                            t.show();
                        }
                        userNameChangeDialog.cancel();
                    }
                });
                userNameChangeDialog.show();
            }

        });

        // Edit profile picture button
        ImageButton editProfilePicture = view.findViewById(R.id.imageButtonEditProfilePic);
        editProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* TODO */
            }
        });

        // GPS position switch
        switchGPSPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(switchGPSPosition.isChecked()){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (getContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                            String[] permissions = new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                            };
                            requestPermissions(permissions, PERMISSION_CODE_GPS_LOCATION);
                        }
                    }
                }
            }
        });


        // Wifi state switch
        switchWifiState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(switchWifiState.isChecked()){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (getContext().checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_DENIED || getContext().checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_DENIED) {
                            String[] permissions = new String[]{
                                    Manifest.permission.ACCESS_WIFI_STATE,
                                    Manifest.permission.ACCESS_NETWORK_STATE
                            };
                            requestPermissions(permissions, PERMISSION_CODE_WIFI);
                        }
                    }
                }
            }
        });


        // Mobile network state switch
        switchMobileNetworkState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(switchMobileNetworkState.isChecked()){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (getContext().checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
                            String[] permissions = new String[]{
                                    Manifest.permission.READ_PHONE_STATE
                            };
                            requestPermissions(permissions, PERMISSION_CODE_MOBILE_NETWORK);
                        }
                    }
                }
            }
        });


        // Bluetooth state switch
        switchBluetoothState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switchBluetoothState.isChecked()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (getContext().checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED) {
                            String[] permissions = new String[]{
                                    Manifest.permission.BLUETOOTH
                            };
                            requestPermissions(permissions, PERMISSION_CODE_BLUETOOTH);
                        }
                    }
                }
            }
        });
    }

    public void saveSettings(){
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();

        boolean shareDeviceData =  switchDeviceData.isChecked();
        Log.d("SWITCHDEVICEDATAIS", Boolean.toString(shareDeviceData));
        editor.putBoolean(SHARED_PREFERENCES_DEVICE_DATA, shareDeviceData);

        boolean shareGPSPosition = switchGPSPosition.isChecked();
        Log.d("SWITCHGPSIS", Boolean.toString(shareGPSPosition));
        editor.putBoolean(SHARED_PREFERENCES_GPS_LOCATION, shareGPSPosition);

        boolean shareWifiState = switchWifiState.isChecked();
        Log.d("SWITCHWIFIIS", Boolean.toString(shareWifiState));
        editor.putBoolean(SHARED_PREFERENCES_WIFI, shareWifiState);

        boolean shareMobileNWState = switchMobileNetworkState.isChecked();
        Log.d("SWITCHNWIS", Boolean.toString(shareMobileNWState));
        editor.putBoolean(SHARED_PREFERENCES_MOBILE_DATA, shareMobileNWState);

        boolean shareBTStatus = switchBluetoothState.isChecked();
        Log.d("SWITCHBTIS", Boolean.toString(shareBTStatus));
        editor.putBoolean(SHARED_PREFERENCES_BLUETOOTH, shareBTStatus);

        editor.apply();
    }

    private void loadSettings(){
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        Log.d("DEVICEDATAKEYEXISTS", Boolean.toString(sharedPreferences.contains(SHARED_PREFERENCES_DEVICE_DATA)));
        boolean shareDeviceData = sharedPreferences.getBoolean("SHARED_PREFERENCES_DEVICE_DATA", true);
        Log.d("DEVICEDATAKEYVALUE", Boolean.toString(shareDeviceData));
        switchDeviceData.setChecked(shareDeviceData);

        Log.d("GPSKEYVALUEEXISTS", Boolean.toString(sharedPreferences.contains(SHARED_PREFERENCES_GPS_LOCATION)));
        boolean shareGPSPosition = sharedPreferences.getBoolean("SHARED_PREFERENCES_GPS_LOCATION", true);
        Log.d("GPSKEYVALUE", Boolean.toString(shareGPSPosition));
        switchGPSPosition.setChecked(shareGPSPosition);

        Log.d("WIFIKEYVALUEEXISTS", Boolean.toString(sharedPreferences.contains(SHARED_PREFERENCES_WIFI)));
        boolean shareWifiState = sharedPreferences.getBoolean("SHARED_PREFERENCES_WIFI", true);
        Log.d("WIFIKEYVALUE", Boolean.toString(shareWifiState));
        switchWifiState.setChecked(shareWifiState);

        Log.d("MOBILEDATAKEYVALUEEXI", Boolean.toString(sharedPreferences.contains(SHARED_PREFERENCES_MOBILE_DATA)));
        boolean shareMobileNWState = sharedPreferences.getBoolean("SHARED_PREFERENCES_MOBILE_DATA", true);
        Log.d("MOBILEDATAKEYVALUE", Boolean.toString(shareMobileNWState));
        switchMobileNetworkState.setChecked(shareMobileNWState);

        Log.d("BTKEYVALUEEXISTS", Boolean.toString(sharedPreferences.contains(SHARED_PREFERENCES_BLUETOOTH)));
        boolean shareBTStatus = sharedPreferences.getBoolean("SHARED_PREFERENCES_BLUETOOTH", true);
        Log.d("BTKEYVALUE", Boolean.toString(shareBTStatus));
        switchBluetoothState.setChecked(shareBTStatus);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case PERMISSION_CODE_BLUETOOTH: {
                if(!(grantResults.length >= 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    switchBluetoothState.setChecked(false);
                    Toast.makeText(getContext(), "Zugriff verweigert", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case PERMISSION_CODE_GPS_LOCATION: {
                if(!(grantResults.length >= 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    switchGPSPosition.setChecked(false);
                    Toast.makeText(getContext(), "Zugriff verweigert", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case PERMISSION_CODE_MOBILE_NETWORK: {
                if(!(grantResults.length >= 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    switchMobileNetworkState.setChecked(false);
                    Toast.makeText(getContext(), "Zugriff verweigert", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case PERMISSION_CODE_WIFI: {
                if(!(grantResults.length >= 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    switchWifiState.setChecked(false);
                    Toast.makeText(getContext(), "Zugriff verweigert", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

}

