package com.shotball.project.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shotball.project.R;
import com.shotball.project.Utils.TextUtil;
import com.shotball.project.models.User;

import java.util.Objects;

public class SignUpActivity extends BaseActivity {

    private static final String TAG = SignUpActivity.class.getSimpleName();

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private TextInputLayout mTextInputLayoutEmail;
    private TextInputLayout mTextInputLayoutPassword;
    private TextInputLayout mTextInputLayoutRePassword;

    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mRePasswordField;
    private EditText mUsernameField;

    private Dialog verifyDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        initToolbar();
        initComponents();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.sign_up);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponents() {
        mTextInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
        mTextInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        mTextInputLayoutRePassword = findViewById(R.id.textInputLayoutRePassword);

        mEmailField = findViewById(R.id.fieldEmail);
        mPasswordField = findViewById(R.id.fieldPassword);
        mRePasswordField = findViewById(R.id.fieldRePassword);
        mUsernameField = findViewById(R.id.fieldUsername);

        FloatingActionButton myFab = findViewById(R.id.signUpButton);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!validateForm()) {
                    return;
                }

                createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    public void onClick(View v) {
        if (!validateForm()) {
            return;
        }

        createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        showProgressDialog();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");

                            FirebaseUser user = mAuth.getCurrentUser();

                            writeNewUser(user.getUid(), mUsernameField.getText().toString(), mEmailField.getText().toString());
                            sendEmailVerification(user);
                        } else {
                            try {
                                throw Objects.requireNonNull(task.getException());
                            } catch (FirebaseAuthWeakPasswordException e) {
                                mTextInputLayoutPassword.setError(getString(R.string.error_weak_password));
                                mTextInputLayoutPassword.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                mTextInputLayoutEmail.setError(getString(R.string.error_invalid_email));
                                mTextInputLayoutEmail.requestFocus();
                            } catch (FirebaseAuthUserCollisionException e) {
                                mTextInputLayoutEmail.setError(getString(R.string.error_user_exists));
                                mTextInputLayoutEmail.requestFocus();
                            } catch (Exception e) {
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            }

                            updateUI(null);
                        }

                        hideProgressDialog();
                    }
                });
    }

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        // Re-enable button

                        if (task.isSuccessful()) {
                            mAuth.signOut();
                            showVerifyDialog();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(SignUpActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // [END_EXCLUDE]
                    }
                });
        // [END send_email_verification]
    }

    private void showVerifyDialog() {
        verifyDialog = new Dialog(this);
        verifyDialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        verifyDialog.setContentView(R.layout.dialog_verify);
        verifyDialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(verifyDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;


        (verifyDialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        verifyDialog.show();
        verifyDialog.getWindow().setAttributes(lp);
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mTextInputLayoutEmail.setError(getString(R.string.required));
            mTextInputLayoutEmail.requestFocus();
            valid = false;
        } else if (!TextUtil.validateEmail(email)) {
            mTextInputLayoutEmail.setError(getString(R.string.error_invalid_email));
            mTextInputLayoutEmail.requestFocus();
            valid = false;
        } else {
            mTextInputLayoutEmail.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mTextInputLayoutPassword.setError(getString(R.string.required));
            mTextInputLayoutPassword.requestFocus();
            valid = false;
        } else {
            mTextInputLayoutPassword.setError(null);
        }

        String rePassword = mRePasswordField.getText().toString();
        if (TextUtils.isEmpty(rePassword)) {
            mTextInputLayoutRePassword.setError(getString(R.string.required));
            mTextInputLayoutRePassword.requestFocus();
            valid = false;
        } else if (!rePassword.equals(password)) {
            mTextInputLayoutRePassword.setError(getString(R.string.error_password_mismatch));
            mTextInputLayoutRePassword.requestFocus();
            valid = false;
        } else {
            mTextInputLayoutRePassword.setError(null);
        }

        return valid;
    }

    private void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email, "");

        mDatabase.child("users").child(userId).setValue(user);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().signOut();
        if (verifyDialog != null) {
            verifyDialog.dismiss();
        }
    }
}
