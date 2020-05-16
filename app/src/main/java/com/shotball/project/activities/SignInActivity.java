package com.shotball.project.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shotball.project.R;
import com.shotball.project.utils.TextUtil;
import com.shotball.project.models.User;

import java.util.Objects;

public class SignInActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = SignInActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 9001;

    private ScrollView mainContainer;

    private TextInputLayout mTextInputLayoutEmail;
    private TextInputLayout mTextInputLayoutPassword;

    private EditText mEmailField;
    private EditText mPasswordField;

    private AlertDialog resetPasswordDialog;

    public FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        initComponents();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void initComponents() {
        mainContainer = findViewById(R.id.sing_in_activity);

        mTextInputLayoutEmail = findViewById(R.id.text_input_layout_email);
        mTextInputLayoutPassword = findViewById(R.id.text_input_layout_password);

        mEmailField = findViewById(R.id.field_email);
        mPasswordField = findViewById(R.id.field_password);

        findViewById(R.id.signin_button).setOnClickListener(this);
        findViewById(R.id.forgot_password).setOnClickListener(this);
        findViewById(R.id.create_account_button).setOnClickListener(this);
        findViewById(R.id.google_signin_button).setOnClickListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Reset password")
                .setMessage("Enter your email address")
                .setView(R.layout.dialog_password_restore)
                .setNeutralButton(R.string.cancel, null)
                .setPositiveButton("Send", null);
        resetPasswordDialog = builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                updateUI(null);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful() && task.getResult().getUser() != null) {
                            Log.d(TAG, "signInWithCredential:success");
                            onAuthSuccess(task.getResult().getUser());
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(mainContainer, R.string.auth_failed, Snackbar.LENGTH_LONG).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            try {
                                throw Objects.requireNonNull(task.getException());
                            } catch (FirebaseAuthWeakPasswordException e) {
                                mTextInputLayoutPassword.setError(getString(R.string.error_weak_password));
                                mTextInputLayoutPassword.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                mTextInputLayoutEmail.setError(getString(R.string.error_invalid_email_or_password));
                                mTextInputLayoutEmail.requestFocus();
                            } catch (FirebaseAuthInvalidUserException e) {
                                mTextInputLayoutEmail.setError(getString(R.string.accout_not_found));
                                mTextInputLayoutEmail.requestFocus();
                            } catch(Exception e) {
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            }

                            Snackbar.make(mainContainer, R.string.auth_failed, Snackbar.LENGTH_LONG).show();
                            updateUI(null);
                        }

                        hideProgressDialog();
                    }
                });
    }

    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void onAuthSuccess(final FirebaseUser user) {
        final String username = user.getDisplayName();

        DatabaseReference userNameRef = mDatabase.child("users").child(user.getUid());
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    writeNewUser(user.getUid(), username, user.getEmail());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        };
        userNameRef.addListenerForSingleValueEvent(eventListener);

        startActivity(new Intent(SignInActivity.this, MainActivity.class));
        finish();
    }

    private void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email);

        mDatabase.child("users").child(userId).setValue(user);
    }

    private boolean validateForm() {
        boolean valid = true;

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mTextInputLayoutPassword.setError(getString(R.string.required));
            mTextInputLayoutPassword.requestFocus();
            valid = false;
        } else {
            mTextInputLayoutPassword.setError(null);
        }

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

        return valid;
    }

    private void resetPassword(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Snackbar.make(mainContainer, R.string.reset_password_email_send, Snackbar.LENGTH_LONG).show();
                            Log.d(TAG, "Email sent.");
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();

        if (user != null) {
            if (!user.isEmailVerified()) {
                mTextInputLayoutEmail.setError(getString(R.string.email_not_verify));
                mTextInputLayoutEmail.requestFocus();
            } else {
                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        hideKeyboard(v);

        if (i == R.id.forgot_password) {
            resetPasswordDialog.show();
            resetPasswordDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextInputLayout textInputLayout = resetPasswordDialog.findViewById(R.id.text_input_layout);
                    TextView input = resetPasswordDialog.findViewById(android.R.id.text1);
                    if (textInputLayout == null || input == null) return;
                    String email = input.getText().toString();

                    if (TextUtils.isEmpty(email)) {
                        textInputLayout.setError(getString(R.string.required));
                        textInputLayout.requestFocus();
                    } else if (!TextUtil.validateEmail(email)) {
                        textInputLayout.setError(getString(R.string.error_invalid_email));
                        textInputLayout.requestFocus();
                    } else {
                        textInputLayout.setError(null);
                        resetPassword(input.getText().toString());
                        input.setText("");
                        resetPasswordDialog.dismiss();
                    }
                }
            });
        } else if (i == R.id.create_account_button) {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
        } else if (i == R.id.signin_button) {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (i == R.id.google_signin_button) {
            signInGoogle();
        }
    }
}
