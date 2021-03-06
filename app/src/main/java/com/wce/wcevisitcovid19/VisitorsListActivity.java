package com.wce.wcevisitcovid19;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wce.wcevisitcovid19.adapters.VisitorsListAdapter;
import com.wce.wcevisitcovid19.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class VisitorsListActivity extends AppCompatActivity {

    private static final String TAG = "VisitorsListActivity";
    EditText editVisitDate;
    ListView  visitorsListView;
    ArrayList<String> outsiderNamesList = new ArrayList<>();
    ArrayList<String> locationOfVisitList = new ArrayList<>();
    ArrayList<String> outsiderDatesOfVisitList = new ArrayList<>();
    ArrayList<String> outsiderIdList = new ArrayList<>();
    final Calendar myCalendar = Calendar.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    VisitorsListAdapter visitorsListAdapter;
    String visitDate;
    String outsiderId;
    TextView countTextView,infoTextView;
    String count;

    //trial
//    ArrayList<String> namesList = new ArrayList<>();
//    ArrayList<String> addressList = new ArrayList<>();
//    ArrayList<String> dateOfVisitList = new ArrayList<>();
//    ArrayList<String> idList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitors_list);

        visitorsListView = findViewById(R.id.visitorsListView);
        visitorsListAdapter = new VisitorsListAdapter(this, outsiderNamesList, locationOfVisitList);
        visitorsListView.setAdapter(visitorsListAdapter);

        countTextView = findViewById(R.id.display_count_text_view);
        infoTextView = findViewById(R.id.visitor_info_text_view);

        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        String m = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        String d = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

        if (m.length() == 1)
            m = "0" + m;

        if (d.length() == 1)
            d = "0" + d;

        editVisitDate = findViewById(R.id.edit_visit_date);
        visitDate = d + "/" + m + "/" + calendar.get(Calendar.YEAR);
        editVisitDate.setHint(visitDate);

        //trying to reduce data fetching time and implement re-usability
        //thereby increasing space consumption on device's RAM
        //successfully optimized
        DateUtils du;
        du = new DateUtils(visitDate);
        String fetchDate = du.extractDate();
        String fetchMonth = du.extractMonth();
        String fetchYear = du.extractYear();
        DatabaseReference selectedOutsidersDatabaseReference;
        selectedOutsidersDatabaseReference = database.getReference("Daily_assessment").child(fetchYear).child(fetchMonth).child(fetchDate).child("Temperature").child("Outsiders");
        selectedOutsidersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    infoTextView.setVisibility(View.GONE);
                    Log.i(TAG, "onDataChange: Found dataSnapshot for "+ visitDate);
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        String outsiderId = postSnapshot.getKey();
                        outsiderIdList.add(outsiderId);
                    }
                    count = String.valueOf(outsiderIdList.size());
                    countTextView.setText(count);
                    DatabaseReference outsiderDatabaseReference;
                    for (String id : outsiderIdList)
                    {
                        outsiderId = id;
                        outsiderDatabaseReference = database.getReference("Outsiders").child(outsiderId);
                        outsiderDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot postSnapshot) {
                                String dateOfVisit = postSnapshot.child("Date of visit").getValue(String.class);
                                String outsiderName = postSnapshot.child("Name").getValue(String.class);
                                String locationOfVisit = postSnapshot.child("Location of visit").getValue(String.class);
                                outsiderDatesOfVisitList.add(dateOfVisit);
                                outsiderNamesList.add(outsiderName);
                                locationOfVisitList.add(locationOfVisit);
                                visitorsListAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("error", error.getMessage());
                            }
                        });
                    }
                }
                else {
                    countTextView.setText("0");
                    infoTextView.setVisibility(View.VISIBLE);
                    Log.i(TAG, "onDataChange: No data found for date " + visitDate);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("error", error.getMessage());
            }
        });

//        DatabaseReference outsiderDatabaseReference;
//        for (String id : outsiderIdList)
//        {
//            outsiderId = id;
//            outsiderDatabaseReference = database.getReference("Outsiders").child(outsiderId);
//            outsiderDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot postSnapshot) {
//                    String dateOfVisit = postSnapshot.child("Date of visit").getValue(String.class);
//                    String outsiderName = postSnapshot.child("Name").getValue(String.class);
//                    String locationOfVisit = postSnapshot.child("Location of visit").getValue(String.class);
//                    outsiderDatesOfVisitList.add(dateOfVisit);
//                    outsiderNamesList.add(outsiderName);
//                    locationOfVisitList.add(locationOfVisit);
//                    visitorsListAdapter.notifyDataSetChanged();
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    Log.e("error", error.getMessage());
//                }
//            });
//        }





