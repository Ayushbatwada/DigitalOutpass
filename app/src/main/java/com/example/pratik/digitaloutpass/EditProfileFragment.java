package com.example.pratik.digitaloutpass;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;
import static android.support.constraint.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EditProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EditProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditProfileFragment extends Fragment {

    private static final int REQUEST_CAMERA = 3;
    private static final int SELECT_FILE = 2;
    private static final String FILE_NAME = "profilepic";

    FirebaseUser currentUser;
    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
    EditText nameS;
    EditText phone;
    ImageView prof;
    Button save;

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;

    DatabaseReference uref;
    StorageReference mStorageRef;

    //IMAGE HOLD URI
    Uri imageHoldUri = null;

    //PROGRESS DIALOG
    ProgressDialog mProgress;

    private OnFragmentInteractionListener mListener;

    public EditProfileFragment() {
        // Required empty public constructor
    }



    public static EditProfileFragment newInstance(FirebaseUser curUser) {
        EditProfileFragment fragment = new EditProfileFragment();
        fragment.currentUser = curUser;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                //Logic check user
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                }
            }
        };

        uref = FirebaseDatabase.getInstance().getReference().child("users");

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        // Inflate the layout for this fragment
        nameS = v.findViewById(R.id.edit_name_stu);
        phone = v.findViewById(R.id.edit_phone_stu);
        prof = v.findViewById(R.id.profPicSet);
        save = v.findViewById(R.id.btn_save_stu);

        initialize();


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveUserProfile();

            }
        });

        prof.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickProfilePic();
            }
        });


        return v;


    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        File file = new File(getActivity().getFilesDir(), FILE_NAME);
        try {
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
//            String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "path", null);
//            return Uri.parse(file.getAbsolutePath());
            return  Uri.fromFile(file);
        } catch (FileNotFoundException e) {

            e.printStackTrace();
            return null;
        }
    }

    private void initialize() {
        if (currentUser != null) {
            if (currentUser.getDisplayName() != null) {
//                nameS.setText(currentUser.getDisplayName());
                usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (phone.getText() == null || phone.getText().toString().equals("")) {
                            phone.setText(dataSnapshot.child("phoneNo").getValue(Long.class) + "");
                        }
                        nameS.setText(dataSnapshot.child("name").getValue(String.class));

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            File curUserImgFile = new File(getActivity().getFilesDir(), FILE_NAME);

            ContextWrapper cw = new ContextWrapper(getContext());
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            File mypath=new File(directory,FILE_NAME+".jpg");
            if (mypath.exists()) {
//                Glide.with(this)
//                        .load(curUserImgFile)
//                        .into(prof);
                Bitmap bitmap[] = new Bitmap[1];
                Glide.with(getActivity())
                        .asBitmap()
                        .load(mypath)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                prof.setImageBitmap(resource);
                                bitmap[0]= resource;
                                imageHoldUri  = getImageUri(getContext(),bitmap[0]);
                            }
                        });

                Toast.makeText(getActivity(), "File image loaded", Toast.LENGTH_SHORT).show();
            }

            usersRef.child(currentUser.getUid()).child("imageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot!=null) {
//                        Glide.with(EditProfileFragment.this)
//                                .load(dataSnapshot.getValue(String.class))
//                                .into(prof);
                        Bitmap bitmap[] = new Bitmap[1];
                        Glide.with(getActivity())
                                .asBitmap()
                                .load(dataSnapshot.getValue(String.class))
                                .into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        prof.setImageBitmap(resource);
                                        bitmap[0]= resource;
                                        imageHoldUri  = getImageUri(getContext(),bitmap[0]);

                                    }
                                });

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }


    private void saveImage(Bitmap image) {
        File file = new File(getActivity().getFilesDir(), FILE_NAME);
        if (file.exists()) {
            Toast.makeText(getActivity(), "File already exists,overwriting", Toast.LENGTH_SHORT).show();
            getContext().deleteFile(FILE_NAME);
           if(file.delete()){
               Log.d("File deletion", "saveImage: "+FILE_NAME +" deleted successfully");;
           }
           else{
               Log.d("File deletion", "saveImage: unable to delete");
           }
        }
        //FileOutputStream fileOutputStream = new FileOutputStream(file);
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            if(image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
                Toast.makeText(getActivity(), "image saved", Toast.LENGTH_SHORT).show();
                Log.d("Image save", "saveImage: Image saved");
                outputStream.close();
                Log.d(TAG, "saveImage: output stream closed");
            }
            else{
                Log.d(TAG, "saveImage: unable to save image");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        uploadImageToFirebase();
    }

    private void uploadImageToFirebase(String username, long phoneno) {
        StorageReference mChildStorage = mStorageRef.child("User_Profile").child(currentUser.getEmail() + ".jpeg");
        String profilePicUrl = imageHoldUri.getLastPathSegment();

        mChildStorage.putFile(imageHoldUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            mChildStorage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    String imageUrl = uri.toString();
                    usersRef.child(currentUser.getUid()).child("imageUrl").setValue(imageUrl);

                }
            });
                usersRef.child(currentUser.getUid()).child("name").setValue(username);
                usersRef.child(currentUser.getUid()).child("phoneNo").setValue(phoneno);


                mProgress.dismiss();
                Bitmap image = null;
                try {
                    image = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageHoldUri);
                    saveToInternalStorage(image,FILE_NAME);
                } catch (IOException e) {
//                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Unable to save file locally", Toast.LENGTH_SHORT).show();
                }

                Toast.makeText(getActivity(), "Updated successfully", Toast.LENGTH_SHORT).show();
