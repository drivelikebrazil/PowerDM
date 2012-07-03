package com.drivelikebrazil.powerdm;

import android.animation.AnimatorInflater;
import android.animation.LayoutTransition;
import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import java.util.ArrayList;
import android.widget.*;
import android.view.View;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.util.DisplayMetrics;
import android.gesture.*;

public class PowerDMActivity extends Activity {

	private int SWIPE_MIN_DISTANCE;
	private int SWIPE_MAX_OFF_PATH;
	private int SWIPE_THRESHOLD_VELOCITY;
	private ListView tview;
	private LayoutTransition deleteTransition;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        deleteTransition = new LayoutTransition();
        deleteTransition.setAnimator(LayoutTransition.APPEARING, AnimatorInflater.loadAnimator(this, R.animator.expand_in));
        deleteTransition.setAnimator(LayoutTransition.DISAPPEARING, AnimatorInflater.loadAnimator(this, R.animator.shrink_out));
        deleteTransition.setDuration(50);
		
		DisplayMetrics dm = getResources().getDisplayMetrics();
		SWIPE_MIN_DISTANCE = (int)(20.0f * dm.densityDpi / 160.0f + 0.5);
		SWIPE_MAX_OFF_PATH = (int)(20.0f * dm.densityDpi / 160.0f + 0.5);
		SWIPE_THRESHOLD_VELOCITY = (int)(50.0f * dm.densityDpi / 160.0f + 0.5);		
		
		ArrayList<String> testArray = new ArrayList<String>();
		
		testArray.add("This is a test\n Two Lines\n ThreeLines\n4");
		testArray.add("Another");
		testArray.add("This too");
		
		InitListAdapter tadapt = new InitListAdapter(testArray);
		
		tview = (ListView) findViewById(R.id.listView_initiative);
		
		tview.setOnItemClickListener(listItemClick);
		tview.setAdapter(tadapt);
    }
	
	private ListView.OnItemClickListener listItemClick = new ListView.OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			String msg = ((InitListAdapter)tview.getAdapter()).getItem(position);
			
			Toast.makeText(PowerDMActivity.this, msg, Toast.LENGTH_SHORT).show();
		}
	};
	
	private static class InitItemHolder
	{
		View row;
		RelativeLayout rowRoot;
		TextView nameTextView;
		Button deleteButton;
		GestureDetector gDetector;
		SingleTapListener sTapListener;
		
		int minSwipeDistance;
		int maxOffPath;
		int velocityThreshold;
		int position;
		public interface SingleTapListener
		{
			public void onSingleTap(View v, int position);
		}
		
		InitItemHolder(View row, int position, int minSwipeDist, int maxOff, int velThresh, Context c)
		{
			this.row = row;
			rowRoot = (RelativeLayout) row.findViewById(R.id.rLayout_initRow);
			nameTextView = (TextView) row.findViewById(R.id.textView_initRowName);
			deleteButton = (Button) row.findViewById(R.id.button_initRowDelete);
			gDetector = new GestureDetector(new HorizontalSwipeDetector());
			this.position = position;
			sTapListener = null;
			
			minSwipeDistance = minSwipeDist;
			maxOffPath = maxOff;
			velocityThreshold = velThresh;
		}
		
		public void setLayoutTransition(LayoutTransition lt)
		{
			rowRoot.setLayoutTransition(lt);
		}
		
		public void setSingleTapListener(SingleTapListener l)
		{
			sTapListener = l;
		}
		
		public void populateFrom(String item)
		{
			nameTextView.setText(item);
			
			
			View.OnTouchListener gListener = new View.OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					if(gDetector.onTouchEvent(event)) {
						return true;
					}

					return false;
				}
			};

			row.setOnTouchListener(gListener);
		}
		
		private class HorizontalSwipeDetector extends GestureDetector.SimpleOnGestureListener
		{
			@Override
			public boolean onSingleTapUp(MotionEvent e)
			{
				return true;
			}
			
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e)
			{
				 if(sTapListener != null)
				 {
					 sTapListener.onSingleTap(row, position);
				 }
				 
				 return true;
			}
			
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
			{
				try{
					if(e1.getX() - e2.getX() > minSwipeDistance)
					{
						if(deleteButton.getVisibility() == Button.INVISIBLE)
							deleteButton.setVisibility(Button.VISIBLE);
					}
					else if(e2.getX() - e1.getX() > minSwipeDistance)
					{
						if( deleteButton.getVisibility() == Button.VISIBLE)
							deleteButton.setVisibility(Button.INVISIBLE);
					}
				} 
				catch (Exception e)
				{
					//Nothing for now
				}

				return false;
			}

			@Override 
			public boolean onDown(MotionEvent e)
			{
				return true;
			}
		}
	}
	
	
	
	private class InitListAdapter extends ArrayAdapter<String>
	{
		public InitListAdapter(ArrayList<String> itemArray)
		{
			super(PowerDMActivity.this, R.layout.rowlayout_initiative, itemArray);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View row = convertView;
			InitItemHolder holder = null;
			
			if(row == null)
			{
				LayoutInflater inflater = LayoutInflater.from(this.getContext());
				
				row = inflater.inflate(R.layout.rowlayout_initiative, parent, false);
				holder = new InitItemHolder(row, position, SWIPE_MIN_DISTANCE, SWIPE_MAX_OFF_PATH, SWIPE_THRESHOLD_VELOCITY, this.getContext());
				row.setTag(holder);
				holder.setLayoutTransition(deleteTransition);
				
				holder.setSingleTapListener(new InitItemHolder.SingleTapListener(){
					public void onSingleTap(View v, int position)
					{
						tview.performItemClick(v,position,InitListAdapter.this.getItemId(position));
					}
				});
			}
			else
			{
				holder = (InitItemHolder) row.getTag();
			}
			
			holder.populateFrom(this.getItem(position));
			
			return row;
		}
	}
}
