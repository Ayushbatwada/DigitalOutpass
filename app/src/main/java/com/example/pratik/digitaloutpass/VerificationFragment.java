package com.example.pratik.digitaloutpass;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VerificationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VerificationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VerificationFragment extends Fragment {

    FirebaseAuth mAuth;
    private TextView tvEmailVer;
    DatabaseReference userDatabase;
    private Button btnSign;
    private TextView tvAlVer;


    private OnFragmentInteractionListener mListener;

    public VerificationFragment() {
        // Required empty public constructor
    }


    public static VerificationFragment newInstance() {
        VerificationFragment fragment = new VerificationFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        userDatabase = FirebaseDatabase.getInstance().getReference("users");






    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_verification, container, false);

        tvEmailVer = v.findViewById(R.id.tvEmailVer);
        btnSign = v.findViewById(R.id.btnsignOutVer);
        tvAlVer = v.findViewById(R.id.tvAlVer);

        tvAlVer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = mAuth.getCurrentUser();
                if(user.isEmailVerified()){
                    startActivity(new Intent(getContext(),MainActivity.class));
                }

            }
        });

        btnSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                LoginStudentFragment loginStudentFragment = LoginStudentFragment.newInstance();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.SSConstraintLayout, loginStudentFragment).commit();

            }
        });
        // Inflate the layout for this fragment
        final FirebaseUser user = mAuth.getCurrentUser();

        if(user != null){
            Toast.makeText(getContext(), user.isEmailVerified()+"", Toast.LENGTH_SHORT).show();
            if(user.isEmailVerified()){
                Toast.makeText(getContext(),"Verified",Toast.LENGTH_SHORT).show();
                tvEmailVer.setText("Email Verified");
                startActivity(new Intent(getContext(), MainActivity.class));
            }
            else{
//                tvEmailVer.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//
//                                if(task.isSuccessful()) {
//                                    Toast.makeText(getContext(), "Email Sent", Toast.LENGTH_SHORT).show();
//                                    tvEmailVer.setText("Email Sent Please verify and login again");
//                                }
//                            }
//                        });
//
//                    }
//                });
                    user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getContext(), "Verification email sent",Toast.LENGTH_SHORT).show();
                                tvEmailVer.setText("Email Sent Please verify and login again");
                            }
                        }
                    });

//                    new Timer().scheduleAtFixedRate(new TimerTask() {
//                        @Override
//                        public void run() {
//                            if(user.isEmailVerified()){
//                                startActivity(new Intent(getContext(), MainActivity.class));
//                            }
//                        }
//                    },0,3000);
            }
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!user.isEmailVerified())
                        handler.postDelayed(this, 1000);
                    else {
                        // do actions
                        startActivity(new Intent(getContext(),MainActivity.class));
                    }
                }
            }, 1000);
        }



//                final Handler handler = new Handler();
//                final Runnable runnable = new Runnable() {
//                    @Override
//                    public void run() {
//                        //Toast.makeText(getContext(), "aknf",Toast.LENGTH_SHORT).show();
//                        if(user.isEmailVerified()){
//                            Toast.makeText(getContext(), "aknf",Toast.LENGTH_SHORT).show();
//                            startActivity(new Intent(getContext(), MainActivity.class));
//                        }
//                        handler.postDelayed(this,500);
//
//
//
//
//                    }
//                };
//
//                runnable.run();




//            while(!(user.isEmailVerified())){
//
//            }
//            startActivity(new Intent(getContext(), MainActivity.class));









        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
