package userphotograph.student.example.com.istogram;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static java.security.AccessController.getContext;

//import com.example.student.userphotograph.R;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText mEmailEd;
    private EditText mPasswordEd;
    private Button mSignInbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mAuth = FirebaseAuth.getInstance();
        mEmailEd = (EditText) findViewById(R.id.et_sign_in_email);
        mPasswordEd = (EditText) findViewById(R.id.et_sign_in_password);
        mSignInbtn = (Button) findViewById(R.id.sign_in_button);

        FirebaseUser user = mAuth.getCurrentUser();
        mEmailEd.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mPasswordEd.setEnabled(true);
                return false;
            }
        });

        if (user != null) {
            mEmailEd.setText(user.getEmail());
            mPasswordEd.setEnabled(false);
            toPosts();

        }
        mSignInbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signInWithEmailAndPassword(mEmailEd.getText().toString(), mPasswordEd.getText().toString()).addOnCompleteListener(HomeActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(HomeActivity.this, "Seuccessful sign in", Toast.LENGTH_SHORT).show();
                            toPosts();


                        } else {
                            mSignInbtn.setClickable(true);

                            Toast.makeText(HomeActivity.this, "Unsuccessful sign in", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

//        goToActivity();
//       // ImageView splashScreen = (ImageView) findViewById(R.id.splash_screen);
//
//
//    }
//
//    private void goToActivity() {
//        finish();
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//
//        if (user != null) {
////            intent= new Intent(this, HomeActivity.class);
//    //        Toast.makeText(this, "failed", Toast.LENGTH_LONG).show();
//
//        } else {
//            Toast.makeText(this, "failed oh sooo bad", Toast.LENGTH_LONG).show();
//        }
////            intent = new Intent(this, LoginActivity.class);}
//
    }

    public void toPosts() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragments, PostsFragment.newInstance())
                .addToBackStack(null)
                .commit();
        //finish();
    }
}
