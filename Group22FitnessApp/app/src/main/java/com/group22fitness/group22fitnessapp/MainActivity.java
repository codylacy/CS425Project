package com.group22fitness.group22fitnessapp;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth auth;
    private TextView mEmailField;
    private TextView mPasswordField;
    private boolean isSignedIn;
    private String oldPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        mEmailField = (TextView) findViewById(R.id.EmailField);
        mPasswordField = (TextView) findViewById(R.id.PasswordField);

        findViewById(R.id.SignIn).setOnClickListener(this);
        findViewById(R.id.SignOut).setOnClickListener(this);
        findViewById(R.id.CreateAccount).setOnClickListener(this);
        findViewById(R.id.ChangePassword).setOnClickListener(this);
        isSignedIn = false;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = auth.getCurrentUser();
    }

    private void createAccount( String email, String password){
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("i", "createUserWithEmail:success");
                            FirebaseUser user = auth.getCurrentUser();
                            Toast.makeText(MainActivity.this, "Account Created",Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("i", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signIn(String email, String password) {
        Log.d("i", "signIn:" + email);
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("i", "signInWithEmail:success");
                            FirebaseUser user = auth.getCurrentUser();
                            Toast.makeText(MainActivity.this, "Sign In Successful.", Toast.LENGTH_SHORT).show();
                            isSignedIn = true;
                            oldPassword = mPasswordField.getText().toString();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("i", "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signOut() {
        auth.signOut();
        Toast.makeText(MainActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
        isSignedIn = false;
    }


    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (password.length() < 8 ) {
            mPasswordField.setError("Need to be at least 8 charactors.");
            Toast.makeText(MainActivity.this, "Password needs to be over 6 characters", Toast.LENGTH_SHORT).show();
            valid = false;
        } else {
            mPasswordField.setError(null);
        }
        return valid;
    }

    public void deleteAccount( String email, String password){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("i", "User account deleted.");
                            Toast.makeText(MainActivity.this, "Account Deleted", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(MainActivity.this, "Error in account deleted", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        return;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.CreateAccount) {
            //Toast.makeText(MainActivity.this, "create account pressed", Toast.LENGTH_SHORT).show();
            if(validateForm()) createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (i == R.id.SignIn) {
            //Toast.makeText(MainActivity.this, "sign in pressed", Toast.LENGTH_SHORT).show();
            if(validateForm()) signIn (mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (i == R.id.SignOut) {
            //Toast.makeText(MainActivity.this, "sign out pressed", Toast.LENGTH_SHORT).show();
            if(isSignedIn) signOut();
        }
        else if (i == R.id.ChangePassword) {
            //Toast.makeText(MainActivity.this, "delete account pressed", Toast.LENGTH_SHORT).show();
            if(validateForm() && isSignedIn ) changePassword( mEmailField.getText().toString(), mPasswordField.getText().toString() );
        }

    }

    public void changePassword(String email, final String password){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(email,oldPassword);
        if( validateForm() ) {
            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        user.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Password change denied", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Password change accepted", Toast.LENGTH_SHORT).show();
                                    oldPassword = mPasswordField.getText().toString();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(MainActivity.this, "Error in email name", Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }
    }
}