//        DatabaseReference outsidersDatabaseReference = database.getReference("Outsiders");
//        outsidersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//            {
//                for (DataSnapshot postSnapshot : dataSnapshot.getChildren())
//                {
//                    String dateOfVisit = postSnapshot.child("Date of visit").getValue(String.class);
//                    String outsiderId = postSnapshot.getKey();
//                    String outsiderName = postSnapshot.child("Name").getValue(String.class);
//                    String outsiderAddress = postSnapshot.child("Location of visit").getValue(String.class);
//                    dateOfVisitList.add(dateOfVisit);
//                    idList.add(outsiderId);
//                    namesList.add(outsiderName);
//                    addressList.add(outsiderAddress);
//                    if (visitDate.equals(dateOfVisit))
//                    {
//                        //add visitor to list
//                        outsiderNamesList.add(outsiderName);
//                        outsidersAddressList.add(outsiderAddress);
//                        outsiderIdList.add(outsiderId);
//                        Log.i(TAG, "onDataChange: Visitor visiting on : " + visitDate + ": " + outsiderName);
//                        visitorsListAdapter.notifyDataSetChanged();
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e("error", error.getMessage());
//            }
//        });

        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                month+=1;
                String smonth = String.valueOf(month);
                if(smonth.length() == 1)
                    smonth = "0"+ month;

                String date = String.valueOf(dayOfMonth);
                if(date.length() == 1)
                    date = "0"+ date;
                visitDate = date+"/"+smonth+"/"+year;
                editVisitDate.setText(visitDate);
            }
        };

        editVisitDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(VisitorsListActivity.this, dateSetListener, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        editVisitDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                Log.i(TAG, "afterTextChanged: Now date changed: "+visitDate);
                outsiderNamesList.clear();
                locationOfVisitList.clear();
                outsiderIdList.clear();
                visitorsListAdapter.notifyDataSetChanged();

                //trying to reduce time and data to fetch from firebase more
                DateUtils du;
                du = new DateUtils(visitDate);
                String fetchDate = du.extractDate();
                String fetchMonth = du.extractMonth();
                String fetchYear = du.extractYear();
                DatabaseReference selectedOutsidersDatabaseReference;
                selectedOutsidersDatabaseReference = database.getReference("Daily_assessment").child(fetchYear).child(fetchMonth).child(fetchDate).child("Temperature").child("Outsiders");
                selectedOutsidersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            infoTextView.setVisibility(View.GONE);
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                String outsiderId = postSnapshot.getKey();
                                outsiderIdList.add(outsiderId);
                            }

                            count = String.valueOf(outsiderIdList.size());
                            countTextView.setText(count);

                            DatabaseReference outsiderDatabaseReference;
                            for (String id : outsiderIdList)
                            {
                                outsiderId = id;
                                outsiderDatabaseReference = database.getReference("Outsiders").child(outsiderId);
                                outsiderDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot postSnapshot) {
                                        String dateOfVisit = postSnapshot.child("Date of visit").getValue(String.class);
                                        String outsiderName = postSnapshot.child("Name").getValue(String.class);
                                        String locationOfVisit = postSnapshot.child("Location of visit").getValue(String.class);
                                        outsiderDatesOfVisitList.add(dateOfVisit);
                                        outsiderNamesList.add(outsiderName);
                                        locationOfVisitList.add(locationOfVisit);
                                        visitorsListAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e("error", error.getMessage());
                                    }
                                });
                            }
                        }
                        else
                        {
                            countTextView.setText("0");
                            infoTextView.setVisibility(View.VISIBLE);
                            Log.i(TAG, "onDataChange: No visitor found for "+visitDate);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("error", error.getMessage());
                    }
                });

//                DatabaseReference outsiderDatabaseReference;
//                for (String id : outsiderIdList)
//                {
//                    outsiderId = id;
//                    outsiderDatabaseReference = database.getReference("Outsiders").child(outsiderId);
//                    outsiderDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot postSnapshot) {
//                            String dateOfVisit = postSnapshot.child("Date of visit").getValue(String.class);
//                            String outsiderName = postSnapshot.child("Name").getValue(String.class);
//                            String locationOfVisit = postSnapshot.child("Location of visit").getValue(String.class);
//                            outsiderDatesOfVisitList.add(dateOfVisit);
//                            outsiderNamesList.add(outsiderName);
//                            locationOfVisitList.add(locationOfVisit);
//                            visitorsListAdapter.notifyDataSetChanged();
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//                            Log.e("error", error.getMessage());
//                        }
//                    });
//                }




                //successfully optimized..
//                int i=0;
//                for(String dateOfVisit: dateOfVisitList)
//                {
//                    if(visitDate.equals(dateOfVisit))
//                    {
//                        outsiderNamesList.add(namesList.get(i));
//                        locationOfVisitList.add(addressList.get(i));
//                        outsiderIdList.add(idList.get(i));
//                        Log.i(TAG, "onDataChange: Visitor visiting on : " + visitDate + ": " + namesList.get(i));
//                        visitorsListAdapter.notifyDataSetChanged();
//                    }
//                    i++;
//                }

            }
        });

        visitorsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String userName = outsiderNamesList.get(position);
                String userType = "Outsiders";
                String userId = outsiderIdList.get(position);
                Intent intent = new Intent(VisitorsListActivity.this,UserDetailsActivity.class);
                intent.putExtra("userName",userName);
                intent.putExtra("userType",userType);
                intent.putExtra("userId",userId);
                startActivity(intent);
            }
        }  );

        outsiderIdList.clear();
        outsiderNamesList.clear();
        locationOfVisitList.clear();
    }
}