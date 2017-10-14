package io.github.utshaw.iut;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

/**
 * Created by Utshaw on 10/13/2017.
 */

public class MainActivity extends AppCompatActivity {
    public  final int RC_SIGN_IN = 1;
    private final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    protected FirebaseAuth mFirebaseAuth;

    private TextView name;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseAuth = FirebaseAuth.getInstance();

        name = (TextView) findViewById(R.id.Name);

         sharedPref = this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                startActivity(new Intent(MainActivity.this,MapsActivity.class));
            }
        }
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i("Utshaw", "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("Utshaw", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    public void login(View view) {

        String userame = name.getText().toString();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.Username), userame);
        editor.apply();

        //Toast.makeText(this, "Username :" + userame, Toast.LENGTH_SHORT).show();

        if(userame == null || userame.equals("")){
            Toast.makeText(this, "Please Enter username" , Toast.LENGTH_SHORT).show();
            return;
        }

        if(mFirebaseAuth.getCurrentUser() == null) {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(
                                    Arrays.asList(
                                            new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build()))
                            .build(),
                    RC_SIGN_IN);
        }
        else{
            startActivity(new Intent(MainActivity.this,MapsActivity.class));
//            try {
//                Intent intent =
//                        new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
//                                .build(this);
//                startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
//            } catch (GooglePlayServicesRepairableException e) {
//                // TODO: Handle the error.
//            } catch (GooglePlayServicesNotAvailableException e) {
//                // TODO: Handle the error.
//            }

        }
    }


}
