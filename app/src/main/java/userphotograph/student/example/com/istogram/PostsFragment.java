package userphotograph.student.example.com.istogram;


import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import userphotograph.student.example.com.istogram.Model;
import userphotograph.student.example.com.istogram.Constants;
import userphotograph.student.example.com.istogram.FirebaseHelper;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static userphotograph.student.example.com.istogram.Constants.ADDRESS;
import static userphotograph.student.example.com.istogram.Constants.AVATAR;
import static userphotograph.student.example.com.istogram.Constants.AVATAR_URI;
import static userphotograph.student.example.com.istogram.Constants.CAMERA_INFO;
import static userphotograph.student.example.com.istogram.Constants.EMAIL;
import static userphotograph.student.example.com.istogram.Constants.GALLERY;
import static userphotograph.student.example.com.istogram.Constants.POSTS;
import static userphotograph.student.example.com.istogram.Constants.NAME;
import static userphotograph.student.example.com.istogram.Constants.PHONE;
import static userphotograph.student.example.com.istogram.Constants.PHOTOGRAPHS;
import static userphotograph.student.example.com.istogram.Constants.USER_ID;

public class PostsFragment extends Fragment implements View.OnClickListener {

//    private EditText mName;
//    private EditText mAddress;
//    private EditText mCameraInfo;
//    private EditText mPhone;
    private ImageView mAvatar;

    private Uri mFilePath;

    private DatabaseReference mDatabaseRef;
    private DatabaseReference mDatabasePostsRef;
    private StorageReference mStorageAvatarRef;
    private StorageReference mStoragePostsRef;

    private List<Model> mItemViewPager;
    private FirebaseUser mUser;
    private EditText photoTitle;
    private AlertDialog alertDialog;
    private FloatingActionButton saveInfo;
    private TextView mName;
    private TextView numLike;


    public static PostsFragment newInstance() {
        return new PostsFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_posts, container, false);

        findViewById(rootView);
        firebaseRef();

        mItemViewPager = new ArrayList<>();
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //recyclerView.setHasFixedSize(true);
        onCreateFirebaseRecyclerAdapter(recyclerView);

        writeWithFbDb();
        //FirebaseHelper.downloadImageAndSetAvatar(mStorageAvatarRef, mAvatar);

        return rootView;
    }


