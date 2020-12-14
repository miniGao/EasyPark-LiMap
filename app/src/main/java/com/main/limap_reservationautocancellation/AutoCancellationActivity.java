package com.main.limap_reservationautocancellation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.main.limap_reservationautocancellation.models.ParkingLot;
import com.main.limap_reservationautocancellation.models.ParkingSpot;
import com.main.limap_reservationautocancellation.models.Reservation;

import java.util.List;

public class AutoCancellationActivity extends AppCompatActivity {

    private String reservationId;
    private String parkingLotId = "V77E58vEwmoL6Fv6nxo7";
    private Button btnArrived;
    private Button btnAddDummy;
    private TextView txtReservationId;
    private static final long DURATION = 10 * 1000;
    private TextView timer;
    private Handler timerHandler;
    long endTime, updateTime = 0L;
    int mSec, sec, min;

    private static final String TAG = "AutoCancelActivity";

    private FirebaseFirestore db;
    private Reservation reservation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_cancellation);

        db = FirebaseFirestore.getInstance();

        txtReservationId = findViewById(R.id.txtReservationId);
        timer = findViewById(R.id.timer);
        timerHandler = new Handler();

        // for integration
//        Intent intent = getIntent();
//        Bundle bundle = intent.getExtras();
//        reservationId = bundle.getString("reservationId");
//        Log.e("RESERVATION ID:", reservationId);
//        txtReservationId.setText(reservationId);

        btnAddDummy = findViewById(R.id.btnAddDummy);
        btnAddDummy.setOnClickListener(v->{
            DocumentReference docRef =  db.collection("ReservationDemo").document();
            Log.d(TAG, "docId: " + docRef.getId());
            txtReservationId.setText(docRef.getId());
            Reservation r = new Reservation();
            r.setCost(12);
            r.setParkingLotId(parkingLotId);
            r.setParkingSpotId(1);
            r.setReserveTime(Timestamp.now());
            r.setUserId("QLdaWjjxgZONlaEPd6o3HJ9rUud2");

            updateFreeSpotToPending();

            Task insert = docRef.set(r);
            insert.addOnSuccessListener(o -> {
                Log.d(TAG, "YAY! dummy added");
                endTime = SystemClock.uptimeMillis() + DURATION;
                timerHandler.postDelayed(timerRun, 0);
            });
            insert.addOnFailureListener(o->{
                Log.e(TAG, "OhO.. dummy escaped..");
            });
        });

        btnArrived = findViewById(R.id.btnArrived);
        btnArrived.setOnClickListener(v -> {
            checkReservation();
            Intent intentMap = new Intent(getApplicationContext(), MapActivity.class);
            startActivity(intentMap);
        });
    }

    public Runnable timerRun = new Runnable() {
        @Override
        public void run() {
            updateTime = endTime - SystemClock.uptimeMillis();
            sec = (int) (updateTime/1000);
            min = sec /60;
            sec = sec%60;
            mSec = (int) (updateTime%100);
            timer.setText(String.format("%02d:%02d:%02d", min, sec, mSec));
            timerHandler.postDelayed(this,50);
            if(updateTime <= 0){
                timerHandler.removeCallbacks(timerRun);
                timer.setText("00:00:00");
                Log.e(TAG, "times up");
                deleteReservation();
            }
        }
    };

    private void deleteReservation(){
        // delete reservation
        if(!txtReservationId.getText().toString().equals("")){
            reservationId = txtReservationId.getText().toString();
            DocumentReference docRef =  db.collection("ReservationDemo").document(reservationId);
            docRef.get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "reservation data: " + document.getData());
                        Log.e(TAG, "RESERVATION EXPIRED!");
                        docRef.delete().addOnSuccessListener(o->{
                            // change spot status to free
                            updatePendingSpotToFree();

                            Log.d(TAG, "RESERVATION DELETED!");
                            Intent intentToMap = new Intent(getApplicationContext(), MapActivity.class);
                            startActivity(intentToMap);
                        }).addOnFailureListener(o -> {
                            Log.e(TAG, "oops, error on reservation deletion");
                        });
                    } else {
                        Log.d(TAG, "No such reservation");
                    }
                }else{
                    Log.d(TAG, "get reservation failed with ", task.getException());
                }
            });
        }else{
            Log.d(TAG, "Invalid Reservation ID");
        }
    }

    private void checkReservation(){
        if(!txtReservationId.getText().toString().equals("")){
            reservationId = txtReservationId.getText().toString();
            DocumentReference docRef =  db.collection("ReservationDemo").document(reservationId);
            docRef.get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "reservation data: " + document.getData());
                        reservation = document.toObject(Reservation.class);
                        if(reservation.getReserveTime().getSeconds() + 15 * 60 > Timestamp.now().getSeconds()){
                            Log.d(TAG, "RESERVATION OK!");

                            updatePendingSpotToBooked();

                            timerHandler.removeCallbacks(timerRun);
                            timer.setText("00:00:00");
                            endTime=0L;
                            updateTime=0L;
                            sec=0;
                            min=0;
                            mSec=0;
                        }else {
                            Log.e(TAG, "RESERVATION EXPIRED!");
                            docRef.delete().addOnSuccessListener(o->{
                                Log.d(TAG, "RESERVATION DELETED!");
                            }).addOnFailureListener(o -> {
                                Log.e(TAG, "oops, error on reservation deletion");
                            });
                        }
                    } else {
                        Log.d(TAG, "No such reservation");
                    }
                }else{
                    Log.d(TAG, "get reservation failed with ", task.getException());
                }
            });
        }else{
            Log.d(TAG, "Invalid Reservation ID");
        }
    }

    private void updateFreeSpotToPending(){
        updateSpotStatus("free", "pending");
    }

    private void updatePendingSpotToFree(){
        updateSpotStatus("pending", "free");
    }

    private void updatePendingSpotToBooked(){
        updateSpotStatus("pending", "booked");
    }

    private void updateSpotStatus(String previous, String after){
        DocumentReference lotDocRef = db.collection("ParkingLotDemo").document(parkingLotId);

        lotDocRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d("TAG", "reservation data: " + document.getData());

                    ParkingLot existingParkingLot = document.toObject(ParkingLot.class);

                    List<ParkingSpot> spots=existingParkingLot.getSpots();

                    if(spots != null){
                        for(ParkingSpot s : spots){
                            if(s.getStatus().equals(previous)){
                                s.setStatus(after);
                                break;
                            }
                        }

                        lotDocRef.update("spots", spots)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("TAG", "DocumentSnapshot successfully updated!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("TAG", "Error updating document", e);
                                    }
                                });
                    }

                } else {
                    Log.d("TAG", "No such Parking Lot");
                }
            }else{
                Log.d("TAG", "get parking lot failed with ", task.getException());
            }
        });
    }
}