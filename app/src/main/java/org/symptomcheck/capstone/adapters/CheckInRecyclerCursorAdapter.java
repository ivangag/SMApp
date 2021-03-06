package org.symptomcheck.capstone.adapters;

import android.animation.ValueAnimator;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.collect.Lists;

import org.symptomcheck.capstone.R;
import org.symptomcheck.capstone.model.CheckIn;
import org.symptomcheck.capstone.model.FeedStatus;
import org.symptomcheck.capstone.model.PainLevel;
import org.symptomcheck.capstone.provider.ActiveContract;
import org.symptomcheck.capstone.utils.CheckInUtils;
import org.symptomcheck.capstone.utils.Constants;
import org.symptomcheck.capstone.utils.DateTimeUtils;

import java.util.List;

/**
 * Created by igaglioti on 09/02/2015.
 */
public class CheckInRecyclerCursorAdapter extends
        CursorExRecyclerAdapter<CheckInRecyclerCursorAdapter.CheckInViewHolder> implements View.OnClickListener{

    private static String TAG = "CheckInRecyclerCursorAdapter";
    IRecyclerItemToggleListener mListener;
    private int lastPosition = -1;
    private List<String> mExpandedPositions = Lists.newArrayList();
    private List<String> mCollapsedPositions = Lists.newArrayList();
    private int originalExpandAreaHeight;

    @Override
    public void onClick(View viewHeaderArea) {
        CheckInViewHolder holder = (CheckInViewHolder) viewHeaderArea.getTag();
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
    }

    private void doExpand(int originalHeight, boolean animate,final CheckInViewHolder holder){

        holder.viewCheckInExpandableArea.setVisibility(View.VISIBLE);
        if(animate) {
            final ValueAnimator valueAnimator = ValueAnimator.ofInt(0, originalHeight); // These values in this method can be changed to expand however much you like
            doAnimate(valueAnimator,holder);
        }
    }

    private void doCollapse(int originalHeight, boolean animate,final CheckInViewHolder holder){

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
                    holder.viewCheckInExpandableArea.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            // Set the animation on the custom view
            holder.viewCheckInExpandableArea.startAnimation(a);
            doAnimate(valueAnimator,holder);
        }else {
            holder.viewCheckInExpandableArea.setVisibility(View.GONE);
        }
    }

    private void doAnimate(ValueAnimator valueAnimator,final CheckInViewHolder holder) {

        valueAnimator.setDuration(500);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                holder.viewCheckInExpandableArea.getLayoutParams().height = value;
                holder.viewCheckInExpandableArea.requestLayout();
            }
        });
        valueAnimator.start();
    }

    private void handleTogglingExpandedAreaHolder(CheckInViewHolder holder) {
        final String itemId = String.valueOf(holder.getLayoutPosition());
        final boolean isExpandAreaVisible =  holder.viewCheckInExpandableArea.getVisibility() == View.VISIBLE;

        final boolean animate = false;
        if(mExpandedPositions.contains(itemId) && !isExpandAreaVisible){
            doExpand(originalExpandAreaHeight,animate,holder);
            Log.d(TAG, String.format("handleTogglingExpandedAreaHolder=>Expand ItemId:%s", itemId));
                /*
                this.viewCheckInExpandableArea.setVisibility(View.VISIBLE);
                this.viewCheckInExpandableArea.requestLayout();
                */
        }else if(mCollapsedPositions.contains(itemId) && isExpandAreaVisible){
            doCollapse(originalExpandAreaHeight,animate,holder);
            Log.d(TAG, String.format("handleTogglingExpandedAreaHolder=>Collapse ItemId:%s", itemId));
                /*
                this.viewCheckInExpandableArea.requestLayout();
                this.viewCheckInExpandableArea.setVisibility(View.GONE);
                */
        }else{

        }
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class CheckInViewHolder extends RecyclerView.ViewHolder{ /*implements View.OnClickListener*/
        // each data item is just a string in this case
        protected TextView vCheckInStatus;
        protected TextView vCheckInTime;
        protected View viewCheckInExpandableArea;
        protected View viewCheckInHeader;
        protected ListView mListView;
        protected ImageButton mBtnCheckInDetailsInfo;
        protected int originalHeight;

        public CheckInViewHolder(View v) {
            super(v);
            //this.setIsRecyclable(false);
            vCheckInStatus =  (TextView) v.findViewById(R.id.txtViewCheckInPainLevel);
            vCheckInTime = (TextView)  v.findViewById(R.id.txtViewCheckInTime);
            viewCheckInExpandableArea =  v.findViewById(R.id.viewCheckInDetails);
            viewCheckInHeader =  v.findViewById(R.id.viewHeaderCheckInDetails);
            mListView = (ListView)  v.findViewById(R.id.list_medicines_question);
            mBtnCheckInDetailsInfo = (ImageButton)  v.findViewById(R.id.btnCheckInDetailsInfo);
            //viewCheckInHeader.setOnClickListener(this);
            viewCheckInHeader.setTag(this);
        }

        public void resetToDefault() {
            viewCheckInExpandableArea.setVisibility(View.VISIBLE);
        }


/*
        @Override
        public void onClick(final View view) {
            // If the originalExpandAreaHeight is 0 then find the height of the View being used
            // This would be the height of the cardview
            //this.setIsRecyclable(false);
            if (originalExpandAreaHeight == 0) {
                originalExpandAreaHeight = viewCheckInExpandableArea.getHeight();
            }

            final String itemId = String.valueOf(this.getLayoutPosition());
            final boolean isExpandAreaVisible =  viewCheckInExpandableArea.getVisibility() == View.VISIBLE;
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
/*
            final boolean animate = false;
            // Declare a ValueAnimator object
            if (!isExpandAreaVisible) {
                doExpand(originalExpandAreaHeight, animate);
            } else {
                doCollapse(originalExpandAreaHeight, animate);
            }*//*

        }
*/


/*        @Override
        public void onClick(View viewExpandableArea) {
            //vh.isExpandCollapsingRequested = true;
            DoctorViewHolder holder = (DoctorViewHolder) viewExpandableArea.getTag();
            holder.setIsRecyclable(false);
            final String position = String.valueOf(holder.getLayoutPosition());
            final boolean isExpandAreaVisible =  holder.viewCheckInExpandableArea.getVisibility() == View.VISIBLE;
            if(isExpandAreaVisible) {
                if(mExpandedPositions.contains(position)) {
                    mExpandedPositions.remove(position);
                }
                mCollapsedPositions.add(position);
            }else {
                if(mCollapsedPositions.contains(position)){
                    mCollapsedPositions.remove(position);
                }
                mExpandedPositions.add(position);
            }
            //notifyItemChanged(vh.getLayoutPosition());
            notifyDataSetChanged();
        }
        */
    }





    public interface IRecyclerItemToggleListener{
        void onItemToggled(int position);
    }
    
    
    public void addEventListener(IRecyclerItemToggleListener listener){
        mListener = listener;
    }
    
    private final Context mContext;

    @Override
    protected void onContentChanged() {
        lastPosition = -1;
    }
    
    public CheckInRecyclerCursorAdapter(Cursor cursor, Context context) {
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
    public void onViewRecycled(CheckInViewHolder holder) {
        super.onViewRecycled(holder);
        holder.resetToDefault();
        Log.d(TAG,String.format("onViewRecycled=> ItemId:%d OldPosition:%d CurrentPosition:%d. ExpandedArea:%s",
                holder.getItemId(),holder.getOldPosition(), holder.getLayoutPosition(),traceVisibility(holder.viewCheckInExpandableArea)));
    }

    @Override
    public void onBindViewHolder(final CheckInViewHolder holder, Cursor cursor) {
       
        Animation slide = AnimationUtils.loadAnimation(mContext, (cursor.getPosition() > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        //holder.itemView.startAnimation(slide);
        lastPosition = holder.getLayoutPosition();
        //handleTogglingExpandedArea(holder);
        this.handleTogglingExpandedAreaHolder(holder);
        final CheckIn checkIn = CheckIn.getByUnitId(cursor.getString(cursor.getColumnIndex(ActiveContract.CHECKIN_COLUMNS.UNIT_ID)));

        final PainLevel painLevel = Enum.valueOf(PainLevel.class,cursor.getString(cursor.getColumnIndex(ActiveContract.CHECKIN_COLUMNS.PAIN_LEVEL)));
        final FeedStatus feedStatus =  Enum.valueOf(FeedStatus.class, cursor.getString(cursor.getColumnIndex(ActiveContract.CHECKIN_COLUMNS.FEED_STATUS)));
        holder.vCheckInStatus.setText(painLevel + " - " + feedStatus);// + String.format(" (%s)",lastPosition));
        if(checkIn != null) {
            //if(holder.mListView != null) {
                holder.mListView.setAdapter(new MedicationQuestionAdapter(mContext, MedicationQuestionItem.makeItemByCheckinQuestions(checkIn.getItemsQuestion()), false));
            //}
            holder.vCheckInTime.setText(mContext.getString(R.string.text_checkin_submission) +
                    " " + DateTimeUtils.convertEpochToHumanTime(checkIn.getIssueDateTime(), Constants.TIME.DEFAULT_FORMAT));

            /*
            final Patient patient = Patient.getByMedicalNumber(mPatientOwner.getMedicalRecordNumber());
            card.mainHeader = patient.getFirstName() + " " + patient.getLastName() + " " + getString(R.string.checkin_header);


            switch (checkIn.getIssuePainLevel()){
                case UNKNOWN:
                case WELL_CONTROLLED:
                    card.resourceIdAlertIcon = R.drawable.ic_alert_green;
                    card.resourceIdMainTextColor = getResources().getColor(R.color.card_background_green);
                    break;
                case MODERATE:
                    card.resourceIdAlertIcon = R.drawable.ic_alert_orange;
                    card.resourceIdMainTextColor = getResources().getColor(R.color.card_background_orange);
                    break;
                case SEVERE:
                    card.resourceIdAlertIcon = R.drawable.ic_alert_red;
                    card.resourceIdMainTextColor = getResources().getColor(R.color.card_background_red);
                    break;
            }
            */
        }else{
            //card.resourceIdAlertIcon = R.drawable.ic_alert_green;
        }
        switch(painLevel)
        {
            case MODERATE:
                holder.vCheckInStatus.setTextColor(CheckInUtils.SM_CHECKIN_COLORS[1]);
                break;
            case SEVERE:
                holder.vCheckInStatus.setTextColor(CheckInUtils.SM_CHECKIN_COLORS[2]);
                break;
            case WELL_CONTROLLED:
                holder.vCheckInStatus.setTextColor(CheckInUtils.SM_CHECKIN_COLORS[0]);
            case UNKNOWN:
                break;
        }
        Log.d(TAG, String.format("onBindViewHolderCursor=> CheckInID:%d. CursorPosition:%d. CurrentPosition:%d. ExpandedArea:%s",
                checkIn == null ? -1 : checkIn.getId(), lastPosition, holder.getLayoutPosition(), traceVisibility(holder.viewCheckInExpandableArea)));
        

        //card.resourceIdThumb=R.drawable.ic_check_in;
    }


    // Create new views (invoked by the layout manager)
    @Override
    public CheckInViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_google_cardview_checkins, parent, false);
        // set the view's size, margins, paddings and layout parameters        
        v.setClickable(true);

        CheckInViewHolder holder = new CheckInViewHolder(v);
        holder.viewCheckInHeader.setOnClickListener(this);
        Log.d(TAG, String.format("onCreateViewHolder=> ItemId:%d OldPosition:%d CurrentPosition:%d. ExpandedArea:%s",
                holder.getItemId(), holder.getOldPosition(), holder.getLayoutPosition(), traceVisibility(holder.viewCheckInExpandableArea)));
        return holder;
    }
}