//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putString(PHONE, mPhone.getText().toString());
//        outState.putString(ADDRESS, mAddress.getText().toString());
//        outState.putString(CAMERA_INFO, mCameraInfo.getText().toString());
//    }


    private void findViewById(View rootView) {
      mName = (EditText) rootView.findViewById(R.id.et_st_name);
      numLike = (TextView) rootView.findViewById(R.id.posts_numLikes);
//        mAddress = (EditText) rootView.findViewById(R.id.et_st_address);
//        mCameraInfo = (EditText) rootView.findViewById(R.id.st_camera_info);
//        mPhone = (EditText) rootView.findViewById(R.id.et_st_phone);
        mAvatar = (ImageView) rootView.findViewById(R.id.st_avatar);
        ImageView mAddImg = (ImageView) rootView.findViewById(R.id.add_image);
        saveInfo = (FloatingActionButton) rootView.findViewById(R.id.save_info);

       mName.setHorizontallyScrolling(true);
        saveInfo.setOnClickListener(this);
        mAvatar.setOnClickListener(this);
        mAddImg.setOnClickListener(this);
    }

    private void onCreateFirebaseRecyclerAdapter(RecyclerView recyclerView) {

        final FirebaseRecyclerAdapter<Model, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Model, MyViewHolder>(
                Model.class,
                R.layout.grid_view_item,
                MyViewHolder.class,
                mDatabasePostsRef
        ) {
            @Override
            protected void populateViewHolder(MyViewHolder viewHolder, Model model, final int position) {
                viewHolder.tvPosts.setText(model.getTitle());
                viewHolder.numLikes.setText(model.getNumLikes());
                Glide.with(getActivity())
                        .load(model.getImageUri())
                        .into(viewHolder.imgPosts);

                mItemViewPager.add(model);

                viewHolder.imgPosts.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(getString(R.string.images), (Serializable) mItemViewPager);
                        bundle.putInt(getString(R.string.position), position);

                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                        SlideshowDialogFragment newFragment = SlideshowDialogFragment.newInstance();
                        newFragment.setArguments(bundle);
                        newFragment.show(ft, getString(R.string.slideshow));
                    }
                });

                viewHolder.imgPosts.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        final AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(getContext(), R.style.MyDialogTheme);
                        mAlertDialog.setTitle(R.string.removed)
                                .setMessage(R.string.are_you_sure);
                        mAlertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                String imageName = getItem(position).getImageName();
                                getRef(position).removeValue();

                                StorageReference sRef = FirebaseStorage.getInstance()
                                        .getReference().child(PHOTOGRAPHS).child(POSTS)
                                        .child(mUser.getUid()).child(imageName);
                                sRef.delete();

                                notifyDataSetChanged();
                                Toast.makeText(getContext(), R.string.removed, Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getContext(), R.string.canceled, Toast.LENGTH_SHORT).show();
                            }
                        })
                                .create().show();
                        return true;
                    }
                });
            }
        };
        recyclerView.setAdapter(adapter);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imgPosts;
        TextView tvPosts;
        ImageView likeIcon;
        public TextView numLikes;

        public MyViewHolder(View view) {
            super(view);
            tvPosts = (TextView) view.findViewById(R.id.tv_image_posts);
            imgPosts = (ImageView) view.findViewById(R.id.posts_img);
            numLikes = (TextView) view.findViewById(R.id.posts_numLikes);
            likeIcon =(ImageView)view.findViewById(R.id.posts_like_icon);
            likeIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int nl=Integer.parseInt(numLikes.getText().toString())+1;
                    numLikes.setText(String.valueOf(nl));
                }
            });
        }
    }

    private void firebaseRef() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        mUser = auth.getCurrentUser();

        assert mUser != null;
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child(PHOTOGRAPHS).child(mUser.getUid());
        mDatabasePostsRef = mDatabaseRef.child(POSTS);

        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        mStorageAvatarRef = mStorageRef.child(PHOTOGRAPHS).child(AVATAR).child(mUser.getUid());
        mStoragePostsRef = mStorageRef.child(PHOTOGRAPHS).child(POSTS).child(mUser.getUid());

        mDatabaseRef.child(USER_ID).setValue(mUser.getUid());
        mDatabaseRef.child(EMAIL).setValue(mUser.getEmail());
    }

    private void writeWithFbDb() {
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child(NAME).getValue(String.class);
//                String address = dataSnapshot.child(ADDRESS).getValue(String.class);
//                String cameraInfo = dataSnapshot.child(CAMERA_INFO).getValue(String.class);
//                String phone = dataSnapshot.child(PHONE).getValue(String.class);

//                mName.setText(name);
//                mAddress.setText(address);
//                mCameraInfo.setText(cameraInfo);
//                mPhone.setText(phone);
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }
    //String uploadId = mDatabasePostsRef.push().getKey();

    @Override
    public void onClick(View v) {
        switch (v.getId()) {


//    case R.id.save_info: {
//        asePostsRef.chimDatabld(uploadId).child("numLikes").setValue(numLike.getText().toString());
//////                mDatabaseRef.child(ADDRESS).setValue(mAddress.getText().toString());
//////                mDatabaseRef.child(CAMERA_INFO).setValue(mCameraInfo.getText().toString());
//////                mDatabaseRef.child(PHONE).setValue(mPhone.getText().toString());
//////                Toast.makeText(getContext(), "Successfull saveing dates", Toast.LENGTH_SHORT).show();
////
////                break;
////            }
//
////            case R.id.st_avatar: {
////                choosePic(Constants.REQUEST_AVATAR_CHOOSE_PICK);
////                break;
//         }
            case R.id.add_image: {
                AlertDialog.Builder dialogBuilder = initDialog();
                alertDialog = dialogBuilder.create();
                alertDialog.show();
                break;
            }
        }
    }

    private AlertDialog.Builder initDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle("Uploading ...");
        dialogBuilder.setView(getDialogLayout());
        return dialogBuilder;
    }

    private View getDialogLayout() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.upload_dialog_layout, null);
        photoTitle = (EditText) dialogView.findViewById(R.id.et_dialog_title);
        numLike=(TextView)dialogView.findViewById(R.id.posts_numLikes);
        final Button btnChoosePhoto = (Button) dialogView.findViewById(R.id.btn_choose_photo);
        btnChoosePhoto.setEnabled(false);
        photoTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    btnChoosePhoto.setEnabled(true);
                } else btnChoosePhoto.setEnabled(false);
            }
        });


        btnChoosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePic(Constants.REQUEST_POSTS_CHOOSE_PICK);
            }
        });
        return dialogView;
    }

    private void choosePic(int requestCode) {
        Intent choosePicIntent = new Intent(Intent.ACTION_GET_CONTENT);
        choosePicIntent.setType("image/*");
        startActivityForResult(choosePicIntent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Constants.REQUEST_AVATAR_CHOOSE_PICK && resultCode == RESULT_OK) {
            final Uri uri = data.getData();

            mStorageAvatarRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mAvatar.setImageURI(uri);
                    @SuppressWarnings("VisibleForTests") Uri uri = taskSnapshot.getDownloadUrl();
                    assert uri != null;
                    mDatabaseRef.child(AVATAR_URI).setValue(uri.toString());
                }
            });
        }
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_POSTS_CHOOSE_PICK && resultCode == RESULT_OK && data.getData() != null) {
            mFilePath = data.getData();
            String mImageName = System.currentTimeMillis() + "." + FirebaseHelper.getFileExtension(mFilePath, getActivity());
            FirebaseHelper.upload(getContext(), mImageName, photoTitle, numLike, mDatabasePostsRef, mStoragePostsRef, mFilePath);
            alertDialog.dismiss();
        }
    }
}
