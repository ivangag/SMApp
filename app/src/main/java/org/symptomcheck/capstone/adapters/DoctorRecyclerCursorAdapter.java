package org.symptomcheck.capstone.adapters;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;

import org.symptomcheck.capstone.R;
import org.symptomcheck.capstone.model.Doctor;
import org.symptomcheck.capstone.provider.ActiveContract;

import java.text.BreakIterator;
import java.util.List;

/**
 * Created by igaglioti on 28/03/2015.
 */
public class DoctorRecyclerCursorAdapter extends
        CursorExRecyclerAdapter<DoctorRecyclerCursorAdapter.DoctorViewHolder> implements View.OnClickListener{

    private static String TAG = "DoctorRecyclerCursorAdapter";
    IRecyclerItemListener mListener;
    private int lastPosition = -1;
    private List<String> mExpandedPositions = Lists.newArrayList();
    private List<String> mCollapsedPositions = Lists.newArrayList();
    private int originalExpandAreaHeight;

    @Override
    public void onClick(View view) {
        /*
        DoctorViewHolder holder = (DoctorViewHolder) viewHeaderArea.getTag();
        // If the originalExpandAreaHeight is 0 then find the height of the View being used
        // This would be the height of the cardview
        //this.setIsRecyclable(false);
        if (originalExpandAreaHeight == 0) {
            originalExpandAreaHeight = holder.viewCheckInExpandableArea.getHeight();
        }

        final String itemId = String.valueOf(holder.getLayoutPosition());
        final boolean isExpandAreaVisible =  holder.viewCheckInExpandableArea.getVisibility() == View.VISIBLE;
        Log.d(TAG, String.format("onClickViewHolder=> ItemId:%s", itemId));
        if(isExpandAreaVisible) {
            if(mExpandedPositions.contains(itemId)) {
                mExpandedPositions.remove(itemId);
            }
            mCollapsedPositions.add(itemId);
        }else {
            if(mCollapsedPositions.contains(itemId)){
                mCollapsedPositions.remove(itemId);
            }
            mExpandedPositions.add(itemId);
        }
        notifyDataSetChanged();
        */
    }

    private void doExpand(int originalHeight, boolean animate,final DoctorViewHolder holder){

        //holder.viewCheckInExpandableArea.setVisibility(View.VISIBLE);
        if(animate) {
            final ValueAnimator valueAnimator = ValueAnimator.ofInt(0, originalHeight); // These values in this method can be changed to expand however much you like
            doAnimate(valueAnimator,holder);
        }
    }

    private void doCollapse(int originalHeight, boolean animate,final DoctorViewHolder holder){

        if(animate){
            final ValueAnimator valueAnimator = ValueAnimator.ofInt(originalHeight, 0);

            Animation a = new AlphaAnimation(1.00f, 0.00f); // Fade out

            a.setDuration(500);
            // Set a listener to the animation and configure onAnimationEnd
            a.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //holder.viewCheckInExpandableArea.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            // Set the animation on the custom view
            //holder.viewCheckInExpandableArea.startAnimation(a);
            doAnimate(valueAnimator,holder);
        }else {
            //holder.viewCheckInExpandableArea.setVisibility(View.GONE);
        }
    }

    private void doAnimate(ValueAnimator valueAnimator,final DoctorViewHolder holder) {

        valueAnimator.setDuration(500);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                //holder.viewCheckInExpandableArea.getLayoutParams().height = value;
                //holder.viewCheckInExpandableArea.requestLayout();
            }
        });
        valueAnimator.start();
    }

    private void handleTogglingExpandedAreaHolder(DoctorViewHolder holder) {

/*        final String itemId = String.valueOf(holder.getLayoutPosition());
        final boolean isExpandAreaVisible =  holder.viewCheckInExpandableArea.getVisibility() == View.VISIBLE;

        final boolean animate = false;
        if(mExpandedPositions.contains(itemId) && !isExpandAreaVisible){
            doExpand(originalExpandAreaHeight,animate,holder);
            Log.d(TAG, String.format("handleTogglingExpandedAreaHolder=>Expand ItemId:%s", itemId));
                *//*
                this.viewCheckInExpandableArea.setVisibility(View.VISIBLE);
                this.viewCheckInExpandableArea.requestLayout();
                *//*
        }else if(mCollapsedPositions.contains(itemId) && isExpandAreaVisible){
            doCollapse(originalExpandAreaHeight,animate,holder);
            Log.d(TAG, String.format("handleTogglingExpandedAreaHolder=>Collapse ItemId:%s", itemId));
                *//*
                this.viewCheckInExpandableArea.requestLayout();
                this.viewCheckInExpandableArea.setVisibility(View.GONE);
                *//*
        }else{

        }*/
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class DoctorViewHolder extends RecyclerView.ViewHolder{ /*implements View.OnClickListener*/
        // each data item is just a string in this case

        protected TextView mDoctorName;
        protected TextView mDoctorId;
        protected View mPhoneCallAction;
        protected View mPhoneCallContainer;
        protected View mEmailContainer;
        protected TextView mPhoneNumber;
        protected TextView mEmail;

        public DoctorViewHolder(View v) {
            super(v);
            //this.setIsRecyclable(false);
            mDoctorName = (TextView)v.findViewById(R.id.txtViewDoctorName);
            mDoctorId = (TextView)v.findViewById(R.id.txtViewDoctorId);
            mPhoneNumber = (TextView)v.findViewById(R.id.txtPhoneInfo);
            mEmail = (TextView)v.findViewById(R.id.txtEmailInfo);
            mPhoneCallAction = v.findViewById(R.id.imageDoctorCallInfo);
            mPhoneCallContainer = v.findViewById(R.id.phoneInfoContainer);
            mEmailContainer = v.findViewById(R.id.emailInfoContainer);

            //viewCheckInHeader.setTag(this);
        }

    }

    public interface IRecyclerItemListener {
        void onItemToggled(int position);
        void onDoctorPhoneClicked(Doctor doctor);
    }


    public void addEventListener(IRecyclerItemListener listener){
        mListener = listener;
    }

    private final Context mContext;

    @Override
    protected void onContentChanged() {
        lastPosition = -1;
    }

    public DoctorRecyclerCursorAdapter(Cursor cursor, Context context) {
        super(context,cursor,FLAG_REGISTER_CONTENT_OBSERVER);
        this.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                
            }

            @Override
            public void onChanged() {
                super.onChanged();
                lastPosition = -1;
            }
        });
        mContext = context;
    }

    

    private String traceVisibility(View v){
        return v.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE";
    }        

    @Override
    public void onViewRecycled(DoctorViewHolder holder) {
        super.onViewRecycled(holder);
        //holder.resetToDefault();
    }

    @Override
    public void onBindViewHolder(final DoctorViewHolder holder, Cursor cursor) {
       
        Animation slide = AnimationUtils.loadAnimation(mContext, (cursor.getPosition() > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        //holder.itemView.startAnimation(slide);
        lastPosition = holder.getLayoutPosition();
        //handleTogglingExpandedArea(holder);
        //this.handleTogglingExpandedAreaHolder(holder);
        final Doctor doctor = Doctor.getByDoctorNumber(cursor.getString(cursor.getColumnIndex(ActiveContract.DOCTORS_COLUMNS.DOCTOR_ID)));


        if(doctor != null) {
            final String email = doctor.getEmail();
            final String phoneNumber = doctor.getPhoneNumber();
            //final int id = item.getItemId();
            holder.mDoctorName.setText(String.format("%s %s",doctor.getFirstName(),doctor.getLastName()));
            holder.mDoctorId.setText(String.format("%s",doctor.getUniqueDoctorId()));
            holder.mPhoneNumber.setText(phoneNumber);
            holder.mEmail.setText(email);

            holder.mPhoneCallAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doPhoneCall(phoneNumber);
                }
            });
            holder.mEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doSendEmail(email);
                }
            });
            holder.mPhoneCallContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doPhoneCall(phoneNumber);
                }
            });
            holder.mEmailContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doSendEmail(email);
                }
            });
        }

        Log.d(TAG, String.format("onBindViewHolderCursor=> DoctorID:%s. CursorPosition:%d. CurrentPosition:%d.",
                doctor == null ? -1 : doctor.getUniqueDoctorId(), lastPosition, holder.getLayoutPosition()));
    }

    private void doSendEmail(String email) {
        if (email != null) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
            i.putExtra(Intent.EXTRA_SUBJECT, "Your Patient needs you");
            i.putExtra(Intent.EXTRA_TEXT, "Here write your body message");
            try {
                //mContext.startActivity(Intent.createChooser(i, "Send e-mail...").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                mContext.startActivity(i);
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(mContext, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void doPhoneCall(String phoneNumber) {
        if (phoneNumber != null) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //callIntent.setData(Uri.parse("tel:123456789"));
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            mContext.startActivity(callIntent);
        }
    }


    // Create new views (invoked by the layout manager)
    @Override
    public DoctorViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_google_cardview_doctors, parent, false);
        // set the view's size, margins, paddings and layout parameters        
        v.setClickable(true);

        DoctorViewHolder holder = new DoctorViewHolder(v);
        //holder.viewCheckInHeader.setOnClickListener(this);
        //Log.d(TAG, String.format("onCreateViewHolder=> ItemId:%d OldPosition:%d CurrentPosition:%d. ExpandedArea:%s",
        //        holder.getItemId(), holder.getOldPosition(), holder.getLayoutPosition(), traceVisibility(holder.viewCheckInExpandableArea)));
        return holder;
    }
}