//                Activity parentActivity= getActivity();
//                getActivity().getFragmentManager().popBackStack();
//                if(parentActivity instanceof MainActivity) {
//                    MyOutpassesFragment myOutpassesFragment = MyOutpassesFragment.newInstance();
//                    getActivity().getSupportFragmentManager().beginTransaction()
//                            .replace(R.id.content_main_relative, myOutpassesFragment, "findThisFragment")
//                            .addToBackStack(null)
//                            .commit();
//                }
//                else if(parentActivity instanceof  WardenActivity){
//
//                }

                //finish fragment
//                getActivity().getSupportFragmentManager().beginTransaction().remove(EditProfileFragment.this).commit();
//                getActivity().getSupportFragmentManager().popBackStackImmediate();

            }
        });
    }

    private void saveUserProfile() {


        final String username;
        final long phoneno;

        username = nameS.getText().toString().trim();

        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(phone.getText().toString())) {
            phoneno = Long.parseLong(phone.getText().toString());


            if (imageHoldUri != null) {
                mProgress = ProgressDialog.show(getActivity(), "Saving profile", "Please wait...");
//                mProgress.setTitle("Saveing Profile");
//                mProgress.setMessage("Please wait....");
//                mProgress.show();
                mProgress.show();

                uploadImageToFirebase(username, phoneno);
            } else {

                Toast.makeText(getActivity(), "Please select the profile pic", Toast.LENGTH_LONG).show();

            }

        } else {

            Toast.makeText(getActivity(), "Please enter username and status", Toast.LENGTH_LONG).show();

        }


    }

    private void galleryIntent() {

        //CHOOSE IMAGE FROM GALLERY
        Log.d("gola", "entered here");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(getActivity(), "onActivityResult", Toast.LENGTH_SHORT).show();
        if (requestCode == REQUEST_CAMERA && data != null && resultCode == RESULT_OK) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
//            String[] filePathColumn = {MediaStore.Images.Media.DATA};
//            Cursor cursor = getActivity().getContentResolver().query(imageHoldUri,
//                    filePathColumn, null, null, null);
//            cursor.moveToFirst();
//            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//            String picturePath = cursor.getString(columnIndex);


            prof.setImageBitmap(image);
            imageHoldUri = getImageUri(getContext(), image);

        } else if (requestCode == SELECT_FILE && data != null && resultCode == RESULT_OK) {
            Toast.makeText(getActivity(), "seleted file", Toast.LENGTH_SHORT).show();
            imageHoldUri = data.getData();
//            String[] filePathColumn = {MediaStore.Images.Media.DATA};
//            Cursor cursor = getActivity().getContentResolver().query(imageHoldUri,
//                    filePathColumn, null, null, null);
//            cursor.moveToFirst();
//            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//            String picturePath = cursor.getString(columnIndex);

            Bitmap image = null;
            try {
                image = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageHoldUri);
                prof.setImageBitmap(image);
//                saveImage(image);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void cameraIntent() {

        //CHOOSE CAMERA

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void pickProfilePic() {
        //DISPLAY DIALOG TO CHOOSE CAMERA OR GALLERY

        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add Photo!");

        //SET ITEMS AND THERE LISTENERS
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Take Photo")) {
                    cameraIntent();
                } else if (items[item].equals("Choose from Library")) {
                    galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
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

    private void loadImageFromStorage(String path)
    {

        try {
            File f=new File(path, "profile.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
//            ImageView img=(ImageView)findViewById(R.id.imgPicker);
//            img.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

    }

    private String saveToInternalStorage(Bitmap bitmapImage, String fileName){
        ContextWrapper cw = new ContextWrapper(getContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,fileName+".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }
}
