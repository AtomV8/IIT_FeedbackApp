package ch.fhnw.ip6_feedbackapp;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

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

import org.w3c.dom.Text;

public class SettingsFragment extends Fragment {

    public MainActivity mainActivity;
    private FirebaseUser user;
    private View view;
    private TextView textViewUserName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mainActivity = (MainActivity) getActivity();
        user = mainActivity.mUser;

        view = inflater.inflate(R.layout.fragment_settings, container, false);
        updateUI();
        initializeButtons();
        return view;
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
    }

}

