package com.example.pratik.digitaloutpass;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.server.converter.StringToIntConverter;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;


public class OutpassRequestFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    TextView tvNoOutpasses;
    RecyclerView requestList;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser curUser;
    DatabaseReference usersRef;
    DatabaseReference hostelsRef;
    DatabaseReference outpassesRef;
    DatabaseReference cref;
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    Boolean cRight;

    String hostelOfCaretaker;

//    ArrayList<Outpass> outpassRequests = new ArrayList<>();
    ArrayList<String> outpassIds = new ArrayList<String>();
    HashMap<String, Outpass> outpassRequestsMap = new HashMap<>();
    HashMap<String, String> personNames = new HashMap<>();
    OutpassRequestsAdapter requestsAdapter;
    public OutpassRequestFragment() {

    }

    public static OutpassRequestFragment newInstance() {
        OutpassRequestFragment fragment = new OutpassRequestFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestsAdapter = new OutpassRequestsAdapter(getContext(), outpassRequestsMap, outpassIds);
        curUser = mAuth.getCurrentUser();
        usersRef = database.child("users");
        hostelsRef = database.child("hostels");
        outpassesRef = database.child("outpasses");
        cref = database.child("careRight");


        usersRef.child(curUser.getUid()).child("hostel").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                hostelOfCaretaker = dataSnapshot.getValue(String.class);
                hostelsRef.child(hostelOfCaretaker).child("members").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        final DataSnapshot childId = dataSnapshot;

                        usersRef.child(dataSnapshot.getValue(String.class).toString()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue(String.class)!=null && !dataSnapshot.getValue(String.class).toString().equals("")) {
                                    final String studentName = dataSnapshot.getValue(String.class).toString();
                                    usersRef.child(childId.getValue(String.class).toString()).child("myOutpasses").addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                            outpassesRef.child(dataSnapshot.getValue(String.class).toString()).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    Outpass curOutpass = (Outpass) dataSnapshot.getValue(Outpass.class);
                                                    if (!curOutpass.isVerified()) {
//                                                    curOutpass.setPersonName(studentName);
                                                        personNames.put(dataSnapshot.getKey(), studentName);
//                                                    outpassRequests.add(curOutpass);
                                                        outpassRequestsMap.put(dataSnapshot.getKey(), curOutpass);
                                                        outpassIds.add(dataSnapshot.getKey());
                                                        requestsAdapter.notifyDataSetChanged();
                                                    } else if (outpassRequestsMap.get(dataSnapshot.getKey()) != null) {
                                                        personNames.remove(dataSnapshot.getKey());
                                                        outpassRequestsMap.remove(dataSnapshot.getKey());
                                                        outpassIds.remove(dataSnapshot.getKey());
                                                        requestsAdapter.notifyDataSetChanged();
                                                    }
                                                    if(requestList!=null && tvNoOutpasses!=null) {
                                                        if (outpassIds.isEmpty()) {
                                                            tvNoOutpasses.setVisibility(View.VISIBLE);
                                                            requestList.setVisibility(View.GONE);
                                                        } else {
                                                            tvNoOutpasses.setVisibility(View.GONE);
                                                            requestList.setVisibility(View.VISIBLE);
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                        }

                                        @Override
                                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                        }

                                        @Override
                                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                                        }

                                        @Override
                                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_outpass_request, container, false);
        tvNoOutpasses = view.findViewById(R.id.tvNoOutpasses);

        requestList= view.findViewById(R.id.requestList);
        requestList.setHasFixedSize(true);

