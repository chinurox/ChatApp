package com.example.gargc.chatapp1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    TextInputLayout mDisplayName,mEmail,mPassword;
    Button mCreateButton;

    Toolbar mToolbar;

    DatabaseReference mDatabase;

    FirebaseAuth mAuth;

    ProgressDialog  mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mToolbar=(Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressDialog=new ProgressDialog(this);

        mAuth=FirebaseAuth.getInstance();

        mDisplayName=(TextInputLayout) findViewById(R.id.login_email);
        mEmail=(TextInputLayout) findViewById(R.id.login_password);
        mPassword=(TextInputLayout) findViewById(R.id.reg_password);
        mCreateButton=(Button) findViewById(R.id.reg_create_btn);

        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String displayname=mDisplayName.getEditText().getText().toString();
                String email=mEmail.getEditText().getText().toString();
                String password=mPassword.getEditText().getText().toString();
                Log.i("name1",displayname);
                if(!TextUtils.isEmpty(displayname)&&!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(password))
                {
                    mProgressDialog.setTitle("Registering User");
                    mProgressDialog.setMessage("Please Wait .....");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();
                    register_user(displayname,email,password);
                }


            }
        });
    }

    private void register_user(final String displayname, String email, String password) {
        Log.i("name",displayname);
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    FirebaseUser current_user=FirebaseAuth.getInstance().getCurrentUser();
                    String uid=current_user.getUid();
                    mDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    HashMap<String,String> userMap=new HashMap<String, String>();
                    userMap.put("name",displayname);
                    userMap.put("status","Hi !!! there I am using chat App");
                    userMap.put("image","default");
                    userMap.put("thumb_image","default");
                    userMap.put("device_token", FirebaseInstanceId.getInstance().getToken());
                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Log.i("name2",displayname);
                                mProgressDialog.dismiss();

                                Intent mainIntent=new Intent(RegisterActivity.this,MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                            }
                        }
                    });






                }
                else
                {
                    mProgressDialog.hide();
                    Toast.makeText(RegisterActivity.this, "Cannot Create Account", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}
