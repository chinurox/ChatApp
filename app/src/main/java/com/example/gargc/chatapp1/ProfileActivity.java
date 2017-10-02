package com.example.gargc.chatapp1;

import android.app.ProgressDialog;
import android.icu.text.DateFormat;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    TextView mProfileName,mProfileStatus,mProfileFriendsCount;
    Button mProfileequestButton,mProfileDeclineButton;


    ImageView mProfileImageView;

    DatabaseReference mUsersDatabase;
    DatabaseReference mFriendRequestDatabase;
    DatabaseReference mFriendDatabase;
    DatabaseReference mNotificationDatabase;

    DatabaseReference mRootRef;
    DatabaseReference mUserRef;

    FirebaseAuth mAuth;


    FirebaseUser mCurrentUser;

    ProgressDialog mProgressDialog;

    String mCurrent_state;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        final String user_id=getIntent().getStringExtra("user_id");

        mRootRef =FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();


        mUsersDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

        mUserRef= FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        mFriendRequestDatabase=FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase=FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase=FirebaseDatabase.getInstance().getReference().child("notifications");
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();

        mProfileImageView=(ImageView) findViewById(R.id.profile_image);
        mProfileName=(TextView) findViewById(R.id.profile_display_name);
        mProfileStatus=(TextView) findViewById(R.id.profile_status);
        mProfileFriendsCount=(TextView) findViewById(R.id.profile_total_friends);
        mProfileequestButton=(Button) findViewById(R.id.profile_send_request_button);
        mProfileDeclineButton=(Button) findViewById(R.id.PROFILE_DECLINE_REQUEST);

        mCurrent_state="not_friends";

        mProfileDeclineButton.setVisibility(View.INVISIBLE);
        mProfileDeclineButton.setEnabled(false);


        mProgressDialog=new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please Wait we load the user data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();



        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name=dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String image=dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.defaultavatar).into(mProfileImageView);

                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(user_id))
                        {
                            String req_type=dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if(req_type.equals("received")){
                                mCurrent_state="req_received";
                                mProfileequestButton.setText("Accept Friend Request");

                                mProfileDeclineButton.setVisibility(View.VISIBLE);
                                mProfileDeclineButton.setEnabled(true);

                            }
                            else  if(req_type.equals("sent"))
                            {
                                mCurrent_state="req_sent";
                                mProfileequestButton.setText("Cancel Friend Request");

                                mProfileDeclineButton.setVisibility(View.INVISIBLE);
                                mProfileDeclineButton.setEnabled(false);
                            }
                            mProgressDialog.dismiss();

                        }
                        else
                        {
                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id))
                                    {
                                        mCurrent_state="friends";
                                        mProfileequestButton.setText("UNFRIEND");

                                        mProfileDeclineButton.setVisibility(View.INVISIBLE);
                                        mProfileDeclineButton.setEnabled(false);
                                    }
                                    mProgressDialog.dismiss();


                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    mProgressDialog.dismiss();

                                }
                            });
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileequestButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {

                mProfileequestButton.setEnabled(false);

                if(mCurrent_state.equals("not_friends"))
                {

                    DatabaseReference newNotificationref=mRootRef.child("notifications").child(user_id).push();
                    String newNotificationId=newNotificationref.getKey();

                    HashMap<String,String> notificationData=new HashMap<String, String>();
                    notificationData.put("from",mCurrentUser.getUid());
                    notificationData.put("type","request");

                    Map requestMap=new HashMap<>();
                    requestMap.put("Friend_req/"+mCurrentUser.getUid()+"/"+user_id+"/request_type","sent");
                    requestMap.put("Friend_req/"+user_id+"/"+mCurrentUser.getUid()+"/request_type","received");
                    requestMap.put("notifications/"+user_id+"/"+newNotificationId,notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError!=null)
                            {
                                Toast.makeText(ProfileActivity.this, "There Was Error in sending request", Toast.LENGTH_SHORT).show();
                            }
                            mProfileequestButton.setEnabled(true);

                            mCurrent_state="req_sent";
                            mProfileequestButton.setText("Cancel FRIEND REQUEST");
                        }
                    });


                }

                if(mCurrent_state.equals("req_sent"))
                {
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileequestButton.setEnabled(true);
                                    mCurrent_state="not_friends";
                                    mProfileequestButton.setText("SEND FRIEND REQUEST");

                                    mProfileDeclineButton.setVisibility(View.INVISIBLE);
                                    mProfileDeclineButton.setEnabled(false);

                                }
                            });
                        }
                    });

                }

                if(mCurrent_state.equals("req_received"))
                {
                    final String currentDate= DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap=new HashMap();
                    friendsMap.put("Friends/"+mCurrentUser.getUid()+"/"+user_id+"/date",currentDate);
                    friendsMap.put("Friends/"+user_id+"/"+mCurrentUser.getUid()+"/date",currentDate);

                    friendsMap.put("Friend_req/"+mCurrentUser.getUid()+"/"+user_id,null);
                    friendsMap.put("Friend_req/"+user_id+"/"+mCurrentUser.getUid(),null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError==null)
                            {
                                mProfileequestButton.setEnabled(true);
                                mCurrent_state="friends";
                                mProfileequestButton.setText("UNFRIEND");

                                mProfileDeclineButton.setVisibility(View.INVISIBLE);
                                mProfileDeclineButton.setEnabled(false);
                            }
                            else
                            {
                                String error=databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    //




                        }



                if(mCurrent_state.equals("friends"))
                {
                    Map unfriendMap=new HashMap();
                    unfriendMap.put("Friends/"+mCurrentUser.getUid()+"/"+user_id,null);
                    unfriendMap.put("Friends/"+user_id+"/"+mCurrentUser.getUid(),null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError==null)
                            {

                                mCurrent_state="not_friends";
                                mProfileequestButton.setText("Send Friend Request");

                                mProfileDeclineButton.setVisibility(View.INVISIBLE);
                                mProfileDeclineButton.setEnabled(false);
                            }
                            else
                            {
                                String error=databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                            mProfileequestButton.setEnabled(true);
                        }
                    });

                  }
            }
        });




    }
    @Override
    protected void onStart() {
        super.onStart();
        mUserRef.child("online").setValue(true);

    }

}
