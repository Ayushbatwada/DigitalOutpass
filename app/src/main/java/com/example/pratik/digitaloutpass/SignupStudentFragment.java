package com.example.pratik.digitaloutpass;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
//import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SignupStudentFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SignupStudentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignupStudentFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private EditText etEmail;
    private EditText etName;
    private EditText etPhone;
    private EditText etEnroll;
    private EditText etPassword;
    private Button bSignup;
    private FirebaseAuth mAuth;
    private TextView tvGoToLogin;
    DatabaseReference userDatabase;
    private Spinner spBranch;
    private Spinner spBatch;
    private String branch;
    private String batch;
    private List<String> years;
    private List<String> branches;

    private OnFragmentInteractionListener mListener;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference users;

    public SignupStudentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SignupStudentFragment.
     */

    public static SignupStudentFragment newInstance() {
        SignupStudentFragment fragment = new SignupStudentFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        userDatabase = FirebaseDatabase.getInstance().getReference("users");
        //users = database.getReference('')


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_signup_student, container, false);
        etEmail = v.findViewById(R.id.etEmailSignupStudent);
        etPassword = v.findViewById(R.id.etPasswordSignupStudent);
        etEnroll = v.findViewById(R.id.etEnrollStudent);
        spBatch = v.findViewById(R.id.spBatchStudent);
        spBranch = v.findViewById(R.id.spBranchStudent);
        etPhone = v.findViewById(R.id.etPhoneStudent);
        etName = v.findViewById(R.id.etNameStudent);
        tvGoToLogin = v.findViewById(R.id.tvGoToLogin);
        bSignup = v.findViewById(R.id.bSignupStudent);
        bSignup.setOnClickListener(this);
        tvGoToLogin.setOnClickListener(this);
        spBranch.setOnItemSelectedListener(this);
        spBatch.setOnItemSelectedListener(this);

        Date today = new Date(); // Fri Jun 17 14:54:28 PDT 2016
        Calendar cal = Calendar.getInstance();
        cal.setTime(today); // don't forget this if date is arbitrary e.g. 01-01-2014

        int year = cal.get(Calendar.YEAR);

        if(cal.get(Calendar.MONTH) < 7){
            year--;
        }

        years = new ArrayList<String>();
        for(int i = 0; i<4; i++){
            years.add(Integer.toString(year--));
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, years);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spBatch.setAdapter(dataAdapter);

        branches = new ArrayList<String>();
        branches.add("CSE");
        branches.add("ECE");

        ArrayAdapter<String> dataAdapter1 = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, branches);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spBranch.setAdapter(dataAdapter1);
        return  v;
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bSignupStudent:
                clickOnSignup();
                break;
            case R.id.tvGoToLogin:
                goToLogin();
                break;
        }

    }

    private void goToLogin() {
        mListener.switchFragment();
    }

    private void clickOnSignup() {
        final String email = etEmail.getText().toString().trim();
        final String password = etPassword.getText().toString();
        final String name = etName.getText().toString();
        final int enroll = Integer.parseInt(etEnroll.getText().toString());
        final long phone = Long.parseLong(etPhone.getText().toString());



        //Toast.makeText(getContext(), "Email: "+email, Toast.LENGTH_SHORT).show();

        //Toast.makeText(getContext(), email.length()+"", Toast.LENGTH_SHORT).show();
        if(email==null || email.length()==0){
            etEmail.setError("Enter an email");
            etEmail.requestFocus();
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
        }
        else if(password==""){
            etPassword.setError("Please enter a password");
            etPassword.requestFocus();
        }
        else if (password.length()<6){
            etPassword.setError("Password length should be greater than 6");
            etPassword.requestFocus();
        }
        else {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getContext(), "User created successfully", Toast.LENGTH_SHORT).show();
                        String newUserKey = userDatabase.push().getKey();
                        User newUser = new User(newUserKey, email.substring(0, email.indexOf('@')), User.STUDENT, email, phone);
                        newUser = new Student(newUserKey, name, User.STUDENT, email, phone, enroll, batch, branch);
                        userDatabase.child(newUserKey).setValue(newUser);
                        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(getContext(), "Login successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getContext(), MainActivity.class));
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item

        if(parent.getId() == R.id.spBatchStudent)
        {
            //do this
            batch = parent.getItemAtPosition(position).toString();
        }
        else if(parent.getId() == R.id.spBranchStudent)
        {
            //do this
            branch = parent.getItemAtPosition(position).toString();
        }




        // Showing selected spinner item

    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
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

        void switchFragment();
    }
}