//        ArrayList<Outpass> requests = new ArrayList<>();
//        requests.add(new Outpass("Pratik Gupta","Champa","IIIT-NR", new Date(),new Date(),"011"));
//        requests.add(new Outpass("Himanshu Khairajani","Raipur","IIIT-NR", new Date(),new Date(),"1234"));
//        requests.add(new Outpass("konduri lakshmi pati verma","Vizak","IIIT-NR", new Date(),new Date(),"23523"));


        requestList.setLayoutManager(new LinearLayoutManager(getActivity()));

        requestList.setAdapter(requestsAdapter);

        return view;


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
        void onFragmentInteraction(Uri uri);
    }

    public class OutpassRequestsAdapter extends RecyclerView.Adapter<OutpassRequestsAdapter.RequestViewHolder> {

        private HashMap<String, Outpass> requests ;
        private ArrayList<String> outpassIds;
        Context ctx;
        public OutpassRequestsAdapter(Context ctx, HashMap<String, Outpass> requests, ArrayList<String> outpasseIds) {
            this.requests= requests ;
            this.ctx = ctx;
            this.outpassIds = outpasseIds;
        }

        @NonNull
        @Override
        public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater= LayoutInflater.from(viewGroup.getContext());
            View view = inflater.inflate(R.layout.request_items, viewGroup,false);
            return  new RequestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RequestViewHolder requestViewHolder, int i) {

            final Outpass currentItem = requests.get(outpassIds.get(i));
            requestViewHolder.name.setText(personNames.get(outpassIds.get(i)));
            requestViewHolder.id.setText(currentItem.getId()+"");
            requestViewHolder.from.setText(currentItem.getFrom());
            requestViewHolder.to.setText(currentItem.getTo());
            usersRef.child(currentItem.getPersonName()).child("imageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String url = dataSnapshot.getValue(String.class);
                    Glide.with(getActivity())
                            .load(url)
                            .into(requestViewHolder.image);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    //        RequestViewHolder.ReturnDate.setText(currentItem.getReturnDate().toString());
    //        RequestViewHolder.LeavingDate.setText(currentItem.getLeaveDate().toString());
            Calendar leaveCal = Calendar.getInstance();
            leaveCal.setTime(currentItem.getLeaveDate());
            Calendar returnCal = Calendar.getInstance();
            returnCal.setTime(currentItem.getReturnDate());
            requestViewHolder.leavingDate.setText(leaveCal.get(Calendar.DAY_OF_MONTH)+ "/" + (leaveCal.get(Calendar.MONTH))+"/" + leaveCal.get(Calendar.YEAR));
            requestViewHolder.returnDate.setText(returnCal.get(Calendar.DAY_OF_MONTH)+ "/" + (returnCal.get(Calendar.MONTH))+"/" + returnCal.get(Calendar.YEAR));
            requestViewHolder.bVerify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(getContext(), "cliked", Toast.LENGTH_SHORT).show();
                    usersRef.child(curUser.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String role = dataSnapshot.child("role").getValue(String.class);
                            if(role.equals("CARETAKER")){
                                String hostel = dataSnapshot.child("hostel").getValue(String.class);
                                cref.child(hostel).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        cRight = dataSnapshot.getValue(Boolean.class);
                                        if(!cRight){
                                            Toast.makeText(getContext(),"You are not authorised",Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            currentItem.setVerified(true);
                                            outpassesRef.child("" + currentItem.getId()).child("verified").setValue(true);
                                            usersRef.child(currentItem.getPersonName()).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    String studentToken = dataSnapshot.getValue(String.class);
                                                    NotificationHelper.sendNotification(studentToken, "Outpass verified", "Your outpass from " + currentItem.getFrom() + " to " + currentItem.getTo() + " has been verified");
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                        }


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                            else{
                                currentItem.setVerified(true);
                                outpassesRef.child("" + currentItem.getId()).child("verified").setValue(true);
                                usersRef.child(currentItem.getPersonName()).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String studentToken = dataSnapshot.getValue(String.class);
                                        NotificationHelper.sendNotification(studentToken, "Outpass verified", "Your outpass from " + currentItem.getFrom() + " to " + currentItem.getTo() + " has been verified");
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            });

        }

        @Override
        public int getItemCount() {
            return requests.size() ;
        }

        public  class RequestViewHolder extends RecyclerView.ViewHolder{
            ImageView image;
            TextView name,id,from,to,returnDate,leavingDate;
            Button bVerify, bDeny;

            public RequestViewHolder(@NonNull View itemView) {
                super(itemView);
                image= itemView.findViewById(R.id.StudentImage);
                name=itemView.findViewById(R.id.textViewName);
                id=itemView.findViewById(R.id.textViewOutpassId);
                from=itemView.findViewById(R.id.textViewFrom);
                to=itemView.findViewById(R.id.textViewTo);
                returnDate=itemView.findViewById(R.id.textViewReturnDate);
                leavingDate =itemView.findViewById(R.id.textViewLeavingDate);
                bVerify = itemView.findViewById(R.id.accept);
                bDeny = itemView.findViewById(R.id.deny);
            }
        }
    }
}
