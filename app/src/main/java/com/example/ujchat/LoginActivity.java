package com.example.ujchat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ujchat.Utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText mPhoneNumber, mCode;
    private Button mSend; 
    
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    String userVerificationId, userMobileNumber;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);

        userIsLoggedIn();

        mPhoneNumber = findViewById(R.id.phoneNumber);
//        mCode = findViewById(R.id.code);

        mSend = findViewById(R.id.send);

        mAuth = FirebaseAuth.getInstance();

        Intent i = new Intent(LoginActivity.this, VerificationActivity.class);
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userMobileNumber = mPhoneNumber.getText().toString().trim();
                if(userVerificationId != null){
//                    i.putExtra(Constants.VERIFICATION_ID, userVerificationId);
//                    i.putExtra(Constants.USER_MOBILE_KEY, userMobileNumber);
//                    startActivity(i);
//                    verifyPhoneNumberWithCode();
               }else
                    startPhoneNumberVerification(userMobileNumber);
            }
        });


        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
            }


            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verificationId, forceResendingToken);

                userVerificationId = verificationId;
                i.putExtra(Constants.VERIFICATION_ID, userVerificationId);
                Log.e("log",userVerificationId);
                i.putExtra(Constants.USER_MOBILE_KEY, userMobileNumber);
                Log.e("log",userMobileNumber);

                startActivity(i);
//                mSend.setText("Verify Code");
            }
        };

    }

//    private void verifyPhoneNumberWithCode(){
//        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, mCode.getText().toString());
//        signInWithPhoneAuthCredential(credential);
//    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if(user != null){
                        final DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user").child(user.getUid());
                        mUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(!dataSnapshot.exists()){
                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("phone", user.getPhoneNumber());
                                    userMap.put("name", user.getPhoneNumber());
                                    mUserDB.updateChildren(userMap);
                                }
                                userIsLoggedIn();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                }

            }
        });
    }

    private void userIsLoggedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            startActivity(new Intent(getApplicationContext(), MainPageActivity.class));
            finish();
            return;
        }
    }

    private void startPhoneNumberVerification(String userMobileNumber) {
//        PhoneAuthProvider.getInstance().verifyPhoneNumber(
//                mPhoneNumber.getText().toString(),
//                60,
//                TimeUnit.SECONDS,
//                this,
//                mCallbacks);


        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+963" + userMobileNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(LoginActivity.this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }
}
